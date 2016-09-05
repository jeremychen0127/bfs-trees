package util;

import java.util.Comparator;

public class Pair {
  public int id;
  public int degree;

  public Pair(int id, int degree) {
    this.id = id;
    this.degree = degree;
  }
  
  public static class PairComparator implements Comparator<Pair> {

    @Override
    public int compare(Pair o1, Pair o2) {
      if (o1.degree < o2.degree) { return -1; }
      else if (o1.degree > o2.degree) { return 1; }
      else return 0;
    }
    
  }
}
