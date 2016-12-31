package cn.dmdl.stl.hospitalbudget.common.session;

import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.Query;

@Name("paginationModel")
public class PaginationModel {

	private Query<EntityManager, ?> query;// Base class for components which manage a query result set.

	public void init(Query<EntityManager, ?> query) {
		this.query = query;
	}

	public Query<EntityManager, ?> getQuery() {
		return query;
	}

	public int getPreviousPage() {
		if (query instanceof CriterionEntityQuery) {
			return ((CriterionEntityQuery<?>) query).getPreviousPage();
		} else {
			return ((CriterionNativeQuery<?>) query).getPreviousPage();
		}
	}

	public int getNextPage() {
		if (query instanceof CriterionEntityQuery) {
			return ((CriterionEntityQuery<?>) query).getNextPage();
		} else {
			return ((CriterionNativeQuery<?>) query).getNextPage();
		}
	}

	public boolean isPreviousPageExist() {
		if (query instanceof CriterionEntityQuery) {
			return ((CriterionEntityQuery<?>) query).isPreviousPageExist();
		} else {
			return ((CriterionNativeQuery<?>) query).isPreviousPageExist();
		}
	}

	public boolean isNextPageExist() {
		if (query instanceof CriterionEntityQuery) {
			return ((CriterionEntityQuery<?>) query).isNextPageExist();
		} else {
			return ((CriterionNativeQuery<?>) query).isNextPageExist();
		}
	}

	public int getLastPage() {
		if (query instanceof CriterionEntityQuery) {
			return ((CriterionEntityQuery<?>) query).getLastPage();
		} else {
			return ((CriterionNativeQuery<?>) query).getLastPage();
		}
	}

	public int getPage() {
		if (query instanceof CriterionEntityQuery) {
			return ((CriterionEntityQuery<?>) query).getPage();
		} else {
			return ((CriterionNativeQuery<?>) query).getPage();
		}
	}

	public List<Long> getPageNoList() {
		if (query instanceof CriterionEntityQuery) {
			return ((CriterionEntityQuery<?>) query).getPageNoList();
		} else {
			return ((CriterionNativeQuery<?>) query).getPageNoList();
		}
	}

}
