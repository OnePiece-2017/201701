package cn.dmdl.stl.hospitalbudget.budget.entity;

// Generated 2017-3-23 20:09:17 by Hibernate Tools 3.4.0.CR1

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
 * YsExpandDraft generated by hbm2java
 */
@Entity
@Table(name = "ys_expand_draft", catalog = "hospital_budget")
public class YsExpandDraft implements java.io.Serializable {

	private static final long serialVersionUID = 1L;
	private Integer ysExpandDraftId;
	private String year;
	private String projectSource;
	private int topLevelProjectId;
	private Integer routineProjectId;
	private Integer genericProjectId;
	private double projectAmount;
	private String formulaRemark;
	private Double withLastYearNum;
	private Float withLastYearPercent;
	private String attachment;
	private Date insertTime;
	private int insertUser;
	private Date updateTime;
	private Integer updateUser;
	private int status;
	private String auditInfo;
	private boolean delete;

	public YsExpandDraft() {
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "ys_expand_draft_id", unique = true, nullable = false)
	public Integer getYsExpandDraftId() {
		return this.ysExpandDraftId;
	}

	public void setYsExpandDraftId(Integer ysExpandDraftId) {
		this.ysExpandDraftId = ysExpandDraftId;
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

	@Column(name = "project_source", length = 1024)
	@Length(max = 1024)
	public String getProjectSource() {
		return this.projectSource;
	}

	public void setProjectSource(String projectSource) {
		this.projectSource = projectSource;
	}

	@Column(name = "top_level_project_id", nullable = false)
	public int getTopLevelProjectId() {
		return this.topLevelProjectId;
	}

	public void setTopLevelProjectId(int topLevelProjectId) {
		this.topLevelProjectId = topLevelProjectId;
	}

	@Column(name = "routine_project_id")
	public Integer getRoutineProjectId() {
		return this.routineProjectId;
	}

	public void setRoutineProjectId(Integer routineProjectId) {
		this.routineProjectId = routineProjectId;
	}

	@Column(name = "generic_project_id")
	public Integer getGenericProjectId() {
		return this.genericProjectId;
	}

	public void setGenericProjectId(Integer genericProjectId) {
		this.genericProjectId = genericProjectId;
	}

	@Column(name = "project_amount", nullable = false, precision = 22, scale = 0)
	public double getProjectAmount() {
		return this.projectAmount;
	}

	public void setProjectAmount(double projectAmount) {
		this.projectAmount = projectAmount;
	}

	@Column(name = "formula_remark", length = 5000)
	@Length(max = 5000)
	public String getFormulaRemark() {
		return this.formulaRemark;
	}

	public void setFormulaRemark(String formulaRemark) {
		this.formulaRemark = formulaRemark;
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

	@Column(name = "attachment", length = 32)
	@Length(max = 32)
	public String getAttachment() {
		return this.attachment;
	}

	public void setAttachment(String attachment) {
		this.attachment = attachment;
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

	@Column(name = "insert_user", nullable = false)
	public int getInsertUser() {
		return this.insertUser;
	}

	public void setInsertUser(int insertUser) {
		this.insertUser = insertUser;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "update_time", length = 19)
	public Date getUpdateTime() {
		return this.updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	@Column(name = "update_user")
	public Integer getUpdateUser() {
		return this.updateUser;
	}

	public void setUpdateUser(Integer updateUser) {
		this.updateUser = updateUser;
	}

	@Column(name = "status", nullable = false)
	public int getStatus() {
		return this.status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	@Column(name = "audit_info", length = 200)
	@Length(max = 200)
	public String getAuditInfo() {
		return this.auditInfo;
	}

	public void setAuditInfo(String auditInfo) {
		this.auditInfo = auditInfo;
	}

	@Column(name = "delete", nullable = false)
	public boolean isDelete() {
		return this.delete;
	}

	public void setDelete(boolean delete) {
		this.delete = delete;
	}

}
