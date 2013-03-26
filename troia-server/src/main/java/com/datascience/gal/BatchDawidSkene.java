/*******************************************************************************
 * Copyright (c) 2012 Panagiotis G. Ipeirotis & Josh M. Attenberg
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.datascience.gal;

import java.util.HashMap;
import java.util.Map;

import com.datascience.core.base.*;
import com.datascience.core.results.WorkerResult;
import com.datascience.core.stats.ConfusionMatrixNormalizationType;
import com.datascience.core.stats.ErrorRateCalculators;
import org.apache.log4j.Logger;

public class BatchDawidSkene extends AbstractDawidSkene {

	private static Logger logger = Logger.getLogger(BatchDawidSkene.class);

	public BatchDawidSkene() {
		super(new ErrorRateCalculators.BatchErrorRateCalculator());
	}

	public Map<String, Double> getCategoryPriors() {
		Map<String, Double> out = new HashMap<String, Double>(data.getCategories().size());
		for (Category cat : data.getCategories())
			out.put(cat.getName(), cat.getPrior());
		return out;
	}
	
	@Override
	public double prior(String categoryName) {
		return data.getCategory(categoryName).getPrior();
	}

	private void updateObjectClassProbabilities() {
		for (LObject<String> obj : data.getObjects()) {
			this.updateObjectClassProbabilities(obj);
		}
	}

	private void updateObjectClassProbabilities(LObject<String> obj) {
		Map<String, Double> probabilities = getObjectClassProbabilities(obj, null);
		if (probabilities == null)
			return;
		results.getOrCreateDatumResult(obj).setCategoryProbabilites(probabilities);
	}

	private void rebuildWorkerConfusionMatrices() {
		for (Worker<String> worker : data.getWorkers()) {
			rebuildWorkerConfusionMatrix(worker);
		}
	}

	private void rebuildWorkerConfusionMatrix(Worker<String> worker) {
		WorkerResult wr = results.getWorkerResult(worker);
		wr.empty();

		// Scan all objects and change the confusion matrix for each worker
		// using the class probability for each object
		for (AssignedLabel<String> al : worker.getAssigns()) {

			// Get the name of the object and the category it
			// is classified from this worker.
			String destination = al.getLabel();
			// We get the classification of the object
			// based on the votes of all the other workers
			// We treat this classification as the "correct" one
			Map<String, Double> probabilities = getObjectClassProbabilities(al.getLobject(), worker);
			if (probabilities == null)
				continue; // No other worker labeled the object

			for (String source : probabilities.keySet()) {
				double error = probabilities.get(source);
				wr.addError(source, destination, error);
			}
		}
//        System.out.println("before: "
//                + ((MultinomialConfusionMatrix) w.cm).rowDenominator);
		wr.normalize(ConfusionMatrixNormalizationType.UNIFORM);
////        System.out.println("after: "
//                + ((MultinomialConfusionMatrix) w.cm).rowDenominator);

	}

	@Override
	public void compute(){
		estimate(epsilon, iterations);
	}

	public void estimate(double epsilon, int maxIterations) {
		double prevLogLikelihood = Double.POSITIVE_INFINITY;
		double currLogLikelihood = 0d;
		int iteration = 0;
		for (;iteration < maxIterations && Math.abs(currLogLikelihood -
				prevLogLikelihood) > epsilon; iteration++) {
			prevLogLikelihood = currLogLikelihood;
			estimateInner();
			currLogLikelihood = getLogLikelihood();
		}
		double diffLogLikelihood = Math.abs(currLogLikelihood - prevLogLikelihood);
		logger.info("Estimated: performed " + iteration + " / " +
				maxIterations + " with log-likelihood difference " +
				diffLogLikelihood);
	}

	protected void estimateInner() {
		updateObjectClassProbabilities();
		updatePriors();
		rebuildWorkerConfusionMatrices();
	}
}
