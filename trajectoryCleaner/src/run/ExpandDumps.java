package run;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import expansion.AstExpander;
import expansion.TrajectoryExpander;
import raw.RawActivities;
import raw.RawData;
import raw.RawSources;
import util.FileSystem;
import util.IdCounter;
import util.MapSorter;
import minions.TreeJasonizer;
import models.ast.Program;
import models.ast.Tree;
import models.trajectory.Trajectory;

/**
 * Method: Run
 * -----------
 * Some notes:
 *   - counts are from the activities log directly, not from user xml lists...
 *     (user xml lists lose some of the rows in activities log probably because
 *     of ordering).
 */
public class ExpandDumps {
	
	private static final String LEVEL_DIR = "hoc1";

	private Map<String, String> xmlAstIdMap = null;

	private void run() {
		FileSystem.setAssnId(LEVEL_DIR);
		RawData raw = loadDumps();
		expandAsts(raw);
		expandTrajectories(raw);
		System.out.println("done");
	}
	
	private RawData loadDumps() {
		return new RawData();
	}
	
	private void expandAsts(RawData raw) {
		AstExpander expander = new AstExpander(raw);
		expander.expandAsts();
		xmlAstIdMap = expander.getXmlAstIdMap();
	}
	
	private void expandTrajectories(RawData raw) {
		TrajectoryExpander expander = new TrajectoryExpander(raw, xmlAstIdMap);
		expander.expandTrajectories();
	}

	public static void main(String[] args) {
		new ExpandDumps().run();
	}

}
