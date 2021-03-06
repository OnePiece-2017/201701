package cn.dmdl.stl.hospitalbudget.execute.entity;

// Generated 2017-3-5 14:03:51 by Hibernate Tools 3.4.0.CR1

import static javax.persistence.GenerationType.IDENTITY;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import cn.dmdl.stl.hospitalbudget.hospital.entity.YsDepartmentInfo;

/**
 * EarnConfirm generated by hbm2java
 */
@Entity
@Table(name = "earn_confirm", catalog = "hospital_budget")
public class EarnConfirm implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3027209890031354918L;
	private int earnConfirmId;
	private String receiptsCode; //单据编号
	private YsDepartmentInfo ysDepartmentInfo;
	private String reimbursementMan;
	private String voucherCode;
	private String paymentUnit;
	private String invoiceCode;
	private String chequeCode;
	private float totalAmount;
	private Date postingDate;
	private String registerMan;
	private Date registerDate;
	private String remark;
	private Date insertTime;
	private boolean state;
	private Integer process_type;

	public EarnConfirm() {
	}

//	public EarnConfirm(int earnConfirmId, String receiptsCode, int reimbursementDepartmentId, String reimbursementMan, float totalAmount, Date postingDate) {
//		this.earnConfirmId = earnConfirmId;
//		this.receiptsCode = receiptsCode;
//		this.reimbursementDepartmentId = reimbursementDepartmentId;
//		this.reimbursementMan = reimbursementMan;
//		this.totalAmount = totalAmount;
//		this.postingDate = postingDate;
//	}
//
//	public EarnConfirm(int earnConfirmId, String receiptsCode, int reimbursementDepartmentId, String reimbursementMan, String voucherCode, String paymentUnit, String invoiceCode, String chequeCode, float totalAmount, Date postingDate, Date registerDate, String remark, Date insertTime, Integer state) {
//		this.earnConfirmId = earnConfirmId;
//		this.receiptsCode = receiptsCode;
//		this.reimbursementDepartmentId = reimbursementDepartmentId;
//		this.reimbursementMan = reimbursementMan;
//		this.voucherCode = voucherCode;
//		this.paymentUnit = paymentUnit;
//		this.invoiceCode = invoiceCode;
//		this.chequeCode = chequeCode;
//		this.totalAmount = totalAmount;
//		this.postingDate = postingDate;
//		this.registerDate = registerDate;
//		this.remark = remark;
//		this.insertTime = insertTime;
//		this.state = state;
//	}

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "earn_confirm_id", unique = true, nullable = false)
	public int getEarnConfirmId() {
		return this.earnConfirmId;
	}

	public void setEarnConfirmId(int earnConfirmId) {
		this.earnConfirmId = earnConfirmId;
	}

	@Column(name = "receipts_code", nullable = false)
	@NotNull
	public String getReceiptsCode() {
		return this.receiptsCode;
	}

	public void setReceiptsCode(String receiptsCode) {
		this.receiptsCode = receiptsCode;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "reimbursement_department_id")
	public YsDepartmentInfo getYsDepartmentInfo() {
		return ysDepartmentInfo;
	}

	public void setYsDepartmentInfo(YsDepartmentInfo ysDepartmentInfo) {
		this.ysDepartmentInfo = ysDepartmentInfo;
	}

	@Column(name = "reimbursement_man", nullable = false)
	@NotNull
	public String getReimbursementMan() {
		return this.reimbursementMan;
	}

	public void setReimbursementMan(String reimbursementMan) {
		this.reimbursementMan = reimbursementMan;
	}

	@Column(name = "voucher_code")
	public String getVoucherCode() {
		return this.voucherCode;
	}

	public void setVoucherCode(String voucherCode) {
		this.voucherCode = voucherCode;
	}

	@Column(name = "payment_unit")
	public String getPaymentUnit() {
		return this.paymentUnit;
	}

	public void setPaymentUnit(String paymentUnit) {
		this.paymentUnit = paymentUnit;
	}

	@Column(name = "invoice_code")
	public String getInvoiceCode() {
		return this.invoiceCode;
	}

	public void setInvoiceCode(String invoiceCode) {
		this.invoiceCode = invoiceCode;
	}

	@Column(name = "cheque_code")
	public String getChequeCode() {
		return this.chequeCode;
	}

	public void setChequeCode(String chequeCode) {
		this.chequeCode = chequeCode;
	}

	@Column(name = "total_amount", nullable = false, precision = 11)
	public float getTotalAmount() {
		return this.totalAmount;
	}

	public void setTotalAmount(float totalAmount) {
		this.totalAmount = totalAmount;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "posting_date", nullable = false, length = 19)
	@NotNull
	public Date getPostingDate() {
		return this.postingDate;
	}

	public void setPostingDate(Date postingDate) {
		this.postingDate = postingDate;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "register_date", length = 19)
	public Date getRegisterDate() {
		return this.registerDate;
	}

	public void setRegisterDate(Date registerDate) {
		this.registerDate = registerDate;
	}

	@Column(name = "remark", length = 500)
	@Length(max = 500)
	public String getRemark() {
		return this.remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "insert_time", length = 19)
	public Date getInsertTime() {
		return this.insertTime;
	}

	public void setInsertTime(Date insertTime) {
		this.insertTime = insertTime;
	}

	@Column(name = "state", nullable = false)
	public boolean isState() {
		return this.state;
	}

	public void setState(boolean state) {
		this.state = state;
	}
	
	@Column(name = "process_type")
	public Integer getProcess_type() {
		return process_type;
	}

	public void setProcess_type(Integer process_type) {
		this.process_type = process_type;
	}
	
	@Column(name = "register_man", length = 100)
	@Length(max = 100)
	public String getRegisterMan() {
		return registerMan;
	}

	public void setRegisterMan(String registerMan) {
		this.registerMan = registerMan;
	}
}
