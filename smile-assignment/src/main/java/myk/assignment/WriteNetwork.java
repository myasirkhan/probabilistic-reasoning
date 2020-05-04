package myk.assignment;

import smile.*;

/**
 * Writes a sample network
 */
public class WriteNetwork {

    public static void run(String outputFile) {
        System.out.println("Starting...");
        Network net = new Network();

        int e = createCptNode(net,
                "Economy", "State of the economy",
                new String[]{"Up", "Flat", "Down"},
                160, 40);

        int s = createCptNode(net,
                "Success", "Success of the venture",
                new String[]{"Success", "Failure"},
                60, 40);

        int f = createCptNode(net,
                "Forecast", "Expert forecast",
                new String[]{"Good", "Moderate", "Poor"},
                110, 140);

        net.addArc(e, s);
        net.addArc(s, f);


        // we can also use node identifiers when creating arcs

        net.addArc("Economy", "Forecast");


        double[] economyDef = {
                0.2, // P(Economy=U)
                0.7, // P(Economy=F)
                0.1  // P(Economy=D)

        };

        net.setNodeDefinition(e, economyDef);


        double[] successDef = new double[]{
                0.3, // P(Success=S|Economy=U)
                0.7, // P(Success=F|Economy=U)
                0.2, // P(Success=S|Economy=F)
                0.8, // P(Success=F|Economy=F)
                0.1, // P(Success=S|Economy=D)
                0.9  // P(Success=F|Economy=D)
        };
        net.setNodeDefinition(s, successDef);

        double[] forecastDef = new double[]{
                0.70, // P(Forecast=G|Success=S,Economy=U)
                0.29, // P(Forecast=M|Success=S,Economy=U)
                0.01, // P(Forecast=P|Success=S,Economy=U)

                0.65, // P(Forecast=G|Success=S,Economy=F)
                0.30, // P(Forecast=M|Success=S,Economy=F)
                0.05, // P(Forecast=P|Success=S,Economy=F)

                0.60, // P(Forecast=G|Success=S,Economy=D)
                0.30, // P(Forecast=M|Success=S,Economy=D)
                0.10, // P(Forecast=P|Success=S,Economy=D)

                0.15, // P(Forecast=G|Success=F,Economy=U)
                0.30, // P(Forecast=M|Success=F,Economy=U)
                0.55, // P(Forecast=P|Success=F,Economy=U)

                0.10, // P(Forecast=G|Success=F,Economy=F)
                0.30, // P(Forecast=M|Success=F,Economy=F)
                0.60, // P(Forecast=P|Success=F,Economy=F)

                0.05, // P(Forecast=G|Success=F,Economy=D)
                0.25, // P(Forecast=G|Success=F,Economy=D)
                0.70  // P(Forecast=G|Success=F,Economy=D)

        };

        net.setNodeDefinition(f, forecastDef);
        net.writeFile(outputFile);
        System.out.printf("Write network complete: Network written to %s\n", outputFile);

    }


    private static int createCptNode(
            Network net, String id, String name,
            String[] outcomes, int xPos, int yPos) {
        int handle = net.addNode(Network.NodeType.CPT, id);

        net.setNodeName(handle, name);
        net.setNodePosition(handle, xPos, yPos, 85, 55);

        int initialOutcomeCount = net.getOutcomeCount(handle);
        for (int i = 0; i < initialOutcomeCount; i++) {
            net.setOutcomeId(handle, i, outcomes[i]);
        }

        for (int i = initialOutcomeCount; i < outcomes.length; i++) {
            net.addOutcome(handle, outcomes[i]);
        }
        return handle;

    }

}