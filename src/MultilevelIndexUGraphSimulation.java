import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class MultilevelIndexUGraphSimulation {
  public int[] vertices;
  public int[] degrees;
  public int[] edges;


  // TMP GRAPH
  //  String fileName = "/Users/semihsalihoglu/tmp/tmp-graph.txt";
  //  int n = 6; // for soc-epinions

  
  // General Directory: /Users/semihsalihoglu/Desktop/research/stanford/databases/gps/data/
  // SOC-EPINIONS
  //  String fileName = soc-Epinions1-u-n.txt;
  //  int n = 75889; // for soc-epinions
  
  // LIVE JOURNAL
  // soc-LiveJournal1_full_u_n.txt;
  // n = 4847571;
  
  // YOUTUBE social network
  // com-youtube.ungraph-u-n.txt
  // nV = 1157828
  
  // amazon0601-u-n: 
  // amazon0601-u-n.txt
  // nV = 403394
  
  // p2p-Gnutella31-u-n:
  // p2p-Gnutella31-u-n.txt
  // nV = 62586
  
  // web-BerkStan-u-n:
  // web-BerkStan-u-n.txt
  // nV = 685231
  
  // roadNet-CA-u-n:
  // roadNet-CA-u-n.txt
  // nv = 1971281
  
  // eDonkey2days2004-u-n:        eDonkey2days2004-u-n-l1:
  // eDonkey2days2004-u-n.txt     eDonkey2days2004-u-n-l1.txt
  // nv = 5792298
  
  // com-orkut.ungraph-u-n:
  // com-orkut.ungraph-u-n.txt
  // nv = 3072627
  public static void main(String[] args) throws NumberFormatException, IOException {
//    String dir = "/Users/semihsalihoglu/Desktop/research/stanford/databases/gps/data/";
    String dir = "/Users/semihsalihoglu/Desktop/research/waterloo/shortest-paths/multi-level-data/";    
    String file = dir + "com-youtube.ungraph-u-n-l1.txt";
    int nV = 1157828;
    int[] nvsToRemove = new int[] {0, 100, 1000, 10000, 20000, 50000};
    for (int nVToRemove : nvsToRemove) {
      removeVerticesAndFindCCs(file, nV, nVToRemove, true /* remove by degrees */);
    }
  }

  public static List<Pair> removeVerticesAndFindCCs(String inputFile, int nV, int nVToRemove,
    boolean removeByDegrees) throws IOException, FileNotFoundException {
    String strLine;
    Set<Integer> removedVs = MultilevelOptimizationUtils.getRemovedVertices(removeByDegrees,
      inputFile, nV, nVToRemove);
    UF uf = new UF(nV);
//    int numLinesParsed = 0;
    int src, dst;
    BufferedReader br = new BufferedReader(new InputStreamReader(new DataInputStream(
      new FileInputStream(inputFile))));
    while ((strLine = br.readLine()) != null) {
//      numLinesParsed++;
//        if ((numEdgesParsed % 1000000) == 0) {
//          System.out.println("parsed " + numEdgesParsed + " edges...");
//        }
      String[] split = strLine.split("\\s+");
      try {
        src = Integer.parseInt(split[0]);
        // Skipped all of the edges on this line if src is a removed vertex.
        if (removedVs.contains(src)) {
          continue;
        }
        for (int j = 1; j < split.length; ++j) {
          dst = Integer.parseInt(split[j]);
          if (removedVs.contains(dst)) {
            // Skipped this edge if dst is a removed vertex.
            continue;
          }
          uf.checkConnectedAndAddEdgeOrUnion(src, dst);
        }
      } catch (NumberFormatException nfe) {
        System.err.println(nfe.getMessage());
        System.err.println("Ignoring and continuing...");
      }
    }
    br.close();
    long timeAfterFindingComponents = System.currentTimeMillis();
//      System.out.println("Time taken to find components: "
//        + (timeAfterFindingComponents - timeAfterRemovingVertices));
    int[] components = uf.getParentComponents();
//      System.out.println("Number of components: " + components.length);
    List<Pair> componentSizes = new ArrayList<Pair>();
    for (int root : components) {
      if (uf.getESize(root) == 0) {
        continue;
      }
      componentSizes.add(new Pair(uf.getESize(root), uf.getVSize(root)));
    }
    Collections.sort(componentSizes, new Pair.SortByFirst());
    return componentSizes;
//    System.out.println("START: Printing Results for nvToRemove: " + nVToRemove);
//    System.out.println("Printing in decreasing sorted order of edges.");
//    for (int i = 0; i < Math.min(5, componentSizes.size()); ++i) {
//      System.out.println("Component : " + i + "\t V: " + componentSizes.get(i).second + "\t E: "
//        + componentSizes.get(i).first);
//    }
//
//    Collections.sort(componentSizes, new Pair.SortBySecond());
//    System.out.println("Printing in decreasing sorted order of vertices.");
//    for (int i = 0; i < Math.min(5, componentSizes.size()); ++i) {
//      System.out.println("Component : " + i + "\t V: " + componentSizes.get(i).second + "\t E: "
//        + componentSizes.get(i).first);
//    }
//    System.out.println("END: Printing Results for nvToRemove: " + nVToRemove);
  }

}
