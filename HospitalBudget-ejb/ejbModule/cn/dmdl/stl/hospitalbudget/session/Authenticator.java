package cn.dmdl.stl.hospitalbudget.session;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Credentials;
import org.jboss.seam.security.Identity;

import cn.dmdl.stl.hospitalbudget.admin.entity.LoginInfo;
import cn.dmdl.stl.hospitalbudget.admin.entity.MenuInfo;
import cn.dmdl.stl.hospitalbudget.admin.entity.UserInfo;
import cn.dmdl.stl.hospitalbudget.boot.ConfigureCache;
import cn.dmdl.stl.hospitalbudget.util.CommonToolLocal;
import cn.dmdl.stl.hospitalbudget.util.GlobalConstant;
import cn.dmdl.stl.hospitalbudget.util.MD5;
import cn.dmdl.stl.hospitalbudget.util.SessionToken;

@Stateful
@Name("authenticator")
public class Authenticator implements AuthenticatorLocal {

	@In(create = true)
	SessionToken sessionToken;

	@Logger
	private Log log;

	@In
	Identity identity;

	@In
	Credentials credentials;

	@PersistenceContext
	EntityManager entityManager;

	@In
	protected FacesMessages facesMessages;

	@In(create = true)
	private CommonToolLocal commonTool;

	@SuppressWarnings("unchecked")
	public boolean authenticate() {
		// TODO: 防注入，防黑机制，机器登录检测。
		sessionToken.setMessage("");
		int whoami = Integer.parseInt(ConfigureCache.getProjectValue("whoami"));
		log.info("whoami-->" + (1 == whoami ? "development environment" : (2 == whoami ? "test environment" : (3 == whoami ? "regular environment" : "?"))));
		log.info("authenticating [username:{0}][password:{1}].", credentials.getUsername(), credentials.getPassword());

		List<Object> userInfoIdList = entityManager.createNativeQuery("select user_info_id from user_info where deleted = 0 and enabled = 1 and binary username = '" + credentials.getUsername() + "'").getResultList();
		if (userInfoIdList != null && userInfoIdList.size() > 0) {
			UserInfo userInfo = entityManager.find(UserInfo.class, Integer.valueOf(userInfoIdList.get(0).toString()));
			if (credentials.getPassword() != null && userInfo.getPassword().equals(MD5.getMD5Alpha(credentials.getPassword()))) {
				log.info("{0} login.", credentials.getUsername());
				String permissionKeySql = "select permission_key from permission_info where deleted = 0";
				if (GlobalConstant.ROLE_OF_ROOT != userInfo.getRoleInfo().getRoleInfoId()) {
					permissionKeySql += " and permission_info_id in (select permission_info_id from role_permission where role_info_id = " + userInfo.getRoleInfo().getRoleInfoId() + ")";
				}
				List<Object> permissionKeyList = entityManager.createNativeQuery(permissionKeySql).getResultList();
				if (permissionKeyList != null && permissionKeyList.size() > 0) {
					for (Object permissionKey : permissionKeyList) {
						identity.addRole(permissionKey.toString());
					}
				}
				identity.addRole("?");// <restrict>#{s:hasRole('?')}</restrict>

				sessionToken.setUserInfoId(userInfo.getUserInfoId());
				sessionToken.setRoleInfoId(userInfo.getRoleInfo().getRoleInfoId());
				sessionToken.setUsername(userInfo.getUsername());
				sessionToken.setFullname(userInfo.getUserInfoExtend().getFullname());

				// 设置主题（优先级：用户>角色>系统）
				String systemThemeName = null;
				String systemThemeNameSource = "﹝继承系统﹞";// 默认显示继承系统
				String systemThemeCssPath = null;
				if (userInfo.getSystemTheme() != null && userInfo.getSystemTheme().isEnabled()) {
					systemThemeName = userInfo.getSystemTheme().getTheValue();
					systemThemeNameSource = "﹝私有﹞";
					systemThemeCssPath = userInfo.getSystemTheme().getCssPath();
				} else if (userInfo.getRoleInfo().getSystemTheme() != null && userInfo.getRoleInfo().getSystemTheme().isEnabled()) {
					systemThemeName = userInfo.getRoleInfo().getSystemTheme().getTheValue();
					systemThemeNameSource = "﹝继承角色﹞";
					systemThemeCssPath = userInfo.getRoleInfo().getSystemTheme().getCssPath();
				} else {
					List<Object[]> systemThemeList = entityManager.createNativeQuery("select the_value, css_path from system_theme where enabled = 1 and the_id in (select the_value from system_settings where the_key = 'system_theme')").getResultList();
					if (systemThemeList != null && systemThemeList.size() > 0) {
						systemThemeName = systemThemeList.get(0)[0].toString();
						systemThemeCssPath = systemThemeList.get(0)[1].toString();
					}
				}
				sessionToken.setSystemThemeName(systemThemeName);
				sessionToken.setSystemThemeNameSource(systemThemeNameSource);
				sessionToken.setSystemThemeCssPath(systemThemeCssPath);

				// 设置科室
				sessionToken.setDepartmentInfoId(userInfo.getYsDepartmentInfo() != null ? userInfo.getYsDepartmentInfo().getTheId() : null);

				return true;
			} else {
				sessionToken.setMessage(ConfigureCache.getMessageValue("incorrect_username_or_password"));
				log.info("authenticating {0} password wrong.", credentials.getUsername());
				return false;
			}
		} else {
			sessionToken.setMessage(ConfigureCache.getMessageValue("incorrect_username_or_password"));
			log.info("authenticating {0} username wrong.", credentials.getUsername());
			return false;
		}
	}

