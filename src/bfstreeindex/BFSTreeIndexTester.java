package bfstreeindex;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;

import util.Utils;
import util.Utils.BFSColor;

public class BFSTreeIndexTester {
  static int[][] graph;
  static BFSColor[] globalBFSColors = null;
  static int[] globalBFSLevel = null;
  static int[] globalBFSParent = null;
  static ArrayBlockingQueue<Integer> localBFSQueue = null;
  static int[] localBFSLevel = null;
  static BFSColor[] localBFSColors = null;
  public static void main(String[] args) throws NumberFormatException, IOException {
    graph = Utils.getGraph(Utils.GPS_DATA_DIR + "/soc-Epinions1-u-n.txt");
    Utils.permuteGraph(graph);
    int numVertices = graph.length;
    System.out.println("numVertices: " + numVertices);
    int source = Utils.getMaxDegreeVertex(graph);
    int maxNumVerticesInTree = -1;
//    for (int source = 0; source < numVertices; ++source) {
      globalBFSColors = new BFSColor[numVertices];
      globalBFSLevel = new int[numVertices];
      globalBFSParent = new int[numVertices];
      for (int i = 0; i < globalBFSColors.length; ++i) {
        globalBFSColors[i] = BFSColor.UNVISITED;
        globalBFSLevel[i] = -1;
        globalBFSParent[i] = -1;
      }
      ArrayBlockingQueue<Integer> globalBFSQueue = new ArrayBlockingQueue<Integer>(numVertices);
      localBFSQueue = new ArrayBlockingQueue<Integer>(numVertices);
      localBFSLevel = new int[graph.length];
      localBFSColors = new BFSColor[graph.length];
      globalBFSQueue.add(source);
      globalBFSColors[source] = BFSColor.VISITED;
      globalBFSLevel[source] = 0;
      int currentVertex = -1;
      int currentVertexLevel = -1;
      int numVerticesInGlobalBFSTree = 1;
      while (!globalBFSQueue.isEmpty()) {
        currentVertex = globalBFSQueue.remove();
        currentVertexLevel = globalBFSLevel[currentVertex];
        // See if any of the neighbors can be put into the BFS tree
        for (int neighbor : graph[currentVertex]) {
          if (globalBFSColors[neighbor] == BFSColor.UNVISITED) {
            // Tentatively set the parent and level
            globalBFSParent[neighbor] = currentVertex;
            globalBFSLevel[neighbor] = currentVertexLevel + 1;
            if (isVertexConsistentWithBFSTree(neighbor, numVerticesInGlobalBFSTree)) {
              globalBFSColors[neighbor] = BFSColor.VISITED;
              globalBFSQueue.add(neighbor);
              numVerticesInGlobalBFSTree++;
              if (numVerticesInGlobalBFSTree % 1000 == 0) {
                System.out.println("numVerticesInGlobalBFSTree: " + numVerticesInGlobalBFSTree
                  + " current level: " + globalBFSLevel[neighbor]);
              }
//              System.out.println("vertex: " + neighbor + " IS CONSISTENT.");
            } else {
              globalBFSColors[neighbor] = BFSColor.VISITED_NOT_CONSISTENT;
              globalBFSParent[neighbor] = -1;
              globalBFSLevel[neighbor] = -1;
//              System.out.println("vertex: " + neighbor + " IS NOT CONSISTENT.");
            }
          }
        }
      }
      maxNumVerticesInTree = Math.max(maxNumVerticesInTree, numVerticesInGlobalBFSTree);
      System.out.println("Num Vertices In Tree: " + numVerticesInGlobalBFSTree 
        + " max: " + maxNumVerticesInTree);
 //   }
      System.out.println("numVertices: " + numVertices);
      System.out.println("maxNumVerticesInTree: " + maxNumVerticesInTree);
  }

  private static boolean isVertexConsistentWithBFSTree(int vertex,
    int numVerticesInGlobalBFSTreeToVisit) {
    for (int i = 0; i < graph.length; ++i) {
      localBFSColors[i] = BFSColor.UNVISITED;
      localBFSLevel[i] = -1;
    }
    localBFSQueue.clear();
    localBFSQueue.add(vertex);
    localBFSLevel[vertex] = 0;
    localBFSColors[vertex] = BFSColor.VISITED;
    int nextVertex, actualDistance, lengthInBFSTree;
    int numVerticesInBFSTreeVisited = 0;
    while(!localBFSQueue.isEmpty()) {
      nextVertex = localBFSQueue.remove();
      actualDistance = localBFSLevel[nextVertex];
//      System.out.println("actualDistance of " + vertex + " and " + nextVertex + " is "
//        + actualDistance);
      for (int nbr : graph[nextVertex]) {
        if (BFSColor.UNVISITED == localBFSColors[nbr]) {
          if (BFSColor.VISITED == globalBFSColors[nbr]) {
            numVerticesInBFSTreeVisited++;
            lengthInBFSTree = getLenghInBFSTree(vertex, nbr);
            if (lengthInBFSTree > actualDistance + 1) {
              return false;
            }
            if (numVerticesInBFSTreeVisited == numVerticesInGlobalBFSTreeToVisit) {
//              System.out.println("Returning because visited all vertices in globalBFSTree.");
              return true;
            }
          }
          localBFSQueue.add(nbr);
          localBFSColors[nbr] = BFSColor.VISITED;
          localBFSLevel[nbr] = actualDistance + 1;
        }
      }
    }
//    System.out.println("Returning because BFS from local vertex finished.");
    return true;
  }

  private static int getLenghInBFSTree(int v, int nbr) {
//    System.out.println("Getting Lenght In BFS Tree of v: " + v + " nbr: " + nbr);
    int currentLevelOfV = globalBFSLevel[v];
    int currentParentOfV = globalBFSParent[v];
    int currentV = v;
    int levelofNbr = globalBFSLevel[nbr];
//    System.out.println("currentLevelOfV: " + currentLevelOfV + " levelofNbr: " + levelofNbr);
//    System.out.println("Starting: currentParentOfV: " + currentParentOfV);
    int length = 0;
    while (currentLevelOfV > levelofNbr) {
      currentLevelOfV--;
      currentV = currentParentOfV;
      currentParentOfV = globalBFSParent[currentParentOfV];
//      System.out.println("Updating: currentParentOfV: " + currentParentOfV);
      length++;
    }
    if (currentV == nbr) {
      return length;
    }
    int currentParentOfNbr = globalBFSParent[nbr];
    // TODO: Fix the currentV
//    System.out.println("currentParentOfV: " + currentParentOfV + " currentParentOfNbr: " + currentParentOfNbr);
    
    while(currentParentOfV != currentParentOfNbr) {
      length += 2;
      currentParentOfV = globalBFSParent[currentParentOfV];      
      currentParentOfNbr = globalBFSParent[currentParentOfNbr];
    }
//    System.out.println("currentParentOfV: " + currentParentOfV
//      + " currentParentOfNbr: " + currentParentOfNbr);
//    System.out.println("lenght of " + v + " and " + nbr + " is " + length); 
    return length + 2;
  }
}
