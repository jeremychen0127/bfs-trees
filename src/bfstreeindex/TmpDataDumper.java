package bfstreeindex;

import java.io.IOException;
import java.util.Random;

import util.Utils;

public class TmpDataDumper {
  public static void main(String[] args) throws NumberFormatException, IOException {
//    int[][] graph = Utils.getGraph(Utils.GPS_DATA_DIR + "/com-orkut.ungraph-u-n.txt");
//    int numVertices = graph.length;
//    System.out.println("numVertices: " + numVertices);
//    int source = Utils.getMaxDegreeVertex(graph);
//    BFSRunner bfs = new BFSRunner(graph);
//    bfs.runBFS(source);
//    bfs.summarizeLevelData();
//    bfs.summarizeParentsData();
//    int[][] foo = new int[1][3];
//    MultipleBFSTreesIndexTester.loadIsConsistentFiles(foo, "foo");
//    for (int x : foo[0]) {
//      System.out.println("" + x);2
//    }
//    String graphName = "soc-Epinions1-u-n.txt";
//    System.out.println(graphName.substring(0, graphName.length()-4));
    int graphLength = 75887; //403393; //1157827;// 685230; // 75887;
    int numIsConsistentArrays = 757;
    int[][] isConsistent = new int[numIsConsistentArrays][graphLength + 1];
    String graphName = "soc-Epinions1-u-n.txt"; //"amazon0601-u-n.txt"; //Utils.graphNames[2];
    MultipleBFSTreesIndexTester.loadIsConsistencyFiles(
      "/Users/semihsalihoglu/Desktop/research/waterloo/"
      + "shortest-paths/bfs-tree-index-data/is-consistent-files/soc-Epinions1-u-n/",
      isConsistent,
      graphName.substring(0, graphName.length()-4));
    Random random = new Random(0);
    int src, dest;
    int numTrials = 50000;
    int[] numIsConsistentPairs = new int[numIsConsistentArrays];
    for (int i = 0; i < numTrials; ++i) {
      src = random.nextInt(graphLength);
      dest = random.nextInt(graphLength);
      for (int j = 0; j < numIsConsistentArrays; ++j) {
        if ((isConsistent[j][src] == 1) && (isConsistent[j][dest] == 1)) {
          for (int k = j; k < numIsConsistentArrays; ++k) {
            numIsConsistentPairs[k]++;
          }
          break;
        }
      }
    }
    for (int i = 0; i < numIsConsistentArrays; ++i) {
        System.out.println("numTrees: " + i + " numIsConsistentPairs: "
          + numIsConsistentPairs[i] +
          " percentage: " + ((double) numIsConsistentPairs[i] / numTrials));
    }
//    }
  }
}
