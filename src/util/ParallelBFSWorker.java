package util;

import util.ParallelBFSRunner.ContinueComputationFlag;
import util.graph.AbstractGraph;


public class ParallelBFSWorker implements Runnable {
  
  private AbstractGraph graph;
  private int partStartIndex, partEndIndex;
  private int[] distances;
  private int numSuperstep;
  int numActiveVertices;
  volatile boolean superstepFinished;
  private int source;
//  int[][] messageQueue1;
//  int[] messageQueue1Indices;
//  int[][] messageQueue2;
//  int[] messageQueue2Indices;
//  private int numWorkers;
  private int workerID;
  volatile boolean done;
  private ContinueComputationFlag continueComputationFlag;

  public ParallelBFSWorker(AbstractGraph graph, int partStartIndex, int partEndIndex, int[] distances,
    int source, int workerID, ContinueComputationFlag continueComputationFlag) {
    this.graph = graph;
    this.partStartIndex = partStartIndex;
    this.partEndIndex = partEndIndex;
    this.distances = distances;
    this.source = source;
    this.workerID = workerID;
    this.continueComputationFlag = continueComputationFlag;
//    this.numWorkers = numWorkers;
    this.numSuperstep = 0;
    this.superstepFinished = false;
  }
  
//  public void setContinueFlag(ContinueFlag continueFlag) {
//    this.continueFlag = continueFlag;
//  }
    
  @Override
  public void run() {
    while (continueComputationFlag.continueComp) {
      try {
        this.superstepFinished = false;
        runSuperstep();
        this.done = true; 
//        continueFlag.incrementNumFinishedWorkers();
        while (!this.superstepFinished) {
//          System.out.println("Worker sleeping until superstep finishes...");
//          synchronized (lock) { 
//            System.out.println("Worker " + workerID + " is waiting..");
//            lock.wait();
//            System.out.println("Worker " + workerID + " woke up");
//          }
//          Thread.sleep(1);
          Utils.busySleep(100);
//          Thread.sleep(0, 1);
        }
      } catch (Exception e) {
        e.printStackTrace();
        System.exit(-1);
      } 
    }
//    System.out.println("Exiting workerID: " + workerID);
  }

  private void runSuperstep() {
    numSuperstep++;
    int previousDist = numSuperstep - 1;
    int[] adjList;
    int startIndex;
    int endIndex;
    int nbr;
    for (int i = partStartIndex; i < partEndIndex; ++i) {
      if (numSuperstep == 1 && i != source) {
        continue;
      }
      if (this.distances[i] == previousDist) {
        adjList = graph.getNbrsArray(i);
        startIndex = graph.getStartIndex(i);
        endIndex = graph.getEndIdnex(i);
        for (int j = startIndex; j < endIndex; ++j) {
          nbr = adjList[j];
//          if (nbr == 2) { System.out.println("performing vertex 2: distance: " + this.distances[nbr]); }
          if (this.distances[nbr] == 0 && nbr != source) {
            this.distances[nbr] = numSuperstep;
//            if (nbr == 2) { System.out.println("setting v 2's dist to: " + numSuperstep); }
            this.numActiveVertices++;
          }
        }
      }
    }
  }
}
