package raw;

import java.io.File;
import java.util.List;
import java.util.Map;

import util.FileSystem;
import util.Warnings;
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
		File sourceDumpFile = new File(dumpDir, "activities.csv");
		List<String> lines = FileSystem.getFileLines(sourceDumpFile);
		
		return lines.subList(1, lines.size());
	}
	
	public static String replaceUnquote(String xml) {
		return xml.replace("\\\"","\"");
	}

	public static String stripQuotes(String string) {
		char first = string.charAt(0);
		char last = string.charAt(string.length() - 1);
		Warnings.check(first == '"', "not in quotes");
		if(last != '"') return null;
		return string.substring(1, string.length() - 1);
	}
	
}
