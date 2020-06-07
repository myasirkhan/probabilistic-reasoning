package myk.assignment;

import com.opencsv.CSVReader;
import smile.License;
import smile.Network;
import smile.SMILEException;

import java.io.FileReader;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class Assignment4 {
    private static final Map<Integer, String> outcomes = getOutComesMap();

    private static Map<Integer, String> getOutComesMap() {
        Map<Integer, String> map = new HashMap<>();
        map.put(0, "Zero");
        map.put(1, "One");
        map.put(2, "Two");
        map.put(3, "Three");
        map.put(4, "Four");
        map.put(5, "Five");
        map.put(6, "Six");
        map.put(7, "Seven");
        map.put(8, "Eight");
        map.put(9, "Nine");
        map.put(10, "Ten");
        map.put(11, "Eleven");
        map.put(12, "Twelve");
        return map;
    }

    private static final double LOG2 = 1 / Math.log(2);
    private static final double U = 4;

    private static void combinationsHelper(List<int[]> combinations, int[] data, int start, int end, int index) {
        if (index == data.length) {
            int[] combination = data.clone();
            combinations.add(combination);
        } else if (start <= end) {
            data[index] = start;
            combinationsHelper(combinations, data, start + 1, end, index + 1);
            combinationsHelper(combinations, data, start + 1, end, index);
        }
    }

    public static List<int[]> generateCombinations(int n, int r) {
        List<int[]> combinations = new ArrayList<>();
        combinationsHelper(combinations, new int[r], 0, n - 1, 0);
        return combinations;
    }


    /***
     * Computes MI between variables t and a. Assumes that a.length == t.length.
     * @param a candidate variable a
     * @param avals number of values a can take (max(a) == avals)
     * @param t target variable
     * @param tvals number of values a can take (max(t) == tvals)
     * @return MI
     */
    static double computeMI(Integer[] a, int avals, Integer[] t, int tvals) {
        double numinst = a.length;
        double oneovernuminst = 1 / numinst;
        double sum = 0;

        // longs are required here because of big multiples in calculation
        long[][] crosscounts = new long[avals][tvals];
        long[] tcounts = new long[tvals];
        long[] acounts = new long[avals];
        // Compute counts for the two variables
        for (int i = 0; i < a.length; i++) {
            int av = a[i];
            int tv = t[i];
            acounts[av]++;
            tcounts[tv]++;
            crosscounts[av][tv]++;
        }

        for (int tv = 0; tv < tvals; tv++) {
            for (int av = 0; av < avals; av++) {
                if (crosscounts[av][tv] != 0) {
                    // Main fraction: (n|x,y|)/(|x||y|)
                    double sumtmp = (numinst * crosscounts[av][tv]) / (acounts[av] * tcounts[tv]);
                    // Log bit (|x,y|/n) and update product
                    sum += oneovernuminst * crosscounts[av][tv] * getLN(sumtmp);
                }
            }

        }

        return sum;
    }

    // function to sort hashmap by values
    public static HashMap<String, Double> sortByValue(HashMap<String, Double> hm) {
        // Create a list from elements of HashMap
        List<Map.Entry<String, Double>> list =
                new LinkedList<>(hm.entrySet());

        // Sort the list
        list.sort(Map.Entry.comparingByValue());

        // put data from sorted list to hashmap
        HashMap<String, Double> temp = new LinkedHashMap<>();
        for (Map.Entry<String, Double> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }

    static Network constructDAG(HashMap<String, Double> miScores, Map<String, List<Integer>> colWiseData) {
        // Using Chow and Liu Algorithm, construct a dag
        HashMap<String, Double> sortedMap = sortByValue(miScores);
        Network net = new Network();
        AtomicInteger count = new AtomicInteger(0);
        miScores.keySet().stream().map(s -> s.split(",")).flatMap(Stream::of).distinct().forEach(node -> {
            System.out.println("Node Name: " + node);
            WriteNetwork.createCptNode(net,
                    node, node,
                    getOutcomes(colWiseData.get(node)),
                    160 * count.incrementAndGet(), 40 * count.incrementAndGet());
        });
        sortedMap.forEach((key, value) -> {
            String[] nodes = key.split(",");
            if (areNodesConnected(net, nodes[0], nodes[1])) {
                return;
            }
            System.out.println("Connecting " + nodes[0] + " and " + nodes[1]);
            net.addArc(nodes[0], nodes[1]);
        });
        return net;
    }

    private static boolean areNodesConnected(Network net, String node1, String node2) {
        return isNodeParent(net, node1, node2) || isNodeParent(net, node2, node1);
    }


    private static boolean isNodeParent(Network net, String node1, String node2) {
        Set<String> nodesSet = new HashSet<>();
        Set<String> parentsNode1 = getConnectedNodes(net, node1, nodesSet);
        // Set<String> parentsNode2 = getConnectedNodes(net, node2, nodesSet);
        return parentsNode1.contains(node2);
    }

    private static Set<String> getConnectedNodes(Network net, String node1, Set<String> nodesSet) {
        String[] parents = net.getParentIds(node1);
        String[] children = net.getChildIds(node1);
        int originalNodeSize = nodesSet.size();
        nodesSet.addAll(List.of(parents));
        nodesSet.addAll(List.of(children));
        if (parents.length == 0 || nodesSet.size() == originalNodeSize) {
            return nodesSet;
        }
        for (String parent : parents) {
            nodesSet.addAll(getConnectedNodes(net, parent, nodesSet));
        }
        for (String child : children) {
            nodesSet.addAll(getConnectedNodes(net, child, nodesSet));
        }
        return nodesSet;
    }

    private static String[] getOutcomes(List<Integer> nodeData) {
        return nodeData.stream().distinct().map(outcomes::get).toArray(String[]::new);
    }


    public static void main(String[] args) {
        License license = new smile.License(
                "",
                new byte[]{}
        );
        try {

            // Create an object of filereader
            // class with CSV file as a parameter.
            FileReader filereader = new FileReader("/Users/yasir/Desktop/test1.csv");

            // create csvReader object passing
            // file reader as a parameter
            CSVReader csvReader = new CSVReader(filereader);
            List<String[]> rowWiseData = csvReader.readAll();
            // transform dataframe
            Map<String, List<Integer>> colWiseData = getColWiseData(rowWiseData);
            // get the column names
            String[] colNamesArray = colWiseData.keySet().toArray(String[]::new);
            // get the mutual information map per column combination and score in a map
            HashMap<String, Double> miScores = getMutualInfoScoreMap(colWiseData, colNamesArray);
            Network net = constructDAG(miScores, colWiseData);
            // get the BIC score for the DAG
            double score = calculateBICForDAG(net, colWiseData, colNamesArray);
            updateNetworkUsingK2Approach(net, colWiseData, colNamesArray);
            double scoreNew = calculateBICForDAG(net, colWiseData, colNamesArray);
            for (int h = net.getFirstNode(); h >= 0; h = net.getNextNode(h)) {
                ReadAndUpdateNetwork.printNodeInfo(net, h); // prints all the info
            }
            net.writeFile("file.xdsl");
            System.out.println("Old Score: " + score + " Score New: " + scoreNew);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void updateNetworkUsingK2Approach(Network net, Map<String, List<Integer>> colWiseData, String[] colNamesArray) {
        int[] allNodes = net.getAllNodes();
        for (int h = net.getFirstNode(); h >= 0; h = net.getNextNode(h)) {
            double score = calculateBICForDAG(net, colWiseData, colNamesArray);
            double scoreNew = 0;
            int k;
            for (k = net.getNextNode(h); k >= 0; k = net.getNextNode(k)) {
                try {
                    net.addArc(h, k);
                    scoreNew = calculateBICForDAG(net, colWiseData, colNamesArray);
                    if (scoreNew > score) {
                        break;
                    }
                    net.deleteArc(h, k);
                } catch (Exception e) {
                    // pass
                }
                try {
                    net.addArc(k, h);
                    scoreNew = calculateBICForDAG(net, colWiseData, colNamesArray);
                    if (scoreNew > score) {
                        break;
                    }
                    net.deleteArc(k, h);
                } catch (Exception e) {
                    // pass
                }
                try {
                    net.deleteArc(k, h);
                    scoreNew = calculateBICForDAG(net, colWiseData, colNamesArray);
                    if (scoreNew > score) {
                        break;
                    }
                    net.addArc(k, h);
                } catch (Exception e) {
                    // pass
                }
                try {
                    net.deleteArc(h, k);
                    scoreNew = calculateBICForDAG(net, colWiseData, colNamesArray);
                    if (scoreNew > score) {
                        break;
                    }
                    net.addArc(h, k);
                } catch (Exception e) {
                    // pass
                }
            }

            boolean findMore = true;
            while (findMore && net.getParents(h).length < U && k >= 0) {
                if (scoreNew > score) {
                    score = scoreNew;
                    // net.addArc(h, k);
                } else {
                    findMore = false;
                }
            }
        }
    }

    private static double calculateBICForDAG(Network net, Map<String, List<Integer>> colWiseData, String[] colNamesArray) {
        List<int[]> combinations = generateCombinations(colNamesArray.length, 2);
        double score = 0;
        double dataLen = 0;
        double d = 0;
        // sum up bic score for all combinations of columns
        for (int[] comb : combinations) {
            try {
                Integer[] col1 = colWiseData.get(colNamesArray[comb[0]]).toArray(Integer[]::new);
                Integer[] col2 = colWiseData.get(colNamesArray[comb[1]]).toArray(Integer[]::new);
                dataLen = col1.length;
                boolean nodesConnected = areNodesConnected(net, colNamesArray[comb[0]], colNamesArray[comb[1]]);
                if (nodesConnected) {
                    d += 2;
                } else {
                    d += 1;
                }
                score += applyBICCriteria(col1, col2, nodesConnected);
            } catch (Exception e) {
                // pass
                e.printStackTrace();
            }
        }

        return getLN(score) - ((d / 2) * getLN(dataLen));
    }

    private static double getLN(double score) {
        return score > 0 ? Math.log(score) : 0;
    }

    private static double applyBICCriteria(Integer[] col1, Integer[] col2, boolean areNodesConnected) {
        // get maximum combinations
        int order1 = getMax(col1);
        int order2 = getMax(col2);
        double bicScore = 1;

        double fGivenJ = 0;
        double jcount = 0;
        for (int f = 0; f < order1; f++) {
            for (int j = 0; j < order2; j++) {
                fGivenJ = 0;
                jcount = 0;
                for (int v = 0; v < col1.length; v++) {
                    if (areNodesConnected) {
                        // if nodes are connected then use F | J
                        if (col1[v] == f && col2[v] == j) {
                            fGivenJ++;
                        }
                    } else {
                        // if nodes are connected then use F, but using the same variable "fGivenJ"
                        // for compactness of algo
                        if (col1[v] == f) {
                            fGivenJ++;
                        }
                    }
                    if (col2[v] == j) {
                        jcount++;
                    }
                }
            }
            // calculate bic score by multiplying all marginal / conditional probabilities..
            if (jcount > 0 && fGivenJ > 0) {
                bicScore *= (fGivenJ / jcount) * (jcount / col1.length);
            }
        }

        return bicScore;
    }

    private static Map<String, List<Integer>> getColWiseData(List<String[]> rowWiseData) {
        Map<Integer, String> colIndexMap = new HashMap<>();
        Map<String, List<Integer>> colWiseData = new HashMap<>();
        AtomicBoolean firstRow = new AtomicBoolean(true);

        rowWiseData.forEach(row -> {
            int countIndex = 0;
            for (String elem : row) {
                if (firstRow.get()) {
                    elem = elem.replace("\uFEFF", "");
                    colIndexMap.put(countIndex++, elem);
                    colWiseData.put(elem, new ArrayList<>());
                } else {
                    String colName = colIndexMap.get(countIndex++);
                    colWiseData.get(colName).add(Integer.parseInt(elem));
                }
            }
            firstRow.set(false);
        });
        return colWiseData;
    }

    private static HashMap<String, Double> getMutualInfoScoreMap(Map<String, List<Integer>> colWiseData, String[] colNamesArray) {
        // all nCr, with r 2 for the column count
        List<int[]> combinations = generateCombinations(colNamesArray.length, 2);
        HashMap<String, Double> miScores = new HashMap<>();
        // for each combination, calulate the MI score and store in a map
        combinations.forEach(comb -> {
            Integer[] col1 = colWiseData.get(colNamesArray[comb[0]]).toArray(Integer[]::new);
            Integer[] col2 = colWiseData.get(colNamesArray[comb[1]]).toArray(Integer[]::new);
            double miScore = computeMI(col1, getMax(col1), col2, getMax(col2));
            miScores.put(colNamesArray[comb[0]] + "," + colNamesArray[comb[1]], miScore);
        });
        return miScores;
    }

    private static int getMax(Integer[] col1) {
        return Arrays.stream(col1).max(Comparator.naturalOrder()).orElse(0) + 1;
    }
}
