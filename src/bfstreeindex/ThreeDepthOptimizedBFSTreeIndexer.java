package bfstreeindex;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;

import util.BFSData;
import util.BFSRunner;
import util.Utils;
import util.Utils.BFSColor;

public class ThreeDepthOptimizedBFSTreeIndexer {
  static BFSRunner originalBFS;
  static BFSData secondBFSData;
  static int numLevelOneNbrsSkipped = 0;
  static int numLevelTwoNbrsSkipped = 0;
  static int numLevelThreeNbrsSkipped = 0;
  static int[][] graph;
  public static void main(String[] args) throws NumberFormatException, IOException {
    graph = Utils.getGraph(Utils.GPS_DATA_DIR + "/soc-Epinions1-u-n.txt");
    Utils.permuteGraph(graph);
    int numVertices = graph.length;
    System.out.println("numVertices: " + numVertices);
    int source = Utils.getMaxDegreeVertex(graph);
    originalBFS = new BFSRunner(graph);
    originalBFS.runBFS(source);
    originalBFS.summarizeLevelData();
//    System.out.println("DUMPING ORIGINAL BFS RESULTS");
//    originalBFS.dumpBFSRunnerData();
//    System.out.println("END OF DUMPING ORIGINAL BFS RESULTS");
    
    ArrayBlockingQueue<Integer> optBFSQueue = new ArrayBlockingQueue<Integer>(numVertices);
    optBFSQueue.add(source);
    secondBFSData = new BFSData(numVertices, source);
    secondBFSData.initializeBFSData();
    secondBFSData.bfsColor[source] = BFSColor.VISITED;
    secondBFSData.bfsLevel[source] = 0;
    int nextV;
    int currentLevel = 0;
    int[] nbrsNbrs = null;
    int[] numVerticesInEachLevel = new int[] {0, 0, 0, 0};
    int numVerticesInTree = 0;
    while (!optBFSQueue.isEmpty()) {
      nextV = optBFSQueue.remove();
      currentLevel = secondBFSData.bfsLevel[nextV];
//      System.out.println("nextV: " + nextV + " currentLevel: " + currentLevel);
      if (currentLevel == 3) {
        break;
      }
      // See if any of the neighbors can be put into the BFS tree
      for (int nbr : graph[nextV]) {
        if (secondBFSData.bfsColor[nbr] == BFSColor.UNVISITED) {
          nbrsNbrs = graph[nbr];
          if ((currentLevel == 0 && isValidFirstLevelVertex(nbrsNbrs)) ||
            (currentLevel == 1 && isValidSecondLevelVertex(nbr, nbrsNbrs)) ||
            (currentLevel == 2 && isValidThirdLevelVertex(nbr, nbrsNbrs))) {
            secondBFSData.bfsColor[nbr] = BFSColor.VISITED;
            secondBFSData.bfsLevel[nbr] = currentLevel + 1;
            secondBFSData.bfsParent[nbr] = nextV;
            numVerticesInEachLevel[currentLevel + 1]++;
            numVerticesInTree++;
            if ((numVerticesInTree % 50000) == 0) {
              dumpNumVerticesInEachLevel(numVerticesInEachLevel, numVerticesInTree);
            }
            optBFSQueue.add(nbr);
          } else {
            secondBFSData.bfsColor[nbr] = BFSColor.VISITED_NOT_CONSISTENT;
          }
        }
      }
    }
//    secondBFSData.dumpBFSData();
    dumpNumVerticesInEachLevel(numVerticesInEachLevel, numVerticesInTree);
  }

  public static void dumpNumVerticesInEachLevel(int[] numVerticesInEachLevel, int numVerticesInTree) {
    System.out.println("numVerticesInTree: " + numVerticesInTree);
    System.out.println("Level 1: " + numVerticesInEachLevel[1] +
      "\nLevel 2: " + numVerticesInEachLevel[2] +
      "\nLevel 3: " + numVerticesInEachLevel[3]);
  }

