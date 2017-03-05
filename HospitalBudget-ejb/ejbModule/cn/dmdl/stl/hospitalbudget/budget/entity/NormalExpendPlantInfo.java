package cn.dmdl.stl.hospitalbudget.budget.entity;

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


@Entity
@Table(name = "normal_expend_plan_info", catalog = "hospital_budget")
public class NormalExpendPlantInfo implements java.io.Serializable{
	
	private static final long serialVersionUID = 1L;
	private Integer normalExpendPlantId;
	private Integer deptId;//科室
	private String year;
	private Integer projectId;
	private Float budgetAmount;
	private Float budgetAmountFrozen;
	private Float budgetAmountSurplus;
	private Integer insertUser;
	private Date insertTime;//发票号
	private Integer updateUser;
	private Date updateTime;
	
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "normal_expend_plan_id", unique = true, nullable = false)
	public Integer getNormalExpendPlantId() {
		return normalExpendPlantId;
	}
	public void setNormalExpendPlantId(Integer normalExpendPlantInfoId) {
		this.normalExpendPlantId = normalExpendPlantInfoId;
	}
	
	@Column(name = "dept_id")
	@NotNull
	public Integer getDeptId() {
		return deptId;
	}
	public void setDeptId(Integer deptId) {
		this.deptId = deptId;
	}
	
	@Column(name = "year")
	@NotNull
	public String getYear() {
		return year;
	}
	public void setYear(String year) {
		this.year = year;
	}
	
	@Column(name = "project_id")
	public Integer getProjectId() {
		return projectId;
	}
	public void setProjectId(Integer projectId) {
		this.projectId = projectId;
	}
	
	@Column(name = "budget_amount")
	@NotNull
	public Float getBudgetAmount() {
		return budgetAmount;
	}
	public void setBudgetAmount(Float budgetAmount) {
		this.budgetAmount = budgetAmount;
	}
	@Column(name = "budget_amount_frozen")
	@NotNull
	public Float getBudgetAmountFrozen() {
		return budgetAmountFrozen;
	}
	public void setBudgetAmountFrozen(Float budgetAmountFrozen) {
		this.budgetAmountFrozen = budgetAmountFrozen;
	}
	
	@Column(name = "budget_amount_surplus")
	@NotNull
	public Float getBudgetAmountSurplus() {
		return budgetAmountSurplus;
	}
	public void setBudgetAmountSurplus(Float budgetAmountSurplus) {
		this.budgetAmountSurplus = budgetAmountSurplus;
	}
	
	@Column(name = "insert_user")
	@NotNull
	public Integer getInsertUser() {
		return insertUser;
	}
	public void setInsertUser(Integer insertUser) {
		this.insertUser = insertUser;
	}
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "insert_time", nullable = false, length = 19)
	@NotNull
	public Date getInsertTime() {
		return insertTime;
	}
	public void setInsertTime(Date insertTime) {
		this.insertTime = insertTime;
	}
	
	@Column(name = "update_user")
	public Integer getUpdateUser() {
		return updateUser;
	}
	public void setUpdateUser(Integer updateUser) {
		this.updateUser = updateUser;
	}
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "update_time", nullable = false, length = 19)
	@NotNull
	public Date getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}
	
	
	
	
}
