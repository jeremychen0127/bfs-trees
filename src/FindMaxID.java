import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class FindMaxID {

  public static void main(String[] args) throws IOException {
    findMaxIDNumVerticesAndNumEdges("/Users/semihsalihoglu/Desktop/research/"
      + "stanford/databases/gps/data/com-orkut.ungraph-u-n.txt");
  }

  public static Pair findMaxIDNumVerticesAndNumEdges(String graphFile) throws FileNotFoundException, IOException {
    FileInputStream fstream = new FileInputStream(graphFile);
    DataInputStream in = new DataInputStream(fstream);
    BufferedReader br = new BufferedReader(new InputStreamReader(in));
    String strLine;
    int maxID = -1;
    int id;
    int numEdges = 0;
    while ((strLine = br.readLine()) != null) {
      String[] split = strLine.split("\\s+");
      numEdges += split.length - 1;
      for (String str : split) {
        id = Integer.parseInt(str);
        if (id > maxID) {
          maxID = id;
        }
      }
    }
    System.out.println("MaxID: " + maxID);
    System.out.println("numEdges: " + numEdges);
    // Pair used to return two integers back.
    return new Pair(numEdges, maxID);
  }
}
