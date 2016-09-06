package util;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import util.Pair.PairComparator;

public class UtilsTester {

  public static void main(String[] args) throws NumberFormatException, IOException {
//    long javaMaxArraySize = Integer.MAX_VALUE - 5;
//    int[] test = new int[(int) javaMaxArraySize];
//    System.out.println("javaMaxArraySize: " + javaMaxArraySize);
//    System.out.println("Integer.MAX_VALUE: " + Integer.MAX_VALUE);
//    if (257542748 > Integer.MAX_VALUE) {
//      System.out.println("257542748 is bigger than: " + Integer.MAX_VALUE);
//    } else {
//      System.out.println("257542748 is smaller than: " + Integer.MAX_VALUE);
//    }
    String graphFile = args[0];
    int numHighDegreeBFSTrees = Integer.parseInt(args[1]);
    int numRandomBFSTrees = Integer.parseInt(args[2]);
    int numTrials = Integer.parseInt(args[3]);
    System.out.println("graphFile:" + graphFile);
    System.out.println("numHighDegreeBFSTrees:" + numHighDegreeBFSTrees);
    System.out.println("numRandomBFSTrees:" + numRandomBFSTrees);
    System.out.println("numTrials: " + numTrials);
    long startTime = System.currentTimeMillis();
    int[][] graph = Utils.getGraph(graphFile);
    long endTime = System.currentTimeMillis();
    System.out.println("TIME TAKEN TO PARSE THE GRAPH: " + ((endTime - startTime)/1000));
    PairComparator degreeComparator = new PairComparator();
    Pair[] idDegrees = new Pair[graph.length];
    for (int i = 0; i < graph.length; ++i) {
      idDegrees[i] = new Pair(i, graph[i].length);
    }
    startTime = System.currentTimeMillis();
    Arrays.sort(idDegrees, degreeComparator);
    endTime = System.currentTimeMillis();
    System.out.println("TIME TAKEN TO SORT DEGREES: " + ((endTime - startTime)/1000));

    SimpleBFSData[] bfsTrees = new SimpleBFSData[numHighDegreeBFSTrees + numRandomBFSTrees];
    startTime = System.currentTimeMillis(); 
    for (int i = 0; i < numHighDegreeBFSTrees; ++i) {
      System.out.println("Source of High Degree BFS Tree vertex: " + idDegrees[idDegrees.length - (i + 1)].id);
      bfsTrees[i] = BFSImplementations.getBFSTree(graph, idDegrees[idDegrees.length - (i + 1)].id);
    }

    Random random = new Random(0);
    for (int i = 0; i < numRandomBFSTrees; ++i) {
      int nextSrc = random.nextInt(graph.length);
      if (graph[nextSrc].length == 0) {
        i--;
        continue;
      } else {
        System.out.println("Source of Random BFS Tree vertex: " + nextSrc);
        bfsTrees[numHighDegreeBFSTrees + i] = BFSImplementations.getBFSTree(graph, nextSrc);
      }
    }
    endTime = System.currentTimeMillis();
    int totalNumBFSTrees = numHighDegreeBFSTrees + numRandomBFSTrees;
    System.out.println("TIME TAKEN TO CONSTRUCT" + totalNumBFSTrees
      + " BFS TREES: " + ((endTime - startTime)/1000));
    System.out.println("AVG TIME TAKEN TO CONSTRUCT 1 BFS TREE: "
      + ((endTime - startTime)/(1000*totalNumBFSTrees)));
    int src, dst, distInBFSTrees, actualDist, tmpDist;
    int numEqual = 0;
    long totalTimeForBiDirSSSDSP = 0;
    long totalTimeForSearchInBFSTrees = 0;
    TreeMap<Integer, Integer> histogram = new TreeMap<>();
    for (int i = 0; i < numTrials; ++i) {
      if (i > 0 && (i % 1000) == 0) { 
        System.out.println("Starting " + i + "th trial. numEqual: " + numEqual + " percentage: "
          + ((double) numEqual/i));
        System.out.println("totalTimeForBiDirSSSDSP: " + totalTimeForBiDirSSSDSP +
          " totalTimeForSearchInBFSTrees: " + totalTimeForSearchInBFSTrees);
      }
      src = random.nextInt(graph.length);
      dst = random.nextInt(graph.length);
      startTime = System.nanoTime();
      actualDist = Utils.getSSSDSPBiDirBFS(graph, src, dst);
      endTime = System.nanoTime();
      totalTimeForBiDirSSSDSP += (endTime - startTime);
      startTime = System.nanoTime();
      distInBFSTrees = Integer.MAX_VALUE;

      for (int j = 0; j < numHighDegreeBFSTrees + numRandomBFSTrees; ++j) {
        tmpDist = Utils.distanceInBFSTree(bfsTrees[j], src, dst);
        if (tmpDist < distInBFSTrees) {
          distInBFSTrees = tmpDist;
          if (distInBFSTrees == actualDist) {
            break;
          }
        }
      }
      endTime = System.nanoTime();
      totalTimeForSearchInBFSTrees += (endTime - startTime);
      if (distInBFSTrees == actualDist) {
        numEqual++;
      }

      Integer difference = distInBFSTrees - actualDist;
      if (histogram.containsKey(difference)) {
        Integer oldValue = histogram.get(difference);
        histogram.put(difference, oldValue + 1);
      } else {
        histogram.put(difference, 1);
      }
    }
    System.out.println("FINISHED! numEqual: " + numEqual + " percentage: "
      + ((double) numEqual/numTrials));
    System.out.println("totalTimeForBiDirSSSDSP: " + totalTimeForBiDirSSSDSP +
      " totalTimeForSearchInBFSTrees: " + totalTimeForSearchInBFSTrees);
    for (Map.Entry<Integer, Integer> entry: histogram.entrySet()) {
      System.out.println("Difference: " + entry.getKey()
        + ", # of Queries: " + entry.getValue());
    }
  }
}
