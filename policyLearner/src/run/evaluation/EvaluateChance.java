package run.evaluation;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import minions.GraphLoader;
import minions.ProgramLoader;
import models.ast.Program;
import util.DirectedGraph;
import util.FileSystem;
import util.MapSorter;
import util.RandomUtil;
import util.Warnings;

/**
 * Class: Evaluate Policy
 * ----------------------
 * Given a ground truth graph, and a policy graph, test how well the policy
 * does at recreating the ground truth. Give stats for number of edges correct
 * and also number of edges, weighted by student counts.
 * @author Chris Piech
 */
public class EvaluateChance {

	// constants
	private static final String MAIN_ASSN_ID = "hoc18";
	private static final int ITERATIONS = 10000;

	// instance variables;
	private DirectedGraph<String> groundTruth = null;
	private DirectedGraph<String> roadMap = null;
	private Map<String, Integer> countMap = null;

	private void run() {
		FileSystem.setAssnId(MAIN_ASSN_ID);

		// load the necessary graphs
		loadGraphs();

		double sum = 0;
		for(int i = 0; i < ITERATIONS; i++) {
			Map<String, String> random = makeRandomPolicy();
			double stat = anyStat(random, true);
			sum += stat;
			if(i % 1000 == 0) System.out.println(i);
		}

		double avg = sum / ITERATIONS;
		
		System.out.println(avg);

	}

	private Map<String, String> makeRandomPolicy() {
		Map<String, String> randomRab = new HashMap<String, String>();
		for(String key : groundTruth.vertexSet()) {
			if(key.equals("0")) continue;
			if(groundTruth.getOutgoing(key).size() == 0) continue;
			
			Set<String> options = roadMap.getOutgoing(key);
			String chosen = choseRandom(options);
			
			randomRab.put(key, chosen);
		}
		return randomRab;
	}

	private String choseRandom(Set<String> options) {
		ArrayList<String> l = new ArrayList<String>();
		l.addAll(options);
		int index = RandomUtil.randInt(l.size());
		return l.get(index);
	}

	private void loadGraphs() {
		groundTruth = GraphLoader.loadGroundTruth();
		roadMap = GraphLoader.loadRoadMap();
		countMap = ProgramLoader.loadCountMap();
	}

	private double anyStat(Map<String, String> policy, 
			boolean weighted) {
		int numTested = 0;
		int numCorrect = 0;
		for(String key : groundTruth.vertexSet()) {
			if(key.equals("0")) continue;

			if(groundTruth.getOutgoing(key).size() == 0) continue;

			String guess = policy.get(key);
			int weight = weighted ? countMap.get(key) : 1;
			if(groundTruth.getOutgoing(key).contains(guess)) {
				numCorrect += weight;
			}
			numTested += weight;
		}
		return 100.0 * numCorrect / numTested;
	}

	public static void main(String[] args) {
		new EvaluateChance().run();
	}

}
