package cn.dmdl.stl.hospitalbudget.budget.session;

import javax.persistence.Query;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.common.session.CriterionNativeQuery;
import cn.dmdl.stl.hospitalbudget.util.SessionToken;

@Name("taskUntreatedList")
public class TaskUntreatedList extends CriterionNativeQuery<Object[]> {

	private static final long serialVersionUID = 1L;
	@In
	protected SessionToken sessionToken;
	private String keyword;// 关键词

	@Override
	protected Query createQuery() {
		StringBuffer sql = new StringBuffer();
		sql.append(" select");
		sql.append(" task_order.task_order_id,");
		sql.append(" task_order.order_sn,");
		sql.append(" task_order.task_name,");
		sql.append(" ys_department_info.the_value as department_name,");
		sql.append(" user_info_extend.fullname as edit_user,");
		sql.append(" task_order.task_type,");
		sql.append(" task_order.order_status,");
		sql.append(" task_order.audit_opinion,");
		sql.append(" task_order.process_step_info_id");
		sql.append(" from task_order");
		sql.append(" left join ys_department_info on ys_department_info.the_id = task_order.dept_id");
		sql.append(" left join user_info on user_info.user_info_id = task_order.edit_user_id");
		sql.append(" left join user_info_extend on user_info_extend.user_info_extend_id = user_info.user_info_extend_id");
		sql.append(" where task_order.task_order_id in (select task_order_id from task_user where handle_flg = 0 and user_id = " + sessionToken.getUserInfoId() + " and task_order_id not in (select task_order_id from task_user where handle_flg = 1))");
		sql.append(" or (task_order.edit_user_id = " + sessionToken.getUserInfoId() + " and task_order.order_status = 3)");
		if (keyword != null && !"".equals(keyword)) {
			sql.append(" and (task_order.order_sn like '%" + keyword + "%'");
			sql.append(" or task_order.task_name like '%" + keyword + "%'");
			sql.append(" )");
		}
		sql.append(" order by task_order.task_order_id desc");
		sql.insert(0, "select * from (").append(") as recordset");
		setEjbql(sql.toString());
		return super.createQuery();
	}

	public TaskUntreatedList() {
		setEjbql("");
		setAttribute("");
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

}
