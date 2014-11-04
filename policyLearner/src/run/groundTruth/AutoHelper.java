package run.groundTruth;

import java.util.*;


class AutoHelper {

	private static boolean output = false;

	class Move {
		public Move(String m1, String m2, double cost, Move last) {
			this.m1 = m1;
			this.m2 = m2;
			this.cost = cost;
			this.last = last;
		}

		String m1;
		String m2;
		double cost;
		Move last;
	}



	public static String removeEquivalences(String s) {
		while(true) {
			int length = s.length();
			s = s.replace("LR", "");
			s = s.replace("RL", "");
			s = s.replace("RRR", "L");
			s = s.replace("LLL", "R");
			if(s.length() == length) return s;
		}
	}

	private String s1;
	private String s2;
	private AutoCosts costs;
	private boolean useEq;

	public static String align(String s1, String s2, AutoCosts costs, boolean eq) {
		AutoHelper nw = new AutoHelper();
		nw.useEq = eq;
		nw.s1 = s1;
		nw.s2 = s2;
		nw.costs = costs;
		Move best = nw.getBestMove(s1.length() - 1, s2.length() - 1);

		int matchLength = best.m1.length();
		String editStr = "";
		for(int i = 0; i < matchLength; i++) {
			char a = best.m1.charAt(i);
			char b = best.m2.charAt(i);
			if(b =='*' || a == b) {
				editStr += " ";
			} else {
				if(a == '-') {
					editStr += "i";
				} else if(b == '-') {
					editStr += "d";
				} else {
					editStr += "r";
				}
			}
			/*

			if(b == '*') {
				firstFix += best.m1.charAt(i);
				continue;
			}

			if(a != b && !madeFix) {
				if(a == '-' || b != '-') {
					firstFix += b;
				}
				madeFix = true;
			} else if(a != '-') {
				firstFix += a;
			}*/
		}

		int fixIndex = getFixIndex(editStr, best);
		String fixStr = "";
		for(int i = 0; i < matchLength; i++) {
			char a = best.m1.charAt(i);
			char b = best.m2.charAt(i);
			if(i == fixIndex) {
				if(b != '-') {
					fixStr += b;
				}
			} else if(a != '-') {
				fixStr += a;
			}
		}


		if(output) {
			detailedReport(best, editStr);
			System.out.println(s1);
			System.out.println(fixStr);
			System.out.println(fixIndex);
			System.out.println("");
		}

		return fixStr;
	}

	private static int getFixIndex(String editStr, Move best) {
		boolean foundDelete = false;
		boolean deleteIsMove = false;
		for(int i = 0; i < editStr.length(); i++) {
			char a = editStr.charAt(i);

			if(foundDelete) {
				if(a != 'd') return i - 1;
				boolean isMove = best.m1.charAt(i) == 'M';
				if(isMove != deleteIsMove) return i - 1;
			}

			if(a == 'i') {
				return i;
			}
			if(a == 'r') {
				return i;
			}
			if(a == 'd') {
				deleteIsMove = best.m1.charAt(i) == 'M';
				foundDelete = true;
			}
		}
		return -1;
	}

	private static void simpleReport(String s1, String firstFix) {

	}

	private static void detailedReport(Move best, String editStr) {
		System.out.println(best.m1);
		System.out.println(best.m2);
		System.out.println(editStr);
		System.out.println("cost = " + best.cost);
		System.out.println("");
		Move curr = best;
		System.out.println("backtracking");
		while(curr != null) {
			System.out.println(curr.m1);
			System.out.println(curr.m2);
			System.out.println("");
			curr = curr.last;
		}
	}

	private Move getBestMove(int i, int j) {
		if(i < 0 || j < 0) {
			return baseCase(i, j);
		}

		List<Move> options = new ArrayList<Move>();

		// match option
		options.add(bestMatch(i, j));

		// insert option
		options.add(bestInsert(i, j));

		// delete option
		options.add(bestDelete(i, j));

		// eq option
		if(useEq) {
			Move eqMove = bestEq(i, j);
			if(eqMove != null) {
				options.add(eqMove);
			}
		}


		/*// insert at end
		Move bestLeft = getBestMove(i - 1, j);
		double newCost = bestLeft.cost + END_INSERT_COST;
		String newM1 = bestLeft*/

		return getBestOption(options);
	}

	private Move bestEq(int i, int j) {
		int remaining = i;
		if(remaining >= 2) {
			String end = s1.substring(i - 2, i + 1);
			char otherEnd = s2.charAt(j);
			if(end.equals("LLL") && otherEnd == 'R') {
				Move old = getBestMove(i - 3, j - 1);
				String newM1 = old.m1 + "LLL";
				String newM2 = old.m2 + "***";
				return new Move(newM1, newM2, old.cost + costs.eqCost, old);
			}
			if(end.equals("RRR") && otherEnd == 'L') {
				Move old = getBestMove(i - 3, j - 1);
				String newM1 = old.m1 + "RRR";
				String newM2 = old.m2 + "***";
				return new Move(newM1, newM2, old.cost + costs.eqCost, old);
			}
		}
		if(remaining >= 1) {
			String end = s1.substring(i - 1, i + 1);
			if(end.equals("LR") || end.equals("RL")) {
				Move old = getBestMove(i - 2, j);
				String newM1 = old.m1 + end;
				String newM2 = old.m2 + "**";
				return new Move(newM1, newM2, old.cost + costs.eqCost, old);
			}
		}
		return null;
	}

