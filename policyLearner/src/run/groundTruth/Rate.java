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
import util.FileSystem;
import util.IdCounter;

public class Rate {

	protected static final String GROUNT_TRUTH_FILE_NAME = "rater.txt";

	private static final String MAIN_ASSN_ID = "hoc3";

	private List<Program> programs;

	public void run() {
		FileSystem.setAssnId(MAIN_ASSN_ID);
		File assnDir = FileSystem.getAssnDir();
		File graphs = new File(assnDir, "graphs");
		File groundTruthNext = new File(graphs, GROUNT_TRUTH_FILE_NAME);
		List<String> lines = FileSystem.getFileLines(groundTruthNext);
		String txt = "";
		Set<String> done = new HashSet<String>();
		if(lines != null) {
			for(String line : lines) {
				String[] elems = line.split(",");
				String key = elems[0].trim();
				done.add(key);
				txt += line + "\n";
			}
		}

		System.out.println("load programs: ");
		programs = ProgramLoader.loadPrograms("contiguous", 500);
		System.out.println("----");
		
		printInstructions();

		for(Program p : programs) {
			if(!p.perfectOnUnitTests() && !shouldSkip(done, p)) {
				String groundTruthId = getGroundTruth(p);
				if(groundTruthId != null) {
					System.out.println(p.getId() + ", " + groundTruthId);
					System.out.println("");
					txt += p.getId() + "," + groundTruthId + "\n";
					FileSystem.createFile(graphs, GROUNT_TRUTH_FILE_NAME, txt);
				}
			}
		}
		
		finished();
	}

	protected void finished() {
	}

	protected boolean shouldSkip(Set<String> done, Program p) {
		return done.contains(p.getId());
	}

	private void printInstructions() {
		System.out.println("Welcome expert!");
		System.out.println("Given a partial solution, your job is");
		System.out.println("to check what the next blocky move that a learner");
		System.out.println("should take. Good luck.");
		System.out.println("");
	}

	protected String getGroundTruth(Program p) {
		System.out.println("Get ground truth for:");
		System.out.println(p);
		
		// TODO Auto-generated method stub
		System.out.print("Enter a comand string:");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try {
			String cmdString = br.readLine();
			if(!cmdString.isEmpty()) {
				Program match = findProgram(cmdString);
				return match.getId();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return null;
	}

	protected Program findProgram(String cmdString) {
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
		new Rate().run();
	}

}
