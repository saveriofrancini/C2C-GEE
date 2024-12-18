package c2c.bottomup;

/**
 * Class defining "Segments" to select specific sequences of "Points" over a "TimeLine"
 */
public class Segment {
	private int start;
	private int finish;

	public Segment(int start, int finish) {
		super();
		this.start = start;
		this.finish = finish;
	}
	public int getStart() {
		return start;
	}
	public void setStart(int start) {
		this.start = start;
	}
	public int getFinish() {
		return finish;
	}
	public void setFinish(int finish) {
		this.finish = finish;
	}
}
