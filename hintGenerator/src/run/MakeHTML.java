package run;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import minions.ProgramLoader;
import models.ast.Program;
import models.ast.Tree;
import util.FileSystem;

public class MakeHTML {
	private static final String ASSN_ID = "hoc1";
	private static final String HINTS_XML_MAP = "manual.txt";

	public static void main(String[] args) {
		new MakeHTML().run();
	}


	private void run() {
		FileSystem.setAssnId(ASSN_ID);

		List<Program> programs = ProgramLoader.loadPrograms(200);
		Map<String, Program> treeMap = new HashMap<String, Program>();
		for(Program p : programs) {
			treeMap.put(p.getId(), p);
		}
		
		
		File hintsFile = getHintsFile();
		List<String> lines = FileSystem.getFileLines(hintsFile);
		String html = getHeader();
		for(String line : lines) {
			String[] cols = line.split("\t");
			String astId = cols[0];
			Tree ast = treeMap.get(astId).getAst();
			String hint = cols[1];
			html += "<tr>";
			html += "<td>"+astId+ "</td>";
			html += "<td>" + getAstHtml(ast) + "</td>";
			html += "<td>" + getHintHTML(hint) + "</td>";

			html += "<td>"+treeMap.get(astId).getCount() + "</td>";
			html += "</tr>";
		}
		html += getFooter();	
		File hintsDir = new File(FileSystem.getAssnDir(), "hints");
		FileSystem.createFile(hintsDir, "manual.html", html);
		System.out.println("done");
		
	}


	private String getHintHTML(String hint) {
		hint = hint.replace("<", "&lt;");
		hint = hint.replace(">", "&gt;");
		return hint;
	}


	private String getAstHtml(Tree ast) {
		String astStr = ast.toString();
		astStr = astStr.replace("\n", "<br/>");
		astStr = astStr.replace(" ", "&nbsp&nbsp&nbsp");
		return astStr;
	}


	private String getFooter() {
		return "</body>";
	}

	private String getHeader() {
		String head = "<head><style>table, th, td {border: 1px solid black;}</style></head>\n";
		head += "<body>\n";
		head += "<table style=\"width:100%\">\n";
		head += "<tr>";
	    head += "<th>Id</th>";
	   
	    head += "<th>Program</th>";
	    head += "<th>Hint</th>";
	    head += "<th>Count</th>";
	    head += "</tr>";
		return head;
	}

	private File getHintsFile() {
		File hintsDir = new File(FileSystem.getAssnDir(), "hints");
		hintsDir.mkdirs();
		File hintsFile = new File(hintsDir, HINTS_XML_MAP);
		return hintsFile;
	}
}
