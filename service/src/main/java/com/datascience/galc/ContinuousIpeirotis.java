package com.datascience.galc;

import com.datascience.core.base.*;
import com.datascience.core.results.DatumContResults;
import com.datascience.core.results.WorkerContResults;
import org.apache.log4j.Logger;

import java.util.*;

public class ContinuousIpeirotis extends Algorithm<ContValue, IData<ContValue>, DatumContResults, WorkerContResults> {

	private static Logger logger = Logger.getLogger(ContinuousIpeirotis.class);

	protected int iterations = 10;
	protected double  epsilon = 1e-6;

	protected Double getLabel(AssignedLabel<ContValue> assign){
		return assign.getLabel().getValue();
	}

	private void initWorkers() {
		double initial_rho = 0.9;
		for (Worker<ContValue> w : data.getWorkers()) {
			WorkerContResults wcr = new WorkerContResults(w);
			wcr.setEst_rho(initial_rho);
			wcr.computeZetaValues(getData().getWorkerAssigns(w));
			results.addWorkerResult(w, wcr);
		}
	}

	public Double getAverageLabel(LObject<ContValue> object) {
		Collection<AssignedLabel<ContValue>> assigns = data.getAssignsForObject(object);
		return getAverageLabel(assigns);
	}

	public Double getAverageLabel(Collection<AssignedLabel<ContValue>> assigns){
		double sum = 0;

		for (AssignedLabel<ContValue> al: assigns) {
			sum += getLabel(al);
		}
		return sum / assigns.size();
	}

	private double estimate(double epsilon, int max_iters) {
		logger.info("GALC estimate START");

		double pastLogLikelihood;
		double logLikelihood = 0d;

		int round = 0;
		double diff = Double.POSITIVE_INFINITY;
		while (diff > epsilon && round < max_iters) {
			round++;
			Double diffZetas = estimateObjectZetas();
			Double diffWorkers = estimateWorkerRho();
			if (Double.isNaN(diffZetas + diffWorkers)) {
				logger.error("ERROR: Check for division by 0");
				break;
			}
			pastLogLikelihood = logLikelihood;
			logLikelihood = getLogLikelihood();
			diff = Math.abs(logLikelihood - pastLogLikelihood);
		}
		// Estimate distribution mu and sigma.
		double mu = estimateDistributionMu();
		double sigma = estimateDistributionSigma();
		// Estimate objects' values. 
		for (LObject<ContValue> obj : data.getObjects()){
			DatumContResults dcr = results.getDatumResult(obj);
			dcr.setDistributionMu(mu);
			dcr.setDistributionSigma(sigma);
			dcr.computeEst_value();
			results.addDatumResult(obj, dcr);
		}
		logger.info(String.format("GALC estimate STOP. iterations %d/%d, loglikelihood =%f",
				round, max_iters, logLikelihood));

		return logLikelihood;
	}

	protected double getLogLikelihood() {

		double result = 0d;
		for (Worker<ContValue> worker : data.getWorkers()){
			WorkerContResults wcr = results.getWorkerResult(worker);
			for (AssignedLabel<ContValue> al : wcr.getZetaValues()) {
				HashMap<LObject<ContValue>, Double> zetas = estimateObjectZetas(worker);  //MOVE ^
				LObject<ContValue> object = al.getLobject();
				Double zeta = zetas.get(object);
				double rho = wcr.getEst_rho();
				result += 0.5 * Math.pow(zeta, 2) / (1 - Math.pow(rho, 2)) - Math.log(Math.sqrt(1 - Math.pow(rho, 2)));
			}
		}
		return result;

	}
	
	private void initObjectZetas() {
		for (LObject<ContValue> obj : data.getObjects()){
			DatumContResults dr = new DatumContResults(obj);
			results.addDatumResult(obj, dr);
		}
		estimateObjectZetas();
	}

