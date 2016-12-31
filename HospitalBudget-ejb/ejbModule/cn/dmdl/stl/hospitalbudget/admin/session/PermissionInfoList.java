package cn.dmdl.stl.hospitalbudget.admin.session;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.admin.entity.PermissionInfo;
import cn.dmdl.stl.hospitalbudget.common.session.CriterionEntityQuery;

@Name("permissionInfoList")
public class PermissionInfoList extends CriterionEntityQuery<PermissionInfo> {

	private static final long serialVersionUID = 1L;
	private static final String EJBQL = "select permissionInfo from PermissionInfo permissionInfo where permissionInfo.deleted = 0";
	private PermissionInfo permissionInfo = new PermissionInfo();
	private String keyword;// 关键词

	@Override
	protected Query createQuery() {
		String sql = EJBQL;
		if (keyword != null && !"".equals(keyword)) {
			sql += " and (permissionInfo.permissionName like '%" + keyword + "%' or permissionInfo.permissionKey like '%" + keyword + "%' or permissionInfo.description like '%" + keyword + "%')";
		}
		setEjbql(sql);
		return super.createQuery();
	}

	public PermissionInfoList() {
		setEjbql(EJBQL);
		setAttribute("permissionInfo.permissionInfoId");
	}

	public PermissionInfo getPermissionInfo() {
		return permissionInfo;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
}
