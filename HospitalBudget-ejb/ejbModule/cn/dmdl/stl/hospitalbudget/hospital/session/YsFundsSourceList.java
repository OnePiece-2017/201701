package cn.dmdl.stl.hospitalbudget.hospital.session;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.common.session.CriterionEntityQuery;
import cn.dmdl.stl.hospitalbudget.hospital.entity.YsFundsSource;

@Name("ysFundsSourceList")
public class YsFundsSourceList extends CriterionEntityQuery<YsFundsSource> {

	private static final long serialVersionUID = 1L;
	private static final String EJBQL = "select ysFundsSource from YsFundsSource ysFundsSource where ysFundsSource.deleted = 0";
	private YsFundsSource ysFundsSource = new YsFundsSource();
	private String keyword;// 关键词

	@Override
	protected Query createQuery() {
		String sql = EJBQL;
		if (keyword != null && !"".equals(keyword)) {
			sql += " and ysFundsSource.theValue like '%" + keyword + "%'";
		}
		setEjbql(sql);
		return super.createQuery();
	}

	public YsFundsSourceList() {
		setEjbql(EJBQL);
		setAttribute("ysFundsSource.theId");
	}

	public YsFundsSource getYsFundsSource() {
		return ysFundsSource;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
}
