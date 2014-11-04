package run.vis;

import java.io.File;
import java.util.*;

import org.apache.commons.math3.util.Pair;
import org.json.JSONObject;

import util.DirectedGraph;
import util.Edge;
import util.FileSystem;
import minions.GraphLoader;
import minions.TrajectoryLoader;
import models.trajectory.Trajectory;

public class MakeGroundTruthJson {
	
	
	private void run() {
		FileSystem.setAssnId("hoc3");
		
		// the important data
		
		DirectedGraph<String> truth = GraphLoader.loadGroundTruth();
		
		String json = "";
		json += "{\n";
		json += "	\"edges\" : [\n";
		
		json += getTruthJson(truth);
		
		json += "	]\n";
		json += "}\n";
		
		File outFile = new File("/Users/cpiech/Documents/Research/policyLearner/demo/groundTruth.json");
		
		FileSystem.createFile(outFile, json);
	}

	private String getTruthJson(DirectedGraph<String> truth) {
		File theFile = new File("/Users/cpiech/Documents/Research/policyLearner/demo/nodes.json");
		JSONObject obj = new JSONObject(FileSystem.getFileContents(theFile));
		Set<String> nodes = new HashSet<String>();
		Iterator<?> keys = obj.keys();
        while( keys.hasNext() ){
            String key = (String)keys.next();
            nodes.add(key);
        }
		
		
		String truthJson = "";
		for(Edge<String> edge : truth.getEdges()) {
			
			if(!nodes.contains(edge.getSource())) continue;
			if(!nodes.contains(edge.getTarget())) continue;
			//if(Integer.parseInt(edge.getSource()) > 55) continue;
			
			if(!truthJson.isEmpty()) {
				truthJson += ",\n";
			}
			truthJson += "[" + edge.getSource() + "," + edge.getTarget() + "]";
			
		}
		return truthJson + "\n";
	}

	
	public static void main(String[] args) {
		new MakeGroundTruthJson().run();
	}

	
}
