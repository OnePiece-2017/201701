package cn.dmdl.stl.hospitalbudget.admin.session;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.admin.entity.RoleInfo;
import cn.dmdl.stl.hospitalbudget.admin.entity.UserInfo;
import cn.dmdl.stl.hospitalbudget.admin.entity.UserInfoExtend;
import cn.dmdl.stl.hospitalbudget.boot.ConfigureCache;
import cn.dmdl.stl.hospitalbudget.common.session.CriterionEntityHome;
import cn.dmdl.stl.hospitalbudget.hospital.entity.YsDepartmentInfo;
import cn.dmdl.stl.hospitalbudget.util.Assit;
import cn.dmdl.stl.hospitalbudget.util.DateTimeHelper;
import cn.dmdl.stl.hospitalbudget.util.GlobalConstant;
import cn.dmdl.stl.hospitalbudget.util.MD5;

@Name("userInfoHome")
public class UserInfoHome extends CriterionEntityHome<UserInfo> {

	private static final long serialVersionUID = 1L;
	private List<Object[]> roleInfoList;// 角色select
	private Integer roleInfoId;// 角色value
	private UserInfoExtend userInfoExtend;// 用户信息-扩展
	private String password;// 密码
	private String birthday;// 生日
	private String interest;// 兴趣
	private List<Object[]> departmentInfoList;// 科室select
	private Integer departmentInfoId;// 科室id
	private boolean isFirstTime;// 首次标记（提交表单后返回错误状态重载页面控件，如下拉框。。。）

	@Override
	public String persist() {
		setMessage("");

		if (null == roleInfoId || GlobalConstant.ROLE_OF_ROOT == roleInfoId) {
			setMessage(ConfigureCache.getMessageValue("reject_request"));
			return null;
		}

		if (Assit.getResultSetSize("select user_info_id from user_info where deleted = 0 and binary username = '" + instance.getUsername() + "'") > 0) {
			setMessage("此用户名太受欢迎,请更换一个");
			return null;
		}

		instance.setRoleInfo(getEntityManager().find(RoleInfo.class, roleInfoId));
		if (departmentInfoId != null) {
			instance.setYsDepartmentInfo(getEntityManager().find(YsDepartmentInfo.class, departmentInfoId));
		} else {
			instance.setYsDepartmentInfo(null);
		}
		instance.setPassword(MD5.getMD5Alpha(password));
		instance.setInsertTime(new Date());
		instance.setInsertUser(sessionToken.getUserInfoId());
		userInfoExtend.setInsertTime(new Date());
		userInfoExtend.setInsertUser(sessionToken.getUserInfoId());
		if (birthday != null && !"".equals(birthday)) {
			userInfoExtend.setBirthday(DateTimeHelper.strToDate(birthday, DateTimeHelper.PATTERN_DATE));
		} else {
			userInfoExtend.setBirthday(null);
		}
		userInfoExtend.setInterest(interest);
		getEntityManager().persist(userInfoExtend);
		instance.setUserInfoExtend(userInfoExtend);
		getEntityManager().persist(instance);

		getEntityManager().flush();
		raiseAfterTransactionSuccessEvent();
		return "persisted";
	}

	@Override
	public String update() {
		setMessage("");

		if (GlobalConstant.ROLE_OF_ROOT == instance.getRoleInfo().getRoleInfoId() || null == roleInfoId || GlobalConstant.ROLE_OF_ROOT == roleInfoId) {
			setMessage(ConfigureCache.getMessageValue("reject_request"));
			return null;
		}

		if (Assit.getResultSetSize("select user_info_id from user_info where deleted = 0 and binary username = '" + instance.getUsername() + "' and user_info_id != " + instance.getUserInfoId()) > 0) {
			setMessage("此用户名太受欢迎,请更换一个");
			return null;
		}

		joinTransaction();
		instance.setRoleInfo(getEntityManager().find(RoleInfo.class, roleInfoId));
		if (departmentInfoId != null) {
			instance.setYsDepartmentInfo(getEntityManager().find(YsDepartmentInfo.class, departmentInfoId));
		} else {
			instance.setYsDepartmentInfo(null);
		}
		instance.setUpdateTime(new Date());
		instance.setUpdateUser(sessionToken.getUserInfoId());
		userInfoExtend.setUpdateTime(new Date());
		userInfoExtend.setUpdateUser(sessionToken.getUserInfoId());
		if (birthday != null && !"".equals(birthday)) {
			userInfoExtend.setBirthday(DateTimeHelper.strToDate(birthday, DateTimeHelper.PATTERN_DATE));
		} else {
			userInfoExtend.setBirthday(null);
		}
		userInfoExtend.setInterest(interest);
		getEntityManager().flush();
		raiseAfterTransactionSuccessEvent();
		return "updated";
	}

