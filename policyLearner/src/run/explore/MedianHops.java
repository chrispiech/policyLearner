package run.explore;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import minions.BlockyStringParser;
import minions.GraphLoader;
import minions.ProgramLoader;
import minions.TrajectoryLoader;
import models.ast.Program;
import models.ast.Tree;
import models.trajectory.Trajectory;
import util.DirectedGraph;
import util.FileSystem;
import util.IdCounter;

public class MedianHops {


	private static final String MAIN_ASSN_ID = "hoc3";

	public void run() {
		List<Trajectory> trajectories = TrajectoryLoader.load("interpolated");
		DescriptiveStatistics stats = new DescriptiveStatistics();
		for(Trajectory t : trajectories) {
			stats.addValue(t.getNumHops());
		}
		System.out.println(stats.getPercentile(.5));
	}




	public static void main(String[] args) {
		FileSystem.setAssnId(MAIN_ASSN_ID);
		new MedianHops().run();
	}

}
