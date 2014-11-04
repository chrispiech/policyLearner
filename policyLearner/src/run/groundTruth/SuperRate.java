package run.groundTruth;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import minions.BlockyStringParser;
import minions.ProgramLoader;
import models.ast.Program;
import models.ast.Tree;
import util.DirectedGraph;
import util.FileSystem;
import util.IdCounter;
import util.Warnings;

public class SuperRate {

	private static final String GROUNT_TRUTH_FILE_NAME = "superRater.txt";

	private static final String MAIN_ASSN_ID = "hoc3";

	private List<Program> programs;

	private DirectedGraph<String> ratings;

	public void run() {

		System.out.println("loading so far: ");
		loadSuperRatings();


		System.out.println("load programs: ");
		programs = ProgramLoader.loadPrograms("contiguous", 3000);
		System.out.println("----");

		printInstructions();

		for(Program p : programs) {
			if(!p.perfectOnUnitTests()) {
				int numRated = getNumRatings(p.getId());
				while(numRated < 7) {
					String groundTruthId = getGroundTruth(p);
					if(groundTruthId == null) break;
					
					int count = getCount();
					ratings.addEdge(p.getId(), groundTruthId, count);
					numRated = getNumRatings(p.getId());
					
					saveSuperRatings();
					System.out.println(p.getId() + ", " + groundTruthId);
					System.out.println("");
				}
			}
		}
	}

	private int getNumRatings(String source) {
		int n = 0;
		if(!ratings.containsVertex(source)) return 0;
		for(String out : ratings.getOutgoing(source)) {
			n += ratings.getWeight(source, out);
		}
		System.out.println("(num rated: " + n + ")");
		return n;
	}

	private void saveSuperRatings() {
		File ratingsFile = getRatingsFile();
		ratings.saveEdgeMap(ratingsFile, true);
	}

	private void loadSuperRatings() {
		File ratingsFile = getRatingsFile();
		ratings = DirectedGraph.loadEdgeMap(ratingsFile);
	}

	private File getRatingsFile() {
		File assnDir = FileSystem.getAssnDir();
		File groundTruthDir = new File(assnDir, "groundTruth");
		File ratingsFile = new File(groundTruthDir, GROUNT_TRUTH_FILE_NAME);
		return ratingsFile;
	}

	private void printInstructions() {
		System.out.println("Welcome expert!");
		System.out.println("Given a partial solution, your job is");
		System.out.println("to check what the next blocky move that a learner");
		System.out.println("should take. Good luck.");
		System.out.println("");
	}

	private int getCount() {
		System.out.print("Num raters:");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try {
			String numString = br.readLine();
			int num = Integer.parseInt(numString);
			return num;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private String getGroundTruth(Program p) {
		System.out.println("Get ground truth for:");
		System.out.println(p);

		// TODO Auto-generated method stub
		System.out.print("Enter a comand string:");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try {
			String cmdString = br.readLine();
			if(cmdString.equals("empty")) {
				Program match = findProgram("");
				return match.getId();
			}
			if(!cmdString.isEmpty()) {
				Program match = findProgram(cmdString);
				return match.getId();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return null;
	}

	private Program findProgram(String cmdString) {
		//List<Tree> programChildren = getStatementList(cmdString);
		Tree toFind = BlockyStringParser.parseBlockyString(cmdString);


		for(Program p : programs) {

			if(p.getAst().equals(toFind)) {
				return p;
			}
		}
		return null;
	}



	public static void main(String[] args) {
		FileSystem.setAssnId(MAIN_ASSN_ID);
		new SuperRate().run();
	}

}
