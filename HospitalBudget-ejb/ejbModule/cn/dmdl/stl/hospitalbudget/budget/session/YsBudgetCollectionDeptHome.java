package cn.dmdl.stl.hospitalbudget.budget.session;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.common.session.CriterionEntityHome;
import cn.dmdl.stl.hospitalbudget.hospital.entity.YsDepartmentInfo;
import cn.dmdl.stl.hospitalbudget.util.DataSourceManager;

@Name("ysBudgetCollectionDeptHome")
public class YsBudgetCollectionDeptHome extends CriterionEntityHome<Object> {
	private String year;
	private String deptIds;
	private JSONObject budgetCollectionInfo;
	private String budgetCollectionDeptIds;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


/*	public JSONObject pageInitCollectionInfo(){
		
		return filterBudgetCollectionInfo();
	}*/
	
	/**
	 * 按照年份和部门id筛选汇总信息
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public void filterBudgetCollectionInfo(){
		if (budgetCollectionInfo != null) {
			budgetCollectionInfo.clear();
		} else {
			budgetCollectionInfo = new JSONObject();
		}
		String[] depteIdArr = {};
		if(null != deptIds && !deptIds.isEmpty() && !"null".equals(deptIds)){
			depteIdArr = deptIds.split(",");
		}
		//收入预算
		//获取收入预算部门
		Map<Integer, YsDepartmentInfo> incomeDeptMap = getIncomDeptMap(); 
		StringBuilder incomeSql = new StringBuilder();
		incomeSql.append("select bcd.budget_collection_dept_id, ");
		incomeSql.append("bcd.`year`, ");
		incomeSql.append("di.`the_value` as dept_name, ");
		incomeSql.append("SUM(IF(ici.bottom_level = 1,ici.project_amount,0)) as total_amount, ");
		incomeSql.append("bcd.`status`, ");
		incomeSql.append("bcd.dept_id ");
		incomeSql.append("FROM hospital_budget.ys_budget_collection_dept bcd ");
		incomeSql.append("INNER JOIN hospital_budget.ys_income_collection_info ici on bcd.budget_collection_dept_id = ici.budget_collection_dept_id ");
		incomeSql.append("and ici.`year` = '").append(year).append("' and ici.delete=0 ");
		incomeSql.append("INNER JOIN hospital_budget.ys_department_info di ON bcd.dept_id = di.the_id ");
		incomeSql.append("where bcd.`year` = '").append(year).append("' ");
		incomeSql.append("AND bcd.budget_type = 1 ");//收入
		if(depteIdArr.length > 0){
			incomeSql.append("AND bcd.dept_id in (").append(deptIds).append(") ");
		}
		incomeSql.append("GROUP BY bcd.dept_id ");
		System.out.println(incomeSql);
		incomeSql.insert(0, "select * from (").append(") t ");
		//TODO 获得上一年下达的预算数据
		
		//按科室加载编制数据
		double totalIncome = 0;
		List<Object[]> incomeCollectionList = getEntityManager().createNativeQuery(incomeSql.toString()).getResultList();
		JSONArray incomeJsonArr = new JSONArray();
		for(Object[] incomeObjArr : incomeCollectionList){
			JSONObject incomeJson = new JSONObject();
			incomeJson.element("budget_collection_dept_id", incomeObjArr[0]);
			incomeJson.element("year", incomeObjArr[1]);
			incomeJson.element("dept_name", incomeObjArr[2]);
			incomeJson.element("total_amount", incomeObjArr[3]);
			if(null != incomeObjArr[3] && !incomeObjArr[3].toString().isEmpty()){
				totalIncome += Double.parseDouble(incomeObjArr[3].toString());
			}
			incomeJson.element("status", incomeObjArr[4]);
			incomeJson.element("status_name", decodeStatus(incomeObjArr[4].toString()));
			if(incomeDeptMap.containsKey(incomeObjArr[5])){
				incomeDeptMap.remove(incomeObjArr[5]);
			}
			incomeJsonArr.add(incomeJson);
		}
		budgetCollectionInfo.element("income", incomeJsonArr);
		budgetCollectionInfo.element("total_income", totalIncome);
		//加载未提交编制的科室
		JSONArray incomeUnfinishedJsonArr = new JSONArray();
		for(Integer unFinishedDeptId : incomeDeptMap.keySet()){
			JSONObject incomeJson = new JSONObject();
			incomeJson.element("dept_id", incomeDeptMap.get(unFinishedDeptId).getTheId());
			incomeJson.element("dept_name", incomeDeptMap.get(unFinishedDeptId).getTheValue());
			incomeJson.element("year", year);
			incomeJson.element("status_name", "未提交");
			if(depteIdArr.length > 0){//有筛选部门
				if(Arrays.binarySearch(depteIdArr, incomeDeptMap.get(unFinishedDeptId).getTheId().toString()) > -1){//只添加筛选的部门
					incomeUnfinishedJsonArr.add(incomeJson);
				}
			}else{//未筛选部门
				incomeUnfinishedJsonArr.add(incomeJson);
			}
			
		}
		budgetCollectionInfo.element("income_unfinished", incomeUnfinishedJsonArr);
		
		//支出预算
		//获取支出预算部门
		Map<Integer, YsDepartmentInfo> expendDeptMap = getExpendDeptMap(); 
		StringBuilder expendSql = new StringBuilder();
		expendSql.append("select bcd.budget_collection_dept_id, ");
		expendSql.append("bcd.`year`, ");
		expendSql.append("di.`the_value` as dept_name, ");
		expendSql.append("SUM(IF(ici.bottom_level = 1,ici.project_amount,0)) as total_amount, ");
		expendSql.append("bcd.`status`, ");
		expendSql.append("bcd.dept_id ");
		expendSql.append("FROM hospital_budget.ys_budget_collection_dept bcd ");
		expendSql.append("INNER JOIN hospital_budget.ys_expend_collection_info ici on bcd.budget_collection_dept_id = ici.budget_collection_dept_id ");
		expendSql.append("and ici.`year` = '").append(year).append("' and ici.delete=0 ");
		expendSql.append("INNER JOIN hospital_budget.ys_department_info di ON bcd.dept_id = di.the_id ");
		expendSql.append("where bcd.`year` = '").append(year).append("' ");
		expendSql.append("AND bcd.budget_type = 2 ");//支出
		if(depteIdArr.length > 0){
			expendSql.append("AND bcd.dept_id in (").append(deptIds).append(") ");
		}
		expendSql.append("GROUP BY bcd.dept_id ");
		expendSql.insert(0, "select * from (").append(") t ");
		//TODO 获得上一年下达的预算数据
		
		//按科室加载编制数据
		double totalExpend = 0;
		List<Object[]> expendCollectionList = getEntityManager().createNativeQuery(expendSql.toString()).getResultList();
		JSONArray expendJsonArr = new JSONArray();
		for(Object[] expendObjArr : expendCollectionList){
			JSONObject expendJson = new JSONObject();
			expendJson.element("budget_collection_dept_id", expendObjArr[0]);
			expendJson.element("year", expendObjArr[1]);
			expendJson.element("dept_name", expendObjArr[2]);
			expendJson.element("total_amount", expendObjArr[3]);
			if(null != expendObjArr[3] && !expendObjArr[3].toString().isEmpty()){
				totalExpend += Double.parseDouble(expendObjArr[3].toString());
			}
			expendJson.element("status", expendObjArr[4]);
			expendJson.element("status_name", decodeStatus(expendObjArr[4].toString()));
			if(expendDeptMap.containsKey(expendObjArr[5])){
				expendDeptMap.remove(expendObjArr[5]);
			}
			expendJsonArr.add(expendJson);
		}
		budgetCollectionInfo.element("expend", expendJsonArr);
		budgetCollectionInfo.element("total_expend", totalExpend);
		//加载未提交编制的科室
		JSONArray expendUnfinishedJsonArr = new JSONArray();
		for(Integer unFinishedDeptId : expendDeptMap.keySet()){
			JSONObject expendJson = new JSONObject();
			expendJson.element("dept_id", expendDeptMap.get(unFinishedDeptId).getTheId());
			expendJson.element("dept_name", expendDeptMap.get(unFinishedDeptId).getTheValue());
			expendJson.element("year", year);
			expendJson.element("status_name", "未提交");
			if(depteIdArr.length > 0){//有筛选部门
				if(Arrays.binarySearch(depteIdArr, expendDeptMap.get(unFinishedDeptId).getTheId().toString()) > -1){//只添加筛选的部门
					expendUnfinishedJsonArr.add(expendJson);
				}
			}else{//未筛选部门
				expendUnfinishedJsonArr.add(expendJson);
			}
			
		}
		budgetCollectionInfo.element("expend_unfinished", expendUnfinishedJsonArr);
		System.out.println(budgetCollectionInfo);
	}


	/**
	 * 获取支出预算部门
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Map<Integer, YsDepartmentInfo> getExpendDeptMap() {
		Map<Integer, YsDepartmentInfo> incomDeptMap = new HashMap<Integer, YsDepartmentInfo>();
		List<YsDepartmentInfo> YsDepartmentInfoList = getEntityManager().createQuery("select ysDepartmentInfo from YsDepartmentInfo ysDepartmentInfo where ysDepartmentInfo.budgetAttribute is not null and ysDepartmentInfo.deleted=0 ").getResultList();
		for(YsDepartmentInfo ysDepartmentInfo : YsDepartmentInfoList){
			if(ysDepartmentInfo.getBudgetAttribute().indexOf("2") > -1){
				incomDeptMap.put(ysDepartmentInfo.getTheId(), ysDepartmentInfo);
			}
		}
		return incomDeptMap;
	}


	/**
	 * 获取收入预算部门
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Map<Integer, YsDepartmentInfo> getIncomDeptMap() {
		Map<Integer, YsDepartmentInfo> incomDeptMap = new HashMap<Integer, YsDepartmentInfo>();
		List<YsDepartmentInfo> YsDepartmentInfoList = getEntityManager().createQuery("select ysDepartmentInfo from YsDepartmentInfo ysDepartmentInfo where ysDepartmentInfo.budgetAttribute is not null and ysDepartmentInfo.deleted=0 ").getResultList();
		for(YsDepartmentInfo ysDepartmentInfo : YsDepartmentInfoList){
			if(ysDepartmentInfo.getBudgetAttribute().indexOf("1") > -1){
				incomDeptMap.put(ysDepartmentInfo.getTheId(), ysDepartmentInfo);
			}
		}
		return incomDeptMap;
	}

	
	/**
	 * @return
	 */
	public JSONObject submitCollection(){
		System.out.println(budgetCollectionDeptIds);
		JSONObject result = new JSONObject();
		Connection connection = DataSourceManager.open(DataSourceManager.BY_JDBC_DEFAULT);
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			preparedStatement = connection.prepareStatement("update ys_budget_collection_dept set status = 1 where budget_collection_dept_id in (" + budgetCollectionDeptIds + ")");
			preparedStatement.executeUpdate();
			result.element("invoke_result", "ok");
			// TODO: 入下达表
		} catch (Exception e) {
			result.element("invoke_result", "fail");
			result.element("invoke_message", "下达失败！");
		} finally {
			DataSourceManager.close(connection, preparedStatement, resultSet);
		}
		return result;
	}
	

	/**
	 * 获取下达属性描述
	 * @param object
	 * @return
	 */
	private String decodeStatus(String status) {
		String statusName = null;
		switch(Integer.parseInt(status)) { 
			case 0 :
				statusName = "待处理"; 
				break; 
			case 1 : 
				statusName = "已下达"; 
				break;  
			case 2 : 
				statusName = "被追回"; 
				break; 
			default: 
				statusName = "无";
				break; 
		} 
		return statusName;
		
	}


	public String getYear() {
		return year;
	}


	public void setYear(String year) {
		this.year = year;
	}


	public String getDeptIds() {
		return deptIds;
	}


	public void setDeptIds(String deptIds) {
		this.deptIds = deptIds;
	}


	public JSONObject getBudgetCollectionInfo() {
		return budgetCollectionInfo;
	}


	public void setBudgetCollectionInfo(JSONObject budgetCollectionInfo) {
		this.budgetCollectionInfo = budgetCollectionInfo;
	}


	public String getBudgetCollectionDeptIds() {
		return budgetCollectionDeptIds;
	}


	public void setBudgetCollectionDeptIds(String budgetCollectionDeptIds) {
		this.budgetCollectionDeptIds = budgetCollectionDeptIds;
	}

}
