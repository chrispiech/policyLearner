package run.vis;

import java.io.File;
import java.util.List;

import util.FileSystem;

public class SimplifyLogCsv {

	private static final String ROOT = "annotationImpact18";
	private static final String IN_FILE_NAME = ROOT + ".csv";
	private static final String OUT_FILE_NAME = ROOT + "_small.csv";

	public static void main(String[] args) {
		new SimplifyLogCsv().run();
	}

	private void run() {
		File resultsDir = new File("results");
		File csv = new File(resultsDir, IN_FILE_NAME);
		System.out.println(csv.getAbsolutePath());
		
		List<String> lines = FileSystem.getFileLines(csv);
		String header = lines.get(0);
		System.out.println("Skipping header:");
		System.out.println(header);
		
		double logPreviousFirst = -1;
		
		String outCsvTxt = header + "\n";
		
		for(int i = 1; i < lines.size(); i++) {
			String[] cols = lines.get(i).split(",");
			double first = Double.parseDouble(cols[0]);
			double second = Double.parseDouble(cols[1]);
			
			double logFirst = Math.log(first);
			
			double diff = logFirst - logPreviousFirst;
			if(diff > 0.01) {
				outCsvTxt += first + "," + second + "\n";
				logPreviousFirst = logFirst;
			}
		}
		
		FileSystem.createFile(resultsDir, OUT_FILE_NAME, outCsvTxt);
		
	}
	
}
