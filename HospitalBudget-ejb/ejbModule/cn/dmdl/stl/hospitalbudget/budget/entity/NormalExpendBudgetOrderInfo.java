package cn.dmdl.stl.hospitalbudget.budget.entity;

// Generated 2017-1-5 18:04:01 by Hibernate Tools 3.4.0.CR1

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
 * NormalBudgetOrderInfo generated by hbm2java
 */
@Entity
@Table(name = "normal_expend_budget_order_info", catalog = "hospital_budget")
public class NormalExpendBudgetOrderInfo implements java.io.Serializable {

	private static final long serialVersionUID = 1L;
	private Integer normalExpendBudgetOrderId;
	private int taskOrderId;// 订单id
	private String orderSn;
	private String year;
	private Integer normalProjectId;
	private Integer subProjectId;
	private int projectSource;
	private double nowAmount;
	private double projectAmount;
	private String formula;
	private Double withLastYearNum;
	private Float withLastYearPercent;
	private String remark;
	private boolean isNew;
	private Date insertTime;
	private Integer insertUser;

	public NormalExpendBudgetOrderInfo() {
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "normal_expend_budget_order_id", unique = true, nullable = false)
	public Integer getNormalExpendBudgetOrderId() {
		return normalExpendBudgetOrderId;
	}

	public void setNormalExpendBudgetOrderId(Integer normalExpendBudgetOrderId) {
		this.normalExpendBudgetOrderId = normalExpendBudgetOrderId;
	}

	@Column(name = "task_order_id", nullable = false)
	public int getTaskOrderId() {
		return taskOrderId;
	}

	public void setTaskOrderId(int taskOrderId) {
		this.taskOrderId = taskOrderId;
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

	@Column(name = "year", nullable = false, length = 10)
	@NotNull
	@Length(max = 10)
	public String getYear() {
		return this.year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	@Column(name = "normal_project_id")
	public Integer getNormalProjectId() {
		return this.normalProjectId;
	}

	public void setNormalProjectId(Integer normalProjectId) {
		this.normalProjectId = normalProjectId;
	}

	@Column(name = "sub_project_id")
	public Integer getSubProjectId() {
		return this.subProjectId;
	}

	public void setSubProjectId(Integer subProjectId) {
		this.subProjectId = subProjectId;
	}

	@Column(name = "project_source", nullable = false)
	public int getProjectSource() {
		return this.projectSource;
	}

	public void setProjectSource(int projectSource) {
		this.projectSource = projectSource;
	}

	@Column(name = "project_amount", nullable = false, precision = 22, scale = 0)
	public double getProjectAmount() {
		return this.projectAmount;
	}

	public void setProjectAmount(double projectAmount) {
		this.projectAmount = projectAmount;
	}

	@Column(name = "formula", length = 100)
	@Length(max = 100)
	public String getFormula() {
		return this.formula;
	}

	public void setFormula(String formula) {
		this.formula = formula;
	}

	@Column(name = "with_last_year_num", precision = 22, scale = 0)
	public Double getWithLastYearNum() {
		return this.withLastYearNum;
	}

	public void setWithLastYearNum(Double withLastYearNum) {
		this.withLastYearNum = withLastYearNum;
	}

	@Column(name = "with_last_year_percent", precision = 12, scale = 0)
	public Float getWithLastYearPercent() {
		return this.withLastYearPercent;
	}

	public void setWithLastYearPercent(Float withLastYearPercent) {
		this.withLastYearPercent = withLastYearPercent;
	}

	@Column(name = "remark", length = 100)
	@Length(max = 100)
	public String getRemark() {
		return this.remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	@Column(name = "is_new", nullable = false)
	public boolean isIsNew() {
		return this.isNew;
	}

	public void setIsNew(boolean isNew) {
		this.isNew = isNew;
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

	@Column(name = "insert_user")
	public Integer getInsertUser() {
		return this.insertUser;
	}

	public void setInsertUser(Integer insertUser) {
		this.insertUser = insertUser;
	}
	@Column(name = "now_amout", nullable = false, precision = 22, scale = 0)
	public double getNowAmount() {
		return nowAmount;
	}

	public void setNowAmount(double nowAmount) {
		this.nowAmount = nowAmount;
	}

}
