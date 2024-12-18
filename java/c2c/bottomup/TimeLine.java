package c2c.bottomup;

import java.util.ArrayList;

/**
 * Class defining "TimeLine" (time series) of "Points".
 */
public class TimeLine {

	private ArrayList<Points> timeLine;

	public TimeLine() {
		timeLine = new ArrayList<Points>();
	}

	public void addPoint(double date, double value) {
		Points point = new Points(date, value);
		timeLine.add(point);
	}

	public void addPoint(Points point) {
		timeLine.add(point);
	}

	public TimeLine append(TimeLine timeLineToAppend) {
		ArrayList<Points> pointsToAppend = timeLineToAppend.timeLine;
		for (int i = 0; i < pointsToAppend.size(); i++) {
			timeLine.add(pointsToAppend.get(i));
		}
		return this;
	}

	public TimeLine createSegment(int start, int finish) {
		TimeLine segment = new TimeLine();
		for (int i = start; i <= finish; i++) {
			segment.addPoint(new Points(timeLine.get(i)));
		}
		return null;
	}

	public Points getPoint(int index) {
		return timeLine.get(index);
	}

	public int getSize() {
		return timeLine.size();
	}

	public boolean hasPoint(double date) {
		for (int i = 0; i < timeLine.size(); i++) {
			if (timeLine.get(i).getDate() == date) {
				return true;
			}
		}
		return false;
	}

	public void setInPosition(double date, double value, int index) {
		Points aPoint = new Points(date, value);
		timeLine.set(index, aPoint);
	}

	public void setInPosition(double value, int index) {

		Points aPoint = new Points(timeLine.get(index));
		aPoint.setValue(value);
		timeLine.set(index, aPoint);

	}

}
