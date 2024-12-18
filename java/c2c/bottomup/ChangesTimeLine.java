package c2c.bottomup;

import java.util.ArrayList;

/**
 * Class defining "ChangesTimeLine", i.e. time series of "Changes".
 */
public class ChangesTimeLine {

	private ArrayList<Changes> changesTimeLine;

	public ChangesTimeLine() {
		changesTimeLine = new ArrayList<Changes>();
	}

	public void addChange(Changes change) {
		changesTimeLine.add(change);
	}

	public void addChange(double date, double value, double magnitude, double duration, double postMagnitude,
			double postDuration) {
		Changes change = new Changes(date, value, magnitude, duration, postMagnitude, postDuration);
		changesTimeLine.add(change);
	}

	public Changes getChange(int index) {
		return changesTimeLine.get(index);
	}

	public int getSize() {
		return changesTimeLine.size();
	}

	public boolean hasChange(double date) {
		for (int i = 0; i < changesTimeLine.size(); i++) {
			if (changesTimeLine.get(i).getDate() == date) {
				return true;
			}
		}
		return false;
	}

}
