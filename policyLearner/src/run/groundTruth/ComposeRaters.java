package run.groundTruth;

import java.io.File;
import java.util.*;

import minions.GraphLoader;
import util.DirectedGraph;
import util.FileSystem;
import util.Warnings;

public class ComposeRaters {
	
	private static final String MAIN_ASSN_ID = "hoc3";
	private List<DirectedGraph<String>> raters;
	private DirectedGraph<String> roadMap; 

	private void run() {
		FileSystem.setAssnId(MAIN_ASSN_ID);
		
		loadRaters();
		roadMap = GraphLoader.loadRoadMap();
		
		DirectedGraph<String> roadMap = GraphLoader.loadRoadMap();
		DirectedGraph<String> groundTruth = makeGroundTruth(roadMap);
		
		File groundTruthDir = new File(FileSystem.getAssnDir(), "groundTruth");
		File groundTruthFile = new File(groundTruthDir, "groundTruth.txt");
		groundTruth.saveEdgeMap(groundTruthFile, true);
		
		System.out.println("done");
		
	}

	private DirectedGraph<String> makeGroundTruth(DirectedGraph<String> roadMap) {
		DirectedGraph<String> groundTruth = new DirectedGraph<String>();
		for(String v : roadMap.vertexSet()) {
			Map<String, Integer> countMap = new HashMap<String, Integer>();
			for(DirectedGraph<String> raterGraph : raters) {
				if(!raterGraph.containsVertex(v)) continue;
				Set<String> out = raterGraph.getOutgoing(v);
				if(out.size() == 0) continue;
				Warnings.check(out.size() <= 1, "should be only one");
				String next = out.iterator().next();
				if(!roadMap.containsEdge(v, next)) continue;
				if(!countMap.containsKey(next)) {
					countMap.put(next, 0);
				}
				countMap.put(next, countMap.get(next) + 1);
			}
			
			int max = 0;
			for(String next : countMap.keySet()) {
				if(countMap.get(next) > max) {
					max = countMap.get(next);
				}
			}
			
			for(String next : countMap.keySet()) {
				double p = (double)(countMap.get(next)) / max;
				groundTruth.addEdge(v, next, p);
			}
		}
		return groundTruth;
	}
	
	private void loadRaters() {
		raters = new ArrayList<DirectedGraph<String>>();
		File groundTruthDir = new File(FileSystem.getAssnDir(), "groundTruth");
		File ratersDir = new File(groundTruthDir, "raters");
		for(File f : FileSystem.listFiles(ratersDir)) {
			raters.add(DirectedGraph.loadEdgeMap(f));
		}
		System.out.println("num raters: " + raters.size());
	}

	public static void main(String[] args) {
		new ComposeRaters().run();
	}
	
}
