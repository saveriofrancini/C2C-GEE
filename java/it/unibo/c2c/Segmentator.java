package it.unibo.c2c;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import java.util.ArrayList;
import java.util.List;

/**
 * This segmentation algorithm consist in a modification of the bottom up algorithm
 * (https://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.23.6570&rep=rep1&type=pdf) proposed by
 * Hermosilla et al. (2015)
 */
public class Segmentator {

  private static final int REGROWTH_INDICATOR_OFFSET = 5;

  private Segmentator() {}

  private static class Segment {
    int start;
    int finish;

    public Segment(int start, int finish) {
      this.start = start;
      this.finish = finish;
    }
  }

  public static List<Changes> segment(
      DoubleArrayList dates, DoubleArrayList values, C2cSolver.Args args) {
    double maxError = args.maxError;
    int maxSegm = args.maxSegments;
    ArrayList<Segment> segments = new ArrayList<>();
    ArrayList<Double> mergeCost = new ArrayList<>();
    // initial segments
    for (int i = 0; i < values.size() - 1; i++) {
      segments.add(new Segment(i, i + 1));
    }
    // merging cost of initial segments
    for (int i = 0; i < segments.size() - 1; i++) {
      Segment left = segments.get(i);
      Segment right = segments.get(i + 1);
      mergeCost.add(calculateError(dates, values, left.start, right.finish));
    }
    // minimum merging cost
    int index = argMin(mergeCost);
    double min = mergeCost.get(index);
    while (min < maxError || segments.size() > maxSegm) {
      // merge the adjacent segments with the smaller cost
      Segment segment = segments.get(index);
      Segment segment2 = segments.get(index + 1);
      segment.finish = segment2.finish;
      // update segments
      segments.remove(index + 1);
      mergeCost.remove(index);
      if (mergeCost.isEmpty()) {
        break;
      }
      if (index + 1 < segments.size()) {
        Segment left = segments.get(index);
        Segment right = segments.get(index + 1);
        mergeCost.set(index, calculateError(dates, values, left.start, right.finish));
      }
      if (index - 1 >= 0) {
        Segment left = segments.get(index - 1);
        Segment right = segments.get(index);
        mergeCost.set(index - 1, calculateError(dates, values, left.start, right.finish));
      }
      index = argMin(mergeCost);
      min = mergeCost.get(index);
    }
    List<Changes> segmented = new ArrayList<>();
    int leftIndex = 999;
    int rightIndex = 999;
    int centralIndex;
    for (int i = 0; i < segments.size(); i++) {
      centralIndex = segments.get(i).start;
      if (i == 0) {
        rightIndex = segments.get(i).finish;
      } else {
        leftIndex = segments.get(i - 1).start;
        rightIndex = segments.get(i).finish;
      }
      Changes c = changeMetricsCalculator(dates, values, leftIndex, centralIndex, rightIndex, args);
      if (c != null) {
        segmented.add(c);
      }
    }
    // add last change
    centralIndex = segments.get(segments.size() - 1).finish;
    leftIndex = segments.get(segments.size() - 1).start;
    Changes c = changeMetricsCalculator(dates, values, leftIndex, centralIndex, rightIndex, args);
    if (c != null) {
      segmented.add(c);
    }
    return segmented;
  }

  private static double calculateError(
      DoubleArrayList dates, DoubleArrayList values, int start, int finish) {
    // linearInterpolation
    double y1 = values.getDouble(start);
    double y2 = values.getDouble(finish);
    double x1 = dates.getDouble(start);
    double x2 = dates.getDouble(finish);
    double timeWindow = x2 - x1;
    double error = 0;
    for (int i = start; i <= finish; i++) {
      double xFraction = (dates.getDouble(i) - x1) / timeWindow;
      double interpolated = lerp(y1, y2, xFraction);
      double diff = values.getDouble(i) - interpolated;
      error += diff * diff;
    }
    return Math.sqrt(error / (finish - start));
  }

