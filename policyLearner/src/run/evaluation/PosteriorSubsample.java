package run.evaluation;

import java.io.File;
import java.util.*;

import minions.GraphLoader;
import minions.ProgramLoader;
import minions.TrajectoryLoader;
import minions.UnseenPathMinion;
import models.trajectory.Trajectory;
import util.DirectedGraph;
import util.Edge;
import util.FileSystem;
import util.IdCounter;
import util.MapSorter;
import util.RandomCollection;
import util.Warnings;

public class PosteriorSubsample {

	private static final String MAIN_ASSN_ID = "hoc18";
	private static final int ITERATIONS = 30;
	private static final String TRAJECTORY_DIR = "interpolated";
	private static final double EPSILON = 0.0001;

	private DirectedGraph<String> roadMap = null;
	private DirectedGraph<String> groundTruth = null;
	private Map<String, Integer> countMap = null;
	DirectedGraph<String> perfectGraph = null;

	public void run() {

		roadMap = GraphLoader.loadRoadMap();
		groundTruth = GraphLoader.loadGroundTruth();
		countMap = ProgramLoader.loadCountMap();

		// Get the number of students who visit each node.
		List<Trajectory> trajectories = TrajectoryLoader.load(TRAJECTORY_DIR);
		

		for(int subsamples = 100; subsamples < 100000; subsamples *= 2) {
			double sum = 0;
			for(int i = 0; i < ITERATIONS; i++) {
				List<Trajectory> subsample = subsample(trajectories, subsamples);
				Map<String, String> nodeRank = posterior(subsample);
				double score = evaluate(nodeRank, true);
				sum += score;
			}

			double result = sum / ITERATIONS;
			System.out.println(subsamples + "\t" + result);
		}
	}

	private Map<String, String> posterior(List<Trajectory> subsample) {
		DirectedGraph<String> perfectGraph = new DirectedGraph<String>();
		
		for(Trajectory t : subsample) {
			for(Edge<String> edge : t.getEdges()) {
				int count = t.getCount();
				perfectGraph.increaseWeight(edge.getSource(), edge.getTarget(), count);
			}
		}
		
		Map<String, String> post = new HashMap<String, String>();
		for(String node : groundTruth.vertexSet()) {
			if(groundTruth.getOutgoing(node).size() == 0) continue;
			
			String next = getBest(perfectGraph, node);
			post.put(node, next);
		}
		return post;
			
	}

	private String getBest(DirectedGraph<String> perfectGraph, String node) {
		String argMax = null;
		double best = 0;
		for(String next : perfectGraph.getOutgoing(node)) {
			if(!roadMap.containsEdge(node, next)) continue;
			double score = perfectGraph.getWeight(node, next);
			if(argMax == null || score > best) {
				best = score;
				argMax = next;
			}
		}
		return argMax;
	}





	private List<Trajectory> subsample(List<Trajectory> trajectories, int num) {
		RandomCollection<Trajectory> bag = new RandomCollection<Trajectory>();
		for(Trajectory t : trajectories) {
			int count = t.getCount();
			bag.add(t, count);
		}

		List<Trajectory> sample = new ArrayList<Trajectory>();
		IdCounter ids = new IdCounter();
		for(int i = 0; i < num; i++) {
			Trajectory n = bag.next();
			Warnings.msg("This ensures that only perfect trajectories are included");
			if(!n.endsInState("0")) continue;
			List<String> programIds = n.getProgramIds();
			Trajectory copy = new Trajectory(ids.getNextIdStr(), 
					programIds, 1);
			sample.add(copy);
		}
		return sample;
	}

	private double evaluate(Map<String, String> policy, boolean weighted) {
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
		FileSystem.setAssnId(MAIN_ASSN_ID);
		new PosteriorSubsample().run();
	}
}
