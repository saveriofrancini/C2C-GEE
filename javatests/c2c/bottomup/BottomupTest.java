package c2c.bottomup;

import static org.junit.Assert.assertEquals;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import java.io.FileInputStream;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class BottomupTest {

  private static final String SAMPLES_FILE = "javatests/c2c/bottomup/testdata/input.csv";
  private static final String EXPECTED_FILE = "javatests/c2c/bottomup/testdata/output.csv";

  @Test
  public void testGoldens() throws Exception {
    FileInputStream inputStream = new FileInputStream(SAMPLES_FILE);
    FileInputStream outputStream = new FileInputStream(EXPECTED_FILE);
    // Read input file.  It has dates as column headers and each row is a full timeline.
    Csv inputs = Csv.vertical(inputStream);
    DoubleArrayList dates = DoubleArrayList.wrap(
        inputs.headers.stream().skip(1).mapToDouble(Double::parseDouble).toArray());
    int numberOfInputs = inputs.values.get(0).size();
    // Read expected results file and split by plot ID.
    List<Csv> expected = Csv.vertical(outputStream)
        .groupByColumn("id");
    assertEquals(numberOfInputs, expected.size());
    // Apply the Main function on each timeLine.
    int nullCount = 0;
    C2cSolver.Args arguments = new C2cSolver.Args();
    C2cSolver solver = new C2cSolver();
    for (int i = 0; i < numberOfInputs; i++) {
      // The inputs have a plot ID in the first column that isn't used in the timeline.  Skip it.
      DoubleArrayList timeline = inputs.getRow(i, /* skip */ 1);
      List<Changes> result = solver.c2cBottomUp(dates, timeline, arguments);
      if (result != null) {
        verify(result, expected.get(i));
      } else {
        nullCount++;
      }
    }
    // There are 3 inputs that don't have enough points.
    assertEquals(nullCount, 3);
  }

  /** Verify that the changes match the expected values. */
  private void verify(List<Changes> actual, Csv expected) {
    List<DoubleArrayList> values = expected.values;
    assertEquals(actual.size(), values.get(0).size());
    for (int j = 0; j < actual.size(); j++) {
      Changes c = actual.get(j);
      assertEquals(c.date, expected.getColumn("year").getDouble(j), 1e-9);
      assertEquals(c.value, expected.getColumn("index").getDouble(j), 1e-9);
      assertEquals(c.duration, expected.getColumn("duration").getDouble(j), 1e-9);
      assertEquals(c.magnitude, expected.getColumn("magnitude").getDouble(j), 1e-9);
      assertEquals(c.postMagnitude, expected.getColumn("postMagnitude").getDouble(j), 1e-9);
      assertEquals(c.postDuration, expected.getColumn("postDuration").getDouble(j), 1e-9);
      assertEquals(c.postRate, expected.getColumn("postRate").getDouble(j), 1e-9);
      assertEquals(c.rate, expected.getColumn("rate").getDouble(j), 1e-9);
    }
  }
}
