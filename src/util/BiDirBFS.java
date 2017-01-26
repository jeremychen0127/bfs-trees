package util;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;

public class BiDirBFS {
  public static void main(String[] args) throws NumberFormatException, IOException {
    String graphFile = args[0];
    int numTrials = Integer.parseInt(args[1]);
    boolean isDirectedGraph = Boolean.parseBoolean(args[2]);
    System.out.println("graphFile:" + graphFile);
    System.out.println("numTrials: " + numTrials);
    System.out.println("directed: " + isDirectedGraph);
    long startTime = System.currentTimeMillis();
    int[][] graph = Utils.getGraph(graphFile);
    int[][] revGraph = null; //new int[graph.length][];
    if (isDirectedGraph) {
      revGraph = Utils.reverseGraph(graph);
    }
    long endTime = System.currentTimeMillis();
    System.out.println("TIME TAKEN TO PARSE THE GRAPH: " + ((endTime - startTime)/1000));

    Random random = new Random(0);

    int src, dst;
    long numEdgesShortestPath;
    int shortestPathLength;
    int numAbleToFindPath = 0;
    long numEdgesShortestPathSum = 0;
    long totalTimeForBiDirSSSDSP = 0;
    int[] fwBfsLevels = new int[graph.length];
    int[] bwBfsLevels = new int[graph.length];
    ArrayBlockingQueue<Integer> fwBfsQueue = null;
    ArrayBlockingQueue<Integer> bwBfsQueue = null;

    for (int i = 0; i < numTrials; ++i) {
      if (i > 0 && (i % 200) == 0) {
        System.out.println("Starting " + i + "th trial.");
      }

      // Generates random queries from s to t with s != t
      src = random.nextInt(graph.length);
      dst = random.nextInt(graph.length);
      while (src == dst) {
        dst = random.nextInt(graph.length);
      }

      Utils.BiDirBFSInit(fwBfsLevels, bwBfsLevels);
      fwBfsQueue = new ArrayBlockingQueue<Integer>(graph.length);
      bwBfsQueue = new ArrayBlockingQueue<Integer>(graph.length);

      startTime = System.nanoTime();
      shortestPathLength = Utils.getSSSDSPBiDirBFS(graph, fwBfsLevels, bwBfsLevels, fwBfsQueue, bwBfsQueue, src, dst);
      endTime = System.nanoTime();
      totalTimeForBiDirSSSDSP += (endTime - startTime);
      numEdgesShortestPath = Utils.numEdgesTraversed;

      if (shortestPathLength > 0) {
        numEdgesShortestPathSum += numEdgesShortestPath;
        numAbleToFindPath++;
      }
    }

    System.out.println("totalTimeForBiDirSSSDSP: " + totalTimeForBiDirSSSDSP);
    System.out.println("Avg #Edges Traversed of BFS: " + (1.0 * numEdgesShortestPathSum / numAbleToFindPath));
  }
}
