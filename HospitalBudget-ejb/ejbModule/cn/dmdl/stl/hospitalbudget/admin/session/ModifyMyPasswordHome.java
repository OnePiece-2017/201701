package cn.dmdl.stl.hospitalbudget.admin.session;

import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.admin.entity.UserInfo;
import cn.dmdl.stl.hospitalbudget.common.session.CriterionEntityHome;
import cn.dmdl.stl.hospitalbudget.util.MD5;

@Name("modifyMyPasswordHome")
public class ModifyMyPasswordHome extends CriterionEntityHome<UserInfo> {

	private static final long serialVersionUID = 1L;
	private String oldPassword;// 旧密码
	private String newPassword;// 新密码

	@Override
	public String update() {
		if (null == oldPassword) {
			setMessage("旧密码不能为null值！");
			return null;
		} else if (!instance.getPassword().equals(MD5.getMD5Alpha(oldPassword))) {
			setMessage("旧密码错误！");
			return null;
		} else {
			joinTransaction();
			instance.setPassword(MD5.getMD5Alpha(newPassword));
			getEntityManager().flush();
			raiseAfterTransactionSuccessEvent();
			setMessage("修改成功！");
			return "updated";
		}
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
		setId(sessionToken.getUserInfoId());// 获取当前登录人实例
		getInstance();
	}

	public boolean isWired() {
		return true;
	}

	public UserInfo getDefinedInstance() {
		return isIdDefined() ? getInstance() : null;
	}

	public String getOldPassword() {
		return oldPassword;
	}

	public void setOldPassword(String oldPassword) {
		this.oldPassword = oldPassword;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

}
