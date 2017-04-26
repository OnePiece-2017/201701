package cn.dmdl.stl.hospitalbudget.budget.session;

import java.net.URLDecoder;
import java.net.URLEncoder;
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
import cn.dmdl.stl.hospitalbudget.util.Helper;
import cn.dmdl.stl.hospitalbudget.util.HospitalConstant;

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
	private String saveData2args;// 保存数据容器参数
	private String submitData2args;// 提交数据容器参数
	private JSONObject saveData2result;// 保存数据容器返回
	private JSONObject submitData2result;// 提交数据容器返回
	Map<String, Map<Object, Object>> projectLevelMap = new HashMap<String, Map<Object, Object>>();// 级别集合（项目、常规）实时更新
	private String gainOriginalData2args;// 获取原始数据参数
	private String gainTamperData2args;// 获取变化数据参数
	private JSONObject gainOriginalData2result;// 获取原始数据返回
	private JSONObject gainTamperData2result;// 获取变化数据返回
	public final int BUILD_DATA_ACTION_SAVE = 1;
	public final String PROJECT_TYPE_GENERIC = "generic";
	public final String PROJECT_TYPE_ROUTINE = "routine";

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

	public Object backtrackTopId(String projectType, Object id) {
		Object result = id;
		if (projectLevelMap != null) {
			Map<Object, Object> levelMap = projectLevelMap.get(projectType);
			if (levelMap != null) {
				while (levelMap.get(result) != null) {
					result = levelMap.get(result);
				}
			}
		}
		return result;
	}

	public void gainOriginalData2action() {
		if (gainOriginalData2result != null) {
			gainOriginalData2result.clear();
		} else {
			gainOriginalData2result = new JSONObject();
		}
		gainOriginalData2result.accumulate("new_generic", pullNewGenericProject(gainOriginalData2args));
		gainOriginalData2result.accumulate("new_routine", pullNewRoutineProject(gainOriginalData2args));
	}

	public void gainTamperData2action() {
		if (gainTamperData2result != null) {
			gainTamperData2result.clear();
		} else {
			gainTamperData2result = new JSONObject();
		}
		gainTamperData2result.accumulate("old_generic", pullOldProject(gainTamperData2args, "generic_project_id", "generic_project", "generic_project_compiler"));
		gainTamperData2result.accumulate("old_routine", pullOldProject(gainTamperData2args, "routine_project_id", "routine_project", "routine_project_compiler"));
		System.out.println(gainTamperData2result);
	}

	@SuppressWarnings("unchecked")
	public JSONArray pullNewGenericProject(String args) {
		JSONArray result = new JSONArray();
		Map<Object, Object> levelMap = new HashMap<Object, Object>();// 创建候选级别集合
		Map<Integer, List<Integer>> nexusMap = new HashMap<Integer, List<Integer>>();// 上下级关系集合
		final Map<Integer, GenericProject> valueMap = new HashMap<Integer, GenericProject>();// 值集合
		StringBuffer dataSql = new StringBuffer("select data from GenericProject data where deleted = 0");
		dataSql.append(" and projectType = 2");// 支出预算
		dataSql.append(" and topLevelProjectId in (select projectId from GenericProjectCompiler where userInfoId = " + sessionToken.getUserInfoId() + ")");
		boolean refuse = true;
		if (args != null && !"".equals(args)) {
			JSONObject argsJson = JSONObject.fromObject(args);
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
				levelMap.put(theId, thePid);// 填充候选级别集合
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
				nodeTmp.accumulate("bottomLevel", valueMap.get(root).isBottomLevel());
				nodeTmp.accumulate("subset", new JSONArray());
				result.add(nodeTmp);
				processGenericProjectLeaf(result.getJSONObject(result.size() - 1), nexusMap, valueMap, nexusMap.get(root));
			}
		}
		projectLevelMap.put(PROJECT_TYPE_GENERIC, levelMap);// 更新候选级别集合
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
	public JSONArray pullNewRoutineProject(String args) {
		JSONArray result = new JSONArray();
		Map<Object, Object> levelMap = new HashMap<Object, Object>();// 创建候选级别集合
		Map<Integer, List<Integer>> nexusMap = new HashMap<Integer, List<Integer>>();// 上下级关系集合
		final Map<Integer, RoutineProject> valueMap = new HashMap<Integer, RoutineProject>();// 值集合
		StringBuffer dataSql = new StringBuffer("select data from RoutineProject data where deleted = 0");
		dataSql.append(" and projectType = 2");// 支出预算
		dataSql.append(" and topLevelProjectId in (select projectId from RoutineProjectCompiler where userInfoId = " + sessionToken.getUserInfoId() + ")");
		boolean refuse = true;
		if (args != null && !"".equals(args)) {
			JSONObject argsJson = JSONObject.fromObject(args);
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
				levelMap.put(theId, thePid);// 填充候选级别集合
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
				nodeTmp.accumulate("bottomLevel", valueMap.get(root).isBottomLevel());
				nodeTmp.accumulate("subset", new JSONArray());
				result.add(nodeTmp);
				processRoutineProjectLeaf(result.getJSONObject(result.size() - 1), nexusMap, valueMap, nexusMap.get(root));
			}
		}
		projectLevelMap.put(PROJECT_TYPE_ROUTINE, levelMap);// 更新候选级别集合
		return result;
	}

	private void processRoutineProjectLeaf(JSONObject node, Map<Integer, List<Integer>> nexusMap, Map<Integer, RoutineProject> valueMap, List<Integer> leafList) {
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
				processRoutineProjectLeaf(leafArr.getJSONObject(leafArr.size() - 1), nexusMap, valueMap, nexusMap.get(leaf));
			}
		}
	}

	public JSONArray pullOldProject(String args, String projectIdField, String projectTable, String projectCompilerTable) {
		JSONArray result = new JSONArray();
		if (args != null && !"".equals(args)) {
			JSONObject argsJson = JSONObject.fromObject(args);
			if (argsJson != null && argsJson.has("budgetYear")) {
				List<Object[]> list = commonTool.selectIntermediate("ys_expand_draft", new String[] { projectIdField, "project_amount", "project_source", "formula_remark", "attachment", "status" }, "status in (0, 1, 2, 3, 4)" + " and draft_type = " + HospitalConstant.TYPE_DRAFT + " and year = " + argsJson.get("budgetYear") + " and insert_user = " + sessionToken.getUserInfoId() + " and " + projectIdField + " in (select the_id from " + projectTable + " where project_type = 2 and top_level_project_id in (select project_id from " + projectCompilerTable + " where user_info_id = " + sessionToken.getUserInfoId() + "))");
				if (list != null && list.size() > 0) {
					for (Object[] objects : list) {
						JSONObject value = new JSONObject();
						value.accumulate("projectId", objects[0]);
						value.accumulate("projectAmount", objects[1]);
						String projectSource = "", formulaRemark = "";
						try {
							projectSource = URLDecoder.decode(objects[2] != null ? objects[2].toString() : "", "UTF-8");// 解码
							formulaRemark = URLDecoder.decode(objects[3] != null ? objects[3].toString() : "", "UTF-8");// 解码
						} catch (Exception e) {
							e.printStackTrace();
						}
						value.accumulate("projectSource", projectSource);
						value.accumulate("formulaRemark", formulaRemark);
						value.accumulate("attachment", objects[4] != null ? objects[4].toString() : "");
						value.accumulate("theStatus", objects[5]);
						result.add(value);
					}
				}
			}
		}
		return result;
	}

	public boolean buildDataBySection(int buildDataAction, JSONArray projectArray, String projectIdField, String projectType) {
		boolean result = true;
		try {
			for (int i = 0; i < projectArray.size(); i++) {
				JSONObject data = projectArray.getJSONObject(i);
				List<Object[]> list = commonTool.selectIntermediate("ys_expand_draft", new String[] { "ys_expand_draft_id" }, "`year` = " + data.get("budgetYear") + " and " + projectIdField + " = " + data.get("projectId") + " and insert_user = " + sessionToken.getUserInfoId());
				String projectSource = URLEncoder.encode(data.get("projectSource") != null ? data.getString("projectSource") : "", "UTF-8");// 编码
				String formulaRemark = URLEncoder.encode(data.get("formulaRemark") != null ? data.getString("formulaRemark") : "", "UTF-8");// 编码
				Object attachment = !JSONNull.getInstance().equals(data.get("attachment")) && !"".equals(data.get("attachment")) ? data.get("attachment") : null;
				int projectId = data.getInt("projectId");// 注意类型，否则无法查找顶级项目id
				Object expandDraftId = null;// 编制表主键id
				if (list.size() > 0) {
					Map<String, Object> columnToValue = new HashMap<String, Object>();
					columnToValue.put("project_amount", data.get("projectAmount"));
					columnToValue.put("project_source", projectSource);
					columnToValue.put("formula_remark", formulaRemark);
					columnToValue.put("attachment", attachment);
					columnToValue.put("top_level_project_id", backtrackTopId(projectType, projectId));
					columnToValue.put("status", buildDataAction != BUILD_DATA_ACTION_SAVE ? 1 : 0);
					expandDraftId = list.get(0);
					commonTool.updateIntermediate("ys_expand_draft", columnToValue, "ys_expand_draft_id = " + expandDraftId);// 理论上只应有一条匹配记录
				} else {
					synchronized (Helper.getInstance()) {
						commonTool.insertIntermediate("ys_expand_draft", new String[] { projectIdField, "year", "project_amount", "project_source", "formula_remark", "attachment", "top_level_project_id", "with_last_year_num", "with_last_year_percent", "insert_time", "insert_user", "status" }, new Object[] { data.get("projectId"), data.get("budgetYear"), data.get("projectAmount"), projectSource, formulaRemark, attachment, backtrackTopId(projectType, projectId), 0.1, 0.2, DateTimeHelper.dateToStr(new Date(), DateTimeHelper.PATTERN_DATE_TIME), sessionToken.getUserInfoId(), buildDataAction != BUILD_DATA_ACTION_SAVE ? 1 : 0 });
						expandDraftId = commonTool.selectIntermediate("ys_expand_draft", new String[] { "max(ys_expand_draft_id)" }, null).get(0);
					}
				}
				if (buildDataAction != BUILD_DATA_ACTION_SAVE) {
					List<Object[]> processUsrList = commonTool.selectIntermediate("process_step_user", new String[] { expandDraftId.toString(), "process_step_info_id", "user_id" }, "type = 0 and process_step_info_id = (select process_step_info_id from process_step_info where step_index = 1 and process_info_id = (select process_info_id from process_info where deleted = 0 and process_type = " + (PROJECT_TYPE_GENERIC.equals(projectType) ? 2 : 1) + " and dept_id = (select department_info_id from " + (PROJECT_TYPE_GENERIC.equals(projectType) ? "generic_project" : "routine_project") + " where the_id = " + projectId + ") and project_process_type = 2 limit 1) limit 1)");
					if (processUsrList != null && processUsrList.size() > 0) {
						commonTool.insertIntermediateBatch("ys_expand_draft_process", new String[] { "ys_expand_draft_id", "process_step_info_id", "user_id" }, processUsrList);
					} else {
						// TODO 未找到流程信息
					}
				}
			}
		} catch (Exception e) {
			result = false;
			e.printStackTrace();
		}
		return result;
	}

	public void saveData2action() {
		if (saveData2result != null) {
			saveData2result.clear();
		} else {
			saveData2result = new JSONObject();
		}
		saveData2result.accumulate("invoke_result", "INVOKE_SUCCESS");
		saveData2result.accumulate("result_message", "保存成功！");
		if (saveData2args != null && !"".equals(saveData2args)) {
			try {
				JSONObject argsJson = JSONObject.fromObject(saveData2args);
				if (argsJson != null && argsJson.has(PROJECT_TYPE_GENERIC) && argsJson.has(PROJECT_TYPE_ROUTINE)) {
					if (!buildDataBySection(1, argsJson.getJSONArray(PROJECT_TYPE_GENERIC), "generic_project_id", PROJECT_TYPE_GENERIC) || !buildDataBySection(1, argsJson.getJSONArray(PROJECT_TYPE_ROUTINE), "routine_project_id", PROJECT_TYPE_ROUTINE)) {
						saveData2result.element("invoke_result", "INVOKE_FAILURE");
						saveData2result.element("result_message", "保存失败（处理项目、常规时发生内部错误）！");
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				saveData2result.element("invoke_result", "INVOKE_FAILURE");
				saveData2result.element("result_message", "保存失败！");
			}
		}
	}

	public void submitData2action() {
		if (submitData2result != null) {
			submitData2result.clear();
		} else {
			submitData2result = new JSONObject();
		}
		submitData2result.accumulate("invoke_result", "INVOKE_SUCCESS");
		submitData2result.accumulate("result_message", "提交成功！");
		if (submitData2args != null && !"".equals(submitData2args)) {
			try {
				JSONObject argsJson = JSONObject.fromObject(submitData2args);
				if (argsJson != null && argsJson.has(PROJECT_TYPE_GENERIC) && argsJson.has(PROJECT_TYPE_ROUTINE)) {
					if (!buildDataBySection(2, argsJson.getJSONArray(PROJECT_TYPE_GENERIC), "generic_project_id", PROJECT_TYPE_GENERIC) || !buildDataBySection(2, argsJson.getJSONArray(PROJECT_TYPE_ROUTINE), "routine_project_id", PROJECT_TYPE_ROUTINE)) {
						submitData2result.element("invoke_result", "INVOKE_FAILURE");
						submitData2result.element("result_message", "提交失败（处理项目、常规时发生内部错误）！");
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				submitData2result.element("invoke_result", "INVOKE_FAILURE");
				submitData2result.element("result_message", "提交失败！");
			}
		}
	}

	public void wire() {
		getInstance();
		if (firstTime) {
			wireBudgetYearList();// 预算年份list
			budgetYear = Calendar.getInstance().get(Calendar.YEAR);
			wireFundsSource();// 资金来源list
			if (fundsSourceList != null && fundsSourceList.size() > 1) {
				fundsSourceId = Integer.valueOf(fundsSourceList.get(1)[0].toString());
			}
			wireDepartmentInfo();// 主管科室list
			departmentInfoId = sessionToken.getDepartmentInfoId();

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

	public void setSaveData2args(String saveData2args) {
		this.saveData2args = saveData2args;
	}

	public void setSubmitData2args(String submitData2args) {
		this.submitData2args = submitData2args;
	}

	public JSONObject getSaveData2result() {
		return saveData2result;
	}

	public JSONObject getSubmitData2result() {
		return submitData2result;
	}

	public JSONObject getGainOriginalData2result() {
		return gainOriginalData2result;
	}

	public JSONObject getGainTamperData2result() {
		return gainTamperData2result;
	}

	public void setGainOriginalData2args(String gainOriginalData2args) {
		this.gainOriginalData2args = gainOriginalData2args;
	}

	public void setGainTamperData2args(String gainTamperData2args) {
		this.gainTamperData2args = gainTamperData2args;
	}

}
