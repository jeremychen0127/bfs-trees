package util;

import java.io.IOException;

public class SingletonRemover {

  /**
   * This class works for undirected graphs.
   */
  public static void main(String[] args) throws NumberFormatException, IOException {
    String fileName = args[0];
    int[][] graph = Utils.getGraph(fileName);
    int[] newIDs = new int[graph.length];
    int nextID = 0;
    for (int i = 0; i < graph.length; ++i) {
      if (graph[i].length > 0) {
        newIDs[i] = nextID;
        nextID++;
      }
    }
    
    int[][] newGraph = new int[nextID][];
    for (int i = 0; i < graph.length; ++i) {
      if (graph[i].length > 0) {
        int[] newAdjList = new int[graph[i].length];
        for (int j = 0; j < graph[i].length; ++j) {
          newAdjList[j] = newIDs[graph[i][j]];
        }
        newGraph[newIDs[i]] = newAdjList;
      }
    }
    
    Utils.saveGraph(newGraph, args[1]);
  }
}
