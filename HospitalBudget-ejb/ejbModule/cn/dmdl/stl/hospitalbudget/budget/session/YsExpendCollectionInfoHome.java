package cn.dmdl.stl.hospitalbudget.budget.session;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.common.session.CriterionEntityHome;
import cn.dmdl.stl.hospitalbudget.util.CommonDaoUtil;
import cn.dmdl.stl.hospitalbudget.util.DataSourceManager;
import cn.dmdl.stl.hospitalbudget.util.NumberUtil;

@Name("ysExpendCollectionInfoHome")
public class YsExpendCollectionInfoHome extends CriterionEntityHome<Object> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Integer expendCollectionDeptId;
	private Integer collectionId;
	
	/**
	 * 获取汇总明细数据
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public JSONObject getCollectionExpendInfo(){
		Map<String, String> projectNatureMap = CommonDaoUtil.getProjectNatureMap();
		//先获取年份
		StringBuilder sql = new StringBuilder();
		sql.append("select bcd.year from hospital_budget.ys_budget_collection_dept bcd ");
		sql.append("WHERE bcd.budget_collection_dept_id = ").append(expendCollectionDeptId).append(" ");
		String lastYear = "2016";
		List<Object> dataList = getEntityManager().createNativeQuery(sql.toString()).getResultList();
		if(dataList.size() > 0){
			lastYear = (Integer.parseInt(dataList.get(0).toString()) - 1) + "";
		}
		//获取明细数据
		sql.delete(0, sql.length());
		sql.append("select rp.the_value as project_name, ");
		sql.append("1 as is_usual, ");
		sql.append("ici.project_source, ");
		sql.append("ici.project_amount, ");
		sql.append("ici.formula_remark, ");
		sql.append("ici.routine_project_id as project_id, ");
		sql.append("rp.the_pid, ");
		sql.append("ici.bottom_level, ");
		sql.append("ici.attachment, ");
		sql.append("ici.expend_collection_info_id, ");
		sql.append("bcd.status, ");
		sql.append("rp.top_level_project_id, ");
		sql.append("ici.project_nature ");
		sql.append("from hospital_budget.ys_budget_collection_dept bcd ");
		sql.append("INNER JOIN hospital_budget.ys_expend_collection_info ici ON bcd.budget_collection_dept_id = ici.budget_collection_dept_id  ");
		sql.append("AND ici.`delete` = 0 ");
		sql.append("INNER JOIN hospital_budget.routine_project rp ON ici.routine_project_id = rp.the_id ");
		sql.append("WHERE bcd.budget_collection_dept_id = ").append(expendCollectionDeptId).append(" ");
		sql.append("UNION ALL ");
		sql.append("select rp.the_value as project_name, ");
		sql.append("2 as is_usual, ");
		sql.append("ici.project_source, ");
		sql.append("ici.project_amount, ");
		sql.append("ici.formula_remark, ");
		sql.append("ici.generic_project_id as project_id, ");
		sql.append("rp.the_pid, ");
		sql.append("ici.bottom_level, ");
		sql.append("ici.attachment, ");
		sql.append("ici.expend_collection_info_id, ");
		sql.append("bcd.status, ");
		sql.append("rp.top_level_project_id, ");
		sql.append("ici.project_nature ");
		sql.append("from hospital_budget.ys_budget_collection_dept bcd ");
		sql.append("INNER JOIN hospital_budget.ys_expend_collection_info ici ON bcd.budget_collection_dept_id = ici.budget_collection_dept_id  ");
		sql.append("AND ici.`delete` = 0 ");
		sql.append("INNER JOIN hospital_budget.generic_project rp ON ici.generic_project_id = rp.the_id ");
		sql.append("WHERE bcd.budget_collection_dept_id = ").append(expendCollectionDeptId).append(" ");
		sql.insert(0, "select * from (").append(") t order by t.top_level_project_id,t.bottom_level ");
		List<Object[]> incomeCollectionInfoList = getEntityManager().createNativeQuery(sql.toString()).getResultList();
		//获取上一年的预算数据
		ExpendDraftHome expendDraftHome = new ExpendDraftHome();
		Map<String, Double> lastYearRoutineMap = expendDraftHome.getRoutineProjectBudgetMoney(lastYear);
		Map<String, Double> lastYearGenericMap = expendDraftHome.getGenericProjectBudgetMoney(lastYear);
		
		JSONObject collectionInfo = new JSONObject();
		try{
			JSONArray collectionInfoArr = new JSONArray();
			double totalAmount = 0;
			for(Object[] object : incomeCollectionInfoList){
				JSONObject json = new JSONObject();
				json.element("project_name", object[0]);
				int is_usual = Integer.parseInt(object[1].toString());
				json.element("is_usual", is_usual);
				json.element("with_last_year_num", "--");
				json.element("with_last_year_percent", "--");
				if(is_usual == 1){//常规项目
					String projectId = object[5].toString();
					if(lastYearRoutineMap.containsKey(projectId)){
						double lastYearBudget = lastYearRoutineMap.get(projectId);
						double budgetDifference = Double.parseDouble(object[3].toString()) - lastYearBudget;
						json.element("with_last_year_num", budgetDifference/10000);
						json.element("with_last_year_percent", NumberUtil.double2Str(budgetDifference/lastYearBudget*100));
					}
					
				}else{//项目
					String projectId = object[5].toString();
					if(lastYearGenericMap.containsKey(projectId)){
						double lastYearBudget = lastYearGenericMap.get(projectId);
						double budgetDifference = Double.parseDouble(object[3].toString()) - lastYearBudget;
						json.element("with_last_year_num", budgetDifference);
						json.element("with_last_year_percent", NumberUtil.double2Str(budgetDifference/lastYearBudget*100));
					}
				}
				json.element("project_source", URLDecoder.decode(object[2].toString(), "utf-8"));
				json.element("project_amount", Double.parseDouble(object[3].toString())/10000);
				json.element("formula_remark", URLDecoder.decode(object[4].toString(), "utf-8"));
				json.element("project_id", object[5]);
				json.element("the_pid", object[6]);
				json.element("bottom_level", object[7]);
				if(Integer.parseInt(object[7].toString()) == 1){
					totalAmount += Double.parseDouble(object[3].toString());
				}
				json.element("attachment", object[8]);
				json.element("expend_collection_info_id", object[9]);
				json.element("status", object[10]);
				json.element("project_nature", projectNatureMap.get(object[12].toString()));
				collectionInfoArr.add(json);
			}
			collectionInfo.element("collection_info", collectionInfoArr);
			collectionInfo.element("total_amount", totalAmount/10000);
			String deptNameSql = "select di.the_value from ys_budget_collection_dept bcd INNER JOIN ys_department_info di ON bcd.dept_id = di.the_id "
					+ "WHERE bcd.budget_collection_dept_id =" + expendCollectionDeptId;
			List<Object[]> deptNameList = getEntityManager().createNativeQuery(deptNameSql).getResultList();
			String deptName = "";
			for(Object obj : deptNameList){
				deptName = obj.toString();
			}
			collectionInfo.element("dept_name", deptName);
			
			System.out.println(collectionInfo);
		}catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return collectionInfo;
	}
	
	/**
	 * 删除某条编制信息
	 */
	public void deleteCollectionById(){
		Connection connection = DataSourceManager.open(DataSourceManager.BY_JDBC_DEFAULT);
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			preparedStatement = connection.prepareStatement("delete from ys_expend_collection_info where expend_collection_info_id = ?");
			preparedStatement.setInt(1, collectionId);
			preparedStatement.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DataSourceManager.close(connection, preparedStatement, resultSet);
		}
	}

	public Integer getExpendCollectionDeptId() {
		return expendCollectionDeptId;
	}

	public void setExpendCollectionDeptId(Integer expendCollectionDeptId) {
		this.expendCollectionDeptId = expendCollectionDeptId;
	}

	public Integer getCollectionId() {
		return collectionId;
	}

	public void setCollectionId(Integer collectionId) {
		this.collectionId = collectionId;
	}
	
	


}
