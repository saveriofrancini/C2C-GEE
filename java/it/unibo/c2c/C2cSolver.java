package it.unibo.c2c;

import com.google.earthengine.api.base.ArgsBase;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.Nullable;

/** Solver for the BottomUp Segmentation algorithm. */
public class C2cSolver {

  public static class Args extends ArgsBase {
    @Doc(help = "Maximum error (RMSE) allowed to remove points and construct segments.")
    @Optional
    public double maxError = 75;

    @Doc(help = "Maximum number of segments to be fitted on the time series.")
    @Optional
    public int maxSegments = 6;

    @Doc(help = "Year of the first image in the output image collection.")
    @Optional
    public int startYear = 1984;

    @Doc(help = "Year of the last image in the output image collection.")
    @Optional
    public int endYear = 2019;

    @Doc(help = "Whether to apply the pre-infill process.")
    @Optional
    public boolean infill = true;

    @Doc(help = "Tolerance of spikes in the time series. A value of 1 indicates no spike removal.")
    @Optional
    public double spikesTolerance = 0.85;

    @Doc(help = "Add post metric information.")
    @Optional
    public boolean includePostMetrics = true;

    @Doc(help = "Add regrowth information.")
    @Optional
    public boolean includeRegrowth = false;

    @Doc(help = "Use changes to linearly interpolate regrowth metrics.")
    @Optional
    public boolean interpolateRegrowth = false;

    @Doc(help = "Calculate regrowth by % of positive magnitude.")
    @Optional
    public boolean useRelativeRegrowth = false;

    @Doc(help = "Whether filter out changes having a non-negative magnitude.")
    @Optional
    public boolean negativeMagnitudeOnly = false;
  }

  public @Nullable List<Changes> c2cBottomUp(
      DoubleArrayList dates, DoubleArrayList values, Args args) {
    if (values.doubleStream().filter(v -> v != 0).count() < 3) {
      return null;
    }
    // Pre-process as requested.
    if (args.infill) {
      fillValues(values);
    }
    if (args.spikesTolerance < 1) {
      despikeTimeLine(values, args.spikesTolerance);
    }
    
    // Start segmentation.
    if (args.includeRegrowth && args.interpolateRegrowth) {
      args.includeRegrowth = false;
      List<Changes> changes = Segmentator.segment(dates, values, args);
      interpolateValuesInplace(dates, values, changes);
      args.includeRegrowth = true;
      return interpolatedRegrowth(dates, values, changes, args);
    } else {
      return Segmentator.segment(dates, values, args);
    }
  }

  List<Changes> interpolatedRegrowth(
      DoubleArrayList dates, DoubleArrayList values, List<Changes> changes, C2cSolver.Args args) {
    double prevChangeValue = Double.NaN;
    ArrayList<Changes> changesWithRegrowth = new ArrayList<>();
    for (Changes change : changes) {
      changesWithRegrowth.add((Segmentator.addRegrowthToChange(dates, values, change, args)));
      prevChangeValue = change.value();
    }
    return changesWithRegrowth;
  }

  private static void fillValues(DoubleArrayList values) {
    // Infill missing data
    for (int i = 0; i < values.size(); i++) {
      if (values.getDouble(i) != 0) {
        continue;
      }
      // Find the first two valid observations in timeLine before and after i
      int left1 = findValid(values, i, -1);
      int left2 = findValid(values, left1, -1);
      int right1 = findValid(values, i, 1);
      int right2 = findValid(values, right1, 1);
      if (left2 == -1) {
        values.set(i, values.getDouble(right1));
      } else if (right2 == -1) {
        if (right1 != -1) {
          values.set(i, values.getDouble(right1));
        } else {
          values.set(i, values.getDouble(left1));
        }
      } else {
        double leftDif = Math.abs(values.getDouble(left1) - values.getDouble(left2));
        double rightDif = Math.abs(values.getDouble(right1) - values.getDouble(right2));
        // Fill using value with smaller difference
        if (leftDif < rightDif) {
          values.set(i, values.getDouble(left1));
        } else {
          values.set(i, values.getDouble(right1));
        }
      }
    }
    int size = values.size();
    double lastValue = values.getDouble(size - 1);
    double lastValueL = values.getDouble(size - 2);
    double lastValueLL = values.getDouble(size - 3);
    double lastDif = Math.abs(lastValue - lastValueL);
    double secondLastDif = Math.abs(lastValueL - lastValueLL);
    if (lastDif >= secondLastDif) {
      values.set(size - 1, lastValueL);
    }
  }

