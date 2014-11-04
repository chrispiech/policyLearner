package run.userStudy;

import java.io.File;
import java.util.*;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import minions.ProgramLoader;
import util.DirectedGraph;
import util.FileSystem;
import util.Warnings;

public class Explore {

	
	private void run() {
		FileSystem.setAssnId("hoc18");
		
		List<UserStudyTrajectory> trajectories = UserStudyLoader.load();
		
		Map<String, List<UserStudyTrajectory>> groupMap = 
				new HashMap<String, List<UserStudyTrajectory>>();
		
		for(UserStudyTrajectory tj : trajectories) {
			String group = tj.getGroup();
			if(!groupMap.containsKey(group)) {
				groupMap.put(group, new ArrayList<UserStudyTrajectory>());
			}
			groupMap.get(group).add(tj);
		}
		
		DirectedGraph<String> edgeCost = CoherenceLoader.load();
		
		for(String group : groupMap.keySet()) {
			int size = groupMap.get(group).size();
			List<UserStudyTrajectory> paths = groupMap.get(group);
			double passRate = getPassRate(paths);
			double pathScores = getAveragePathScore(edgeCost, paths);
			double numHops = getAverageHops(paths);
			String groupName = getGroupName(group);
			String line = "";
			line += group + "(" + groupName + ") \t";
			line += size + "\t";
			line += numHops + "\t";
			line += passRate + "\t";
			line += pathScores;
			System.out.println(line);
		}
		
		System.out.println("done");
	}
	
	private double getAverageHops(List<UserStudyTrajectory> paths) {
		SummaryStatistics stats = new SummaryStatistics();
		for(UserStudyTrajectory tj : paths) {
			int numHops = tj.numHops();
			stats.addValue(numHops);
		}
		return stats.getMean();
	}

	private String getGroupName(String group) {
		if(group.equals("0")) return "crowd";
		if(group.equals("1")) return "stanford";
		if(group.equals("2")) return "control";
		throw new RuntimeException("test");
	}

	private double getAveragePathScore(DirectedGraph<String> edgeCost,
			List<UserStudyTrajectory> list) {
		SummaryStatistics stats = new SummaryStatistics();
		int numFail = 0;
		for(UserStudyTrajectory tj : list) {
			List<String> nodeList = tj.getNodeList();
			Double cost = getPathCost(edgeCost, nodeList);
			if(cost != null) {
				double score = Math.log(1/cost);
				stats.addValue(score);
			} else {
				numFail++;
				getPathCost(edgeCost, nodeList);
			}
		}
		return stats.getMean();
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

	
	private double getPassRate(List<UserStudyTrajectory> list) {
		SummaryStatistics stats = new SummaryStatistics();
		for(UserStudyTrajectory t : list) {
			boolean pass = t.getLastId().equals("0");
			int score = pass ? 1 : 0;
			stats.addValue(score);
		}
		return stats.getMean();
	}


	public static void main(String[] args) {
		new Explore().run();
	}
}
