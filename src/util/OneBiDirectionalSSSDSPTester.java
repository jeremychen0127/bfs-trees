package util;

import java.io.IOException;
import java.util.Random;

public class OneBiDirectionalSSSDSPTester {

  public static void main(String[] args) throws NumberFormatException, IOException {
    for (int k = 0; k < Utils.graphNames.length; ++k) {
      String graphName = Utils.graphNames[k];
      System.out.println(graphName);
      int[][] graph = Utils.getGraph(Utils.GPS_DATA_DIR + "/" + graphName);
      int numTrials = 500;
      int[] srcs = new int[numTrials];
      int[] dests = new int[numTrials];
      Random random = new Random();
      for (int i = 0; i < numTrials; ++i) {
        srcs[i] = random.nextInt(graph.length);
        dests[i] = random.nextInt(graph.length);
      }
      int[] distances = new int[numTrials];
      long numEdgesTraversedOneDir = 0;
      long startTime = System.currentTimeMillis();
      for (int i = 0; i < numTrials; ++i) {
        distances[i] = Utils.getSSSDSPOneDirBFS(graph, srcs[i], dests[i]);
        numEdgesTraversedOneDir += Utils.numEdgesTraversed; 
      }
      long oneDirTime = (System.currentTimeMillis() - startTime);

      long numEdgesTraversedBiDir = 0;
      startTime = System.currentTimeMillis();
      int distance;
      for (int i = 0; i < numTrials; ++i) {
        distance = Utils.getSSSDSPBiDirBFS(graph, srcs[i], dests[i]);
        numEdgesTraversedBiDir += Utils.numEdgesTraversed;
        if (distances[i] != distance) {
          System.err.println("ERROR! OneDir distance != BiDir distance src: " + srcs[i] + 
            " dest: " + dests[i] + " onedirDist: " + distances[i] + " bidirDist: " + distance);
          System.exit(-1);
        }
        
      }
      long biDirTime = (System.currentTimeMillis() - startTime);

      System.out.println("OneDirTime: " + oneDirTime);
      System.out.println("OneDir #-edges traversed: " + numEdgesTraversedOneDir);
      System.out.println("BiDirTime: " + biDirTime);
      System.out.println("BiDir #-edges traversed: " + numEdgesTraversedBiDir);
    }
  }
}
