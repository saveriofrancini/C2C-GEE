package c2c.bottomup;

public class Despiker {

	public static TimeLine despikeTimeLine(TimeLine T, double spikesTolerance) {

		TimeLine inputT = T;

		for (int i = 1; i < T.getSize() - 1; i++) {
			double leftValue = inputT.getPoint(i - 1).getValue();
			double centerValue = inputT.getPoint(i).getValue();
			double rightValue = inputT.getPoint(i + 1).getValue();
			double ceneterValueFitted = (leftValue + rightValue) / 2;
			double leftRightDelta = Math.abs(leftValue - rightValue);
			double spikeValue = Math.abs(ceneterValueFitted - centerValue);
			double despikeProportion = leftRightDelta / spikeValue;
//			despike conditions
//			#1# The value of the spike is greater than 100
//			#2# The difference between spectral values on either side of the spike
//			is less than 1-despike desawtooth proportion of the spike itself" (Landtrendr)
			if (spikeValue > 100 && despikeProportion < (1 - spikesTolerance)) {
				double leftValueOfT = T.getPoint(i - 1).getValue();
				double rightValueOfT = T.getPoint(i + 1).getValue();
				double ceneterValueFittedOfT = (leftValueOfT + rightValueOfT) / 2;
				T.setInPosition(ceneterValueFittedOfT, i);
			}
		}
		return T;
	}

}
