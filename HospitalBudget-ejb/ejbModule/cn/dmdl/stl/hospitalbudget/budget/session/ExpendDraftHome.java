package cn.dmdl.stl.hospitalbudget.budget.session;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.budget.entity.GenericProject;
import cn.dmdl.stl.hospitalbudget.budget.entity.RoutineProject;
import cn.dmdl.stl.hospitalbudget.common.session.CriterionEntityHome;
import cn.dmdl.stl.hospitalbudget.util.CommonToolLocal;
import cn.dmdl.stl.hospitalbudget.util.DateTimeHelper;

@Name("expendDraftHome")
public class ExpendDraftHome extends CriterionEntityHome<Object> {

	@In(create = true)
	private CommonToolLocal commonTool;

	private static final long serialVersionUID = 1L;
	private List<Object[]> budgetYearList;// 预算年份select
	private Integer budgetYear;// 预算年份
	private List<Object[]> departmentInfoList;// 科室select
	private Integer departmentInfoId;// 科室id
	private List<Object[]> fundsSourceList;// 资金来源list
	private Integer fundsSourceId;// 资金来源id
	private JSONObject dataContainer;// 数据容器
	private String renewDataContainerArgs;
	private String saveDataContainerArgs;
	private JSONObject saveDataContainerResult;// 保存数据容器返回结果

	public String draft() {

		setMessage("保存成功！");
		return "drafted";
	}

	public String submit() {

		setMessage("提交成功！");
		return "submitted";
	}

