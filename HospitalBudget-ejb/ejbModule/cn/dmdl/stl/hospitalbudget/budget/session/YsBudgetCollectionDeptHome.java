package cn.dmdl.stl.hospitalbudget.budget.session;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.common.session.CriterionEntityHome;
import cn.dmdl.stl.hospitalbudget.hospital.entity.YsDepartmentInfo;
import cn.dmdl.stl.hospitalbudget.util.Assit;
import cn.dmdl.stl.hospitalbudget.util.DataSourceManager;
import cn.dmdl.stl.hospitalbudget.util.HospitalConstant;

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
		//获得上一年下达的预算数据
		CommonDaoHome commonDaoHome = new CommonDaoHome();
		Map<String, Double> lastYearIncomeAmountMap = commonDaoHome.getLastYearTotalIncomeBudget(year);
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
			incomeJson.element("with_last_year_num", "--");
			incomeJson.element("with_last_year_percent", "--");
			if(null != incomeObjArr[3] && !incomeObjArr[3].toString().isEmpty()){
				double thisYearTotalAmount = Double.parseDouble(incomeObjArr[3].toString());
				totalIncome += thisYearTotalAmount;
				if(lastYearIncomeAmountMap.containsKey(incomeObjArr[5].toString())){
					double lastYearTotalAmount = lastYearIncomeAmountMap.get(incomeObjArr[5].toString());
					incomeJson.element("with_last_year_num", thisYearTotalAmount - lastYearTotalAmount);
					incomeJson.element("with_last_year_percent", Assit.formatDouble2((thisYearTotalAmount - lastYearTotalAmount)/lastYearTotalAmount*100) + "%");
				}
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
		System.out.println(expendSql);
		//TODO 获得上一年下达的预算数据
		Map<String, Double> lastYearExpendAmountMap = commonDaoHome.getLastYearTotalExpendBudget(year);
		//按科室加载编制数据
		double totalExpend = 0;
		List<Object[]> expendCollectionList = getEntityManager().createNativeQuery(expendSql.toString()).getResultList();
		JSONArray expendJsonArr = new JSONArray();
		for(Object[] expendObjArr : expendCollectionList){
			JSONObject expendJson = new JSONObject();
			expendJson.element("budget_collection_dept_id", expendObjArr[0]);
			expendJson.element("year", expendObjArr[1]);
			expendJson.element("dept_name", expendObjArr[2]);
			expendJson.element("total_amount", Double.parseDouble(expendObjArr[3].toString())/10000);
			//默认无数据
			expendJson.element("with_last_year_num", "--");
			expendJson.element("with_last_year_percent", "--");
			if(null != expendObjArr[3] && !expendObjArr[3].toString().isEmpty()){
				double thisYearTotalAmount = Double.parseDouble(expendObjArr[3].toString());
				totalExpend += thisYearTotalAmount;
				if(lastYearExpendAmountMap.containsKey(expendObjArr[5].toString())){
					double lastYearTotalAmount = lastYearExpendAmountMap.get(expendObjArr[5].toString());
					expendJson.element("with_last_year_num", thisYearTotalAmount - lastYearTotalAmount);
					expendJson.element("with_last_year_percent", Assit.formatDouble2((thisYearTotalAmount - lastYearTotalAmount)/lastYearTotalAmount*100) + "%");
				}
			}
			expendJson.element("status", expendObjArr[4]);
			expendJson.element("status_name", decodeStatus(expendObjArr[4].toString()));
			if(expendDeptMap.containsKey(expendObjArr[5])){
				expendDeptMap.remove(expendObjArr[5]);
			}
			expendJsonArr.add(expendJson);
		}
		budgetCollectionInfo.element("expend", expendJsonArr);
		budgetCollectionInfo.element("total_expend", totalExpend/10000);
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
		JSONObject result = new JSONObject();
		Connection connection = DataSourceManager.open(DataSourceManager.BY_JDBC_DEFAULT);
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			connection.setAutoCommit(false);
			//收入预算下达
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT bcd.dept_id, ");
			sql.append("bcd.`year`, ");
			sql.append("ici.generic_project_id, ");
			sql.append("ici.routine_project_id, ");
			sql.append("ici.project_amount, ");
			sql.append("ici.bottom_level ");
			sql.append("FROM ys_budget_collection_dept bcd ");
			sql.append("INNER JOIN ys_income_collection_info ici ON bcd.budget_collection_dept_id = ici.budget_collection_dept_id AND ici.delete = 0 ");
			sql.append("WHERE bcd.status = 0 AND bcd.budget_type = 1 AND ici.bottom_level = 1 ");
			sql.append("AND bcd.budget_collection_dept_id in (").append(budgetCollectionDeptIds).append(")");
			preparedStatement = connection.prepareStatement(sql.toString());
			resultSet = preparedStatement.executeQuery();
			while(resultSet.next()){
				preparedStatement = connection.prepareStatement("INSERT INTO `ys_income_plan_info` (`dept_id`, `year`, `routine_project_id`, `generic_project_id`, `budget_amount`, `insert_user`, `insert_time`) VALUES "
						+ "(?, ?, ?, ?, ?, ?, NOW())");
				preparedStatement.setInt(1, resultSet.getInt("dept_id"));
				preparedStatement.setString(2, resultSet.getString("year"));
				preparedStatement.setString(3, resultSet.getString("routine_project_id"));
				preparedStatement.setString(4, resultSet.getString("generic_project_id"));
				preparedStatement.setDouble(5, resultSet.getDouble("project_amount"));
				preparedStatement.setInt(6, sessionToken.getUserInfoId());
				preparedStatement.executeUpdate();
			}
			
			//支出预算下达
			StringBuilder sql2 = new StringBuilder();
			sql2.append("SELECT bcd.dept_id, ");
			sql2.append("bcd.`year`, ");
			sql2.append("ici.generic_project_id, ");
			sql2.append("ici.routine_project_id, ");
			sql2.append("ici.project_amount, ");
			sql2.append("ici.bottom_level ");
			sql2.append("FROM ys_budget_collection_dept bcd ");
			sql2.append("INNER JOIN ys_expend_collection_info ici ON bcd.budget_collection_dept_id = ici.budget_collection_dept_id AND ici.delete = 0 ");
			sql2.append("WHERE bcd.status = 0 AND bcd.budget_type = 2 AND ici.bottom_level = 1 ");
			sql2.append("AND bcd.budget_collection_dept_id in (").append(budgetCollectionDeptIds).append(")");
			System.out.println(sql2);
			preparedStatement = connection.prepareStatement(sql2.toString());
			resultSet = preparedStatement.executeQuery();
			while(resultSet.next()){
				preparedStatement = connection.prepareStatement("INSERT INTO `normal_expend_plan_info` (`dept_id`, `year`, `project_id`, `generic_project_id`, `budget_amount`, `budget_amount_surplus`,  `insert_user`, `insert_time`) VALUES "
						+ "(?, ?, ?, ?, ?, ?, ?, NOW())");
				preparedStatement.setInt(1, resultSet.getInt("dept_id"));
				preparedStatement.setString(2, resultSet.getString("year"));
				preparedStatement.setString(3, resultSet.getString("routine_project_id"));
				preparedStatement.setString(4, resultSet.getString("generic_project_id"));
				preparedStatement.setDouble(5, resultSet.getDouble("project_amount"));
				preparedStatement.setDouble(6, resultSet.getDouble("project_amount"));
				preparedStatement.setInt(7, sessionToken.getUserInfoId());
				preparedStatement.executeUpdate();
			}
			preparedStatement = connection.prepareStatement("update ys_budget_collection_dept set status = 1 where budget_collection_dept_id in (" + budgetCollectionDeptIds + ")");
			preparedStatement.executeUpdate();
			result.element("invoke_result", "ok");
			connection.commit();
			connection.setAutoCommit(true);
		} catch (Exception e) {
			result.element("invoke_result", "fail");
			result.element("invoke_message", "下达失败！");
			try {
				connection.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
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
			case HospitalConstant.COLLECTIONSTATUS_WAIT :
				statusName = "待处理"; 
				break; 
			case HospitalConstant.COLLECTIONSTATUS_FINISH : 
				statusName = "已下达"; 
				break;  
			case HospitalConstant.COLLECTIONSTATUS_RETURN : 
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
