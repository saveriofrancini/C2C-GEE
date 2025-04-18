package it.unibo.c2c.changes;

import it.unimi.dsi.fastutil.doubles.DoubleList;

import java.util.List;

import static it.unibo.c2c.DoubleLists.doubleListOf;

/**
 * Just a POD to record a change segment.
 */
record BaseChanges(
        double date,
        double value,
        double magnitude,
        double duration
) implements Changes {

    public double rate() {
        return magnitude / duration;
    }

    @Override
    public DoubleList toDoubleList(List<Double> prepend) {
        var result = doubleListOf(prepend);
        var list = doubleListOf(date, value, magnitude, duration, rate());
        result.addAll(list);
        return result;
    }

    @Override
    public RegrowthChanges withRegrowth(List<Double> nextDates, List<Double> nextValues) {
        return new RegrowthChangesDecorator(this, doubleListOf(nextDates), doubleListOf(nextValues));
    }

    @Override
    public PostChanges withPost(double postMagnitude, double postDuration) {
        return new PostChangesDecorator(this, postMagnitude, postDuration);
    }
}
