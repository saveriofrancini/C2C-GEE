package c2c.bottomup;

public class UnsegmentableTimeLineManager {
	public static ChangesTimeLine manageUnsegmentableTimeLine(TimeLine T, double startYear, double endYear) {
		ChangesTimeLine C = new ChangesTimeLine();
		int nYears = (int) (endYear - startYear);
		for (int i = 0; i <= nYears; i++) {
			double noData = Double.NaN;
			Changes changePoint = new Changes(noData, noData, noData, noData, noData, noData);
			C.addChange(changePoint);
		}
		return C;
	}
}
