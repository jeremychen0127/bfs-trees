package bfstreeindex;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Random;

import bfstreeindex.bfs.MultipleBFSMaster;
import util.BFSImplementations;
import util.Pair;
import util.SimpleBFSData;
import util.Utils;
import util.Pair.PairComparator;
import util.graph.AbstractGraph;
import util.graph.CRSGraph;
import util.graph.LargeCRSGraph;
import util.graph.DoubleArrayGraph;

public class MultipleBFSTreesIndexTester {

//  static String IS_CONSISTENT_FILES_DIR = "/Users/semihsalihoglu/Desktop/research/"
//    + "waterloo/shortest-paths/bfs-tree-index-data/is-consistent-files";
  public volatile static int numOutstandingRunners = 0;
  private static NewConsistencySetRunner newConsistencySetRunner;
  // TODO(semih): Have 200 isConsistent arrays and see how much you can boost
  // the success percentages
  public static void main(String[] args) throws NumberFormatException, IOException,
    InterruptedException {
    // int numIsConsistentFilesToConstruct = 500;
    // TODO: ConstructingTreesFromBFSTrees is BUGGY
    // boolean constructIsConsistentTreesByBFSOnTrees = false;
    String graphName = args[0];
    String graphFileDir = args[1];
    String isConsistentFilesDir = args[2];
    int numHighDegreeBFSTrees = Integer.parseInt(args[3]);
    int numRandomBFSTrees = Integer.parseInt(args[4]);
    int numConcurrentIndices = Integer.parseInt(args[5]);
    int parallelismLevel = Integer.parseInt(args[6]);
    int numIsConsistentFiles = Integer.parseInt(args[7]);
    GRAPH_TYPE graphType = null;
    String graphTypeFlag = args[8];
    if ("doublearray".equals(graphTypeFlag)) {
      graphType = GRAPH_TYPE.DOUBLE_ARRAY;
    } else if ("crs".equals(graphTypeFlag)) {
      graphType = GRAPH_TYPE.CRS;
    } else if ("largecrs".equals(graphTypeFlag)) {
      graphType = GRAPH_TYPE.LARGE_CRS;
    }
    System.out.println("graphName: " + graphName);
    System.out.println("graphFileDir: " + graphFileDir);
    System.out.println("isConsistentFilesDir: " + isConsistentFilesDir);
    System.out.println("numHighDegreeBFSTrees: " + numHighDegreeBFSTrees);
    System.out.println("numRandomBFSTrees: " + numRandomBFSTrees);
    System.out.println("numConcurrentIndices: " + numConcurrentIndices);
    System.out.println("bfsParallelismLevel: " + parallelismLevel);
    System.out.println("numIsConsistentFiles: " + numIsConsistentFiles);
    System.out.println("graphType: " + graphType);
    System.out.println(graphName);
    int[][] graph = Utils.getGraph(graphFileDir + "/" + graphName);

    PairComparator degreeComparator = new PairComparator();
    Pair[] idDegrees = new Pair[graph.length];
    for (int i = 0; i < graph.length; ++i) {
      idDegrees[i] = new Pair(i, graph[i].length);
    }
    Arrays.sort(idDegrees, degreeComparator);
    SimpleBFSData[] bfsTrees = new SimpleBFSData[numHighDegreeBFSTrees + numRandomBFSTrees];
    long startTime = System.currentTimeMillis();
    for (int i = 0; i < numHighDegreeBFSTrees; ++i) {
      System.out.println("Source of BFS Tree vertex: " + idDegrees[idDegrees.length - (i + 1)].id);
      bfsTrees[i] = BFSImplementations.getBFSTree(graph, idDegrees[idDegrees.length - (i + 1)].id);
    }
    /* we always fix the source so that multiple runs indices can be combined */
    Random random = new Random(0);
    for (int i = 0; i < numRandomBFSTrees; ++i) {
      int nextSrc = random.nextInt(graph.length);
      if (graph[nextSrc].length == 0) {
        i--;
        continue;
      } else {
        bfsTrees[numHighDegreeBFSTrees + i] = BFSImplementations.getBFSTree(graph, nextSrc);
      }
    }
    System.out.println("Done indexing bfs trees: " + (System.currentTimeMillis() - startTime));
    AbstractGraph abstractGraph = null;
    switch (graphType) {
    case DOUBLE_ARRAY: 
      System.out.println("CONSTRUCTING DOUBLE_ARRAY GRAPH.");
      abstractGraph = new DoubleArrayGraph(graph);
      break;
    case CRS:
      System.out.println("CONSTRUCTING CRSGraph.");
      abstractGraph = new CRSGraph(graph);
      break;
    case LARGE_CRS:
      System.out.println("CONSTRUCTING LargeCRSGraph.");
      abstractGraph = new LargeCRSGraph(graph);
      break;
    default:
      System.out.println("CONSTRUCTING DEFAULT GRAPH TYPE of DOUBLE_ARRAY");
      abstractGraph = new DoubleArrayGraph(graph);
      break;
    }
    for (int j = 0; j < numIsConsistentFiles; ++j) {
      while (numOutstandingRunners >= numConcurrentIndices) {
        System.out.println("Sleeping for one minute second...");
        Thread.sleep(60000);
      }
      numOutstandingRunners++;
      System.out.println("Starting a new ConsistencyRunner File");
      byte[] consistencyArray = new MultipleBFSMaster(abstractGraph, bfsTrees, parallelismLevel,
        j /* consistencyCheckerIndex */).checkConsistencies();
      Utils.saveIsConsistentFile(consistencyArray, isConsistentFilesDir, graphName,
        random.nextInt() /* consistencyFileIndex */);
//      newConsistencySetRunner = new NewConsistencySetRunner(graph, bfsTrees, idDegrees,
//        j /* consistencyCheckerIndex */, graphName, isConsistentFilesDir, parallelismLevel);
//      new Thread(newConsistencySetRunner).start();
    }
  }

