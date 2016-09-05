package bfstreeindex;

import java.io.IOException;

import util.BFSImplementations;
import util.Pair;
import util.SimpleBFSData;
import util.Utils;
import util.graph.AbstractGraph;
import util.graph.LargeCRSGraph;

public class NewConsistencySetRunner extends BaseConsistencyChecker implements Runnable {

//  private int[] consistentVs;
//  List<Integer> consistentIDs;
  private String graphName;
  private String isConsistentFilesDir;
  private int[] bfsQueue; // new int[graph.length];
  private int[] potentialInconsistentV;
//  private int[] distancesInBFSTrees;
//  private int[] bfsDistances;

  public NewConsistencySetRunner(AbstractGraph graph, SimpleBFSData[] bfsTrees,
    Pair[] idDegrees, int consistencyCheckerIndex, String graphName,
    String isConsistentFilesDir, int parallelismLevel) {
    super(graph, bfsTrees, parallelismLevel, consistencyCheckerIndex);
    this.graphName = graphName;
    this.isConsistentFilesDir = isConsistentFilesDir;
//    this.consistentVs = new int[graph.length];
    System.out.println("consistentVs ARE NOT NULL!!!!!!");
    bfsQueue = new int[graph.numV];
    potentialInconsistentV = new int[graph.numV];
//    distancesInBFSTrees = new int[graph.length];
//    bfsDistances = new int[graph.length];
  }
  
