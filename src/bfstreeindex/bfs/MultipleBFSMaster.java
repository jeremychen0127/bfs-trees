package bfstreeindex.bfs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import util.SimpleBFSData;
import util.graph.AbstractGraph;
import bfstreeindex.BaseConsistencyChecker;
import bfstreeindex.bfs.BFSWorker.CONSISTENCY_RESULT;

public class MultipleBFSMaster extends BaseConsistencyChecker {

  public static int END_OF_COMPUTATION_FLAG = -1245; 
  private ArrayList<BFSWorker> availableWorkers;
  private BlockingQueue<BFSWorker> doneWorkers;
  private LinkedList<BFSWorker> conditionallyConsistentWorkers;
  private int nextVertexIndexToCheck;
//  private int numConditionallyConsistentCaseHappened = 0;
//  private int numPlacedConditionalWorkerNotInTheBeginning = 0;
//  private int numMovedFromCondToAvailAndWasConsistent = 0;
//  private int numMovedFromCondToAvailAndWasInconsistent = 0;
  private Set<Integer> outstandingBFSSrcs;
  public MultipleBFSMaster(AbstractGraph graph, SimpleBFSData[] bfsTrees, int parallelismLevel,
    int consistencyCheckerIndex) {
    super(graph, bfsTrees, parallelismLevel, consistencyCheckerIndex);
    availableWorkers = new ArrayList<BFSWorker>(parallelismLevel);
    doneWorkers = new ArrayBlockingQueue<BFSWorker>(parallelismLevel);
    conditionallyConsistentWorkers = new LinkedList<BFSWorker>();
    for (int i = 0; i < parallelismLevel; ++i) {
      BFSWorker worker = new BFSWorker(graph, bfsTrees, doneWorkers,
        5000 /* numMaxPotentialInconsistentV */, i);
      availableWorkers.add(worker);
      new Thread(worker).start();
    }
    nextVertexIndexToCheck = 0;
    outstandingBFSSrcs = new HashSet<Integer>();
  }

  public byte[] checkConsistencies() throws InterruptedException {
    long startTimeForWholeIndex = System.currentTimeMillis();
    assignInitialWorkers();
    BFSWorker doneWorker = null;
    while ((doneWorker = doneWorkers.take()) != null) {
//      System.out.println("In Master: Worker " + doneWorker.workerID + " done for source: " + doneWorker.source);
      if (consistencyArray[doneWorker.source] == -1) {
//        System.out.println("In Master: Worker: " + doneWorker.source
//          + "  however has already been labeled inconsistent.");
        doneWorker.consistencyResult = CONSISTENCY_RESULT.INCONSISTENT;
        handleDoneInconsistentWorker(doneWorker, false /* don't incrementNumNotIsConsistent */);
      } else {
        placeWorkerInAppropriateQueue(doneWorker);
      }
      if (doneWorker.consistencyResult == CONSISTENCY_RESULT.CONSISTENT) {
        int nextInconsistentV;
        for (int i = 0; i < doneWorker.numPotentialInconsitentV; ++i) {
          nextInconsistentV = doneWorker.potentialInconsistentV[i];
          if (consistencyArray[nextInconsistentV] == 0) {
            incrementNumNotIsConsistent(nextInconsistentV);
          } else if (consistencyArray[nextInconsistentV] == 1) {
            System.err.println("BFS from " + doneWorker.source + " identified vertex "
              + nextInconsistentV + " as an inconsistent vertex but vertex " + nextInconsistentV +
              " appears as consistent in the consistencyArray!!!");
            System.exit(-1);
          }
        }
      }
      // If worker's source is consistent or inconsistent check whether any
      // other worker depended on it and also increment numVertices checked
//      if (doneWorker.consistencyResult != CONSISTENCY_RESULT.CONDITIONALLY_CONSISTENT) {
//        System.out.println("Master is beginning to iteratively check conditionally consistent workers...");  
//        while (checkConditionallyConsistentWorkers()) {
////          System.out.println("Master is iteratively checking conditionally consistent workers...");
//        }
        incrementNumVerticesCheckedAndRemoveSrcFromOutstandingSrcs(doneWorker.source);
//      }

      while ((numVerticesChecked < graph.numV) && (nextVertexIndexToCheck < graph.numV)
        && !availableWorkers.isEmpty()) {
        if (consistencyArray[permutedGraphIDs[nextVertexIndexToCheck]] == 0
          && graph.getDegree(permutedGraphIDs[nextVertexIndexToCheck]) > 0) {
          scheduleNextBFSWorker(permutedGraphIDs[nextVertexIndexToCheck]);
        } else {
          incrementNumVerticesChecked();
        }
        nextVertexIndexToCheck++;
      }
      if (numVerticesChecked == graph.numV) {
        System.out.println("Breaking...");
        break;
      }
//      System.out.println("Done with one while loop in the master.");
    }
    System.out.println("TOTAL numIsConsistent: " + this.numIsConsistent + " numNotIsConsistent: "
       + this.numNotIsConsistent);
    System.out.println("Total Time Taken: " + ((System.currentTimeMillis() - startTimeForWholeIndex)/1000)
      + " seconds.");
    assert availableWorkers.size() == parallelismLevel : "THERE MUST BE " + parallelismLevel
      + " AVAILABLE WORKERS UPON EXITTING. numAvailableWorkers: " + availableWorkers.size();
    assert conditionallyConsistentWorkers.isEmpty() : "conditionallyConsistentWorkers SHOULD BE "
      + "EMPTY UPON EXITTING!!!";
    assert doneWorkers.isEmpty() : "doneWorkers SHOULD BE EMPTY UPON EXITTING!!!";
    assert outstandingBFSSrcs.isEmpty() : "OUTSTANDING BFS SOURCES SHOULD BE EMPTY!!!";
    System.out.println("availableWorkers.size: " + availableWorkers.size());
    for (BFSWorker worker : availableWorkers) {
      if (!worker.sources.isEmpty()) { 
        System.err.println("Worker's source must be empty. Src: " + worker.sources.peek());
      }
      worker.sources.add(END_OF_COMPUTATION_FLAG);
    }
    return consistencyArray;
  }

