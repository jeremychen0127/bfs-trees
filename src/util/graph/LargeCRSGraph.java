package util.graph;

public class LargeCRSGraph extends AbstractGraph {
  long[] nbrIndices;
  int[][] nbrs;
  long javaMaxArraySize = Integer.MAX_VALUE - 5;

  public LargeCRSGraph(int[][] graph) {
    this.nbrIndices = new long[2*graph.length];
    this.numV = graph.length;

    
    long numNbrs = 0;
    for (int i = 0; i < graph.length; ++i) {
      numNbrs += graph[i].length;
    }
    
    int numNbrArrays = (int) Math.ceil((double) numNbrs / javaMaxArraySize);
    System.out.println("numNbrArrays: " + numNbrArrays);
    this.nbrs = new int[numNbrArrays][];
    for (int i = 0; i < numNbrArrays - 1; ++i) {
      this.nbrs[i] = new int[(int) javaMaxArraySize];
    }
//    System.out.println("numNbrs: " + numNbrs + " javaMaxArraySize: " + javaMaxArraySize +
//      " (numNbrs % javaMaxArraySize): " + (numNbrs % javaMaxArraySize));
    this.nbrs[numNbrArrays-1] = new int[(int) (numNbrs % javaMaxArraySize)];
    
//    this.nbrs = new long[numNbrs];
    long lastIndex = 0;
    for (int i = 0; i < graph.length; ++i) {
      if (graph[i].length == 0) {
        nbrIndices[2*i] = nbrIndices[2*i + 1] = -1;
        continue;
      }
      nbrIndices[2*i] = lastIndex;
      nbrIndices[2*i + 1] = lastIndex + graph[i].length;
      int firstArrayIndex;
      int secondArrayIndex;
      for (int j = 0; j < graph[i].length; ++j) {
        firstArrayIndex = (int) (lastIndex / javaMaxArraySize);
        secondArrayIndex = (int) (lastIndex % javaMaxArraySize);
//        if (firstArrayIndex >= nbrs.length) {
//          System.out.println("firstArrayIndex: " + firstArrayIndex + " is larger than: "
//            + nbrs.length + " lastIndex: " + lastIndex);
//        } else if (secondArrayIndex >= nbrs[firstArrayIndex].length){
//          System.out.println("secondArrayIndex: " + secondArrayIndex + " is larger than: "
//            + nbrs[firstArrayIndex].length + " lastIndex: " + lastIndex);
//        }
        nbrs[firstArrayIndex][secondArrayIndex] = graph[i][j];
        lastIndex++;
//        nbrs[lastIndex++] = graph[i][j];
      }
    }
    this.numE = nbrs.length;
    this.avgDegree = this.numE / this.numV;
  }

  @Override
  public int[] getNbrsArray(int v) {
    long beginNbrIndex = nbrIndices[2*v];
    long endNbrIndex = nbrIndices[2*v + 1];
    int nbrsArrayFirstIndex = (int) (beginNbrIndex / javaMaxArraySize);
    int nbrsArraySecondIndex = (int) (endNbrIndex / javaMaxArraySize);
    if (nbrsArrayFirstIndex == nbrsArraySecondIndex) {
      return nbrs[nbrsArrayFirstIndex];
    } else {
//      System.out.println("BoundaryVertex: " + v + " getNbrsArray() called."
//        + " beginNbrsIndex: " + beginNbrIndex + " endNbrsIndex: " + endNbrIndex
//        + " nbrsArrayFirstIndex: " + nbrsArrayFirstIndex
//        + " nbrsArraySecondIndex: " + nbrsArraySecondIndex);
      int[] boundaryArray = new int[(int) (endNbrIndex - beginNbrIndex)];
      int firstArrayOffset = (int) (beginNbrIndex % javaMaxArraySize);
      int lastPos = 0;
      for (int i = firstArrayOffset; i < javaMaxArraySize; ++i) {
        boundaryArray[lastPos++] = this.nbrs[nbrsArrayFirstIndex][i]; 
      }
      int secondArrayEndPos = (int) (endNbrIndex % javaMaxArraySize);
      for (int i = 0; i < secondArrayEndPos; ++i) {
        boundaryArray[lastPos++] = this.nbrs[nbrsArraySecondIndex][i];
      }
      return boundaryArray;
    }
  }
  
  @Override
  public int getStartIndex(int v) {
    return (int) (nbrIndices[2*v] % javaMaxArraySize);
  }

  @Override
  public int getEndIdnex(int v) {
    return (int) (nbrIndices[2*v + 1] % javaMaxArraySize);
  }

  @Override
  public int getDegree(int v) {
    return (int) (nbrIndices[2*v + 1] - nbrIndices[2*v]);
  }
}
