package run.abilities;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import util.CountMap;
import util.DirectedGraph;
import util.FileSystem;
import util.Histogram;
import util.MapSorter;
import util.RandomUtil;
import util.Warnings;
import minions.GraphLoader;
import minions.TrajectoryLoader;
import models.trajectory.Trajectory;

public class CreatePredictionTask {

	DirectedGraph<String> roadMap;
	double EPSILON = 0.0001;
	int FOLDS = 10;
	
	private void run() {
		FileSystem.setAssnId("hoc18");
		roadMap = GraphLoader.loadRoadMap();
		List<Trajectory> interpolated = 
				TrajectoryLoader.load("interpolated");
		Map<String, Integer> nodeCount = getNodeCount(interpolated);
		DirectedGraph<String> edgeCost = makeCoherenceCost(nodeCount);
		//chrisExp(edgeCost, nodeCount);
		//saveScoreCountCorrelation(interpolated, edgeCost);
		
		File nextProblemDir = new File(FileSystem.getAssnDir(), "nextProblem");
		File attemptFile = new File(nextProblemDir, "attemptSet.txt");
		File perfectFile = new File(nextProblemDir, "perfectSet.txt");
		Set<String> nextProblemAttempts = FileSystem.getFileSet(attemptFile);
		Set<String> nextProblemPerfects = FileSystem.getFileSet(perfectFile);
		
		// Trajectory id map goes studentId, trajectoryId
		Map<String, String> idMap = TrajectoryLoader.loadIdMap("trajectories");
		Map<String, List<String>> reverseMap = MapSorter.reverseMap(idMap);
		
		List<Trajectory> trajectories = 
				TrajectoryLoader.load("trajectories");
		
		int numPerfectAndFinish = 0;
		int numAttemptAndFinish = 0;
		int numAttemptNext = 0;
		int numFinish = 0;
		int num = 0;
		
		List<List<String>> features = new ArrayList<List<String>>();
		for(int i = 0; i < FOLDS; i++) {
			features.add(new ArrayList<String>());
		}
		
		
		
		
		int done = 0;
		System.out.println("making features...");
		int numStudents = 0;
		for(Trajectory t : trajectories) {
			List<String> nodeList = getNodeList(t);
			if(nodeList == null || nodeList.size() <= 1) continue;
			
			boolean finished = t.endsInState("0");
			
			List<String> students = reverseMap.get(t.getId());
			
			Double pathCost = getPathCost(edgeCost, nodeList);
			if(pathCost == null) continue;
			double pathCountAprox = 1.0 / pathCost;
			Warnings.check(pathCountAprox >= 0, "yo");
			
			for(String studentId : students) {
				boolean attemptNext = nextProblemAttempts.contains(studentId);
				boolean perfectNext = nextProblemPerfects.contains(studentId);
				
				String featureList = "";
				featureList += t.getNumHops() + ",";
				featureList += pathCountAprox + ",";
				featureList += t.getLastNode() + ",";
				featureList += attemptNext + ",";
				featureList += perfectNext;
				
				int fold = RandomUtil.randInt(FOLDS);
				features.get(fold).add(featureList);
				
				if(attemptNext) numAttemptNext++;
				if(finished) numFinish++;
				if(attemptNext && finished) numAttemptAndFinish++;
				if(perfectNext && finished) numPerfectAndFinish++;
				num++;
				numStudents++;
			}
			
			if(++done % 100 == 0) System.out.println(done);
		}
		
		File foldsDir = new File(nextProblemDir, "prediction");
		for(int i = 0; i < features.size(); i++) {
			System.out.println("saving fold " + i);
			List<String> foldFeatures = features.get(i);
			Collections.shuffle(foldFeatures);
			FileSystem.createFile(foldsDir, i + ".txt", foldFeatures);
		}
		
		System.out.println("num students: " + numStudents);
		System.out.println("attemptN attemptN+1: " + 100.0 * numAttemptNext / num);
		System.out.println("perfectN attemptN+1: " + 100.0 * numAttemptAndFinish / numFinish);
		System.out.println("perfectN perfectN+1: " + 100.0 * numPerfectAndFinish / numFinish);
		
		//System.out.println(argMin);
		//System.out.println(h);
		
		
	}
	
	private void chrisExp(DirectedGraph<String> edgeCost, Map<String, Integer> nodeCount) {
		List<String> nodeList = new ArrayList<String>();
		nodeList.add("34");
		nodeList.add("i53");
		nodeList.add("28");
		nodeList.add("10");
		nodeList.add("238");
		
		for(String s : nodeList) {
			Integer count = nodeCount.get(s);
			if(count == null) {
				System.out.println("null");
				continue;
			}
			System.out.println(s + "\t" + count);
		}
		
		for(int i = 1; i < nodeList.size(); i++) {
			String a = nodeList.get(i - 1);
			String b = nodeList.get(i);
			Warnings.check(roadMap.containsEdge(a, b), "wooah");
		}
		
		List<String> shortList = new ArrayList<String>();
		shortList.add("34");
		shortList.add("238");
		
		Double pathCost = getPathCost(edgeCost, nodeList);
		System.out.println(pathCost);
		
		Double shortCost = getPathCost(edgeCost, shortList);
		System.out.println(shortCost);
		
		// TODO Auto-generated method stub
		throw new RuntimeException("yep");
	}

	private Double getPathCost(DirectedGraph<String> edgeCost,
			List<String> nodeList) {
		Warnings.check(nodeList.size() >= 2, "no");
		
		double cost = 0;
		for(int i = 1; i < nodeList.size(); i++) {
			String a = nodeList.get(i - 1);
			String b = nodeList.get(i);
			if(a.equals(b)) continue;
			Double shortestPathCost = edgeCost.getShortestPathCost(a, b);
			if(shortestPathCost == null) return null;
			cost += shortestPathCost;
		}
		
		return cost;
	}

	private List<String> getNodeList(Trajectory t) {
		Warnings.check(FileSystem.getAssnId().equals("hoc18"), "no");
		List<String> nodes = new ArrayList<String>();
		nodes.add("34");
		for(String id : t.getProgramIds()) {
			if(!roadMap.containsVertex(id)) return null;
		}
		nodes.addAll(t.getProgramIds());
		return nodes;
	}

	private DirectedGraph<String> makeCoherenceCost(Map<String, Integer> nodeCount) {
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
	
	private Map<String, Integer> getNodeCount(List<Trajectory> trajectories) {
		Map<String, Integer> nodeCount = new HashMap<String, Integer>();
		for(Trajectory t : trajectories) {
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
	
	public static void main(String[] args) {
		new CreatePredictionTask().run();
	}

}