	/** 拉取菜单信息 */
	@SuppressWarnings("unchecked")
	private JSONArray pullMenuInfo() {
		JSONArray result = new JSONArray();
		Map<Integer, List<Integer>> nexusMap = new HashMap<Integer, List<Integer>>();// 上下级关系集合
		final Map<Integer, MenuInfo> valueMap = new HashMap<Integer, MenuInfo>();// 值集合
		String dataSql = "select menuInfo from MenuInfo menuInfo where menuInfo.deleted = 0";
		dataSql += " order by menuInfo.displayOrder asc";// 注意：这个排序至关重要，避免使用Collections.sort降低效率。
		List<MenuInfo> menuInfoList = entityManager.createQuery(dataSql).getResultList();
		if (menuInfoList != null && menuInfoList.size() > 0) {
			for (MenuInfo menuInfo : menuInfoList) {
				if (null == menuInfo.getPermissionKey() || "".equals(menuInfo.getPermissionKey()) || identity.hasRole(menuInfo.getPermissionKey())) {
					Integer theId = menuInfo.getTheId();
					Integer thePid = menuInfo.getMenuInfo() != null ? menuInfo.getMenuInfo().getTheId() : null;
					valueMap.put(theId, menuInfo);
					List<Integer> leafList = nexusMap.get(thePid);
					if (leafList != null) {
						leafList.add(theId);
					} else {
						leafList = new ArrayList<Integer>();
						leafList.add(theId);
						nexusMap.put(thePid, leafList);
					}
				}
			}
		}

		List<Integer> rootList = nexusMap.get(null);
		if (rootList != null && rootList.size() > 0) {
			for (Integer root : rootList) {
				JSONObject nodeTmp = new JSONObject();
				nodeTmp.accumulate("theId", valueMap.get(root).getTheId());
				nodeTmp.accumulate("theValue", valueMap.get(root).getTheValue());
				nodeTmp.accumulate("iconSrc", valueMap.get(root).getIconSrc());
				nodeTmp.accumulate("tabTitle", valueMap.get(root).getTabTitle());
				nodeTmp.accumulate("tabUrl", valueMap.get(root).getTabUrl());
				nodeTmp.accumulate("leaf", new JSONArray());
				result.add(nodeTmp);
				disposeLeaf(result.getJSONObject(result.size() - 1), nexusMap, valueMap, nexusMap.get(root));
			}
		}
		return result;
	}

