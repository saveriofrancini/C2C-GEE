package it.unibo.c2c;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;

import java.util.List;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

public class DoubleLists {
    private DoubleLists() {
    }

    public static DoubleList doubleListOf(double... values) {
        return DoubleArrayList.wrap(values);
    }

    public static DoubleList doubleListOf(List<Double> values) {
        return new DoubleArrayList(values);
    }

    public static DoubleList doubleListOf(DoubleStream values) {
        return new DoubleArrayList(values.toArray());
    }

    public static DoubleList doubleListOf(Stream<Double> values) {
        return doubleListOf(doubleStreamOf(values));
    }

    public static DoubleStream doubleStreamOf(double... values) {
        return DoubleStream.of(values);
    }

    public static DoubleStream doubleStreamOf(DoubleList values) {
        return values.doubleStream();
    }

    public static DoubleStream doubleStreamOf(Stream<Double> values) {
        return values.mapToDouble(it -> it);
    }

    public static DoubleStream doubleStreamOf(List<Double> values) {
        if (values instanceof DoubleList doubles) {
            return doubles.doubleStream();
        } else {
            return doubleStreamOf(values.stream());
        }
    }

    public static int argMin(DoubleStream values) {
        var i = values.iterator();
        if (!i.hasNext()) {
            return -1;
        }
        double min = i.next();
        int index = 0;
        int minIndex = 0;
        while (i.hasNext()) {
            double value = i.next();
            index++;
            if (value < min) {
                min = value;
                minIndex = index;
            }
        }
        return minIndex;
    }

    public static int argMin(DoubleList values) {
        return argMin(values.doubleStream());
    }

    public static int argMin(List<Double> values) {
        return argMin(doubleStreamOf(values));
    }

    public static int argMin(Stream<Double> values) {
        return argMin(doubleStreamOf(values));
    }

    public static int argMin(double... values) {
        return argMin(doubleStreamOf(values));
    }
}
