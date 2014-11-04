package run.evaluation;

import interpolate.Interpolate;

import java.io.File;
import java.util.*;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

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

public class NodeRankSubsample {

	private static final String MAIN_ASSN_ID = "hoc3";
	private static final int ITERATIONS = 100;
	private static final String TRAJECTORY_DIR = "trajectories";
	private static final double EPSILON = 0.0001;

	private DirectedGraph<String> roadMap = null;
	private DirectedGraph<String> groundTruth = null;
	private Map<String, Integer> countMap = null;

	public void run() {

		roadMap = GraphLoader.loadRoadMap();
		groundTruth = GraphLoader.loadGroundTruth();
		countMap = ProgramLoader.loadCountMap();

		// Get the number of students who visit each node.
		List<Trajectory> trajectories = TrajectoryLoader.load(TRAJECTORY_DIR);

		for(int subsamples = 1; subsamples < 100000; subsamples *= 2) {
			SummaryStatistics stats = new SummaryStatistics();
			for(int i = 0; i < ITERATIONS; i++) {
				System.out.print(".");
				List<Trajectory> subsample = subsample(trajectories, subsamples);
				List<Trajectory> interpolated = Interpolate.interp(subsample);
				Map<String, String> nodeRank = nodeRank(interpolated);
				double score = evaluate(nodeRank, true);
				stats.addValue(score);
			}
			System.out.println("");

			double mean = stats.getMean();
			double std = stats.getStandardDeviation();
			System.out.println(subsamples + "\t" + mean + "\t" + std);
		}
	}

	private Map<String, String> nodeRank(List<Trajectory> subsample) {
		Map<String, Integer> nodeCount = getNodeCount(subsample);

		DirectedGraph<String> coherenceCost = makeCoherenceCost(nodeCount);

		Map<String, String> nodeRank = makePrediction(coherenceCost);
		return nodeRank;
	}

	private Map<String, String> makePrediction(
			DirectedGraph<String> coherenceCost) {
		// Turn it all into a prediction
		Map<String, String> nodeRank = new HashMap<String, String>();
		String soln = "0";
		for(String node : groundTruth.vertexSet()) {
			if(node.equals(soln)) continue;
			
			if(groundTruth.getOutgoing(node).size() == 0) continue;

			if(!coherenceCost.containsVertex(node)) continue;
			if(!coherenceCost.containsVertex(soln)) continue;
			List<Edge<String>> path = coherenceCost.getShortestPath(node, soln);
			if(path != null) {

				Edge<String> firstEdge = path.get(0);
				String next = firstEdge.getTarget();
				nodeRank.put(node, next);
			}
		}
		return nodeRank;
	}

	private DirectedGraph<String> makeCoherenceCost(
			Map<String, Integer> nodeCount) {
		// Make a graph where each edge has cost = coherence of the next node
		DirectedGraph<String> coherenceCost = new DirectedGraph<String>();
		for(String node : roadMap.vertexSet()) {
			double count = EPSILON;
			if(nodeCount.containsKey(node)) {
				count = nodeCount.get(node);
			}

			if(count == 0) continue;

			for(String previous : roadMap.getIncoming(node)) {

				double cost = 1.0 / (count);

				coherenceCost.addEdge(previous, node, cost);
			}
		}
		return coherenceCost;
	}

	private Map<String, Integer> getNodeCount(List<Trajectory> subsample) {
		Map<String, Integer> nodeCount = new HashMap<String, Integer>();
		for(Trajectory t : subsample) {
			List<String> visited = t.getProgramIds();
			List<String> noCycle = new ArrayList<String>();
			for(int i = 0; i < visited.size(); i++) {
				String state = visited.get(i);
				noCycle.add(state);
				int lastIndex = visited.lastIndexOf(state);
				i = lastIndex;
			}

			for(String v : noCycle) {
				if(!nodeCount.containsKey(v)) {
					nodeCount.put(v, 0);
				}
				int newCount = nodeCount.get(v) + t.getCount();
				nodeCount.put(v, newCount);
			}
		}
		return nodeCount;
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
			List<String> programIds = n.getProgramIds();
			Trajectory copy = new Trajectory(ids.getNextIdStr(), 
					programIds,  1);
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
		new NodeRankSubsample().run();
	}
}