  public static void addEdgeToGraph(SimpleBFSData bfsTree, int[][] graph, int[] degrees, int src,
    int dest) {
    if (src == -1 || dest == -1) { return; }
    int srcDegree = degrees[src];
    if (graph[src].length < (srcDegree + 1)) {
      int newDegreeSize = Math.max(graph[src].length + 1,
        (int) Math.round(graph[src].length*1.2));
      int[] newNeighbors = new int[newDegreeSize];
      System.arraycopy(graph[src], 0, newNeighbors, 0, graph[src].length);
      graph[src] = newNeighbors;
    }
    graph[src][srcDegree] = dest;
    degrees[src]++;
  }

  public static int countExistingConsistencyFiles(String isConsistentFilesDir, String graphName)
     throws IOException {
    DirectoryStream<Path> files = Files.newDirectoryStream(Paths.get(isConsistentFilesDir));
    int numExistingConsistencyFiles = 0;
    for (Path file : files) {
      String isConsistentFileName = file.getName(file.getNameCount() - 1).toString();
      if (isConsistentFileName.contains(graphName)) {
        System.out.println("Existing consistency file: " + isConsistentFileName);
        numExistingConsistencyFiles++;
      }
    }
    return numExistingConsistencyFiles;
  }
  
  public static int loadIsConsistencyFiles(String isConsistentFilesDir, int[][] isConsistencyArrays,
    String graphName) throws IOException {
    DirectoryStream<Path> files = Files.newDirectoryStream(Paths.get(isConsistentFilesDir));
    int numLoadedIsConsistentFiles = 0;
    int totalNumConsistent = 0;
    for (Path file : files) {
      String isConsistentFileName = file.getName(file.getNameCount() - 1).toString();
      if (isConsistentFileName.contains(graphName)) {
        System.out.println("Reading isConsistent File: " + isConsistentFileName);
        BufferedReader br = Utils.getBufferedReader(file.toAbsolutePath().toString());
        String strLine = br.readLine();
        String[] split = strLine.split("\\s+");
        int numIsConsistent = 0;
        for (int i = 0; i < split.length; ++i) {
          int isConsistent = Integer.parseInt(split[i]);
          if (isConsistent == 1) { numIsConsistent++; }
          isConsistencyArrays[numLoadedIsConsistentFiles][i] = isConsistent;
        }
        System.out.println("numIsConsistent: " + numIsConsistent);
        totalNumConsistent += numIsConsistent;
        numLoadedIsConsistentFiles++;
      }
    }
    System.out.println("avgTotalNumIsConsistent: " + (totalNumConsistent / numLoadedIsConsistentFiles));
    return numLoadedIsConsistentFiles;
  }
  
  public enum GRAPH_TYPE {
    DOUBLE_ARRAY,
    CRS,
    LARGE_CRS
  }
}

// UNUSED CODE
//private static int[][] constructGraphFromBFSTree(SimpleBFSData bfsTree) {
//int[][] graph = new int[bfsTree.bfsLevel.length][];
//int[] degrees = new int[bfsTree.bfsLevel.length];
//for (int i = 0; i < bfsTree.bfsLevel.length; ++i) {
//degrees[i] = 0;
//graph[i] = new int[0];
//}
//for (int src = 0; src < bfsTree.bfsParent.length; ++src) {
//addEdgeToGraph(bfsTree, graph, degrees, src, bfsTree.bfsParent[src]);
//addEdgeToGraph(bfsTree, graph, degrees, bfsTree.bfsParent[src], src);
//}
//return graph;
//}

//private static boolean checkConsistencyByCheckingBFSTrees(int id, int[][] graph,
//  int[] consistencyArray, int[][][] bfsTreesAsGraphs) {
//  SimpleBFSData actualDistances = Utils.getBFSTree(graph, id);
//  System.out.println("Priting Actual Distances");
//  for (int i = 0; i < 10; ++i) {
//    System.out.print(actualDistances.bfsLevel[i] + " ");
//  }
//  System.out.println();
//  int[] distancesOnBFSTrees = new int[graph.length];
//  for (int i = 0; i < graph.length; ++i) {
//    distancesOnBFSTrees[i] = (i != id) ? Integer.MAX_VALUE : 0;
//  }
//  for (int i = 0; i < bfsTreesAsGraphs.length; ++i) {
//    updateDistancesOnBFSTree(bfsTreesAsGraphs[i], distancesOnBFSTrees, id);
//  }
//  System.out.println("Printing Other Distances");
//  for (int i = 0; i < 10; ++i) {
//    System.out.print(distancesOnBFSTrees[i] + " ");
//  }
//  System.out.println();
//  for (int i = 0; i < actualDistances.bfsLevel.length; ++i) {
//    if (actualDistances.bfsLevel[i] > -1) {
//      if (actualDistances.bfsLevel[i] != distancesOnBFSTrees[i]) {
//        return false;
//      }
//    } else {
//      if (distancesOnBFSTrees[i] != Integer.MAX_VALUE) {
//        return false;
//      }
//    }
//  }
//  System.out.println("Returning true");
//  return true;
//}

//private static void updateDistancesOnBFSTree(int[][] bfsTreesAsGraph,
//  int[] distancesOnBFSTrees, int id) {
//  SimpleBFSData distancesOnBfsTree = Utils.getBFSTree(bfsTreesAsGraph, id);
//  for (int i = 0; i < distancesOnBFSTrees.length; ++i) {
//    distancesOnBFSTrees[i] = Math.min(distancesOnBFSTrees[i], distancesOnBfsTree.bfsLevel[i]);
//  }
//}
