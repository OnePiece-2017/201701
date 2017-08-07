package cn.dmdl.stl.hospitalbudget.budget.session;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.common.session.CriterionEntityHome;
import cn.dmdl.stl.hospitalbudget.util.DataSourceManager;
import cn.dmdl.stl.hospitalbudget.util.HospitalConstant;

@Name("expendAuditHome")
public class ExpendAuditHome extends CriterionEntityHome<Object>{
	
	private BusinessCheckHome businessCheckHome = new BusinessCheckHome();
	
	private String year;
	private Integer deptId;
	private Integer fundsSourceId;

	private Integer genericProjectId;
	private Integer contractId;
	private double auditAmount;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * 删除合同
	 * @return
	 */
	public JSONObject deleteContract(){
		JSONObject json = new JSONObject();
		Connection connection = DataSourceManager.open(DataSourceManager.BY_JDBC_DEFAULT);
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			preparedStatement = connection.prepareStatement("update ys_audit_contract_info t set t.deleted = 1 where t.audit_contract_info_id = ? ");
			preparedStatement.setInt(1, contractId);
			preparedStatement.executeUpdate();
			json.element("isok", "ok");
			json.element("message", "删除成功！");
		} catch (SQLException e) {
			e.printStackTrace();
			json.element("isok", "err");
			json.element("message", "系统错误！");
			return json;
		} finally{
			DataSourceManager.close(connection, preparedStatement, resultSet);
		}
		return json;
	}
	
	
	/**
	 * 审计项目
	 * @return
	 */
	public JSONObject auditProject(){
		JSONObject json = new JSONObject();
		//业务验证 审计金额不能大于项目预算金额
		double projectBudgetAmount = businessCheckHome.getGenericProjectTotalAmount(genericProjectId);
		if(auditAmount > projectBudgetAmount){
			json.element("isok", "err");
			json.element("message", "审计金额不能大于项目总金额！ 项目金额：" + projectBudgetAmount);
			return json;
		}
		Connection connection = DataSourceManager.open(DataSourceManager.BY_JDBC_DEFAULT);
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			preparedStatement = connection.prepareStatement("update generic_project t set t.audit_status = ?,t.audit_amount = ? where t.the_id = ? ");
			preparedStatement.setInt(1, HospitalConstant.PROJECT_IS_AUDIT_FINISH);
			preparedStatement.setDouble(2, auditAmount);
			preparedStatement.setInt(3, genericProjectId);
			preparedStatement.executeUpdate();
			json.element("isok", "ok");
			json.element("message", "审计成功！");
		} catch (SQLException e) {
			e.printStackTrace();
			json.element("isok", "err");
			json.element("message", "系统错误！");
			return json;
		} finally{
			DataSourceManager.close(connection, preparedStatement, resultSet);
		}
		return json;
	}
	
	
	
	/**
	 * 获取合同列表
	 * @return
	 */
	public JSONObject getAuditContract(){
		JSONObject auditContractJson = new JSONObject();
		StringBuilder sql = new StringBuilder();
		sql.append("select t.audit_contract_info_id,t.audit_title,t.audit_amount,t.attachment FROM ys_audit_contract_info t ");
		sql.append("where t.deleted = 0 and t.generic_project_id = ? ");
		Connection connection = DataSourceManager.open(DataSourceManager.BY_JDBC_DEFAULT);
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			preparedStatement = connection.prepareStatement(sql.toString());
			preparedStatement.setInt(1, genericProjectId);
			resultSet = preparedStatement.executeQuery();
			List<JSONObject> list = new ArrayList<JSONObject>(); 
			while(resultSet.next()){
				JSONObject json = new JSONObject();
				json.element("audit_contract_info_id", resultSet.getInt("audit_contract_info_id"));
				json.element("audit_title", resultSet.getString("audit_title"));
				json.element("audit_amount", resultSet.getDouble("audit_amount"));
				json.element("attachment", resultSet.getString("attachment"));
				list.add(json);
			}
			auditContractJson.element("data", list);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			DataSourceManager.close(connection, preparedStatement, resultSet);
		}
		return auditContractJson;
	}

	
	/**
	 * 获取审计项目列表
	 * @return
	 */
	public JSONObject getExpendAuditProject(){
		JSONObject expendAuditProjectJson = new JSONObject();
		StringBuilder sql = new StringBuilder();
		Connection connection = DataSourceManager.open(DataSourceManager.BY_JDBC_DEFAULT);
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			List<Object> paramList = new ArrayList<Object>();
			sql.append("SELECT t.`year`,t.generic_project_id,gp.the_value,di.the_value as dept_name,gp.audit_amount FROM normal_expend_plan_info t  ");
			sql.append("INNER JOIN generic_project gp ON t.generic_project_id = gp.the_id ");
			sql.append("INNER JOIN ys_department_info di ON gp.department_info_id = di.the_id ");
			sql.append("where gp.is_audit = ? ");
			paramList.add(HospitalConstant.PROJECT_IS_AUDIT);
			if(null != deptId){
				sql.append("AND t.dept_id = ? ");
				paramList.add(deptId);
			}
			if(null != year){
				sql.append("AND t.`year` = ? ");
				paramList.add(year);
			}
			if(null != fundsSourceId){
				sql.append("AND gp.funds_source_id = ? ");
				paramList.add(fundsSourceId);
			}
			preparedStatement = connection.prepareStatement(sql.toString());
			DataSourceManager.setParams(preparedStatement, paramList);
			resultSet = preparedStatement.executeQuery();
			List<JSONObject> list = new ArrayList<JSONObject>(); 
			while(resultSet.next()){
				JSONObject json = new JSONObject();
				json.element("year", resultSet.getString("year"));
				json.element("generic_project_id", resultSet.getInt("generic_project_id"));
				json.element("project_name", resultSet.getString("the_value"));
				json.element("dept_name", resultSet.getString("dept_name"));
				json.element("audit_amount", null == resultSet.getObject("audit_amount") ? "" : resultSet.getDouble("audit_amount"));
				list.add(json);
			}
			expendAuditProjectJson.element("data", list);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			DataSourceManager.close(connection, preparedStatement, resultSet);
		}
		return expendAuditProjectJson;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public Integer getDeptId() {
		return deptId;
	}

	public void setDeptId(Integer deptId) {
		this.deptId = deptId;
	}

	public Integer getFundsSourceId() {
		return fundsSourceId;
	}

	public void setFundsSourceId(Integer fundsSourceId) {
		this.fundsSourceId = fundsSourceId;
	}

	public Integer getGenericProjectId() {
		return genericProjectId;
	}

	public void setGenericProjectId(Integer genericProjectId) {
		this.genericProjectId = genericProjectId;
	}

	public Integer getContractId() {
		return contractId;
	}

	public void setContractId(Integer contractId) {
		this.contractId = contractId;
	}

	public double getAuditAmount() {
		return auditAmount;
	}

	public void setAuditAmount(double auditAmount) {
		this.auditAmount = auditAmount;
	}
	
	

}
