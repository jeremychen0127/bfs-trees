package bfs.bidirectional;

import util.BitMap;
import util.graph.AbstractGraph;

public class BiDirectionalBFSRunner {
  BitMap visited;
  int[] frontier1;
  int[] frontier2;
  int currentFrontierID; // 1 or 2
  private AbstractGraph graph;
  int nFrontier;
  int mFrontier;
  long mRemaining;
  
  public BiDirectionalBFSRunner(AbstractGraph graph) {
    this.graph = graph;
    visited = new BitMap(graph.numV);
    frontier1 = new int[graph.numV];
    frontier2 = new int[graph.numV];
    currentFrontierID = 1;
  }
  
  public void initiliazeBFS() {
    visited.reset();
    nFrontier = 0;
    mFrontier = 0;
    mRemaining = graph.numE;
  }
  
  public int[] runBFS(int source) {
    initiliazeBFS();
    int[] distances = new int[graph.numV];
    getCurrentFrontier()[0] = source;
    visited.set(source);
    nFrontier++;
    mRemaining -= graph.getDegree(source);
    int currentDist = 1;
    runTopDownStep(distances, currentDist++);
//    dumpStats();
    while(nFrontier > 0) {
      if (nFrontier < (graph.numV/25)) {
//      System.out.println("RUNNING a new BFS iteration...");
        runTopDownStep(distances, currentDist++);
      } else {
        if (mFrontier > (mRemaining/14)) {
          runBottomUpStep(distances, currentDist++);
        } else {
          runTopDownStep(distances, currentDist++);
        }
      }
//      dumpStats();
    }
    return distances;
  }
  
  private void dumpStats() {
    System.out.println("nFrontier: " + nFrontier + " mFrontier: " + mFrontier + " mRemaining: "
      + mRemaining + " totalEdges: " + (mFrontier + mRemaining));
  }

  private void runBottomUpStep(int[] distances, int currentDist) {
//    System.out.println("RUNNING STEP: " + currentDist + " BOTTOM UP.");
    int[] nextFrontier = getNextFrontier();
    nFrontier = 0;
    mFrontier = 0;
    int[] adjList;
    int startIndex;
    int endIndex;
    int nbr;
    BitMap newVisited = new BitMap(visited);
    for (int nextVertex = 0; nextVertex < graph.numV; ++nextVertex) {
      if (visited.get(nextVertex) == 0) {
        adjList = graph.getNbrsArray(nextVertex);
        startIndex = graph.getStartIndex(nextVertex);
        endIndex = graph.getEndIdnex(nextVertex);
//        for (int nbr : graph[nextVertex]) {
        int nextVDegree;
        for (int i = startIndex; i < endIndex; ++i) {
          nbr = adjList[i];
          if (visited.get(nbr) == 1) {
            nextFrontier[nFrontier++] = nextVertex;
            newVisited.set(nextVertex);
            distances[nextVertex] = currentDist;
            nextVDegree = graph.getDegree(nextVertex);
            mFrontier += nextVDegree;
            mRemaining -= nextVDegree;
            break;
          }
        }
      }
    }
    visited = newVisited;
    swapFrontierIDs();
  }

  private void runTopDownStep(int[] distances, int currentDist) {
//    System.out.println("RUNNING STEP: " + currentDist + " TOP DOWN.");
    int[] currentFrontier = getCurrentFrontier();
    int[] nextFrontier = getNextFrontier();
    int currentIndex = 0;
    int lastIndex = nFrontier;
    nFrontier = 0;
    mFrontier = 0; // We'll compute this next
    int nextVertex;
    int[] adjList;
    int startIndex;
    int endIndex;
    int nbr;
    while(currentIndex != lastIndex) {
      nextVertex = currentFrontier[currentIndex++];
//      currentDist = distances[nextVertex];
//      System.out.println("actualDistance of " + vertex + " and " + nextVertex + " is "
//        + actualDistance);
      adjList = graph.getNbrsArray(nextVertex);
      startIndex = graph.getStartIndex(nextVertex);
      endIndex = graph.getEndIdnex(nextVertex);
//      for (int nbr : graph[nextVertex]) {
      int nbrDegree;
      for (int i = startIndex; i < endIndex; ++i) {
        nbr = adjList[i];
        if (visited.get(nbr) == 0) {
          nextFrontier[nFrontier++] = nbr;
          visited.set(nbr);
          distances[nbr] = currentDist;
          nbrDegree = graph.getDegree(nbr);
          mFrontier += nbrDegree;
          mRemaining -= nbrDegree;
        }
      }
    }
    swapFrontierIDs();
  }
  
  private void swapFrontierIDs () {
    currentFrontierID = (currentFrontierID == 1) ? 2 : 1;
  }

  private int[] getCurrentFrontier() {
    return (currentFrontierID == 1) ? frontier1 : frontier2;
  }
  
  private int[] getNextFrontier() {
    return (currentFrontierID == 1) ? frontier2 : frontier1;
  }
}
