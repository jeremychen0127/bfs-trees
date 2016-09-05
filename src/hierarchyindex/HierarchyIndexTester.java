package hierarchyindex;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import util.Pair;
import util.Pair.PairComparator;
import util.Utils;

public class HierarchyIndexTester {
  static int[] tiers;
  public static void main(String[] args) throws NumberFormatException, IOException {
    int[][] wholeGraph = Utils.getGraph(Utils.GPS_DATA_DIR + "/com-orkut.ungraph-u-n.txt");
    int[][] hGraph = getHierarchicalGraph(wholeGraph);
    Random random = new Random();
    int src, dest, distInWholeGraph, distInHGraph;
    int numEqual, numDifferent;
    numEqual = numDifferent = 0;
    for (int i = 0; i < 100; ++i) {
      src = random.nextInt(wholeGraph.length);
      dest = random.nextInt(wholeGraph.length);
      if (src != dest && wholeGraph[src].length > 0 && wholeGraph[dest].length > 0) {
        if (tiers[src] > tiers[dest]) {
          int tmp = src;
          src = dest;
          dest = src;
        }
        distInWholeGraph = Utils.getSSSDSPOneDirBFS(wholeGraph, src, dest);
//        System.out.println("src: " + src + " tiers[src]: " + tiers[src] + " dest: " + dest + 
//          " tiers[dest]: " + tiers[dest]);
//        System.out.println("distInWholeGraph: " + distInWholeGraph);
        if (distInWholeGraph > 3 && distInWholeGraph <= 6) {
          distInHGraph = Utils.getSSSDSPOneDirBFS(hGraph, src, dest);
//          System.out.println("distInHGraph: " + distInHGraph);
          if (distInWholeGraph == distInHGraph) {
            numEqual++;
          } else {
            numDifferent++;
          }
        }
      } else {
        i--;
      }
    }
    System.out.println("numTotal: " + (numEqual + numDifferent) + " numEqual: " + numEqual
      + " numDifferent: " + numDifferent);
  }

  private static int[][] getHierarchicalGraph(int[][] wholeGraph) {
    tiers = new int[wholeGraph.length];
    int avgDegree = getAverageDegree(wholeGraph);
    int numTier3V, numTier2V, numTier1V;
    numTier3V = numTier2V = numTier1V = 0;
    int numTier3E, numTier2E, numTier1E;
    numTier3E = numTier2E = numTier1E = 0;
    for (int i = 0; i < wholeGraph.length; ++i) {
      if (wholeGraph[i].length > (avgDegree * 25)) {
        tiers[i] = 3; numTier3V++; numTier3E += wholeGraph[i].length;
      } else if (wholeGraph[i].length > (avgDegree * 5)) {
        tiers[i] = 2; numTier2V++; numTier2E += wholeGraph[i].length;
      } else {
        tiers[i] = 1; numTier1V++; numTier1E += wholeGraph[i].length;
      }
    }
    System.out.println("numTier3V: " + numTier3V + " numTier2V: " + numTier2V
      + " numTier1V: " + numTier1V);
    System.out.println("numTier3E: " + numTier3E + " numTier2E: " + numTier2E
      + " numTier1E: " + numTier1E);
    System.out.println("avgTier3Degree: " + ((double) numTier3E / numTier3V)
      + " avgTier2Degree: " + ((double) numTier2E / numTier2V)
      + " avgTier3Degree: " + ((double) numTier1E / numTier1V));
    HashSet<Integer>[] hGraphSet = new HashSet[wholeGraph.length];
    for (int i = 0; i < wholeGraph.length; ++i) {
      hGraphSet[i] = new HashSet<Integer>();
    }
    int nbrID;
    PairComparator degreeComparator = new PairComparator();
    for (int i = 0; i < wholeGraph.length; ++i) {
//      int numHigherTierNbrs = 0;
      List<Pair> pairAdjList = new ArrayList<Pair>();
      for (int j = 0; j < wholeGraph[i].length; ++j) {
        nbrID = wholeGraph[i][j];
//        if (tiers[i] == 1 && tiers[nbrID] >= 2 || tiers[i] >= 2) { 
//          numHigherTierNbrs++; 
          pairAdjList.add(new Pair(nbrID, tiers[nbrID]));
//        } 
//      else {
//          numHigherTierNbrs++;
////          if (tiers[nbrID] == 3) { numHigherTierNbrs++; }
//        }
//        else if (tiers[i] == 2) { // For tier 2 vertices keep tier 1 & 3 edges
//          if (tiers[nbrID] != 2) { numHigherTierNbrs++; }
//        } else { // For tier 3 vertices all edges
//          numHigherTierNbrs++;
//        }
      }

//      numEdges += numHigherTierNbrs;
      Collections.sort(pairAdjList, degreeComparator);
      int numNbrsToAdd = Math.min(pairAdjList.size(), (avgDegree / 2));
      if (tiers[i] == 3) {
        numNbrsToAdd = pairAdjList.size();
      }
      for (int k = 0; k < numNbrsToAdd; ++k) {  
        hGraphSet[i].add(pairAdjList.get(k).id);
        hGraphSet[pairAdjList.get(k).id].add(i);
      }
    }
    
    int numEdges = 0;
    int[][] hGraph = new int[wholeGraph.length][];
    for (int i = 0; i < wholeGraph.length; ++i) {
      int[] adjacencyList = new int[hGraphSet[i].size()];
      numEdges += hGraphSet[i].size();
      int index = 0;
      for (int nbr : hGraphSet[i]) {
        adjacencyList[index++] = nbr;
      }
      hGraph[i] = adjacencyList;
    }
    System.out.println("numEdges in hGraph: " + numEdges);
    return hGraph;
  }

  private static int getAverageDegree(int[][] wholeGraph) {
    int numVertices = 0;
    int degreeSum = 0;
    for (int i = 0; i < wholeGraph.length; ++i) {
      if (wholeGraph[i].length > 0) {
        degreeSum += wholeGraph[i].length;
        numVertices++;
      }
    }
    System.out.println("degreeSum: " + degreeSum + " numVertices: " + numVertices);
    System.out.println("avgDegree: " + (int) Math.ceil(((double) degreeSum / (double) numVertices)));
    return (int) Math.ceil(((double) degreeSum / (double) numVertices));
  }
}
