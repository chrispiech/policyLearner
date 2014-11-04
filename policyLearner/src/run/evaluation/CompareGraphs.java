package run.evaluation;

import java.util.*;

import minions.GraphLoader;
import util.DirectedGraph;
import util.FileSystem;

public class CompareGraphs {
	private static final String MAIN_ASSN_ID = "hoc3";

	private void run() {
		FileSystem.setAssnId(MAIN_ASSN_ID);
		DirectedGraph<String> p1 = GraphLoader.loadPolicy("nodeRank.txt");
		DirectedGraph<String> p2 = GraphLoader.loadPolicy("nodeRank6.txt");
		
		Set<String> verticies = new HashSet<String>();
		verticies.addAll(p1.vertexSet());
		verticies.addAll(p2.vertexSet());
		ArrayList<Integer> idSet = new ArrayList<Integer>();
		for(String s : verticies) {
			int id = Integer.parseInt(s);
			idSet.add(id);
		}
		Collections.sort(idSet);
		List<String> sortedList = new ArrayList<String>();
		for(int s : idSet) {
			sortedList.add("" + s);
		}
		
		for(String v : sortedList) {
			if(Integer.parseInt(v) > 100) continue;
			
			if(!p1.containsVertex(v)) {
				System.out.println("p1 missing: " + v);
			}
			if(!p2.containsVertex(v)) {
				System.out.println("p2 missing: " + v);
			}
			
			String n1 = getNext(p1, v);
			String n2 = getNext(p2, v);
			
			if(!n1.equals(n2)) {
				System.out.println("diff " + v + ":\t" + n1 + "\t" + n2);
			}
		}
	}
	
	private String getNext(DirectedGraph<String> p, String v) {
		if(p.getOutgoing(v).isEmpty()) return "null";
		return p.getOutgoing(v).iterator().next();
	}

	public static void main(String[] args) {
		new CompareGraphs().run();
	}

	
}
