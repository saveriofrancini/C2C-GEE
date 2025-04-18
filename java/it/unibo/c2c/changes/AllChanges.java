package it.unibo.c2c.changes;

import java.util.ArrayList;
import java.util.List;

public interface AllChanges extends PostChanges, RegrowthChanges {
    static List<String> headers(String... prepend) {
        return headers(List.of(prepend));
    }

    static List<String> headers(List<String> prepend) {
        var result = new ArrayList<>(Changes.headers(prepend));
        result.addAll(PostChanges.headers());
        result.addAll(RegrowthChanges.headers());
        return result;
    }
}
