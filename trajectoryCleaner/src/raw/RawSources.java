package raw;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import minions.BlocklyHelper;
import minions.ProgramLoader;
import minions.ProgramSaver;
import models.ast.Program;
import models.ast.Tree;
import util.FileSystem;

public class RawSources {

	public static Map<String, Tree> getXmlIdProgramMap() {
		List<String> lines = loadDump();
		return makeAsts(lines);
	}

	private static Map<String, Tree> makeAsts(List<String> lines) {
		int done = 0;
		int corrupted = 0;

		Map<String, Tree> programMap = new HashMap<String, Tree>();
		for(String line : lines) {
			String[] cols = line.split(",");
			String xml = stripQuotes(cols[3]);
			xml = replaceUnquote(xml);

			String sourceId = stripQuotes(cols[0]);
			Program p = ProgramLoader.programFromXml(sourceId, xml, 100);

			if(p != null && BlocklyHelper.isContiguous(p)) {
				programMap.put(sourceId, p.getAst());
			} else if(p == null) {
				corrupted++;
			}
			if(++done % 100 == 0) System.out.println(done);
		}
		System.out.println("corrupted: " + corrupted);
		return programMap;
	}

	private static List<String> loadDump() {
		File dumpDir = new File(FileSystem.getAssnDir(), "dumps");
		File sourceDumpFile = new File(dumpDir, "sourcesDump.csv");

		List<String> lines = FileSystem.getFileLines(sourceDumpFile);
		return lines;
	}

	private static String replaceUnquote(String xml) {
		return xml.replace("\\\"","\"");
	}

	private static String stripQuotes(String string) {
		return string.substring(1, string.length() - 1);
	}

}