  // Third level nbrs are in the tree if in the original tree they:
  // (1) do not have a direct edge to another level 3 vertex;
  // (2) do not have more than one parent;
  // (3) do not have a level 4 nbr that has more than one parent; and
  // (4) do not have a level 5 nbr-of-nbr with more than one parent.
  private static boolean isValidThirdLevelVertex(int nbr, int[] nbrsOfNbrs) {
//    System.out.println("IS VALID THIRD LEVEL VERTEX CALLED. nbr: " + nbr);
    boolean isValid = true;
    if (hasAnEdgeToLevel(nbrsOfNbrs, originalBFS.bfsData.bfsLevel, 3)) {
//      System.out.println("not a valid level 3 vertex because nbr " + nbr
//        + " has a direct edge to another level 3 vertex.");
      isValid = false;
    }
    if (isValid) {
//      && (originalBFS.numParents[nbr] > 1)) {
//      System.out.println("not a valid level 3 vertex because nbr " + nbr
//        + " has more than one parent.");
      isValid = false;
    }
    if (isValid) {
      for (int nbrofnbr: nbrsOfNbrs) {
        if ((originalBFS.bfsData.bfsLevel[nbrofnbr] == 4 && 
          originalBFS.numParents[nbrofnbr] > 1)) {
          isValid = false;
//          System.out.println("not a valid level 3 vertex because nbrofnbr " + nbrofnbr
//            + " is level 4 and has more than one parent");
          break;
        }
        if (!isValid) { break; }
        for (int nbrofnbrofnbr : graph[nbrofnbr]) {
          if ((originalBFS.bfsData.bfsLevel[nbrofnbrofnbr] == 5 && 
            originalBFS.numParents[nbrofnbrofnbr] > 1)) {
            isValid = false;
//            System.out.println("not a valid level 3 vertex because nbrofnbrofnbr " + nbrofnbrofnbr
//              + " is level 5 and has more than one parent.");

            break;
          }
        }
        if (!isValid) { break; }
      }
    }
    if (!isValid) {
      if ((numLevelThreeNbrsSkipped % 5000) == 0) {
        System.out.println("numLevelThreeNbrsSkipped: " + numLevelThreeNbrsSkipped);
      }
      numLevelThreeNbrsSkipped++;
    }
    return isValid;
  }

  // Second level nbrs are in the tree if in the original tree they:
  // (1) do not have a direct edge to another second level nbr; and
  // (2) have only one parent.
  private static boolean isValidSecondLevelVertex(int nbr, int[] nbrsNbrs) {
//    System.out.println("IS VALID SECOND LEVEL VERTEX CALLED. nbr: " + nbr);
    if (hasAnEdgeToLevel(nbrsNbrs, originalBFS.bfsData.bfsLevel, 2)
      || originalBFS.numParents[nbr] > 1) {
      if ((numLevelTwoNbrsSkipped % 5000) == 0) {
        System.out.println("numLevelTwoNbrsSkipped: " + numLevelTwoNbrsSkipped);
      }
      numLevelTwoNbrsSkipped++;
      return false;
    }
    return true;
  }

  // First level nbrs are in the tree iff: they do not have an edge to another level 1 vertex in
  // the bfs tree we are constructing.
  private static boolean isValidFirstLevelVertex(int[] nbrsNbrs) {
//    System.out.println("IS VALID FIRST LEVEL VERTEX CALLED.");
    if (hasAnEdgeToLevel(nbrsNbrs, secondBFSData.bfsLevel, 1)) {
      if ((numLevelOneNbrsSkipped % 5000) == 0) {
        System.out.println("numLevelOneNbrsSkipped: " + numLevelOneNbrsSkipped);
      }
      numLevelOneNbrsSkipped++;
      return false;
    }
    return true;
  }

  private static boolean hasAnEdgeToLevel(int[] nbrsNbrs, int[] bfsLevel, int level) {
    for (int nbrOfNbr : nbrsNbrs) {
      if (bfsLevel[nbrOfNbr] == level) {
        return true;
      }
    }
    return false;
  }  
}
