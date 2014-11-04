package run.explore;

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

public class FindTree {


	private static final String MAIN_ASSN_ID = "hoc18";

	private List<Program> programs;

	public void run() {


		System.out.println("load programs: ");
		programs = ProgramLoader.loadPrograms("asts", 1000);
		programs.addAll(ProgramLoader.loadPrograms("unseen"));

		while(true) {
			Program p = findTree();
			System.out.println(p);
		}
	}

	private Program findTree() {
		System.out.print("Enter a comand string:");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try {
			String cmdString = br.readLine();
			Program match = findProgram(cmdString);
			return match;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
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
		new FindTree().run();
	}

}
