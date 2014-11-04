package run.explore;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import minions.BlockyStringParser;
import minions.GraphLoader;
import minions.ProgramLoader;
import models.ast.Program;
import models.ast.Tree;
import util.DirectedGraph;
import util.FileSystem;
import util.IdCounter;

public class RoadMapDegree {


	private static final String MAIN_ASSN_ID = "hoc18";

	public void run() {
		DirectedGraph<String> roadMap = GraphLoader.loadRoadMap();

		SummaryStatistics stats = new SummaryStatistics();
		for(String node : roadMap.vertexSet()) {
			int outDegree = roadMap.getOutgoing(node).size();
			if(outDegree > 0) {
				stats.addValue(outDegree);
			}
		}
		System.out.println(stats.getMean());
	}




	public static void main(String[] args) {
		FileSystem.setAssnId(MAIN_ASSN_ID);
		new RoadMapDegree().run();
	}

}
