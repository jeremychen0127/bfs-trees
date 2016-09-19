package util;

import java.util.LinkedList;

import util.graph.AbstractGraph;

public class BFSImplementations {

  public static int[] getBiDirectionalBFSWithBitmaps(int[] bfsQueue, AbstractGraph graph, int source) {
//    BitMap distance
    return null;
  }
  
  public static int[] getBFSDistances2(int[] bfsQueue, AbstractGraph graph, int source) {
    int currentIndex, lastIndex;
    currentIndex = lastIndex = 0;
//    for (int i = 0; i < distances.length; ++i) { distances[i] = Integer.MAX_VALUE; }
    int[] distances = new int[graph.numV]; //distances[source] = 0;
//    LinkedList<Integer> bfsQueue = new LinkedList<Integer>();
    bfsQueue[lastIndex++] = source;
    int nextVertex, currentDist;
    int[] adjList;
    int startIndex;
    int endIndex;
    int nbr;
    while(currentIndex != lastIndex) {
      nextVertex = bfsQueue[currentIndex++];
      currentDist = distances[nextVertex];
//      System.out.println("actualDistance of " + vertex + " and " + nextVertex + " is "
//        + actualDistance);
      adjList = graph.getNbrsArray(nextVertex);
      startIndex = graph.getStartIndex(nextVertex);
      endIndex = graph.getEndIdnex(nextVertex);
//      for (int nbr : graph[nextVertex]) {
      for (int i = startIndex; i < endIndex; ++i) {
        nbr = adjList[i];
        if (0 == distances[nbr] && nbr != source) {
          bfsQueue[lastIndex++] = nbr;
          distances[nbr] = currentDist + 1;
        }
      }
    }
    return distances;
  }
  
  public static int[] getParallelBFSDistances(AbstractGraph graph, int source, int pL) {
    return new ParallelBFSRunner(graph, source, pL).computeBFSInParallel();
  }
  
  public static SimpleBFSData getBFSTree(int[][] graph, int source) {
    LinkedList<Integer> bfsQueue = new LinkedList<Integer>();
    bfsQueue.add(source);
    SimpleBFSData bfsData = new SimpleBFSData(graph.length, source);
    bfsData.initializeBFSData();
    bfsData.bfsLevel[source] = 0;
    int nextVertex, currentDist;
    while(!bfsQueue.isEmpty()) {
      nextVertex = bfsQueue.remove();
      currentDist = bfsData.bfsLevel[nextVertex];
//      System.out.println("actualDistance of " + vertex + " and " + nextVertex + " is "
//        + actualDistance);
      for (int nbr : graph[nextVertex]) {
        if (-1 == bfsData.bfsLevel[nbr]) {
          bfsQueue.add(nbr);
          bfsData.numVertices++;
          bfsData.bfsLevel[nbr] = currentDist + 1;
          bfsData.bfsParent[nbr] = nextVertex;
        }
      }
    }
    return bfsData;
  }

  
  public static int[] getBFSDistances(int[][] graph, int source) {
    LinkedList<Integer> bfsQueue = new LinkedList<Integer>();
    bfsQueue.add(source);
    int[] distances = new int[graph.length];
    int nextVertex, currentDist;
    while(!bfsQueue.isEmpty()) {
      nextVertex = bfsQueue.pop();
      currentDist = distances[nextVertex];
//      System.out.println("actualDistance of " + vertex + " and " + nextVertex + " is "
//        + actualDistance);
      for (int nbr : graph[nextVertex]) {
        if (0 == distances[nbr] && nbr != source) {
          bfsQueue.add(nbr);
          distances[nbr] = currentDist + 1;
        }
      }
    }
    return distances;
  }
}
