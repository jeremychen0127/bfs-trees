package bfstreeindex;

import java.text.SimpleDateFormat;
import java.util.Date;

import util.SimpleBFSData;
import util.Utils;
import util.graph.AbstractGraph;

public class BaseConsistencyChecker {
  protected AbstractGraph graph;
  protected int[] permutedGraphIDs;
  protected SimpleBFSData[] bfsTrees;
  protected int parallelismLevel;
  protected int numIsConsistent;
  protected int numNotIsConsistent;
  protected byte[] consistencyArray;
  protected SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
  protected int numVerticesChecked = 0;
  protected long startTimeForNumVerticesChecked;
  protected int consistencyCheckerIndex;
  protected BaseConsistencyChecker(AbstractGraph graph, SimpleBFSData[] bfsTrees, int parallelismLevel,
    int consistencyCheckerIndex) {
    this.graph = graph;
    this.bfsTrees = bfsTrees;
    this.parallelismLevel = parallelismLevel;
    this.consistencyCheckerIndex = consistencyCheckerIndex;
    this.numIsConsistent = 0;
    this.numNotIsConsistent = 0;
    permutedGraphIDs = Utils.getPermutedIDs(graph.numV);
    this.consistencyArray = new byte[graph.numV];
    for (int i = 0; i < bfsTrees.length; ++i) {
      incrementNumIsConsistent(bfsTrees[i].source);
    }
    startTimeForNumVerticesChecked = System.currentTimeMillis();
  }
  
  public void incrementNumIsConsistent(int v) {
//    System.out.println("incrementing num is consistent for vertex ID: " + v);
    consistencyArray[v] = 1;
    this.numIsConsistent++;
    if ((this.numIsConsistent % 500) == 0) {
      System.out.println(sdf.format(new Date()) + " consistencyCheckerIndex: "
          + consistencyCheckerIndex + " numIsConsistent: " + this.numIsConsistent);
    }
  }
  
  public void incrementNumNotIsConsistent(int v) {
//    System.out.println("incrementing numnotisconsistent for vertex ID: " + v);
    consistencyArray[v] = -1;
    this.numNotIsConsistent++;
    if ((this.numNotIsConsistent % 5000) == 0) {
      System.out.println(sdf.format(new Date()) + " consistencyCheckerIndex: " 
        + consistencyCheckerIndex + " numNotIsConsistent: " + this.numNotIsConsistent);
    }
  }
  
  /**
   * @return indicates whether we are dumping debugging information
   */
  protected boolean incrementNumVerticesChecked() {
    numVerticesChecked++;
    if (numVerticesChecked % 1000 == 0) {
      System.out.println("numVerticesChecked: " + numVerticesChecked);
      System.out.println(sdf.format(new Date()) + " Time to check 1000 vertices: "
        + ((double) (System.currentTimeMillis() - startTimeForNumVerticesChecked)) / ((double) 1000) + " seconds.");
      System.out.println("numIsConsistent: " + numIsConsistent+ " numNotIsConsistent: "
        + numNotIsConsistent + " graph.length: " + graph.numV);
      double percentageChecked = (((double) numIsConsistent + (double)numNotIsConsistent)/
        ((double)graph.numV));
      System.out.println("percentageChecked: " + percentageChecked);
      if (percentageChecked > 1) {
        System.err.println("percentageChecked cannot be > 1. ");
        System.exit(-1);
      }
      startTimeForNumVerticesChecked = System.currentTimeMillis();
      return true;
    }
    return false;
  }
}
