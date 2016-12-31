package cn.dmdl.stl.hospitalbudget.hospital.session;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.common.session.CriterionEntityQuery;
import cn.dmdl.stl.hospitalbudget.hospital.entity.YsDepartmentInfo;

@Name("ysDepartmentInfoList")
public class YsDepartmentInfoList extends CriterionEntityQuery<YsDepartmentInfo> {

	private static final long serialVersionUID = 1L;
	private static final String EJBQL = "select ysDepartmentInfo from YsDepartmentInfo ysDepartmentInfo where ysDepartmentInfo.deleted = 0";
	private YsDepartmentInfo ysDepartmentInfo = new YsDepartmentInfo();
	private String keyword;// 关键词

	@Override
	protected Query createQuery() {
		String sql = EJBQL;
		if (keyword != null && !"".equals(keyword)) {
			sql += " and ysDepartmentInfo.theValue like '%" + keyword + "%'";
		}
		setEjbql(sql);
		return super.createQuery();
	}

	public YsDepartmentInfoList() {
		setEjbql(EJBQL);
		setAttribute("ysDepartmentInfo.theId");
	}

	public YsDepartmentInfo getYsDepartmentInfo() {
		return ysDepartmentInfo;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
}
