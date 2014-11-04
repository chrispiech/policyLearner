package run.evaluation;

import java.io.File;
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
import util.Warnings;

/**
 * Class: Evaluate Policy
 * ----------------------
 * Given a ground truth graph, and a policy graph, test how well the policy
 * does at recreating the ground truth. Give stats for number of edges correct
 * and also number of edges, weighted by student counts.
 * @author Chris Piech
 */
public class EvaluatePolicy {

	// constants
	private static final String MAIN_ASSN_ID = "hoc3";
	private static final String POLICY_NAME = "rivers.txt";
	private static final boolean SHOW_WORST = true;
	private static final int NUM_ASTS_FOR_WORST = 1000;

	// instance variables;
	private DirectedGraph<String> groundTruth = null;
	private Map<String, String> policy = null;
	private Map<String, Integer> countMap = null;
	private DirectedGraph<String> roadMap = null;

	private void run() {
		FileSystem.setAssnId(MAIN_ASSN_ID);

		// load the necessary graphs
		loadGraphs();

		// test the policy graph against the ground truth
		Map<String, Integer> howWrong = generateStats();

		// if desired, output some examples of where the policy went wrong.
		if(SHOW_WORST) {
			showWorst(howWrong);
		}
	}

	private void loadGraphs() {
		groundTruth = GraphLoader.loadGroundTruth();
		policy = loadPolicy();
		countMap = ProgramLoader.loadCountMap();
		roadMap = null;
	}

	private Map<String, Integer> generateStats() {
		// calculate stats
		int numCorrect = 0;
		int numEdges = 0;
		int numStudents = 0;
		int numStudentsCorrect = 0;

		Map<String, Integer> howWrong = new HashMap<String, Integer>();	

		for(String key : groundTruth.vertexSet()) {
			if(key.equals("0")) continue;
			Set<String> truth = groundTruth.getOutgoing(key);
			if(truth.size() == 0) continue;
			String guessValue = policy.get(key);
			int studentCount = countMap.get(key);
			
			// no guess counts as a wrong guess!!!
			if(guessValue != null && truth.contains(guessValue)) {
				numCorrect++;
				numStudentsCorrect += studentCount;
			} else {
				for(String a : truth) {
					if(roadMap != null && !roadMap.containsEdge(key, a)) {
						System.out.println("illegal: " + key + ", " + a);
					}
				}
				howWrong.put(key, studentCount);
			}	
			numEdges++;
			numStudents += studentCount;
		}

		double percentCorrect = (double)(numCorrect) / numEdges;
		double percentStudents = (double)(numStudentsCorrect) / numStudents;
		System.out.println("Results:");
		System.out.println("num edges: " + numEdges);
		System.out.println("percent correct: " + percentCorrect);
		System.out.println("");
		System.out.println("num students: " + numStudents);
		System.out.println("students wrong: " + (numStudents - numStudentsCorrect));
		System.out.println("percent students: " + percentStudents);
		return howWrong;
	}

	private void showWorst(Map<String, Integer> howWrong) {
		System.out.println("");
		System.out.println("wrong edges");
		HashMap<String, Program> programMap = loadPrograms();
		
		List<String> worst = new MapSorter<String>().sortInt(howWrong);
		for(int i = 0; i < Math.min(10, worst.size()); i++){
			String id = worst.get(i);
			String guessValue = policy.get(id);
			
			if(roadMap != null && !roadMap.containsEdge(id, guessValue)) {
				System.out.println("GUESS OUTSIDE ROADMAP!!!");
			}
			
			int count = countMap.get(id);
			System.out.println("node: " + id);
			System.out.println(programMap.get(id));
			for(String truth : groundTruth.getOutgoing(id) ) {
				System.out.println("truth: " + truth);
				System.out.println(programMap.get(truth));
			}
			System.out.println("guess: " + guessValue);
			System.out.println(programMap.get(guessValue));
			System.out.println("size: " + count);

			System.out.println("---");
			System.out.println("");
			System.out.println("");
			System.out.println("");
		}
	}

	private Map<String, String> loadPolicy() {
		File policyDir = new File(FileSystem.getAssnDir(), "policies");
		File policyFile = new File(policyDir, POLICY_NAME);
		return FileSystem.getFileMapString(policyFile);
	}

	private HashMap<String, Program> loadPrograms() {
		System.out.println("load programs: ");
		List<Program> programs = ProgramLoader.loadPrograms("asts", NUM_ASTS_FOR_WORST);
		programs.addAll(ProgramLoader.loadPrograms("unseen"));
		System.out.println(programs.size());
		HashMap<String, Program> programMap = new HashMap<String, Program>();
		for(Program p : programs) {
			String id = p.getId();
			programMap.put(id, p);
		}
		return programMap;
	}

	public static void main(String[] args) {
		new EvaluatePolicy().run();
	}

}
