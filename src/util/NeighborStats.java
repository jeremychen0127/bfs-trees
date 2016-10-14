package util;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

public class NeighborStats {

  public static void main(String[] args) throws NumberFormatException, IOException {
    String graphFile = args[0];
    int numHighDegreeBFSTrees = Integer.parseInt(args[1]);
    int numRandomBFSTrees = Integer.parseInt(args[2]);
    int numBFSTrees = numHighDegreeBFSTrees + numRandomBFSTrees;
    int numTrials = Integer.parseInt(args[3]);
    boolean isDirectedGraph = Boolean.parseBoolean(args[4]);
    System.out.println("graphFile:" + graphFile);
    System.out.println("numHighDegreeBFSTrees:" + numHighDegreeBFSTrees);
    System.out.println("numRandomBFSTrees:" + numRandomBFSTrees);
    System.out.println("numTrials: " + numTrials);
    System.out.println("directed: " + isDirectedGraph);
    long startTime = System.currentTimeMillis();
    int[][] graph = Utils.getGraph(graphFile);
    int[][] revGraph = new int[graph.length][];
    if (isDirectedGraph) {
      revGraph = Utils.reverseGraph(graph);
    }
    long endTime = System.currentTimeMillis();
    System.out.println("TIME TAKEN TO PARSE THE GRAPH: " + ((endTime - startTime)/1000));
    Pair.PairComparator degreeComparator = new Pair.PairComparator();
    Pair[] idDegrees = new Pair[graph.length];
    for (int i = 0; i < graph.length; ++i) {
      idDegrees[i] = new Pair(i, graph[i].length);
    }
    startTime = System.currentTimeMillis();
    Arrays.sort(idDegrees, degreeComparator);
    endTime = System.currentTimeMillis();
    System.out.println("TIME TAKEN TO SORT DEGREES: " + ((endTime - startTime)/1000));

    SimpleBFSData[] bfsTrees = new SimpleBFSData[numHighDegreeBFSTrees + numRandomBFSTrees];
    SimpleBFSData[] revBfsTrees = new SimpleBFSData[numHighDegreeBFSTrees + numRandomBFSTrees];
    startTime = System.currentTimeMillis();
    for (int i = 0; i < numHighDegreeBFSTrees; ++i) {
      System.out.println("Source of High Degree BFS Tree vertex: " + idDegrees[idDegrees.length - (i + 1)].id);
      bfsTrees[i] = BFSImplementations.getBFSTree(graph, idDegrees[idDegrees.length - (i + 1)].id);
      if (isDirectedGraph) {
        revBfsTrees[i] = BFSImplementations.getBFSTree(revGraph, idDegrees[idDegrees.length - (i + 1)].id);
      }
    }

    Random random = new Random(0);
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
      + " BFS TREES: " + ((endTime - startTime)/1000));
    System.out.println("AVG TIME TAKEN TO CONSTRUCT 1 BFS TREE: "
      + ((endTime - startTime)/(1000*totalNumBFSTrees)));

    // parentHistogram.get(vertexId).get(neighborId) is the count
    ArrayList<TreeMap<Integer, Integer>> parentHistogram = Utils.getParentHistogram(graph, bfsTrees);

    // print # of BFS trees that each neighbor to be the vertex's parent
    for (int v = 0; v < parentHistogram.size(); ++v) {
//      System.out.println("===== Vertex " + v + "=====");
      for(Map.Entry<Integer,Integer> entry : parentHistogram.get(v).entrySet()) {
        Integer neighbor = entry.getKey();
        Integer numParents = entry.getValue();

//        System.out.println("Neighbor: " + neighbor + ", # of trees to be the parent: " + numParents);
      }
    }

