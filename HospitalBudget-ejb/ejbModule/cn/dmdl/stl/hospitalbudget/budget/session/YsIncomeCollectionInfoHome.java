package cn.dmdl.stl.hospitalbudget.budget.session;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.common.session.CriterionEntityHome;

@Name("ysIncomeCollectionInfoHome")
public class YsIncomeCollectionInfoHome extends CriterionEntityHome<Object> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Integer expendCollectionDeptId;
	
	/**
	 * 获取汇总明细数据
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public JSONObject getCollectionIncomeInfo(){
		//获取明细数据
		StringBuilder sql = new StringBuilder();
		sql.append("select rp.the_value as project_name, ");
		sql.append("1 as is_usual, ");
		sql.append("ici.project_source, ");
		sql.append("ici.project_amount, ");
		sql.append("ici.formula_remark, ");
		sql.append("ici.routine_project_id as project_id, ");
		sql.append("rp.the_pid, ");
		sql.append("ici.bottom_level, ");
		sql.append("ici.attachment ");
		sql.append("from hospital_budget.ys_budget_collection_dept bcd ");
		sql.append("INNER JOIN hospital_budget.ys_income_collection_info ici ON bcd.budget_collection_dept_id = ici.budget_collection_dept_id  ");
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
		sql.append("ici.attachment ");
		sql.append("from hospital_budget.ys_budget_collection_dept bcd ");
		sql.append("INNER JOIN hospital_budget.ys_income_collection_info ici ON bcd.budget_collection_dept_id = ici.budget_collection_dept_id  ");
		sql.append("AND ici.`delete` = 0 ");
		sql.append("INNER JOIN hospital_budget.generic_project rp ON ici.generic_project_id = rp.the_id ");
		sql.append("WHERE bcd.budget_collection_dept_id = ").append(expendCollectionDeptId).append(" ");
		List<Object[]> incomeCollectionInfoList = getEntityManager().createNativeQuery(sql.toString()).getResultList();
		//TODO 获取上一年的预算数据
		
		
		JSONObject collectionInfo = new JSONObject();
		try{
			JSONArray collectionInfoArr = new JSONArray();
			double totalAmount = 0;
			for(Object[] object : incomeCollectionInfoList){
				JSONObject json = new JSONObject();
				json.element("project_name", object[0]);
				json.element("is_usual", object[1]);
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
				collectionInfoArr.add(json);
			}
			collectionInfo.element("collection_info", collectionInfoArr);
			collectionInfo.element("total_amount", totalAmount/10000);
			
			//获取部门名称
			String deptNameSql = "select di.the_value from ys_budget_collection_dept bcd INNER JOIN ys_department_info di ON bcd.dept_id = di.the_id "
					+ "WHERE bcd.budget_collection_dept_id =" + expendCollectionDeptId;
			List<Object[]> deptNameList = getEntityManager().createNativeQuery(deptNameSql).getResultList();
			String deptName = "";
			for(Object obj : deptNameList){
				deptName = obj.toString();
			}
			collectionInfo.element("dept_name", deptName);
		}catch(UnsupportedEncodingException e){
			e.printStackTrace();
		}

		return collectionInfo;
	}

	public Integer getExpendCollectionDeptId() {
		return expendCollectionDeptId;
	}

	public void setExpendCollectionDeptId(Integer expendCollectionDeptId) {
		this.expendCollectionDeptId = expendCollectionDeptId;
	}

	

}
