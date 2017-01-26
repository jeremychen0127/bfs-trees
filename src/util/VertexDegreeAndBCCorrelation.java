package util;

import java.io.IOException;
import java.util.Random;

public class VertexDegreeAndBCCorrelation {
  public static void main(String[] args) throws IOException {
    String graphFile = args[0];
    int kForLimitedBFS = Integer.parseInt(args[1]);
    System.out.println("graphFile:" + graphFile);
    System.out.println("kForLimitedBFS: " + kForLimitedBFS);

    long startTime = System.currentTimeMillis();
    int[][] graph = Utils.getGraph(graphFile);
    long endTime = System.currentTimeMillis();
    System.out.println("TIME TAKEN TO PARSE THE GRAPH: " + ((endTime - startTime)/1000));

    Random random = new Random(5);
    BCBFSData bcBfsData;
    BCBFSData.initializeBCScores(graph.length);
    int source;
    int sampleSize = 0;

    while (sampleSize < 300) {
      sampleSize++;
      source = random.nextInt(graph.length);
      bcBfsData = BFSImplementations.getBCBFSTree(graph, source);
      bcBfsData.calculateBCScore();
    }

    for (int i = 0; i < graph.length; ++i) {
      BCBFSData.BCVertexRunningSum[i] = (BCBFSData.BCVertexRunningSum[i] / sampleSize) * graph.length;
    }
    int[][] BCScoreNeighborGraph = Utils.getKHighestBCScoresNeighborsGraph(graph, kForLimitedBFS);
    System.out.println("BC score neighbor graph constructed");

    int[][] VertexDegreeNeighborGraph = Utils.getKHighestDegreeNeighborsGraph(graph, kForLimitedBFS);
    System.out.println("Vertex degree neighbor graph constructed");

    double correlation = Utils.calculateCorrelation(BCScoreNeighborGraph, VertexDegreeNeighborGraph);

    System.out.println("Correlation of Vertex Degree and Betweenness Score: " + (correlation * 100) + "%");
  }
}
