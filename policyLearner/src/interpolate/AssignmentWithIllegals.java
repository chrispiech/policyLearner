package interpolate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import models.graphs.TransitionGraph;
import models.trajectory.Trajectory;
import models.trajectory.TrajectoryNode;
import util.DirectedGraph;

public class AssignmentWithIllegals extends Assignment {

	private DirectedGraph<String> assnGraph = new DirectedGraph<String>();
	private TransitionGraph transGraph;
	private DirectedGraph<String> roadMap;
	private Map<String, List<String>> unseenPathMap;


	public AssignmentWithIllegals(
			TransitionGraph transGraph, 
			DirectedGraph<String> roadMap,
			Map<String, List<String>> unseenPathMap) {
		this.transGraph = transGraph;
		this.roadMap = roadMap;
		this.unseenPathMap = unseenPathMap;
		for(String start : roadMap.vertexSet()) {
			for(String next : roadMap.getOutgoing(start)) {

				int numStudents = transGraph.getTransitions(start, next).size();
				String edgeKey = start + ":" + next;
				if(this.unseenPathMap.containsKey(edgeKey)) {
					List<String> unseenStates = unseenPathMap.get(edgeKey);
					List<String> path = new ArrayList<String>();
					path.add(start);
					path.addAll(unseenStates);
					path.add(next);
					for(int i = 1; i < path.size(); i++) {
						String a = path.get(i - 1);
						String b = path.get(i);
						assnGraph.increaseWeight(a, b, numStudents);
					}
				} else {
					assnGraph.increaseWeight(start, next, numStudents);
				}

			}
		}
	}

	public int getTransCount(String a, String b, String type) {
		DirectedGraph<String> g = assnGraph;
		return (int) g.getWeight(a, b);
	}

	public int getTransCountBayes(String a, String b, String type) {
		DirectedGraph<String> g = assnGraph;
		return (int) g.getWeight(a, b) + 1;
	}


	public Set<String> vertexSet() {
		return assnGraph.vertexSet();
	}


}
