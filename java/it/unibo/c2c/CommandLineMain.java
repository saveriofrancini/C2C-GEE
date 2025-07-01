package it.unibo.c2c;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CommandLineMain {

  private static final DecimalFormatSymbols DECIMAL_FORMAT_SYMBOLS = new DecimalFormatSymbols();
  private static final DecimalFormat DECIMAL_FORMAT;

  static {
    DECIMAL_FORMAT_SYMBOLS.setDecimalSeparator('.');
    DECIMAL_FORMAT = new DecimalFormat("#.###############", DECIMAL_FORMAT_SYMBOLS);
  }

  private static C2cSolver.Args parseArgs(Map<String, String> args) {
    var result = new C2cSolver.Args();
    for (var field : result.getClass().getFields()) {
      if (args.containsKey(field.getName())) {
        var type = field.getType();
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
              "Error while parsing value `%s` as %s for property %s"
                  .formatted(value, field.getType().getSimpleName(), field.getName()),
              e);
        }
      }
    }
    return result;
  }

  private static Map<String, String> readArgs(String[] args) {
    var result = new HashMap<String, String>();
    result.put("inputFile", null);
    for (var arg : args) {
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

  private static void printChangeCsv(List<List<Changes>> allChanges, C2cSolver.Args args) {
    ArrayList<String> headers = new ArrayList<>();
    headers.add("id");
    headers.add("year");
    headers.add("index");
    headers.add("magnitude");
    headers.add("duration");
    headers.add("rate");
    if (args.postMetrics) {
      headers.add("postMagnitude");
      headers.add("postDuration");
      headers.add("postRate");
    }
    if (args.includeRegrowth) {
      headers.add("indexRegrowth");
      headers.add("recoveryIndicator");
      headers.add("regrowth60");
      headers.add("regrowth80");
      headers.add("regrowth100");
    }
    System.out.println(String.join(",", headers));
    int zeroPadSize = (int) Math.ceil(Math.log10(allChanges.size()));
    for (int i = 0; i < allChanges.size(); ++i) {
      List<Changes> rowChanges = allChanges.get(i);
      if (rowChanges == null) {
        System.out.println(formatRow(i, Changes.EMPTY, args));
        continue;
      }
      if (rowChanges.isEmpty()) {
        System.out.println(formatRow(i, Changes.EMPTY, args));
        continue;
      }
      for (Changes c : rowChanges) {
        System.out.println(formatRow(i, c, args));
      }
    }
  }

  private static String formatRow(int id, Changes c, C2cSolver.Args args) {
    ArrayList<String> outputStringElements = new ArrayList<>();
    DoubleArrayList outputRow = new DoubleArrayList();
    outputRow.add((double) id);
    outputRow.add(c.date());
    outputRow.add(c.value());
    outputRow.add(c.magnitude());
    outputRow.add(c.duration());
    outputRow.add(c.rate());
    if (args.postMetrics) {
      outputRow.add(c.postMagnitude());
      outputRow.add(c.postDuration());
      outputRow.add(c.postRate());
    }
    if (args.includeRegrowth) {
      outputRow.add(c.indexRegrowth());
      outputRow.add(c.recoveryIndicator());
      outputRow.add(c.regrowth60());
      outputRow.add(c.regrowth80());
      outputRow.add(c.regrowth100());
    }
    return String.join(
        ",",
        outputRow
            .doubleStream()
            .mapToObj(d -> DECIMAL_FORMAT.format(d))
            .collect(Collectors.toList()));
  }

  public static void main(String[] args) throws FileNotFoundException {
    Map<String, String> read = readArgs(args);
    C2cSolver.Args c2cArgs = parseArgs(read);
    if (!read.containsKey("inputFile")) {
      throw new FileNotFoundException("Missing required input file");
    }
    var inputFile = new File(read.get("inputFile"));
    Csv inputCsv = Csv.vertical(new FileInputStream(inputFile));
    DoubleArrayList dates = inputCsv.getDates();
    var solver = new C2cSolver();
    // System.out.println("Running on file %s with args: %s%n", inputFile.getPath(), c2cArgs);
    ArrayList<List<Changes>> allChanges = new ArrayList<>();
    for (int i = 0; i < inputCsv.values.get(0).size(); i++) {
      DoubleArrayList timeline = inputCsv.getRow(i, /* skip= */ 1);
      allChanges.add(solver.c2cBottomUp(dates, timeline, c2cArgs));
    }
    printChangeCsv(allChanges, c2cArgs);
  }
}
