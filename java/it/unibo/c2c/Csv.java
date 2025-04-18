package it.unibo.c2c;

import com.google.common.base.Splitter;
import it.unimi.dsi.fastutil.doubles.DoubleList;

import java.io.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.stream.Collectors;

import static it.unibo.c2c.DoubleLists.doubleListOf;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.stream;

/**
 * Read a resource file as a CSV into a {@link List<DoubleList>} Data is stored as a list of
 * columns with a list of string headers.
 *
 * <p>NOTE: This class does not handle quoted strings and always assumes the separator is a comma.
 */
public record Csv(List<String> headers, List<DoubleList> values) {

    private static final DecimalFormatSymbols DECIMAL_FORMAT_SYMBOLS = new DecimalFormatSymbols();
    private static final DecimalFormat DECIMAL_FORMAT;

    static {
        DECIMAL_FORMAT_SYMBOLS.setDecimalSeparator('.');
        DECIMAL_FORMAT = new DecimalFormat("#.###############", DECIMAL_FORMAT_SYMBOLS);
    }

    public Csv(List<String> headers, List<DoubleList> values) {
        this.headers = headers.stream().map(String::trim).collect(Collectors.toCollection(ArrayList::new));
        this.values = new ArrayList<>(values);
        if (headers.isEmpty()) {
            throw new IllegalArgumentException("headers cannot be empty");
        }
        if (values.size() != headers().size()) {
            throw new IllegalArgumentException("The number of headers must match the number of values");
        }
    }

    public static Csv empty(String... headers) {
        return empty(Arrays.asList(headers));
    }

    public static Csv empty(List<String> headers) {
        var values = headers.stream().map(it -> doubleListOf()).toList();
        return new Csv(List.copyOf(headers), values);
    }

    /**
     * A transposed csv. Each "column" is actually a row, with the column header as the first item of
     * the row.
     */
    public static Csv horizontal(InputStream stream) {
        try (var reader = new BufferedReader(new InputStreamReader(stream, UTF_8))) {
            List<String> headers = new ArrayList<>();
            List<DoubleList> values = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                headers.add(parts[0]);
                double[] doubles = stream(parts).skip(1).mapToDouble(Double::parseDouble).toArray();
                values.add(doubleListOf(doubles));
            }
            return new Csv(headers, values);
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    /**
     * Regular column-oriented file with a header line on top.
     */
    public static Csv vertical(InputStream stream) {
        try (var reader = new BufferedReader(new InputStreamReader(stream, UTF_8))) {
            String line = reader.readLine().trim();
            while (line.startsWith("#")) {
                line = reader.readLine().trim();
            }
            List<String> headers = Arrays.asList(line.split(","));
            List<DoubleList> values = new ArrayList<>();
            for (int i = 0; i < headers.size(); i++) {
                values.add(doubleListOf());
            }
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("#")) {
                    continue;
                }
                List<String> parts = Splitter.on(',').splitToList(line);
                for (int i = 0; i < parts.size(); i++) {
                    try {
                        values.get(i).add(Double.parseDouble(parts.get(i)));
                    } catch (RuntimeException e) {
                        throw new RuntimeException("Error parsing line %d (%s), column %d (%s)".formatted(lineNumber, line, i, parts.get(i)), e);
                    }
                }
                lineNumber++;
            }
            return new Csv(headers, values);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public int getColumnsCount() {
        return headers.size();
    }

    public int getRowsCount() {
        return values.getFirst().size();
    }

    public DoubleList getHeadersAsDoubles() {
        return doubleListOf(
                headers.stream()
                        .mapToDouble(it -> {
                            try {
                                return Double.parseDouble(it);
                            } catch (NumberFormatException e) {
                                return Double.NaN;
                            }
                        }).filter(it -> !Double.isNaN(it))
        );
    }

    /**
     * Get a column by name.
     */
    public DoubleList getColumn(String name) {
        if (!headers.contains(name)) {
            throw new IllegalArgumentException("Column %s not found in headers %s".formatted(name, headers));
        }
        return values.get(headers.indexOf(name));
    }

    /**
     * Get one row of the CSV as a DoubleList, skipping the first `skip` elements.
     */
    public DoubleList getRow(int row, int skip) {
        DoubleList result = doubleListOf();
        for (int col = skip; col < values.size(); col++) {
            result.add(values.get(col).getDouble(row));
        }
        return result;
    }

    /**
     * Get one row of the CSV as a DoubleList, skipping the first element
     */
    public DoubleList getRow(int row) {
        return getRow(row, 0);
    }

    /**
     * Split the Csv based on the value of the column named 'name'.
     * Preserves the partial order of the rows.
     */
    public Map<Double, Csv> groupByColumn(String id) {
        DoubleList groups = doubleListOf(getColumn(id).doubleStream().distinct().sorted());
        var result = new LinkedHashMap<Double, Csv>();
        for (int i = 0; i < groups.size(); i++) {
            result.put(groups.getDouble(i), empty(headers));
        }
        for (int i = 0; i < getRowsCount(); i++) {
            DoubleList row = doubleListOf(getRow(i));
            double group = row.getDouble(headers.indexOf(id));
            result.get(group).addRow(row);
        }
        return result;
    }

    public void addRow(DoubleList row) {
        if (row.size() != getColumnsCount()) {
            throw new IllegalArgumentException("Row size (%s) does not match number of columns (%s)".formatted(row.size(), getColumnsCount()));
        }
        for (int i = 0; i < row.size(); i++) {
            values.get(i).add(row.getDouble(i));
        }
    }

    public void addRows(Csv other) {
        for (int i = 0; i < other.getRowsCount(); i++) {
            addRow(other.getRow(i));
        }
    }

    /**
     * Extract a subset of rows as if it were another Csv
     */
    public Csv subset(int start, int end) {
        List<DoubleList> copies = new ArrayList<>();
        int len = end - start + 1;
        for (DoubleList d : values) {
            double[] copy = new double[len];
            d.getElements(start, copy, 0, len);
            copies.add(doubleListOf(copy));
        }
        return new Csv(headers, copies);
    }

    public void writeTo(Writer writer) throws IOException {
        BufferedWriter w = new BufferedWriter(writer);
        w.write(String.join(", ", headers));
        w.newLine();
        w.flush();
        for (int i = 0; i < getRowsCount(); i++) {
            var row = getRow(i);
            w.write(row.doubleStream().mapToObj(DECIMAL_FORMAT::format).collect(Collectors.joining(",")));
            w.newLine();
            w.flush();
        }
    }

    public String toCsvString() {
        try (StringWriter sw = new StringWriter()) {
            writeTo(sw);
            return sw.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void print() {
        OutputStreamWriter writer = new OutputStreamWriter(System.out, UTF_8);
        try {
            writeTo(writer);
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
