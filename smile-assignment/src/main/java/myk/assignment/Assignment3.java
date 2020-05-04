package myk.assignment;

import smile.*;

import java.io.File;

public class Assignment3 {
    public static void main(String[] args) {
        License license = new smile.License(
                "", new byte[]{}
        );

        String outputFile = null;
        if (args.length == 1) {
            outputFile = args[0];
        } else {
            System.out.println("No input file provided, Exiting");
            System.exit(0);
        }
        if (outputFile == null || !new File(outputFile).exists()) {
            if (outputFile == null) {
                outputFile = "sample_file.xdsl"; // create sample file
            }
            System.out.printf("output file: %s does not exists, creating new one", outputFile);
            WriteNetwork.run(outputFile);
        }

        ReadAndUpdateNetwork.run(outputFile);
    }

}
