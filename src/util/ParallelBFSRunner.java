package util;

import util.graph.AbstractGraph;
import util.graph.LargeCRSGraph;


public class ParallelBFSRunner  {
  private ParallelBFSWorker[] workers; // should only be used by Master
  private int[] distances = null;
  volatile ContinueComputationFlag continueComputationFlag;
  
  public ParallelBFSRunner(AbstractGraph graph, int source, int pL) {
    continueComputationFlag = new ContinueComputationFlag();
    continueComputationFlag.continueComp = true;
    this.distances = new int[graph.numV];
    distances[source] = 0;
    this.workers = new ParallelBFSWorker[pL];
    int avgPartitionSize = graph.numV / pL;
    for (int i = 0; i < pL; ++i) {
      int partStartIndex = i * avgPartitionSize;
      int partEndIndex = (i + 1) * avgPartitionSize;
      if (i == (pL - 1)) {
        partEndIndex = graph.numV;
      }
      workers[i] = new ParallelBFSWorker(graph, partStartIndex, partEndIndex, distances, source, i,
        continueComputationFlag);
    }
  }
  
  public int[] computeBFSInParallel() {
    for (ParallelBFSWorker worker : workers) {
      new Thread(worker).start();
    }
    int numSupersteps = 0;
    while (continueComputationFlag.continueComp) {
      try {
        int numDone = 0;
        while (numDone < workers.length) {
          numDone = 0;
          Utils.busySleep(100);
          for (ParallelBFSWorker worker : workers) {
            if (worker.done) {
              numDone++;
            }
          }
        }
        doMasterEndofSuperstepWork(numSupersteps++);
      } catch (Exception e) {
        e.printStackTrace();
        System.exit(-1);
      }
    }
    return distances;
  }
    
  private void doMasterEndofSuperstepWork(int numSupersteps) {
    int numTotalActiveVertices = 0;
    for (ParallelBFSWorker worker : workers) {
      numTotalActiveVertices += worker.numActiveVertices;
    }
//    System.out.println("numActiveV: " + numTotalActiveVertices);
    if (numTotalActiveVertices > 0) {
      continueComputationFlag.continueComp = true;
    } else {
      continueComputationFlag.continueComp = false;
//      System.out.println("numSupersteps: " + numSupersteps);
    }

    for (ParallelBFSWorker worker : workers) {
      worker.numActiveVertices = 0;
      worker.superstepFinished = true;
      worker.done = false;
//      synchronized (worker.lock) {
//        worker.lock.notify();        
//      }
    }

  }

  public static class ContinueComputationFlag {
    boolean continueComp;
  }
}

// UNUSED CODE
//int numWorkers;
//
//public ContinueComputationFlag(int numWorkers) {
//this.numFinishedWorkers = 0;
//this.numWorkers = numWorkers;
//}
//
//synchronized void incrementNumFinishedWorkers() {
//numFinishedWorkers++;
//System.out.println("Incrementing numFinishedWorkers: " + numFinishedWorkers);
//if (numFinishedWorkers == numWorkers) {
////  System.out.println("Notifying masterLock");
//  this.notify();
//}
//}
