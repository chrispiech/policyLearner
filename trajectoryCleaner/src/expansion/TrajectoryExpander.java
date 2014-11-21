package expansion;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.ast.Tree;
import models.trajectory.Trajectory;
import raw.RawActivities;
import raw.RawData;
import util.FileSystem;
import util.IdCounter;
import util.MapSorter;

public class TrajectoryExpander {

	private RawData raw = null;
	private Map<String, String> xmlAstIdMap = null;

	private Map<List<String>, Integer> trajectoryCounts = null;
	private List<List<String>> sorted = null;
	private Map<String, List<String>> userTrajLists = null;
	private Map<List<String>, String> trajIdMap = null;

	public TrajectoryExpander(RawData raw, Map<String, String> xmlAstIdMap) {
		this.raw = raw;
		this.xmlAstIdMap = xmlAstIdMap;
	}

	public void expandTrajectories() {
		makeUserAstLists();
		makeTrajectoryCounts();
		sortTrajectories();
		saveTrajectories();
		saveTrajectoryCounts();
		saveUserMap();
	}

	private void saveTrajectoryCounts() {
		System.out.println("save trajectory counts...");
		String countsStr = "";
		for(List<String> list : sorted) {
			String id = trajIdMap.get(list);
			int count = trajectoryCounts.get(list);
			countsStr += id + "\t" + count + "\n";
		}
		FileSystem.createFile(getTrajDir(), "counts.txt", countsStr);
	}

	private void saveTrajectories() {
		System.out.println("saving trajectories...");
		int done = 0;
		for(List<String> list : sorted) {
			String fileContents = getTrajectoryFileString(list);
			String id = trajIdMap.get(list);
			String fileName = id + ".txt";
			FileSystem.createFile(getTrajDir(), fileName, fileContents);
			if(++done % 100 == 0) System.out.println(done);
		}
	}

	private void makeTrajectoryCounts() {
		trajectoryCounts = new HashMap<List<String>, Integer>();
		for(String s : userTrajLists.keySet()) {
			List<String> states = userTrajLists.get(s);
			if(states.isEmpty()) continue;
			if(!trajectoryCounts.containsKey(states)) {
				trajectoryCounts.put(states, 0);
			}
			int newCount = trajectoryCounts.get(states) + 1;
			trajectoryCounts.put(states, newCount);
		}
	}

	private void sortTrajectories() {
		sorted = new MapSorter<List<String>>().sortInt(trajectoryCounts);
		trajIdMap = new HashMap<List<String>, String>();
		IdCounter idCounter = new IdCounter();
		for(List<String> list : sorted) {
			String id = idCounter.getNextIdStr();
			trajIdMap.put(list, id);
		}
	}
	
	private void saveUserMap() {
		System.out.println("save trajectory idMap...");
		File f = new File(getTrajDir(), "idMap.txt");
		FileWriter file = null;
		try {
			file = new FileWriter(f);
			int done = 0;
			for(String userId : userTrajLists.keySet()) {
				List<String> states = userTrajLists.get(userId);
				String newId = trajIdMap.get(states);
				if(newId != null) {
					file.write(userId + "," + newId + "\n");
				}
				if(++done % 100 == 0) System.out.println(done);
			}
			file.flush();
			file.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}

	private File getTrajDir() {
		return new File(FileSystem.getAssnDir(), "trajectories");
	}
	
	private void makeUserAstLists() {
		System.out.println("get user ast lists..."); // takes a long time...
		userTrajLists = RawActivities.getUserAstLists(raw.getActivities(), xmlAstIdMap);
	}

	public String getTrajectoryFileString(List<String> states) {
		String str = "";
		for(int i = 0; i < states.size(); i++) {
			str += states.get(i);
			if(i != states.size() - 1) {
				str += "\n";
			}
		}
		return str;
	}

}
