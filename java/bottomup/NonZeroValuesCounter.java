package functions;

import objects.TimeLine;

public class NonZeroValuesCounter {
	public static int countNonZeroValues(TimeLine T) {
		double timeLineValues[] = getNonZeroValuesInTimeLine(T);
		return timeLineValues.length;
	}

	static double[] getNonZeroValuesInTimeLine(TimeLine timeline) {
		double nonZeroValues[] = new double[timeline.getSize()];
		for (int i = 0; i < timeline.getSize(); i++) {
			if (timeline.getPoint(i).getValue() != 0)
				nonZeroValues[i] = timeline.getPoint(i).getValue();
		}
		return nonZeroValues;
	}
}