	/** 预算年份 */
	public void wireBudgetYearList() {
		if (budgetYearList != null) {
			budgetYearList.clear();
		} else {
			budgetYearList = new ArrayList<Object[]>();
		}
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, -1);
		for (int i = 0; i < 3; i++) {
			budgetYearList.add(new Object[] { calendar.get(Calendar.YEAR), calendar.get(Calendar.YEAR) + "年" });
			calendar.add(Calendar.YEAR, +1);
		}
	}

	/** 递归处理子节点 */
	public void disposeLeaf(List<Object[]> targetList, Map<Object, List<Object>> nexusMap, Map<Object, Object> valueMap, int indentWidth, List<Object> leafList) {
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
	private void wireDepartmentInfo() {
		if (departmentInfoList != null) {
			departmentInfoList.clear();
		} else {
			departmentInfoList = new ArrayList<Object[]>();
		}
		departmentInfoList.add(new Object[] { "", "请选择" });
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
				departmentInfoList.add(new Object[] { root, valueMap.get(root) });
				disposeLeaf(departmentInfoList, nexusMap, valueMap, 1, nexusMap.get(root));
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void wireFundsSource() {
		if (fundsSourceList != null) {
			fundsSourceList.clear();
		} else {
			fundsSourceList = new ArrayList<Object[]>();
		}
		fundsSourceList.add(new Object[] { "", "请选择" });
		String dataSql = "select the_id, the_value from ys_funds_source where deleted = 0";
		List<Object[]> dataList = getEntityManager().createNativeQuery(dataSql).getResultList();
		if (dataList != null && dataList.size() > 0) {
			for (Object[] data : dataList) {
				fundsSourceList.add(new Object[] { data[0], data[1] });
			}
		}
	}

	public void renewDataContainer() {
		if (dataContainer != null) {
			dataContainer.clear();
		} else {
			dataContainer = new JSONObject();
		}
		dataContainer.accumulate("new_generic", pullNewGenericProject());
		dataContainer.accumulate("new_routine", pullNewRoutineProject());
		dataContainer.accumulate("old_generic", pullOldGenericProject());
	}

	@SuppressWarnings("unchecked")
	public JSONArray pullNewGenericProject() {
		JSONArray result = new JSONArray();
		Map<Integer, List<Integer>> nexusMap = new HashMap<Integer, List<Integer>>();// 上下级关系集合
		final Map<Integer, GenericProject> valueMap = new HashMap<Integer, GenericProject>();// 值集合
		StringBuffer dataSql = new StringBuffer("select data from GenericProject data where deleted = 0");
		dataSql.append(" and projectType = 2");// 支出预算
		dataSql.append(" and topLevelProjectId in (select projectId from GenericProjectCompiler where userInfoId = " + sessionToken.getUserInfoId() + ")");
		boolean refuse = true;
		if (renewDataContainerArgs != null) {
			JSONObject argsJson = JSONObject.fromObject(renewDataContainerArgs);
			if (argsJson != null && argsJson.has("fundsSourceId") && argsJson.has("departmentInfoId")) {
				String fundsSourceId = argsJson.getString("fundsSourceId");
				String departmentInfoId = argsJson.getString("departmentInfoId");
				if (!JSONNull.getInstance().equals(fundsSourceId) && !"".equals(fundsSourceId) && !JSONNull.getInstance().equals(departmentInfoId) && !"".equals(departmentInfoId)) {
					dataSql.append(" and ysFundsSource.theId = " + fundsSourceId);
					dataSql.append(" and ysDepartmentInfo.theId = " + departmentInfoId);
					refuse = false;
				}
			}
		}
		if (refuse) {
			dataSql.append(" and 0 <> 0");
		}
		List<GenericProject> dataList = getEntityManager().createQuery(dataSql.toString()).getResultList();
		if (dataList != null && dataList.size() > 0) {
			for (GenericProject data : dataList) {
				Integer theId = data.getTheId();
				Integer thePid = data.getGenericProject() != null ? data.getGenericProject().getTheId() : null;
				valueMap.put(theId, data);
				List<Integer> leafList = nexusMap.get(thePid);
				if (leafList != null) {
					leafList.add(theId);
				} else {
					leafList = new ArrayList<Integer>();
					leafList.add(theId);
					nexusMap.put(thePid, leafList);
				}
			}
		}

		List<Integer> rootList = nexusMap.get(null);
		if (rootList != null && rootList.size() > 0) {
			for (Integer root : rootList) {
				JSONObject nodeTmp = new JSONObject();
				nodeTmp.accumulate("id", valueMap.get(root).getTheId());
				nodeTmp.accumulate("projectName", valueMap.get(root).getTheValue());
				nodeTmp.accumulate("fundsSource", valueMap.get(valueMap.get(root).getTopLevelProjectId()).getYsFundsSource().getTheValue());
				nodeTmp.accumulate("departmentName", valueMap.get(valueMap.get(root).getTopLevelProjectId()).getYsDepartmentInfo().getTheValue());
				nodeTmp.accumulate("subset", new JSONArray());
				result.add(nodeTmp);
				processGenericProjectLeaf(result.getJSONObject(result.size() - 1), nexusMap, valueMap, nexusMap.get(root));
			}
		}
		return result;
	}

	private void processGenericProjectLeaf(JSONObject node, Map<Integer, List<Integer>> nexusMap, Map<Integer, GenericProject> valueMap, List<Integer> leafList) {
		if (leafList != null && leafList.size() > 0) {
			for (Integer leaf : leafList) {
				JSONArray leafArr = node.getJSONArray("subset");
				JSONObject leafTmp = new JSONObject();
				leafTmp.accumulate("id", valueMap.get(leaf).getTheId());
				leafTmp.accumulate("projectName", valueMap.get(leaf).getTheValue());
				leafTmp.accumulate("fundsSource", valueMap.get(valueMap.get(leaf).getTopLevelProjectId()).getYsFundsSource().getTheValue());
				leafTmp.accumulate("departmentName", valueMap.get(valueMap.get(leaf).getTopLevelProjectId()).getYsDepartmentInfo().getTheValue());
				leafTmp.accumulate("subset", new JSONArray());
				leafArr.add(leafTmp);
				processGenericProjectLeaf(leafArr.getJSONObject(leafArr.size() - 1), nexusMap, valueMap, nexusMap.get(leaf));
			}
		}
	}

	@SuppressWarnings("unchecked")
	public JSONArray pullNewRoutineProject() {
		JSONArray result = new JSONArray();
		Map<Integer, List<Integer>> nexusMap = new HashMap<Integer, List<Integer>>();// 上下级关系集合
		Map<Integer, Integer> levelMap = new HashMap<Integer, Integer>();// 级别集合
		final Map<Integer, RoutineProject> valueMap = new HashMap<Integer, RoutineProject>();// 值集合
		StringBuffer dataSql = new StringBuffer("select data from RoutineProject data where deleted = 0");
		dataSql.append(" and projectType = 2");// 支出预算
		dataSql.append(" and topLevelProjectId in (select projectId from RoutineProjectCompiler where userInfoId = " + sessionToken.getUserInfoId() + ")");
		boolean refuse = true;
		if (renewDataContainerArgs != null) {
			JSONObject argsJson = JSONObject.fromObject(renewDataContainerArgs);
			if (argsJson != null && argsJson.has("fundsSourceId") && argsJson.has("departmentInfoId")) {
				String fundsSourceId = argsJson.getString("fundsSourceId");
				String departmentInfoId = argsJson.getString("departmentInfoId");
				if (!JSONNull.getInstance().equals(fundsSourceId) && !"".equals(fundsSourceId) && !JSONNull.getInstance().equals(departmentInfoId) && !"".equals(departmentInfoId)) {
					dataSql.append(" and ysFundsSource.theId = " + fundsSourceId);
					dataSql.append(" and ysDepartmentInfo.theId = " + departmentInfoId);
					refuse = false;
				}
			}
		}
		if (refuse) {
			dataSql.append(" and 0 <> 0");
		}
		List<RoutineProject> dataList = getEntityManager().createQuery(dataSql.toString()).getResultList();
		if (dataList != null && dataList.size() > 0) {
			for (RoutineProject data : dataList) {
				Integer theId = data.getTheId();
				Integer thePid = data.getRoutineProject() != null ? data.getRoutineProject().getTheId() : null;
				levelMap.put(theId, thePid);
				valueMap.put(theId, data);
				List<Integer> leafList = nexusMap.get(thePid);
				if (leafList != null) {
					leafList.add(theId);
				} else {
					leafList = new ArrayList<Integer>();
					leafList.add(theId);
					nexusMap.put(thePid, leafList);
				}
			}
		}

		List<Integer> rootList = nexusMap.get(null);
		if (rootList != null && rootList.size() > 0) {
			for (Integer root : rootList) {
				JSONObject nodeTmp = new JSONObject();
				nodeTmp.accumulate("id", valueMap.get(root).getTheId());
				nodeTmp.accumulate("projectName", valueMap.get(root).getTheValue());
				Integer topLevelId = valueMap.get(root).getTheId();
				while (levelMap.get(topLevelId) != null) {
					topLevelId = levelMap.get(topLevelId);
				}
				nodeTmp.accumulate("fundsSource", valueMap.get(topLevelId).getYsFundsSource().getTheValue());
				nodeTmp.accumulate("departmentName", valueMap.get(topLevelId).getYsDepartmentInfo().getTheValue());
				nodeTmp.accumulate("subset", new JSONArray());
				result.add(nodeTmp);
				processRoutineProjectLeaf(result.getJSONObject(result.size() - 1), nexusMap, levelMap, valueMap, nexusMap.get(root));
			}
		}
		return result;
	}

	private void processRoutineProjectLeaf(JSONObject node, Map<Integer, List<Integer>> nexusMap, Map<Integer, Integer> levelMap, Map<Integer, RoutineProject> valueMap, List<Integer> leafList) {
		if (leafList != null && leafList.size() > 0) {
			for (Integer leaf : leafList) {
				JSONArray leafArr = node.getJSONArray("subset");
				JSONObject leafTmp = new JSONObject();
				leafTmp.accumulate("id", valueMap.get(leaf).getTheId());
				leafTmp.accumulate("projectName", valueMap.get(leaf).getTheValue());
				Integer topLevelId = valueMap.get(leaf).getTheId();
				while (levelMap.get(topLevelId) != null) {
					topLevelId = levelMap.get(topLevelId);
				}
				leafTmp.accumulate("fundsSource", valueMap.get(topLevelId).getYsFundsSource().getTheValue());
				leafTmp.accumulate("departmentName", valueMap.get(topLevelId).getYsDepartmentInfo().getTheValue());
				leafTmp.accumulate("subset", new JSONArray());
				leafArr.add(leafTmp);
				processRoutineProjectLeaf(leafArr.getJSONObject(leafArr.size() - 1), nexusMap, levelMap, valueMap, nexusMap.get(leaf));
			}
		}
	}

	public JSONArray pullOldGenericProject() {
		JSONArray result = new JSONArray();
		JSONObject value = new JSONObject();
		value.accumulate("projectId", "61");// TODO: 从库中查询数据
		value.accumulate("projectAmount", 321);
		result.add(value);
		return result;
	}

	public void saveDataContainer() {
		if (saveDataContainerResult != null) {
			saveDataContainerResult.clear();
		} else {
			saveDataContainerResult = new JSONObject();
		}
		saveDataContainerResult.accumulate("invoke_result", "INVOKE_SUCCESS");
		saveDataContainerResult.accumulate("result_message", "保存成功！");
		if (saveDataContainerArgs != null) {
			try {
				JSONObject argsJson = JSONObject.fromObject(saveDataContainerArgs);
				if (argsJson != null && argsJson.has("generic") && argsJson.has("routine")) {
					JSONArray genericArr = argsJson.getJSONArray("generic");
					JSONArray routineArr = argsJson.getJSONArray("routine");
					// 处理项目
					for (int i = 0; i < genericArr.size(); i++) {
						System.out.println(genericArr);
						JSONObject data = genericArr.getJSONObject(i);
						List<Object[]> list = commonTool.selectIntermediate("ys_expand_draft", new String[] { "ys_expand_draft_id" }, "`year` = " + data.get("budgetYear") + " and generic_project_id = " + data.get("projectId") + " and insert_user = " + sessionToken.getUserInfoId());
						if (list.size() > 0) {
							Map<String, Object> columnToValue = new HashMap<String, Object>();
							columnToValue.put("project_amount", data.get("projectAmount"));
							commonTool.updateIntermediate("ys_expand_draft", columnToValue, "ys_expand_draft_id = " + list.get(0));// 理论上只应有一条匹配记录
						} else {
							commonTool.insertIntermediate("ys_expand_draft", new String[] { "year", "project_source", "top_level_project_id", "generic_project_id", "project_amount", "formula_remark", "with_last_year_num", "with_last_year_percent", "attachment", "insert_time", "insert_user", "status" }, new Object[] { data.get("budgetYear"), "project_source", 1024, data.get("projectId"), data.get("projectAmount"), "formula_remark", 0.1, 0.2, "093f65e080a295f8076b1c5722a46aa2", DateTimeHelper.dateToStr(new Date(), DateTimeHelper.PATTERN_DATE_TIME), sessionToken.getUserInfoId(), 0 });
						}
					}
					// 处理常规
					for (int i = 0; i < routineArr.size(); i++) {
						System.out.println(routineArr);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				saveDataContainerResult.element("invoke_result", "INVOKE_FAILURE");
				saveDataContainerResult.element("result_message", "保存失败！");
			}
		}
	}

	public void wire() {
		getInstance();
		if (firstTime) {

			wireBudgetYearList();// 预算年份list
			budgetYear = Calendar.getInstance().get(Calendar.YEAR);
			wireDepartmentInfo();// 主管科室list
			departmentInfoId = null;
			wireFundsSource();// 资金来源list
			fundsSourceId = null;

			firstTime = false;
		}
	}

	public List<Object[]> getBudgetYearList() {
		return budgetYearList;
	}

	public Integer getBudgetYear() {
		return budgetYear;
	}

	public void setBudgetYear(Integer budgetYear) {
		this.budgetYear = budgetYear;
	}

	public List<Object[]> getDepartmentInfoList() {
		return departmentInfoList;
	}

	public Integer getDepartmentInfoId() {
		return departmentInfoId;
	}

	public void setDepartmentInfoId(Integer departmentInfoId) {
		this.departmentInfoId = departmentInfoId;
	}

	public List<Object[]> getFundsSourceList() {
		return fundsSourceList;
	}

	public Integer getFundsSourceId() {
		return fundsSourceId;
	}

	public void setFundsSourceId(Integer fundsSourceId) {
		this.fundsSourceId = fundsSourceId;
	}

	public JSONObject getDataContainer() {
		return dataContainer;
	}

	public String getRenewDataContainerArgs() {
		return renewDataContainerArgs;
	}

	public void setRenewDataContainerArgs(String renewDataContainerArgs) {
		this.renewDataContainerArgs = renewDataContainerArgs;
	}

	public String getSaveDataContainerArgs() {
		return saveDataContainerArgs;
	}

	public void setSaveDataContainerArgs(String saveDataContainerArgs) {
		this.saveDataContainerArgs = saveDataContainerArgs;
	}

	public JSONObject getSaveDataContainerResult() {
		return saveDataContainerResult;
	}

}
