package run.evaluation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import util.DirectedGraph;
import util.FileSystem;
import minions.GraphLoader;
import minions.ProgramLoader;
import models.ast.Program;

public class ExploreGroundTruth {

	private void run() {
		FileSystem.setAssnId("hoc18");
		DirectedGraph<String> gt = GraphLoader.loadGroundTruth();
		List<Program> programs = ProgramLoader.loadPrograms(30000);
		Map<String, Program> programMap = new HashMap<String, Program>();
		for(Program p : programs ){
			programMap.put(p.getId(), p);
		}
		List<Program> unseen = ProgramLoader.loadPrograms("unseen");
		for(Program p : unseen ){
			programMap.put(p.getId(), p);
		}


		for(int i = 0; i < 100; i++) {
			String node = i + "";
			if(!gt.containsVertex(node)) continue;
			if(gt.getOutgoing(node).size() == 0) continue;
			Program p1 = programMap.get(node);

			System.out.println("NODE");
			System.out.println(p1);

			for(String next : gt.getOutgoing(node)) {

				Program p2 = programMap.get(next);

				if(p1 == null || p2 == null) {
					System.out.println(node);
					System.out.println(next);
					System.out.println("");
					continue;
				}

				System.out.println("TRUTH");
				System.out.println(p2);
			}

			waitForEnter();
			System.out.println("\n\n");
		}
	}

	private void waitForEnter() {
		System.out.print("Press ender:");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try {
			br.readLine();
			return;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void main(String[] args) {
		new ExploreGroundTruth().run();
	}
}
