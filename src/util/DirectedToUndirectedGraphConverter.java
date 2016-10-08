package util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;

public class DirectedToUndirectedGraphConverter {

  
  public static void main(String[] args) throws NumberFormatException, IOException {
    String fileName = args[0];
    BufferedReader br = Utils.getBufferedReader(fileName);
    int numV = Utils.getMaxID(fileName) + 1;
    String strLine;
    int src, dst;
    int[][] graph = new int[numV][];
    int[][] reverseGraph = new int[numV][];
    int[] degreesG = new int[numV];
    int[] degreesRG = new int[numV];
    for (int i = 0; i < numV; ++i) {
      graph[i] = new int[2];
      reverseGraph[i] = new int[2];
    }
    br = Utils.getBufferedReader(fileName);

    int numLinesParsed = 0;
    while ((strLine = br.readLine()) != null) {
      numLinesParsed++;
      if ((numLinesParsed % 100000) == 0) {
        System.out.println("Parsed " + numLinesParsed + "th line...");
      }
      if (strLine.startsWith("#") || strLine.trim().isEmpty()) {
        continue;
      }
      String[] split = strLine.split("\\s+");
      src = Integer.parseInt(split[0]);
      if (src >= graph.length) {
        int nextSize = src + 1;
        int[][] newGraph = new int[nextSize][];
        int[][] newReverseGraph = new int[nextSize][];
        int[] newDegreesG = new int[nextSize];
        int[] newDegreesRG = new int[nextSize];
        for (int i = 0; i < graph.length; ++i) {
          newGraph[i] = graph[i];
          newReverseGraph[i] = reverseGraph[i];
          newDegreesG[i] = degreesG[i];
          newDegreesRG[i] = degreesRG[i];
        }
        for (int i = graph.length; i < nextSize; ++i) {
          newGraph[i] = new int[2];
          newReverseGraph[i] = new int[2];
        }
        graph = newGraph;
        reverseGraph = newReverseGraph;
        degreesG = newDegreesG;
        degreesRG = newDegreesRG;
      }
      extendAdjListIfNecessary(src, graph, degreesG, split.length - 1);
      for (int i = 1; i < split.length; ++i) {
        dst = Integer.parseInt(split[i]);
        if (dst >= graph.length) {
          int nextSize = dst + 1;
          int[][] newGraph = new int[nextSize][];
          int[][] newReverseGraph = new int[nextSize][];
          int[] newDegreesG = new int[nextSize];
          int[] newDegreesRG = new int[nextSize];
          for (int t = 0; t < graph.length; ++t) {
            newGraph[t] = graph[t];
            newReverseGraph[t] = reverseGraph[t];
            newDegreesG[t] = degreesG[t];
            newDegreesRG[t] = degreesRG[t];
          }
          for (int t = graph.length; t < nextSize; ++t) {
            newGraph[t] = new int[2];
            newReverseGraph[t] = new int[2];
          }
          graph = newGraph;
          reverseGraph = newReverseGraph;
          degreesG = newDegreesG;
          degreesRG = newDegreesRG;
        }
        graph[src][degreesG[src]] = dst;
        degreesG[src]++;
        extendAdjListIfNecessary(dst, reverseGraph, degreesRG, 1);
        reverseGraph[dst][degreesRG[dst]] = src;
        degreesRG[dst]++;
      }
    }
    
    sortAndShrinkGraph(graph, degreesG);
    sortAndShrinkGraph(reverseGraph, degreesRG);
//    dumpGraph("Graph", graph);
//    dumpGraph("ReverseGraph", reverseGraph);
    graph = mergeGraphs(graph, reverseGraph);
//    dumpGraph("Merged Graph", graph);
    String outputFile = args[1];
    Utils.saveGraph(graph, outputFile);
  }

  private static void dumpGraph(String graphName, int[][] graph) {
    System.out.println("Dumping graph: " + graphName);
    for (int i = 0; i < graph.length; ++i) {
      System.out.print("" + i);
      for (int j = 0; j < graph[i].length; ++j) {
        System.out.print(" " + graph[i][j]);
      }
      System.out.println();
    }
  }

  private static int[][] mergeGraphs(int[][] graph, int[][] reverseGraph) {
    int[][] finalG = new int[graph.length][];
    int[] gNbrs, rgNbrs, finalNbrs;
    for (int i = 0; i < graph.length; ++i) {
      if ((i % 100000) == 0) {
        System.out.println("Merged " + i + "th vertex...");
      }
      gNbrs = graph[i];
      rgNbrs = reverseGraph[i];
      finalNbrs = new int[graph[i].length + reverseGraph[i].length];
      int finalNbrsSize = 0;
      int k = 0;
      int j = 0;
      int previous = Integer.MIN_VALUE;
      while (k < gNbrs.length || j < rgNbrs.length) {
        if (previous != Integer.MIN_VALUE) {
          if (k < gNbrs.length) {
            while (gNbrs[k] == previous) {
              k++;
              if (k == gNbrs.length) {
                break;
              }
            }
          }
          if (j < rgNbrs.length) {
            while (rgNbrs[j] == previous) {
              j++;
              if (j == rgNbrs.length) {
                break;
              }
            }
          }
        }
        if (k < gNbrs.length && j < rgNbrs.length) {
          if (gNbrs[k] < rgNbrs[j]) {
            previous = gNbrs[k];
            finalNbrs[finalNbrsSize++] = previous;
            k++;
          } else {
            previous = rgNbrs[j];
            finalNbrs[finalNbrsSize++] = previous;
            j++;
          }
        } else {
          while (k < gNbrs.length) {
            finalNbrs[finalNbrsSize++] = gNbrs[k];
            k++;
          }
          
          while (j < rgNbrs.length) {
            finalNbrs[finalNbrsSize++] = rgNbrs[j];
            j++;
          }
        }
      }
      int[] shrinkedFinalNbrs = new int[finalNbrsSize];
      System.arraycopy(finalNbrs, 0, shrinkedFinalNbrs, 0, finalNbrsSize);
      finalG[i] = shrinkedFinalNbrs;
    }
    return finalG;
  }

  private static void sortAndShrinkGraph(int[][] graph, int[] degrees) {
    for (int i = 0; i < graph.length; ++i) {
      if ((i % 100000) == 0) {
        System.out.println("Sorted " + i + "th vertex...");
      }
      int[] finalNbrs = new int[degrees[i]];
      System.arraycopy(graph[i], 0, finalNbrs, 0, degrees[i]);
      Arrays.sort(finalNbrs);
      graph[i] = finalNbrs;
    }

  }

  public static void extendAdjListIfNecessary(int src, int[][] graph, int[] degreesG, int numNewNbrs) {
    if ((degreesG[src] + numNewNbrs) > graph[src].length) {
      int newDegreeSize = Math.max(degreesG[src] + numNewNbrs,
        (int) Math.round(graph[src].length*1.2));
      int[] newNeighbors = new int[newDegreeSize];
      System.arraycopy(graph[src], 0, newNeighbors, 0, graph[src].length);
      graph[src] = newNeighbors;
    }
  }
}
