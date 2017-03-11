package cn.dmdl.stl.hospitalbudget.admin.entity;

// Generated 2016-10-14 0:32:45 by Hibernate Tools 3.4.0.CR1

import static javax.persistence.GenerationType.IDENTITY;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

/**
 * RoleInfo generated by hbm2java
 */
@Entity
@Table(name = "role_info", catalog = "hospital_budget")
public class RoleInfo implements java.io.Serializable {

	private static final long serialVersionUID = 1L;
	private Integer roleInfoId;
	private String roleName;
	private SystemTheme systemTheme;
	private String description;
	private Date insertTime;
	private Integer insertUser;
	private Date updateTime;
	private Integer updateUser;
	private boolean deleted;

	public RoleInfo() {
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "role_info_id", unique = true, nullable = false)
	public Integer getRoleInfoId() {
		return this.roleInfoId;
	}

	public void setRoleInfoId(Integer roleInfoId) {
		this.roleInfoId = roleInfoId;
	}

	@Column(name = "role_name", nullable = false, length = 14)
	@NotNull
	@Length(max = 14)
	public String getRoleName() {
		return this.roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "system_theme_id")
	public SystemTheme getSystemTheme() {
		return systemTheme;
	}

	public void setSystemTheme(SystemTheme systemTheme) {
		this.systemTheme = systemTheme;
	}

	@Column(name = "description")
	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "insert_time", length = 19)
	public Date getInsertTime() {
		return this.insertTime;
	}

	public void setInsertTime(Date insertTime) {
		this.insertTime = insertTime;
	}

	@Column(name = "insert_user")
	public Integer getInsertUser() {
		return this.insertUser;
	}

	public void setInsertUser(Integer insertUser) {
		this.insertUser = insertUser;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "update_time", length = 19)
	public Date getUpdateTime() {
		return this.updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	@Column(name = "update_user")
	public Integer getUpdateUser() {
		return this.updateUser;
	}

	public void setUpdateUser(Integer updateUser) {
		this.updateUser = updateUser;
	}

	@Column(name = "deleted", nullable = false)
	public boolean isDeleted() {
		return this.deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

}
