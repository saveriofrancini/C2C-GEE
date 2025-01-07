package it.unibo.c2c;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.stream;

import com.google.common.base.Splitter;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Read a resource file as a CSV into a {@link List<DoubleArrayList>} Data is stored as a list of
 * columns with a list of string headers.
 *
 * <p>NOTE: This class does not handle quoted strings and always assumes the separator is a comma.
 */
class Csv {

  List<String> headers;
  List<DoubleArrayList> values;

  private Csv(List<String> headers, List<DoubleArrayList> values) {
    this.headers = headers;
    this.values = values;
  }

  /**
   * A transposed csv. Each "column" is actually a row, with the column header as the first item of
   * the row.
   */
  public static Csv horizontal(InputStream stream) {
    BufferedReader reader = new BufferedReader(new InputStreamReader(stream, UTF_8));
    try {
      List<String> headers = new ArrayList<>();
      List<DoubleArrayList> values = new ArrayList<>();
      String line;
      while ((line = reader.readLine()) != null) {
        String[] parts = line.split(",");
        headers.add(parts[0]);
        double[] doubles = stream(parts).skip(1).mapToDouble(Double::parseDouble).toArray();
        values.add(DoubleArrayList.wrap(doubles));
      }
      return new Csv(headers, values);
    } catch (IOException e) {
      throw new IllegalStateException(e.getMessage());
    }
  }

  /** Regular column-oriented file with a header line on top. */
  public static Csv vertical(InputStream stream) {
    BufferedReader reader = new BufferedReader(new InputStreamReader(stream, UTF_8));
    try {
      List<String> headers = Arrays.asList(reader.readLine().split(","));
      List<DoubleArrayList> values = new ArrayList<>();
      for (int i = 0; i < headers.size(); i++) {
        values.add(new DoubleArrayList());
      }
      String line;
      while ((line = reader.readLine()) != null) {
        List<String> parts = Splitter.on(',').splitToList(line);
        for (int i = 0; i < parts.size(); i++) {
          values.get(i).add(Double.parseDouble(parts.get(i)));
        }
      }
      return new Csv(headers, values);
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  /** Get a column by name. */
  public DoubleArrayList getColumn(String name) {
    return values.get(headers.indexOf(name));
  }

  /** Get one row of the CSV as a DoubleArrayList, skipping the first `skip` elements. */
  public DoubleArrayList getRow(int row, int skip) {
    DoubleArrayList result = new DoubleArrayList();
    for (int col = skip; col < values.size(); col++) {
      result.add(values.get(col).getDouble(row));
    }
    return result;
  }

  /**
   * Split the Csv based on the value of the 'id' column. Assumes the rows are sorted and grouped
   * together.
   */
  public List<Csv> groupByColumn(String id) {
    DoubleArrayList groupColumn = getColumn(id);
    List<Csv> result = new ArrayList<>();
    int startRow = 0;
    double lastGroup = groupColumn.getDouble(0);
    for (int i = 0; i < groupColumn.size(); i++) {
      if (groupColumn.getDouble(i) != lastGroup) {
        result.add(subset(startRow, i - 1));
        startRow = i;
      }
      lastGroup = groupColumn.getDouble(i);
    }
    result.add(subset(startRow, groupColumn.size() - 1));
    return result;
  }

  /** Extract a subset of rows as if it were another Csv */
  Csv subset(int start, int end) {
    List<DoubleArrayList> copies = new ArrayList<>();
    int len = end - start + 1;
    for (DoubleArrayList d : values) {
      double[] copy = new double[len];
      d.getElements(start, copy, 0, len);
      copies.add(DoubleArrayList.wrap(copy));
    }
    return new Csv(headers, copies);
  }
}
