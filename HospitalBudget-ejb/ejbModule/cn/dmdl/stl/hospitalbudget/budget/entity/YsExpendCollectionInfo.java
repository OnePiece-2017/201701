package cn.dmdl.stl.hospitalbudget.budget.entity;

// Generated 2017-3-20 16:17:32 by Hibernate Tools 3.4.0.CR1

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.IDENTITY;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

/**
 * YsExpendCollectionInfo generated by hbm2java
 */
@Entity
@Table(name = "ys_expend_collection_info", catalog = "hospital_budget")
public class YsExpendCollectionInfo implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer expendCollectionInfoId;
	private int version;
	private int budgetCollectionDeptId;
	private String year;
	private String projectSource;
	private Integer routineProjectId;
	private Integer genericProjectId;
	private double projectAmount;
	private String formulaRemark;
	private Date insertTime;
	private int insertUser;
	private boolean delete;

	public YsExpendCollectionInfo() {
	}

	public YsExpendCollectionInfo(int budgetCollectionDeptId, String year,
			double projectAmount, Date insertTime, int insertUser,
			boolean delete) {
		this.budgetCollectionDeptId = budgetCollectionDeptId;
		this.year = year;
		this.projectAmount = projectAmount;
		this.insertTime = insertTime;
		this.insertUser = insertUser;
		this.delete = delete;
	}

	public YsExpendCollectionInfo(int budgetCollectionDeptId, String year,
			String projectSource, Integer routineProjectId,
			Integer genericProjectId, double projectAmount,
			String formulaRemark, Date insertTime, int insertUser,
			boolean delete) {
		this.budgetCollectionDeptId = budgetCollectionDeptId;
		this.year = year;
		this.projectSource = projectSource;
		this.routineProjectId = routineProjectId;
		this.genericProjectId = genericProjectId;
		this.projectAmount = projectAmount;
		this.formulaRemark = formulaRemark;
		this.insertTime = insertTime;
		this.insertUser = insertUser;
		this.delete = delete;
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "expend_collection_info_id", unique = true, nullable = false)
	public Integer getExpendCollectionInfoId() {
		return this.expendCollectionInfoId;
	}

	public void setExpendCollectionInfoId(Integer expendCollectionInfoId) {
		this.expendCollectionInfoId = expendCollectionInfoId;
	}

	@Version
	@Column(name = "version", nullable = false)
	public int getVersion() {
		return this.version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	@Column(name = "budget_collection_dept_id", nullable = false)
	public int getBudgetCollectionDeptId() {
		return this.budgetCollectionDeptId;
	}

	public void setBudgetCollectionDeptId(int budgetCollectionDeptId) {
		this.budgetCollectionDeptId = budgetCollectionDeptId;
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

	@Column(name = "project_source", length = 45)
	@Length(max = 45)
	public String getProjectSource() {
		return this.projectSource;
	}

	public void setProjectSource(String projectSource) {
		this.projectSource = projectSource;
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

	@Column(name = "formula_remark", length = 500)
	@Length(max = 500)
	public String getFormulaRemark() {
		return this.formulaRemark;
	}

	public void setFormulaRemark(String formulaRemark) {
		this.formulaRemark = formulaRemark;
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

	@Column(name = "delete", nullable = false)
	public boolean isDelete() {
		return this.delete;
	}

	public void setDelete(boolean delete) {
		this.delete = delete;
	}

}
