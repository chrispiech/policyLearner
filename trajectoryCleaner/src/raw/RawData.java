package raw;

import java.io.File;
import java.util.List;
import java.util.Map;

import util.FileSystem;
import models.ast.Tree;

public class RawData {
	
	private List<String> activities = null;
	private Map<String, Tree> xmlAstMap = null;

	public RawData() {
		System.out.println("load dumps...");
		activities = loadActivities();
		xmlAstMap = RawSources.getXmlIdProgramMap();
	}
	
	public List<String> getActivities() {
		return activities;
	}
	
	public Map<String, Tree> getXmlAstMap() {
		return xmlAstMap;
	}
	
	private List<String> loadActivities() {
		File dumpDir = new File(FileSystem.getAssnDir(), "dumps");
		File sourceDumpFile = new File(dumpDir, "activitiesDump.csv");
		return FileSystem.getFileLines(sourceDumpFile);
	}
	
}
