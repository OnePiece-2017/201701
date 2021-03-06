package cn.dmdl.stl.hospitalbudget.budget.session;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.budget.entity.NormalBudgetCollectionInfo;
import cn.dmdl.stl.hospitalbudget.budget.entity.NormalBudgetIncomeInfo;
import cn.dmdl.stl.hospitalbudget.budget.entity.NormalBudgetOrderInfo;
import cn.dmdl.stl.hospitalbudget.common.session.CriterionEntityHome;

@Name("budgetSummaryHome")
public class BudgetSummaryHome extends CriterionEntityHome<Object> {

	private static final long serialVersionUID = 1L;
	private String saveArgs;
	private JSONObject saveResult;
	private String year;
	private Integer amountType;
	private String orderSn;

	@SuppressWarnings("unchecked")
	public void saveAction() {
		if (saveResult != null) {
			saveResult.clear();
		} else {
			saveResult = new JSONObject();
		}
		saveResult.accumulate("invoke_result", "INVOKE_SUCCESS");
		saveResult.accumulate("message", "提交成功！");
		if (saveArgs != null && !"".equals(saveArgs)) {
			try {// TODO: 代码需优化
				joinTransaction();
				JSONObject dataInfo = JSONObject.fromObject(saveArgs);
				JSONArray submitArr = dataInfo.getJSONArray("submit");
				String submitIds = submitArr.toString().substring(1, submitArr.toString().length() - 1);
				if (submitIds != null && !"".equals(submitIds)) {
					List<NormalBudgetCollectionInfo> normalBudgetCollectionInfoList = getEntityManager().createQuery("select normalBudgetCollectionInfo from NormalBudgetCollectionInfo normalBudgetCollectionInfo where normalBudgetCollectionId in (" + submitIds + ")").getResultList();
					if (normalBudgetCollectionInfoList != null && normalBudgetCollectionInfoList.size() > 0) {
						for (NormalBudgetCollectionInfo normalBudgetCollectionInfo : normalBudgetCollectionInfoList) {
							if (normalBudgetCollectionInfo.getAmountType() == 1) {// 收入预算
								normalBudgetCollectionInfo.setSubmitFlag(true);
								getEntityManager().merge(normalBudgetCollectionInfo);
								List<NormalBudgetOrderInfo> normalBudgetOrderInfoList = getEntityManager().createQuery("select normalBudgetOrderInfo from NormalBudgetOrderInfo normalBudgetOrderInfo where isNew = 1 and orderSn = '" + normalBudgetCollectionInfo.getOrderSn() + "'").getResultList();
								if (normalBudgetOrderInfoList != null && normalBudgetOrderInfoList.size() > 0) {
									for (NormalBudgetOrderInfo normalBudgetOrderInfo : normalBudgetOrderInfoList) {
										NormalBudgetIncomeInfo normalBudgetIncomeInfo = new NormalBudgetIncomeInfo();
										normalBudgetIncomeInfo.setDeptId(normalBudgetCollectionInfo.getDeptId());
										normalBudgetIncomeInfo.setYear(normalBudgetOrderInfo.getYear());
										normalBudgetIncomeInfo.setNormalProjectId(normalBudgetOrderInfo.getNormalProjectId());
										normalBudgetIncomeInfo.setSubProjectId(normalBudgetOrderInfo.getSubProjectId());
										normalBudgetIncomeInfo.setProjectSource(normalBudgetOrderInfo.getProjectSource());
										normalBudgetIncomeInfo.setBudgetAmount(normalBudgetOrderInfo.getProjectAmount());
										normalBudgetIncomeInfo.setInsertTime(new Date());
										normalBudgetIncomeInfo.setInsertUser(sessionToken.getUserInfoId());
										normalBudgetIncomeInfo.setConfirmFlag(0);
										getEntityManager().persist(normalBudgetIncomeInfo);
									}
								}
							}
						}
					}
				}
				getEntityManager().flush();
			} catch (Exception e) {
				saveResult.element("invoke_result", "INVOKE_FAILURE");
				saveResult.element("message", "操作失败！" + e.getMessage());
			}
		} else {
			saveResult.element("invoke_result", "INVOKE_FAILURE");
			saveResult.element("message", "提交失败！参数为空。");
		}
	}

