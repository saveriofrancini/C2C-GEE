package it.unibo.c2c.changes;

import java.util.ArrayList;
import java.util.List;

public interface PostChanges extends Changes {

    /**
     * Difference between the value of the next vertex and the value of the current vertex.
     */
    double postMagnitude();

    /**
     * Difference between the date of the next vertex and the date of the current vertex.
     */
    double postDuration();

    default double postRate() {
        return postMagnitude() / postDuration();
    }

    static List<String> headers(String... prepend) {
        return headers(List.of(prepend));
    }

    static List<String> headers(List<String> prepend) {
        var result = new ArrayList<>(prepend);
        result.addAll(List.of("postMagnitude", "postDuration", "postRate"));
        return result;
    }

    @Override
    AllChanges withRegrowth(List<Double> nextDates, List<Double> nextValues);

    @Override
    default AllChanges withoutRegrowth() {
        return withRegrowth(List.of(), List.of());
    }
}
