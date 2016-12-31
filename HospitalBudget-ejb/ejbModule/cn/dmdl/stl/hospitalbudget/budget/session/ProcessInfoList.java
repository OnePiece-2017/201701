package cn.dmdl.stl.hospitalbudget.budget.session;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.budget.entity.ProcessInfo;
import cn.dmdl.stl.hospitalbudget.common.session.CriterionEntityQuery;

@Name("processInfoList")
public class ProcessInfoList extends CriterionEntityQuery<ProcessInfo> {

	private static final long serialVersionUID = 1L;
	private static final String EJBQL = "select processInfo from ProcessInfo processInfo where processInfo.deleted = 0";
	private ProcessInfo processInfo = new ProcessInfo();
	private String keyword;// 关键词

	@Override
	protected Query createQuery() {
		String sql = EJBQL;
		if (keyword != null && !"".equals(keyword)) {
			sql += " and processInfo.processName like '%" + keyword + "%'";
		}
		setEjbql(sql);
		return super.createQuery();
	}

	public ProcessInfoList() {
		setEjbql(EJBQL);
		setAttribute("processInfo.processInfoId");
	}

	public ProcessInfo getProcessInfo() {
		return processInfo;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
}
