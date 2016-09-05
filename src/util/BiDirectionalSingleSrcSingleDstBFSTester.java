package util;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import util.Pair.PairComparator;

public class BiDirectionalSingleSrcSingleDstBFSTester {

  public static void main(String[] args) throws NumberFormatException, IOException {
    String graphFile = Utils.GPS_DATA_DIR + "/soc-LiveJournal1_full_u_n.txt";
    int[][] graph = Utils.getGraph(graphFile);
    
    PairComparator degreeComparator = new PairComparator();
    Pair[] idDegrees = new Pair[graph.length];
    for (int i = 0; i < graph.length; ++i) {
      idDegrees[i] = new Pair(i, graph[i].length);
    }
    Arrays.sort(idDegrees, degreeComparator);
    SimpleBFSData[] bfsTrees = new SimpleBFSData[10];
    for (int i = 0; i < 10; ++i) {
      if ((i % 5) == 0) { System.out.println("Starting " + i + "th BFS Tree."); }
      bfsTrees[i] = BFSImplementations.getBFSTree(graph,
        idDegrees[idDegrees.length - (i + 1)].id);
    }
    
    Random random = new Random(System.currentTimeMillis());
//    Utils.distanceInBFSTree(bfsTrees[n], id, m);
    int totalParallelTime = 0;
    int totalSingleThreadedTime = 0;
    int numTrials = 500;
    System.out.println("STARTING...");
    long startTime = System.nanoTime();
    for (int j = 0; j < numTrials; ++j) {
      if ((j % 100) == 0) { System.out.println("Starting " + j + "th trial."); }
      int source = random.nextInt(graph.length);
      int destination = random.nextInt(graph.length);
//      int distance = Utils.getSSSDSPBiDirBFS(graph, source, destination);
      for (int k = 0; k < 10; ++k) {
        Utils.distanceInBFSTree(bfsTrees[k], source, destination);
      }
    }
    long endTime = System.nanoTime();
    System.out.println("Took: " + (endTime - startTime) + " nanoseconds");
    System.out.println("Avg: " + ((double) (endTime - startTime)/(double)numTrials) + " nanoseconds");
  }
}
