package c2c.bottomup;

/**
 * Class defining "Changes".
 * "Changes" are particular kind of "Points" belonging to particular kind of "TimeLines" defined in the "ChangesTimeLine" class
 * "Changes" represents "Points" which values depend on values of subsequent and previous "Points".
 */
public class Changes {

	private double date;
	private double value;
	private double magnitude;
	private double duration;
	private double postMagnitude;
	private double postDuration;
	private double rate;
	private double postRate;

	public Changes(double date, double value, double magnitude, double duration,
			double postMagnitude, double postDuration) {
		this.date = date;
		this.value = value;
		this.magnitude = magnitude;
		this.duration = duration;
		this.postMagnitude = postMagnitude;
		this.postDuration = postDuration;
		this.rate = this.magnitude/this.duration;
		this.postRate = this.postMagnitude/this.postDuration;
	}

	public Changes(Changes change) {
		this.date = change.date;
		this.value = change.value;
		this.magnitude = change.magnitude;
		this.duration = change.duration;
		this.postMagnitude = change.postMagnitude;
		this.postDuration = change.postDuration;
	}

	public void setDate(double date) {
		this.date = date;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public void setMagnitude(double magnitude) {
		this.magnitude = magnitude;
	}

	public void setDuration(double duration) {
		this.duration = duration;
	}

	public void setPostMagnitude(double postMagnitude) {
		this.postMagnitude = postMagnitude;
	}

	public void setPostDuration(double postDuration) {
		this.postDuration = postDuration;
	}

	public void setRate(double rate) {
		this.rate = rate;
	}

	public void setPostRate(double postRate) {
		this.postRate = postRate;
	}

	public double getRate() {
		return this.rate;
	}

	public double getPostRate() {
		return this.postRate;
	}

	public double getDate() {
		return this.date;
	}

	public double getValue() {
		return this.value;
	}

	public double getMagnitude() {
		return this.magnitude;
	}

	public double getDuration() {
		return this.duration;
	}

	public double getPostMagnitude() {
		return this.postMagnitude;
	}

	public double getPostDuration() {
		return this.postDuration;
	}

}
