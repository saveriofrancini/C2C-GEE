package it.unibo.c2c;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class BottomupTest {

  private static final String SAMPLES_FILE = "/it/unibo/c2c/testdata/input.csv";
  private static final String EXPECTED_FILE = "/it/unibo/c2c/testdata/output.csv";
  private static final String EXPECTED_FILTERED = "/it/unibo/c2c/testdata/output-filtered.csv";
  private static final String EXPECTED_REGROWTH =
      "/it/unibo/c2c/testdata/output-regrowth-negonly.csv";
  private static final String EXPECTED_REGROWTH_RELATIVE =
      "/it/unibo/c2c/testdata/output-regrowth-relative-negonly.csv";

  @Test
  public void c2c_defaultArgs() throws Exception {
    C2cSolver.Args args = new C2cSolver.Args();

    runGoldensTest(SAMPLES_FILE, EXPECTED_FILE, args);
  }

  @Test
  public void c2c_negativeMagnitudes() throws Exception {
    C2cSolver.Args args = new C2cSolver.Args();
    args.negativeMagnitudeOnly = true;
    // To regenerate the golden data:
    // bazel run --java_runtime_version=21 c2c -- javatests/it/unibo/c2c/testdata/input.csv \
    //   --negativeMagnitudeOnly > \
    //   javatests/it/unibo/c2c/testdata/output-filtered.csv
    runGoldensTest(SAMPLES_FILE, EXPECTED_FILTERED, args);
  }

  @Test
  public void c2c_regrowth_noPostRate_negitiveMagnitudes() throws Exception {
    C2cSolver.Args args = new C2cSolver.Args();
    args.includeRegrowth = true;
    args.includePostMetrics = false;
    args.negativeMagnitudeOnly = true;
    // To regenerate the golden data:
    // bazel run --java_runtime_version=21 c2c -- javatests/it/unibo/c2c/testdata/input.csv \
    //   --includePostMetrics=false --includeRegrowth=true --negativeMagnitudeOnly > \
    //   javatests/it/unibo/c2c/testdata/output-regrowth-negonly.csv
    runGoldensTest(SAMPLES_FILE, EXPECTED_REGROWTH, args);
  }

  @Test
  public void c2c_regrowth_relative_noPostRate_negitiveMagnitudes() throws Exception {
    C2cSolver.Args args = new C2cSolver.Args();
    args.includeRegrowth = true;
    args.includePostMetrics = false;
    args.negativeMagnitudeOnly = true;
    // To regenerate the golden data:
    // bazel run --java_runtime_version=21 c2c -- javatests/it/unibo/c2c/testdata/input.csv \
    //   --includePostMetrics=false --includeRegrowth=true --negativeMagnitudeOnly
    // --useRelativeRegrowth > \
    //   javatests/it/unibo/c2c/testdata/output-regrowth-relative-negonly.csv
    runGoldensTest(SAMPLES_FILE, EXPECTED_REGROWTH, args);
  }

  private void runGoldensTest(String inputPath, String goldenPath, C2cSolver.Args args) {
    // Read input file.  It has dates as column headers and each row is a full timeline.
    Csv inputs = Csv.vertical(getClass().getResourceAsStream(inputPath));
    DoubleArrayList dates = inputs.getDates();
    int numberOfInputs = inputs.values().get(0).size();
    // Read expected results file and split by plot ID.
    List<Csv> expected =
        Csv.vertical(getClass().getResourceAsStream(goldenPath)).groupByColumn("id");
    // Apply the Main function on each timeLine.
    int nullCount = 0;
    C2cSolver solver = new C2cSolver();
    for (int i = 0; i < numberOfInputs; i++) {
      // The inputs have a plot ID in the first column that isn't used in the timeline.  Skip it.
      DoubleArrayList timeline = inputs.getRow(i, /* skip= */ 1);
      List<Changes> result = solver.c2cBottomUp(dates, timeline, args);
      if (result != null) {
        try {
          verify(result, expected.get(i), args);
        } catch (AssertionError e) {
          throw new AssertionError(String.format("Failure for input %s", i), e);
        }
      } else {
        System.out.println("null line: " + i);
        nullCount++;
      }
    }
    // There are 3 inputs that don't have enough points.
    assertEquals(3, nullCount);
  }

  /** Verify that the changes match the expected values. */
  private void verify(List<Changes> actual, Csv expected, C2cSolver.Args args) {
    List<DoubleArrayList> values = expected.values();
    // Empty only happens if we have negativeMagnitudeOnly=true, output should contain NaN dates.
    if (actual.isEmpty()) {
      assertTrue(
          "Actual changes is empty, expected \"year\" to be NaN.",
          Double.isNaN(expected.getColumn("year").getDouble(0)));
      return;
    }
    assertEquals(
        String.format(
            "Mismatch in change count: Actual: %s, Expected: %s \n%s \n vs \n%s",
            actual.size(), values.get(0).size(), actual, values.get(0)),
        actual.size(),
        values.get(0).size());
    for (int j = 0; j < actual.size(); j++) {
      Changes actualChanges = actual.get(j);
      final Changes.RegrowthMetric regrowth;
      if (args.includeRegrowth) {
        regrowth =
            new Changes.RegrowthMetric(
                expected.getColumn("indexRegrowth").getDouble(j),
                expected.getColumn("recoveryIndicator").getDouble(j),
                expected.getColumn("regrowth60").getDouble(j),
                expected.getColumn("regrowth80").getDouble(j),
                expected.getColumn("regrowth100").getDouble(j));
      } else {
        regrowth = Changes.EMPTY_REGROWTH;
      }
      Changes expectedChanges =
          new Changes(
              expected.getColumn("year").getDouble(j),
              expected.getColumn("index").getDouble(j),
              expected.getColumn("magnitude").getDouble(j),
              expected.getColumn("duration").getDouble(j),
              args.includePostMetrics
                  ? expected.getColumn("postMagnitude").getDouble(j)
                  : Double.NaN,
              args.includePostMetrics
                  ? expected.getColumn("postDuration").getDouble(j)
                  : Double.NaN,
              expected.getColumn("rate").getDouble(j),
              args.includePostMetrics ? expected.getColumn("postRate").getDouble(j) : Double.NaN,
              regrowth.indexRegrowth(),
              regrowth.recoveryIndicator(),
              regrowth.regrowth60(),
              regrowth.regrowth80(),
              regrowth.regrowth100());
      try {
        assertChangesEquals(expectedChanges, actualChanges);
      } catch (AssertionError e) {
        throw new AssertionError(
            String.format(
                "Change differed for id: %s, year: %s",
                expected.getColumn("id").getDouble(j), expected.getColumn("year").getDouble(j)),
            e);
      }
    }
  }

  private void assertChangesEquals(Changes expected, Changes actual) {
    try {
      assertEquals(expected.date(), actual.date(), 1e-9);
      assertEquals(expected.value(), actual.value(), 1e-9);
      assertEquals(expected.magnitude(), actual.magnitude(), 1e-9);
      assertEquals(expected.duration(), actual.duration(), 1e-9);
      assertEquals(expected.postMagnitude(), actual.postMagnitude(), 1e-9);
      assertEquals(expected.postDuration(), actual.postDuration(), 1e-9);
      assertEquals(expected.rate(), actual.rate(), 1e-9);
      assertEquals(expected.postRate(), actual.postRate(), 1e-9);
      assertEquals(expected.indexRegrowth(), actual.indexRegrowth(), 1e-9);
      assertEquals(expected.recoveryIndicator(), actual.recoveryIndicator(), 1e-9);
      assertEquals(expected.regrowth60(), actual.regrowth60(), 1e-9);
      assertEquals(expected.regrowth80(), actual.regrowth80(), 1e-9);
      assertEquals(expected.regrowth100(), actual.regrowth100(), 1e-9);
    } catch (AssertionError e) {
      throw new AssertionError(String.format("Expected:\n%s\nbut was:\n%s", expected, actual), e);
    }
  }
}
