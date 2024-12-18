package c2c.bottomup;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class OutputFileWriter {

	public void write(List<ChangesTimeLine> segmentedTimeLineWithMetricsFilled, String outputFolder, double startYear,
			double endYear) throws IOException {

		File folder = new File(outputFolder);
		if (!folder.exists()) {
			folder.mkdir();
		}

		for (int i = 0; i < segmentedTimeLineWithMetricsFilled.size(); i++) {

			String s = String.format("%03d", i);

			File file = new File(outputFolder + "/" + s + ".csv");

			FileWriter writer = new FileWriter(file.getAbsoluteFile());

			ChangesTimeLine t = segmentedTimeLineWithMetricsFilled.get(i);

			writer.write("year,");

			for (int j = (int) startYear; j <= endYear; j++) {

				writer.write(j + ",");

			}

			writer.write("\n" + "index,");

			for (int j = 0; j < t.getSize(); j++) {

				Changes change = t.getChange(j);
				writer.write(String.valueOf(change.getValue()) + ",");

			}

			writer.write("\n" + "magnitude,");

			for (int j = 0; j < t.getSize(); j++) {

				Changes change = t.getChange(j);
				writer.write(String.valueOf(change.getMagnitude()) + ",");

			}

			writer.write("\n" + "duration,");

			for (int j = 0; j < t.getSize(); j++) {

				Changes change = t.getChange(j);
				writer.write(String.valueOf(change.getDuration()) + ",");

			}

			writer.write("\n" + "rate,");

			for (int j = 0; j < t.getSize(); j++) {

				Changes change = t.getChange(j);
				writer.write(String.valueOf(change.getRate()) + ",");

			}

			writer.write("\n" + "postMagnitude,");

			for (int j = 0; j < t.getSize(); j++) {

				Changes change = t.getChange(j);
				writer.write(String.valueOf(change.getPostMagnitude()) + ",");

			}

			writer.write("\n" + "postDuration,");

			for (int j = 0; j < t.getSize(); j++) {

				Changes change = t.getChange(j);
				writer.write(String.valueOf(change.getPostDuration()) + ",");

			}

			writer.write("\n" + "postRate,");

			for (int j = 0; j < t.getSize(); j++) {

				Changes change = t.getChange(j);
				writer.write(String.valueOf(change.getPostRate()) + ",");

			}

			writer.close();

		}

	}
}
