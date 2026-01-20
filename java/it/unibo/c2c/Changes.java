package it.unibo.c2c;

/** Just a POD to record a change segment. */
public record Changes(
    int dateIndex,
    double date,
    double value,
    double magnitude,
    double duration,
    double postMagnitude,
    double postDuration,
    double rate,
    double postRate,
    double indexRegrowth,
    double recoveryIndicator,
    double regrowth60,
    double regrowth80,
    double regrowth100) {
  public static final RegrowthMetric EMPTY_REGROWTH =
      new RegrowthMetric(Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN);
  public static final Changes EMPTY =
      Changes.create(
          -1, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, EMPTY_REGROWTH);

  public static Changes create(
      int dateIndex,
      double date,
      double value,
      double magnitude,
      double duration,
      double postMagnitude,
      double postDuration,
      RegrowthMetric regrowthMetric) {
    return new Changes(
        dateIndex,
        date,
        value,
        magnitude,
        duration,
        postMagnitude,
        postDuration,
        /* rate= */ magnitude / duration,
        /* postRate= */ postMagnitude / postDuration,
        regrowthMetric.indexRegrowth,
        regrowthMetric.recoveryIndicator,
        regrowthMetric.regrowth60,
        regrowthMetric.regrowth80,
        regrowthMetric.regrowth100);
  }

  record RegrowthMetric(
      double indexRegrowth,
      double recoveryIndicator,
      double regrowth60,
      double regrowth80,
      double regrowth100) {}
}
