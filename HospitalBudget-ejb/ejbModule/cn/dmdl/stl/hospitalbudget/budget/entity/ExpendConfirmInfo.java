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
@Table(name = "expend_confirm_info", catalog = "hospital_budget")
public class ExpendConfirmInfo implements java.io.Serializable{
	
	private static final long serialVersionUID = 1L;
	private Integer expend_confirm_info_id;
	private Integer expend_apply_info_id;//申请单号
	private String year;
	private Integer insertUser;
	private Date insertTime;
	private Date updateTime;
	private Integer confirmUser;
	private Float totalMoney;//总金额
	private Date confirm_time;//确认时间
	private Integer confirm_status;//确认单状态 0初始状态 1确认完成 2 确认驳回
	private boolean deleted;//删除
	
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "expend_confirm_info_id", unique = true, nullable = false)
	public Integer getExpend_confirm_info_id() {
		return expend_confirm_info_id;
	}
	public void setExpend_confirm_info_id(Integer expend_confirm_info_id) {
		this.expend_confirm_info_id = expend_confirm_info_id;
	}
	
	@Column(name = "expend_apply_info_id")
	@NotNull
	public Integer getExpend_apply_info_id() {
		return expend_apply_info_id;
	}
	public void setExpend_apply_info_id(Integer expend_apply_info_id) {
		this.expend_apply_info_id = expend_apply_info_id;
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
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "update_time",  length = 19)
	public Date getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}
	
	@Column(name = "confirm_user")
	public Integer getConfirmUser() {
		return confirmUser;
	}
	public void setConfirmUser(Integer confirmUser) {
		this.confirmUser = confirmUser;
	}
	
	@Column(name = "total_money")
	@NotNull
	public Float getTotalMoney() {
		return totalMoney;
	}
	public void setTotalMoney(Float totalMoney) {
		this.totalMoney = totalMoney;
	}
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "confirm_time", length = 19)
	public Date getConfirm_time() {
		return confirm_time;
	}
	public void setConfirm_time(Date confirm_time) {
		this.confirm_time = confirm_time;
	}
	
	@Column(name = "confirm_status")
	@NotNull
	public Integer getConfirm_status() {
		return confirm_status;
	}
	public void setConfirm_status(Integer confirm_status) {
		this.confirm_status = confirm_status;
	}
	@Column(name = "year")
	@NotNull
	public String getYear() {
		return year;
	}
	public void setYear(String year) {
		this.year = year;
	}
	@Column(name = "deleted")
	@NotNull
	public boolean isDeleted() {
		return deleted;
	}
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}
	
	
	
	
}
