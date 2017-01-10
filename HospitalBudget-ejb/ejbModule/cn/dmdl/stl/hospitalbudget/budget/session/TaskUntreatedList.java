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
		sql.append(" task_order_id,");
		sql.append(" order_sn,");
		sql.append(" task_name,");
		sql.append(" dept_id,");
		sql.append(" edit_user_id,");
		sql.append(" task_type,");
		sql.append(" order_status,");
		sql.append(" audit_opinion,");
		sql.append(" process_step_info_id");
		sql.append(" from task_order where task_order_id in (select task_order_id from task_user where handle_flg = 0 and user_id = " + sessionToken.getUserInfoId() + " and task_order_id not in (select task_order_id from task_user where handle_flg = 1))");
		sql.append(" or (edit_user_id = " + sessionToken.getUserInfoId() + " and order_status = 3)");
		if (keyword != null && !"".equals(keyword)) {
			sql.append(" and (order_sn like '%" + keyword + "%'");
			sql.append(" or task_name like '%" + keyword + "%'");
			sql.append(" )");
		}
		setEjbql(sql.toString());
		System.out.println(getEjbql());
		return super.createQuery();
	}

	public TaskUntreatedList() {
		setEjbql("");
		setAttribute("task_order_id");
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

}
