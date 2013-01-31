package com.datascience.galc;

import com.datascience.core.storages.JSONUtils;
import java.util.Set;

public class DatumCont extends Datum {
	
	private DatumContResults 				results;

	public DatumCont(String name) {
		super(name);
		this.results = new DatumContResults();	
	}

	public DatumContResults getResults() {
		return results;
	}

	public void setResults(DatumContResults results) {
		this.results = results;
	}

	/**
	 * @return the est_value
	 */
	public Double getAverageLabel() {

		double sum =0;
		Set<AssignedLabel> labels = getAssignedLabels();
		for (AssignedLabel al: labels) {
			sum += al.getLabel();
		}
		
		return sum/labels.size();
	}	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
//	@Override
//	public String toString() {
//		DatumContResults dr = getResults();
//		return Objects.toStringHelper(this)
//			       .add("name", getName())
//			       .add("est_value", dr.getEst_value())
//			       .add("est_zeta", dr.getEst_zeta())
//			       .toString();
//	}

	@Override
	public String toString() {
		return JSONUtils.gson.toJson(this);
	}
	
}