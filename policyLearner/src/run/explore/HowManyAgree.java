package run.explore;

import java.util.*;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import minions.GraphLoader;
import minions.TrajectoryLoader;
import models.trajectory.Trajectory;
import util.DirectedGraph;
import util.Edge;
import util.FileSystem;
import util.Histogram;
import util.Warnings;

public class HowManyAgree {

	private void run() {
		FileSystem.setAssnId("hoc3");
		List<Trajectory> trajectories = TrajectoryLoader.load("interpolated");
		
		DirectedGraph<String> groundTruth = GraphLoader.loadGroundTruth();
		
		Histogram h = new Histogram(0, 1.0, 0.1);
		
		for(Trajectory t : trajectories) {
			SummaryStatistics trajStats = new SummaryStatistics();
			for(Edge<String> edge : t.getEdges()) {
				String a = edge.getSource();
				String b = edge.getTarget();
				if(hasAnswer(groundTruth, a)) {
					Set<String> correct = groundTruth.getOutgoing(a);
					boolean goodChoice = correct.contains(b);
					int score = goodChoice ? 1 : 0;
					trajStats.addValue(score);
				}
			}
			Warnings.check(trajStats.getN() > 0, "what?");
			h.addPoint(trajStats.getMean(), t.getCount());
		}
		
		System.out.println(h);
	}
	
	private boolean hasAnswer(DirectedGraph<String> groundTruth, String a) {
		if(!groundTruth.containsVertex(a)) return false;
		return groundTruth.getOutgoing(a).size() > 0;
	}
	

	public static void main(String[] args) {
		new HowManyAgree().run();
	}
}
