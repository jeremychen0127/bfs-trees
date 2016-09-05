package bfstreeindex.bfs;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import bfs.bidirectional.BiDirectionalBFSRunner;
import util.BFSImplementations;
import util.SimpleBFSData;
import util.Utils;
import util.graph.AbstractGraph;

public class BFSWorker implements Runnable {

  CONSISTENCY_RESULT consistencyResult;
//  Set<Integer> conditionalVertices;
  int[] potentialInconsistentV;
  int numPotentialInconsitentV;
  int source;
//  private int[][] graph;
  private AbstractGraph graph;
  int numMaxPotentialInsconsistentV;
  private SimpleBFSData[] bfsTrees;
  private BlockingQueue<BFSWorker> doneWorkers;
  BlockingQueue<Integer> sources;
  byte[] workersConsistencyArray = null;
  int workerID;
//  private int[] bfsQueue;
  BiDirectionalBFSRunner biDirBFSRunner;
  public BFSWorker(AbstractGraph graph, SimpleBFSData[] bfsTrees, BlockingQueue<BFSWorker> doneWorkers,
    int numMaxPotentialInsconsistentV, int workerID) {
    this.graph = graph;
    this.bfsTrees = bfsTrees;
    this.doneWorkers = doneWorkers;
    this.workerID = workerID;
    consistencyResult = null;
//    conditionalVertices = new HashSet<Integer>();
    this.numMaxPotentialInsconsistentV = numMaxPotentialInsconsistentV;
    this.potentialInconsistentV = new int[numMaxPotentialInsconsistentV];
    sources = new ArrayBlockingQueue<Integer>(1);
    this.workersConsistencyArray = new byte[graph.numV];
//    this.bfsQueue = new int[graph.numV];
    this.biDirBFSRunner = new BiDirectionalBFSRunner(graph);
  }
  
  @Override
  public void run() {
    try {
      int nextV;
//      System.out.println("Worker " + workerID + " starting to wait for a source (outside while loop)....");
      while((nextV = sources.take()) >= 0) {
//        System.out.println("Worker " + workerID + " read the next source: " + nextV);
        source = nextV; // This may be unnecessary
        if (source == MultipleBFSMaster.END_OF_COMPUTATION_FLAG) {
          break;
        }
        startNewBFS(source);
//        System.out.println("Worker " + workerID + " with source: " + source + " is starting to wait for another source (inside while loop)....");
      }
//      System.out.println("WORKER " + workerID + " SAW END_OF_COMPUTATION_FLAG. nextV: " + nextV + " Quitting...");
    } catch (InterruptedException e) {
      e.printStackTrace();
      System.exit(-1);
    }
  }

  public void startNewBFS(int source) {
//    System.out.println("Worker: " + source + " is starting a new BFS.");
    initializeBFS(source);
//    System.out.println("Worker: " + source + " is done initializing BFS.");
    int[] bfsDistances = biDirBFSRunner.runBFS(source);//BFSImplementations.getBFSDistances2(bfsQueue, graph, source);
//    int[] bfsDistances = Utils.getParallelBFSDistances(graph, source, 1);
//    System.out.println("Worker: " + source + " is done getting parallel BFS distances.");
    int distInBFSTree, actualDist;
//    System.out.println("Worker: " + source + " is starting to loop through the graph.");
    int newDist;
    for (int m = 0; m < graph.numV; ++m) {
      if (m == source) { continue; }
      if (workersConsistencyArray[m] != -1) {
        if (workersConsistencyArray[m] == 0 && 
          numPotentialInconsitentV >= numMaxPotentialInsconsistentV) {
          continue;
        }
        distInBFSTree = Integer.MAX_VALUE;
        actualDist = bfsDistances[m];
        if (actualDist == 0 && m != source) { actualDist = Integer.MAX_VALUE; }
        for (int n = 0; n < bfsTrees.length; ++n) {
          newDist = Utils.distanceInBFSTree(bfsTrees[n], source, m);
          if (newDist < distInBFSTree) {
            distInBFSTree = newDist;
          }
          if (distInBFSTree == actualDist) {
            break;
          }
        }
        if (distInBFSTree > actualDist) {
          if (workersConsistencyArray[m] == 0) {
            potentialInconsistentV[numPotentialInconsitentV++] = m;
//            System.out.println("Worker: " + workerID + " with source: "+ source + " identified a potentially inconsistent vertex: "
//            + m + " distanceInBFSTree: " + distInBFSTree + " actualDistance: " + actualDist);
          } else if (workersConsistencyArray[m] == 1) {
            consistencyResult = CONSISTENCY_RESULT.INCONSISTENT;
//            System.out.println("Worker: " + workerID + " with source: "+ source + " setting its consistencyResult "
//              + " to INCONSISTENT and addint itself to done workers.");
            doneWorkers.add(this);
            return;
          } 
//            else if (workersConsistencyArray[m] == 2) {
////            System.out.println("Worker: " + workerID + " with source: "+ source + " setting its consistencyResult "
////              + " to CONDITIONALLY_CONSISTENT.");
//            consistencyResult = CONSISTENCY_RESULT.CONDITIONALLY_CONSISTENT;
//            conditionalVertices.add(m);
//          }
        }
      }
    }
    if (consistencyResult == null) {
//      System.out.println("Worker: " + workerID + " with source: "+ source + " setting its consistencyResult "
//        + " to CONSISTENT.");
      consistencyResult = CONSISTENCY_RESULT.CONSISTENT;
    }
//    System.out.println("Worker: " + workerID + " with source: " + source
//      + " and consistencyResult: " + consistencyResult + " is adding itself to done workers.");
    doneWorkers.add(this);
  }

  private void initializeBFS(int source) {
    this.source = source;
    this.numPotentialInconsitentV = 0;
    this.consistencyResult = null;
//    conditionalVertices.clear();
  }

  public enum CONSISTENCY_RESULT {
    CONSISTENT, // Consistent irrespective of other concurrently running BFS's
    INCONSISTENT, // Inconsistent irrespective of other concurrently running BFS's
//    // Consistent if one or more of the concurrently running BFS's are inconsistent,
//    // and inconsistent if one of them are consistent
//    CONDITIONALLY_CONSISTENT, 
  }
}
