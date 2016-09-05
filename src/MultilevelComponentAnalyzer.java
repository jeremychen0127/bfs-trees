import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;


public class MultilevelComponentAnalyzer {

  private static String[] fileNames = {
    "soc-Epinions1-u-n",
    "soc-LiveJournal1_full_u_n",
    "com-youtube.ungraph-u-n",
    "amazon0601-u-n",
    "p2p-Gnutella31-u-n",
    "web-BerkStan-u-n",
    "roadNet-CA-u-n",
    "eDonkey2days2004-u-n"};
  private static int[] nvs = {
    75889,
    4847571,
    1157828,
    403394,
    62586,
    685231,
    1971281,
    5792298};
  private static int[] nvsToRemove = {
    8000,
    1000000,
    100000,
    200000,
    14000,
    200000,
    300000,
    100000};
  // Make sure you put 0 as the first size of the graph to remove
  private static int[][] nvsToRemoveFromLevelOne = {
    new int[] {0,100, 1000, 2000, 4000},
    new int[] {0, 100, 1000, 10000, 100000, 200000, 500000},
    new int[] {0, 100, 1000, 10000, 20000, 50000},
    new int[] {0, 100, 1000, 10000, 50000, 100000},
    new int[] {0, 100, 1000, 2000, 4000, 7000},
    new int[] {0, 100, 1000, 10000, 50000, 100000},
    new int[] {0, 100, 1000, 10000, 50000, 100000, 150000},
    new int[] {0, 100, 1000, 10000, 20000, 50000}};
  private static String dataDir = "/Users/semihsalihoglu/Desktop/research/stanford/databases/"
    + "gps/data/";
  private static String multilevelDir = "/Users/semihsalihoglu/Desktop/research/waterloo/"
    + "shortest-paths/multi-level-data/";
  public static void main(String[] args) throws FileNotFoundException, IOException { 
    Pair[] level0GraphSizes = new Pair[fileNames.length];
    Pair[] level0CCSizesAfterRemoval = new Pair[fileNames.length];
    Pair[] level1GraphSizes = new Pair[fileNames.length];
    Pair[][] level1CCSizes = new Pair[fileNames.length][];
    Pair[][] level2GraphSizes = new Pair[fileNames.length][];
    Pair[][] level2CCSizes = new Pair[fileNames.length][];
    Pair[][] level3GraphSizes = new Pair[fileNames.length][];
    for(int i = 0; i < fileNames.length; ++i) {
      String fileName = fileNames[i];
      String originalGraphFile = dataDir + fileName + ".txt";
      level0GraphSizes[i] = FindMaxID.findMaxIDNumVerticesAndNumEdges(originalGraphFile);

      level0CCSizesAfterRemoval[i] = MultilevelIndexUGraphSimulation.removeVerticesAndFindCCs(
        originalGraphFile, nvs[i], nvsToRemove[i], true /* remove by degree */).get(0);

      String level1OutputFile = multilevelDir + fileName + "-" + nvsToRemove[i] + "removed-l1.txt";
      SubgraphExtractor.extractSubgraph(originalGraphFile, level1OutputFile, nvs[i],
        nvsToRemove[i]);
      
      level1GraphSizes[i] = FindMaxID.findMaxIDNumVerticesAndNumEdges(level1OutputFile);

      level1CCSizes[i] = new Pair[nvsToRemoveFromLevelOne[i].length];
      level2GraphSizes[i] = new Pair[nvsToRemoveFromLevelOne[i].length];
      level2CCSizes[i] = new Pair[nvsToRemoveFromLevelOne[i].length];
      level3GraphSizes[i] = new Pair[nvsToRemoveFromLevelOne[i].length];
      for (int j = 0; j < nvsToRemoveFromLevelOne[i].length; ++j) {
        level1CCSizes[i][j] = MultilevelIndexUGraphSimulation.removeVerticesAndFindCCs(
          level1OutputFile, nvs[i], nvsToRemoveFromLevelOne[i][j],
          true /* remove by degree */).get(0);
        
        String level2OutputFile = multilevelDir + fileName + "-" + nvsToRemove[i] + "-rm-"
          + nvsToRemoveFromLevelOne[i][j] + "-l1rm-l2"+ ".txt";
        SubgraphExtractor.extractSubgraph(level1OutputFile, level2OutputFile, nvs[i], 
          nvsToRemoveFromLevelOne[i][j]);
        
        Pair level2Component = FindMaxID.findMaxIDNumVerticesAndNumEdges(level2OutputFile);
        level2GraphSizes[i][j] = new Pair(level2Component.first, nvsToRemoveFromLevelOne[i][j]);

        int numLevel2VToRemove = nvsToRemoveFromLevelOne[i][j]/2;
//        System.out.println("nvsToRemoveFromLevelOne[i][j]: " + nvsToRemoveFromLevelOne[i][j] + " numLevel2VToRemove: " + numLevel2VToRemove);
        List<Pair> level2CCs = MultilevelIndexUGraphSimulation.removeVerticesAndFindCCs(level2OutputFile, nvs[i],
          numLevel2VToRemove, true /* remove by degree */);
        if (!level2CCs.isEmpty()) {
          level2CCSizes[i][j] = level2CCs.get(0);
        } else {
          level2CCSizes[i][j] = new Pair(0, 0);
        }
        String level3OutputFile = multilevelDir + fileName + "-" + nvsToRemove[i] + "-rm-"
          + nvsToRemoveFromLevelOne[i][j] + "-l1rm-" + numLevel2VToRemove + "l2rm-l3.txt";
        SubgraphExtractor.extractSubgraph(level2OutputFile, level3OutputFile, nvs[i], 
          numLevel2VToRemove);
        Pair level3Component = FindMaxID.findMaxIDNumVerticesAndNumEdges(level3OutputFile);
        level3GraphSizes[i][j] = new Pair(level3Component.first, numLevel2VToRemove);
      }
    }

    System.out.println("START OF DUMPING RESULTS...");
    for(int i = 0; i < fileNames.length; ++i) {
      System.out.println(fileNames[i]);
//      System.out.println("Level0 :");
//      System.out.println("RemovedV: 0 " + level0GraphSizes[i].second + " " + level0GraphSizes[i].first);
//      System.out.println("RemovedV: " + nvsToRemove[i] + " " + level0CCSizesAfterRemoval[i].second + " " +
//        level0CCSizesAfterRemoval[i].first);
//      System.out.println("Level1:");     
      System.out.println(
        "L0-G-V\tL0-G-E\tL0-RmV\tL0-RCC-V\tL0-RCC-E\t" +
        "L1-G-V\tL1-G-E\tL1-RmV\tL1-RCC-V\tL1-RCC-E\t" + 
        "L2-G-V\tL2-G-E\tL2-RmV\tL2-RCC-V\tL2-RCC-E\t" +
        "L3-G-V\tL3-G-E");
      for (int j = 0; j < nvsToRemoveFromLevelOne[i].length; ++j) {
        System.out.print(level0GraphSizes[i].second +"\t" + level0GraphSizes[i].first +"\t"); 
        System.out.print(nvsToRemove[i] + "\t");
        System.out.print(level0CCSizesAfterRemoval[i].second + "\t"
          + level0CCSizesAfterRemoval[i].first + "\t");
        System.out.print(level1GraphSizes[i].second +"\t" + level1GraphSizes[i].first +"\t"); 
        System.out.print(nvsToRemoveFromLevelOne[i][j] + "\t");
        System.out.print(level1CCSizes[i][j].second +"\t" + level1CCSizes[i][j].first +"\t");
        System.out.print(level2GraphSizes[i][j].second +"\t" + level2GraphSizes[i][j].first +"\t");
        // Level 2 removed vertices
        System.out.print(nvsToRemoveFromLevelOne[i][j]/2 + "\t");
        System.out.print(level2CCSizes[i][j].second +"\t" + level2CCSizes[i][j].first +"\t");
        System.out.print(level3GraphSizes[i][j].second +"\t" + level3GraphSizes[i][j].first +"\t");
//        System.out.print("RemovedV: " + nvsToRemoveFromLevelOne[i][j] + "\t" +
//          level1CCSizes[i][j].second + "\t" + level1CCSizes[i][j].first);
//        System.out.println("\tLevel2-VE:\t" + level2GraphSizes[i][j].second + "\t" +
//          level2GraphSizes[i][j].first);
        System.out.println();
      }
      System.out.println();
    } 
    System.out.println("END OF DUMPING RESULTS...");
  }
}
