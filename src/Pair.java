import java.util.Comparator;

public class Pair {
  int first;
  int second;

  public Pair(int edges, int vertices) {
    this.first = edges;
    this.second = vertices;
  }

  public static class SortByFirst implements Comparator<Pair> {

    public int compare(Pair c1, Pair c2) {
      return c2.first - c1.first;
    }
  }

  public static class SortBySecond implements Comparator<Pair> {

    public int compare(Pair c1, Pair c2) {
      return c2.second - c1.second;
    }
  }
}
