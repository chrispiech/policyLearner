package run.userStudy;

import java.util.*;

import minions.BlocklyHelper;
import minions.GraphLoader;
import minions.ProgramLoader;
import models.ast.Program;
import models.ast.Tree;
import util.DirectedGraph;
import util.FileSystem;
import util.Warnings;

class UserStudyTrajectory {
	private String id;
	private String testGroupId;
	private TreeMap<Integer, String> attemptMap;
	private List<String> programIds;
	
	private static Map<String, String> astIdMap = null;
	private static DirectedGraph<String> roadMap = null;
	private static Map<Tree, String> contiguous = null;
	private static Map<String, Tree> unique = null;
	
	private boolean issue;

	public UserStudyTrajectory(String id, String testGroupId) {
		if(astIdMap == null) {
			astIdMap = ProgramLoader.loadIdMap();
			roadMap = GraphLoader.loadRoadMap();
			contiguous = new HashMap<Tree, String>();
			for(Program p : ProgramLoader.loadPrograms()) {
				Tree flat = BlocklyHelper.makeContiguous(p.getAst());
				contiguous.put(flat, p.getId());
			}
			
			
			unique = new HashMap<String, Tree>();
			for(Program p : ProgramLoader.loadPrograms("unique")) {
				unique.put(p.getId(), p.getAst());
			}
		}
		
		this.id = id;
		this.testGroupId = testGroupId;
		attemptMap = new TreeMap<Integer, String>();
		issue = false;
	}
	
	public void addAttempt(int attemptNum, String sourceId) {
		//Warnings.check(!attemptMap.containsKey(attemptNum), id);
		attemptMap.put(attemptNum, sourceId);
	}

	public void makeProgramIdList() {
		programIds = new ArrayList<String>();
		for(int attempt : attemptMap.keySet()) {
			String xmlId = attemptMap.get(attempt);
			String astId = astIdMap.get(xmlId);
			System.out.println(attempt + "\t" + astId);
			if(astId != null && !roadMap.containsVertex(astId)) {
				Tree ast = unique.get(astId);
				Tree flat = BlocklyHelper.makeContiguous(ast);
				astId = contiguous.get(flat);
			}
			
			if(astId == null) {
				issue = true;
				Warnings.msg(xmlId);
				break;
			}
			
			programIds.add(astId);
			if(astId.equals("0")) break;
		}
		System.out.println("");
	}
	
	public int numHops() {
		return programIds.size();
	}
	
	public String getLastId() {
		return programIds.get(programIds.size() - 1);
	}

	public boolean shouldKeep() {
		if(issue) return false;
		if(numHops() == 0) return false;
		if(numHops() == 1 && getLastId().equals("0")) {
			return false;
		}
		return true;
	}

	public String getGroup() {
		return testGroupId;
	}

	public List<String> getNodeList() {
		if(!FileSystem.getAssnId().equals("hoc3")) {
			throw new RuntimeException("no");
		}
		List<String> nodeList = new ArrayList<String>();
		nodeList.add("51");
		nodeList.addAll(programIds);
		return nodeList;
	}
}