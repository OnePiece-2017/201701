package cn.dmdl.stl.hospitalbudget.util;

import static org.jboss.seam.ScopeType.SESSION;
import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.io.Serializable;

import net.sf.json.JSONArray;

import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;

/**
 * 会话令牌
 */
@Name(value = "sessionToken")
@Scope(value = SESSION)
@Install(precedence = BUILT_IN)
@Startup
public class SessionToken implements Serializable {

	private static final long serialVersionUID = 1L;
	private Integer userInfoId;
	private Integer roleInfoId;
	private String username;
	private Integer loginInfoId;
	private String message;
	private String userInfoIdMD5;
	private String nickname;
	private JSONArray menuInfoJsonArray;
	private Integer departmentInfoId;

	public Integer getUserInfoId() {
		return userInfoId;
	}

	public void setUserInfoId(Integer userInfoId) {
		this.userInfoId = userInfoId;
	}

	public Integer getRoleInfoId() {
		return roleInfoId;
	}

	public void setRoleInfoId(Integer roleInfoId) {
		this.roleInfoId = roleInfoId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Integer getLoginInfoId() {
		return loginInfoId;
	}

	public void setLoginInfoId(Integer loginInfoId) {
		this.loginInfoId = loginInfoId;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getUserInfoIdMD5() {
		return userInfoIdMD5;
	}

	public void setUserInfoIdMD5(String userInfoIdMD5) {
		this.userInfoIdMD5 = userInfoIdMD5;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public JSONArray getMenuInfoJsonArray() {
		return menuInfoJsonArray;
	}

	public void setMenuInfoJsonArray(JSONArray menuInfoJsonArray) {
		this.menuInfoJsonArray = menuInfoJsonArray;
	}

	public Integer getDepartmentInfoId() {
		return departmentInfoId;
	}

	public void setDepartmentInfoId(Integer departmentInfoId) {
		this.departmentInfoId = departmentInfoId;
	}

}
