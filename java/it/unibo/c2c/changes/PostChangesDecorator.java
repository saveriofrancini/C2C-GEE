package it.unibo.c2c.changes;

import it.unimi.dsi.fastutil.doubles.DoubleList;

import java.util.List;

import static it.unibo.c2c.DoubleLists.doubleListOf;

record PostChangesDecorator(
        Changes changes,
        double postMagnitude,
        double postDuration
) implements PostChanges {

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
    public boolean hasNegativeMagnitude() {
        return changes.hasNegativeMagnitude();
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
    public double previousValue() {
        return changes.previousValue();
    }

    public double postRate() {
        return postMagnitude / postDuration;
    }

    @Override
    public DoubleList toDoubleList(List<Double> prepend) {
        var result = changes.toDoubleList(prepend);
        result.addAll(List.of(postMagnitude(), postDuration(), postRate()));
        return result;
    }

    @Override
    public AllChanges withRegrowth(List<Double> nextDates, List<Double> nextValues) {
        return new AllChangesDecorator(
                new RegrowthChangesDecorator(this, doubleListOf(nextDates), doubleListOf(nextValues))
        );
    }

    @Override
    public PostChanges withPost(double postMagnitude, double postDuration) {
        return new PostChangesDecorator(changes, postMagnitude, postDuration);
    }
}
