package util.graph;

public class CRSGraph extends AbstractGraph {
  int[] nbrIndices;
  int[] nbrs;

  public CRSGraph(int[][] graph) {
    this.nbrIndices = new int[2*graph.length];
    this.numV = graph.length;

    
    int numNbrs = 0;
    for (int i = 0; i < graph.length; ++i) {
      numNbrs += graph[i].length;
    }
    
    this.nbrs = new int[numNbrs];
    int lastIndex = 0;
    for (int i = 0; i < graph.length; ++i) {
      if (graph[i].length == 0) {
        nbrIndices[2*i] = nbrIndices[2*i + 1] = -1;
        continue;
      }
      nbrIndices[2*i] = lastIndex;
      nbrIndices[2*i + 1] = lastIndex + graph[i].length;
      for (int j = 0; j < graph[i].length; ++j) {
        nbrs[lastIndex++] = graph[i][j];
      }
    }
    this.numE = nbrs.length;
    this.avgDegree = this.numE / this.numV;
  }

  @Override
  public int[] getNbrsArray(int v) {
    return nbrs;
  }
  
  @Override
  public int getStartIndex(int v) {
    return nbrIndices[2*v];
  }

  @Override
  public int getEndIdnex(int v) {
    return nbrIndices[2*v + 1];
  }

  @Override
  public int getDegree(int v) {
    return nbrIndices[2*v + 1] - nbrIndices[2*v];
  }
}
