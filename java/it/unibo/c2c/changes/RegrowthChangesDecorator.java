package it.unibo.c2c.changes;

import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.ints.IntList;

import java.util.List;
import java.util.Objects;

import static it.unibo.c2c.DoubleLists.doubleListOf;

record RegrowthChangesDecorator(
        Changes changes,
        DoubleList nextDates,
        DoubleList nextValues
) implements RegrowthChanges {

    RegrowthChangesDecorator(Changes changes, DoubleList nextDates, DoubleList nextValues) {
        this.changes = Objects.requireNonNull(changes);
        this.nextDates = Objects.requireNonNull(nextDates);
        this.nextValues = Objects.requireNonNull(nextValues);
        if (nextDates.size() != nextValues.size()) {
            throw new IllegalArgumentException("nextDates and nextValues must have the same size");
        }
    }

    @Override
    public double date() {
        return changes.date();
    }

    @Override
    public double value() {
        return changes.value();
    }

    @Override
    public double magnitude() {
        return changes.magnitude();
    }

    @Override
    public double duration() {
        return changes.duration();
    }

    @Override
    public double rate() {
        return changes.rate();
    }

    @Override
    public DoubleList toDoubleList(List<Double> prepend) {
        var result = changes.toDoubleList(prepend);
        result.add(indexRegrowth());
        result.add(recoveryIndicator());
        result.add(yearsToRegrowth(60));
        result.add(yearsToRegrowth(80));
        result.add(yearsToFullRegrowth());
        return result;
    }

    private double getValueAfterYears(int years) {
        try {
            return nextValues.getDouble(years - 1);
        } catch (IndexOutOfBoundsException e) {
            return 0;
        }
    }

    private static final IntList DATES_TO_SAMPLE = IntList.of(4, 5, 6);

    @Override
    public double indexRegrowth() {
        if (nextValues.isEmpty()) return Double.NaN;
        try {
            var average = DATES_TO_SAMPLE.intStream()
                    .mapToDouble(this::getValueAfterYears)
                    .filter(i -> i != 0)
                    .average()
                    .orElseGet(() -> Double.NaN);
            return average - value();
        } catch (IndexOutOfBoundsException e) {
            return Double.NaN;
        }
    }

    @Override
    public double yearsToRegrowth(int percent) {
        if (percent < 1 || percent > 100) throw new IllegalArgumentException("Percent must be between 1 and 100");
        if (nextValues.isEmpty()) return Double.NaN;
        try {
            double target = percent / 100.0;
            double threshold = (value() - magnitude()) * target;
            for (int i = 0; i < nextValues.size(); i++) {
                var nextValue = nextValues.getDouble(i);
                if (nextValue >= threshold) {
                    return nextDates.getDouble(i) - date();
                }
            }
            return Double.NaN;
        } catch (IndexOutOfBoundsException e) {
            return Double.NaN;
        }
    }

    @Override
    public RegrowthChanges withRegrowth(List<Double> nextDates, List<Double> nextValues) {
        return new RegrowthChangesDecorator(changes, doubleListOf(nextDates), doubleListOf(nextValues));
    }

    @Override
    public AllChanges withPost(double postMagnitude, double postDuration) {
        return new AllChangesDecorator(
                new RegrowthChangesDecorator(
                        changes.withPost(postMagnitude, postDuration),
                        nextDates,
                        nextValues
                )
        );
    }
}
