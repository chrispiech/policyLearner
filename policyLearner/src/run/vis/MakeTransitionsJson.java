package run.vis;

import java.io.File;
import java.util.*;

import org.apache.commons.math3.util.Pair;
import org.json.JSONObject;

import util.Edge;
import util.FileSystem;
import minions.TrajectoryLoader;
import models.trajectory.Trajectory;

public class MakeTransitionsJson {
	
	class MoveCounter {
		public Map<String, Integer> moveCounts = 
				new HashMap<String, Integer>();
		
		public void add(String s, String t, int count) {
			String key = s + ":" + t;
			if(!moveCounts.containsKey(key)) {
				moveCounts.put(key, 0);
			}
			int newCount = moveCounts.get(key) + count;
			moveCounts.put(key, newCount);
		}
		
		
	}
	
	private void run() {
		FileSystem.setAssnId("hoc3");
		
		// the important data
		
		
		File theFile = new File("/Users/cpiech/Documents/Research/policyLearner/demo/nodes.json");
		JSONObject obj = new JSONObject(FileSystem.getFileContents(theFile));
		Set<String> nodes = new HashSet<String>();
		Iterator<?> keys = obj.keys();
        while( keys.hasNext() ){
            String key = (String)keys.next();
            nodes.add(key);
        }
		
		List<Trajectory> trajectories = 
				TrajectoryLoader.load("interpolated", "contiguous");
		
		Map<Integer, MoveCounter> timeMap = new HashMap<Integer, MoveCounter>();
		for(Trajectory t : trajectories) {
			int count = t.getCount();
			List<Edge<String>> edges = t.getEdges();
			for(int i = 0; i < edges.size(); i++) {
				Edge<String> edge = edges.get(i);
				String s = edge.getSource();
				String x = edge.getTarget();
				
				if(!nodes.contains(s)) continue;
				if(!nodes.contains(x)) continue;
				if(s.equals(x)) continue;
				
				if(!timeMap.containsKey(i)) {
					timeMap.put(i, new MoveCounter());
				}
				MoveCounter mc = timeMap.get(i);
				
				mc.add(s, x, count);
			}
		}
		String timeJson = "{\n";
		timeJson += makeTimeJson(timeMap);
		timeJson += "}";
		
		File outFile = new File("/Users/cpiech/Documents/Research/policyLearner/demo/interpolated.json");
		
		FileSystem.createFile(outFile, timeJson);
	}

	private String makeTimeJson(Map<Integer, MoveCounter> timeMap) {
		String timeJson = "";
		for(int i : timeMap.keySet()) {
			MoveCounter mc = timeMap.get(i);
			
			if(!timeJson.isEmpty()) {
				timeJson += ",\n";
			}
			timeJson += "\"" +i + "\": [\n";
			timeJson += makeListJson(mc);
			timeJson += "]";
			
		}
		return timeJson + "\n";
	}

	private String makeListJson(MoveCounter mc) {
		String listJson = "";
		for(String edgeKey : mc.moveCounts.keySet()) {
			String[] transIds = edgeKey.split(":");
			String first = transIds[0];
			String second = transIds[1];
			int count = mc.moveCounts.get(edgeKey);
			if(!listJson.isEmpty()) {
				listJson += ",\n";
			}
			String line = "[" + first + ", " + second + ", " + count + "]";
			listJson += line;
		}
		return listJson + "\n";
	}
	
	public static void main(String[] args) {
		new MakeTransitionsJson().run();
	}

	
}
