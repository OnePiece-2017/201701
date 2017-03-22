package cn.dmdl.stl.hospitalbudget.budget.entity;

// Generated 2017-3-8 15:38:50 by Hibernate Tools 3.4.0.CR1

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

import cn.dmdl.stl.hospitalbudget.hospital.entity.YsDepartmentInfo;
import cn.dmdl.stl.hospitalbudget.hospital.entity.YsFundsSource;

/**
 * GenericProject generated by hbm2java
 */
@Entity
@Table(name = "generic_project", catalog = "hospital_budget")
public class GenericProject implements java.io.Serializable {

	private static final long serialVersionUID = 1L;
	private Integer theId;
	private int theLevel;
	private boolean bottomLevel;
	private GenericProject genericProject;
	private String theValue;
	private Integer projectType;
	private YsDepartmentInfo ysDepartmentInfo;
	private YsFundsSource ysFundsSource;
	private Double budgetAmount;
	private String theDescription;
	private Integer theState;
	private Date insertTime;
	private Integer insertUser;
	private Date updateTime;
	private Integer updateUser;
	private boolean deleted;

	public GenericProject() {
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

	@Column(name = "the_level", nullable = false)
	public int getTheLevel() {
		return theLevel;
	}

	public void setTheLevel(int theLevel) {
		this.theLevel = theLevel;
	}

	@Column(name = "bottom_level", nullable = false)
	public boolean isBottomLevel() {
		return bottomLevel;
	}

	public void setBottomLevel(boolean bottomLevel) {
		this.bottomLevel = bottomLevel;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "the_pid")
	public GenericProject getGenericProject() {
		return genericProject;
	}

	public void setGenericProject(GenericProject genericProject) {
		this.genericProject = genericProject;
	}

	@Column(name = "the_value", nullable = false)
	@NotNull
	public String getTheValue() {
		return this.theValue;
	}

	public void setTheValue(String theValue) {
		this.theValue = theValue;
	}

	@Column(name = "project_type")
	public Integer getProjectType() {
		return this.projectType;
	}

	public void setProjectType(Integer projectType) {
		this.projectType = projectType;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "department_info_id")
	public YsDepartmentInfo getYsDepartmentInfo() {
		return ysDepartmentInfo;
	}

	public void setYsDepartmentInfo(YsDepartmentInfo ysDepartmentInfo) {
		this.ysDepartmentInfo = ysDepartmentInfo;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "funds_source_id")
	public YsFundsSource getYsFundsSource() {
		return ysFundsSource;
	}

	public void setYsFundsSource(YsFundsSource ysFundsSource) {
		this.ysFundsSource = ysFundsSource;
	}

	@Column(name = "budget_amount", precision = 22, scale = 0)
	public Double getBudgetAmount() {
		return this.budgetAmount;
	}

	public void setBudgetAmount(Double budgetAmount) {
		this.budgetAmount = budgetAmount;
	}

	@Column(name = "the_description", length = 1024)
	@Length(max = 1024)
	public String getTheDescription() {
		return this.theDescription;
	}

	public void setTheDescription(String theDescription) {
		this.theDescription = theDescription;
	}

	@Column(name = "the_state")
	public Integer getTheState() {
		return this.theState;
	}

	public void setTheState(Integer theState) {
		this.theState = theState;
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
