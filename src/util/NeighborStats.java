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

    ArrayList<TreeMap<Integer, Integer>> parentHistogram = Utils.getParentHistogram(graph, bfsTrees);

    for (int v = 0; v < parentHistogram.size(); ++v) {
      System.out.println("===== Vertex " + v + "=====");
      for(Map.Entry<Integer,Integer> entry : parentHistogram.get(v).entrySet()) {
        Integer neighbor = entry.getKey();
        Integer numParents = entry.getValue();

        System.out.println("Neighbor: " + neighbor + ", # of trees to be the parent: " + numParents);
      }
    }
  }
}
