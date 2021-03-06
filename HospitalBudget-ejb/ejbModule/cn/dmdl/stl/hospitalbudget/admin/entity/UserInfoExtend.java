package cn.dmdl.stl.hospitalbudget.admin.entity;

// Generated 2016-10-6 22:00:53 by Hibernate Tools 3.4.0.CR1

import static javax.persistence.GenerationType.IDENTITY;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

/**
 * UserInfoExtend generated by hbm2java
 */
@Entity
@Table(name = "user_info_extend", catalog = "hospital_budget")
public class UserInfoExtend implements java.io.Serializable {

	private static final long serialVersionUID = 1L;
	private Integer userInfoExtendId;
	private String fullname;
	private String avatar;
	private String mail;
	private Date insertTime;
	private Integer insertUser;
	private Date updateTime;
	private Integer updateUser;
	private boolean deleted;
	private int sex;

	public UserInfoExtend() {
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "user_info_extend_id", unique = true, nullable = false)
	public Integer getUserInfoExtendId() {
		return this.userInfoExtendId;
	}

	public void setUserInfoExtendId(Integer userInfoExtendId) {
		this.userInfoExtendId = userInfoExtendId;
	}

	@Column(name = "fullname", nullable = false, length = 24)
	@NotNull
	@Length(max = 24)
	public String getFullname() {
		return this.fullname;
	}

	public void setFullname(String fullname) {
		this.fullname = fullname;
	}

	@Column(name = "avatar", length = 500)
	@Length(max = 500)
	public String getAvatar() {
		return this.avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	@Column(name = "mail", length = 30)
	@Length(max = 30)
	public String getMail() {
		return this.mail;
	}

	public void setMail(String mail) {
		this.mail = mail;
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

	@Column(name = "sex", nullable = false)
	public int getSex() {
		return sex;
	}

	public void setSex(int sex) {
		this.sex = sex;
	}

}
