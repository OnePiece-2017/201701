package cn.dmdl.stl.hospitalbudget.budget.session;

import java.util.Date;

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
