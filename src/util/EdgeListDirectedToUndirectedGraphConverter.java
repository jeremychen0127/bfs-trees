package util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigInteger;

public class EdgeListDirectedToUndirectedGraphConverter {
  public static void main(String[] args) throws IOException {
    String fileName = args[0];
    BufferedReader br = Utils.getBufferedReader(fileName);
    String outputFile = args[1];
    BufferedWriter bw = Utils.getBufferedWriter(outputFile);
    String strLine;
    String v1, v2;

    int numLinesParsed = 0;
    while ((strLine = br.readLine()) != null) {
      numLinesParsed++;
      if ((numLinesParsed % 100000) == 0) {
        System.out.println("Parsed " + numLinesParsed + "th line...");
      }
      if (strLine.startsWith("#") || strLine.trim().isEmpty()) {
        continue;
      }

      String[] split = strLine.split("\\s+");
      v1 = split[0];
      v2 = split[1];

      if (numLinesParsed != 1) {
        bw.newLine();
      }
      bw.write(v1 + " " + v2);
      bw.newLine();
      bw.write(v2 + " " + v1);
    }

    br.close();
    bw.close();
  }
}