	private Double estimateObjectZetas() {
		// See equation 9
		double diff = 0.0;
		for (LObject<ContValue> obj : data.getObjects()){
			DatumContResults dr = results.getDatumResult(obj);
			Double oldZeta;
			Double newZeta;
			Double zeta = 0.0;
			Double betasum = 0.0;
            logger.warn(data.getGoldObjects());
			if(!data.getGoldObjects().contains(obj)) {
				oldZeta = dr.getEst_zeta();

				for (AssignedLabel<ContValue> al : data.getAssignsForObject(obj)) {
					WorkerContResults wr = results.getWorkerResult(al.getWorker());
					Double b = wr.getBeta();
					Double r = wr.getEst_rho();
					Double z = wr.getZeta(getLabel(al));

					zeta += b * r * z;
					betasum += b;

					//Single Label Worker gives a z=NaN, due to its current est_sigma which is equal to 0
					if (Double.isNaN(zeta))
						logger.warn("["+ z + "," + al.getLabel() + "," + wr.getEst_mu() + "," +
								wr.getEst_sigma() + "," + wr.getWorker().getName()+"], ");

				}

				//d.setEst_zeta(zeta / betasum);
				newZeta = zeta / betasum;
			} else {
				oldZeta = data.getGoldObject(obj.getName()).getGoldLabel().getZeta();
				newZeta = oldZeta;
			}

			dr.setEst_zeta(newZeta);

			results.addDatumResult(obj, dr);
			if (data.getGoldObjects().contains(obj))
				continue;
			else if (oldZeta == null) {
				diff += 1;
				continue;
			}

			diff += Math.abs(dr.getEst_zeta() - oldZeta);
		}
		return diff;
	}

	private HashMap<LObject<ContValue>, Double> estimateObjectZetas(Worker<ContValue> workerToIgnore) {

		HashMap<LObject<ContValue>, Double> result = new HashMap<LObject<ContValue>, Double>();

		// See equation 9 without the influence of worker w
		for (LObject<ContValue> object: data.getObjects()) {
			Double newZeta = 0.0;
			Double zeta = 0.0;
			Double betasum = 0.0;

			for (AssignedLabel<ContValue> al : data.getAssignsForObject(object)) {
				Worker<ContValue>  worker = al.getWorker();
				if(worker.equals(workerToIgnore))
					continue;
				WorkerContResults wr = results.getWorkerResult(worker);
				Double b = wr.getBeta();
				Double r = wr.getEst_rho();
				Double z = wr.getZeta(getLabel(al));
				zeta += b * r * z;
				betasum += b;
			}

			//d.setEst_zeta(zeta / betasum);
			newZeta = zeta / betasum;
			result.put(object, newZeta);
			//if (Double.isNaN(newZeta)) logger.info("estimateObjectZetas NaNbug@: " + zeta +","+ betasum + "," +d.getName());
		}
		return result;
	}

	private double estimateWorkerRho() {

		// See equation 10

		double diff = 0.0;
		for (Worker<ContValue> worker : data.getWorkers()){
			WorkerContResults wcr = results.getWorkerResult(worker);
			Double sum_prod = 0.0;
			Double sum_zi = 0.0;
			Double sum_zij = 0.0;

			double oldrho = wcr.getEst_rho();
			for (AssignedLabel<ContValue> al : wcr.getZetaValues()) {

				HashMap<LObject<ContValue>, Double> zeta = estimateObjectZetas(worker);

				LObject<ContValue> object = al.getLobject();
				Double z_i = zeta.get(object);
				double z_ij = getLabel(al);

				sum_prod += z_i * z_ij;
				sum_zi += z_i * z_i;
				sum_zij += z_ij * z_ij;
			}
			double rho = sum_prod / Math.sqrt(sum_zi * sum_zij);

			if (Double.isNaN(rho)) {
				logger.warn("estimateWorkerRho NaNbug@: " + sum_zi +","+ sum_zij + ","
						+ worker.getName());
				rho = 0.0;
			}
			wcr.setEst_rho(rho);
			results.addWorkerResult(worker, wcr);
			diff += Math.abs(wcr.getEst_rho() - oldrho);
		}
		return diff;
	}
	
	private Double estimateDistributionSigma() {

		Double nominatorSigma = 0.0;
		Double denominatorSigma = 0.0;
		for (Worker<ContValue> worker : data.getWorkers()){
			WorkerContResults wcr = results.getWorkerResult(worker);
			Double b = wcr.getBeta();
			Double coef = Math.sqrt(b * b - b);
			Double s = wcr.getEst_sigma();
			nominatorSigma += coef * s;
			denominatorSigma += b;
		}
		return nominatorSigma / denominatorSigma;
	}
	
	private Double estimateDistributionMu() {

		Double nominatorMu = 0.0;
		Double denominatorMu = 0.0;
		for (Worker<ContValue> worker : data.getWorkers()){
			WorkerContResults wcr = results.getWorkerResult(worker);
			Double b = wcr.getBeta();
			Double coef = Math.sqrt(b * b - b);
			Double m = wcr.getEst_mu();
			nominatorMu += coef * m;
			denominatorMu += b;
		}
		return nominatorMu / denominatorMu;
	}

	public void setIterations(int iterations){
		this.iterations = iterations;
	}

	public void setEpsilon(double epsilon){
		this.epsilon = epsilon;
	}

	@Override
	public void compute(){
		initWorkers();
		initObjectZetas();
		estimate(epsilon, iterations);
	}
}
