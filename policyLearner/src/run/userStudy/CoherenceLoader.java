package run.userStudy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import minions.GraphLoader;
import minions.TrajectoryLoader;
import models.trajectory.Trajectory;
import util.DirectedGraph;

public class CoherenceLoader {

	private static final String TRAJECTORY_DIR = "interpolated";
	private static final double EPSILON = 0.001;

	public static DirectedGraph<String> load() {
		System.out.println("node rank");
		// Get the number of students who visit each node.
		List<Trajectory> trajectories = TrajectoryLoader.load(TRAJECTORY_DIR);
		System.out.println("next");
		Map<String, Integer> nodeCount = new HashMap<String, Integer>();
		for(Trajectory t : trajectories) {
			List<String> visited = t.getProgramIds();
			List<String> noCycle = new ArrayList<String>();
			for(int i = 0; i < visited.size(); i++) {
				String state = visited.get(i);
				noCycle.add(state);
				int lastIndex = visited.lastIndexOf(state);
				i = lastIndex;
			}

			for(String v : noCycle) {
				if(!nodeCount.containsKey(v)) {
					nodeCount.put(v, 0);
				}
				int newCount = nodeCount.get(v) + t.getCount();
				nodeCount.put(v, newCount);
			}
		}
		
		DirectedGraph<String> roadMap = GraphLoader.loadRoadMap();

		// Make a graph where each edge has cost = coherence of the next node
		System.out.println("make coherenceCost");
		DirectedGraph<String> coherenceCost = new DirectedGraph<String>();
		for(String node : roadMap.vertexSet()) {
			double count = EPSILON;
			if(nodeCount.containsKey(node)) {
				count = nodeCount.get(node);
			}

			for(String previous : roadMap.getIncoming(node)) {

				double cost = 1.0 / (count);

				coherenceCost.addEdge(previous, node, cost);
			}
		}
		
		return coherenceCost;
	}
}
