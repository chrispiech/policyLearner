package run;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import minions.BlocklyHelper;
import minions.ProgramLoader;
import minions.ProgramSaver;
import models.ast.Program;
import util.FileSystem;

public class ExpandLevelSources {

	public static void main(String[] args) {
		new ExpandLevelSources().run();
	}

	private void run() {
		System.out.println("Hello world.");
		FileSystem.setAssnId("hoc4");
		System.out.println(FileSystem.getAssnDir());

		File dumpDir = new File(FileSystem.getAssnDir(), "dumps");
		File sourceDumpFile = new File(dumpDir, "sourcesDump.csv");

		System.out.println(sourceDumpFile);

		System.out.println("loading...");
		List<String> lines = FileSystem.getFileLines(sourceDumpFile);
		System.out.println("done loading.");

		int todo = lines.size();
		int done = 0;
		int corrupted = 0;

		System.out.println("todo: " + todo);
		Map<Program, List<Program>> programMap = new HashMap<Program, List<Program>>();


		for(String line : lines) {
			String[] cols = line.split(",");
			String xml = stripQuotes(cols[3]);
			xml = replaceUnquote(xml);

			String sourceId = stripQuotes(cols[0]);
			Program p = ProgramLoader.programFromXml(sourceId, xml, 100);
			
			if(p != null && BlocklyHelper.isContiguous(p)) {
				if(!programMap.containsKey(p)) {
					programMap.put(p, new ArrayList<Program>());
				} 
				programMap.get(p).add(p);
			} else if(p == null) {
				corrupted++;
			}
			if(++done % 100 == 0) System.out.println(done);
		}

		System.out.println("All done!");
		System.out.println("corrupted: " + corrupted);

		ProgramSaver.saveUniques(programMap, "uniques");

	}

	private String replaceUnquote(String xml) {
		return xml.replace("\\\"","\"");
	}

	private String stripQuotes(String string) {
		return string.substring(1, string.length() - 1);
	}

}
