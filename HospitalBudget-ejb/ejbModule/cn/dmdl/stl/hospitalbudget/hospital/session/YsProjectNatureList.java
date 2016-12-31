package cn.dmdl.stl.hospitalbudget.hospital.session;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.common.session.CriterionEntityQuery;
import cn.dmdl.stl.hospitalbudget.hospital.entity.YsProjectNature;

@Name("ysProjectNatureList")
public class YsProjectNatureList extends CriterionEntityQuery<YsProjectNature> {

	private static final long serialVersionUID = 1L;
	private static final String EJBQL = "select ysProjectNature from YsProjectNature ysProjectNature where ysProjectNature.deleted = 0";
	private YsProjectNature ysProjectNature = new YsProjectNature();
	private String keyword;// 关键词

	@Override
	protected Query createQuery() {
		String sql = EJBQL;
		if (keyword != null && !"".equals(keyword)) {
			sql += " and ysProjectNature.theValue like '%" + keyword + "%'";
		}
		setEjbql(sql);
		return super.createQuery();
	}

	public YsProjectNatureList() {
		setEjbql(EJBQL);
		setAttribute("ysProjectNature.theId");
	}

	public YsProjectNature getYsProjectNature() {
		return ysProjectNature;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
}
