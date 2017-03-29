package cn.dmdl.stl.hospitalbudget.admin.session;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.admin.entity.UserInfo;
import cn.dmdl.stl.hospitalbudget.common.session.CriterionEntityQuery;
import cn.dmdl.stl.hospitalbudget.util.GlobalConstant;

@Name("userInfoList")
public class UserInfoList extends CriterionEntityQuery<UserInfo> {

	private static final long serialVersionUID = 1L;
	private static final String EJBQL = "select userInfo from UserInfo userInfo where userInfo.deleted = 0";
	private UserInfo userInfo = new UserInfo();

	@Override
	protected Query createQuery() {
		StringBuffer sql = new StringBuffer(EJBQL);
		sql.append(" and userInfo.roleInfo.roleInfoId != " + GlobalConstant.ROLE_OF_ROOT);
		if (keyword != null && !"".equals(keyword)) {
			sql.append(" and (userInfo.username like '%" + keyword + "%'");
			sql.append(" or userInfo.userInfoExtend.fullname like '%" + keyword + "%'");
			sql.append(" )");
		}
		setEjbql(sql.toString());
		return super.createQuery();
	}

	public UserInfoList() {
		setEjbql(EJBQL);
		setAttribute("userInfo.userInfoId");
	}

	public UserInfo getUserInfo() {
		return userInfo;
	}

}
