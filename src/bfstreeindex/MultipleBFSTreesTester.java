package bfstreeindex;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import util.BFSImplementations;
import util.Pair;
import util.SimpleBFSData;
import util.Utils;
import util.Pair.PairComparator;

public class MultipleBFSTreesTester {

  public static void main(String[] args) throws NumberFormatException, IOException {
    String graphFile = args[0];
    int numHighDegreeBFSTrees = Integer.parseInt(args[1]);
    int numRandomBFSTrees = Integer.parseInt(args[2]);
    System.out.println("graphFile: " + graphFile);
    System.out.println("numHighDegreeBFSTrees: " + numHighDegreeBFSTrees);
    System.out.println("numRandomBFSTrees: " + numRandomBFSTrees);
    int numTotalBFSTrees = numHighDegreeBFSTrees + numRandomBFSTrees;
    // long previousTime = System.currentTimeMillis();
    // long currentTime = -1;
    // for (String graphName : Utils.graphNames) {
    int[][] graph = Utils.getGraph(graphFile);
    // currentTime = System.currentTimeMillis();
    // System.out.println("Time (in s) to construct graph: "
    // + ((double) (currentTime - previousTime))/(double)1000);
    // previousTime = currentTime;
    PairComparator degreeComparator = new PairComparator();
    Pair[] idDegrees = new Pair[graph.length];
    for (int i = 0; i < graph.length; ++i) {
      idDegrees[i] = new Pair(i, graph[i].length);
    }
    Arrays.sort(idDegrees, degreeComparator);
    // currentTime = System.currentTimeMillis();
    // System.out.println("Time (in s) to sort graph by degrees: "
    // + ((double) (currentTime - previousTime))/(double)1000);
    // previousTime = currentTime;
    SimpleBFSData[] bfsTrees = new SimpleBFSData[numHighDegreeBFSTrees + numRandomBFSTrees];
    for (int i = 0; i < numHighDegreeBFSTrees; ++i) {
      bfsTrees[i] = BFSImplementations.getBFSTree(graph, idDegrees[idDegrees.length - (i + 1)].id);
    }
    Random random = new Random();
    for (int i = 0; i < numRandomBFSTrees; ++i) {
      bfsTrees[i + numHighDegreeBFSTrees] = BFSImplementations.getBFSTree(graph,
        random.nextInt(graph.length));
    }
    // currentTime = System.currentTimeMillis();
    // System.out.println("Time (in s) to construct 100 BFS Trees: "
    // + ((double) (currentTime - previousTime))/(double)1000);
    // previousTime = currentTime;
    int[][][] resultsHistogram = new int[6][numTotalBFSTrees / 10][2];
    int src, dest, distInGraph, distInBFSTree;
    random = new Random();
    for (int i = 0; i < 3000; ++i) {
      // previousTime = System.currentTimeMillis();
      src = random.nextInt(graph.length);
      dest = random.nextInt(graph.length);
      if (src != dest && graph[src].length > 0 && graph[dest].length > 0) {
        distInGraph = Utils.getSSSDSPOneDirBFS(graph, src, dest);
        // currentTime = System.currentTimeMillis();
        // // System.out.print("Time (in ms) to find distance in graph: " +
        // (currentTime - previousTime));
        // previousTime = currentTime;
        // System.out.println("distInGraph: " + distInGraph);
        if (distInGraph > 0) {
          distInBFSTree = Integer.MAX_VALUE;
          int correctBFSTreeIndex = -1;
          for (int j = 0; j < numTotalBFSTrees; ++j) {
            distInBFSTree = Math
              .min(distInBFSTree, Utils.distanceInBFSTree(bfsTrees[j], src, dest));
            if (distInGraph == distInBFSTree) {
              correctBFSTreeIndex = j;
              break;
            } else if (distInBFSTree < distInGraph) {
              System.err
                .println("BUG!! DISTANCE IN BFS TREE CANNOT BE SHORTER THAN ACTUAL DISTANCE!!");
              System.exit(-1);
            }
          }
          // currentTime = System.currentTimeMillis();
          // System.out.println(" in BFS trees: " + (currentTime -
          // previousTime));
          // previousTime = currentTime;
          for (int k = 0; k < resultsHistogram.length; ++k) {
            if (k + 1 <= distInGraph) {
              for (int m = 0; m < resultsHistogram[k].length; ++m) {
                if (correctBFSTreeIndex == -1) {
                  resultsHistogram[k][m][1]++;
                } else if ((double) m < ((double) correctBFSTreeIndex / 10)) {
                  resultsHistogram[k][m][1]++;
                } else {
                  resultsHistogram[k][m][0]++;
                }
              }
            }
          }
          // System.out.println("distInBFSTree: " + distInBFSTree);
        } else {
          i--;
        }
      } else {
        i--;
      }
      // }
    }
    System.out.println("dist >\t#-bfs-trees\tcorrect\twrong\tpercentage");
    for (int k = 0; k < resultsHistogram.length; ++k) {
      for (int m = 0; m < resultsHistogram[k].length; ++m) {
        System.out.print((k + 1) + "\t" + ((m + 1) * 10));
        System.out.print("\t" + resultsHistogram[k][m][0] + "\t" + resultsHistogram[k][m][1]);
        if (resultsHistogram[k][m][0] + resultsHistogram[k][m][1] > 0) {
          double percentage = (double) resultsHistogram[k][m][0]
            / (double) (resultsHistogram[k][m][0] + resultsHistogram[k][m][1]);
          System.out.println("\t" + percentage);
        } else {
          System.out.println("\tn/a");
        }
      }
    }
  }

}
