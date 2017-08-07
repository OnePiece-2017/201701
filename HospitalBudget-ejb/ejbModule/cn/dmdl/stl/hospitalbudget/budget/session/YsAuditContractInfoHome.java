package cn.dmdl.stl.hospitalbudget.budget.session;

import java.util.Date;

import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.budget.entity.YsAuditContractInfo;
import cn.dmdl.stl.hospitalbudget.common.session.CriterionEntityHome;

@Name("ysAuditContractInfoHome")
public class YsAuditContractInfoHome extends CriterionEntityHome<YsAuditContractInfo> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Integer genericProjectId;
	
	private BusinessCheckHome businessCheckHome = new BusinessCheckHome();

	public void setYsAuditContractInfoAuditContractInfoId(Integer id) {
		setId(id);
	}

	public Integer getYsAuditContractInfoAuditContractInfoId() {
		return (Integer) getId();
	}

	@Override
	protected YsAuditContractInfo createInstance() {
		YsAuditContractInfo ysAuditContractInfo = new YsAuditContractInfo();
		return ysAuditContractInfo;
	}

	public void load() {
		if (isIdDefined()) {
			wire();
		}
	}

	public void wire() {
		getInstance();
	}
	
	@Override
	public String persist() {
		//业务校验，合同总金额不能大于项目金额
		setMessage("");
		double projectBudgetAmount = businessCheckHome.getGenericProjectTotalAmount(genericProjectId);
		double contractAmount = businessCheckHome.getAuditContractTotalAmount(genericProjectId) + instance.getAuditAmount();
		if(contractAmount > projectBudgetAmount){
			setMessage("合同总金额不能超过项目总预算金额！合同金额： " + contractAmount + "  项目金额：" + projectBudgetAmount);
			return null;
		}
		
		joinTransaction();
		instance.setGenericProjectId(genericProjectId);
		instance.setDeptId(sessionToken.getDepartmentInfoId());
		//instance.setAttachment("");
		instance.setInsertUser(sessionToken.getUserInfoId());
		instance.setInsertTime(new Date());
		getEntityManager().persist(instance);
		getEntityManager().flush();
		raiseAfterTransactionSuccessEvent();
		return "persisted";
	}

	public boolean isWired() {
		return true;
	}

	public YsAuditContractInfo getDefinedInstance() {
		return isIdDefined() ? getInstance() : null;
	}

	public Integer getGenericProjectId() {
		return genericProjectId;
	}

	public void setGenericProjectId(Integer genericProjectId) {
		this.genericProjectId = genericProjectId;
	}
}
