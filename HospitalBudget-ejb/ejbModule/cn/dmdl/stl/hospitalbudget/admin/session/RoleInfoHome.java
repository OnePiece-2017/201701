package cn.dmdl.stl.hospitalbudget.admin.session;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.admin.entity.RoleInfo;
import cn.dmdl.stl.hospitalbudget.admin.entity.SystemTheme;
import cn.dmdl.stl.hospitalbudget.boot.ConfigureCache;
import cn.dmdl.stl.hospitalbudget.common.session.CriterionEntityHome;
import cn.dmdl.stl.hospitalbudget.util.Assit;
import cn.dmdl.stl.hospitalbudget.util.GlobalConstant;

@Name("roleInfoHome")
public class RoleInfoHome extends CriterionEntityHome<RoleInfo> {

	private static final long serialVersionUID = 1L;
	List<Object[]> systemThemeList;
	private Integer systemThemeId;

	@Override
	public String persist() {
		setMessage("");

		if (Assit.getResultSetSize("select role_info_id from role_info where role_name = '" + instance.getRoleName() + "'") > 0) {
			setMessage("此名称太受欢迎,请更换一个");
			return null;
		}

		if (systemThemeId != null) {
			instance.setSystemTheme(getEntityManager().find(SystemTheme.class, systemThemeId));
		} else {
			instance.setSystemTheme(null);
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

		if (GlobalConstant.ROLE_OF_ROOT == instance.getRoleInfoId()) {
			setMessage(ConfigureCache.getMessageValue("reject_request"));
			return null;
		}

		if (Assit.getResultSetSize("select role_info_id from role_info where role_name = '" + instance.getRoleName() + "' and role_info_id != " + instance.getRoleInfoId()) > 0) {
			setMessage("此名称太受欢迎,请更换一个");
			return null;
		}

		if (systemThemeId != null) {
			instance.setSystemTheme(getEntityManager().find(SystemTheme.class, systemThemeId));
		} else {
			instance.setSystemTheme(null);
		}
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

		if (instance.getRoleInfoId() == GlobalConstant.ROLE_OF_ROOT) {
			setMessage(ConfigureCache.getMessageValue("reject_request"));
			log.warn("警告：正在尝试删除系统内置角色");
			return "failure";
		}

		if (Assit.getResultSetSize("select user_info_id from user_info where deleted = 0 and role_info_id = " + instance.getRoleInfoId()) > 0) {
			setMessage("该项已被使用，操作被取消");
			return "failure";
		}

		getEntityManager().createNativeQuery("delete from role_permission where role_info_id = " + instance.getRoleInfoId()).executeUpdate();

		instance.setDeleted(true);
		getEntityManager().flush();
		raiseAfterTransactionSuccessEvent();
		return "removed";
	}

	/** 初始化系统主题 */
	@SuppressWarnings("unchecked")
	public void wireSystemThemeList() {
		if (systemThemeList != null) {
			systemThemeList.clear();
		} else {
			systemThemeList = new ArrayList<Object[]>();
		}
		List<Object[]> list = getEntityManager().createNativeQuery("select the_id, the_value from system_theme where enabled = 1").getResultList();
		if (list != null && list.size() > 0) {
			systemThemeList.addAll(list);
		}
	}

	public void wire() {
		getInstance();

		if (firstTime) {
			wireSystemThemeList();
			systemThemeId = instance.getSystemTheme() != null ? instance.getSystemTheme().getTheId() : null;

			firstTime = false;// 修改首次标记
		}

	}

	public void setRoleInfoRoleInfoId(Integer id) {
		setId(id);
	}

	public Integer getRoleInfoRoleInfoId() {
		return (Integer) getId();
	}

	@Override
	protected RoleInfo createInstance() {
		RoleInfo roleInfo = new RoleInfo();
		return roleInfo;
	}

	public void load() {
		if (isIdDefined()) {
			wire();
		}
	}

	public boolean isWired() {
		return true;
	}

	public RoleInfo getDefinedInstance() {
		return isIdDefined() ? getInstance() : null;
	}

	public List<Object[]> getSystemThemeList() {
		return systemThemeList;
	}

	public Integer getSystemThemeId() {
		return systemThemeId;
	}

	public void setSystemThemeId(Integer systemThemeId) {
		this.systemThemeId = systemThemeId;
	}

}