	private Move bestDelete(int i, int j) {
		Move bestUp = getBestMove(i - 1, j);
		boolean isEnd = i == s1.length() - 1;
		double cost = isEnd ? costs.endDelete : costs.delete;
		double newCost = bestUp.cost + cost;
		String newM1 = bestUp.m1 + s1.charAt(i);
		String newM2 = bestUp.m2 + "-";
		return new Move(newM1, newM2, newCost, bestUp);
	}

	private Move bestInsert(int i, int j) {
		Move bestLeft = getBestMove(i, j - 1);
		boolean isEnd = i == s1.length() - 1;
		double cost = isEnd ? costs.endInsert : costs.middleInsert;
		double newCost = bestLeft.cost + cost;
		String newM1 = bestLeft.m1 + "-";
		String newM2 = bestLeft.m2 + s2.charAt(j);
		return new Move(newM1, newM2, newCost, bestLeft);
	}

	private Move bestMatch(int i, int j) {
		Move bestDiag = getBestMove(i - 1, j - 1);
		double newCost = bestDiag.cost + match(i, j);
		String newM1 = bestDiag.m1 + s1.charAt(i);
		String newM2 = bestDiag.m2 + s2.charAt(j);
		return new Move(newM1, newM2, newCost, bestDiag);
	}

	private Move baseCase(int i, int j) {
		if(i < 0 && j < 0) {
			return new Move("", "", 0, null);
		}
		if(i < 0) {
			int length = j + 1;
			String dashes = "";
			for(int k = 0; k < length; k++) {
				dashes += "-";
			}
			return new Move(dashes, s2.substring(0, length), 
					costs.beginInsert * length, null);
		}
		if(j < 0) {
			int length = i + 1;
			String dashes = "";
			for(int k = 0; k < length; k++) {
				dashes += "-";
			}
			return new Move(s1.substring(0, length), dashes, 
					costs.delete * length, null);
		}
		throw new RuntimeException("no");
	}

	private Move getBestOption(List<Move> options) {
		Move argMin = null;
		for(int i = options.size() - 1; i >= 0; i--) {
			Move m = options.get(i);
			if(argMin == null || m.cost < argMin.cost) {
				argMin = m;
			}
		}
		return argMin;
	}

	private double match(int i, int j) {
		char a = s1.charAt(i);
		char b = s2.charAt(j);
		if(a == b) return 0;
		if(a != 'M' && b != 'M') return costs.leftRight;
		return 10000;
	}

	public static void main(String[] args) {
		AutoHelper.output = true;

		AutoCosts c = new AutoCosts();
		c.eqCost = 0.2;

		c.endInsert = 0.1;
		c.middleInsert = 5.0;
		c.beginInsert = 2.0;

		c.endDelete = 0.8;
		c.delete = 1.0;

		c.leftRight = 0.5;
		c.eqCost = 20;



		/*private static final double END_INSERT_COST = 0.1;
		private static final double INSERT_COST = 1.0;
		private static final double BEGIN_INSERT_COST = 0.9;

		private static final double END_DELETE_COST = 1.0;
		private static final double DELETE_COST = 1.0;
		private static final double LEFT_RIGHT_CHANGE = 0.9;*/

		/*AutoHelper.align("MLM", "MLMRM", c);
		AutoHelper.align("MM", "MLMRM", c);
		AutoHelper.align("MMLM", "MLMRM", c);
		AutoHelper.align("MMRM", "MLMRM", c);
		AutoHelper.align("MLMRMR", "MLMRM", c);
		AutoHelper.align("MLMRMM", "MLMRM", c);
		AutoHelper.align("MMRML", "MLMRM", c);
		AutoHelper.align("LMRM", "MLMRM", c);
		AutoHelper.align("MRM", "MLMRM", c);
		AutoHelper.align("MLLRM", "MLMRM", c);
		AutoHelper.align("MRRRM", "MLMRM", c);
		AutoHelper.align("MLMM", "MLMRM", c);
		AutoHelper.align("LM", "MLMRM", c);
		AutoHelper.align("L", "MLMRM", c);
		AutoHelper.align("MMM", "MLMRM", c);
		AutoHelper.align(removeEquivalences("MRRRM"), "MLMRM", c);
		System.out.println("---");
		String s = removeEquivalences("MLLLRRLLRLM");
		System.out.println(s);*/
		AutoHelper.align("MMRL", "MLMRM", c, true);
	}
}