package minions;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import util.FileSystem;
import util.IdCounter;
import models.ast.Program;
import models.ast.Tree;

public class ProgramLoader {

	public static boolean simplify = true;;

	public static List<Program> loadPrograms(String xmlDirName, int max) {
		File treeDir = FileSystem.getAssnDir();
		File xmlDir = new File(treeDir, xmlDirName);
		return loadPrograms(xmlDir, max);
	}

	public static List<Program> loadPrograms(String dirName) {
		File assnDir = FileSystem.getAssnDir();
		File programDir = new File(assnDir, dirName);
		return loadPrograms(programDir, -1);
	}


	public static List<Program> loadUniquePrograms(int max) {
		File treeDir = FileSystem.getAssnDir();
		File xmlDir = new File(treeDir, "unique");
		return loadPrograms(xmlDir, max);
	}

	public static List<Program> loadUniquePrograms() {
		return loadUniquePrograms(-1);
	}

	public static List<Program> loadPrograms(int max) {
		File treeDir = FileSystem.getAssnDir();
		File xmlDir = new File(treeDir, "asts");
		return loadPrograms(xmlDir, max);
	}

	public static List<Program> loadPrograms() {
		return loadPrograms(-1);
	}

	public static Map<String, Integer> loadCountMap() {
		File assnDir = FileSystem.getAssnDir();
		File astDir = new File(assnDir, "asts");
		return loadCountMap(astDir);
	}

	public static Map<String, String> loadIdMap() {
		File assnDir = FileSystem.getAssnDir();
		File astDir = new File(assnDir, "asts");
		return loadIdMap(astDir);
	}

	public static Map<String, Integer> loadUnitTestMap(String dir) {
		if(dir == null) return null;
		File treeDir = FileSystem.getAssnDir();
		File xmlDir = new File(treeDir, dir);
		return loadUnitTestResult(xmlDir);
	}

	public static List<Program> loadPrograms(
			String assnId,
			String[] toLoad) {
		File dataDir = FileSystem.getDataDir();
		File treeDir = new File(dataDir, assnId);
		File xmlDir = new File(treeDir, "xml");

		Map<String, Integer> countMap = loadCountMap(xmlDir);
		Map<String, Integer> unitTestResult = loadUnitTestResult(xmlDir);

		List<Program> programs = new ArrayList<Program>();
		for(String name : toLoad) {
			File programFile = new File(xmlDir, name);
			Program p = loadXmlProgram(programFile, countMap, unitTestResult);
			programs.add(p);
		}
		return programs;
	}

	private static Map<String, Integer> loadUnitTestResult(File dir) {
		File unitTestFile = new File(dir, "unitTestResults.txt");
		if(!unitTestFile.exists()) return null;
		return FileSystem.getFileMap(unitTestFile);
	}

	public static Map<String, Integer> loadCountMap(File dir) {
		File countFile = new File(dir, "counts.txt");
		if(!countFile.exists()) return null;
		return FileSystem.getFileMap(countFile);
	}

	private static Map<String, String> loadIdMap(File dir) {
		File idMapFile = new File(dir, "idMap.txt");
		if(!idMapFile.exists()) return null;
		return FileSystem.getFileMapString(idMapFile);
	}




	private static List<Program> loadPrograms(File programDir, int max) {
		List<Program> programs = new ArrayList<Program>();
		List<File> xmlDirFiles = FileSystem.listNumericalFiles(programDir);
		if(xmlDirFiles.size() == 0) {
			xmlDirFiles = FileSystem.listFiles(programDir);
		}

		Map<String, Integer> countMap = loadCountMap(programDir);
		Map<String, Integer> unitTestMap = loadUnitTestResult(programDir);

		for(File f : xmlDirFiles) {
			String ext = FileSystem.getExtension(f.getName());
			if(ext.equals("xml")) {
				Program p = loadXmlProgram(f, countMap, unitTestMap);
				if(p != null) {
					programs.add(p);
				}
			}
			if(ext.equals("json")) {
				Program p = loadJsonProgram(f, countMap, unitTestMap);
				if(p != null) {
					programs.add(p);
				}
			}
			int numLoaded = programs.size();
			if(numLoaded % 100 == 0) {
				System.out.println("loaded: " + numLoaded);
			}
			if(max > 0 && numLoaded >= max) {
				break;
			}
		}

		return programs;
	}