	@SuppressWarnings("unchecked")
	public JSONArray getAttendProject() {
		JSONArray resultSet = new JSONArray();
		StringBuffer dataSql = new StringBuffer();
		dataSql.append(" select");
		dataSql.append(" normal_budget_collection_info.normal_budget_collection_id,");
		dataSql.append(" normal_budget_collection_info.`year`,");
		dataSql.append(" ys_department_info.the_id as department_id,");
		dataSql.append(" ys_department_info.the_value as department_name,");
		dataSql.append(" normal_budget_collection_info.order_sn,");
		dataSql.append(" normal_budget_collection_info.amount_type,");
		dataSql.append(" normal_budget_collection_info.budget_amount");
		dataSql.append(" from normal_budget_collection_info");
		dataSql.append(" left join ys_department_info on ys_department_info.the_id = normal_budget_collection_info.dept_id");
		dataSql.append(" where normal_budget_collection_info.submit_flag = 0");
		dataSql.insert(0, "select * from (").append(") as recordset");// 解决找不到列
		List<Object[]> dataList = getEntityManager().createNativeQuery(dataSql.toString()).getResultList();
		if (dataList != null && dataList.size() > 0) {
			for (Object[] data : dataList) {
				JSONObject row = new JSONObject();
				row.accumulate("normalBudgetCollectionId", data[0]);
				row.accumulate("year", data[1]);
				row.accumulate("departmentId", data[2]);
				row.accumulate("departmentName", data[3]);
				row.accumulate("orderSn", data[4]);
				int amountType = (Integer) data[5];
				if (amountType == 1) {
					row.accumulate("amountTypeName", "收入预算");
				} else if (amountType == 2) {
					row.accumulate("amountTypeName", "支出预算");
				}
				row.accumulate("amountType", amountType);
				row.accumulate("budgetAmount", data[6]);
				resultSet.add(row);
			}
		}
		return resultSet;
	}
	
