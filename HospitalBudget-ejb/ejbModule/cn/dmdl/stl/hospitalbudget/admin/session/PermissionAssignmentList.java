package cn.dmdl.stl.hospitalbudget.admin.session;

import java.util.List;

import javax.persistence.Query;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.admin.entity.PermissionAssignment;
import cn.dmdl.stl.hospitalbudget.common.session.CriterionEntityQuery;

@Name("permissionAssignmentList")
public class PermissionAssignmentList extends CriterionEntityQuery<PermissionAssignment> {

	private static final long serialVersionUID = 1L;
	private static final String EJBQL = "select permissionAssignment from PermissionAssignment permissionAssignment where permissionAssignment.deleted = 0";
	private PermissionAssignment permissionAssignment = new PermissionAssignment();
	private String keyword;// 关键词
	private JSONArray sortableEcho4displayOrder;// sortable回显数据
	private String sortableArgs4displayOrder;// sortable参数（推荐使用ids）
	private JSONObject sortableResult4displayOrder;// sortable返回结果

	/** sortable回显数据初始化 */
	@SuppressWarnings("unchecked")
	public void sortableInit4displayOrder() {
		if (sortableEcho4displayOrder != null) {
			sortableEcho4displayOrder.clear();
		} else {
			sortableEcho4displayOrder = new JSONArray();
		}
		List<PermissionAssignment> permissionAssignmentList = getEntityManager().createQuery(EJBQL + " order by permissionAssignment.displayOrder asc").getResultList();
		if (permissionAssignmentList != null && permissionAssignmentList.size() > 0) {
			for (PermissionAssignment permissionAssignment : permissionAssignmentList) {
				JSONObject sortableEcho = new JSONObject();
				sortableEcho.accumulate("value", permissionAssignment.getPermissionAssignmentId());
				sortableEcho.accumulate("label", permissionAssignment.getModuleName());
				sortableEcho4displayOrder.add(sortableEcho);
			}
		}
	}

	/** sortable排序实现 */
	public void sortableInvoke4displayOrder() {
		if (sortableResult4displayOrder != null) {
			sortableResult4displayOrder.clear();
		} else {
			sortableResult4displayOrder = new JSONObject();
		}
		sortableResult4displayOrder.accumulate("message", "排序成功！");
		if (sortableArgs4displayOrder != null && !"".equals(sortableArgs4displayOrder)) {
			String[] args = sortableArgs4displayOrder.split(",");
			if (args != null && args.length > 0) {
				for (int i = 0; i < args.length; i++) {
					PermissionAssignment permissionAssignment = getEntityManager().find(PermissionAssignment.class, Integer.valueOf(args[i]));
					permissionAssignment.setDisplayOrder(i + 1);
					getEntityManager().merge(permissionAssignment);
				}
				getEntityManager().flush();
			}
		} else {
			sortableResult4displayOrder.element("message", "排序失败！参数为空。");
		}
	}

	@Override
	protected Query createQuery() {
		String sql = EJBQL;
		if (keyword != null && !"".equals(keyword)) {
			sql += " and (permissionAssignment.moduleName like '%" + keyword + "%' or permissionAssignment.description like '%" + keyword + "%')";
		}
		setEjbql(sql);
		return super.createQuery();
	}

	public PermissionAssignmentList() {
		setEjbql(EJBQL);
		setAttribute("permissionAssignment.displayOrder");
		setOrderDirection(DIR_ASC);
	}

	public PermissionAssignment getPermissionAssignment() {
		return permissionAssignment;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public String getSortableArgs4displayOrder() {
		return sortableArgs4displayOrder;
	}

	public void setSortableArgs4displayOrder(String sortableArgs4displayOrder) {
		this.sortableArgs4displayOrder = sortableArgs4displayOrder;
	}

	public JSONArray getSortableEcho4displayOrder() {
		return sortableEcho4displayOrder;
	}

	public JSONObject getSortableResult4displayOrder() {
		return sortableResult4displayOrder;
	}

}
