package bfs.bidirectional;

import java.io.IOException;
import java.util.Random;

import util.BFSImplementations;
import util.Utils;
import util.graph.LargeCRSGraph;

public class BiDirectionalBFSTester {

  public static void main(String[] args) throws NumberFormatException, IOException {
    String graphFile = args[0];
    int numTrials = Integer.parseInt(args[1]);
    System.out.println("numTrials: " + numTrials);
    LargeCRSGraph graph = new LargeCRSGraph(Utils.getGraph(graphFile));
    BiDirectionalBFSRunner biDirBFSWorker = new BiDirectionalBFSRunner(graph);
    Random random = new Random(System.currentTimeMillis());
    long totalTimeBiDir, totalTimeOneDir, totalParallelBFS;
    totalTimeBiDir = totalTimeOneDir = totalParallelBFS = 0;
    long startTime, endTime; 
    int source;
    int[] bfsQueueForOneDir = new int[graph.numV];
    for (int j = 0; j < numTrials; ++j) {
      if ((j % 50) == 0) { System.out.println(j + "th run..");}
      source = random.nextInt(graph.numV);
      startTime = System.currentTimeMillis();
      int[] distancesBiDir = biDirBFSWorker.runBFS(source);
      endTime = System.currentTimeMillis();
      totalTimeBiDir += endTime - startTime;
//
//      startTime = System.currentTimeMillis();
//      int[] distancesOneDir = BFSImplementations.getBFSDistances2(bfsQueueForOneDir, graph, source);
//      endTime = System.currentTimeMillis();
//      totalTimeOneDir += endTime - startTime;
//      
//      startTime = System.currentTimeMillis();
//      int[] distancesParallelDir = BFSImplementations.getParallelBFSDistances(graph, source, 3);
//      endTime = System.currentTimeMillis();
//      totalParallelBFS += endTime - startTime;
////      
//      for (int i = 0; i < graph.numV; ++i) {
//        if (distancesBiDir[i] != distancesParallelDir[i]) {
//          System.err.println("BIDIR != ONEDIR!!! source: " + source + " distancesBiDir[i]: " +
//            distancesBiDir[i] + " distancesOneDir[i]: " + distancesOneDir[i]);
//        }
//      }
    }
    System.out.println("TOTAL TIME TAKEN BIDIR: " + totalTimeBiDir);
    System.out.println("AVG TIME PER BIDIR: " + totalTimeBiDir/numTrials);

//    System.out.println("TOTAL TIME TAKEN ONEDIR: " + totalTimeOneDir);
//    System.out.println("AVG TIME PER ONEDIR: " + totalTimeOneDir/numTrials);
//
//    System.out.println("TOTAL TIME TAKEN PARALLEL DIR: " + totalParallelBFS);
//    System.out.println("AVG TIME PER PARALLEL DIR: " + totalParallelBFS/numTrials);
  }
}
