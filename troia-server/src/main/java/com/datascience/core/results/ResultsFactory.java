package com.datascience.core.results;

import com.datascience.core.base.Category;
import com.datascience.core.base.ContValue;
import com.datascience.core.base.LObject;
import com.datascience.core.base.Worker;

import java.util.Collection;

/**
 * User: artur
 */
public class  ResultsFactory {

	public interface IDatumResultCreator<T, U>{
		U create(LObject<T> obj);
	}

	public static class DatumResultFactory implements IDatumResultCreator<String, DatumResult>{
		public DatumResult create(LObject<String> obj){
			return new DatumResult();
		}
	}

	public static class DatumContResultFactory implements  IDatumResultCreator<ContValue, DatumContResults>{
		public DatumContResults create(LObject<ContValue> obj){
			return new DatumContResults(obj);
		}
	}

	public interface IWorkerResultCreator<T, U>{
		U create(Worker<T> w);
	}

	public static class WorkerResultNominalFactory implements IWorkerResultCreator<String, WorkerResult> {

		protected Collection<Category> categories;

		public WorkerResultNominalFactory(Collection<Category> categories){
			this.categories = categories;
		}

		public WorkerResult create(Worker<String> obj){
			return new WorkerResult(categories);
		}
	}

	public static class WorkerContResultFactory implements IWorkerResultCreator<ContValue, WorkerContResults>{
		public WorkerContResults create(Worker<ContValue> obj){
			return new WorkerContResults(obj);
		}
	}
}
