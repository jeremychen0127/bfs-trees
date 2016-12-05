package util;

import java.util.ArrayList;
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

  public static BCBFSData getBCBFSTree(int[][] graph, int source) {
    LinkedList<Integer> bfsQueue = new LinkedList<Integer>();
    bfsQueue.add(source);
    BCBFSData bcbfsData = new BCBFSData(graph.length, source);

    bcbfsData.initializeBFSData(graph.length);
    bcbfsData.level.set(source, 0);
    ArrayList<Integer> levelZero = new ArrayList<>();
    levelZero.add(source);
    bcbfsData.verticesOnLevel.add(levelZero);
    bcbfsData.numShortestPaths.set(source, 1);

    int nextVertex, currentLevel;
    while (!bfsQueue.isEmpty()) {
      nextVertex = bfsQueue.remove();
      currentLevel = bcbfsData.level.get(nextVertex);

      for (int neighbor: graph[nextVertex]) {
        if (bcbfsData.level.get(neighbor) == -1) {
          bfsQueue.add(neighbor);
          bcbfsData.numVertices++;
          bcbfsData.level.set(neighbor, currentLevel + 1);

          // add to corresponding level set
          while (bcbfsData.verticesOnLevel.size() - 1 < currentLevel + 1) {
            bcbfsData.verticesOnLevel.add(new ArrayList<Integer>());
          }
          bcbfsData.verticesOnLevel.get(currentLevel + 1).add(neighbor);

          // add nextVertex as a parent
          if (bcbfsData.parents.get(neighbor) == null) {
            bcbfsData.parents.set(neighbor, new ArrayList<Integer>());
          }
          bcbfsData.parents.get(neighbor).add(nextVertex);

          bcbfsData.numShortestPaths.set(neighbor, 1);
        } else if (bcbfsData.level.get(neighbor) > bcbfsData.level.get(nextVertex)) {
          // add nextVertex as another parent
          bcbfsData.parents.get(neighbor).add(nextVertex);

          // increment number of shortest paths from source to this neighbor
          bcbfsData.numShortestPaths.set(neighbor, bcbfsData.numShortestPaths.get(neighbor) + 1);
        }
      }
    }

    return bcbfsData;
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
