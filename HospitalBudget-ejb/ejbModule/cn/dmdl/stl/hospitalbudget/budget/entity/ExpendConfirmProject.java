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
@Table(name = "expend_confirm_project", catalog = "hospital_budget")
public class ExpendConfirmProject implements java.io.Serializable{
	
	private static final long serialVersionUID = 1L;
	private Integer expend_confirm_project_id;
	private Integer expend_confirm_info_id;
	private Integer expend_apply_project_id;//申请单项目表
	private Integer projectId;//项目
	private Integer genericProjectId;//常规项目
	private Float confirm_money;//确认金额
	private boolean deleted;//删除
	
	
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "expend_confirm_project_id", unique = true, nullable = false)
	public Integer getExpend_confirm_project_id() {
		return expend_confirm_project_id;
	}
	public void setExpend_confirm_project_id(Integer expend_confirm_project_id) {
		this.expend_confirm_project_id = expend_confirm_project_id;
	}
	
	@Column(name = "expend_apply_project_id")
	@NotNull
	public Integer getExpend_apply_project_id() {
		return expend_apply_project_id;
	}
	public void setExpend_apply_project_id(Integer expend_apply_project_id) {
		this.expend_apply_project_id = expend_apply_project_id;
	}
	
	@Column(name = "confirm_money")
	public Float getConfirm_money() {
		return confirm_money;
	}
	public void setConfirm_money(Float confirm_money) {
		this.confirm_money = confirm_money;
	}
	
	@Column(name = "expend_confirm_info_id")
	@NotNull
	public Integer getExpend_confirm_info_id() {
		return expend_confirm_info_id;
	}
	public void setExpend_confirm_info_id(Integer expend_confirm_info_id) {
		this.expend_confirm_info_id = expend_confirm_info_id;
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
	@Column(name = "deleted")
	@NotNull
	public boolean isDeleted() {
		return deleted;
	}
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

}
