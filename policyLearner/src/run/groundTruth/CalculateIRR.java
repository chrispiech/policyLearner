package run.groundTruth;

import java.io.File;
import java.util.*;

import raterReliability.FleissKappaEmpirical;

import com.sun.tools.javac.util.Warner;

import minions.GraphLoader;
import util.DirectedGraph;
import util.FileSystem;
import util.Warnings;

public class CalculateIRR {

	private static final String MAIN_ASSN_ID = "hoc18";
	private List<DirectedGraph<String>> raters;
	private DirectedGraph<String> roadMap;
	
	private void run() {
		FileSystem.setAssnId(MAIN_ASSN_ID);
		loadRaters();
		roadMap = GraphLoader.loadRoadMap();
		List<String> states = getStatesWithRatings();
		
		int[][] raterData = new int[raters.size()][];
		for(int r = 0; r < raters.size(); r++) {
			raterData[r] = new int[states.size()];
		}
		
		for(int r = 0; r < raters.size(); r++) {
			DirectedGraph<String> rater = raters.get(r);
			for(int c = 0; c < states.size(); c++) {
				String state = states.get(c);
				int nextId = getNextId(rater, state);
				raterData[r][c] = nextId;
			}
		}
		
		int[] numOptions = new int[states.size()];
		for(int c = 0; c < states.size(); c++) {
			String state = states.get(c);
			numOptions[c] = roadMap.getOutgoing(state).size();
		}
		
		double irr = FleissKappaEmpirical.agreement(raterData, numOptions);
		System.out.println("IRR: " + irr);
	}
	
	
	private int getNextId(DirectedGraph<String> rater, String state) {
		if(!rater.containsVertex(state)) return -1;
		Set<String> out = rater.getOutgoing(state);
		if(out.isEmpty()) return -1;
		Warnings.check(out.size() == 1, "should be 1");
		String next = out.iterator().next();
		if(!roadMap.containsEdge(state, next)) return -1;
		return Integer.parseInt(next);
	}


	private List<String> getStatesWithRatings() {
		Set<String> statesWithRatings = new HashSet<String>();
		for(DirectedGraph<String> rater : raters) {
			for(String s : rater.vertexSet()) {
				if(!rater.getOutgoing(s).isEmpty()) {
					statesWithRatings.add(s);
				}
			}
		}
		return new ArrayList<String>(statesWithRatings);
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
		new CalculateIRR().run();
	}
}
