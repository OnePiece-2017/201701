package cn.dmdl.stl.hospitalbudget.budget.session;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.budget.entity.GenericProject;
import cn.dmdl.stl.hospitalbudget.common.session.CriterionEntityHome;

@Name("expendDraftHome")
public class ExpendDraftHome extends CriterionEntityHome<Object> {

	private static final long serialVersionUID = 1L;
	private List<Object[]> budgetYearList;// 预算年份select
	private Integer budgetYear;// 预算年份
	private List<Object[]> departmentInfoList;// 科室select
	private Integer departmentInfoId;// 科室id
	private List<Object[]> fundsSourceList;// 资金来源list
	private Integer fundsSourceId;// 资金来源id
	private JSONObject dataContainer;// 数据容器
	private String renewDataContainerArgs;

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
		System.out.println("renewDataContainerArgs:" + renewDataContainerArgs);

		dataContainer.accumulate("generic", pullProjectArray());
		dataContainer.accumulate("routine", new JSONArray());

	}

	/** 拉取项目设置 */
	@SuppressWarnings("unchecked")
	public JSONArray pullProjectArray() {
		JSONArray result = new JSONArray();
		Map<Integer, List<Integer>> nexusMap = new HashMap<Integer, List<Integer>>();// 上下级关系集合
		Map<Integer, Integer> levelMap = new HashMap<Integer, Integer>();// 级别集合
		final Map<Integer, GenericProject> valueMap = new HashMap<Integer, GenericProject>();// 值集合
		String dataSql = "select genericProject from GenericProject genericProject where genericProject.deleted = 0";
		List<GenericProject> genericProjectList = getEntityManager().createQuery(dataSql).getResultList();
		if (genericProjectList != null && genericProjectList.size() > 0) {
			for (GenericProject genericProject : genericProjectList) {
				Integer theId = genericProject.getTheId();
				Integer thePid = genericProject.getGenericProject() != null ? genericProject.getGenericProject().getTheId() : null;
				levelMap.put(theId, thePid);
				valueMap.put(theId, genericProject);
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
				disposeLeaf(result.getJSONObject(result.size() - 1), nexusMap, levelMap, valueMap, nexusMap.get(root));
			}
		}
		return result;
	}

	/** 递归处理子节点 */
	private void disposeLeaf(JSONObject node, Map<Integer, List<Integer>> nexusMap, Map<Integer, Integer> levelMap, Map<Integer, GenericProject> valueMap, List<Integer> leafList) {
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
				disposeLeaf(leafArr.getJSONObject(leafArr.size() - 1), nexusMap, levelMap, valueMap, nexusMap.get(leaf));
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

}
