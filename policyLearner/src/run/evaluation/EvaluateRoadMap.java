package run.evaluation;

import java.io.File;
import java.util.*;

import minions.ProgramLoader;
import util.DirectedGraph;
import util.FileSystem;
import util.MapSorter;

public class EvaluateRoadMap {

	private static final String MAIN_ASSN_ID = "17";

	private void run() {

		/*DirectedGraph<String> truth = DirectedGraph.loadEdgeMap("groundTruthNext.txt");
		DirectedGraph<String> roadMap = DirectedGraph.loadEdgeMap("illegalMoveRoadMap.txt");

		List<String> wrong = new ArrayList<String>();
		
		int numEdges = 0;
		int numCorrect = 0;
		for(String node : truth.vertexSet()) {
			String trueNext = getTrueNext(truth, node);
			if(trueNext != null) {
				if(roadMap.containsEdge(node, trueNext)) {
					numCorrect++;
				} else {
					wrong.add(node);
				}
				numEdges++;
			}
		}

		double percentCorrect = (double)(numCorrect) / numEdges;
		System.out.println("Results:");
		System.out.println("num edges: " + numEdges);
		System.out.println("percent correct: " + percentCorrect);
		
		System.out.println("missing edges");
		for(int i = 0; i < wrong.size(); i++){
			String id = wrong.get(i);
			String next = getTrueNext(truth, id);
			System.out.println(id + " -> " + next);
		}*/
		throw new RuntimeException("depricated");
	}

	private String getTrueNext(DirectedGraph<String> truth, String node) {
		Set<String> out = truth.getOutgoing(node);
		if(out.size() == 0) {
			return null;
		}
		if(out.size() != 1) {
			System.out.println("multiple for: " + node);
			for(String next : out) {
				System.out.println(next);
			}
			throw new RuntimeException("whaaat?");
		}
		for(String next : out) {
			return next;
		}
		return null;
	}

	public static void main(String[] args) {
		FileSystem.setAssnId(MAIN_ASSN_ID);
		new EvaluateRoadMap().run();
	}
}
