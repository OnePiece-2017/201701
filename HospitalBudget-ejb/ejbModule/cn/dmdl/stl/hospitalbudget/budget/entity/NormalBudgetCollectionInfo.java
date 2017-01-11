package cn.dmdl.stl.hospitalbudget.budget.entity;

// Generated 2017-1-11 22:56:03 by Hibernate Tools 3.4.0.CR1

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
 * NormalBudgetCollectionInfo generated by hbm2java
 */
@Entity
@Table(name = "normal_budget_collection_info", catalog = "hospital_budget")
public class NormalBudgetCollectionInfo implements java.io.Serializable {

	private static final long serialVersionUID = 1L;
	private Integer normalBudgetCollectionId;
	private String orderSn;
	private int deptId;
	private String year;
	private double budgetAmount;
	private int amountType;
	private int insertUser;
	private Date insertTime;
	private boolean submitFlag;

	public NormalBudgetCollectionInfo() {
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "normal_budget_collection_id", unique = true, nullable = false)
	public Integer getNormalBudgetCollectionId() {
		return this.normalBudgetCollectionId;
	}

	public void setNormalBudgetCollectionId(Integer normalBudgetCollectionId) {
		this.normalBudgetCollectionId = normalBudgetCollectionId;
	}

	@Column(name = "order_sn", nullable = false, length = 45)
	@NotNull
	@Length(max = 45)
	public String getOrderSn() {
		return this.orderSn;
	}

	public void setOrderSn(String orderSn) {
		this.orderSn = orderSn;
	}

	@Column(name = "dept_id", nullable = false)
	public int getDeptId() {
		return this.deptId;
	}

	public void setDeptId(int deptId) {
		this.deptId = deptId;
	}

	@Column(name = "year", nullable = false, length = 45)
	@NotNull
	@Length(max = 45)
	public String getYear() {
		return this.year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	@Column(name = "budget_amount", nullable = false, precision = 22, scale = 0)
	public double getBudgetAmount() {
		return this.budgetAmount;
	}

	public void setBudgetAmount(double budgetAmount) {
		this.budgetAmount = budgetAmount;
	}

	@Column(name = "amount_type", nullable = false)
	public int getAmountType() {
		return this.amountType;
	}

	public void setAmountType(int amountType) {
		this.amountType = amountType;
	}

	@Column(name = "insert_user", nullable = false)
	public int getInsertUser() {
		return this.insertUser;
	}

	public void setInsertUser(int insertUser) {
		this.insertUser = insertUser;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "insert_time", nullable = false, length = 19)
	@NotNull
	public Date getInsertTime() {
		return this.insertTime;
	}

	public void setInsertTime(Date insertTime) {
		this.insertTime = insertTime;
	}

	@Column(name = "submit_flag", nullable = false)
	public boolean isSubmitFlag() {
		return this.submitFlag;
	}

	public void setSubmitFlag(boolean submitFlag) {
		this.submitFlag = submitFlag;
	}

}
