package interpolate;

import java.util.List;

import util.DirectedGraph;
import util.Edge;

public class EdgeProbabilities {

	private DirectedGraph<String> probGraph = new DirectedGraph<String>();
	private DirectedGraph<String> roadMap;
	
	public EdgeProbabilities(Assignment assn, DirectedGraph<String> roadMap) {
		this.roadMap = roadMap;
		for(String start : roadMap.vertexSet()) {
			calcOutgoingProbabilities(assn, "", probGraph, start);
		}
	}

	public List<Edge<String>> getMostLikelyPath(String start, String end) {
		DirectedGraph<String> graph = probGraph;
		return graph.getShortestPath(start, end);
	}

	private void calcOutgoingProbabilities(Assignment assn, String result,
			DirectedGraph<String> graph, String start) {
		double sum = 0;
		for(String next : roadMap.getOutgoing(start)) {
			int trans = assn.getTransCountBayes(start, next, result);
			sum += trans;
		}
		for(String next : roadMap.getOutgoing(start)) {
			int trans = assn.getTransCountBayes(start, next, result);
			double prob = trans / sum;
			double cost = -Math.log(prob);
			graph.addEdge(start, next, cost);
		}
	}


	public double getNegLogProb(String a, String b, String type) {
		DirectedGraph<String> graph = probGraph;
		return graph.getWeight(a, b);
	}
}
