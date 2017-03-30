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
	private Integer applyUserId;
	private String reciveCompany;
	private String invoiceNum;//发票号
	private Integer insertUser;
	private Boolean deleted;
	private Date insertTime;
	private Date updateTime;
	private Integer updateUser;
	private String summary;
	private String reimbursementer;
	private String comment ;//备注
	private Float totalMoney;//总金额
	private Date applyTime;//申请时间
	private Date registTime;//登记时间
	private Integer register;//登记人
	private Integer expendApplyStatus;//申请单状态 0初始状态 1确认完成 2 确认驳回
	
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
	
	
	@Column(name = "year", nullable = false, length = 10)
	@NotNull
	@Length(max = 10)
	public String getYear() {
		return year;
	}
	public void setYear(String year) {
		this.year = year;
	}
	
	
	@Column(name = "applay_user_id")
	@NotNull
	public Integer getApplyUserId() {
		return applyUserId;
	}
	public void setApplyUserId(Integer applyUserId) {
		this.applyUserId = applyUserId;
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
	
	
	@Column(name = "summary")
	public String getSummary() {
		return summary;
	}
	public void setSummary(String summary) {
		this.summary = summary;
	}
	
	@Column(name = "reimbursementer")
	public String getReimbursementer() {
		return reimbursementer;
	}
	public void setReimbursementer(String reimbursementer) {
		this.reimbursementer = reimbursementer;
	}
	
	@Column(name = "task_order_id")
	@NotNull
	public Integer getTaskOrderId() {
		return taskOrderId;
	}
	public void setTaskOrderId(Integer taskOrderId) {
		this.taskOrderId = taskOrderId;
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
	@Column(name = "comment")
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	@Column(name = "total_money")
	public Float getTotalMoney() {
		return totalMoney;
	}
	public void setTotalMoney(Float totalMoney) {
		this.totalMoney = totalMoney;
	}
	
	@Column(name = "register")
	public Integer getRegister() {
		return register;
	}
	public void setRegister(Integer register) {
		this.register = register;
	}
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "apply_time",  length = 19)
	@NotNull
	public Date getApplyTime() {
		return applyTime;
	}
	public void setApplyTime(Date applyTime) {
		this.applyTime = applyTime;
	}
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "register_time",  length = 19)
	public Date getRegistTime() {
		return registTime;
	}
	public void setRegistTime(Date registTime) {
		this.registTime = registTime;
	}
	
	@Column(name = "expend_apply_status")
	@NotNull
	public Integer getExpendApplyStatus() {
		return expendApplyStatus;
	}
	public void setExpendApplyStatus(Integer expendApplyStatus) {
		this.expendApplyStatus = expendApplyStatus;
	}
	
	
	
}
