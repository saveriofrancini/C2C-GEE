package it.unibo.c2c;

/** Just a POD to record a change segment. */
public class Changes {
  
  public static final RegrowthMetric EMPTY_REGROWTH = 
      new RegrowthMetric(Double.NaN, Double.NaN, Double.NaN, Double.NaN);

  double date;
  double value;
  double magnitude;
  double duration;
  double postMagnitude;
  double postDuration;
  double rate;
  double postRate;
  double recoveryIndicator;
  double regrowth60;
  double regrowth80;
  double regrowth100;

  public Changes(double date, double value, double magnitude, double duration,
      double postMagnitude, double postDuration, RegrowthMetric regrowthMetric) {
    this.date = date;
    this.value = value;
    this.magnitude = magnitude;
    this.duration = duration;
    this.postMagnitude = postMagnitude;
    this.postDuration = postDuration;
    this.rate = this.magnitude / this.duration;
    this.postRate = this.postMagnitude / this.postDuration;
    this.recoveryIndicator = regrowthMetric.recoveryIndicator;
    this.regrowth60 = regrowthMetric.regrowth60;
    this.regrowth80 = regrowthMetric.regrowth80;
    this.regrowth100 = regrowthMetric.regrowth100;
  }

  record RegrowthMetric(double recoveryIndicator, double regrowth60, double regrowth80, double regrowth100) {}
}
