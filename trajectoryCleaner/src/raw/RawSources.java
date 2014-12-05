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
import util.Warnings;

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
			String xml = RawData.stripQuotes(cols[3]);
			xml = RawData.replaceUnquote(xml);

			String sourceId = cols[0];
			Program p = ProgramLoader.programFromXml(sourceId, xml, 100);

			if(p != null) {
				Tree t = p.getAst();
				if(BlocklyHelper.isContiguous(p) && !BlocklyHelper.hasIllegalBlocks(t)) {
					programMap.put(sourceId, p.getAst());
				}
			} else {
				corrupted++;
			}
			if(++done % 100 == 0) System.out.println(done);
		}
		System.out.println("corrupted: " + corrupted);
		return programMap;
	}

	private static List<String> loadDump() {
		File dumpDir = new File(FileSystem.getAssnDir(), "dumps");
		File sourceDumpFile = new File(dumpDir, "sources.csv");

		List<String> lines = FileSystem.getFileLines(sourceDumpFile);
		return lines;
	}

}