  /** Find the first non-zero from `start` in the direction of `dir`. Returns -1 if none found. */
  private static int findValid(DoubleArrayList list, int start, int dir) {
    if (start == -1) {
      return -1;
    }
    int limit = dir == 1 ? list.size() : -1;
    for (int i = start + dir; i != limit; i += dir) {
      if (list.getDouble(i) != 0) {
        return i;
      }
    }
    return -1;
  }

  private static void despikeTimeLine(DoubleArrayList values, double spikesTolerance) {
    for (int i = 1; i < values.size() - 1; i++) {
      double left = values.getDouble(i - 1);
      double center = values.getDouble(i);
      double right = values.getDouble(i + 1);
      double fitted = (left + right) / 2;
      double delta = Math.abs(left - right);
      double spikeValue = Math.abs(fitted - center);
      double despikeProportion = delta / spikeValue;
      //      despike conditions
      //      #1# The value of the spike is greater than 100
      //      #2# The difference between spectral values on either side of the spike
      //      is less than 1-despike desawtooth proportion of the spike itself" (Landtrendr)
      if (spikeValue > 100 && despikeProportion < (1 - spikesTolerance)) {
        // double leftValueOfT = values.getDouble(i - 1);
        // double rightValueOfT = values.getDouble(i + 1);
        // double centerValueFittedOfT = (leftValueOfT + rightValueOfT) / 2;
        values.set(i, fitted);
      }
    }
  }

  private static void interpValues(
    DoubleArrayList dates,
    DoubleArrayList values,
    int startIndex,
    int endIndex,
    double x1,
    double x2,
    double y1,
    double y2) {
    for (int i = startIndex; i < endIndex; i++) {
      double x = dates.get(i);
      double interpValue = y1 + (y2 - y1) * (x - x1) / (x2 - x1);
      values.set(i, interpValue);
    }
  }

  /** Linearly interpolates the input timeline values using the changes breakpoints. */
  private static void interpolateValuesInplace(
    DoubleArrayList dates,
    DoubleArrayList values,
    List<Changes> changes) {

    if (changes == null || changes.isEmpty()) {
      return;
    }
    // Interpolation starts with the start of the series and ends at the start of the first breakpoint.
    int endIndex = changes.get(0).dateIndex() - 1;
    double x1 = dates.get(0);
    double x2 = dates.get(endIndex);
    double y1 = values.get(0);
    double y2 = values.get(endIndex);
    interpValues(dates, values, /* startIndex= */ 0, endIndex, x1, x2, y1, y2);

    // Now interpolate between the rest of the breakpoints.
    for (int changeIndex = 0; changeIndex < changes.size() - 1; ++changeIndex) {
      Changes currChange = changes.get(changeIndex);
      int nextChangeIndex = changes.get(changeIndex + 1).dateIndex();
      x1 = currChange.date();
      x2 = dates.get(nextChangeIndex - 1);
      y1 = currChange.value();
      y2 = values.get(nextChangeIndex - 1);
      interpValues(dates, values, currChange.dateIndex(), nextChangeIndex, x1, x2, y1, y2);
    }
    // For the end of the series interpolate from the start of the last breakpoint until the end of the series.
    Changes lastChange = changes.get(changes.size()-1);
    x1 = lastChange.date();
    x2 = dates.get(dates.size() - 1);
    y1 = lastChange.value();
    y2 = values.get(values.size() - 1);
    interpValues(dates, values, lastChange.dateIndex() + 1, values.size() - 1, x1, x2, y1, y2);
  }
}

