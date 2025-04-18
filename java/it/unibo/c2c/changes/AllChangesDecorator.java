package it.unibo.c2c.changes;

import it.unimi.dsi.fastutil.doubles.DoubleList;

import java.util.List;
import java.util.Objects;

record AllChangesDecorator(RegrowthChangesDecorator changes) implements AllChanges {
    AllChangesDecorator {
        if (!(Objects.requireNonNull(changes).changes() instanceof PostChanges)) {
            throw new IllegalArgumentException("changes must be a PostChanges");
        }
    }

    private PostChanges inner() {
        return (PostChanges) changes.changes();
    }

    @Override
    public double postMagnitude() {
        return inner().postMagnitude();
    }

    @Override
    public double postDuration() {
        return inner().postDuration();
    }

    @Override
    public double indexRegrowth() {
        return changes().indexRegrowth();
    }

    @Override
    public double yearsToRegrowth(int percent) {
        return changes().yearsToRegrowth(percent);
    }

    @Override
    public double date() {
        return inner().date();
    }

    @Override
    public double value() {
        return inner().value();
    }

    @Override
    public double magnitude() {
        return inner().magnitude();
    }

    @Override
    public double duration() {
        return inner().duration();
    }

    @Override
    public DoubleList toDoubleList(List<Double> prepend) {
        return changes.toDoubleList(prepend);
    }

    @Override
    public AllChanges withRegrowth(List<Double> nextDates, List<Double> nextValues) {
        return new AllChangesDecorator((RegrowthChangesDecorator) changes.withRegrowth(nextDates, nextValues));
    }

    @Override
    public AllChanges withPost(double postMagnitude, double postDuration) {
        return changes.withPost(postMagnitude, postDuration);
    }
}
