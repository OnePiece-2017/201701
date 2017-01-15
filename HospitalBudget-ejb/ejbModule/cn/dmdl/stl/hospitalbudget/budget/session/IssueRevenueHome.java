package cn.dmdl.stl.hospitalbudget.budget.session;

import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.budget.entity.NormalBudgetIncomeInfo;
import cn.dmdl.stl.hospitalbudget.common.session.CriterionEntityHome;

@Name("issueRevenueHome")
public class IssueRevenueHome extends CriterionEntityHome<Object> {

	private static final long serialVersionUID = 1L;
	private String issueArgs;
	private JSONObject issueResult;

	public void issueAction() {
		if (issueResult != null) {
			issueResult.clear();
		} else {
			issueResult = new JSONObject();
		}
		issueResult.accumulate("invoke_result", "INVOKE_SUCCESS");
		issueResult.accumulate("message", "提交成功！");
		issueResult.accumulate("callback", new JSONObject());
		if (issueArgs != null && !"".equals(issueArgs)) {
			try {
				joinTransaction();
				JSONObject dataInfo = JSONObject.fromObject(issueArgs);
				int type = dataInfo.getInt("type");
				int primaryKey = dataInfo.getInt("primaryKey");
				NormalBudgetIncomeInfo normalBudgetIncomeInfo = getEntityManager().find(NormalBudgetIncomeInfo.class, primaryKey);
				if (1 == type) {
					normalBudgetIncomeInfo.setConfirmFlag(1);
				} else if (2 == type) {
					normalBudgetIncomeInfo.setConfirmFlag(2);
				}
				getEntityManager().merge(normalBudgetIncomeInfo);
				JSONObject callback = new JSONObject();
				callback.accumulate("primaryKey", primaryKey);
				callback.accumulate("confirmFlag", normalBudgetIncomeInfo.getConfirmFlag());
				issueResult.element("callback", callback);
				getEntityManager().flush();
			} catch (Exception e) {
				issueResult.element("invoke_result", "INVOKE_FAILURE");
				issueResult.element("message", "操作失败！" + e.getMessage());
			}
		} else {
			issueResult.element("invoke_result", "INVOKE_FAILURE");
			issueResult.element("message", "提交失败！参数为空。");
		}
	}

	@SuppressWarnings("unchecked")
	public JSONArray getAttendProject() {
		JSONArray resultSet = new JSONArray();
		StringBuffer dataSql = new StringBuffer();
		dataSql.append(" select");
		dataSql.append(" normal_budget_income_info.normal_budget_year_id,");
		dataSql.append(" normal_budget_income_info.`year`,");
		dataSql.append(" normal_budget_income_info.project_source,");
		dataSql.append(" normal_budget_income_info.dept_id,");
		dataSql.append(" ys_department_info.the_value as department_name,");
		dataSql.append(" ys_convention_project.multilevel,");
		dataSql.append(" normal_budget_income_info.normal_project_id,");
		dataSql.append(" ys_convention_project.the_value as root_project_name,");
		dataSql.append(" normal_budget_income_info.sub_project_id,");
		dataSql.append(" ys_convention_project_extend.the_value as leaf_project_name,");
		dataSql.append(" normal_budget_income_info.budget_amount,");
		dataSql.append(" normal_budget_income_info.confirm_flag");
		dataSql.append(" from normal_budget_income_info");
		dataSql.append(" left join ys_department_info on ys_department_info.the_id = normal_budget_income_info.dept_id");
		dataSql.append(" left join ys_convention_project on ys_convention_project.the_id = normal_budget_income_info.normal_project_id");
		dataSql.append(" left join ys_convention_project_extend on ys_convention_project_extend.the_id = normal_budget_income_info.sub_project_id");
		dataSql.append(" order by normal_budget_income_info.normal_budget_year_id asc");
		dataSql.insert(0, "select * from (").append(") as recordset");// 解决找不到列
		List<Object[]> dataList = getEntityManager().createNativeQuery(dataSql.toString()).getResultList();
		if (dataList != null && dataList.size() > 0) {
			for (Object[] data : dataList) {
				boolean multilevel = data[5].equals(true);
				JSONObject row = new JSONObject();
				row.accumulate("isRoot", !multilevel || null == data[8]);
				row.accumulate("normalBudgetYearId", data[0]);
				row.accumulate("year", data[1]);
				row.accumulate("projectSource", "部门惯例");
				row.accumulate("departmentName", data[4]);
				row.accumulate("multilevel", multilevel);
				row.accumulate("projectName", data[8] != null ? data[9] : data[7]);
				row.accumulate("normalProjectId", data[6]);
				row.accumulate("rootProjectName", data[7]);
				row.accumulate("subProjectId", data[8]);
				row.accumulate("leafProjectName", data[9]);
				row.accumulate("budgetAmount", data[10]);
				row.accumulate("confirmFlag", data[11]);
				resultSet.add(row);
			}
		}
		return resultSet;
	}

	public JSONArray getTableData() {
		return null;
	}

	public JSONObject getIssueResult() {
		return issueResult;
	}

	public void setIssueArgs(String issueArgs) {
		this.issueArgs = issueArgs;
	}
}
