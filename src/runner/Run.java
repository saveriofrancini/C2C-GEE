package runner;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import functions.InputFileReader;
import functions.Main;
import functions.Main.Args;
import functions.OutputFileWriter;
import objects.ChangesTimeLine;
import objects.TimeLine;

public class Run {

	public static void main(String[] args) throws IOException {

		Args arguments = new Args();

		String fileToBeRead = "./data/input.csv";
		String outputFolder = "./data/output";

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