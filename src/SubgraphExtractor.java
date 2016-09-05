import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

public class SubgraphExtractor {

  public static void main(String[] args) throws IOException {
    String inputFile = "/Users/semihsalihoglu/Desktop/research/stanford/databases/"
      + "gps/data/com-youtube.ungraph-u-n.txt";
    String outputFile = "/Users/semihsalihoglu/Desktop/research/waterloo/shortest-paths/"
      + "multi-level-data/com-youtube.ungraph-u-n-l1.txt";
    String strLine;
    int nV = 1157828;
    int nVToRemove = 100000;
    extractSubgraph(inputFile, outputFile, nV, nVToRemove);
  }

  public static void extractSubgraph(String inputFile, String outputFile, int nV, int nVToRemove)
    throws IOException, FileNotFoundException {
    String strLine;
    Set<Integer> removedVs = MultilevelOptimizationUtils.getRemovedVertices(
      true /* remove by degrees */, inputFile, nV, nVToRemove);
    HashMap<Integer, List<Integer>> removedSubgraph = null;
    removedSubgraph = new HashMap<Integer, List<Integer>>();
    for (int removedV : removedVs) {
      removedSubgraph.put(removedV, new ArrayList<Integer>());
    }

    BufferedReader br = new BufferedReader(new InputStreamReader(new DataInputStream(
      new FileInputStream(inputFile))));
    int src, dst;
    boolean containsSrc;
    while ((strLine = br.readLine()) != null) {
      String[] split = strLine.split("\\s+");
      try {
        src = Integer.parseInt(split[0]);
        // Skipped all of the edges on this line if src is a removed vertex.
        containsSrc = removedVs.contains(src);
        if (!containsSrc) {
          continue;
        }
        for (int j = 1; j < split.length; ++j) {
          dst = Integer.parseInt(split[j]);
          if (removedVs.contains(dst)) {
            removedSubgraph.get(src).add(dst);
          }
        }
      } catch (NumberFormatException nfe) {
        System.err.println(nfe.getMessage());
        System.err.println("Ignoring and continuing...");
      }
    }
    
    BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
    for (Entry<Integer, List<Integer>> entry : removedSubgraph.entrySet()) {
      bw.write("" + entry.getKey());
      for (int nbr : entry.getValue()) {
        bw.write(" " + nbr);
      }
      bw.write("\n");
    }
    bw.close();
  }
}
