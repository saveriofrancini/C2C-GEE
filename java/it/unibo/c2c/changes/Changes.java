package it.unibo.c2c.changes;

import it.unimi.dsi.fastutil.doubles.DoubleList;

import java.util.ArrayList;
import java.util.List;

import static it.unibo.c2c.DoubleLists.doubleListOf;

public interface Changes {
    double date();

    double value();

    /**
     * Difference between the value of the current vertex and the value of the previous vertex.
     */
    double magnitude();

    default boolean hasNegativeMagnitude() {
        double magnitude = magnitude();
        return !Double.isNaN(magnitude) && magnitude < 0;
    }

    /**
     * Difference between the date of the current vertex and the date of the previous vertex.
     */
    double duration();

    default double rate() {
        return magnitude() / duration();
    }

    default double previousValue() {
        return value() - magnitude();
    }

    default DoubleList toDoubleList(double... prepend) {
        return toDoubleList(doubleListOf(prepend));
    }

    DoubleList toDoubleList(List<Double> prepend);

    RegrowthChanges withRegrowth(List<Double> nextDates, List<Double> nextValues);

    default RegrowthChanges withoutRegrowth() {
        return withRegrowth(List.of(), List.of());
    }

    PostChanges withPost(double postMagnitude, double postDuration);

    default PostChanges asPostChanges() {
        return (PostChanges) this;
    }

    default RegrowthChanges asRegrowthChanges() {
        return (RegrowthChanges) this;
    }

    static List<String> headers(String... prepend) {
        return headers(List.of(prepend));
    }

    static List<String> headers(List<String> prepend) {
        var result = new ArrayList<>(prepend);
        result.addAll(List.of("year", "value", "magnitude", "duration", "rate"));
        return result;
    }

    static Changes of(double date, double value, double magnitude, double duration) {
        return new BaseChanges(date, value, magnitude, duration);
    }
}
