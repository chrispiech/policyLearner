package run;

import java.util.*;

import util.FileSystem;
import minions.ProgramLoader;
import minions.ProgramSaver;
import models.ast.Program;

public class MakeUniqueAsts {
	
	private static final String ASSN_ID = "hoc4";	
	private static final String XML_DIR = "xml";

	public List<Program> run() {
		
		FileSystem.setAssnId(ASSN_ID);
		System.out.println("loading programs...");
		List<Program> programs = ProgramLoader.loadPrograms(XML_DIR);
		
		Map<Program, List<Program>> programMap = new HashMap<Program, List<Program>>();

		System.out.println("sorting programs...");
		for(Program p : programs) {
			if(!programMap.containsKey(p)) {
				programMap.put(p, new ArrayList<Program>());
			} 
			programMap.get(p).add(p);
		}
		return ProgramSaver.saveUniques(programMap, "unique");
	}

	public static void main(String [] args) {
		
		new MakeUniqueAsts().run();
	}

}
