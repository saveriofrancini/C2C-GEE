package c2c.bottomup;

import java.util.ArrayList;

/**
 * This segmentation algorithm consist in a modification of the bottom up algorithm
 * (https://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.23.6570&rep=rep1&type=pdf)
 * proposed by Hermosilla et al. (2015)
 */
public class Segmentator {

	public static double calculateError(TimeLine timeline, int start, int finish) {

//		linearInterpolation
		double firstValue = timeline.getPoint(start).getValue();
		double lastValue = timeline.getPoint(finish).getValue();
		double firstDate = timeline.getPoint(start).getDate();
		double lastDate = timeline.getPoint(finish).getDate();
		double timeWindow = lastDate - firstDate;

		double error = 0;

		for (int i = start; i <= finish; i++) {
			double xFraction = (timeline.getPoint(i).getDate() - firstDate) / timeWindow;
			double interpolated = lerp(firstValue, lastValue, xFraction);
			double diff = timeline.getPoint(i).getValue() - interpolated;
			error += diff * diff;
		}

		return Math.sqrt(error / (finish - start));
	}

	public static Changes changeMetricsCalculator(TimeLine T, int leftPointIndex, int centralPointIndex,
			int rightPointIndex) {

		Points p = new Points(T.getPoint(centralPointIndex));
		Changes changePoint;
		double date = p.getDate();
		double value = p.getValue();
		double noData = Double.NaN;

		if (centralPointIndex == 0) {
			Points pNext = new Points(T.getPoint(rightPointIndex));
			double postMagnitude = pNext.getValue() - p.getValue();
			double postDuration = pNext.getDate() - p.getDate();
			changePoint = new Changes(date, value, noData, noData, postMagnitude, postDuration);
		} else if (centralPointIndex == T.getSize() - 1) {
			Points pPre = new Points(T.getPoint(leftPointIndex));
			double magnitude = p.getValue() - pPre.getValue();
			double duration = p.getDate() - pPre.getDate();
			changePoint = new Changes(date, value, magnitude, duration, noData, noData);
		} else {
			Points pPre = new Points(T.getPoint(leftPointIndex));
			Points pNext = new Points(T.getPoint(rightPointIndex));
			double magnitude = p.getValue() - pPre.getValue();
			double duration = p.getDate() - pPre.getDate();
			double postMagnitude = pNext.getValue() - p.getValue();
			double postDuration = pNext.getDate() - p.getDate();
			changePoint = new Changes(date, value, magnitude, duration, postMagnitude, postDuration);
		}

		return changePoint;

	}

	public static double lerp(double y1, double y2, double x) {
		return y1 * (1 - x) + y2 * x;
	}

	private static int minCost(ArrayList<Double> mergeCost) {
		double min = mergeCost.get(0);
		int pos = 0;
		for (int i = 0; i < mergeCost.size(); i++) {
			if (min > mergeCost.get(i)) {
				min = mergeCost.get(i);
				pos = i;
			}
		}
		return pos;
	}

	public static ChangesTimeLine segmentationAlgorithm(TimeLine T, double maxError, int maxSegm) {

//		We used ArrayList for segments and mergeCost because we have to set and remove values
//		at specific positions which requires to define additional functions
//		using arrays in the form of double[]
		ArrayList<Segment> segments = new ArrayList<Segment>();
		ArrayList<Double> mergeCost = new ArrayList<Double>();

//		initial segments
		for (int i = 0; i < T.getSize() - 1; i++) {
			Segment segment = new Segment(i, i + 1);
			segments.add(segment);
		}

//		merging cost of initial segments
		for (int i = 0; i < segments.size() - 1; i++) {
			Segment left = segments.get(i);
			Segment right = segments.get(i + 1);
			double cost = calculateError(T, left.getStart(), right.getFinish());
			mergeCost.add(cost);
		}

//		minimum merging cost
		int index = 0;
		index = minCost(mergeCost);
		double min = mergeCost.get(index);

		while (min < maxError | segments.size() > maxSegm) {
//			merge the adjacent segments with the smaller cost
			Segment segment = segments.get(index);
			Segment segment2 = segments.get(index + 1);
			segment.setFinish(segment2.getFinish());
//			update segments
			segments.remove(index + 1);
			mergeCost.remove(index);

			if (index + 1 < segments.size()) {
				Segment left = segments.get(index);
				Segment right = segments.get(index + 1);
				double cost = calculateError(T, left.getStart(), right.getFinish());
				mergeCost.set(index, cost);
			}
			if (index - 1 >= 0) {
				Segment left = segments.get(index - 1);
				Segment right = segments.get(index);
				double cost = calculateError(T, left.getStart(), right.getFinish());
				mergeCost.set(index - 1, cost);
			}
			if (mergeCost.size() == 0) {
				break;
			}

			index = minCost(mergeCost);
			min = mergeCost.get(index);
		}

		ChangesTimeLine segmented = new ChangesTimeLine();

		int leftPointIndex = 999;
		int centralPointIndex = 999;
		int rightPointIndex = 999;

		for (int i = 0; i < segments.size(); i++) {

			centralPointIndex = segments.get(i).getStart();

			if (i == 0) {
				rightPointIndex = segments.get(i).getFinish();
			} else {
				leftPointIndex = segments.get(i - 1).getStart();
				rightPointIndex = segments.get(i).getFinish();
			}

			Changes c = changeMetricsCalculator(T, leftPointIndex, centralPointIndex, rightPointIndex);
			segmented.addChange(c);

		}

//		add last change
		centralPointIndex = segments.get(segments.size() - 1).getFinish();
		leftPointIndex = segments.get(segments.size() - 1).getStart();
		Changes c = changeMetricsCalculator(T, leftPointIndex, centralPointIndex, rightPointIndex);
		segmented.addChange(c);

		return segmented;
	}

}
