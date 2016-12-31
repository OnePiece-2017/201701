package cn.dmdl.stl.hospitalbudget.misc;

import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.admin.entity.UserInfo;
import cn.dmdl.stl.hospitalbudget.boot.ConfigureCache;
import cn.dmdl.stl.hospitalbudget.common.session.CriterionEntityHome;
import cn.dmdl.stl.hospitalbudget.util.GlobalConstant;
import cn.dmdl.stl.hospitalbudget.util.MD5;

@Name("modifyUserPasswordHome")
public class ModifyUserPasswordHome extends CriterionEntityHome<UserInfo> {

	private static final long serialVersionUID = 1L;
	private String password;// 密码

	@Override
	public String update() {
		setMessage("");

		if (instance.getRoleInfo().getRoleInfoId() == GlobalConstant.ROLE_OF_ROOT) {
			setMessage(ConfigureCache.getMessageValue("reject_request"));
			return null;
		}

		joinTransaction();
		instance.setPassword(MD5.getMD5Alpha(password));
		getEntityManager().flush();
		raiseAfterTransactionSuccessEvent();
		return "updated";
	}

	public void setUserInfoUserInfoId(Integer id) {
		setId(id);
	}

	public Integer getUserInfoUserInfoId() {
		return (Integer) getId();
	}

	@Override
	protected UserInfo createInstance() {
		UserInfo userInfo = new UserInfo();
		return userInfo;
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

	public UserInfo getDefinedInstance() {
		return isIdDefined() ? getInstance() : null;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
