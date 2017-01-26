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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ArrayBlockingQueue;

import util.graph.AbstractGraph;
import util.graph.LargeCRSGraph;

public class Utils {

  public static String GPS_DATA_DIR ="/Users/semihsalihoglu/Desktop/research/stanford/databases/gps/data";
  public static String TMP_DIR ="/Users/semihsalihoglu/tmp";
  // "three_triangles.txt"
  public static String[] graphNames = {"p2p-Gnutella31-u-n.txt", "soc-Epinions1-u-n.txt",
    "amazon0601-u-n.txt", "com-youtube.ungraph-u-n.txt", "roadNet-CA-u-n.txt",
    "web-BerkStan-u-n.txt", "eDonkey2days2004-u-n.txt", "soc-LiveJournal1_full_u_n.txt",
    "com-orkut.ungraph-u-n.txt"};
  public static long numEdgesTraversed;
  public static BufferedReader getBufferedReader(String fileName) throws FileNotFoundException {
    FileInputStream fstream = new FileInputStream(fileName);
    DataInputStream in = new DataInputStream(fstream);
    BufferedReader br = new BufferedReader(new InputStreamReader(in));
    return br;
  }

  public static BufferedWriter getBufferedWriter(String fileName) throws FileNotFoundException {
    FileOutputStream fstream = new FileOutputStream(fileName);
    DataOutputStream in = new DataOutputStream(fstream);
    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(in));
    return bw;
  }

  public static int getMaxID(String inputFile) throws NumberFormatException, IOException {
    BufferedReader br = getBufferedReader(inputFile);
    String strLine;
    int maxID = -1;
    int src, dst;
    while ((strLine = br.readLine()) != null) {
      if (strLine.startsWith("#") || strLine.trim().isEmpty()) {
        continue;
      }
      String[] split = strLine.split("\\s+");
      src = Integer.parseInt(split[0]);
      if (src > maxID) {
        maxID = src;
      }
      if (split.length > 1) {
        dst = Integer.parseInt(split[1]);
        if (dst > maxID) {
          maxID = dst;
        }
      }
    }
    br.close();
//    System.out.println("MaxID: " + maxID);
    return maxID;
  }

  public static void permuteGraph(int[][] graph) {
    Random random = new Random();
    for (int i = 0; i < graph.length; ++i) {
      int nbrsSize = graph[i].length;
      int nextLocation, tmp;
      for (int j = 0; j < nbrsSize; ++j) {
        nextLocation = j + random.nextInt(nbrsSize-j);
        tmp = graph[i][nextLocation];
        graph[i][nextLocation] = graph[i][j];
        graph[i][j] = tmp;
      }
    }
  }

  public static LargeCRSGraph getCRSGraph(String fileName) throws NumberFormatException, IOException {
    int[][] graph = getGraph(fileName);
    return new LargeCRSGraph(graph);
  }

  public static int[][] getGraph(String fileName) throws NumberFormatException, IOException {
    int maxID = getMaxID(fileName);
    int[][] graph = new int[maxID + 1][];
    int[] degrees = new int[maxID + 1];
    long numEdges = 0;
    for (int i = 0; i < degrees.length; ++i) { degrees[i] = 0; graph[i] = new int[0];}
    BufferedReader br = getBufferedReader(fileName);
    String strLine;
    int src, dst, srcDegree, newDegreeSize;
    int numLinesRead = 0;
    long startTime = System.currentTimeMillis();

    // Reads the graph file line by line and construct graph data accordingly
    while ((strLine = br.readLine()) != null) {
      numLinesRead++;
      if ((numLinesRead % 100000) == 0) {
        long endTime = System.currentTimeMillis();
        System.out.println("Read " + numLinesRead + "th line. Time Taken: "
          + ((endTime - startTime)/1000) + " seconds.");
        startTime = System.currentTimeMillis();
      }
      if (strLine.startsWith("#")) {
        continue;
      }
      String[] split = strLine.split("\\s+");

      // Gets the index node
      src = Integer.parseInt(split[0]);
      srcDegree = degrees[src];

      // Initializes neighbours of the index node
      if ((srcDegree + split.length - 1) > graph[src].length) {
        newDegreeSize = Math.max(srcDegree + split.length - 1,
          (int) Math.round(graph[src].length*1.2));
        int[] newNeighbors = new int[newDegreeSize];
        System.arraycopy(graph[src], 0, newNeighbors, 0, graph[src].length);
        graph[src] = newNeighbors;
      }

      // Constructs neighbours of the index node
      for (int j = 1; j < split.length; ++j) {
        dst = Integer.parseInt(split[j]);
        graph[src][srcDegree++] = dst;
        numEdges++;
      }
      degrees[src] += split.length - 1;
    }

    int maxDegree = Integer.MIN_VALUE;
    for (int i = 0; i < graph.length; ++i) {
      int[] finalNbrs = new int[degrees[i]];
      System.arraycopy(graph[i], 0, finalNbrs, 0, degrees[i]);
      graph[i] = finalNbrs;
      if (degrees[i] > maxDegree) {
        maxDegree = degrees[i];
      }
    }
    System.out.println("maxID (close to numV): " + maxID + " maxDegree: " + maxDegree + " numE: "
      + numEdges + " numE/numV: " + ((double) numEdges)/(double) maxID);
    return graph;
  }

  public static int[][] reverseGraph(int[][] graph) {
    System.out.println("Generating reversed graph...");
    int[][] reversedGraph = new int[graph.length][];
    int[] degrees = new int[graph.length];
    for (int i = 0; i < degrees.length; ++i) { degrees[i] = 0; reversedGraph[i] = new int[0];}

    for(int i = 0; i < graph.length; ++i) {
      int[] currentNeighbors = graph[i];
      for(int j = 0; j < currentNeighbors.length; ++j) {
        int neighbor = currentNeighbors[j];
        degrees[neighbor]++;
        int[] newNeighbors = new int[degrees[neighbor]];
        System.arraycopy(reversedGraph[neighbor], 0, newNeighbors, 0, reversedGraph[neighbor].length);
        newNeighbors[degrees[neighbor] - 1] = i;
        reversedGraph[neighbor]= newNeighbors;
      }

      if ((i % 10000) == 0) {
        System.out.println(i + " vertices have been process");
      }
    }

    System.out.println("Reversed graph generated");

    return reversedGraph;
  }

  public static int getMaxDegreeVertex(int[][] graph) {
    int maxDegreeVertex = -1;
    int maxDegree = Integer.MIN_VALUE;
    for(int i = 0; i < graph.length; ++i) {
      if (graph[i] != null && graph[i].length > maxDegree) {
        maxDegreeVertex = i;
        maxDegree = graph[i].length;
      }
    }
    System.out.println("Max Degree Vertex: " + maxDegreeVertex + " maxDegree:" + maxDegree);
    return maxDegreeVertex;
  }

  public static int distanceInBFSTree(SimpleBFSData bfsData, int source, int destination) {
    int currentLevelOfSrc = bfsData.bfsLevel[source];
    int currentParentOfSrc = bfsData.bfsParent[source];
    int currentV = source;
    int levelofDest = bfsData.bfsLevel[destination];
    if (currentLevelOfSrc == -1 || levelofDest == -1) {
      return Integer.MAX_VALUE;
    }
    int currentParentOfDest = bfsData.bfsParent[destination];
    int length = 0;
//    System.out.println("currentLevelOfSrc: " + currentLevelOfSrc + " levelofDest: " + levelofDest);

    // Calculates the level offset of the source and destination
    while (currentLevelOfSrc > levelofDest) {
      currentLevelOfSrc--;
      currentV = currentParentOfSrc;
      currentParentOfSrc = bfsData.bfsParent[currentParentOfSrc];
//      System.out.println("Updating: currentParentOfSrc: " + currentParentOfSrc);
      length++;
    }
    while (levelofDest > currentLevelOfSrc) {
      levelofDest--;
      destination = currentParentOfDest;
      currentParentOfDest = bfsData.bfsParent[currentParentOfDest];
//      System.out.println("Updating: currentParentOfDest: " + currentParentOfDest);
      length++;
    }
    if (currentV == destination) {
      return length;
    }
//    System.out.println("currentParentOfV: " + currentParentOfV + " currentParentOfNbr: " + currentParentOfNbr);

    while(currentParentOfSrc != currentParentOfDest) {
//      System.out.println("Trying to find lowest ancestor. currentParentOfSrc: " +currentParentOfSrc
//        + " currentParentOfDest: " + currentParentOfDest);
      length += 2;
      currentParentOfSrc = bfsData.bfsParent[currentParentOfSrc];
      currentParentOfDest = bfsData.bfsParent[currentParentOfDest];
    }
    return length + 2;
  }

  public static boolean isPathThroughRoot(SimpleBFSData bfsData, int source, int destination) {
    int currentLevelOfSrc = bfsData.bfsLevel[source];
    int currentParentOfSrc = bfsData.bfsParent[source];
    int currentV = source;
    int levelofDest = bfsData.bfsLevel[destination];
    if (currentLevelOfSrc == -1 || levelofDest == -1) {
      return false;
    }
    int currentParentOfDest = bfsData.bfsParent[destination];

    while (currentLevelOfSrc > levelofDest) {
      currentLevelOfSrc--;
      currentV = currentParentOfSrc;
      currentParentOfSrc = bfsData.bfsParent[currentParentOfSrc];
    }
    while (levelofDest > currentLevelOfSrc) {
      levelofDest--;
      destination = currentParentOfDest;
      currentParentOfDest = bfsData.bfsParent[currentParentOfDest];
    }

    if (currentV == destination) {
      return currentV == bfsData.source;
    }

    while(currentParentOfSrc != currentParentOfDest) {
      currentParentOfSrc = bfsData.bfsParent[currentParentOfSrc];
      currentParentOfDest = bfsData.bfsParent[currentParentOfDest];
    }

    return currentParentOfSrc == bfsData.source;
  }

  public static int distanceInDirBFSTree(SimpleBFSData origBfsData, SimpleBFSData revBfsData, int source, int destination) {
    // find out which BFS that source vertex is in (orig vs reverse)
    SimpleBFSData sourceBfsData = revBfsData;
    boolean shouldBeSameBranch = false;
    int currentLevelOfSrc = revBfsData.bfsLevel[source];
    if (currentLevelOfSrc == -1) {
      currentLevelOfSrc = origBfsData.bfsLevel[source];
      shouldBeSameBranch = true;
      sourceBfsData = origBfsData;
    }

    int currentParentOfSrc = sourceBfsData.bfsParent[source];
    int currentLevelOfDest = origBfsData.bfsLevel[destination];
    if (currentLevelOfSrc == -1 || currentLevelOfDest == -1) {
      return Integer.MAX_VALUE;
    }
    int currentParentOfDest = origBfsData.bfsParent[destination];

    int length = 0;
    int currentV = source;
//    System.out.println("src: " + source + ", dest: " + destination);
    while (!shouldBeSameBranch && currentLevelOfSrc > currentLevelOfDest) {
//      System.out.println("SrcParent: " + currentParentOfSrc);
      currentLevelOfSrc--;
      currentV = currentParentOfSrc;
      currentParentOfSrc = sourceBfsData.bfsParent[currentParentOfSrc];
      length++;
    }
    while (currentLevelOfDest > currentLevelOfSrc) {
//      System.out.println("DestParent: " + currentParentOfDest);
      currentLevelOfDest--;
      destination = currentParentOfDest;
      currentParentOfDest = origBfsData.bfsParent[currentParentOfDest];
      length++;
    }
    if (currentV == destination) {
//      System.out.println("curV == dest: " + currentV);
      return length;
    } else if (shouldBeSameBranch) {
      return Integer.MAX_VALUE;
    }

    while(currentParentOfSrc != currentParentOfDest) {
      length += 2;
//      System.out.println("SrcParent: " + currentParentOfSrc + ", DestParent: " + currentParentOfDest);
      currentParentOfSrc = sourceBfsData.bfsParent[currentParentOfSrc];
      currentParentOfDest = origBfsData.bfsParent[currentParentOfDest];
    }

//    System.out.println("SrcParent: " + currentParentOfSrc + ", DestParent: " + currentParentOfDest);
    return length + 2;
  }

