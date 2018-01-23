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
@Table(name = "expend_apply_project", catalog = "hospital_budget")
public class ExpendApplyProject implements java.io.Serializable{
	private static final long serialVersionUID = 1L;
	private Integer ExpendApplyProjectId;
	private Integer expendApplyInfoId;
	private Integer projectId;//常规项目
	private Integer genericProjectId;//项目
	private Double expendMoney;//本次预算内支出
	private Double expendBeforFrozen;//支出之前冻结
	private Double expendBeforSurplus;//支出之前剩余
	private Double budgetAppendExpend;//预算追加支出
	private Double budgetAdjustmentAppend;//预算调整追加
	private Double budgetAdjestmentCut;//预算调整减少
	private Double appendBudgetPaid;//追加预算已支出
	private Double appendBudgetCanCut;//追加预算可支出
	private String attachment;//附件
	
	private Date insertTime;
	private Integer insertUser;
	private Boolean deleted;
	private Date updateTime;
	private Integer updateUser;
	
	
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "expend_apply_project_id", unique = true, nullable = false)
	public Integer getExpendApplyProjectId() {
		return ExpendApplyProjectId;
	}
	public void setExpendApplyProjectId(Integer expendApplyProjectId) {
		ExpendApplyProjectId = expendApplyProjectId;
	}
	
	@Column(name = "project_id")
	public Integer getProjectId() {
		return projectId;
	}
	public void setProjectId(Integer projectId) {
		this.projectId = projectId;
	}
	
	@Column(name = "generic_project_id")
	public Integer getGenericProjectId() {
		return genericProjectId;
	}
	public void setGenericProjectId(Integer genericProjectId) {
		this.genericProjectId = genericProjectId;
	}
	@Column(name = "expend_money")
	@NotNull
	public Double getExpendMoney() {
		return expendMoney;
	}
	
	public void setExpendMoney(Double expendMoney) {
		this.expendMoney = expendMoney;
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
	
	@Column(name = "expend_apply_info_id")
	@NotNull
	public Integer getExpendApplyInfoId() {
		return expendApplyInfoId;
	}
	public void setExpendApplyInfoId(Integer expendApplyInfoId) {
		this.expendApplyInfoId = expendApplyInfoId;
	}
	
	@Column(name = "budget_append_expend")
	public Double getBudgetAppendExpend() {
		return budgetAppendExpend;
	}
	public void setBudgetAppendExpend(Double budgetAppendExpend) {
		this.budgetAppendExpend = budgetAppendExpend;
	}
	
	@Column(name = "budget_adjustment_append")
	public Double getBudgetAdjustmentAppend() {
		return budgetAdjustmentAppend;
	}
	public void setBudgetAdjustmentAppend(Double budgetAdjustmentAppend) {
		this.budgetAdjustmentAppend = budgetAdjustmentAppend;
	}
	
	@Column(name = "budget_adjestment_cut")
	public Double getBudgetAdjestmentCut() {
		return budgetAdjestmentCut;
	}
	public void setBudgetAdjestmentCut(Double budgetAdjestmentCut) {
		this.budgetAdjestmentCut = budgetAdjestmentCut;
	}
	@Column(name = "append_budget_paid")
	public Double getAppendBudgetPaid() {
		return appendBudgetPaid;
	}
	public void setAppendBudgetPaid(Double appendBudgetPaid) {
		this.appendBudgetPaid = appendBudgetPaid;
	}
	@Column(name = "append_budget_money")
	public Double getAppendBudgetCanCut() {
		return appendBudgetCanCut;
	}
	public void setAppendBudgetCanCut(Double appendBudgetCanCut) {
		this.appendBudgetCanCut = appendBudgetCanCut;
	}
	@Column(name = "expend_befor_frozen")
	@NotNull
	public Double getExpendBeforFrozen() {
		return expendBeforFrozen;
	}
	public void setExpendBeforFrozen(Double expendBeforFrozen) {
		this.expendBeforFrozen = expendBeforFrozen;
	}
	@Column(name = "expend_befor_surplus")
	@NotNull
	public Double getExpendBeforSurplus() {
		return expendBeforSurplus;
	}
	public void setExpendBeforSurplus(Double expendBeforSurplus) {
		this.expendBeforSurplus = expendBeforSurplus;
	}
	@Column(name = "attachment")
	public String getAttachment() {
		return attachment;
	}
	public void setAttachment(String attachment) {
		this.attachment = attachment;
	}
	
	
	
	
}
