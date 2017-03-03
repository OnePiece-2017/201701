package cn.dmdl.stl.hospitalbudget.budget.session;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.common.session.CriterionNativeQuery;

@Name("usualProjectList")
public class UsualProjectList extends CriterionNativeQuery<Object[]> {

	private static final long serialVersionUID = 1L;

	@Override
	protected Query createQuery() {
		StringBuffer sql = new StringBuffer();
		sql.append(" select");
		sql.append(" usual_project.the_id as project_id,");
		sql.append(" usual_project.the_value as project_name,");
		sql.append(" usual_project.project_type,");
		sql.append(" ys_department_info.the_value as department_name,");
		sql.append(" ys_funds_source.the_value as funds_source_name,");
		sql.append(" usual_project.budget_amount,");
		sql.append(" usual_project.the_state");
		sql.append(" from usual_project");
		sql.append(" left join ys_department_info on ys_department_info.the_id = usual_project.department_info_id");
		sql.append(" left join ys_funds_source on ys_funds_source.the_id = usual_project.funds_source_id");
		sql.append(" where usual_project.deleted = 0 and usual_project.the_pid is null");
		if (keyword != null && !"".equals(keyword)) {
			sql.append(" and usual_project.the_value like '%" + keyword + "%'");
		}
		sql.append(" order by usual_project.the_id desc");
		sql.insert(0, "select * from (").append(") as recordset");
		setEjbql(sql.toString());
		return super.createQuery();
	}

	public UsualProjectList() {
		setEjbql("");
		setAttribute("");
	}

}
