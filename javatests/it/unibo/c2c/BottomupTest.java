package it.unibo.c2c;

import static org.junit.Assert.assertEquals;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class BottomupTest {

  private static final String SAMPLES_FILE = "/it/unibo/c2c/testdata/input.csv";
  private static final String EXPECTED_FILE = "/it/unibo/c2c/testdata/output.csv";

  @Test
  public void testGoldens() throws Exception {
    // Read input file.  It has dates as column headers and each row is a full timeline.
    Csv inputs = Csv.vertical(getClass().getResourceAsStream(SAMPLES_FILE));
    DoubleArrayList dates =
        DoubleArrayList.wrap(
            inputs.headers.stream().skip(1).mapToDouble(Double::parseDouble).toArray());
    int numberOfInputs = inputs.values.get(0).size();
    // Read expected results file and split by plot ID.
    List<Csv> expected =
        Csv.vertical(getClass().getResourceAsStream(EXPECTED_FILE)).groupByColumn("id");
    assertEquals(numberOfInputs, expected.size());
    // Apply the Main function on each timeLine.
    int nullCount = 0;
    C2cSolver.Args arguments = new C2cSolver.Args();
    C2cSolver solver = new C2cSolver();
    for (int i = 0; i < numberOfInputs; i++) {
      // The inputs have a plot ID in the first column that isn't used in the timeline.  Skip it.
      DoubleArrayList timeline = inputs.getRow(i, /* skip= */ 1);
      List<Changes> result = solver.c2cBottomUp(dates, timeline, arguments);
      if (result != null) {
        verify(result, expected.get(i), /* includeRegrowth= */ false);
      } else {
        nullCount++;
      }
    }
    // There are 3 inputs that don't have enough points.
    assertEquals(3, nullCount);
  }

  /** Verify that the changes match the expected values. */
  private void verify(List<Changes> actual, Csv expected, boolean includeRegrowth) {
    List<DoubleArrayList> values = expected.values;
    assertEquals(actual.size(), values.get(0).size());
    for (int j = 0; j < actual.size(); j++) {
      Changes c = actual.get(j);
      assertEquals(expected.getColumn("year").getDouble(j), c.date, 1e-9);
      assertEquals(expected.getColumn("index").getDouble(j), c.value, 1e-9);
      assertEquals(expected.getColumn("duration").getDouble(j), c.duration, 1e-9);
      assertEquals(expected.getColumn("magnitude").getDouble(j), c.magnitude, 1e-9);
      // assertEquals(c.postMagnitude, expected.getColumn("postMagnitude").getDouble(j), 1e-9);
      // assertEquals(c.postDuration, expected.getColumn("postDuration").getDouble(j), 1e-9);
      // assertEquals(c.postRate, expected.getColumn("postRate").getDouble(j), 1e-9);
      assertEquals(expected.getColumn("rate").getDouble(j), c.rate, 1e-9);
      if (includeRegrowth) {
        assertEquals(
            expected.getColumn("indexRegrowth").getDouble(j), c.indexRegrowth, 1e-9);
        assertEquals(
            expected.getColumn("recoveryIndicator").getDouble(j), c.recoveryIndicator, 1e-9);
        assertEquals(expected.getColumn("regrowth60").getDouble(j), c.regrowth60, 1e-9);
        assertEquals(expected.getColumn("regrowth80").getDouble(j), c.regrowth80, 1e-9);
        assertEquals(expected.getColumn("regrowth100").getDouble(j), c.regrowth100, 1e-9);
      }
    }
  }
}
