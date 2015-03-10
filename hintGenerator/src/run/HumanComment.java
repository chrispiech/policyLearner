package run;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import minions.ProgramLoader;
import models.ast.Program;
import util.FileSystem;

public class HumanComment {
	private static final String ASSN_ID = "hoc5";
	private static final String HINTS_XML_MAP = "manual.txt";

	private Set<String> done = new HashSet<String>();
	private String txt = "";

	// these two datastructures help you use shortcuts
	private TreeMap<Integer, String> shortcuts = 
			new TreeMap<Integer, String>();
	private Set<String> seenText = new HashSet<String>();

	// how many students have been covered
	double covered = 0;

	public static void main(String[] args) {
		new HumanComment().run();
	}


	private void run() {
		FileSystem.setAssnId(ASSN_ID);

		loadOldHints();

		System.out.println(txt);

		// First load all of the programs..
		List<Program> programs = ProgramLoader.loadPrograms(200);
		int totalCount = getTotalCount();

		for(Program p : programs) {
			if(!shouldSkip(p)){
				String groundTruthId = getHint(p, totalCount);
				if(groundTruthId != null) {
					txt += p.getId() + "\t" + groundTruthId + "\n";
					saveHints();
				}
			} 
			if(!p.getId().equals("0")) {
				covered += p.getCount();
			}
		}
	}


	private int getTotalCount() {
		Map<String, Integer> countMap = ProgramLoader.loadCountMap();
		int totalCount = 0;
		for(String s : countMap.keySet()) {
			if(s.equals("0")) continue;
			totalCount += countMap.get(s);
		}
		return totalCount;
	}


	private boolean shouldSkip(Program p) {
		return done.contains(p.getId());
	}


	private String getHint(Program p, int totalCount) {
		System.out.println("--------------");
		System.out.println("Shortcuts");
		for(int i : shortcuts.keySet()) {
			System.out.println("{" + i + "}\t" + shortcuts.get(i));
		}
		System.out.println("");

		System.out.println("Program:");
		System.out.print(p);
		double percentCovered = 100.0 * covered / totalCount;
		NumberFormat formatter = new DecimalFormat("#0.0");    
		System.out.println("covered: " + formatter.format(percentCovered) + "%");

		// TODO Auto-generated method stub
		System.out.print("Enter a hint: ");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try {
			String hint = br.readLine();
			if(hint.charAt(0) == '{') {
				String num = hint.substring(1, hint.length() - 1);
				int id = Integer.parseInt(num);
				String lookedUp = shortcuts.get(id);
				System.out.println(lookedUp);
				return lookedUp;
			} else if(hint.charAt(0) == '.') {
				String num = hint.substring(1, hint.length());
				int id = Integer.parseInt(num);
				String lookedUp = shortcuts.get(id);
				System.out.println(lookedUp);
				return lookedUp;
			} else {
				addHintToShortcuts(hint);
			}
			return hint;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void saveHints() {
		File hintsDir = new File(FileSystem.getAssnDir(), "hints");
		FileSystem.createFile(hintsDir, HINTS_XML_MAP, txt);
	}


	private void loadOldHints() {
		File hintsFile = getHintsFile();
		List<String> lines = FileSystem.getFileLines(hintsFile);
		txt = "";
		if(lines != null) {
			for(String line : lines) {
				String[] elems = line.split("\t");
				String key = elems[0].trim();
				String hint = elems[1].trim();
				addHintToShortcuts(hint);
				done.add(key);
				txt += line + "\n";
			}
		}
	}


	private void addHintToShortcuts(String hint) {
		if(!seenText.contains(hint)) {
			shortcuts.put(shortcuts.size(), hint);
			seenText.add(hint);
		}
	}


	private File getHintsFile() {
		File hintsDir = new File(FileSystem.getAssnDir(), "hints");
		hintsDir.mkdirs();
		File hintsFile = new File(hintsDir, HINTS_XML_MAP);
		return hintsFile;
	}
}
