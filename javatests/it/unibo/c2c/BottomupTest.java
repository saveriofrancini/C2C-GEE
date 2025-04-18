package it.unibo.c2c;

import it.unibo.c2c.changes.Changes;
import it.unibo.c2c.changes.PostChanges;
import it.unibo.c2c.changes.RegrowthChanges;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertFalse;

@RunWith(JUnit4.class)
public class BottomupTest {

    private static final String SAMPLES_FILE = "input.csv";
    private static final String EXPECTED_FILE_DEFAULT = "output-default.csv";
    private static final String EXPECTED_FILE_REVERT = "output-reverted.csv";
    private static final String EXPECTED_FILE_FILTER = "output-filtered.csv";
    private static final String EXPECTED_FILE_FILTERED_REGROWTH = "output-regrowth-negonly.csv";

    private static Csv inputs, expectedDefault, expectedRevert, expectedFilter, expectedFilteredRegrowth;

    @Rule
    public TestName testName = new TestName();

    public int lastID;

    @BeforeClass
    public static void loadFiles() {
        // Read input file.  It has dates as column headers and each row is a full timeline.
        inputs = Csv.vertical(BottomupTest.class.getResourceAsStream(SAMPLES_FILE));
        // Read expected results files
        expectedDefault = Csv.vertical(BottomupTest.class.getResourceAsStream(EXPECTED_FILE_DEFAULT));
        expectedRevert = Csv.vertical(BottomupTest.class.getResourceAsStream(EXPECTED_FILE_REVERT));
        expectedFilter = Csv.vertical(BottomupTest.class.getResourceAsStream(EXPECTED_FILE_FILTER));
        expectedFilteredRegrowth = Csv.vertical(BottomupTest.class.getResourceAsStream(EXPECTED_FILE_FILTERED_REGROWTH));
    }

    @Test
    public void testC2cBottomUpWithDefaultArgs() {
        testC2cBottomUpWithArgs(new C2cSolver.Args(), expectedDefault);
    }

    @Test
    public void testC2cBottomUpWithRevertBand() {
        var args = new C2cSolver.Args();
        args.revertBand = true;
        testC2cBottomUpWithArgs(args, expectedRevert);
    }

    @Test
    public void testC2cBottomUpWithNegativeMagnitudeOnly() {
        var args = new C2cSolver.Args();
        args.negativeMagnitudeOnly = true;
        testC2cBottomUpWithArgs(args, expectedFilter);
    }

    @Test
    public void testC2cBottomUpWithNegativeMagnitudeOnlyAndRegrowMetrics() {
        var args = new C2cSolver.Args();
        args.negativeMagnitudeOnly = true;
        args.regrowthMetrics = true;
        args.postMetrics = false;
        testC2cBottomUpWithArgs(args, expectedFilteredRegrowth);
    }

    private void testC2cBottomUpWithArgs(C2cSolver.Args args, Csv expected) {
        // There are 3 inputs that don't have enough points.
        testC2cBottomUpWithArgs(args, expected, 3);
    }

    private void testC2cBottomUpWithArgs(C2cSolver.Args args, Csv expected, int expectedNullCount) {
        var dates = inputs.getHeadersAsDoubles();
        //  Split expectedById results file by plot ID.
        Map<Double, Csv> expectedById = expected.groupByColumn("id");
        // Apply the Main function on each timeLine.
        int nullCount = 0;
        C2cSolver solver = new C2cSolver(args);
        for (int i = 0; i < inputs.getRowsCount(); i++) {
            // The inputs have a plot ID in the first column that isn't used in the timeline.  Skip it.
            DoubleList timeline = inputs.getRow(i, /* skip= */ 1);
            List<Changes> result = solver.c2cBottomUp(dates, timeline);
            Double id = Double.valueOf(i);
            Csv expectedCsv = expectedById.getOrDefault(id, Csv.empty(expected.headers()));
            if (result != null) {
                verify(i, result, expectedCsv);
            } else {
                nullCount++;
                System.out.printf("[%s] Null result for row: %d\n", testName.getMethodName(), i);
                assertFalse(expectedById.containsKey(id));
                verify(i, List.of(), expectedCsv);
            }
        }
        assertEquals(expectedNullCount, nullCount);
    }

    /**
     * Verify that the changes match the expected values.
     */
    private void verify(int id, List<Changes> actual, Csv expected) {
        this.lastID = id;
        List<DoubleList> values = expected.values();
        assertEquals(actual.size(), values.getFirst().size());
        for (int j = 0; j < actual.size(); j++) {
            Changes c = actual.get(j);
            assertEquals(c.date(), expected.getColumn("year").getDouble(j));
            assertEquals(c.value(), expected.getColumn("value").getDouble(j));
            assertEquals(c.duration(), expected.getColumn("duration").getDouble(j));
            assertEquals(c.magnitude(), expected.getColumn("magnitude").getDouble(j));
            if (c instanceof PostChanges pc) {
                assertEquals(pc.postMagnitude(), expected.getColumn("postMagnitude").getDouble(j));
                assertEquals(pc.postDuration(), expected.getColumn("postDuration").getDouble(j));
                assertEquals(pc.postRate(), expected.getColumn("postRate").getDouble(j));
                assertEquals(pc.rate(), expected.getColumn("rate").getDouble(j));
            }
            if (c instanceof RegrowthChanges rc) {
                assertEquals(rc.indexRegrowth(), expected.getColumn("indexRegrowth").getDouble(j));
                assertEquals(rc.recoveryIndicator(), expected.getColumn("recoveryIndicator").getDouble(j));
                assertEquals(rc.yearsToRegrowth(60), expected.getColumn("y2r60").getDouble(j));
                assertEquals(rc.yearsToRegrowth(80), expected.getColumn("y2r80").getDouble(j));
                assertEquals(rc.yearsToFullRegrowth(), expected.getColumn("y2r100").getDouble(j));
            }
        }
    }

    private void assertEquals(double actual, double expected) {
        Assert.assertEquals(
                "Failed equality assertion in %s, row with ID %d: %s != %s".formatted(
                        testName.getMethodName(),
                        lastID,
                        expected,
                        actual
                ),
                expected,
                actual,
                1e-9
        );
    }
}
