package cn.dmdl.stl.hospitalbudget.budget.session;

import java.util.List;

import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.common.session.CriterionEntityHome;
import cn.dmdl.stl.hospitalbudget.util.HospitalConstant;

/**
 * 业务校验类
 * @author HASEE
 *
 */
@Name("businessCheckHome")
public class BusinessCheckHome extends CriterionEntityHome<Object>{


	private static final long serialVersionUID = 1L;
	
	/**
	 * 获取项目的总金额
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public double getGenericProjectTotalAmount(int genericProjectId){
		double budgetAmount = 0;
		String dataSql = "select t.budget_amount from generic_project t where t.the_id = " + genericProjectId;
		List<Object> dataList = getEntityManager().createNativeQuery(dataSql).getResultList();
		if (dataList != null && dataList.size() > 0) {
			budgetAmount = Double.parseDouble(dataList.get(0).toString());
		}
		return budgetAmount;
	}
	
	/**
	 * 根据项目id获取已经添加的合同的总金额
	 * @param projectId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public double getAuditContractTotalAmount(int genericProjectId){
		double totalContractAmount = 0;
		String dataSql = "select SUM(t.audit_amount) from ys_audit_contract_info t where t.generic_project_id = " + genericProjectId + " and t.deleted = 0";
		List<Object> dataList = getEntityManager().createNativeQuery(dataSql).getResultList();
		if (dataList != null && dataList.size() > 0) {
			totalContractAmount = Double.parseDouble(dataList.get(0).toString());
		}
		return totalContractAmount;
	}
	
	
	/**
	 * 根据项目id获取项目的审计金额
	 * @param projectId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public double getAuditAmount(int genericProjectId){
		double auditAmount = 0;
		String dataSql = "select t.audit_amount from generic_project t where t.the_id = " + genericProjectId;
		List<Object> dataList = getEntityManager().createNativeQuery(dataSql).getResultList();
		if (dataList != null && dataList.size() > 0) {
			if(null != dataList.get(0) && !dataList.get(0).toString().isEmpty()){
				auditAmount = Double.parseDouble(dataList.get(0).toString());
			}
		}
		return auditAmount;
	}
	
	/**
	 * 获取项目的审计完成状态
	 * @param genericProjectId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public boolean isAuditFinished(int genericProjectId){
		boolean isAuditFinished = false;
		String dataSql = "select t.audit_status from generic_project t where t.the_id = " + genericProjectId;
		List<Object> dataList = getEntityManager().createNativeQuery(dataSql).getResultList();
		if (dataList != null && dataList.size() > 0) {
			if(Integer.parseInt(dataList.get(0).toString()) == HospitalConstant.PROJECT_IS_AUDIT_FINISH){
				isAuditFinished = true;
			}
		}
		return isAuditFinished;
	}

}
