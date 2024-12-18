/*
 * This function allows the user to select desired years for the output
 * An additional advantage of using this function is that the output can be an image collection
 * instead of an image array
 */

package functions;

import objects.Changes;
import objects.ChangesTimeLine;

public class FilterAndFillChangesTimeLine {

	public static ChangesTimeLine filterTimeLine(ChangesTimeLine changesTL, int startYear, int endYear) {

		ChangesTimeLine filteredTL = new ChangesTimeLine();

		double noData = Double.NaN;

		for (int yearI = startYear; yearI <= endYear; yearI++) {

			boolean gotIt = false;

			for (int i = 0; i < changesTL.getSize(); i++) {
				if (changesTL.getChange(i).getDate() == yearI) {
					filteredTL.addChange(changesTL.getChange(i));
					gotIt = true;
					break;
				}
			}

			if (gotIt)
				continue;

			Changes missingChange = new Changes(yearI, noData, noData, noData, noData, noData);
			filteredTL.addChange(missingChange);

		}

		return filteredTL;

	}

}