  @Override
  public void run() { 
    System.out.println("Constructing a new isConsistent File: consistencyCheckerIndex: "+ consistencyCheckerIndex);
    int[] permutedGraphIDs = Utils.getPermutedIDs(graph.numV);
//    int[] permutedGraphIDs = getPermutedIDsRandomizedByExistingConsistencyArrays();
    numNotIsConsistent = 0;
    startTimeForNumVerticesChecked = System.currentTimeMillis();
    long startTimeForWholeIndex = System.currentTimeMillis();
    for (int id : permutedGraphIDs) {
      incrementNumVerticesChecked();
      if (consistencyArray[id] == 1 || consistencyArray[id] == -1) {
        continue;
      } else {
//        double percentageChecked = (((double) numIsConsistent + (double)numNotIsConsistent)/ ((double)graph.length));
//        if (percentageChecked < 0.95) {
          checkConsistencyByCheckingAllPaths2(id);
//        } else {
//          checkConsistencyByCheckingAllPaths(graph, bfsTrees, consistencyArray, id, crsGraph);
//        }
      }
    }
    System.out.println("TOTAL numIsConsistent in " + consistencyCheckerIndex + "th file: "
      + this.numIsConsistent + " numNotIsConsistent: " + this.numNotIsConsistent);
    System.out.println("Total Time Taken: " + ((System.currentTimeMillis() - startTimeForWholeIndex)/1000)
      + " seconds.");
    try {
      Utils.saveIsConsistentFile(consistencyArray, isConsistentFilesDir, graphName,
        consistencyCheckerIndex);
      MultipleBFSTreesIndexTester.numOutstandingRunners--;
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

//  public void checkConsistencyByCheckingAllPaths(int[][] graph,
//    SimpleBFSData[] bfsTrees, int[] consistencyArray, int id, CRSGraph crsGraph2) {
//    int[] bfsDistances = Utils.getBFSDistances2(bfsQueue, graph, id);
//    int distInBFSTree, actualDist;
////    for (int m : consistentIDs) {
////    for (int m = 0; m < graph.length; ++m) {
//    int m;
//    for (int i = 0; i < this.numIsConsistent; ++i) {
//       m = consistentVs[i];
////      if (consistencyArray[m] == 1) {
//        distInBFSTree = Integer.MAX_VALUE;
//        actualDist = bfsDistances[m];
////        if (actualDist == 0 && m != id) { actualDist = Integer.MAX_VALUE; }
//        for (int n = 0; n < bfsTrees.length; ++n) {
//          distInBFSTree = Math.min(distInBFSTree,
//            Utils.distanceInBFSTree(bfsTrees[n], id, m));
//          if (distInBFSTree < actualDist) {
//            System.err.println("distInBFSTree < actualDist" + distInBFSTree + " " + actualDist);
//            System.exit(-1);
//          }
//          if (distInBFSTree == actualDist) {
//            break;
//          }
//        }
//        if (distInBFSTree != actualDist) {
//          incrementNumNotIsConsistent(id);
//          return;
//        }
////      }
//    }
//    incrementNumIsConsistent(id);
//  }
  
  public void checkConsistencyByCheckingAllPaths2(int id) {
//    int[] bfsDistances = Utils.getBFSDistances2(bfsQueue, graph, id);
    int[] bfsDistances = BFSImplementations.getParallelBFSDistances(graph, id, parallelismLevel);
    int distInBFSTree, actualDist;
    int numPotentialInconsitentV = 0;
    int numMaxPotentialInsconsistentV = 5000;
    for (int m = 0; m < graph.numV; ++m) {
      if (consistencyArray[m] != -1) {
        if (consistencyArray[m] == 0 && numPotentialInconsitentV > numMaxPotentialInsconsistentV) {
          continue;
        }
        distInBFSTree = Integer.MAX_VALUE;
        actualDist = bfsDistances[m];
//        if (actualDist == 0 && m != id) { actualDist = Integer.MAX_VALUE; }
        for (int n = 0; n < bfsTrees.length; ++n) {
          distInBFSTree = Math.min(distInBFSTree,
            Utils.distanceInBFSTree(bfsTrees[n], id, m));
          if (distInBFSTree == actualDist) {
            break;
          }
        }
        if (distInBFSTree > actualDist) {
          if (consistencyArray[m] == 0) {
            potentialInconsistentV[numPotentialInconsitentV++] = m;
          } else if (consistencyArray[m] == 1) {
            incrementNumNotIsConsistent(id);
            return;
          }
        }
      }
    }
//    System.out.println("numPotentialInconsitentV: "+ numPotentialInconsitentV);
    for (int i = 0; i < numPotentialInconsitentV; ++i) {
      consistencyArray[potentialInconsistentV[i]] = -1;
      incrementNumNotIsConsistent(potentialInconsistentV[i]);   
    }
    incrementNumIsConsistent(id);
  }
  

  @Override
  public void incrementNumIsConsistent(int v) {
//    consistentVs[this.numIsConsistent] = v;
    super.incrementNumIsConsistent(v);
  }
}

// UNUSED CODE
//public void checkConsistencyByCheckingAllPathsPBFSDoBFSLater(int id) {
////int[] bfsDistances = Utils.getBFSDistances2(bfsQueue, graph, id);
//int distInBFSTree, actualDist;
//int numPotentialInconsitentV = 0;
//int numMaxPotentialInsconsistentV = 5000;
////for (int m : consistentIDs) {
//for (int m = 0; m < graph.length; ++m) {
//if (consistencyArray[m] != -1) {
//if (consistencyArray[m] == 0 & numPotentialInconsitentV > numMaxPotentialInsconsistentV) {
//  continue;
//}
//distInBFSTree = Integer.MAX_VALUE;
////if (actualDist == 0 && m != id) { actualDist = Integer.MAX_VALUE; }
//for (int n = 0; n < bfsTrees.length; ++n) {
//  distInBFSTree = Math.min(distInBFSTree,
//    Utils.distanceInBFSTree(bfsTrees[n], id, m));
//}
//distancesInBFSTrees[m] = distInBFSTree;
//if (consistencyArray[m] == 0) {
//  numPotentialInconsitentV++;
//}
//}
//}
//int[] bfsDistances = Utils.checkParallelCheckBFSDistances(graph, id, bfsParallelismLevel,
//consistencyArray, distancesInBFSTrees);
//
////System.out.println("numPotentialInconsitentV: "+ numPotentialInconsitentV);
//for (int i = 0; i < numPotentialInconsitentV; ++i) {
//consistencyArray[potentialInconsistentV[i]] = -1;
//updateNumNotIsConsistent(potentialInconsistentV[i]);   
//}
//updateNumIsConsistent(id);
//}

//private int[] getPermutedIDsRandomizedByExistingConsistencyArrays() {
//Pair[] idsWithPriorities = new Pair[graph.length];
//int[] histograms = new int[graph.length];
//for (int i = 0; i < existingConsistencyArrays.length; ++i) {
//for (int j = 0; j > graph.length; ++j) {
//  if (existingConsistencyArrays[i][j] == 1) {
//    histograms[j]++;
//  }
//}
//}
//
//Random random = new Random();
//for (int i = 0; i < graph.length; ++i) {
//idsWithPriorities[i] = new Pair(i, histograms[i] + random.nextInt(20));
//}
//
//Arrays.sort(idsWithPriorities, new PairComparator());
//int[] permutedIDs = new int[graph.length];
//for (int i = 0; i < idsWithPriorities.length; ++i) {
//permutedIDs[i] = idsWithPriorities[i].id;
//}
//return permutedIDs;
//}