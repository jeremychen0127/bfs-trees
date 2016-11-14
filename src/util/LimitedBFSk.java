package util;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.ArrayBlockingQueue;

public class LimitedBFSk {
  public static void main(String[] args) throws NumberFormatException, IOException {
    String graphFile = args[0];
    int numHighDegreeBFSTrees = Integer.parseInt(args[1]);
    int numRandomBFSTrees = Integer.parseInt(args[2]);
    int numBFSTrees = numHighDegreeBFSTrees + numRandomBFSTrees;
    int numTrials = Integer.parseInt(args[3]);
    int kForLimitedBFS = Integer.parseInt(args[4]);
    String neighborSelectionMethod = args[5];
    if (!Utils.contains(Constants.ALLOWED_NEIGHBOR_FILTER, neighborSelectionMethod)) {
      System.out.println("Allowed options for neighborSelectionMethod argument:");
      System.out.println(Constants.RANDOM + ", " + Constants.PARENT_FREQ + ", " + Constants.DEGREE);
      return;
    }
    boolean isDirectedGraph = Boolean.parseBoolean(args[6]);
    System.out.println("graphFile:" + graphFile);
    System.out.println("numHighDegreeBFSTrees:" + numHighDegreeBFSTrees);
    System.out.println("numRandomBFSTrees:" + numRandomBFSTrees);
    System.out.println("numTrials: " + numTrials);
    System.out.println("kForLimitedBFS: " + kForLimitedBFS);
    System.out.println("neighborSelectionMethod: " + neighborSelectionMethod);
    System.out.println("directed: " + isDirectedGraph);
    long startTime = System.currentTimeMillis();
    int[][] graph = Utils.getGraph(graphFile);
    int[][] revGraph = null; //new int[graph.length][];
    if (isDirectedGraph) {
      revGraph = Utils.reverseGraph(graph);
    }
    long endTime = System.currentTimeMillis();
    System.out.println("TIME TAKEN TO PARSE THE GRAPH: " + ((endTime - startTime)/1000));

    SimpleBFSData[] bfsTrees = {};
    SimpleBFSData[] revBfsTrees = {};
    if (neighborSelectionMethod.equals(Constants.PARENT_FREQ)) {
      Pair.PairComparator degreeComparator = new Pair.PairComparator();
      Pair[] idDegrees = new Pair[graph.length];
      for (int i = 0; i < graph.length; ++i) {
        idDegrees[i] = new Pair(i, graph[i].length);
      }
      startTime = System.currentTimeMillis();
      Arrays.sort(idDegrees, degreeComparator);
      endTime = System.currentTimeMillis();
      System.out.println("TIME TAKEN TO SORT DEGREES: " + ((endTime - startTime) / 1000));

      bfsTrees = new SimpleBFSData[numHighDegreeBFSTrees + numRandomBFSTrees];
      revBfsTrees = new SimpleBFSData[numHighDegreeBFSTrees + numRandomBFSTrees];
      startTime = System.currentTimeMillis();
      for (int i = 0; i < numHighDegreeBFSTrees; ++i) {
        System.out.println("Source of High Degree BFS Tree vertex: " + idDegrees[idDegrees.length - (i + 1)].id);
        bfsTrees[i] = BFSImplementations.getBFSTree(graph, idDegrees[idDegrees.length - (i + 1)].id);
        if (isDirectedGraph) {
          revBfsTrees[i] = BFSImplementations.getBFSTree(revGraph, idDegrees[idDegrees.length - (i + 1)].id);
        }
      }
    }

    Random random = new Random(0);

    if (neighborSelectionMethod.equals(Constants.PARENT_FREQ)) {
      for (int i = 0; i < numRandomBFSTrees; ++i) {
        int nextSrc = random.nextInt(graph.length);
        if (graph[nextSrc].length == 0) {
          i--;
          continue;
        } else {
          System.out.println("Source of Random BFS Tree vertex: " + nextSrc);
          bfsTrees[numHighDegreeBFSTrees + i] = BFSImplementations.getBFSTree(graph, nextSrc);
          if (isDirectedGraph) {
            revBfsTrees[numHighDegreeBFSTrees + i] = BFSImplementations.getBFSTree(revGraph, nextSrc);
          }
        }
      }
      endTime = System.currentTimeMillis();
      int totalNumBFSTrees = numHighDegreeBFSTrees + numRandomBFSTrees;
      System.out.println("TIME TAKEN TO CONSTRUCT" + totalNumBFSTrees
              + " BFS TREES: " + ((endTime - startTime) / 1000));
      System.out.println("AVG TIME TAKEN TO CONSTRUCT 1 BFS TREE: "
              + ((endTime - startTime) / (1000 * totalNumBFSTrees)));
    }

    // parentHistogram.get(vertexId).get(neighborId) is the count
    ArrayList<TreeMap<Integer, Integer>> parentHistogram = null;
    if (neighborSelectionMethod.equals(Constants.PARENT_FREQ)) {
      parentHistogram = Utils.getParentHistogram(graph, bfsTrees);
    }

    int[][] kNeighborsGraph;
    if (neighborSelectionMethod.equals(Constants.RANDOM)) {
      kNeighborsGraph = Utils.getRandomKNeighborsGraph(graph, kForLimitedBFS);
    } else if (neighborSelectionMethod.equals(Constants.PARENT_FREQ)) {
      kNeighborsGraph = Utils.getKParentFreqNeighborsGraph(graph, parentHistogram, kForLimitedBFS);
    } else if (neighborSelectionMethod.equals(Constants.DEGREE)) {
      kNeighborsGraph = Utils.getKHighestDegreeNeighborsGraph(graph, kForLimitedBFS);
    } else {
      System.out.println("ERROR: Invalid neighbor selection method");
      return;
    }

    graph = null;

    int src, dst;
    long numEdgesLimitedBFSk;
    int limitedBFSkPathLength;
    int numAbleToFindPath = 0;
    long numEdgesLimitedBFSkSum = 0;
    long totalTimeForLimitedBFSk = 0;

    int[] fwBfsLevels = new int[kNeighborsGraph.length];
    int[] bwBfsLevels = new int[kNeighborsGraph.length];
    ArrayBlockingQueue<Integer> fwBfsQueue = null;
    ArrayBlockingQueue<Integer> bwBfsQueue = null;

    for (int i = 0; i < numTrials; ++i) {
      if (i > 0 && (i % 200) == 0) {
        System.out.println("Starting " + i + "th trial.");
      }

      // Generates random queries from s to t with s != t
      src = random.nextInt(kNeighborsGraph.length);
      dst = random.nextInt(kNeighborsGraph.length);
      while (src == dst) {
        dst = random.nextInt(kNeighborsGraph.length);
      }

      Utils.BiDirBFSInit(fwBfsLevels, bwBfsLevels);
      fwBfsQueue = new ArrayBlockingQueue<Integer>(kNeighborsGraph.length);
      bwBfsQueue = new ArrayBlockingQueue<Integer>(kNeighborsGraph.length);

      startTime = System.nanoTime();
      limitedBFSkPathLength = Utils.getSSSDSPBiDirBFS(kNeighborsGraph, fwBfsLevels, bwBfsLevels, fwBfsQueue, bwBfsQueue, src, dst);
      endTime = System.nanoTime();
      totalTimeForLimitedBFSk += (endTime - startTime);
      numEdgesLimitedBFSk = Utils.numEdgesTraversed;

      if (limitedBFSkPathLength > 0) {
        numEdgesLimitedBFSkSum += numEdgesLimitedBFSk;
        numAbleToFindPath++;
      }
    }

    System.out.println("totalTimeForLimitedBFSk: " + totalTimeForLimitedBFSk);
    System.out.println("Avg #Edges Traversed of BFS-k: " + (1.0 * numEdgesLimitedBFSkSum / numAbleToFindPath));

  }
}
