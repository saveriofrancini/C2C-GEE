package it.unibo.c2c;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

public class CommandLineMain {

    private static C2cSolver.Args parseArgs(Map<String, String> args) {
        var result = new C2cSolver.Args();
        for (var field : result.getClass().getFields()) {
            if (args.containsKey(field.getName())) {
                Class<?> type = field.getType();
                Object value = args.get(field.getName());
                try {
                    if (type == int.class) {
                        value = Integer.parseInt((String) value);
                    } else if (type == boolean.class) {
                        value = Boolean.parseBoolean((String) value);
                    } else if (type == double.class) {
                        value = Double.parseDouble((String) value);
                    } else if (type == String.class) {
                        // No conversion needed
                    } else {
                        throw new IllegalArgumentException("Unsupported type: " + type);
                    }
                    field.set(result, value);
                } catch (Exception e) {
                    throw new RuntimeException(
                        "Error while parsing value `%s` as %s for property %s".formatted(
                            value, 
                            field.getType().getSimpleName(),
                            field.getName()
                        ), 
                        e
                    );
                }
            }
        }
        return result;
    }

    private static Map<String, String> readArgs(String[] args) {
        var result = new HashMap<String, String>();
        result.put("inputFile", null);
        for (String arg : args) {
            arg = arg.trim();
            if (arg.startsWith("--")) {
                arg = arg.substring(2);
                if (arg.contains("=")) {
                    var parts = arg.split("=");
                    result.put(parts[0], parts[1]);
                } else {
                    result.put(arg, "true");
                }
            } else {
                result.put("inputFile", arg);
            }
        }
        return result;
    }

    public static void main(String[] args) throws FileNotFoundException {
        var read = readArgs(args);
        C2cSolver.Args parsed = parseArgs(read);
        if (!read.containsKey("inputFile")) {
            throw new FileNotFoundException("Missing required input file");
        }
        var inputFile = new File(read.get("inputFile"));
        Csv input = Csv.vertical(new FileInputStream(inputFile));
        C2cSolver solver = new C2cSolver(parsed);
        System.out.printf("# Running on file %s with args: %s%n", inputFile.getPath(), parsed);
        Csv result = solver.c2cBottomUp(input);
        result.print();
    }
}
