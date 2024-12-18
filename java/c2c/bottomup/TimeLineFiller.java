package c2c.bottomup;

import java.util.ArrayList;

public class TimeLineFiller {

	public static TimeLine fillTimeLine(TimeLine T) {

//		adjust last value
		ArrayList<Double> timeLineValues = getTimeLineValues(T);

//		infill missing data
		for (int i = 0; i < T.getSize(); i++) {

//		    check if pointI needs to be filled
			if (timeLineValues.get(i) != 0)
				continue;

//		    select the first two valid observations in timeLine before i
			ArrayList<Double> leftNearest2ValidObs = new ArrayList<Double>();
			int leftValuesFound = 0;
			for (int z = i; z >= 0; z--) {
				if (timeLineValues.get(z) != 0) {
					leftNearest2ValidObs.add(timeLineValues.get(z));
					leftValuesFound++;
				}
				if (leftValuesFound == 2)
					break;
			}

//			select the first two valid observations in timeLine after i
			ArrayList<Double> rightNearest2ValidObs = new ArrayList<Double>();
			int rightValuesFound = 0;
			for (int z = i; z < timeLineValues.size(); z++) {
				if (timeLineValues.get(z) != 0) {
					rightNearest2ValidObs.add(timeLineValues.get(z));
					rightValuesFound++;
				}
				if (rightValuesFound == 2)
					break;
			}

			if (leftValuesFound < 2) {
//				fill with the first valid observation in timeLine
				timeLineValues.set(i, rightNearest2ValidObs.get(0));
				T.setInPosition(T.getPoint(i).getDate(), rightNearest2ValidObs.get(0), i);
			} else if (rightValuesFound < 2) {
				if (rightValuesFound == 1) {
					timeLineValues.set(i, rightNearest2ValidObs.get(0));
					T.setInPosition(T.getPoint(i).getDate(), rightNearest2ValidObs.get(0), i);
				} else {
//				fill with the last valid observation in timeLine
					timeLineValues.set(i, leftNearest2ValidObs.get(0));
					T.setInPosition(T.getPoint(i).getDate(), leftNearest2ValidObs.get(0), i);
				}
			} else if (leftValuesFound >= 2 && rightValuesFound >= 2) {

				double leftDif = Math.abs(leftNearest2ValidObs.get(0) - leftNearest2ValidObs.get(1));
				double rightDif = Math.abs(rightNearest2ValidObs.get(0) - rightNearest2ValidObs.get(1));
//				fill using value with smaller difference
				if (leftDif < rightDif) {
					timeLineValues.set(i, leftNearest2ValidObs.get(0));
					T.setInPosition(T.getPoint(i).getDate(), leftNearest2ValidObs.get(0), i);
				} else {
					timeLineValues.set(i, rightNearest2ValidObs.get(0));
					T.setInPosition(T.getPoint(i).getDate(), rightNearest2ValidObs.get(0), i);
				}

			}

		}

		int size = T.getSize();
		double lastValue = T.getPoint(size - 1).getValue();
		double lastValueL = T.getPoint(size - 2).getValue();
		double lastValueLL = T.getPoint(size - 3).getValue();
		double lastDif = Math.abs(lastValue - lastValueL);
		double secondLastDif = Math.abs(lastValueL - lastValueLL);
		if (lastDif >= secondLastDif) {
			T.setInPosition(T.getPoint(size - 1).getDate(), lastValueL, size - 1);
		}

		return T;
	}

	static ArrayList<Double> getTimeLineValues(TimeLine timeline) {
		ArrayList<Double> timeLinesValues = new ArrayList<Double>();
		for (int i = 0; i < timeline.getSize(); i++) {
			timeLinesValues.add(timeline.getPoint(i).getValue());
		}
		return timeLinesValues;
	}

}
