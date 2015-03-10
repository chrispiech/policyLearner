package run;

import java.io.File;
import java.util.*;

import editDistance.TreeEditDistance;
import editDistance.TreeMove;
import editDistance.TreeReplace;
import minions.BlocklyHelper;
import minions.GraphLoader;
import minions.ProgramLoader;
import minions.XmlIdMap;
import models.ast.Program;
import models.ast.Tree;
import util.DirectedGraph;
import util.FileSystem;
import util.Warnings;

public class VanillaComment {

	private static final String ASSN_ID = "hoc1";

	private Map<String, Program> programMap;
	private DirectedGraph<String> policy;
	
	private void run() {
		FileSystem.setAssnId(ASSN_ID);
		
		// First load all of the programs..
		List<Program> programs = ProgramLoader.loadPrograms(10000);
		programMap = new HashMap<String, Program>();
		for(Program p : programs) {
			String id = p.getId();
			programMap.put(id, p);
		}
		
		// Then make a comment for each edge in the policy.
		policy = GraphLoader.loadGraph("policy.txt");
		for(int i = 1; i < 1000; i++) {
			String currId = i + "";
			if(!policy.containsVertex(currId)) continue;
			Set<String> outgoing = policy.getOutgoing(currId);
			Warnings.check(outgoing.size() == 1, "must be 1");
			String nextId = outgoing.iterator().next();
			makeComment(currId, nextId);
		}
	}

	private void makeComment(String currId, String nextId) {
		Tree a = programMap.get(currId).getAst();
		Tree b = programMap.get(nextId).getAst();
		List<TreeMove> treeMoves = TreeEditDistance.calcMoves(a, b);
		
		System.out.println("\n");
		System.out.println("Here is an example edit!");
		System.out.println("original:    " + a);
		System.out.println("policy next: " + b);

		Warnings.check(treeMoves.size() == 1, "should be 1");
		TreeMove move = treeMoves.get(0);
		System.out.println("move: " + move);
		
		System.out.println("\n");
		//throw new RuntimeException("this is just a test");
	}

	public static void main(String[] args) {
		new VanillaComment().run();
	}

}
