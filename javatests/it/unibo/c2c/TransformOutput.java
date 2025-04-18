package it.unibo.c2c;

import it.unimi.dsi.fastutil.doubles.DoubleList;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static it.unibo.c2c.DoubleLists.doubleListOf;

public class TransformOutput {
    private static final String DEFAULT_OUTPUT_FILE = "output-default.csv";
    private static final String TARGET_DIR = "src/test/resources/it/unibo/c2c";

    public static void main(String[] args) throws IOException {
        Csv input = Csv.vertical(TransformOutput.class.getResourceAsStream(DEFAULT_OUTPUT_FILE));
        Csv output = Csv.empty(input.headers());
        if (args.length == 0) {
            throw new IllegalArgumentException("Please provide a task name as a command-line argument. " +
                    "Available tasks: revert, filter");
        }
        var task = args[0].trim().toLowerCase();
        String newOutputFileName = "output-%sed.csv".formatted(task);
        switch (task) {
            case "revert" -> revertBand(input, output);
            case "filter" -> negativeMagnitudeOnly(input, output);
            case "default" -> doNothing(input, output);
            default -> throw new IllegalArgumentException("Unknown task: " + task);
        }
        var newOutputFile = new File(TARGET_DIR, newOutputFileName);
        try (var writer = new FileWriter(newOutputFile, /* append= */ false)) {
            output.writeTo(writer);
        } finally {
            System.out.println("Output written to " + newOutputFile.getPath());
        }
    }

    private static void doNothing(Csv input, Csv output) {
        // No transformation needed
        for (int i = 0; i < input.getRowsCount(); i++) {
            output.addRow(input.getRow(i));
        }
    }

    private static void revertBand(Csv input, Csv output) {
        // # +id, +year, -index, -magnitude, +duration, -rate, -postMagnitude, +postDuration, -postRate
        for (int i = 0; i < input.getRowsCount(); i++) {
            DoubleList row = doubleListOf(input.getRow(i));
            int[] fieldsToInvert = {
                    input.headers().indexOf("index"),
                    input.headers().indexOf("magnitude"),
                    input.headers().indexOf("rate"),
                    input.headers().indexOf("postMagnitude"),
                    input.headers().indexOf("postRate"),
            };
            for (int field : fieldsToInvert) {
                row.set(field, -row.getDouble(field));
            }
            output.addRow(row);
        }
    }

    private static void negativeMagnitudeOnly(Csv input, Csv output) {
        for (int i = 0; i < input.getRowsCount(); i++) {
            DoubleList row = doubleListOf(input.getRow(i));
            int magnitudeIndex = input.headers().indexOf("magnitude");
            double magnitudeValue = row.getDouble(magnitudeIndex);
            if (!Double.isNaN(magnitudeValue) && magnitudeValue < 0) {
                output.addRow(row);
            }
        }
    }
}
