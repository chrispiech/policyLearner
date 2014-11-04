package run.evaluation;

import java.util.*;

import util.DirectedGraph;
import util.Edge;
import util.FileSystem;
import minions.GraphLoader;
import minions.TrajectoryLoader;
import minions.UnseenPathMinion;
import models.trajectory.Trajectory;

public class SanityCheck {

	private static final String TRAJECTORY_DIR = "interpolated";
	private DirectedGraph<String> roadMap;

	private void run() {
		FileSystem.setAssnId("hoc18");
		
		roadMap = GraphLoader.loadRoadMap();
		
		Map<String, List<String>> unseenPathMap = UnseenPathMinion.loadPathMap();
		for(String key : unseenPathMap.keySet()) {
			String[] pair = key.split(":");
			List<String> pathList = new ArrayList<String>();
			pathList.add(pair[0]);
			pathList.addAll(unseenPathMap.get(key));
			pathList.add(pair[1]);
			for(int i = 1; i < pathList.size(); i++) {
				String a = pathList.get(i - 1);
				String b = pathList.get(i);
				if(!roadMap.containsEdge(a, b)) {
					throw new RuntimeException(a + " " + b);
				} 
			}
		}
		System.out.println("sanity check on unseen paths passed");
		
		int done = 0;
		int weird = 0;
		int total = 0;
		List<Trajectory> trajectories = TrajectoryLoader.load(TRAJECTORY_DIR);
		for(Trajectory t : trajectories) {
			for(Edge<String> edge : t.getEdges()) {
				String start = edge.getSource();
				String end = edge.getTarget();
				if(!roadMap.containsEdge(start, end)) {
					//System.out.println("Fail: " + start + "->" + end);
					//throw new RuntimeException("no " + start + " -> " + end);
					weird += t.getCount();
				}
				total += t.getCount();
			}
			if(++done % 100 == 0) System.out.println(done);
		}
		
		System.out.println("sanity check on interpolated passed...");
		System.out.println(100.0 * weird / total);
	}
	
	public static void main(String[] args) {
		new SanityCheck().run();
	}

}
