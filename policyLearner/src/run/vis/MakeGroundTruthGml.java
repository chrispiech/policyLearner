package run.vis;

import java.io.File;
import java.util.*;

import minions.GraphLoader;
import minions.ProgramLoader;
import util.DirectedGraph;
import util.Edge;
import util.FileSystem;

public class MakeGroundTruthGml {
	private void run() {
		FileSystem.setAssnId("hoc3");
		
		// the important data
		
		DirectedGraph<String> truth = GraphLoader.loadGroundTruth();
		
		Map<String, Integer> countMap = ProgramLoader.loadCountMap();
		
		Set<String> nodes = truth.getInConnected("0");
		
		DirectedGraph<String> culled = new DirectedGraph<String>();
		for(Edge<String> e : truth.getEdges()) {
			String s = e.getSource();
			String t = e.getTarget();
			if(nodes.contains(s) && nodes.contains(t)) {
				culled.addEdge(s, t);
			}
		}
		
		File assnDir = FileSystem.getAssnDir();
		culled.saveGml(assnDir, "groundTruth.gml", countMap);
		
	}

	

	
	public static void main(String[] args) {
		new MakeGroundTruthGml().run();
	}
}
