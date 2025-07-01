package it.unibo.c2c;

/** Just a POD to record a change segment. */
public record Changes(
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

  public static Changes create(
      double date,
      double value,
      double magnitude,
      double duration,
      double postMagnitude,
      double postDuration,
      RegrowthMetric regrowthMetric) {
    return new Changes(
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