  public void scheduleNextBFSWorker(int nextVertexIDToCheck) throws InterruptedException {
//    System.out.println("SCHEDULING NEXT WORKER with ID: " + nextVertexIDToCheck);
    BFSWorker nextWorker = availableWorkers.remove(0);
    System.arraycopy(this.consistencyArray, 0, nextWorker.workersConsistencyArray, 0, graph.numV);
    for (int outstandingSrc : outstandingBFSSrcs) {
//      System.out.println("!!!PUTTING outstandingSrc: " + outstandingSrc);
      if (nextWorker.workersConsistencyArray[outstandingSrc] == 0) {
        nextWorker.workersConsistencyArray[outstandingSrc] = 1;
      }
    }
    outstandingBFSSrcs.add(nextVertexIDToCheck);
    nextWorker.source = nextVertexIDToCheck;
    nextWorker.sources.put(nextVertexIDToCheck);
//    System.out.println("DONE SCHEDULING NEXT WORKER");
  }

//  private void checkConditionallyConsistentWorkers() {
////    System.out.println("Checking conditionally consistent workers...");
//    Iterator<BFSWorker> workerItr = conditionallyConsistentWorkers.iterator();
//    BFSWorker worker;
//    while (workerItr.hasNext()) {
//      worker = workerItr.next();
////      CONSISTENCY_RESULT newConsistency = checkAndAssignConditionalConsistentWorkersStatus(worker);
//      switch (worker.consistencyResult) {
//      case CONSISTENT:
//        consistencyArray[worker.source] = 1;
//        moveFromConditionalToAvailable(workerItr, worker);
////        numMovedFromCondToAvailAndWasConsistent++;
////        return true;
//        break;
//      case INCONSISTENT:
//        if (consistencyArray[worker.source] == 0) {
//          consistencyArray[worker.source] = -1;
//          incrementNumNotIsConsistent(worker.source);
//        }
//        moveFromConditionalToAvailable(workerItr, worker);
//        numMovedFromCondToAvailAndWasInconsistent++;
//        break;
////        return true;
////      case CONDITIONALLY_CONSISTENT:
////        break;
////      }
//      }
//    }
////    return false;
//  }
  
//  private CONSISTENCY_RESULT checkAndAssignConditionalConsistentWorkersStatus(BFSWorker worker) {
//    boolean isStillPossiblyConsistent = true;
//    Iterator<Integer> conditionalVItr = worker.conditionalVertices.iterator();
//    while (conditionalVItr.hasNext()) {
//      int conditionalVertex = conditionalVItr.next();
//      if (consistencyArray[conditionalVertex] == 1) {
//        worker.consistencyResult = CONSISTENCY_RESULT.INCONSISTENT;
//        return worker.consistencyResult;
//      } else if (consistencyArray[conditionalVertex] == -1) {
//        conditionalVItr.remove();
//      }
//    }
//    if (isStillPossiblyConsistent && worker.conditionalVertices.isEmpty()) {
//      worker.consistencyResult = CONSISTENCY_RESULT.CONSISTENT;
//      return worker.consistencyResult;
//    }
////    worker.consistencyResult = CONSISTENCY_RESULT.CONDITIONALLY_CONSISTENT;
//    return worker.consistencyResult;
//  }

//  public void moveFromConditionalToAvailable(Iterator<BFSWorker> workerItr, BFSWorker worker) {
//    workerItr.remove();
//    incrementNumVerticesCheckedAndRemoveSrcFromOutstandingSrcs(worker.source);
//    availableWorkers.add(worker);
//  }