//  public static int[] getParallelCRSBFSDistances(int[][]graph, CRSGraph crsGraph, int source,
//     int pL) {
//    return new ParallelBFSRunner(graph, source, pL, crsGraph).computeBFSInParallel();
//  }

  // pL: parallelismLevel

  public static int getSSSDSPOneDirBFS(int[][] graph, int source, int destination) {
    int[] bfsLevels = new int[graph.length];
    for (int i = 0; i < graph.length; ++i) {
      bfsLevels[i] = -1;
    }
    ArrayBlockingQueue<Integer> bfsQueue = new ArrayBlockingQueue<Integer>(graph.length);
    bfsQueue.add(source);
    bfsLevels[source] = 0;
    int nextVertex, currentDist;
    int numEdgesTraversed = 0;
    while(!bfsQueue.isEmpty()) {
      nextVertex = bfsQueue.remove();
      currentDist = bfsLevels[nextVertex];
//      System.out.println("actualDistance of " + vertex + " and " + nextVertex + " is "
//        + actualDistance);
      for (int nbr : graph[nextVertex]) {
        numEdgesTraversed++;
        if (nbr == destination) {
//          System.out.println("OneDirectional numEdgesTraversed: " + numEdgesTraversed);
          Utils.numEdgesTraversed = numEdgesTraversed;
          return currentDist + 1;
        } else if (-1 == bfsLevels[nbr]) {
          bfsQueue.add(nbr);
          bfsLevels[nbr] = currentDist + 1;
        }
      }
    }
//    System.out.println("OneDirectional numEdgesTraversed: " + numEdgesTraversed);
    Utils.numEdgesTraversed = numEdgesTraversed;
    return -1;
  }

  public static void BiDirBFSInit(int[] fwBfsLevels, int[] bwBfsLevels) {
    for (int i = 0; i < fwBfsLevels.length; ++i) {
      fwBfsLevels[i] = -1;
      bwBfsLevels[i] = -1;
    }
  }

  public static int getSSSDSPBiDirBFS(int[][] graph, int[] fwBfsLevels, int[] bwBfsLevels, ArrayBlockingQueue<Integer> fwBfsQueue,
                                      ArrayBlockingQueue<Integer> bwBfsQueue, int src, int dest) {
    fwBfsQueue.add(src);
    fwBfsLevels[src] = 0;
    bwBfsQueue.add(dest);
    bwBfsLevels[dest] = 0;
    int nextVertex, currentDist;
    ArrayBlockingQueue<Integer> bfsQueue;
    int[] traversedBfsLevels, otherBfsLevels;
    int numEdgesTraversed = 0;
    int nextBFSStepDistance;
    while(!fwBfsQueue.isEmpty() && !bwBfsQueue.isEmpty()) {
      if (fwBfsQueue.size() <= bwBfsQueue.size()) {
//        System.out.println("Traversing FW Direction...");
        bfsQueue = fwBfsQueue;
        traversedBfsLevels = fwBfsLevels;
        otherBfsLevels = bwBfsLevels;
      } else {
//        System.out.println("Traversing BW Direction...");
        bfsQueue = bwBfsQueue;
        traversedBfsLevels = bwBfsLevels;
        otherBfsLevels = fwBfsLevels;
      }
      nextBFSStepDistance = traversedBfsLevels[bfsQueue.peek()];
      while (!bfsQueue.isEmpty() && traversedBfsLevels[bfsQueue.peek()] == nextBFSStepDistance) {
        nextVertex = bfsQueue.remove();
        currentDist = traversedBfsLevels[nextVertex];
        // System.out.println("actualDistance of " + vertex + " and " +
        // nextVertex + " is "
        // + actualDistance);
        for (int nbr : graph[nextVertex]) {
          numEdgesTraversed++;
          if (otherBfsLevels[nbr] >= 0) {
//            System.out.println("BiDirectional numEdgesTraversed: " + numEdgesTraversed);
            Utils.numEdgesTraversed = numEdgesTraversed;
            return otherBfsLevels[nbr] + currentDist + 1;
          } else if (-1 == traversedBfsLevels[nbr]) {
            bfsQueue.add(nbr);
            traversedBfsLevels[nbr] = currentDist + 1;
          }
        }
      }
    }
//    System.out.println("BiDirectional numEdgesTraversed: " + numEdgesTraversed);
    Utils.numEdgesTraversed = numEdgesTraversed;
    return -1;
  }

  public static int[][] getRandomKNeighborsGraph(int[][] graph, int k) {
    int[][] randomKNeighborGraph = new int[graph.length][];
    int[] neighbors;
    int[] randomKNeighbors;
    int randomIndex = -1;
    for (int i = 0; i < graph.length; ++i) {
      if (i > 0 && (i % 1000000) == 0) {
        System.out.println("Processing " + i + "th vertex for creating random k neighbors graph");
      }

      neighbors = graph[i];

      if (neighbors.length <= k) {
        randomKNeighborGraph[i] = neighbors;
      } else {
        randomKNeighbors = new int[k];
        Random random = new Random();
        for (int j = 0; j < k; ++j) {
          randomIndex = random.nextInt(neighbors.length);
          randomKNeighbors[j] = neighbors[randomIndex];
        }
        randomKNeighborGraph[i] = randomKNeighbors;
      }
    }

    return randomKNeighborGraph;
  }

  public static int[][] getKParentFreqNeighborsGraph(int[][] graph, ArrayList<TreeMap<Integer, Integer>> parentHistogram, int k) {
    int[][] kParentFreqNeighborsGraph = new int[parentHistogram.size()][];

    for (int i = 0; i < parentHistogram.size(); ++i) {
      if (i > 0 && (i % 1000000) == 0) {
        System.out.println("Processing " + i + "th vertex for creating k highest parent-frequency neighbors graph");
      }

      if (parentHistogram.get(i).size() <= k) {
        System.out.println("i:" + i + ", #neighbor:" + graph[i].length);
        kParentFreqNeighborsGraph[i] = new int[graph[i].length];
        System.arraycopy(graph[i], 0, kParentFreqNeighborsGraph[i], 0, graph[i].length);
      } else {
        kParentFreqNeighborsGraph[i] = getTopKNeighbors(parentHistogram.get(i), k);
      }
    }

    return kParentFreqNeighborsGraph;
  }

  public static int[] getTopKNeighbors(TreeMap<Integer, Integer> neighbors, int k) {
    if (neighbors.size() <= k) {
      System.out.println("ERROR: neighbors.length <= k, should have been handled");
    }
    Pair[] topKNeighbors = new Pair[k];
    for (int i = 0; i < topKNeighbors.length; ++i) {
      topKNeighbors[i] = new Pair(-1, -1);
    }
    Pair.PairComparator degreeComparator = new Pair.PairComparator();

    Integer neighborId = -1;
    Integer numAsParent = -1;
    for(Map.Entry<Integer,Integer> entry : neighbors.entrySet()) {
      neighborId = entry.getKey();
      numAsParent = entry.getValue();

      if (topKNeighbors[0].id == -1) {
        System.arraycopy(topKNeighbors, 1, topKNeighbors, 0, topKNeighbors.length - 1);
        topKNeighbors[topKNeighbors.length - 1] = new Pair(neighborId, numAsParent);
        Arrays.sort(topKNeighbors, degreeComparator);
      } else {
        if (numAsParent >= topKNeighbors[topKNeighbors.length - 1].degree) {
          System.arraycopy(topKNeighbors, 1, topKNeighbors, 0, topKNeighbors.length - 1);
          topKNeighbors[topKNeighbors.length - 1] = new Pair(neighborId, numAsParent);
        }
      }
    }

    int[] topKNeighborIds = new int[topKNeighbors.length];
    for (int i = 0; i < topKNeighbors.length; ++i) {
      if (topKNeighbors[i].id == -1) {
        System.out.println("ERROR: id is -1");
      }
      topKNeighborIds[i] = topKNeighbors[i].id;
    }

    return topKNeighborIds;
  }

  public static int[][] getKHighestDegreeNeighborsGraph(int[][] graph, int k) {
    int[][] kHighestDegreeNeighborsGraph = new int[graph.length][];
    int[] neighbors;
    Pair[] idDegrees;
    Pair.PairComparator degreeComparator = new Pair.PairComparator();
    for (int v = 0; v < graph.length; ++v) {
      if (v > 0 && (v % 1000000) == 0) {
        System.out.println("Processing " + v + "th vertex for creating k highest degree neighbors graph");
      }

      neighbors = new int[graph[v].length];
      System.arraycopy(graph[v], 0, neighbors, 0, graph[v].length);
      if (neighbors.length <= k) {
        kHighestDegreeNeighborsGraph[v] = neighbors;
      } else {
        idDegrees = new Pair[neighbors.length];
        for (int i = 0; i < neighbors.length; ++i) {
          idDegrees[i] = new Pair(neighbors[i], graph[neighbors[i]].length);
        }
        Arrays.sort(idDegrees, degreeComparator);

        int[] kHighestDegrees = new int[k];
        for (int i = 0; i < k; ++i) {
          kHighestDegrees[i] = idDegrees[neighbors.length - 1 - i].id;
        }
        kHighestDegreeNeighborsGraph[v] = kHighestDegrees;
      }
    }

    return kHighestDegreeNeighborsGraph;
  }

  public static int[][] getKHighestBCScoresNeighborsGraph(int[][] graph, int k) {
    int[][] kHighestBCScoresNeighborsGraph = new int[graph.length][];
    int[] neighbors;
    Pair[] idBCScores;
    Pair.ScoreComparator BCScoreComparator = new Pair.ScoreComparator();

    for (int v = 0; v < graph.length; ++v) {
      if (v > 0 && (v % 1000000) == 0) {
        System.out.println("Processing " + v + "th vertex for creating k BC neighbors graph");
      }

      neighbors = new int[graph[v].length];
      System.arraycopy(graph[v], 0, neighbors, 0, graph[v].length);
      if (neighbors.length <= k) {
        kHighestBCScoresNeighborsGraph[v] = neighbors;
      } else {
        idBCScores = new Pair[neighbors.length];
        for (int i = 0; i < neighbors.length; ++i) {
          idBCScores[i] = new Pair(neighbors[i], BCBFSData.BCVertexRunningSum[neighbors[i]]);
        }
        Arrays.sort(idBCScores, BCScoreComparator);

        int[] kHighestBCScores = new int[k];
        for (int i = 0; i < k; ++i) {
          kHighestBCScores[i] = idBCScores[neighbors.length - 1 - i].id;
        }
        kHighestBCScoresNeighborsGraph[v] = kHighestBCScores;
      }
    }

    return kHighestBCScoresNeighborsGraph;
  }

  public static boolean contains(int[] array, int value) {
    for (int num : array) {
      if (num == value) return true;
    }
    return false;
  }

  static boolean contains(String[] array, String value) {
    for (String str : array) {
      if (str.equals(value)) return true;
    }
    return false;
  }

  public static int getSSSDSPBiDirBFSDirGraph(int[][] origGraph, int[][] revGraph, int src, int dest) {
    int[] fwBfsLevels = new int[origGraph.length];
    int[] bwBfsLevels = new int[revGraph.length];
    for (int i = 0; i < origGraph.length; ++i) {
      fwBfsLevels[i] = -1;
      bwBfsLevels[i] = -1;
    }
    ArrayBlockingQueue<Integer> fwBfsQueue = new ArrayBlockingQueue<Integer>(origGraph.length);
    fwBfsQueue.add(src);
    fwBfsLevels[src] = 0;
    ArrayBlockingQueue<Integer> bwBfsQueue = new ArrayBlockingQueue<Integer>(revGraph.length);
    bwBfsQueue.add(dest);
    bwBfsLevels[dest] = 0;
    int nextVertex, currentDist;
    ArrayBlockingQueue<Integer> bfsQueue;
    int[] traversedBfsLevels, otherBfsLevels;
    int numEdgesTraversed = 0;
    int nextBFSStepDistance;
    int[][] graph;
    while(!fwBfsQueue.isEmpty() && !bwBfsQueue.isEmpty()) {
      if (fwBfsQueue.size() <= bwBfsQueue.size()) {
        bfsQueue = fwBfsQueue;
        traversedBfsLevels = fwBfsLevels;
        otherBfsLevels = bwBfsLevels;
        graph = origGraph;
      } else {
        bfsQueue = bwBfsQueue;
        traversedBfsLevels = bwBfsLevels;
        otherBfsLevels = fwBfsLevels;
        graph = revGraph;
      }
      nextBFSStepDistance = traversedBfsLevels[bfsQueue.peek()];
      while (!bfsQueue.isEmpty() && traversedBfsLevels[bfsQueue.peek()] == nextBFSStepDistance) {
        nextVertex = bfsQueue.remove();
        currentDist = traversedBfsLevels[nextVertex];
        for (int nbr : graph[nextVertex]) {
          numEdgesTraversed++;
          if (otherBfsLevels[nbr] >= 0) {
            System.out.println("BiDirectional numEdgesTraversed: " + numEdgesTraversed);
            Utils.numEdgesTraversed = numEdgesTraversed;
            return otherBfsLevels[nbr] + currentDist + 1;
          } else if (-1 == traversedBfsLevels[nbr]) {
            bfsQueue.add(nbr);
            traversedBfsLevels[nbr] = currentDist + 1;
          }
        }
      }
    }

    System.out.println("BiDirectional numEdgesTraversed: " + numEdgesTraversed);
    Utils.numEdgesTraversed = numEdgesTraversed;
    return -1;
  }

  public static ArrayList<TreeMap<Integer, Integer>> getParentHistogram(int[][] graph, SimpleBFSData[] bfsTrees) {
    ArrayList<TreeMap<Integer, Integer>> parentHistogram = new ArrayList<>();

    for (int v = 0; v < graph.length; v++) {
      if (v > 0 && (v % 1000000) == 0) {
        System.out.println("Processing " + v + "th vertex for initializing parent histogram.");
      }
      int[] neighbors = graph[v];
      TreeMap<Integer, Integer> parentHistogramForOneVertex = new TreeMap<>();
      for (int neighbor: neighbors) {
        parentHistogramForOneVertex.put(neighbor, 0);
      }
      parentHistogram.add(parentHistogramForOneVertex);
    }

    for (int i = 0; i < bfsTrees.length; ++i) {
      int[] parents = bfsTrees[i].bfsParent;
      for (int v = 0; v < parents.length; ++v) {
        if (v > 0 && (v % 1000000) == 0) {
          System.out.println("Processing " + v + "th vertex for " + i + "th tree as generating parent histogram");
        }
        if (parents[v] == -1) {
          continue;
        }

        int count = parentHistogram.get(v).get(parents[v]);
        parentHistogram.get(v).put(parents[v], count + 1);
      }
    }

    return parentHistogram;
  }

  public static double calculateCorrelation(int[][] graph1, int[][] graph2) {
    Set<Integer> graph1Set;
    Set<Integer> graph2Set;
    int sumCommon = 0;
    int totalNumTopNeighbors = 0;
    for (int i = 0; i < graph1.length; ++i) {
      graph1Set = new HashSet<>();
      graph2Set = new HashSet<>();

      for (int j = 0; j < graph1[i].length; ++j) {
        graph1Set.add(graph1[i][j]);
      }

      for (int j = 0; j < graph2[i].length; ++j) {
        graph2Set.add(graph2[i][j]);
      }

      graph1Set.retainAll(graph2Set);
      sumCommon += graph1Set.size();
      totalNumTopNeighbors += graph1[i].length;
    }

    return 1.0 * sumCommon / totalNumTopNeighbors;
  }

  public static void dumpGraph(int[][] graph) {
    for (int i = 0; i < graph.length; ++i) {
      System.out.print("" + i);
      for (int j = 0; j < graph[i].length; ++j) {
        System.out.print(" " + graph[i][j]);
      }
      System.out.println();
    }
  }

  public static int[] getPermutedIDs(int length) {
    int[] permutedIDs = new int[length];
    for (int i = 0; i < length; ++i) {
      permutedIDs[i] = i;
    }
    Random random = new Random();
    int tmp, nextRandom;
    for (int i = 0; i < length; ++i) {
      tmp = permutedIDs[i];
      nextRandom = i + random.nextInt(length - i);
      permutedIDs[i] = permutedIDs[nextRandom];
      permutedIDs[nextRandom] = tmp;
    }
    return permutedIDs;
  }

  public static void saveIsConsistentFile(byte[] consistencyArray, String isConsistentFilesDir,
                                          String graphName, int consistencyFileIndex)
    throws IOException {
    String isConsistentFileName = isConsistentFilesDir + "/"
      + graphName.substring(0, graphName.length() - 4) + "-" + consistencyFileIndex + ".txt";
    System.out.println("Saving is consistent file: " + isConsistentFileName);
    BufferedWriter bw = Utils.getBufferedWriter(isConsistentFileName);
    for (byte x : consistencyArray) {
      bw.write(x + " ");
    }
    bw.write("\n");
    bw.close();
  }

  public static void busySleep(long nanos)
  {
    long elapsed;
    final long startTime = System.nanoTime();
    do {
      elapsed = System.nanoTime() - startTime;
    } while (elapsed < nanos);
  }


  public static void saveGraph(int[][] graph, String outputFile) throws FileNotFoundException,
    IOException {
    FileOutputStream foutstream = new FileOutputStream(outputFile);
    DataOutputStream out = new DataOutputStream(foutstream);
    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
    for (int i = 0; i < graph.length; ++i) {
      int[] neighbors = graph[i];
      bw.write(i + "");
      for (int neighbor : neighbors) {
        bw.write(" " + neighbor);
      }
      bw.write("\n");
    }
    bw.close();
  }

  public enum BFSColor {
    UNVISITED,
    VISITED,
    VISITED_NOT_CONSISTENT;
  }
}
