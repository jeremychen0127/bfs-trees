package util;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;

public class BetweennessCentrality {
    public static void main(String[] args) throws NumberFormatException, IOException {
      String graphFile = args[0];
      int numTrials = Integer.parseInt(args[1]);
      int kForLimitedBFS = Integer.parseInt(args[2]);
      boolean isDirectedGraph = Boolean.parseBoolean(args[3]);
      System.out.println("graphFile:" + graphFile);
      System.out.println("numTrials: " + numTrials);
      System.out.println("kForLimitedBFS: " + kForLimitedBFS);
      System.out.println("directed: " + isDirectedGraph);
      long startTime = System.currentTimeMillis();
      int[][] graph = Utils.getGraph(graphFile);
      long endTime = System.currentTimeMillis();
      System.out.println("TIME TAKEN TO PARSE THE GRAPH: " + ((endTime - startTime)/1000));

      Random random = new Random(1);

      BCBFSData bcBfsData;
      BCBFSData.initializeBCScores(graph.length);
      int source;
      int sampleSize = 0;

      startTime = System.currentTimeMillis();
      while (BCBFSData.BCGlobalSum < 5 * graph.length) {
        sampleSize++;
        source = random.nextInt(graph.length);
        bcBfsData = BFSImplementations.getBCBFSTree(graph, source);
        bcBfsData.calculateBCScore();
      }

      for (int i = 0; i < graph.length; ++i) {
        BCBFSData.BCVertexRunningSum[i] = (BCBFSData.BCVertexRunningSum[i] / sampleSize) * graph.length;
      }
      endTime = System.currentTimeMillis();
      System.out.println("TIME TAKEN TO CALCULATE BETWEENNESS CENTRALITY: " + (endTime - startTime)/1000);

      startTime = System.currentTimeMillis();
      int[][] kNeighborsGraph = Utils.getKHighestBCScoresNeighborsGraph(graph, kForLimitedBFS);
      endTime = System.currentTimeMillis();
      System.out.println("TIME TAKEN TO PROCESS GRAPH FOR LIMITED-k BFS: " + (endTime - startTime)/1000);

      graph = null;

      random = new Random(0);

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
