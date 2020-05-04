package myk.assignment;

import smile.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;

/**
 * reads a network and update it based on evidence
 */
public class ReadAndUpdateNetwork {
    private static final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

    /**
     * reads and updates the netword
     *
     * @param inputFile input file
     */
    public static void run(String inputFile) {

        System.out.println("================================================================================");
        Network net = new Network();
        net.readFile(inputFile);
        for (int h = net.getFirstNode(); h >= 0; h = net.getNextNode(h)) {
            printNodeInfo(net, h); // prints all the info
        }
        net.updateBeliefs(); // update beliefs for the network
        System.out.println("================================================================================");
        System.out.println("---Prior Probability---");
        System.out.println("================================================================================");
        printAllPosteriors(net);
        for (int h = net.getFirstNode(); h >= 0; h = net.getNextNode(h)) {
            askForEvidence(net, h); // ask for evidence for every node of the network
        }
        net.updateBeliefs(); // update beliefs for the network

        System.out.println("================================================================================");
        System.out.println("---Posterior Probability---");
        System.out.println("================================================================================");
        printAllPosteriors(net);
        System.out.println("ReadAndUpdateNetwork complete.");

    }

    /**
     * exec callable in try/catch block
     *
     * @param callable callable
     */
    static void safeTry(Callable<Void> callable) {
        try {
            callable.call();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * safely gets the `callable`
     *
     * @param callable callable
     * @param <T> callable that returns type T
     * @return T type from the result of callable
     */
    static <T> T safeGet(Callable<T> callable) {
        try {
            return callable.call();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * asks for evidence for node of the network
     *
     * @param net Network
     * @param nodeHandle node id
     */
    private static void askForEvidence(Network net, int nodeHandle) {
        String evidence = safeGet(() -> net.getEvidenceId(nodeHandle));
        System.out.printf("Node name: %s, with current evidence on: %s\n", net.getNodeName(nodeHandle), evidence);
        System.out.printf("Add Evidence on %s: ", String.join(",", net.getOutcomeIds(nodeHandle)));
        String output = safeGet(br::readLine);
        if (output != null && output.trim().length() > 0) {
            safeTry(() -> {
                net.setEvidence(nodeHandle, output);
                return null;
            });
        }
    }

    /**
     * Print network's node info
     *
     * @param net network
     * @param nodeHandle node id
     */
    private static void printNodeInfo(Network net, int nodeHandle) {

        System.out.printf("Node id/name: %s/%s\n",
                net.getNodeId(nodeHandle),
                net.getNodeName(nodeHandle));

        System.out.print("  Outcomes:");
        for (String outcomeId : net.getOutcomeIds(nodeHandle)) {
            System.out.print(" " + outcomeId);
        }

        System.out.println();

        String[] parentIds = net.getParentIds(nodeHandle);
        if (parentIds.length > 0) {
            System.out.print("  Parents:");
            for (String parentId : parentIds) {
                System.out.print(" " + parentId);
            }
            System.out.println();
        }

        String[] childIds = net.getChildIds(nodeHandle);
        if (childIds.length > 0) {
            System.out.print("  Children:");
            for (String childId : childIds) {
                System.out.print(" " + childId);
            }
            System.out.println();
        }

        printCptMatrix(net, nodeHandle);
    }

    /**
     * prints the posteriors
     *
     * @param net network
     */
    private static void printAllPosteriors(Network net) {
        for (int h = net.getFirstNode(); h >= 0; h = net.getNextNode(h)) {
            printPosteriors(net, h);
        }
        System.out.println();
    }

    /**
     * prints the posteriors
     *
     * @param net Network
     * @param nodeHandle netword id
     */
    private static void printPosteriors(Network net, int nodeHandle) {
        String nodeId = net.getNodeId(nodeHandle);
        if (net.isEvidence(nodeHandle)) {
            System.out.printf("%s has evidence set (%s)\n",
                    nodeId,
                    net.getOutcomeId(nodeHandle, net.getEvidence(nodeHandle)));
        } else {
            double[] posteriors = net.getNodeValue(nodeHandle);
            for (int i = 0; i < posteriors.length; i++) {
                System.out.printf("P(%s=%s)=%f\n",
                        nodeId,
                        net.getOutcomeId(nodeHandle, i),
                        posteriors[i]);
            }
        }
    }

    /**
     * prints the confidence matrix
     *
     * @param net Network
     * @param nodeHandle network id
     */
    private static void printCptMatrix(Network net, int nodeHandle) {
        double[] cpt = net.getNodeDefinition(nodeHandle);
        int[] parents = net.getParents(nodeHandle);
        int dimCount = 1 + parents.length;

        int[] dimSizes = new int[dimCount];
        for (int i = 0; i < dimCount - 1; i++) {
            dimSizes[i] = net.getOutcomeCount(parents[i]);
        }
        dimSizes[dimSizes.length - 1] = net.getOutcomeCount(nodeHandle);

        int[] coords = new int[dimCount];
        for (int elemIdx = 0; elemIdx < cpt.length; elemIdx++) {
            indexToCoords(elemIdx, dimSizes, coords);
            String outcome = net.getOutcomeId(nodeHandle, coords[dimCount - 1]);
            System.out.printf("    P(%s", outcome);
            if (dimCount > 1) {
                System.out.print(" | ");
                for (int parentIdx = 0; parentIdx < parents.length; parentIdx++) {
                    if (parentIdx > 0) System.out.print(",");
                    int parentHandle = parents[parentIdx];
                    System.out.printf("%s=%s",
                            net.getNodeId(parentHandle),
                            net.getOutcomeId(parentHandle, coords[parentIdx]));
                }
            }

            double prob = cpt[elemIdx];
            System.out.printf(")=%f\n", prob);
        }
    }

    /**
     * converts index to coordinates
     *
     * @param index index
     * @param dimSizes size
     * @param coords coordinates
     */
    private static void indexToCoords(int index, int[] dimSizes, int[] coords) {
        int prod = 1;
        for (int i = dimSizes.length - 1; i >= 0; i--) {
            coords[i] = (index / prod) % dimSizes[i];
            prod *= dimSizes[i];
        }
    }
}
