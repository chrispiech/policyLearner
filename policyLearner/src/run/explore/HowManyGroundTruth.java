package run.explore;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import minions.BlockyStringParser;
import minions.GraphLoader;
import minions.ProgramLoader;
import models.ast.Program;
import models.ast.Tree;
import util.DirectedGraph;
import util.FileSystem;
import util.IdCounter;

public class HowManyGroundTruth {


	private static final String MAIN_ASSN_ID = "hoc18";

	public void run() {
		DirectedGraph<String> gt = GraphLoader.loadGroundTruth();

		int count = 0;
		for(String node : gt.vertexSet()) {
			if(gt.getOutgoing(node).size() > 0) {
				count++;
			}
		}
		System.out.println(count);
	}




	public static void main(String[] args) {
		FileSystem.setAssnId(MAIN_ASSN_ID);
		new HowManyGroundTruth().run();
	}

}