	/** 递归处理子节点 */
	private void disposeLeaf(JSONObject node, Map<Integer, List<Integer>> nexusMap, Map<Integer, MenuInfo> valueMap, List<Integer> leafList) {
		if (leafList != null && leafList.size() > 0) {
			for (Integer leaf : leafList) {
				JSONArray leafArr = node.getJSONArray("leaf");
				JSONObject leafTmp = new JSONObject();
				leafTmp.accumulate("theId", valueMap.get(leaf).getTheId());
				leafTmp.accumulate("theValue", valueMap.get(leaf).getTheValue());
				leafTmp.accumulate("iconSrc", valueMap.get(leaf).getIconSrc());
				leafTmp.accumulate("tabTitle", valueMap.get(leaf).getTabTitle());
				leafTmp.accumulate("tabUrl", valueMap.get(leaf).getTabUrl());
				leafTmp.accumulate("leaf", new JSONArray());
				leafArr.add(leafTmp);
				disposeLeaf(leafArr.getJSONObject(leafArr.size() - 1), nexusMap, valueMap, nexusMap.get(leaf));
			}
		}
	}

	public void excludeInvalidFunction(JSONArray pmi) {
		for (int i = 0; i < pmi.size(); i++) {
			if (JSONNull.getInstance().equals(pmi.getJSONObject(i).get("tabUrl")) && 0 == pmi.getJSONObject(i).getJSONArray("leaf").size()) {
				pmi.getJSONObject(i).accumulate("deleted", true);
				pmi.discard(i);
			} else {
				excludeInvalidFunction(pmi.getJSONObject(i).getJSONArray("leaf"));
			}
		}
	}

	public String login() {
		String login = identity.login();
		if (login != null) {
			// 功能菜单
			JSONArray pmi = pullMenuInfo();
			for (int i = 0; i < 16; i++) {
				excludeInvalidFunction(pmi);
			}
			sessionToken.setMenuInfoJsonArray(pmi);
			// 快捷启动
			sessionToken.setQuickStartConfig(commonTool.selectIntermediateAsIds("quick_start", "menu_info_id", "user_info_id = " + sessionToken.getUserInfoId()));
			// 登录统计
			LoginInfo loginInfo = new LoginInfo();
			loginInfo.setUserInfoId(sessionToken.getUserInfoId());
			loginInfo.setLoginTime(new Date());
			entityManager.persist(loginInfo);
			sessionToken.setLoginInfoId(loginInfo.getLoginInfoId());
			sessionToken.setUserInfoIdMD5(MD5.getMD5Alpha(sessionToken.getUserInfoId()));
			entityManager.flush();
			return "success";
		} else {
			return "failure";
		}
	}

	public void logout() {// TODO: 重置sessionToken 优化 使用Class遍历属性来实现
		log.info("{0} logout.", credentials.getUsername());
		if (identity != null) {
			identity.logout();

			LoginInfo loginInfo = entityManager.find(LoginInfo.class, sessionToken.getLoginInfoId());
			loginInfo.setLogoutTime(new Date());
			entityManager.merge(loginInfo);
			entityManager.flush();

			sessionToken.setMessage(null);
			sessionToken.setLoginInfoId(null);
			sessionToken.setRoleInfoId(null);
			sessionToken.setUserInfoId(null);
			sessionToken.setUsername(null);
			sessionToken.setUserInfoIdMD5(null);
			sessionToken.setFullname(null);
			sessionToken.setSystemThemeName(null);
			sessionToken.setSystemThemeNameSource(null);
			sessionToken.setSystemThemeCssPath(null);
			sessionToken.setMenuInfoJsonArray(null);
			sessionToken.setQuickStartConfig(null);
			sessionToken.setDepartmentInfoId(null);
		}
	}

	@Remove
	public void remove() {
	}

	@Destroy
	public void destroy() {
	}
}