  /**
   * For calculating regrowth we want to find when we are at .6, .8 and 1.0 of the previous
   * breakpoint.
   */
  private static Changes.RegrowthMetric calculateRegrowthMetric(
      DoubleArrayList dates,
      DoubleArrayList values,
      int currIndex,
      int postIndex,
      double preValue,
      double currValue,
      double magnitude,
      C2cSolver.Args args) {
    // This is the last breakpoint so no regrowth can be calculated as there is no more trend.
    if (currIndex == values.size() - 1) {
      return Changes.EMPTY_REGROWTH;
    }
    int index100 = -1;
    int index80 = -1;
    int index60 = -1;
    if (!Double.isNaN(preValue)) {
      // Note: regrowth metric extends beyond the current change window.
      for (int i = currIndex + 1; i < values.size(); ++i) {
        final double value = values.getDouble(i);
        final double regrowthRatio;
        if (args.useRelativeRegrowth) {
          regrowthRatio = (currValue - value) / magnitude;
        } else {
          regrowthRatio = value / preValue;
        }
        if (regrowthRatio >= 1.0 && index100 == -1) {
          index100 = i;
        }
        if (regrowthRatio >= 0.8 && index80 == -1) {
          index80 = i;
        }
        if (regrowthRatio >= 0.6 && index60 == -1) {
          index60 = i;
        }
        // End regrowth calculation if we reached 100% regrowth.
        if (index100 != -1) {
          break;
        }
      }
    }
    double indicatorSample =
        currIndex + REGROWTH_INDICATOR_OFFSET < values.size()
            ? values.getDouble(currIndex + REGROWTH_INDICATOR_OFFSET)
            : Double.NaN;
    double indexRegrowth = indicatorSample - currValue;
    double currDate = dates.getDouble(currIndex);
    // System.out.println(indexRegrowth / magnitude);
    return new Changes.RegrowthMetric(
        indexRegrowth,
        /* recoveryIndicator= */ indexRegrowth / magnitude,
        /* regrowth60= */ index60 == -1 ? Double.NaN : dates.getDouble(index60) - currDate,
        /* regrowth80= */ index80 == -1 ? Double.NaN : dates.getDouble(index80) - currDate,
        /* regrowth100= */ index100 == -1 ? Double.NaN : dates.getDouble(index100) - currDate);
  }

  public static Changes changeMetricsCalculator(
      DoubleArrayList dates,
      DoubleArrayList values,
      int preIndex,
      int currIndex,
      int postIndex,
      C2cSolver.Args args) {
    final double currDate = dates.getDouble(currIndex);
    final double currValue = values.getDouble(currIndex);
    final double magnitude;
    final double duration;
    final double postMagnitude;
    final double postDuration;
    final double preValue;
    if (currIndex == 0) {
      magnitude = Double.NaN;
      duration = Double.NaN;
      postMagnitude = values.getDouble(postIndex) - currValue;
      postDuration = dates.getDouble(postIndex) - currDate;
      preValue = Double.NaN;
    } else if (currIndex == values.size() - 1 || args.postMetrics == false) {
      magnitude = currValue - values.getDouble(preIndex);
      duration = currDate - dates.getDouble(preIndex);
      postMagnitude = Double.NaN;
      postDuration = Double.NaN;
      preValue = values.getDouble(preIndex);
    } else {
      magnitude = currValue - values.getDouble(preIndex);
      duration = currDate - dates.getDouble(preIndex);
      postMagnitude = values.getDouble(postIndex) - currValue;
      postDuration = dates.getDouble(postIndex) - currDate;
      preValue = values.getDouble(preIndex);
    }
    if (args.negativeMagnitudeOnly) {
      if (!(magnitude < 0)) {
        return null;
      }
    }
    var regrowth =
        args.includeRegrowth
            ? calculateRegrowthMetric(
                dates, values, currIndex, postIndex, preValue, currValue, magnitude, args)
            : Changes.EMPTY_REGROWTH;
    return Changes.create(
        currDate, currValue, magnitude, duration, postMagnitude, postDuration, regrowth);
  }

  private static double lerp(double y1, double y2, double x) {
    return y1 * (1 - x) + y2 * x;
  }

  private static int argMin(List<Double> mergeCost) {
    double min = mergeCost.get(0);
    int pos = 0;
    for (int i = 1; i < mergeCost.size(); i++) {
      double value = mergeCost.get(i);
      if (min > value) {
        min = value;
        pos = i;
      }
    }
    return pos;
  }
}
