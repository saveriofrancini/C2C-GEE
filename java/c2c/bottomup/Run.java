package c2c.bottomup;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class Run {

	public static void main(String[] args) throws IOException {

                Main.Args arguments = new Main.Args();

		String fileToBeRead = "java/c2c/bottomup/testdata/input.csv";
		String outputFolder = "java/c2c/bottomup/testdata/output";

		InputFileReader inputFileReader;
		Main main;
		OutputFileWriter outputFileWriter;

		inputFileReader = new InputFileReader();
		main = new Main();
		outputFileWriter = new OutputFileWriter();
//		read input file
		List<TimeLine> timeLines = inputFileReader.read(fileToBeRead);

//		Apply the Main function on each timeLine within timeLines
		List<ChangesTimeLine> segmentedTimeLineWithMetricsFilled = timeLines.stream()
				.map(s -> main.c2cBottomUp(s, arguments)).collect(Collectors.toList());

//		write each ChangesTimeLine in segmentedTimeLineWithMetricsFilled
		outputFileWriter.write(segmentedTimeLineWithMetricsFilled, outputFolder, arguments.startYear,
				arguments.endYear);
	}

}
