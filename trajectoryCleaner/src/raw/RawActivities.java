package raw;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import util.FileSystem;
import util.MapSorter;

public class RawActivities {

	private static final int USER_ID_INDEX = 1;
	private static final int TIME_STAMP_INDEX = 5;
	private static final int ATTEMPT_COL_INDEX = 7;
	private static final int UNIT_TEST_COL_INDEX = 9;
	private static final int SOURCE_COL_INDEX = 10;
	
	public static Map<String, List<String>> getUserAstLists(
			List<String> activities, 
			Map<String, String> xmlAstIdMap) {
		
		Map<String,List<String>> userRowMap = new HashMap<String,List<String>>();

		for(String l: activities) {
			String[] cols = l.split(",");
			String userId = cols[USER_ID_INDEX];
			if (!userRowMap.containsKey(userId)) {
				userRowMap.put(userId, new ArrayList<String>());
			}
			userRowMap.get(userId).add(l);
		}

		Map<String, List<String>> userAsts = new HashMap<String, List<String>>();
		for(String userId : userRowMap.keySet()) {
			List<String> userRows = userRowMap.get(userId);
			List<String> sortedRows = sortRows(userRows);
			List<String> astIds = getAstIds(sortedRows, xmlAstIdMap);
			userAsts.put(userId, astIds);
		}
		return userAsts;
	}
	
	private static List<String> getAstIds(
			List<String> sortedRows, 
			Map<String, String> xmlAstIdMap) {
		List<String> astIds = new ArrayList<String>();
		for(String activity : sortedRows) {
			String xmlId = getXmlId(activity);
			String astId = xmlAstIdMap.get(xmlId);
			int unitTest = getUnitTest(activity);
			astIds.add(astId);
			if(unitTest >= 100) break;
		}
		return astIds;
	}

	private static List<String> sortRows(List<String> userRows) {
		TreeMap<String, Integer> rowMap = new TreeMap<String, Integer>();
		for(String row : userRows) {
			int linuxTime = getAttempt(row);
			rowMap.put(row, linuxTime);
		}

		MapSorter<String> sorter = new MapSorter<String>();
		List<String> sorted = sorter.sortInt(rowMap);
		Collections.reverse(sorted);

		return sorted;
	}

	private static int getAttempt(String row) {
		String[] cols = row.split(",");
		return Integer.parseInt(cols[ATTEMPT_COL_INDEX]);
	}

	private static Long getLinuxTime(String row) {
		String[] cols = row.split(",");
		String timeStamp = RawData.stripQuotes(cols[TIME_STAMP_INDEX]);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			Date date = sdf.parse(timeStamp);
			long time = date.getTime();
			return time;
		} catch (ParseException e) {
			e.printStackTrace();
		}

		throw new RuntimeException("todo");
	}

	public static String getXmlId(String activity) {
		String[] cols = activity.split(",");
		return cols[SOURCE_COL_INDEX];
	}

	public static int getUnitTest(String activity) {
		String[] cols = activity.split(",");
		return Integer.parseInt(cols[UNIT_TEST_COL_INDEX]);
	}


}
