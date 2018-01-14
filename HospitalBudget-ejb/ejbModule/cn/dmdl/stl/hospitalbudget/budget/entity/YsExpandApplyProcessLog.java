package cn.dmdl.stl.hospitalbudget.budget.entity;

import static javax.persistence.GenerationType.IDENTITY;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "ys_expand_apply_process_log", catalog = "hospital_budget")
public class YsExpandApplyProcessLog implements java.io.Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2980136531688820146L;
	private Integer processLogId;
	private Integer ysExpandApplyId;
	private Integer userId;
	private Integer processStepInfoId;
	private Integer operateType;
	
	
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "process_log_id", unique = true, nullable = false)
	public Integer getProcessLogId() {
		return processLogId;
	}
	public void setProcessLogId(Integer processLogId) {
		this.processLogId = processLogId;
	}
	
	@Column(name = "ys_expand_apply_id")
	public Integer getYsExpandApplyId() {
		return ysExpandApplyId;
	}
	public void setYsExpandApplyId(Integer ysExpandApplyId) {
		this.ysExpandApplyId = ysExpandApplyId;
	}
	@Column(name = "user_id")
	public Integer getUserId() {
		return userId;
	}
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	
	@Column(name = "process_step_info_id")
	public Integer getProcessStepInfoId() {
		return processStepInfoId;
	}
	public void setProcessStepInfoId(Integer processStepInfoId) {
		this.processStepInfoId = processStepInfoId;
	}
	
	@Column(name = "operate_type")
	public Integer getOperateType() {
		return operateType;
	}
	public void setOperateType(Integer operateType) {
		this.operateType = operateType;
	}
	
	
}
