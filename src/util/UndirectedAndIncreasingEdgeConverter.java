package util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashSet;

public class UndirectedAndIncreasingEdgeConverter {

  /**
   * This class was written for our Generic Join work on Timely.
   */
  public static void main(String[] args) throws NumberFormatException, IOException {
    String fileName = args[0];
    BufferedReader br = Utils.getBufferedReader(fileName);
    FileOutputStream foutstream = new FileOutputStream(args[1]);
    DataOutputStream out = new DataOutputStream(foutstream);
    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
    int maxID = Utils.getMaxID(fileName);
    String strLine;
    int src, dst;
    ArrayList<HashSet<Integer>> graph = new ArrayList<HashSet<Integer>>(maxID + 1);
    for (int i = 0; i < maxID + 1; ++i) {
      graph.add(new HashSet<Integer>());
    }
    br = Utils.getBufferedReader(fileName);
    while ((strLine = br.readLine()) != null) {
      if (strLine.startsWith("#")) {
        continue;
      }
      String[] split = strLine.split("\\s+");
      src = Integer.parseInt(split[0]);
      dst = Integer.parseInt(split[1]);
      if (src < dst) {
        graph.get(src).add(dst);
      } else {
        graph.get(dst).add(src);
      }
    }

    for (int i = 0; i < graph.size(); ++i) {
      HashSet<Integer> neighbors = graph.get(i);
      for (int neighbor : neighbors) {
        bw.write(i + " " + neighbor + "\n");
      }
    }
    bw.close();
  }
}
