package util;

import java.util.ArrayList;

public class BCBFSData {
  public ArrayList<Integer> level = null;
  public ArrayList<ArrayList<Integer>> parents = null;
  public ArrayList<ArrayList<Integer>> verticesOnLevel = null;
  public ArrayList<Integer> numShortestPaths = null;
  public double[] BCFromThisSource = null;
  public static double[] BCVertexRunningSum = null;
  public static double BCGlobalSum;
  public int source;

  public int numVertices;

  public BCBFSData(int numV, int source) {
    level = new ArrayList<>();
    parents = new ArrayList<>();
    verticesOnLevel = new ArrayList<>();
    numShortestPaths = new ArrayList<>();
    BCFromThisSource = new double[numV];
    this.source = source;
    this.numVertices = 1;
  }

  public void initializeBFSData(int numV) {
    for (int i = 0; i < numV; ++i) {
      level.add(-1);
      parents.add(null);
      numShortestPaths.add(0);
      BCFromThisSource[i] = 0;
    }
  }

  public static void initializeBCScores(int numV) {
    BCVertexRunningSum = new double[numV];
    for (int i = 0; i < numV; ++i) {
      BCVertexRunningSum[i] = 0;
    }
    BCGlobalSum = 0;
  }

  public void calculateBCScore() {
    double scoreFromChild;
    int v;
    int parent;
    for (int l = verticesOnLevel.size() - 1; l > 0; --l) {
      for (int i = 0; i < verticesOnLevel.get(l).size(); ++i) {
        v = verticesOnLevel.get(l).get(i);
        for (int p = 0; p < parents.get(v).size(); ++p) {
          parent = parents.get(v).get(p);
          scoreFromChild = (1.0 * numShortestPaths.get(parent) / numShortestPaths.get(v)) * (1 + BCFromThisSource[v]);
          BCFromThisSource[parent] += scoreFromChild;
          BCVertexRunningSum[parent] += scoreFromChild;
          BCGlobalSum += scoreFromChild;
        }
      }
    }
  }
}
