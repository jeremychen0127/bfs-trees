package util.graph;

public abstract class AbstractGraph {

  public int numV;
  public long numE;
  public long avgDegree;
  
  public abstract int[] getNbrsArray(int v);
  
  public abstract int getStartIndex(int v);
  
  public abstract int getEndIdnex(int v);
  
  public abstract int getDegree(int v);
}
