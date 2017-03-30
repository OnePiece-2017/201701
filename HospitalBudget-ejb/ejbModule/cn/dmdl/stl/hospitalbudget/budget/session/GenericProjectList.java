package cn.dmdl.stl.hospitalbudget.budget.session;

import javax.persistence.Query;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.common.session.CriterionNativeQuery;

@Name("genericProjectList")
public class GenericProjectList extends CriterionNativeQuery<Object[]> {

	private static final long serialVersionUID = 1L;

	@In(create = true)
	CommonDaoHome commonDaoHome;

	@Override
	protected Query createQuery() {
		StringBuffer sql = new StringBuffer();
		sql.append(" select");
		sql.append(" generic_project.the_id as project_id,");
		sql.append(" generic_project.the_value as project_name,");
		sql.append(" generic_project.project_type,");
		sql.append(" ys_department_info.the_value as department_name,");
		sql.append(" ys_funds_source.the_value as funds_source_name,");
		sql.append(" generic_project.budget_amount,");
		sql.append(" generic_project.the_state");
		sql.append(" from generic_project");
		sql.append(" left join ys_department_info on ys_department_info.the_id = generic_project.department_info_id");
		sql.append(" left join ys_funds_source on ys_funds_source.the_id = generic_project.funds_source_id");
		sql.append(" where generic_project.deleted = 0 and generic_project.the_pid is null");
		if (keyword != null && !"".equals(keyword)) {
			sql.append(" and generic_project.the_value like '%" + keyword + "%'");
		}
		String wc = commonDaoHome.getDepartmentInfoListByUserIdWhereCondition();
		if (wc != null && !"".equals(wc)) {
			sql.append(" and generic_project.department_info_id in (" + wc + ")");
		}
		sql.append(" order by generic_project.the_id desc");
		sql.insert(0, "select * from (").append(") as recordset");
		setEjbql(sql.toString());
		return super.createQuery();
	}

	public GenericProjectList() {
		setEjbql("");
		setAttribute("");
	}

}
