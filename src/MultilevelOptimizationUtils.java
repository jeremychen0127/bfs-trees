import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;


public class MultilevelOptimizationUtils {

  public static Set<Integer> getRemovedVertices(boolean removeByDegree, String fileName, int numV,
    int numVToRemove) throws IOException {
    Set<Integer> removedV = new HashSet<Integer>();
    int numVerticesToRemove = Math.min(numV, numVToRemove);
    if (removeByDegree) {
      List<Pair> degrees = getDegrees(fileName, numV);
      long timeAfterDegrees = System.currentTimeMillis();
      Collections.sort(degrees, new Pair.SortBySecond());
      for (int i = 0; i < 3; ++i) {
        System.out.println("id: " + degrees.get(i).first + " degree: " + degrees.get(i).second);
      }
      for (int i = 0; i < Math.min(degrees.size(), numVerticesToRemove); ++i) {
        if (i == numVerticesToRemove - 1) {
          System.out.println("lowest degree vertex's degree: " + degrees.get(i).second);
        }
        removedV.add(degrees.get(i).first);
      }
    } else { // remove vertices randomly
      Random random = new Random();
      for (int i = 0; i < numVerticesToRemove; ++i) {
        removedV.add(random.nextInt(numV));
      }
    }
    return removedV;
  }

  /**
   * Returns <id, degree> pairs.
   */
  private static List<Pair> getDegrees(String fileName, int nv) throws NumberFormatException,
    IOException {
    List<Pair> degrees = new ArrayList<Pair>(nv);
    for (int i = 0; i < nv; ++i) {
      degrees.add(new Pair(i, 0));
    }
    BufferedReader br = new BufferedReader(new InputStreamReader(new DataInputStream(
      new FileInputStream(fileName))));
    String strLine;
    while ((strLine = br.readLine()) != null) {
      String[] split = strLine.split("\\s+");
      int src = Integer.parseInt(split[0]); 
      degrees.get(src).second = split.length - 1;
    }
    br.close();
    return degrees;
  }
}
