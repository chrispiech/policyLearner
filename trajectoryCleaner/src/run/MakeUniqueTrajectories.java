package run;

import java.io.File;
import java.util.*;

import minions.TrajectoryLoader;
import models.trajectory.Trajectory;
import util.FileSystem;
import util.IdCounter;
import util.MapSorter;

public class MakeUniqueTrajectories {

	private static final String MAIN_ASSN_ID = "hoc4";
	
	private static final String AST_DIR = "unique";
	private static final String ORIGINAL_DIR = "trajectories";
	private static final String UNIQUE_DIR = "uniqueTrajectories";

	private void run() {
		System.out.println("loading");
		List<Trajectory> trajectories = TrajectoryLoader.load(ORIGINAL_DIR, AST_DIR);
		Map<List<String>, Integer> uniques = new HashMap<List<String>, Integer>();
		System.out.println("compressing");
		for(Trajectory t : trajectories) {
			List<String> states = t.getProgramIds();
			if(!uniques.containsKey(states)) {
				uniques.put(states, 0);
			}
			int newCount = uniques.get(states) + 1;
			uniques.put(states, newCount);
		}
		System.out.println("original size: " + trajectories.size());
		System.out.println("uniques size: " + uniques.size());
		
		File assnDir = FileSystem.getAssnDir();
		File uniqueTrajDir = new File(assnDir, UNIQUE_DIR);
		
		List<List<String>> sorted = new MapSorter<List<String>>().sortInt(uniques);
		Map<List<String>, String> idMap = new HashMap<List<String>, String>();
		IdCounter idCounter = new IdCounter();
		String countsStr = "";
		for(List<String> list : sorted) {
			String fileContents = getFileString(list);
			int count = uniques.get(list);
			int id = idCounter.getNextId();
			String fileName = id + ".txt";
			FileSystem.createFile(uniqueTrajDir, fileName, fileContents);
			countsStr += id + "\t" + count + "\n";
			idMap.put(list, ""+id);
		}

		String idMapTxt = "";
		for(Trajectory t : trajectories) {
			List<String> states = t.getProgramIds();
			String oldId = t.getId();
			String newId = idMap.get(states);
			idMapTxt += oldId + "," + newId + "\n";
		}
		FileSystem.createFile(uniqueTrajDir, "counts.txt", countsStr);
		FileSystem.createFile(uniqueTrajDir, "idMap.txt", idMapTxt);
	}
	
	public String getFileString(List<String> states) {
		String str = "";
		for(int i = 0; i < states.size(); i++) {
			str += states.get(i);
			if(i != states.size() - 1) {
				str += "\n";
			}
		}
		return str;
	}
	
	public static void main(String[] args) {
		FileSystem.setAssnId(MAIN_ASSN_ID);
		new MakeUniqueTrajectories().run();
	}


}