	//--------------------------
	// LOAD JSON PROGRAMS
	//---------------------------
	private static Program loadJsonProgram(File f, Map<String, Integer> countMap, Map<String, Integer> unitTestMap) {
		String programId = FileSystem.getNameWithoutExtension(f.getName());
		String jsonString = FileSystem.getFileContents(f);
		JSONObject json = new JSONObject(jsonString);
		ProgramLoader loader = new ProgramLoader(programId);
		Tree blocklyTree = loader.loadJsonTree(json);
		if(simplify) {
			blocklyTree = BlocklyHelper.simplify(blocklyTree);
		}
		int count = getProgramCount(countMap, programId);
		int unitTestResult = getUnitTestResult(unitTestMap, programId);
		return new Program(programId, jsonString, blocklyTree, count, unitTestResult);
	}

	//--------------------------
	// LOAD XML PROGRAMS
	//---------------------------

	public static Program programFromXml(String programId, String xml, int unitTestResult) {
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(xml));
			Document doc = dBuilder.parse(is);
			doc.getDocumentElement().normalize();
			Node e = doc.getDocumentElement();
			ProgramLoader loader = new ProgramLoader(programId);
			Tree t = loader.loadXmlTree(e);
			return new Program(programId, xml, t, 1, unitTestResult);
		} catch (SAXException e1) {
			System.out.println(e1);
			System.out.println("Failed to parse: " + programId);
		} catch (IOException e1) {
			System.out.println(e1);
			System.out.println("Failed to parse: " + programId);
		} catch (ParserConfigurationException e1) {
			System.out.println(e1);
			System.out.println("Failed to parse: " + programId);
		}
		return null;
	}

	private static Program loadXmlProgram(File f, Map<String, Integer> countMap, Map<String, Integer> unitTestMap) {
		String programId = FileSystem.getNameWithoutExtension(f.getName());
		String xml = FileSystem.getFileContents(f);
		try {


			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(f);
			doc.getDocumentElement().normalize();
			Node e = doc.getDocumentElement();
			ProgramLoader loader = new ProgramLoader(programId);
			Tree t = loader.loadXmlTree(e);
			int count = getProgramCount(countMap, programId);
			int result = getUnitTestResult(unitTestMap, programId);
			return new Program(programId, xml, t, count, result);
		} catch (SAXException e1) {
			System.out.println("Failed to parse: " + programId);
		} catch (IOException e1) {
			System.out.println("Failed to parse: " + programId);
		} catch (ParserConfigurationException e1) {
			System.out.println("Failed to parse: " + programId);
		}
		return null;
	}

	private static int getProgramCount(Map<String, Integer> countMap,
			String programId) {
		int count = 0;
		if(countMap == null) return 0;
		if(countMap.containsKey(programId)) {
			count = countMap.get(programId);
		}
		return count;
	}

	private static int getUnitTestResult(Map<String, Integer> unitTestMap,
			String programId) {
		int result = -1;
		if(unitTestMap == null) return 0;
		if(unitTestMap.containsKey(programId)) {
			result = unitTestMap.get(programId);
		}
		return result;
	}

	//--------------------------
	// PROGRAM LOADER OBJECT
	//---------------------------

	private IdCounter idCounter = new IdCounter();
	private String programId;

	public ProgramLoader(String programId) {
		this.programId = programId;
	}

	private Tree loadJsonTree(JSONObject json) {
		String type = json.getString("type");
		String name = "";
		if(json.has("name")) {
			name = json.getString("name");
		}

		List<Tree> children = new ArrayList<Tree>();
		if(json.has("children")) {
			JSONArray childrenJson = json.getJSONArray("children");
			for(int i = 0; i < childrenJson.length(); i++) {
				JSONObject childJson = childrenJson.getJSONObject(i);
				Tree child = loadJsonTree(childJson);
				children.add(child);
			}
		}

		String nodeId = "";
		if(json.has("id")) {
			nodeId = json.getString("id");
		} else {
			throw new RuntimeException("should not be true");
			//nodeId = getNextId();
		}
		return new Tree(type, name, children, nodeId);
	}

	private Tree loadXmlTree(Node xmlNode) {
		if(!isTree(xmlNode)) return null;
		String type = getTreeType(xmlNode);
		String name = getTreeName(xmlNode);

		// Get children
		List<Tree> treeChildren = new ArrayList<Tree>();
		NodeList xmlChildren = xmlNode.getChildNodes();
		for(int i = 0; i < xmlChildren.getLength(); i++) {
			Node xmlChild = xmlChildren.item(i);
			List<Node> flattened = flatten(xmlChild);
			if(flattened.size() == 1) {
				// add regular tree
				Tree treeChild = loadXmlTree(xmlChild);
				if(treeChild != null) {
					treeChildren.add(treeChild);
				}
			} else {
				// add statement list
				Tree treeChild = loadStatementList(flattened);
				treeChildren.add(treeChild);
			}
		}

		String nodeId = getNextId();
		return new Tree(type, name, treeChildren, nodeId);
	}

	private String getNextId() {
		return "" + idCounter.getNextId();
	}

	private Tree loadStatementList(List<Node> nodes) {
		String type = "statementList";
		List<Tree> children = new ArrayList<Tree>();
		for(Node n : nodes) {
			Tree child = loadXmlTree(n);
			if(child != null) {
				children.add(child);
			}
		}
		String nodeId = getNextId();
		return new Tree(type, "", children, nodeId);
	}

	private String getTreeName(Node xmlNode) {
		return "";
	}

	private boolean isTree(Node n) {
		if(isXmlType(n, "#text")) return false;
		if(isXmlType(n, "next")) return false;
		if(isXmlType(n, "comment")) return false;
		return true;
	}

	private String getTreeType(Node xmlNode) {
		if(isXmlType(xmlNode, "xml")) {
			return "program";
		} else if(isXmlType(xmlNode, "block")) {
			Element eElement = (Element) xmlNode;
			return eElement.getAttribute("type");
		} else if(isXmlType(xmlNode, "title")) {
			Element eElement = (Element) xmlNode;
			return eElement.getTextContent();
		} else if(isXmlType(xmlNode, "next")) {
			return "next";
		} else if(isXmlType(xmlNode, "statement")) {
			Element eElement = (Element) xmlNode;
			return eElement.getAttribute("name");
		}
		//System.out.println("ERROR");
		//System.out.println(original);
		throw new CorruptedException("unhandeled: " + xmlNode.getNodeName());
	}

	private boolean isXmlType(Node n, String type) {
		return n.getNodeName().equals(type);
	}

	private List<Node> flatten(Node xml) {
		List<Node> flattened = new ArrayList<Node>();
		flattened.add(xml);
		Node next = getNextChild(xml);
		while(next != null) {
			NodeList nextChildren = next.getChildNodes();
			if(nextChildren.getLength() != 1) {
				throw new RuntimeException("I assume all next nodes only have 1 child");
			}
			Node curr = nextChildren.item(0);
			flattened.add(curr);
			next = getNextChild(curr);
		}
		return flattened;
	}

	private Node getNextChild(Node n) {
		NodeList children = n.getChildNodes();
		for(int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if(isXmlType(child, "next")) {
				return child;
			}
		}
		return null;
	}

}
