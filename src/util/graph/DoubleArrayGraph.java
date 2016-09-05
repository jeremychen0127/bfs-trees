package util.graph;

public class DoubleArrayGraph extends AbstractGraph {

  private int[][] graph;

  public DoubleArrayGraph(int[][] graph) {
    this.graph = graph;
    this.numV = graph.length;
    this.numE = 0;
    for (int i = 0; i < graph.length; ++i) {
      this.numE += graph[i].length;
    }
    this.avgDegree = this.numE / this.numV;
  }

  @Override
  public int[] getNbrsArray(int v) {
    return graph[v];
  }

  @Override
  public int getStartIndex(int v) {
    return 0;
  }

  @Override
  public int getEndIdnex(int v) {
    return graph[v].length;
  }

  @Override
  public int getDegree(int v) {
    return graph[v].length;
  }

}
