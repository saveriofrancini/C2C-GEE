package it.unibo.c2c;

import it.unibo.c2c.changes.Changes;
import it.unimi.dsi.fastutil.doubles.DoubleList;

import java.util.ArrayList;
import java.util.List;

import static it.unibo.c2c.DoubleLists.argMin;

/**
 * This segmentation algorithm consist in a modification of the bottom up algorithm
 * (https://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.23.6570&rep=rep1&type=pdf) proposed by
 * Hermosilla et al. (2015)
 */
public class Segmentator {

    private Segmentator() {
    }

    private record Segment(int start, int finish) {
        public Segment changeFinish(int newFinish) {
            return new Segment(start, newFinish);
        }

        public Segment copyFinishFrom(Segment other) {
            return changeFinish(other.finish);
        }
    }

    public static List<Changes> segment(
            DoubleList dates,
            DoubleList values,
            C2cSolver.Args args
    ) {
        List<Segment> segments = new ArrayList<>();
        List<Double> mergeCost = new ArrayList<>();
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
        while (min < args.maxError || segments.size() > args.maxSegments) {
            // merge the adjacent segments with the smaller cost
            Segment segment = segments.get(index);
            Segment segment2 = segments.get(index + 1);
            segments.set(index, segment.copyFinishFrom(segment2));
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
            segmented.add(c);
        }
        // add last change
        centralIndex = segments.getLast().finish;
        leftIndex = segments.getLast().start;
        Changes c = changeMetricsCalculator(dates, values, leftIndex, centralIndex, rightIndex, args);
        segmented.add(c);
        return segmented;
    }

    private static double calculateError(DoubleList dates, DoubleList values, int start, int finish) {
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

    private static Changes changeMetricsCalculator(
            DoubleList dates,
            DoubleList values,
            int preIndex,
            int currIndex,
            int postIndex,
            C2cSolver.Args args
    ) {
        double currDate = dates.getDouble(currIndex);
        double currValue = values.getDouble(currIndex);
        boolean isLast = currIndex == values.size() - 1;
        boolean isFirst = currIndex == 0;
        double magnitude = isFirst ? Double.NaN : currValue - values.getDouble(preIndex);
        double duration = isFirst ? Double.NaN : currDate - dates.getDouble(preIndex);
        Changes change = Changes.of(currDate, currValue, magnitude, duration);
        if (args.postMetrics) {
            double postMagnitude = isLast ? Double.NaN : values.getDouble(postIndex) - currValue;
            double postDuration = isLast ? Double.NaN : dates.getDouble(postIndex) - currDate;
            change = change.withPost(postMagnitude, postDuration);
        }
        if (args.regrowthMetrics) {
            if (change.hasNegativeMagnitude()) {
                change = extendWithRegrowthMetrics(dates, values, change, currIndex);
            } else {
                change = change.withoutRegrowth();
            }
        }
        return change;
    }

    private static Changes extendWithRegrowthMetrics(
            DoubleList dates,
            DoubleList values,
            Changes changes,
            int currentIndex
    ) {
        try {
            int nextIndex;
            boolean hasRegrown = false;
            for (int i = 1; (nextIndex = currentIndex + i) < values.size(); i++) {
                var nextValue = values.getDouble(nextIndex);
                if (!hasRegrown && percent(changes, nextValue) >= 1.0) {
                    hasRegrown = true;
                }
                if (i >= 6 && hasRegrown) {
                    nextIndex++;
                    break;
                }
            }
            return changes.withRegrowth(
                    dates.subList(currentIndex + 1, nextIndex),
                    values.subList(currentIndex + 1, nextIndex)
            );
        } catch (ArrayIndexOutOfBoundsException e) {
            return changes.withoutRegrowth();
        }
    }

    private static double percent(Changes changes, double value) {
        double target = Math.abs(changes.magnitude());
        double current = Math.abs(value - changes.value());
        return current / target;
    }

    private static double lerp(double y1, double y2, double x) {
        return y1 * (1 - x) + y2 * x;
    }
}
