package cn.dmdl.stl.hospitalbudget.budget.session;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.admin.entity.UserInfo;
import cn.dmdl.stl.hospitalbudget.budget.entity.NormalBudgetOrderInfo;
import cn.dmdl.stl.hospitalbudget.budget.entity.TaskOrder;
import cn.dmdl.stl.hospitalbudget.budget.entity.TaskUser;
import cn.dmdl.stl.hospitalbudget.common.session.CriterionEntityHome;

@Name("taskUntreatedReviseHome")
public class TaskUntreatedReviseHome extends CriterionEntityHome<Object> {

	private static final long serialVersionUID = 1L;
	private Integer taskOrderId;
	private String saveArgs;
	private JSONObject saveResult;

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
			try {
				joinTransaction();
				JSONObject dataInfo = JSONObject.fromObject(saveArgs);
				JSONObject budgetProject = dataInfo.getJSONObject("budgetProject");
				boolean taskOrderFlag = false;
				TaskOrder oldTaskOrder = getEntityManager().find(TaskOrder.class, taskOrderId);
				oldTaskOrder.setOrderStatus(4);
				getEntityManager().merge(oldTaskOrder);

				TaskOrder newTaskOrder = new TaskOrder();
				newTaskOrder.setOrderSn(oldTaskOrder.getOrderSn());
				newTaskOrder.setTaskName(oldTaskOrder.getTaskName());
				newTaskOrder.setDeptId(oldTaskOrder.getDeptId());
				newTaskOrder.setEditUserId(oldTaskOrder.getEditUserId());
				newTaskOrder.setTaskType(oldTaskOrder.getTaskType());
				newTaskOrder.setInsertTime(new Date());
				newTaskOrder.setInsertUser(sessionToken.getUserInfoId());
				newTaskOrder.setOrderStatus(0);
				String budgetYear = "";
				List<NormalBudgetOrderInfo> oldNormalBudgetOrderInfoList = getEntityManager().createQuery("select normalBudgetOrderInfo from NormalBudgetOrderInfo normalBudgetOrderInfo where normalBudgetOrderInfo.orderSn = '" + newTaskOrder.getOrderSn() + "'").getResultList();
				if (oldNormalBudgetOrderInfoList != null && oldNormalBudgetOrderInfoList.size() > 0) {
					budgetYear = oldNormalBudgetOrderInfoList.get(0).getYear();
					for (NormalBudgetOrderInfo oldNormalBudgetOrderInfo : oldNormalBudgetOrderInfoList) {
						oldNormalBudgetOrderInfo.setIsNew(false);
						getEntityManager().merge(oldNormalBudgetOrderInfo);
					}
				}
				for (Object key : budgetProject.keySet()) {
					if (!taskOrderFlag) {
						UserInfo userInfo = getEntityManager().find(UserInfo.class, sessionToken.getUserInfoId());
						String processInfoSql = "select process_info_id from process_info where deleted = 0 and process_type = 1 and project_process_type = 1 and dept_id = " + userInfo.getYsDepartmentInfo().getTheId();
						List<Object> processInfoList = getEntityManager().createNativeQuery(processInfoSql).getResultList();
						if (processInfoList != null && processInfoList.size() > 0) {
							String processStepInfoSql = "select process_step_info_id from process_step_info where step_index = 1 and process_info_id = " + processInfoList.get(0).toString();// 忽略更多该部门的流程信息
							List<Object> processStepInfoList = getEntityManager().createNativeQuery(processStepInfoSql).getResultList();
							if (processStepInfoList != null && processStepInfoList.size() > 0) {
								newTaskOrder.setProcessStepInfoId(Integer.parseInt(processStepInfoList.get(0).toString()));
								getEntityManager().persist(newTaskOrder);// 提前缓存对象
								String processStepUserSql = "select IFNULL(GROUP_CONCAT(user_id), '') as result from process_step_user where type = 0 and process_step_info_id = " + processStepInfoList.get(0).toString();// 只取第一步处理人
								List<Object> processStepUserList = getEntityManager().createNativeQuery(processStepUserSql).getResultList();
								if (processStepInfoList != null && processStepInfoList.size() > 0) {
									String[] processStepUserArr = processStepUserList.get(0).toString().split(",");
									for (String processStepUser : processStepUserArr) {
										TaskUser taskUser = new TaskUser();
										taskUser.setTaskOrderId(newTaskOrder.getTaskOrderId());
										taskUser.setUserId(Integer.parseInt(processStepUser));
										taskUser.setHandleFlg(false);
										getEntityManager().persist(taskUser);
									}
								} else {
									saveResult.element("invoke_result", "INVOKE_FAILURE");
									saveResult.element("message", "操作失败！请先完善科室[" + userInfo.getYsDepartmentInfo().getTheValue() + "]的步骤配置的用户信息！");
									return;
								}
							} else {
								saveResult.element("invoke_result", "INVOKE_FAILURE");
								saveResult.element("message", "操作失败！请先完善科室[" + userInfo.getYsDepartmentInfo().getTheValue() + "]的步骤配置！");
								return;
							}
						} else {
							saveResult.element("invoke_result", "INVOKE_FAILURE");
							saveResult.element("message", "操作失败！请先完善科室[" + userInfo.getYsDepartmentInfo().getTheValue() + "]的流程信息！");
							return;
						}
						taskOrderFlag = true;
					}
					JSONObject root = budgetProject.getJSONObject(key.toString());
					NormalBudgetOrderInfo nboRoot = new NormalBudgetOrderInfo();
					nboRoot.setOrderSn(newTaskOrder.getOrderSn());
					nboRoot.setYear(String.valueOf(budgetYear));
					nboRoot.setNormalProjectId(root.getInt("projectId"));
					nboRoot.setProjectSource(0);
					nboRoot.setProjectAmount(JSONNull.getInstance().equals(root.getString("projectAmount")) ? 0 : Double.parseDouble(root.getString("projectAmount")));
					nboRoot.setFormula(JSONNull.getInstance().equals(root.getString("formula")) ? "" : root.getString("formula"));
					nboRoot.setRemark(JSONNull.getInstance().equals(root.getString("remark")) ? "" : root.getString("remark"));
					nboRoot.setWithLastYearNum(null);
					nboRoot.setWithLastYearPercent(null);
					nboRoot.setIsNew(true);
					nboRoot.setInsertTime(new Date());
					nboRoot.setInsertUser(sessionToken.getUserInfoId());
					getEntityManager().persist(nboRoot);
					JSONArray leafArr = root.getJSONArray("subProject");
					if (leafArr != null && leafArr.size() > 0) {
						double rootProjectAmount = 0;
						for (int i = 0; i < leafArr.size(); i++) {
							JSONObject leaf = leafArr.getJSONObject(i);
							NormalBudgetOrderInfo nboLeaf = new NormalBudgetOrderInfo();
							nboLeaf.setOrderSn(newTaskOrder.getOrderSn());
							nboLeaf.setYear(String.valueOf(budgetYear));
							nboLeaf.setNormalProjectId(root.getInt("projectId"));
							nboLeaf.setSubProjectId(leaf.getInt("projectId"));
							nboLeaf.setProjectSource(0);
							double projectAmount = JSONNull.getInstance().equals(leaf.getString("projectAmount")) ? 0 : Double.parseDouble(leaf.getString("projectAmount"));
							nboLeaf.setProjectAmount(projectAmount);
							rootProjectAmount += projectAmount;
							nboLeaf.setFormula(JSONNull.getInstance().equals(leaf.getString("formula")) ? "" : leaf.getString("formula"));
							nboLeaf.setRemark(JSONNull.getInstance().equals(leaf.getString("remark")) ? "" : leaf.getString("remark"));
							nboLeaf.setWithLastYearNum(null);
							nboLeaf.setWithLastYearPercent(null);
							nboLeaf.setIsNew(true);
							nboLeaf.setInsertTime(new Date());
							nboLeaf.setInsertUser(sessionToken.getUserInfoId());
							getEntityManager().persist(nboLeaf);
						}
						nboRoot.setProjectAmount(Double.parseDouble(new DecimalFormat("#.##").format(rootProjectAmount)));
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
		TaskOrder taskOrder = getEntityManager().find(TaskOrder.class, taskOrderId);
		String dataSql = "select `year`, normal_project_id, sub_project_id, project_source, project_amount, formula, remark from normal_budget_order_info where is_new = 1 and binary order_sn = '" + taskOrder.getOrderSn() + "'";
		List<Object[]> dataList = getEntityManager().createNativeQuery(dataSql).getResultList();
		if (dataList != null && dataList.size() > 0) {
			Map<Object, Object> normalProjectNameMap = new HashMap<Object, Object>(), subProjectNameMap = new HashMap<Object, Object>();
			Map<Object, Object> multilevelMap = new HashMap<Object, Object>();
			String normalProjectIds = null, subProjectIds = null;
			Set<Object> normalProjectIdSet = new HashSet<Object>(), subProjectIdSet = new HashSet<Object>();
			for (Object[] data : dataList) {
				normalProjectIdSet.add(data[1]);
				subProjectIdSet.add(data[2]);
			}
			subProjectIdSet.remove(null);
			normalProjectIds = normalProjectIdSet.toString().substring(1, normalProjectIdSet.toString().length() - 1);
			subProjectIds = subProjectIdSet.toString().substring(1, subProjectIdSet.toString().length() - 1);
			if (normalProjectIds != null && !"".equals(normalProjectIds)) {
				List<Object[]> objList = getEntityManager().createNativeQuery("select the_id, the_value, multilevel from ys_convention_project where the_id in (" + normalProjectIds + ")").getResultList();
				if (objList != null && objList.size() > 0) {
					for (Object[] obj : objList) {
						normalProjectNameMap.put(obj[0], obj[1]);
						multilevelMap.put(obj[0], obj[2]);
					}
				}
			}
			if (subProjectIds != null && !"".equals(subProjectIds)) {
				List<Object[]> objList = getEntityManager().createNativeQuery("select the_id, the_value from ys_convention_project_extend where the_id in (" + subProjectIds + ")").getResultList();
				if (objList != null && objList.size() > 0) {
					for (Object[] obj : objList) {
						subProjectNameMap.put(obj[0], obj[1]);
					}
				}
			}
			for (Object[] data : dataList) {
				JSONObject row = new JSONObject();
				boolean isRoot = null == data[2];
				row.accumulate("isRoot", isRoot);
				row.accumulate("multilevel", multilevelMap.get(data[1]));
				row.accumulate("projectId", data[1]);
				row.accumulate("subId", data[2]);
				row.accumulate("projectName", isRoot ? normalProjectNameMap.get(data[1]) : subProjectNameMap.get(data[2]));
				row.accumulate("totalAmount", data[4]);
				row.accumulate("formula", data[5]);
				row.accumulate("remark", data[6]);
				resultSet.add(row);
			}
		}
		return resultSet;
	}

	public JSONArray getTableData() {
		return null;
	}

	public Integer getTaskOrderId() {
		return taskOrderId;
	}

	public void setTaskOrderId(Integer taskOrderId) {
		this.taskOrderId = taskOrderId;
	}

	public JSONObject getSaveResult() {
		return saveResult;
	}

	public void setSaveArgs(String saveArgs) {
		this.saveArgs = saveArgs;
	}
}
