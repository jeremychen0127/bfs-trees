package util;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.UnknownFormatConversionException;

import util.Pair.PairComparator;

public class HistogramBuilder {

  public static void main(String[] args) throws NumberFormatException, IOException {
//    long javaMaxArraySize = Integer.MAX_VALUE - 5;
//    int[] test = new int[(int) javaMaxArraySize];
//    System.out.println("javaMaxArraySize: " + javaMaxArraySize);
//    System.out.println("Integer.MAX_VALUE: " + Integer.MAX_VALUE);
//    if (257542748 > Integer.MAX_VALUE) {
//      System.out.println("257542748 is bigger than: " + Integer.MAX_VALUE);
//    } else {
//      System.out.println("257542748 is smaller than: " + Integer.MAX_VALUE);
//    }
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
    int[][] revGraph;
    if (isDirectedGraph) {
      revGraph = Utils.reverseGraph(graph);
    }
    long endTime = System.currentTimeMillis();
    System.out.println("TIME TAKEN TO PARSE THE GRAPH: " + ((endTime - startTime)/1000));
    PairComparator degreeComparator = new PairComparator();
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
    int src, dst, tmpDist, distInBFSTrees, actualDist;
    int numEqual = 0;
    long totalTimeForBiDirSSSDSP = 0;
    long totalTimeForSearchInBFSTrees = 0;

    ArrayList<ArrayList<Integer> > histograms = new ArrayList<>();
    int[] numPathsThroughBFSRoot = new int[numHighDegreeBFSTrees + numRandomBFSTrees];
    int[] numQueriesAcrossComponents = new int[numHighDegreeBFSTrees + numRandomBFSTrees];
    boolean thruRootRecorded = false;
    for (int i = 0; i < numHighDegreeBFSTrees + numRandomBFSTrees; ++i) {
      histograms.add(i, new ArrayList<Integer>());
    }

    for (int i = 0; i < numTrials; ++i) {
      if (i > 0 && (i % 1000) == 0) { 
        System.out.println("Starting " + i + "th trial. numEqual: " + numEqual + " percentage: "
          + ((double) numEqual/i));
        System.out.println("totalTimeForBiDirSSSDSP: " + totalTimeForBiDirSSSDSP +
          " totalTimeForSearchInBFSTrees: " + totalTimeForSearchInBFSTrees);
      }

      thruRootRecorded = false;

      // Generates random queries from s to t with s != t
      src = random.nextInt(graph.length);
      dst = random.nextInt(graph.length);
      while (src == dst) {
        System.out.println("src == dst: " + src);
        dst = random.nextInt(graph.length);
      }
      startTime = System.nanoTime();
      if (isDirectedGraph) {
        actualDist = Utils.getSSSDSPBiDirBFSDirGraph(graph, revGraph, src, dst);
      } else {
        actualDist = Utils.getSSSDSPBiDirBFS(graph, src, dst);
      }
      endTime = System.nanoTime();
      totalTimeForBiDirSSSDSP += (endTime - startTime);
      startTime = System.nanoTime();
      distInBFSTrees = Integer.MAX_VALUE;

      for (int j = 0; j < numHighDegreeBFSTrees + numRandomBFSTrees; ++j) {
        if (isDirectedGraph) {
          tmpDist = Utils.distanceInDirBFSTree(bfsTrees[j], revBfsTrees[j], src, dst);
        } else {
          tmpDist = Utils.distanceInBFSTree(bfsTrees[j], src, dst);
        }

        if (tmpDist <= distInBFSTrees) {
          distInBFSTrees = tmpDist;
        }

        if (!thruRootRecorded && distInBFSTrees == actualDist && Utils.isPathThroughRoot(bfsTrees[j], src, dst)) {
          numPathsThroughBFSRoot[j]++;
          thruRootRecorded = true;
        }

        if (distInBFSTrees == Integer.MAX_VALUE || actualDist == -1) {
          System.out.println("Path does not exist: " + src + " to " + dst + " in tree with source " + bfsTrees[j].source);
          numQueriesAcrossComponents[j]++;
        } else {
          Integer difference = distInBFSTrees - actualDist;
          if (difference < 0) {
            System.out.println("distInTree: " + distInBFSTrees + ", actualDist: " + actualDist);
          }
          ArrayList<Integer> granularHistogram = histograms.get(j);
          if (difference < granularHistogram.size()) {
            granularHistogram.set(difference, granularHistogram.get(difference) + 1);
          } else {
            // Add missing rows in the histogram
            for (int k = 0; k < difference - granularHistogram.size(); ++k) {
              granularHistogram.add(0);
            }

            granularHistogram.add(1);
          }
          histograms.set(j, granularHistogram);
        }
      }
      endTime = System.nanoTime();
      totalTimeForSearchInBFSTrees += (endTime - startTime);
      if (distInBFSTrees == actualDist) {
        numEqual++;
      }
    }

    int numVerticesOfLargestComp = 0;
    for (int i = 0; i < numHighDegreeBFSTrees + numRandomBFSTrees; ++i) {
      System.out.println("numVerticesInBFSTree of source " + bfsTrees[i].source + ": " + bfsTrees[i].numVertices);
      if (bfsTrees[i].numVertices > numVerticesOfLargestComp) {
        numVerticesOfLargestComp = bfsTrees[i].numVertices;
      }

      if (i >= 1) {
        numPathsThroughBFSRoot[i] += numPathsThroughBFSRoot[i - 1];
      }
    }

    System.out.println("FINISHED!");
    System.out.println("totalTimeForBiDirSSSDSP: " + totalTimeForBiDirSSSDSP +
      " totalTimeForSearchInBFSTrees: " + totalTimeForSearchInBFSTrees);
    System.out.println("# of vertices in largest component / # of vertices: " +
      ((double)numVerticesOfLargestComp/graph.length));
    for (int i = 0; i < numHighDegreeBFSTrees + numRandomBFSTrees; ++i) {
      ArrayList<Integer> granularHistogram = histograms.get(i);
      System.out.println("============== Histogram & Stats (" + (i + 1) + " trees)==============");
      System.out.println("# of queries that have paths: " + (numTrials - numQueriesAcrossComponents[i]));
      if (granularHistogram.isEmpty()) {
        System.out.println("Percentage of BFS paths are shortest paths: N/A");
        System.out.println("None of queries has a path");
      } else {
        System.out.println("Percentage of BFS paths are shortest paths: " +
          ((double) granularHistogram.get(0) / (numTrials - numQueriesAcrossComponents[i])));
        System.out.println("# of shortest paths in BFS through root: " + numPathsThroughBFSRoot[i] +
          ", Percentage: " + ((double) numPathsThroughBFSRoot[i] / granularHistogram.get(0)));
      }

      for (int j = 0; j < granularHistogram.size(); ++j) {
        System.out.println("Difference: " + j + ", # of Queries: " + granularHistogram.get(j));
      }
    }
  }
}
