package cn.dmdl.stl.hospitalbudget.admin.session;

import java.util.Date;

import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.admin.entity.PermissionInfo;
import cn.dmdl.stl.hospitalbudget.common.session.CriterionEntityHome;
import cn.dmdl.stl.hospitalbudget.util.Assit;

@Name("permissionInfoHome")
public class PermissionInfoHome extends CriterionEntityHome<PermissionInfo> {

	private static final long serialVersionUID = 1L;

	@Override
	public String persist() {
		setMessage("");

		if (Assit.getResultSetSize("select permission_info_id from permission_info where deleted = 0 and permission_name = '" + instance.getPermissionName() + "'") > 0) {
			setMessage("此名称太受欢迎,请更换一个");
			return null;
		}

		if (Assit.getResultSetSize("select permission_info_id from permission_info where deleted = 0 and permission_key = '" + instance.getPermissionKey() + "'") > 0) {
			setMessage("此键太受欢迎,请更换一个");
			return null;
		}

		instance.setInsertTime(new Date());
		instance.setInsertUser(sessionToken.getUserInfoId());
		getEntityManager().persist(instance);
		getEntityManager().flush();
		raiseAfterTransactionSuccessEvent();
		return "persisted";
	}

	@Override
	public String update() {
		setMessage("");

		if (Assit.getResultSetSize("select permission_info_id from permission_info where deleted = 0 and permission_name = '" + instance.getPermissionName() + "' and permission_info_id != " + instance.getPermissionInfoId()) > 0) {
			setMessage("此名称太受欢迎,请更换一个");
			return null;
		}

		if (Assit.getResultSetSize("select permission_info_id from permission_info where deleted = 0 and permission_key = '" + instance.getPermissionKey() + "' and permission_info_id != " + instance.getPermissionInfoId()) > 0) {
			setMessage("此键太受欢迎,请更换一个");
			return null;
		}

		joinTransaction();
		instance.setUpdateTime(new Date());
		instance.setUpdateUser(sessionToken.getUserInfoId());
		getEntityManager().flush();
		raiseAfterTransactionSuccessEvent();
		return "updated";
	}

	@Override
	public String remove() {
		setMessage("");

		getInstance();

		if (Assit.getResultSetSize("select permission_info_id from permission_assignment_extend where permission_info_id = " + instance.getPermissionInfoId()) > 0) {
			setMessage("该权限已被使用，操作被取消");
			return "failure";
		}

		if (Assit.getResultSetSize("select permission_info_id from role_permission where permission_info_id = " + instance.getPermissionInfoId()) > 0) {
			setMessage("该权限已被使用，操作被取消");
			return "failure";
		}

		instance.setDeleted(true);
		getEntityManager().flush();
		raiseAfterTransactionSuccessEvent();
		return "removed";
	}

	public void setPermissionInfoPermissionInfoId(Integer id) {
		setId(id);
	}

	public Integer getPermissionInfoPermissionInfoId() {
		return (Integer) getId();
	}

	@Override
	protected PermissionInfo createInstance() {
		PermissionInfo permissionInfo = new PermissionInfo();
		return permissionInfo;
	}

	public void load() {
		if (isIdDefined()) {
			wire();
		}
	}

	public void wire() {
		getInstance();
	}

	public boolean isWired() {
		return true;
	}

	public PermissionInfo getDefinedInstance() {
		return isIdDefined() ? getInstance() : null;
	}

}