    int numHighestDegreeIsParent50Percent = 0;
    int numHighestDegreeIsParent90Percent = 0;
    int num2HighestDegreeIsParent50Percent = 0;
    int num2HighestDegreeIsParent90Percent = 0;
    int num3HighestDegreeIsParent50Percent = 0;
    int num3HighestDegreeIsParent90Percent = 0;
    for (int v = 0; v < graph.length; ++v) {
      int[] neighbors = graph[v];
      Pair[] degreeSortedNeighbors = new Pair[neighbors.length];
      for (int n = 0; n < neighbors.length; n++) {
        degreeSortedNeighbors[n] = new Pair(neighbors[n], graph[neighbors[n]].length);
      }
      Arrays.sort(degreeSortedNeighbors, degreeComparator);

      int highestDegreeNeighborId = -1;
      int numHighestIsParent = -1;
      if (degreeSortedNeighbors.length >= 1) {
        highestDegreeNeighborId = degreeSortedNeighbors[degreeSortedNeighbors.length - 1].id;
        numHighestIsParent = parentHistogram.get(v).get(highestDegreeNeighborId);
        if (numHighestIsParent >= numBFSTrees / 2) {
          numHighestDegreeIsParent50Percent++;
        }
        if (numHighestIsParent >= numBFSTrees * 0.9) {
          numHighestDegreeIsParent90Percent++;
        }
      }

      int secHighestDegreeNeighborId = -1;
      int num2HighestAreParent = -1;
      if (degreeSortedNeighbors.length >= 2) {
        secHighestDegreeNeighborId = degreeSortedNeighbors[degreeSortedNeighbors.length - 2].id;
        num2HighestAreParent = numHighestIsParent + parentHistogram.get(v).get(secHighestDegreeNeighborId);
        if (num2HighestAreParent >= numBFSTrees / 2) {
          num2HighestDegreeIsParent50Percent++;
        }
        if (num2HighestAreParent >= numBFSTrees * 0.9) {
          num2HighestDegreeIsParent90Percent++;
        }
      }

      if (degreeSortedNeighbors.length >= 3) {
        int thirdHighestDegreeNeighborId = degreeSortedNeighbors[degreeSortedNeighbors.length - 3].id;
        int num3HighestAreParent = num2HighestAreParent + parentHistogram.get(v).get(thirdHighestDegreeNeighborId);
        if (num3HighestAreParent >= numBFSTrees / 2) {
          num3HighestDegreeIsParent50Percent++;
        }
        if (num3HighestAreParent >= numBFSTrees * 0.9) {
          num3HighestDegreeIsParent90Percent++;
        }
      }

      /*
      System.out.println("===== Vertex " + v + "=====");
      for (int n = 0; n < degreeSortedNeighbors.length; n++) {
        int neighborId = degreeSortedNeighbors[degreeSortedNeighbors.length - (n + 1)].id;
        System.out.print("No." + (n+1) + " ");
        System.out.print("Neighbor: " + neighborId + " ");
        System.out.print("# of trees as a parent: " + parentHistogram.get(v).get(neighborId));
        System.out.println();
      }
      */
    }
    System.out.println("% of vertices having highest-degree neighbor as parent over 50% trees: " +
      100.0 * numHighestDegreeIsParent50Percent / graph.length);
    System.out.println("% of vertices having highest-degree neighbor as parent over 90% trees: " +
      100.0 * numHighestDegreeIsParent90Percent / graph.length);
    System.out.println("% of vertices having top 2 highest-degree neighbors as parent over 50% trees: " +
      100.0 * num2HighestDegreeIsParent50Percent / graph.length);
    System.out.println("% of vertices having top 2 highest-degree neighbors as parent over 90% trees: " +
      100.0 * num2HighestDegreeIsParent90Percent / graph.length);
    System.out.println("% of vertices having top 3 highest-degree neighbors as parent over 50% trees: " +
      100.0 * num3HighestDegreeIsParent50Percent / graph.length);
    System.out.println("% of vertices having top 3 highest-degree neighbors as parent over 90% trees: " +
      100.0 * num3HighestDegreeIsParent90Percent / graph.length);

    int src, dst;
    long numEdgesShortestPath, numEdgesLimitedBFSk;
    int shortestPathLength, limitedBFSkPathLength;
    int numAbleToFindPath = 0;
    int numLimitedBFSkNotFindPath = 0;
    long numEdgesShortestPathSum = 0;
    long numEdgesLimitedBFSkSum = 0;
    int[] differences = new int[10];
    for (int j = 0; j < differences.length; ++j) {
      differences[j] = 0;
    }

    for (int i = 0; i < numTrials; ++i) {
      if (i > 0 && (i % 1000) == 0) {
        System.out.println("Starting " + i + "th trial.");
      }

      // Generates random queries from s to t with s != t
      src = random.nextInt(graph.length);
      dst = random.nextInt(graph.length);
      while (src == dst) {
        dst = random.nextInt(graph.length);
      }

      shortestPathLength = Utils.getSSSDSPBiDirBFS(graph, src, dst);
      numEdgesShortestPath = Utils.numEdgesTraversed;
      limitedBFSkPathLength = Utils.getLimitedKBiDirBFS(graph, parentHistogram, src, dst, 3);
      numEdgesLimitedBFSk = Utils.numEdgesTraversed;
      if (shortestPathLength > 0 && limitedBFSkPathLength > 0) {
        numEdgesShortestPathSum += numEdgesShortestPath;
        numEdgesLimitedBFSkSum += numEdgesLimitedBFSk;
        numAbleToFindPath++;

        if (limitedBFSkPathLength - shortestPathLength >= 0) {
          differences[limitedBFSkPathLength - shortestPathLength]++;
        } else {
          System.out.println("ERROR: limited BFS-k found a shorter path");
        }
      } else if (shortestPathLength > 0) {
        numLimitedBFSkNotFindPath++;
      }

      System.out.println("(" + src + "->" + dst + ") #Edges Traversed (BFS, BFS-k): ("
        + numEdgesShortestPath + ", " + numEdgesLimitedBFSk + ")");
    }

    System.out.println("#queries BFS-k unable to find a path: " + numLimitedBFSkNotFindPath);
    System.out.println("Avg #Edges Traversed (BFS, BFS-k): (" + (1.0 * numEdgesShortestPathSum / numAbleToFindPath) +
      ", " + (1.0 * numEdgesLimitedBFSkSum / numAbleToFindPath) + ")");

    for (int k = 0; k < differences.length; ++k) {
      System.out.println("Difference " + k + ": " + differences[k]);
    }
  }
}
