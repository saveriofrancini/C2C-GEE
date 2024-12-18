package c2c.bottomup;

import com.google.earthengine.api.base.ArgsBase;

public class Main {

	public static class Args extends ArgsBase {

		@Doc(help = "Maximum error (RMSE) allowed to remove points and construct segments.")
		@Optional
		public double maxError = 75;

		@Doc(help = "Maximum number of segments to be fitted on the time series.")
		@Optional
		public int maxSegments = 6;

		@Doc(help = "Year of the first image in the output image collection.")
		@Optional
		public int startYear = 1984;

		@Doc(help = "Year of the last image in the output image collection.")
		@Optional
		public int endYear = 2019;

		@Doc(help = "Whether to apply the pre-infill process.")
		@Optional
		public boolean infill = true;

		@Doc(help = "Tolerance of spikes in the time series. 1 means to don't remove spikes.")
		@Optional
		public double spikesTolerance = 0.85;

	}

//	This is the method that should be available for users on GEE
//	but:
//	(1) the input "TimeLine T" should be an image collection
//	(2) the output "ChangesTimeLine cT" should be an image collection
	public ChangesTimeLine c2cBottomUp(TimeLine T, Args args) {

		ChangesTimeLine cT;

		if (args.endYear < args.startYear) {
			throw new RuntimeException("endYear can not be smaller than startYear");
		}

		if (NonZeroValuesCounter.countNonZeroValues(T) > 2) {

			if (args.infill)
				T = TimeLineFiller.fillTimeLine(T);

			if (args.spikesTolerance < 1)
				T = Despiker.despikeTimeLine(T, args.spikesTolerance);

			cT = Segmentator.segmentationAlgorithm(T, args.maxError, args.maxSegments);

			cT = FilterAndFillChangesTimeLine.filterTimeLine(cT, args.startYear, args.endYear);

		} else {
			cT = UnsegmentableTimeLineManager.manageUnsegmentableTimeLine(T, args.startYear, args.endYear);
		}

		return cT;

	}

}
