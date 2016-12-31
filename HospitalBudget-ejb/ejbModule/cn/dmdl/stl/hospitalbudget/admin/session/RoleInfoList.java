package cn.dmdl.stl.hospitalbudget.admin.session;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.admin.entity.RoleInfo;
import cn.dmdl.stl.hospitalbudget.common.session.CriterionEntityQuery;
import cn.dmdl.stl.hospitalbudget.util.GlobalConstant;

@Name("roleInfoList")
public class RoleInfoList extends CriterionEntityQuery<RoleInfo> {

	private static final long serialVersionUID = 1L;
	private static final String EJBQL = "select roleInfo from RoleInfo roleInfo where roleInfo.deleted = 0";
	private RoleInfo roleInfo = new RoleInfo();
	private String keyword;// 关键词

	@Override
	protected Query createQuery() {
		String sql = EJBQL;
		sql += " and roleInfo.roleInfoId != " + GlobalConstant.ROLE_OF_ROOT;
		if (keyword != null && !"".equals(keyword)) {
			sql += " and roleInfo.roleName like '%" + keyword + "%'";
		}
		setEjbql(sql);
		return super.createQuery();
	}

	public RoleInfoList() {
		setEjbql(EJBQL);
		setAttribute("roleInfo.roleInfoId");
	}

	public RoleInfo getRoleInfo() {
		return roleInfo;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
}
