package run.abilities;

import java.io.File;
import java.util.*;

import math.LogRegression;
import math.PrecisionRecall;

import org.ejml.simple.SimpleMatrix;

import util.FileSystem;
import util.MatrixUtil;
import util.Warnings;

public class PredictRetention {

	private static final int FOLDS = 10;
	private List<List<AbilityFeatures>> allFolds;

	

	private void run() {
		FileSystem.setAssnId("hoc3");

		allFolds = AbilityFeatures.loadAllFolds();
		normalizeFolds();

		for(int i = 0; i < allFolds.size(); i++) {
			System.out.println("loocv " + i);
			List<AbilityFeatures> trainSet = getTrainSet(i);
			List<AbilityFeatures> testSet = getTestSet(i);

			SimpleMatrix Xtrain = getFeatures(trainSet);
			SimpleMatrix ytrain = getLabels(trainSet);

			for(int j = 0; j < 100; j++) {
				String s = MatrixUtil.toString(Xtrain.extractVector(true, j));
				System.out.println(s + ":\t" + ytrain.get(j));
			}

			LogRegression r = new LogRegression();
			r.train(Xtrain, ytrain);

			SimpleMatrix Xtest = getFeatures(testSet);
			SimpleMatrix ytest = getLabels(testSet);

			double trainAcc = r.test(Xtrain, ytrain);
			double testAcc = r.test(Xtest, ytest);
			double baseline = getBaselineScore(testSet);

			System.out.println("base:\t" + baseline);
			System.out.println("train:\t" +  trainAcc);
			System.out.println("test:\t" + testAcc);
			System.out.println("");
			
			PrecisionRecall.auc(r, Xtest, ytest);

			throw new RuntimeException("test");
		}

		System.out.println("done");

	}

	private void normalizeFolds() {
		for(List<AbilityFeatures> fold : allFolds) {
			for(AbilityFeatures f : fold) {
				f.normalize();
			}
		}

	}

	private double getBaselineScore(List<AbilityFeatures> testSet) {
		int correct = 0;
		for(AbilityFeatures f : testSet) {
			if(f.getLabel()) correct++;
		}
		return 100.0 * correct / testSet.size();
	}

	private SimpleMatrix getLabels(List<AbilityFeatures> dataSet) {
		int numRows = dataSet.size();
		SimpleMatrix y = new SimpleMatrix(numRows, 1);
		for(int i = 0; i < dataSet.size(); i++) {
			int label = dataSet.get(i).getLabel() ? 1 : 0;
			y.set(i, label);
		}
		return y;
	}

	private SimpleMatrix getFeatures(List<AbilityFeatures> dataSet) {
		int numRows = dataSet.size();
		int numCols = 3;
		SimpleMatrix X = new SimpleMatrix(numRows, numCols);
		for(int i = 0; i < dataSet.size(); i++) {
			AbilityFeatures f = dataSet.get(i);
			X.set(i, 0, f.hops);
			X.set(i, 1, f.lastNode == 0 ? 1 : 0);
			X.set(i, 2, f.attemptNext ? 1 : 0);
		}
		return X;
	}

	private List<AbilityFeatures> getTestSet(int i) {
		return allFolds.get(i);
	}

	private List<AbilityFeatures> getTrainSet(int leaveOneOutIndex) {
		List<AbilityFeatures> trainSet = new ArrayList<AbilityFeatures>();
		for(int i = 0; i < FOLDS; i++) {
			if(i != leaveOneOutIndex) {
				trainSet.addAll(allFolds.get(i));
			}
		}
		return trainSet;
	}

	public static void main(String[] args) {
		new PredictRetention().run();
	}
}
