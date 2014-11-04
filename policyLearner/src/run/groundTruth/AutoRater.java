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
import util.NeedlemanWunsch;
import util.Warnings;

public class AutoRater extends Rate {

	protected static final String GROUNT_TRUTH_FILE_NAME = "autoRater.txt";

	private String best = "MLMRM";
	private AutoCosts c;

	private String raterTxt = "";

	public AutoRater() {
		c = new AutoCosts();

		c.eqCost = 0.2;

		c.endInsert = 0.1;
		c.middleInsert = 5.0;
		c.beginInsert = 2.0;

		c.endDelete = 0.8;
		c.delete = 1.0;

		c.leftRight = 0.5;
		c.eqCost = 20;
	}

	@Override
	protected String getGroundTruth(Program p) {
		String programStr = getProgramString(p);
		System.out.println(programStr);
		String dna = AutoHelper.align(programStr, best, c, true);
		Program nextP = findProgram(dna);
		if(nextP != null) {
			String programId = nextP.getId();
			raterTxt += p.getId() + "," + programId + "\n";
			return programId;
		}
		return null;
	}

	@Override
	protected boolean shouldSkip(Set<String> done, Program p) {
		return false;
	}

	@Override
	protected void finished() {
		File groundTruth = new File(FileSystem.getAssnDir(), "groundTruth");
		File raters = new File(groundTruth, "raters");
		FileSystem.createFile(raters, GROUNT_TRUTH_FILE_NAME, raterTxt);
	}

	private String getProgramString(Program p) {
		Tree t = p.getAst();
		String str = "";
		for(Tree a : t.getChildren()) {
			String type = a.getType();
			if(type.equals("maze_moveForward")) {
				str += "M";
			}
			if(type.equals("maze_turnLeft")) {
				str += "L";
			}
			if(type.equals("maze_turnRight")) {
				str += "R";
			}
		}
		return str;
	}

	public static void main(String[] args) {
		new AutoRater().run();
		//new Align().align("MRMRM", "MLMRM");
	}

}
