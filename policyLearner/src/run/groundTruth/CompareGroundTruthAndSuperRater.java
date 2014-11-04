package run.groundTruth;

import minions.GraphLoader;
import run.evaluation.CompareGraphs;
import util.DirectedGraph;
import util.FileSystem;
import util.Warnings;

public class CompareGroundTruthAndSuperRater {

	private void run() {
		FileSystem.setAssnId("hoc3");
		
		DirectedGraph<String> groundTruth = GraphLoader.loadGroundTruth();
		DirectedGraph<String> superRater = GraphLoader.loadSuperRater();
		
		// first, check that all nodes in ground truth have only one next
		for(String v : groundTruth.vertexSet()) {
			int count = groundTruth.getOutgoing(v).size();
			Warnings.check(count <= 1, "thats odd");
		}
		
		// then, see where the superrater disagrees
		for(String v : groundTruth.vertexSet()) {
			int count = groundTruth.getOutgoing(v).size();
			if(count == 0) continue;
			
			String gtNext = groundTruth.getOutgoing(v).iterator().next();
			String superNext = getSuperRaterNext(superRater, v);
			
			if(superNext == null) continue;
			
			if(!gtNext.equals(superNext)) {
				System.out.println("node:\t"+ v);
				System.out.println("super:\t" + superNext);
				System.out.println("ground:\t" + gtNext);
				System.out.println("");
			}
		}
		
	}
	
	private String getSuperRaterNext(DirectedGraph<String> sr, String v) {
		String argMax = null;
		double max = 0;
		for(String t : sr.getOutgoing(v)) {
			double votes = sr.getWeight(v, t);
			if(argMax == null || votes > max) {
				argMax = t;
			}
		}
		return argMax;
	}

	public static void main(String[] args) {
		new CompareGroundTruthAndSuperRater().run();
	}

	
}
