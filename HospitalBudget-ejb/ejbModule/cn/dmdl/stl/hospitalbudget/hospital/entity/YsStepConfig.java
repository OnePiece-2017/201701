package cn.dmdl.stl.hospitalbudget.hospital.entity;

// Generated 2016-11-29 23:10:06 by Hibernate Tools 3.4.0.CR1

import static javax.persistence.GenerationType.IDENTITY;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.validator.NotNull;

/**
 * YsStepConfig generated by hbm2java
 */
@Entity
@Table(name = "ys_step_config", catalog = "hospital_budget")
public class YsStepConfig implements java.io.Serializable {

	private static final long serialVersionUID = 1L;
	private Integer stepId;
	private String stepName;
	private String auditor;
	private String notifier;
	private int stepOrder;
	private int workflowId;
	private boolean needAllAuditor;

	public YsStepConfig() {
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "step_id", unique = true, nullable = false)
	public Integer getStepId() {
		return this.stepId;
	}

	public void setStepId(Integer stepId) {
		this.stepId = stepId;
	}

	@Column(name = "step_name", nullable = false)
	@NotNull
	public String getStepName() {
		return this.stepName;
	}

	public void setStepName(String stepName) {
		this.stepName = stepName;
	}

	@Column(name = "auditor", nullable = false)
	@NotNull
	public String getAuditor() {
		return this.auditor;
	}

	public void setAuditor(String auditor) {
		this.auditor = auditor;
	}

	@Column(name = "notifier", nullable = false)
	@NotNull
	public String getNotifier() {
		return this.notifier;
	}

	public void setNotifier(String notifier) {
		this.notifier = notifier;
	}

	@Column(name = "step_order", nullable = false)
	public int getStepOrder() {
		return this.stepOrder;
	}

	public void setStepOrder(int stepOrder) {
		this.stepOrder = stepOrder;
	}

	@Column(name = "workflow_id", nullable = false)
	public int getWorkflowId() {
		return this.workflowId;
	}

	public void setWorkflowId(int workflowId) {
		this.workflowId = workflowId;
	}

	@Column(name = "need_all_auditor", nullable = false)
	public boolean isNeedAllAuditor() {
		return this.needAllAuditor;
	}

	public void setNeedAllAuditor(boolean needAllAuditor) {
		this.needAllAuditor = needAllAuditor;
	}

}