	/**
	 * 临时方法待改造
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Object[]> getCollectionInfo(){
		List<Object[]> list = new ArrayList<Object[]>();
		StringBuffer dataSql = new StringBuffer();
		dataSql.append(" select");
		dataSql.append(" normal_budget_order_info.task_order_id,");
		dataSql.append(" ys_department_info.the_value AS department_name,");
		dataSql.append(" normal_budget_order_info.`year`,");
		dataSql.append(" ys_convention_project.multilevel,");
		dataSql.append(" normal_budget_order_info.normal_project_id,");
		dataSql.append(" normal_budget_order_info.sub_project_id,");
		dataSql.append(" ifnull(ys_convention_project_extend.the_value, ys_convention_project.the_value) as project_name,");
		dataSql.append(" normal_budget_order_info.project_amount,");
		dataSql.append(" normal_budget_order_info.formula,");
		dataSql.append(" normal_budget_order_info.remark,");
		dataSql.append(" normal_budget_order_info.project_source");
		dataSql.append(" from normal_budget_order_info");
		dataSql.append(" left join ys_convention_project on ys_convention_project.the_id = normal_budget_order_info.normal_project_id");
		dataSql.append(" left join ys_convention_project_extend on ys_convention_project_extend.the_id = normal_budget_order_info.sub_project_id");
		dataSql.append(" LEFT JOIN ys_department_info on ys_convention_project.department_info_id = ys_department_info.the_id");
		dataSql.append(" where normal_budget_order_info.is_new = 1 and normal_budget_order_info.order_sn = '").append(orderSn).append("' ");
		dataSql.append(" union all ");
		dataSql.append(" select");
		dataSql.append(" normal_expend_budget_order_info.task_order_id,");
		dataSql.append(" ys_department_info.the_value AS department_name,");
		dataSql.append(" normal_expend_budget_order_info.`year`,");
		dataSql.append(" ys_convention_project.multilevel,");
		dataSql.append(" normal_expend_budget_order_info.normal_project_id,");
		dataSql.append(" normal_expend_budget_order_info.sub_project_id,");
		dataSql.append(" ifnull(ys_convention_project_extend.the_value, ys_convention_project.the_value) as project_name,");
		dataSql.append(" normal_expend_budget_order_info.project_amount,");
		dataSql.append(" normal_expend_budget_order_info.formula,");
		dataSql.append(" normal_expend_budget_order_info.remark,");
		dataSql.append(" normal_expend_budget_order_info.project_source");
		dataSql.append(" from normal_expend_budget_order_info");
		dataSql.append(" left join ys_convention_project on ys_convention_project.the_id = normal_expend_budget_order_info.normal_project_id");
		dataSql.append(" left join ys_convention_project_extend on ys_convention_project_extend.the_id = normal_expend_budget_order_info.sub_project_id");
		dataSql.append(" LEFT JOIN ys_department_info on ys_convention_project.department_info_id = ys_department_info.the_id");
		dataSql.append(" where normal_expend_budget_order_info.is_new = 1 and normal_expend_budget_order_info.order_sn = '").append(orderSn).append("' ");
		System.out.println(dataSql);
		dataSql.insert(0, "select * from (").append(") as recordset");// 解决找不到列
		list = getEntityManager().createNativeQuery(dataSql.toString()).getResultList();
		return list;
	}

	public List<Object[]> getBudgetYearList() {
		List<Object[]> list = new ArrayList<Object[]>();
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, -1);
		for (int i = 0; i < 3; i++) {
			list.add(new Object[] { calendar.get(Calendar.YEAR), calendar.get(Calendar.YEAR) + "年" });
			calendar.add(Calendar.YEAR, +1);
		}
		return list;
	}

	/** 递归处理子节点 */
	private void disposeLeaf(List<Object[]> targetList, Map<Object, List<Object>> nexusMap, Map<Object, Object> valueMap, int indentWidth, List<Object> leafList) {
		if (leafList != null && leafList.size() > 0) {
			for (Object leaf : leafList) {
				String indentStr = "";
				for (int i = 0; i < indentWidth; i++) {
					indentStr += "　";
				}
				targetList.add(new Object[] { leaf, indentStr + valueMap.get(leaf) });
				disposeLeaf(targetList, nexusMap, valueMap, indentWidth + 1, nexusMap.get(leaf));
			}
		}
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> getDepartmentInfoList() {
		List<Object[]> list = new ArrayList<Object[]>();
		Map<Object, List<Object>> nexusMap = new HashMap<Object, List<Object>>();// 上下级关系集合
		Map<Object, Object> valueMap = new HashMap<Object, Object>();// 值集合
		String dataSql = "select the_id, the_pid, the_value from ys_department_info where deleted = 0";
		List<Object[]> dataList = getEntityManager().createNativeQuery(dataSql).getResultList();
		if (dataList != null && dataList.size() > 0) {
			for (Object[] data : dataList) {
				valueMap.put(data[0], data[2]);
				List<Object> leafList = nexusMap.get(data[1]);
				if (leafList != null) {
					leafList.add(data[0]);
				} else {
					leafList = new ArrayList<Object>();
					leafList.add(data[0]);
					nexusMap.put(data[1], leafList);
				}
			}
		}
		List<Object> rootList = nexusMap.get(null);
		if (rootList != null && rootList.size() > 0) {
			for (Object root : rootList) {
				list.add(new Object[] { root, valueMap.get(root) });
				disposeLeaf(list, nexusMap, valueMap, 1, nexusMap.get(root));
			}
		}
		return list;
	}

	public JSONArray getTableData() {
		return null;
	}

	public JSONObject getSaveResult() {
		return saveResult;
	}

	public void setSaveArgs(String saveArgs) {
		this.saveArgs = saveArgs;
	}
	
	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public Integer getAmountType() {
		return amountType;
	}

	public void setAmountType(Integer amountType) {
		this.amountType = amountType;
	}
	

	public String getOrderSn() {
		return orderSn;
	}

	public void setOrderSn(String orderSn) {
		this.orderSn = orderSn;
	}
}
