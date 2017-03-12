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
	private UserInfoExtend userInfoExtend;// 用户信息-扩展
	private String birthday;// 生日
	private String interest;// 兴趣
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
		if (birthday != null && !"".equals(birthday)) {
			userInfoExtend.setBirthday(DateTimeHelper.strToDate(birthday, DateTimeHelper.PATTERN_DATE));
		} else {
			userInfoExtend.setBirthday(null);
		}
		if (systemThemeId != null) {
			instance.setSystemTheme(getEntityManager().find(SystemTheme.class, systemThemeId));
		} else {
			instance.setSystemTheme(null);
		}
		userInfoExtend.setInterest(interest);
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

	public void initViewPage() {
		viewData = new JSONObject();
		UserInfo userInfo = getEntityManager().find(UserInfo.class, sessionToken.getUserInfoId());
		UserInfoExtend userInfoExtend = userInfo.getUserInfoExtend();
		viewData.accumulate("userInfoId", userInfo.getUserInfoId());
		viewData.accumulate("username", userInfo.getUsername());
		viewData.accumulate("roleName", userInfo.getRoleInfo().getRoleName());
		viewData.accumulate("nickname", userInfoExtend.getNickname());
		viewData.accumulate("fullname", userInfoExtend.getFullname());
		viewData.accumulate("sex", userInfoExtend.getSex() == 1 ? "男" : "女");
		viewData.accumulate("birthday", userInfoExtend.getBirthday() != null ? DateTimeHelper.dateToStr(userInfoExtend.getBirthday(), DateTimeHelper.PATTERN_CN_DATE) : "");
		viewData.accumulate("cellphone", userInfoExtend.getCellphone() != null ? userInfoExtend.getCellphone() : "");
		viewData.accumulate("telephone", userInfoExtend.getTelephone() != null ? userInfoExtend.getTelephone() : "");
		viewData.accumulate("mail", userInfoExtend.getMail() != null ? userInfoExtend.getMail() : "");
		viewData.accumulate("address", userInfoExtend.getAddress() != null ? userInfoExtend.getAddress() : "");
		viewData.accumulate("interest", userInfoExtend.getInterest() != null ? userInfoExtend.getInterest() : "");
		viewData.accumulate("insertTime", DateTimeHelper.dateToStr(userInfo.getInsertTime(), DateTimeHelper.PATTERN_DATE_TIME));
		viewData.accumulate("currentSystemTheme", sessionToken.getSystemThemeName());
		viewData.accumulate("mySystemTheme", userInfo.getSystemTheme() != null ? userInfo.getSystemTheme().getTheValue() : "未选择");
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

			firstTime = false;// 修改首次标记
		}

		System.out.println(instance.getUsername());

		if (isManaged()) {
			userInfoExtend = instance.getUserInfoExtend();
			userInfoExtend.setSex(userInfoExtend.getSex() != 0 ? userInfoExtend.getSex() : 1);
			if (instance.getUserInfoExtend().getBirthday() != null) {
				birthday = birthday != null ? birthday : DateTimeHelper.dateToStr(instance.getUserInfoExtend().getBirthday(), DateTimeHelper.PATTERN_DATE);
			}
			interest = interest != null ? interest : instance.getUserInfoExtend().getInterest();
		} else {
			userInfoExtend = userInfoExtend != null ? userInfoExtend : new UserInfoExtend();
			userInfoExtend.setSex(userInfoExtend.getSex() != 0 ? userInfoExtend.getSex() : 1);
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

	public UserInfoExtend getUserInfoExtend() {
		return userInfoExtend;
	}

	public void setUserInfoExtend(UserInfoExtend userInfoExtend) {
		this.userInfoExtend = userInfoExtend;
	}

	public String getBirthday() {
		return birthday;
	}

	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}

	public String getInterest() {
		return interest;
	}

	public void setInterest(String interest) {
		this.interest = interest;
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
