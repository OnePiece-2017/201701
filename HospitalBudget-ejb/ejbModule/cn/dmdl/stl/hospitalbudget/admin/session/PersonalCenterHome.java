package cn.dmdl.stl.hospitalbudget.admin.session;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.admin.entity.SystemTheme;
import cn.dmdl.stl.hospitalbudget.admin.entity.UserInfo;
import cn.dmdl.stl.hospitalbudget.admin.entity.UserInfoExtend;
import cn.dmdl.stl.hospitalbudget.common.session.CriterionEntityHome;
import cn.dmdl.stl.hospitalbudget.util.DateTimeHelper;

@Name("personalCenterHome")
public class PersonalCenterHome extends CriterionEntityHome<UserInfo> {

	private static final long serialVersionUID = 1L;
	private JSONObject viewData;// 查看页数据
	private String birthday;// 生日
	List<Object[]> systemThemeList;
	private Integer systemThemeId;

	@Override
	public String update() {
		setMessage("");

		joinTransaction();
		instance.setUpdateTime(new Date());
		instance.setUpdateUser(sessionToken.getUserInfoId());
		UserInfoExtend userInfoExtend = instance.getUserInfoExtend();
		userInfoExtend.setUpdateTime(new Date());
		userInfoExtend.setUpdateUser(sessionToken.getUserInfoId());
		if (systemThemeId != null) {
			instance.setSystemTheme(getEntityManager().find(SystemTheme.class, systemThemeId));
		} else {
			instance.setSystemTheme(null);
		}
		getEntityManager().flush();
		raiseAfterTransactionSuccessEvent();
		return "updated";
	}

	public void initViewPage() {
		viewData = new JSONObject();
		UserInfo userInfo = getEntityManager().find(UserInfo.class, sessionToken.getUserInfoId());
		UserInfoExtend userInfoExtend = userInfo.getUserInfoExtend();
		viewData.accumulate("userInfoId", userInfo.getUserInfoId());
		viewData.accumulate("username", userInfo.getUsername());
		viewData.accumulate("roleName", userInfo.getRoleInfo().getRoleName());
		viewData.accumulate("fullname", userInfoExtend.getFullname());
		viewData.accumulate("sex", userInfoExtend.getSex() == 1 ? "男" : "女");
		viewData.accumulate("mail", userInfoExtend.getMail() != null ? userInfoExtend.getMail() : "");
		viewData.accumulate("insertTime", DateTimeHelper.dateToStr(userInfo.getInsertTime(), DateTimeHelper.PATTERN_DATE_TIME));
		viewData.accumulate("currentSystemTheme", sessionToken.getSystemThemeName() != null ? sessionToken.getSystemThemeName() : "默认");
		viewData.accumulate("currentSystemThemeSource", sessionToken.getSystemThemeNameSource());
		viewData.accumulate("mySystemTheme", userInfo.getSystemTheme() != null ? userInfo.getSystemTheme().getTheValue() : "默认");
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
		setId(sessionToken.getUserInfoId());// 获取当前登录人实例
		getInstance();

		if (firstTime) {
			wireSystemThemeList();
			systemThemeId = instance.getSystemTheme() != null ? instance.getSystemTheme().getTheId() : null;

			if (isManaged()) {
				UserInfoExtend userInfoExtend = instance.getUserInfoExtend();
				userInfoExtend.setSex(userInfoExtend.getSex() != 0 ? userInfoExtend.getSex() : 1);
			}

			firstTime = false;// 修改首次标记
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

	public boolean isWired() {
		return true;
	}

	public UserInfo getDefinedInstance() {
		return isIdDefined() ? getInstance() : null;
	}

	public JSONObject getViewData() {
		return viewData;
	}

	public String getBirthday() {
		return birthday;
	}

	public void setBirthday(String birthday) {
		this.birthday = birthday;
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
