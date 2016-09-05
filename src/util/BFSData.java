package util;

import util.Utils.BFSColor;

public class BFSData extends SimpleBFSData {
  public BFSColor[] bfsColor = null;
  public BFSData(int numV, int source) {
    super(numV, source);
    bfsColor = new BFSColor[numV];
  }
  
  public void initializeBFSData() {
    super.initializeBFSData();
    for (int i = 0; i < bfsColor.length; ++i) {
      bfsColor[i] = BFSColor.UNVISITED;
    }
  }
  
  public void dumpBFSData() {
    for (int i = 0; i < bfsLevel.length; ++i) {
      System.out.println("v: " + i + " bfsLevel: " + bfsLevel[i] + " bfsParent: " + bfsParent[i]
        + " bfsColor: " + bfsColor[i]);
    }
  }
}
