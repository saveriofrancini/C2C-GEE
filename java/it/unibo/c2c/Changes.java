package it.unibo.c2c;

/** Just a POD to record a change segment. */
public class Changes {
  double date;
  double value;
  double magnitude;
  double duration;
  double postMagnitude;
  double postDuration;
  double rate;
  double postRate;

  public Changes(double date, double value, double magnitude, double duration,
      double postMagnitude, double postDuration) {
    this.date = date;
    this.value = value;
    this.magnitude = magnitude;
    this.duration = duration;
    this.postMagnitude = postMagnitude;
    this.postDuration = postDuration;
    this.rate = this.magnitude / this.duration;
    this.postRate = this.postMagnitude / this.postDuration;
  }
}
