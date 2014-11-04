package run;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import util.FileSystem;
import minions.BlocklyHelper;
import minions.ProgramLoader;
import minions.TreeJasonizer;
import models.ast.Program;
import models.ast.Tree;

public class IdentifyContiguous {
	
	private static final String ASSN_ID = "hoc4";
	
	public String countText = "";
	public String unitTestText = "";
	
	public List<Program> run() {
		FileSystem.setAssnId(ASSN_ID);
		List<Program> programs = ProgramLoader.loadUniquePrograms();
		
		System.out.println("Identify Contiguous.");
		
		System.out.println("num programs: " + programs.size());
		List<Program> contiguous = new ArrayList<Program>();
		for(Program p : programs) {
			if(BlocklyHelper.isContiguous(p)) {
				contiguous.add(p);
				saveProgram(p);
			}
		}
		saveCounts();
		saveUnitTestResults();
		return contiguous;
	}
	
	private void saveCounts() {
		File assnDir = FileSystem.getAssnDir();
		File uniqueDir = new File(assnDir, "contiguous");
		FileSystem.createFile(uniqueDir, "counts.txt", countText);
	}
	
	private void saveUnitTestResults() {
		File assnDir = FileSystem.getAssnDir();
		File uniqueDir = new File(assnDir, "contiguous");
		FileSystem.createFile(uniqueDir, "unitTestResults.txt", unitTestText);
	}
	
	private void saveProgram(Program p) {
		File assnDir = FileSystem.getAssnDir();
		File uniqueDir = new File(assnDir, "contiguous");
		Tree ast = p.getAst();
		try {
			String id = p.getId();
			String json = TreeJasonizer.jsonify(ast).toString(4);
			FileSystem.createFile(uniqueDir, p.getId() + ".json", json);
			countText += id + "\t" + p.getCount() + "\n";
			unitTestText += id + "\t" + p.getUnitTestResult() + "\n";
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String [] args) {
		new IdentifyContiguous().run();
	}
	
}
