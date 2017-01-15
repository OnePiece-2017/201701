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
@Table(name = "expend_apply_info", catalog = "hospital_budget")
public class ExpendApplyInfo implements java.io.Serializable{
	
	private static final long serialVersionUID = 1L;
	private Integer expendApplyInfoId;
	private String expendApplyCode;//单据编号
	private Integer taskOrderId;
	private String orderSn;
	private String year;
	private Integer departmentInfoId;
	private Integer applyUserId;
	private Integer fundsSource;
	private String reciveCompany;
	private String invoiceNum;//发票号
	private Integer normalExpendBudgetOrderId;
	private Float expendMoney;
	private Integer insertUser;
	private Boolean deleted;
	private Date expendTime;
	private Date insertTime;
	private Date updateTime;
	private Integer updateUser;
	private String comment;
	
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "expend_apply_info_id", unique = true, nullable = false)
	public Integer getExpendApplyInfoId() {
		return expendApplyInfoId;
	}
	public void setExpendApplyInfoId(Integer expendApplyInfoId) {
		this.expendApplyInfoId = expendApplyInfoId;
	}
	
	@Column(name = "expend_apply_code", nullable = false, length = 60)
	@NotNull
	@Length(max = 60)
	public String getExpendApplyCode() {
		return expendApplyCode;
	}
	public void setExpendApplyCode(String expendApplyCode) {
		this.expendApplyCode = expendApplyCode;
	}
	
	@Column(name = "order_sn", nullable = false, length = 45)
	@NotNull
	@Length(max = 45)
	public String getOrderSn() {
		return orderSn;
	}
	public void setOrderSn(String orderSn) {
		this.orderSn = orderSn;
	}
	
	@Column(name = "year", nullable = false, length = 10)
	@NotNull
	@Length(max = 10)
	public String getYear() {
		return year;
	}
	public void setYear(String year) {
		this.year = year;
	}
	
	@Column(name = "department_info_id")
	@NotNull
	public Integer getDepartmentInfoId() {
		return departmentInfoId;
	}
	public void setDepartmentInfoId(Integer departmentInfoId) {
		this.departmentInfoId = departmentInfoId;
	}
	
	@Column(name = "applay_user_id")
	@NotNull
	public Integer getApplyUserId() {
		return applyUserId;
	}
	public void setApplyUserId(Integer applyUserId) {
		this.applyUserId = applyUserId;
	}
	
	@Column(name = "funds_source")
	@NotNull
	public Integer getFundsSource() {
		return fundsSource;
	}
	public void setFundsSource(Integer fundsSource) {
		this.fundsSource = fundsSource;
	}
	
	@Column(name = "recive_company", nullable = false, length = 600)
	@NotNull
	@Length(max = 600)
	public String getReciveCompany() {
		return reciveCompany;
	}
	public void setReciveCompany(String reciveCompany) {
		this.reciveCompany = reciveCompany;
	}
	
	@Column(name = "invoice_num", nullable = false, length = 200)
	@NotNull
	@Length(max = 200)
	public String getInvoiceNum() {
		return invoiceNum;
	}
	public void setInvoiceNum(String invoiceNum) {
		this.invoiceNum = invoiceNum;
	}
	
	@Column(name = "normal_expend_budget_order_id")
	@NotNull
	public Integer getNormalExpendBudgetOrderId() {
		return normalExpendBudgetOrderId;
	}
	public void setNormalExpendBudgetOrderId(Integer normalExpendBudgetOrderId) {
		this.normalExpendBudgetOrderId = normalExpendBudgetOrderId;
	}
	
	@Column(name = "expend_money")
	@NotNull
	public Float getExpendMoney() {
		return expendMoney;
	}
	public void setExpendMoney(Float expendMoney) {
		this.expendMoney = expendMoney;
	}
	
	@Column(name = "insert_user")
	@NotNull
	public Integer getInsertUser() {
		return insertUser;
	}
	public void setInsertUser(Integer insertUser) {
		this.insertUser = insertUser;
	}
	
	@Column(name = "deleted")
	@NotNull
	public Boolean getDeleted() {
		return deleted;
	}
	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "expend_time", nullable = false, length = 19)
	@NotNull
	public Date getExpendTime() {
		return expendTime;
	}
	public void setExpendTime(Date expendTime) {
		this.expendTime = expendTime;
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
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "update_time",  length = 19)
	public Date getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}
	@Column(name = "update_user")
	public Integer getUpdateUser() {
		return updateUser;
	}
	public void setUpdateUser(Integer updateUser) {
		this.updateUser = updateUser;
	}
	@Column(name = "comment")
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	@Column(name = "task_order_id")
	@NotNull
	public Integer getTaskOrderId() {
		return taskOrderId;
	}
	public void setTaskOrderId(Integer taskOrderId) {
		this.taskOrderId = taskOrderId;
	}
	
	
	
}