  private void assignInitialWorkers() throws InterruptedException {
//    System.out.println("ASSIGNING INITIAL WORKERS...");
    int numInitialWorkersToAssign = Math.min(parallelismLevel, graph.numV);
    int i = 0;
    while (i < numInitialWorkersToAssign && nextVertexIndexToCheck < graph.numV) {
//    for (int i = 0; i < numInitialWorkersToAssign; ++i) {
      if (consistencyArray[permutedGraphIDs[nextVertexIndexToCheck]] == 0
        && graph.getDegree(permutedGraphIDs[nextVertexIndexToCheck]) > 0) {
        scheduleNextBFSWorker(permutedGraphIDs[nextVertexIndexToCheck]);
      } else {
        i--;
        incrementNumVerticesChecked();
      }
      nextVertexIndexToCheck++;
      i++;
    }
  }

  private void placeWorkerInAppropriateQueue(BFSWorker worker) {
    switch (worker.consistencyResult) {
    case CONSISTENT:
      handleConsistentDoneWorker(worker);
      break;
    case INCONSISTENT:
      handleDoneInconsistentWorker(worker, true /* increment incrementNumNotIsConsistent */);
      break;
//    case CONDITIONALLY_CONSISTENT:
//      handleDoneConditionallyConsistentWorker(worker);
//      break;
    }
  }

//  public void handleDoneConditionallyConsistentWorker(BFSWorker worker) {
//    // System.out.println("Master: Handling conditionally consistent worker: " + worker.workerID);
//    CONSISTENCY_RESULT newConsistency = checkAndAssignConditionalConsistentWorkersStatus(worker);
//    switch (newConsistency) {
//      case CONSISTENT:
//        // System.out.println("Master: Conditional worker " + worker.source + " is now consistent");
//        handleConsistentDoneWorker(worker);
//        break;
//      case INCONSISTENT:
//        // System.out.println("Master: Conditional worker " + worker.workerID + " is now INconsistent");
//        handleDoneInconsistentWorker(worker, true /* incrementNumNotIsConsistent */);
//        break;
//      case CONDITIONALLY_CONSISTENT:
//        // System.out.println("Master: Conditional worker " + worker.workerID + " is still conditionally"
//        //  + " consistent");
//        numConditionallyConsistentCaseHappened++;
//        int indexToPlace = conditionallyConsistentWorkers.size() - 1;
//        while (indexToPlace >= 0) {
//          if (conditionallyConsistentWorkers.get(indexToPlace--).conditionalVertices.contains(
//            worker.source)) {
//            break;
//          }
//        }
//        conditionallyConsistentWorkers.add(indexToPlace + 1, worker);
//        if (indexToPlace > 0) {
//          numPlacedConditionalWorkerNotInTheBeginning++;
//        }
////        System.out.println("Added worker with ID: " + worker.workerID
////          + " to a conditionallyConsistentWorkers...");
//        break;  
//    }
//  }

  public void handleDoneInconsistentWorker(BFSWorker worker, boolean incrementNumNotIsConsistent) {
//    System.out.println("Master: Handling inconsistent worker: " + worker.workerID);
    if (incrementNumNotIsConsistent) {
      incrementNumNotIsConsistent(worker.source);
    }
    availableWorkers.add(worker);
  }

  public void handleConsistentDoneWorker(BFSWorker worker) {
//    System.out.println("Master: Handling consistent worker: " + worker.workerID);
    incrementNumIsConsistent(worker.source);
    availableWorkers.add(worker);
  }
  
  private void incrementNumVerticesCheckedAndRemoveSrcFromOutstandingSrcs(int source) {
//    System.out.println("incrementNumVerticesCheckedAndRemoveSrcFromOutstandingSrcs called.");
    outstandingBFSSrcs.remove(source);
    incrementNumVerticesChecked();
  }
    
//  @Override
//  protected boolean incrementNumVerticesChecked() {
//    boolean isDebuggingInformation = super.incrementNumVerticesChecked();
////    if (isDebuggingInformation) {
////      System.out.println("numConditionallyConsistentCaseHappened: "
////        + numConditionallyConsistentCaseHappened + " numPlacedConditionalWorkerNotInTheBeginning: "
////        + numPlacedConditionalWorkerNotInTheBeginning);
////      System.out.println("numMovedFromCondToAvailAndWasConsistent: "
////        + numMovedFromCondToAvailAndWasConsistent + " numMovedFromCondToAvailAndWasInconsistent: "
////        + numMovedFromCondToAvailAndWasInconsistent);
////    }
//    return isDebuggingInformation;
//  }
}
