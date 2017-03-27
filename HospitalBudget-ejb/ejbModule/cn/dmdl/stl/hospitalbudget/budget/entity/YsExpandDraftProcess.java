package cn.dmdl.stl.hospitalbudget.budget.entity;

// Generated 2017-3-27 11:10:40 by Hibernate Tools 3.4.0.CR1

import static javax.persistence.GenerationType.IDENTITY;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * YsExpandDraftProcess generated by hbm2java
 */
@Entity
@Table(name = "ys_expand_draft_process", catalog = "hospital_budget")
public class YsExpandDraftProcess implements java.io.Serializable {

	private static final long serialVersionUID = 1L;
	private Integer processId;
	private int ysExpandDraftId;
	private int userId;

	public YsExpandDraftProcess() {
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "process_id", unique = true, nullable = false)
	public Integer getProcessId() {
		return this.processId;
	}

	public void setProcessId(Integer processId) {
		this.processId = processId;
	}

	@Column(name = "ys_expand_draft_id", nullable = false)
	public int getYsExpandDraftId() {
		return this.ysExpandDraftId;
	}

	public void setYsExpandDraftId(int ysExpandDraftId) {
		this.ysExpandDraftId = ysExpandDraftId;
	}

	@Column(name = "user_id", nullable = false)
	public int getUserId() {
		return this.userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

}