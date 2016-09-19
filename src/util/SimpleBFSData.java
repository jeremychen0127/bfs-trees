package util;

public class SimpleBFSData {
  public int[] bfsLevel = null;
  public int[] bfsParent = null;
  public int source;

  public int numVertices;

  public SimpleBFSData(int numV, int source) {
    bfsLevel = new int[numV];
    bfsParent = new int[numV];
    this.source = source;
    this.numVertices = 1;
  }
  
  public void initializeBFSData() {
    for (int i = 0; i < bfsLevel.length; ++i) {
      bfsLevel[i] = -1;
      bfsParent[i] = -1;
    }
  }
  
  public void dumpBFSData() {
    for (int i = 0; i < bfsLevel.length; ++i) {
      System.out.println("v: " + i + " bfsLevel: " + bfsLevel[i] + " bfsParent: " + bfsParent[i]);
    }
  }
}
