package c2c.bottomup;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class InputFileReader {

	public ArrayList<TimeLine> read(String fileName) throws IOException {

		ArrayList<TimeLine> timeLines = new ArrayList<TimeLine>();

		FileInputStream fis = new FileInputStream(fileName);
		Scanner scanner = new Scanner(fis);

		ArrayList<Double> values = new ArrayList<Double>();
		ArrayList<Double> date = new ArrayList<Double>();

		ArrayList<Double> currentDate = new ArrayList<Double>();

		int k = 0;
		while (scanner.hasNext()) {
			int start = 1;
			String row = scanner.nextLine();
			if (row.startsWith("\"")) {
				String[] rowSplit = row.split("\"");
				row = rowSplit[2];
				start = 0;
			}
			String[] cols = row.split(",");
			if (start == 1) {
			}

			if (k == 0) {
				for (int i = 1; i < cols.length; i++) {
					date.add(Double.valueOf(cols[i]));
				}
				k++;
			} else {
				for (int i = start; i < cols.length; i++) {
					values.add(Double.valueOf(cols[i]));
					currentDate.add(date.get(i - 1));
				}

				TimeLine aTimeLine = new TimeLine();
				for (int n = 0; n < currentDate.size(); n++) {
					aTimeLine.addPoint(currentDate.get(n), values.get(n));
				}

				timeLines.add(aTimeLine);
				values.clear();
				currentDate.clear();
			}
		}

		fis.close();
		scanner.close();

		return timeLines;

	}

}
