package interpolate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import models.graphs.TransitionGraph;
import models.trajectory.Trajectory;
import models.trajectory.TrajectoryNode;
import util.DirectedGraph;
import util.Edge;

public class Assignment {

	private DirectedGraph<String> assnGraph = new DirectedGraph<String>();
	private TransitionGraph transGraph;
	private DirectedGraph<String> roadMap;

	public Assignment() {}
	
	public Assignment(TransitionGraph transGraph,
			DirectedGraph<String> roadMap, 
			EdgeProbabilities t) {

		/*this.transGraph = transGraph;
		this.roadMap = roadMap;
		this.unseenPathMap = unseenPathMap;

		DirectedGraph<String> graph = assnGraph;
		for(String start : roadMap.vertexSet()) {
			for(String next : transGraph.getNextStates(start)) {
				updateGraph(t, graph, start, next);
			}
		}*/
		throw new RuntimeException("depricated...");
	}

	public Assignment(
			TransitionGraph transGraph, 
			DirectedGraph<String> roadMap) {
		this.transGraph = transGraph;
		this.roadMap = roadMap;
		for(String start : roadMap.vertexSet()) {
			for(String next : roadMap.getOutgoing(start)) {
				
				int numStudents = transGraph.getTransitions(start, next).size();

				assnGraph.increaseWeight(start, next, numStudents);

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

	private void updateGraph(EdgeProbabilities t, 
			DirectedGraph<String> graph, String start, String next) {
		List<TrajectoryNode> nodes = transGraph.getTransitions(start, next);
		int count = nodes.size();
		if(count == 0) return;
		if(roadMap.containsEdge(start, next)) {
			graph.increaseWeight(start, next, count);
		} else {
			List<Edge<String>> path = t.getMostLikelyPath(start, next);
			if(path == null) return;
			for(Edge<String> e : path) {
				String source = e.getSource();
				String target = e.getTarget();
				graph.increaseWeight(source, target, count);
			}
			//System.out.println(start + ": " + next);
			//System.out.println(pathStr);
			//System.out.println("");
		}
	}


	public Set<String> vertexSet() {
		return assnGraph.vertexSet();
	}


}
