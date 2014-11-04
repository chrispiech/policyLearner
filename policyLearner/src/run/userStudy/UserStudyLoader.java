package run.userStudy;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import util.FileSystem;
import util.Warnings;

public class UserStudyLoader {
	
	private static final int TEST_GROUP_COL = 0;
	private static final int LEVEL_ID_COL = 1;
	private static final int USER_ID_COL = 2;
	private static final int ATTEMPT_COL = 3;
	private static final int LEVEL_SOURCE_ID_COL = 5;
	private static final int HINT_ID_COL = 6;
	private static final int HINT_TXT_COL = 7;
	
	public static List<UserStudyTrajectory> load() {
		File dataDir = FileSystem.getDataDir();
		File userStudyDir = new File(dataDir, "userStudy");
		File resultsFile = new File(userStudyDir, "results.csv");
		
		Map<String, UserStudyTrajectory> trajectories = new HashMap<String, UserStudyTrajectory>();
		for(String line : FileSystem.getFileLines(resultsFile)) {
			String[] cols = line.split(",");
			if(cols.length == 0) continue;

			String levelId = cols[LEVEL_ID_COL];
			if(!levelId.equals("3")) continue;
			
			createUserIfNew(trajectories, cols);

			String levelSourceId = cols[LEVEL_SOURCE_ID_COL];
			int attempt = Integer.parseInt(cols[ATTEMPT_COL]);
			
			String userId = cols[USER_ID_COL];
			trajectories.get(userId).addAttempt(attempt, levelSourceId);
			
			// validate
			String testGroup = cols[TEST_GROUP_COL];
			String recordedGroup = trajectories.get(userId).getGroup();
			Warnings.check(recordedGroup.equals(testGroup), "wat");
		}
		
		for(String userId : trajectories.keySet()) {
			trajectories.get(userId).makeProgramIdList();
		}
		
		List<UserStudyTrajectory> culled = new ArrayList<UserStudyTrajectory>();
		for(String userId : trajectories.keySet()) {
			UserStudyTrajectory tj = trajectories.get(userId);
			if(tj.shouldKeep()) {
				culled.add(tj);
			}
		}
		
		System.out.println("original: "  + trajectories.size());
		System.out.println("culled: " + culled.size());
		return culled;
	}

	private static void createUserIfNew(Map<String, UserStudyTrajectory> tMap, String[] cols) {
		String userId = cols[USER_ID_COL];
		if(!tMap.containsKey(userId)) {
			String testGroup = cols[TEST_GROUP_COL];
			UserStudyTrajectory tj = 
					new UserStudyTrajectory(userId, testGroup);
			tMap.put(userId, tj);
		}
	}

}
