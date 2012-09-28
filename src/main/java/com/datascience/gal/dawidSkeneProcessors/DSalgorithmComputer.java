/*******************************************************************************
 * Copyright (c) 2012 Panagiotis G. Ipeirotis & Josh M. Attenberg
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/

package com.datascience.gal.dawidSkeneProcessors;

import com.datascience.gal.DawidSkene;


/**
 * Objects of this class execute Dawid-Skene algorithm on DS model with
 * given id.
 */
public class DSalgorithmComputer extends DawidSkeneProcessor {

	@Override
	public void run() {
		DawidSkene ds = this.getCache().getDawidSkene(this.getDawidSkeneId());
		ds.estimate(this.iterations);
		this.getCache().insertDawidSkene(ds);
		this.setState(DawidSkeneProcessorState.FINISHED);
	}

	/**
	 * How many iterations of Dawid-Skene algorithm will be run
	 */
	private int iterations;


	/**
	 * @return How many iterations of Dawid-Skene algorithm will be run
	 */
	public int  getIterations() {
		return iterations;
	}


	/**
	 * @param iterations How many iterations of Dawid-Skene algorithm will be run
	 */
	public void setIterations(int  iterations) {
		this.iterations = iterations;
	}

}
