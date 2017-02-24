package cn.dmdl.stl.hospitalbudget.common.session;

import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.framework.EntityQuery;

import cn.dmdl.stl.hospitalbudget.boot.ConfigureCache;

public class CriterionEntityQuery<E> extends EntityQuery<E> {

	private static final long serialVersionUID = 1L;
	private String orderDirection;
	private Integer maxResults;
	protected static final int MAX_RESULTS = 20;
	protected static final String DIR_ASC = "asc";
	protected static final String DIR_DESC = "desc";
	protected int page = 1;
	protected String keyword;

	public CriterionEntityQuery() {
		orderDirection = ConfigureCache.getSettingsValue("query_order_direction") != null ? ConfigureCache.getSettingsValue("query_order_direction") : DIR_DESC;
		maxResults = ConfigureCache.getSettingsValue("query_max_results") != null ? Integer.valueOf(ConfigureCache.getSettingsValue("query_max_results")) : MAX_RESULTS;
	}

	protected void setAttribute(String orderColumn) {
		setOrderColumn(orderColumn);
		setOrderDirection(orderDirection);
		setMaxResults(maxResults);
	}

	public int getPreviousPage() {
		return page - 1;
	}

	public int getNextPage() {
		return page + 1;
	}

	public boolean isPreviousPageExist() {
		return page > 1;
	}

	public boolean isNextPageExist() {
		return getPageCount() > page;
	}

	public int getLastPage() {
		return getPageCount();
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
		setFirstResult((page - 1) * maxResults);
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public List<Long> getPageNoList() {
		List<Long> list = new ArrayList<Long>();

		long maxPages = getPageCount();
		int showPages = 5;

		if (maxPages < showPages) {
			for (long i = 1; i <= maxPages; i++) {
				list.add(i);
			}
		} else {
			if (page <= 3) {
				for (long i = 1; i <= showPages; i++) {
					list.add(i);
				}
			} else if (maxPages - page < 3) {
				for (long i = maxPages - showPages + 1; i <= maxPages; i++) {
					list.add(i);
				}
			} else {
				for (long i = page - 2; i <= page + 2; i++) {
					list.add(i);
				}
			}
		}

		return list;
	}
}
