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

public class InterpolateSept {

	private static final String MAIN_ASSN_ID = "hoc3";
	private static final String OUTPUT_DIR = "interpSept";
	private static final double EPSILON = 1e-6;

	private HashMap<String, Program> programMap;
	private DirectedGraph<String> roadMap;
	private List<Trajectory> trajectories;
	
	Map<String, List<String>> cache = new HashMap<String, List<String>>();


	private void run() {
		loadData();

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

		// Make a graph where each edge has cost = coherence of the next node
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

		int done = 0;
		List<Trajectory> interp = new ArrayList<Trajectory>();
		for(Trajectory original : trajectories) {
			Trajectory newTrajectory = getInterpolated(original, coherenceCost);
			interp.add(newTrajectory);
			if(++done % 100 == 0) System.out.println(done);
		}
		
		saveTrajectories(interp);
	}


	private void saveTrajectories(List<Trajectory> interp) {
		System.out.println("making unique...");
		Map<List<String>, List<Trajectory>> uniqueInterps = 
				new HashMap<List<String>, List<Trajectory>>();
		for(Trajectory t : interp) {
			if(!uniqueInterps.containsKey(t.getProgramIds())) {
				uniqueInterps.put(t.getProgramIds(), new ArrayList<Trajectory>());
			}
			List<Trajectory> list = uniqueInterps.get(t.getProgramIds());
			list.add(t);
		}
		
		
		System.out.println("making idMap");
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

		System.out.println("saving...");
		TrajectorySaver.save(compressedTrajectories, OUTPUT_DIR, idMapTxt);
		
	}


	private Trajectory getInterpolated(Trajectory t,
			DirectedGraph<String> edgeCost) {
		List<String> tempList = new ArrayList<String>();

		List<TrajectoryNode> nodeList = t.getNodeList();
		tempList.add(nodeList.get(0).getProgramId());
		for(int i = 1; i < nodeList.size(); i++) {
			String a = nodeList.get(i - 1).getProgramId();
			String b = nodeList.get(i).getProgramId();

			if(!b.equals(a)) {
				List<String> interpPath = getInterpPath(edgeCost, a, b);

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

		int count = t.getCount();
		Trajectory newTj = new Trajectory(t.getId(), reducedList, count);
		return newTj;
	}


	private List<String> getInterpPath(DirectedGraph<String> edgeCost,
			String a, String b) {
		
		String key = a + ":" + b + ":" + "";
		List<String> interpPath = null;
		if(cache.containsKey(key)) {
			interpPath = cache.get(key);
		} else {
			
			List<Edge<String>> path = edgeCost.getShortestPath(a, b);
			interpPath = new ArrayList<String>();
			
			for(Edge<String> e : path) {
				interpPath.add(e.getSource());
				interpPath.add(e.getTarget());
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
		List<Program> programs = ProgramLoader.loadPrograms("contiguous", 100);
		programMap = new HashMap<String, Program>();
		for(Program p : programs) {
			programMap.put(p.getId(), p);
		}

		System.out.println("loading road map");
		roadMap = GraphLoader.loadRoadMap();

		System.out.println("load trajectories: ");
		loadTrajectories();


	}

	/**
	 * Load Trajectories
	 * -----------------
	 * Loads trajectories...
	 * and culls out ones that don't work with the road map.
	 */
	private void loadTrajectories() {
		List<Trajectory> originalTrajectories = 
				TrajectoryLoader.load("trajectories", "contiguous");

		//System.out.println("culling...");
		trajectories = cullTrajectories(originalTrajectories);
	}

	/**
	 * Cull Trajectories
	 * -----------------
	 * Removes any students who don't eventually get a perfect answer.
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
			List<String> nodeList = getNewNodeList(startId, t);
			originalCount += t.getCount();
			if(nodeList != null) {
				Trajectory withStart = new Trajectory(t.getId(), nodeList, 
					t.getCount());
				trajectories.add(withStart);
				newCount += t.getCount();
			}
			done += 1;
			if(done % 100 == 0) {
				System.out.println(done);
			}

		}
		System.out.println("students before cull: " + originalCount);
		System.out.println("students after cull: " + newCount);
		return trajectories;
	}

	/**
	 * Get New Node List
	 * -----------------
	 * Creates a list of nodeIds for the new trajectory (with the startId).
	 */
	private List<String> getNewNodeList(String startId, Trajectory t) {
		List<String> nodeList = new ArrayList<String>();
		nodeList.add(startId);
		
		for(int i = 0; i < t.getNodeList().size(); i++) {
			String b = t.getNodeList().get(i).getProgramId();
			if(!roadMap.containsVertex(b)) return null;
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
		Tree startAst = BlockyStringParser.parseBlockyString("");
		System.out.println(startAst);
		for(String programId : programMap.keySet()) {
			Program p = programMap.get(programId);
			if(p.getAst().equals(startAst)) {
				return p.getId();
			}
		}
		throw new RuntimeException("are you sure thats the start?");
	}

	public static void main(String[] args) {
		FileSystem.setAssnId(MAIN_ASSN_ID);
		new InterpolateSept().run();
	}
}
