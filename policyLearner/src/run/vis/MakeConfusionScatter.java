package run.vis;

import java.io.File;
import java.util.*;

import org.apache.commons.math3.util.Pair;
import org.json.JSONObject;

import util.DirectedGraph;
import util.Edge;
import util.FileSystem;
import minions.GraphLoader;
import minions.ProgramLoader;
import minions.TrajectoryLoader;
import models.graphs.TransitionGraph;
import models.trajectory.Trajectory;

public class MakeConfusionScatter {
	
	private static final String ASSN_ID = "hoc3";
	
	private void run() {
		FileSystem.setAssnId(ASSN_ID);
		DirectedGraph<String> groundTruth = GraphLoader.loadGroundTruth();
		DirectedGraph<String> roadMap = GraphLoader.loadRoadMap();
		List<Trajectory> trajectories = TrajectoryLoader.load("interpolated");
		TransitionGraph trans = new TransitionGraph(trajectories);
		
		Map<String, Integer> countMap = ProgramLoader.loadCountMap();
		
		System.out.println("\n\n\n");
		for(String node : groundTruth.outgoingSet()) {
			double trueCount = getMostPopularTeacher(node, groundTruth, roadMap, trans);
			double falseCount = getMostPopularNotTeacher(node, groundTruth, roadMap, trans);
			double ratio = trueCount / Math.max(falseCount, trueCount);
			ratio = Math.min(3, ratio);
			int count = countMap.get(node);
			System.out.println(count + "\t" + ratio);
		}
	}

	
	private double getMostPopularNotTeacher(String node, DirectedGraph<String> groundTruth,
			DirectedGraph<String> roadMap, TransitionGraph trans) {
		return getMostPopular(node, groundTruth, roadMap, trans, false);
	}


	private double getMostPopularTeacher(String node, DirectedGraph<String> groundTruth,
			DirectedGraph<String> roadMap, TransitionGraph trans) {
		return getMostPopular(node, groundTruth, roadMap, trans, true);
	}
	
	private double getMostPopular(
			String node,
			DirectedGraph<String> groundTruth, 
			DirectedGraph<String> roadMap, 
			TransitionGraph trans, 
			boolean wantTeacher) {
		
		double max = 0;
		
		for(String next : roadMap.getOutgoing(node)) {
			boolean isTeacher = groundTruth.containsEdge(node, next);
			if(isTeacher == wantTeacher) {
				int count = trans.getCount(node, next);
				max = Math.max(count, max);
			}	
		}
		
		return max;
	}


	public static void main(String[] args) {
		new MakeConfusionScatter().run();
	}

	
}
