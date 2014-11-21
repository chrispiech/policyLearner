package expansion;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import minions.TreeJasonizer;
import models.ast.Tree;
import raw.RawActivities;
import raw.RawData;
import util.FileSystem;
import util.IdCounter;
import util.MapSorter;

public class AstExpander {

	/* raw data */
	RawData raw = null;

	/* processed data */
	private List<Tree> sortedAsts = null;
	private Map<String, String> xmlAstIdMap = null;
	private Map<Tree, String> astIdMap = null;
	
	public AstExpander(RawData data) {
		this.raw = data;
	}
	
	public void expandAsts() {
		Map<Tree, Integer> astCounts = getAstCounts();
		sortAsts(astCounts);
		saveAsts(astCounts);
		saveAstCounts(astCounts);
		makeXmlAstIdMap();
		saveUnitTests();
	}

	public Map<String, String> getXmlAstIdMap() {
		return xmlAstIdMap;
	}

	private void sortAsts(Map<Tree, Integer> astCounts) {
		System.out.println("sort asts...");
		MapSorter<Tree> sorter = new MapSorter<Tree>();
		sortedAsts = sorter.sortInt(astCounts);
		astIdMap = new HashMap<Tree, String>();
		IdCounter counter = new IdCounter();
		for(Tree t : sortedAsts) {
			astIdMap.put(t, counter.getNextIdStr());
		}
	}

	private void makeXmlAstIdMap() {
		System.out.println("make xml ast map...");
		xmlAstIdMap = new HashMap<String, String>();
		for(String xmlId : raw.getXmlAstMap().keySet()) {
			Tree t = raw.getXmlAstMap().get(xmlId);
			if(t == null) continue;
			String astId = astIdMap.get(t);
			xmlAstIdMap.put(xmlId, astId);
		}
		System.out.println("save xmlId,astId Map...");
		String xmlAstStr = "";
		for(String xmlId : xmlAstIdMap.keySet()) {
			String astId = xmlAstIdMap.get(xmlId);
			xmlAstStr += xmlId + "," + astId + "\n";
		}
		File astDir = getAstDir();
		FileSystem.createFile(astDir, "idMap.txt", xmlAstStr);
	}
	
	private void saveAsts(Map<Tree, Integer> astCounts) {
		System.out.println("saving programs...");
		for(Tree ast : sortedAsts) {
			String id = astIdMap.get(ast);
			saveAst(ast, id);
		}
	}
	
	private void saveAstCounts(Map<Tree, Integer> astCounts) {
		System.out.println("save ast counts...");
		String countsStr = "";
		for(Tree ast : sortedAsts) {
			String id = astIdMap.get(ast);
			int count = astCounts.get(ast);
			countsStr += id + "\t" + count + "\n";
		}
		FileSystem.createFile(getAstDir(), "counts.txt", countsStr);
	}
	
	private void saveUnitTests() {
		System.out.println("save unit test results..");
		Map<String, Integer> unitTestResults = noteUnitTestResults();
		String unitTestStr = "";
		for(Tree t : sortedAsts) {
			String astId = astIdMap.get(t);
			int result = unitTestResults.get(astId);
			unitTestStr += astId + "\t" + result + "\n";
		}
		File astDir = getAstDir();
		FileSystem.createFile(astDir, "unitTestResults.txt", unitTestStr);
	}
	
	private Map<Tree, Integer> getAstCounts() {
		System.out.println("get ast counts..."); 
		Map<Tree, Integer> astCounts = new HashMap<Tree, Integer>();
		for(String row : raw.getActivities()) {
			String xmlId = RawActivities.getXmlId(row);
			if(raw.getXmlAstMap().containsKey(xmlId)) {
				Tree ast = raw.getXmlAstMap().get(xmlId);
				if(!astCounts.containsKey(ast)) {
					astCounts.put(ast, 0);
				}
				int newCount = astCounts.get(ast) + 1;
				astCounts.put(ast, newCount);
			}
		}
		return astCounts;
	}
	
	private Map<String, Integer> noteUnitTestResults() {
		Map<String, Integer> unitTestResults = new HashMap<String, Integer>();
		for(String row : raw.getActivities()) {
			String xmlId = RawActivities.getXmlId(row);
			String astId = xmlAstIdMap.get(xmlId);
			int unitTest = RawActivities.getUnitTest(row);
			unitTestResults.put(astId, unitTest);
		}
		return unitTestResults;
	}
	
	private void saveAst(Tree ast, String astId) {
		try {
			String json = TreeJasonizer.jsonify(ast).toString(4);
			FileSystem.createFile(getAstDir(), astId + ".json", json);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private File getAstDir() {
		return new File(FileSystem.getAssnDir(), "asts");
	}

	
}
