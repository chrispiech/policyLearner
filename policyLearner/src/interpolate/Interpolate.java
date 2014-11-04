package interpolate;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import minions.BlockyStringParser;
import minions.GraphLoader;
import minions.ProgramLoader;
import minions.TrajectoryLoader;
import minions.TrajectorySaver;
import minions.UnseenPathMinion;
import models.ast.Program;
import models.ast.Tree;
import models.graphs.EditGraph;
import models.graphs.TransitionGraph;
import models.trajectory.Trajectory;
import models.trajectory.TrajectoryNode;
import util.DirectedGraph;
import util.Edge;
import util.FileSystem;
import util.IdCounter;

public class Interpolate {

	private static final String MAIN_ASSN_ID = "hoc3";
	private static final String OUTPUT_DIR = "interpolate2";
	
	private HashMap<String, Program> programMap;
	private DirectedGraph<String> roadMap;
	private List<Trajectory> trajectories;
	private TransitionGraph transGraph;
	

	public static List<Trajectory> interp(List<Trajectory> originals) {
		return new Interpolate().interpolate(originals);
	}
	
	private List<Trajectory> interpolate(List<Trajectory> originals) {
		List<Program> programs = ProgramLoader.loadPrograms("asts", 99);
		programMap = new HashMap<String, Program>();
		for(Program p : programs) {
			programMap.put(p.getId(), p);
		}
		roadMap = GraphLoader.loadRoadMap();
		trajectories = cullTrajectories(originals);
		transGraph = new TransitionGraph(trajectories);
		Assignment assn = new Assignment(transGraph, roadMap);
		EdgeProbabilities edges = new EdgeProbabilities(assn, roadMap);
		
		Map<String, List<String>> cache = new HashMap<String, List<String>>();
		List<Trajectory> tjs = new ArrayList<Trajectory>();
		Map<List<String>, List<Trajectory>> uniqueInterps = 
				new HashMap<List<String>, List<Trajectory>>();
		for(Trajectory original : trajectories) {
			Trajectory newTrajectory = getInterpolated(edges, cache, original);
			tjs.add(newTrajectory);
			if(!uniqueInterps.containsKey(newTrajectory.getProgramIds())) {
				uniqueInterps.put(newTrajectory.getProgramIds(), new ArrayList<Trajectory>());
			}
			List<Trajectory> list = uniqueInterps.get(newTrajectory.getProgramIds());
			list.add(newTrajectory);
		}

		IdCounter counter = new IdCounter();
		List<Trajectory> compressedTrajectories = new ArrayList<Trajectory>();
		for(List<String> idList : uniqueInterps.keySet()) {
			String newId = counter.getNextId() + "";
			List<Trajectory> trajList = uniqueInterps.get(idList);
			int count = 0;
			for(Trajectory t : trajList) {
				count += t.getCount();
			}
			
			Trajectory newT = new Trajectory(newId, idList, count);
			compressedTrajectories.add(newT);
		}
		
		return compressedTrajectories;
		
	}
	
	private void run() {
		loadData();
		Assignment assn = new Assignment(transGraph, roadMap);
		EdgeProbabilities edges = new EdgeProbabilities(assn, roadMap);
		//new Assignment(transGraph, roadMap, edges);

		Map<String, List<String>> cache = new HashMap<String, List<String>>();
		List<Trajectory> tjs = new ArrayList<Trajectory>();
		Map<List<String>, List<Trajectory>> uniqueInterps = 
				new HashMap<List<String>, List<Trajectory>>();
		System.out.println("size: " +trajectories.size());
		for(Trajectory original : trajectories) {
			Trajectory newTrajectory = getInterpolated(edges, cache, original);
			//System.out.println("-----");
			//System.out.println("original:\t" + original);
			//System.out.println("new: \t" + newTrajectory);
			tjs.add(newTrajectory);
			if(!uniqueInterps.containsKey(newTrajectory.getProgramIds())) {
				uniqueInterps.put(newTrajectory.getProgramIds(), new ArrayList<Trajectory>());
			}
			List<Trajectory> list = uniqueInterps.get(newTrajectory.getProgramIds());
			list.add(newTrajectory);
		}

		String idMapTxt = "uniqueTrajectoryId, interpId\n";
		IdCounter counter = new IdCounter();
		List<Trajectory> compressedTrajectories = new ArrayList<Trajectory>();
		for(List<String> idList : uniqueInterps.keySet()) {
			String newId = counter.getNextId() + "";
			List<Trajectory> trajList = uniqueInterps.get(idList);
			int count = 0;
			for(Trajectory t : trajList) {
				count += t.getCount();
			}
			
			for(Trajectory t : trajList) {
				idMapTxt += t.getId() + "," + newId + "\n";
			}
			
			Trajectory newT = new Trajectory(newId, idList, count);
			compressedTrajectories.add(newT);
		}

		TrajectorySaver.save(compressedTrajectories, OUTPUT_DIR, idMapTxt);
	}