	@Override
	public String remove() {
		setMessage("");

		getInstance();

		if (GlobalConstant.ROLE_OF_ROOT == instance.getRoleInfo().getRoleInfoId()) {
			setMessage(ConfigureCache.getMessageValue("reject_request"));
			return "failure";
		}

		instance.setDeleted(true);
		instance.getUserInfoExtend().setDeleted(true);
		getEntityManager().flush();
		raiseAfterTransactionSuccessEvent();
		return "removed";
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

	/** 递归处理子节点 */
	public void disposeLeaf(List<Object[]> targetList, Map<Object, List<Object>> nexusMap, Map<Object, Object> valueMap, int indentWidth, List<Object> leafList) {
		if (leafList != null && leafList.size() > 0) {
			for (Object leaf : leafList) {
				String indentStr = "";
				for (int i = 0; i < indentWidth; i++) {
					indentStr += "　";
				}
				targetList.add(new Object[] { leaf, indentStr + valueMap.get(leaf) });
				disposeLeaf(targetList, nexusMap, valueMap, indentWidth + 1, nexusMap.get(leaf));
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void wire() {
		getInstance();
		if (roleInfoList != null) {
			roleInfoList.clear();
		} else {
			roleInfoList = new ArrayList<Object[]>();
		}
		roleInfoList.add(new Object[] { "", "请选择" });
		List<Object[]> roleInfoListTmp = getEntityManager().createNativeQuery("select role_info_id, role_name from role_info where deleted = 0 and role_info_id != " + GlobalConstant.ROLE_OF_ROOT).getResultList();
		if (roleInfoListTmp != null && roleInfoListTmp.size() > 0) {
			roleInfoList.addAll(roleInfoListTmp);
		}

		if (departmentInfoList != null) {
			departmentInfoList.clear();
		} else {
			departmentInfoList = new ArrayList<Object[]>();
		}
		departmentInfoList.add(new Object[] { "", "请选择" });
		Map<Object, List<Object>> nexusMap = new HashMap<Object, List<Object>>();// 上下级关系集合
		Map<Object, Object> valueMap = new HashMap<Object, Object>();// 值集合
		String dataSql = "select the_id, the_pid, the_value from ys_department_info where deleted = 0";
		List<Object[]> dataList = getEntityManager().createNativeQuery(dataSql).getResultList();
		if (dataList != null && dataList.size() > 0) {
			for (Object[] data : dataList) {
				valueMap.put(data[0], data[2]);
				List<Object> leafList = nexusMap.get(data[1]);
				if (leafList != null) {
					leafList.add(data[0]);
				} else {
					leafList = new ArrayList<Object>();
					leafList.add(data[0]);
					nexusMap.put(data[1], leafList);
				}
			}
		}
		List<Object> rootList = nexusMap.get(null);
		if (rootList != null && rootList.size() > 0) {
			for (Object root : rootList) {
				departmentInfoList.add(new Object[] { root, valueMap.get(root) });
				disposeLeaf(departmentInfoList, nexusMap, valueMap, 1, nexusMap.get(root));
			}
		}

		if (isManaged()) {
			roleInfoId = roleInfoId != null ? roleInfoId : instance.getRoleInfo().getRoleInfoId();
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

		if (!isFirstTime) {
			departmentInfoId = instance.getYsDepartmentInfo() != null ? instance.getYsDepartmentInfo().getTheId() : null;
			isFirstTime = true;
		}
	}

	public boolean isWired() {
		return true;
	}

	public UserInfo getDefinedInstance() {
		return isIdDefined() ? getInstance() : null;
	}

	public List<Object[]> getRoleInfoList() {
		return roleInfoList;
	}

	public void setRoleInfoList(List<Object[]> roleInfoList) {
		this.roleInfoList = roleInfoList;
	}

	public Integer getRoleInfoId() {
		return roleInfoId;
	}

	public void setRoleInfoId(Integer roleInfoId) {
		this.roleInfoId = roleInfoId;
	}

	public UserInfoExtend getUserInfoExtend() {
		return userInfoExtend;
	}

	public void setUserInfoExtend(UserInfoExtend userInfoExtend) {
		this.userInfoExtend = userInfoExtend;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
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

	public List<Object[]> getDepartmentInfoList() {
		return departmentInfoList;
	}

	public Integer getDepartmentInfoId() {
		return departmentInfoId;
	}

	public void setDepartmentInfoId(Integer departmentInfoId) {
		this.departmentInfoId = departmentInfoId;
	}

}
