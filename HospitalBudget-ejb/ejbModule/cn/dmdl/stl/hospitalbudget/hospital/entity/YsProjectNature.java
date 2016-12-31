package cn.dmdl.stl.hospitalbudget.hospital.entity;

// Generated 2016-12-12 15:36:49 by Hibernate Tools 3.4.0.CR1

import static javax.persistence.GenerationType.IDENTITY;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.validator.NotNull;

/**
 * YsProjectNature generated by hbm2java
 */
@Entity
@Table(name = "ys_project_nature", catalog = "hospital_budget")
public class YsProjectNature implements java.io.Serializable {

	private static final long serialVersionUID = 1L;
	private Integer theId;
	private String theValue;
	private String theDescription;
	private Date insertTime;
	private Integer insertUser;
	private Date updateTime;
	private Integer updateUser;
	private boolean deleted;

	public YsProjectNature() {
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "the_id", unique = true, nullable = false)
	public Integer getTheId() {
		return this.theId;
	}

	public void setTheId(Integer theId) {
		this.theId = theId;
	}

	@Column(name = "the_value", nullable = false)
	@NotNull
	public String getTheValue() {
		return this.theValue;
	}

	public void setTheValue(String theValue) {
		this.theValue = theValue;
	}

	@Column(name = "the_description")
	public String getTheDescription() {
		return this.theDescription;
	}

	public void setTheDescription(String theDescription) {
		this.theDescription = theDescription;
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