	/**
	 * Get Interpolated
	 * ------------------
	 * After calculating transition probabilities... and assignments..
	 * turn those back into interpolated trajectories.
	 */
	private Trajectory getInterpolated(EdgeProbabilities transProb,
			Map<String, List<String>> cache, Trajectory tj) {
		List<String> tempList = new ArrayList<String>();

		List<TrajectoryNode> nodeList = tj.getNodeList();
		tempList.add(nodeList.get(0).getProgramId());
		for(int i = 1; i < nodeList.size(); i++) {
			String a = nodeList.get(i - 1).getProgramId();
			String b = nodeList.get(i).getProgramId();

			if(!b.equals(a)) {
				List<String> interpPath = 
						getInterpPath(transProb, cache, a, b);

				tempList.addAll(interpPath);
			}

		}

		// remove all duplicates
		List<String> reducedList = new ArrayList<String>();
		for(String x : tempList) {
			if(reducedList.isEmpty()) {
				reducedList.add(x);
			} else {
				String last = reducedList.get(reducedList.size() - 1);
				if(!last.equals(x)) {
					reducedList.add(x);
				}
			}
		}


		int count = tj.getCount();
		Trajectory newTj = new Trajectory(tj.getId(), reducedList, count);
		return newTj;
	}

	/**
	 * Get Interp Path
	 * ---------------
	 * Not sure yet...
	 */
	private List<String> getInterpPath(EdgeProbabilities transProb,
			Map<String, List<String>> cache, String a, String b) {
		String key = a + ":" + b + ":" + "";
		List<String> interpPath = null;
		if(cache.containsKey(key)) {
			interpPath = cache.get(key);
		} else {
			List<Edge<String>> subPath = 
					transProb.getMostLikelyPath(a, b);
			interpPath = new ArrayList<String>();
			interpPath.add(a);
			for(Edge<String> edge : subPath) {
				interpPath.add(edge.getTarget());
			}
			cache.put(key, interpPath);
		}
		return interpPath;
	}

	/**
	 * Load Data
	 * ---------
	 * Get everything you need!
	 */
	private void loadData() {

		System.out.println("load programs: ");
		// We only need a few programs because we only use them to find the start state!
		List<Program> programs = ProgramLoader.loadPrograms("asts", 100);
		programMap = new HashMap<String, Program>();
		for(Program p : programs) {
			programMap.put(p.getId(), p);
		}

		System.out.println("loading road map");
		roadMap = GraphLoader.loadRoadMap();

		System.out.println("load trajectories: ");
		loadTrajectories();

		transGraph = new TransitionGraph(trajectories);
		
		
	}

	/**
	 * Load Trajectories
	 * -----------------
	 * Loads trajectories...
	 * and culls out ones that don't work with the road map.
	 */
	private void loadTrajectories() {
		List<Trajectory> originalTrajectories = 
				TrajectoryLoader.load("trajectories", "asts");
		
		trajectories = cullTrajectories(originalTrajectories);
	}

	/**
	 * Cull Trajectories
	 * -----------------
	 * Removes the empty trajectory
	 * Removes trajectories that are not perfect...
	 * Removes any trajectories that have edges not connected in the road map... 
	 * Removes any non-contiguous asts
	 * Adds in the empty program as a start state
	 */
	private List<Trajectory> cullTrajectories(
			List<Trajectory> originalTrajectories) {
		String startId = getStartId();

		int originalCount = 0;
		int newCount = 0;
		int done = 0;

		List<Trajectory> trajectories = new ArrayList<Trajectory>();
		for(Trajectory t : originalTrajectories) {
			if(!t.perfect()) continue;
			if(t.getNodeList().size() == 0) continue;
			List<String> nodeList = getNewNodeList(startId, t);
			originalCount += t.getCount();
			if(nodeList != null) {
				Trajectory withStart = new Trajectory(t.getId(), nodeList, 
						 t.getCount());
				trajectories.add(withStart);
				newCount += t.getCount();
			}
			done += 1;
			//if(done % 100 == 0) {
			//	System.out.println(done);
			//}

		}
		//System.out.println("students before cull: " + originalCount);
		//System.out.println("students after cull: " + newCount);
		return trajectories;
	}

	/**
	 * Get New Node List
	 * -----------------
	 * Creates a list of nodeIds for the new trajectory (with the startId).
	 * Also checks if each edge exists in the road map.
	 */
	private List<String> getNewNodeList(String startId, Trajectory t) {
		List<String> nodeList = new ArrayList<String>();
		nodeList.add(startId);
		String secondId = t.getNodeList().get(0).getProgramId();
		nodeList.add(secondId);
		if(!roadMap.hasPath(startId, secondId)) return null;
		for(int i = 1; i < t.getNodeList().size(); i++) {
			String a = t.getNodeList().get(i - 1).getProgramId();
			String b = t.getNodeList().get(i).getProgramId();
			if(!roadMap.hasPath(a, b)) {
				return null;
			}
			nodeList.add(b);
		}
		return nodeList;
	}

	/**
	 * Get Start ID
	 * ------------
	 * Finds the start ID that all students begin on. Assumes that
	 * all students start with the empty program!
	 */
	private String getStartId() {
		String assnId = FileSystem.getAssnId();
		Tree startAst = BlockyStringParser.parseBlockyString("");
		for(String programId : programMap.keySet()) {
			Program p = programMap.get(programId);
			if(p.getAst().equals(startAst)) {
				return p.getId();
			}
		}
		throw new RuntimeException("are you sure thats the start?");
	}

	/**
	 * Load Road Map
	 * -------------
	 * Loads the road map from file.
	 */
	private DirectedGraph<String> loadRoadMap(File roadMapFile) {
		DirectedGraph<String> roadMap = new DirectedGraph<String>();
		for(String line : FileSystem.getFileLines(roadMapFile)) {
			String[] cols = line.split(",");
			String a = cols[0];
			String b = cols[1];
			roadMap.addEdge(a, b);	
		}
		return roadMap;
	}

	public static void main(String[] args) {
		FileSystem.setAssnId(MAIN_ASSN_ID);
		new Interpolate().run();
	}
}
