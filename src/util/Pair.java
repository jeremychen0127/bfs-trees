package util;

import java.util.Comparator;

public class Pair {
  public int id;
  public int degree;
  public double score;

  public Pair(int id, int degree) {
    this.id = id;
    this.degree = degree;
  }

  public Pair(int id, double score) {
    this.id = id;
    this.score = score;
  }

  public static class ScoreComparator implements Comparator<Pair> {
    @Override
    public int compare(Pair p1, Pair p2) {
      if (p1.score < p2.score) { return -1; }
      else if (p1.score > p2.score) { return 1; }
      else return 0;
    }
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
