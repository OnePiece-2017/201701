package cn.dmdl.stl.hospitalbudget.budget.session;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.admin.entity.UserInfo;
import cn.dmdl.stl.hospitalbudget.budget.entity.NormalExpendBudgetOrderInfo;
import cn.dmdl.stl.hospitalbudget.budget.entity.TaskOrder;
import cn.dmdl.stl.hospitalbudget.budget.entity.TaskUser;
import cn.dmdl.stl.hospitalbudget.common.session.CriterionEntityHome;

@Name("reviseExpendHome")
public class ReviseExpendHome extends CriterionEntityHome<Object> {

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
				List<NormalExpendBudgetOrderInfo> oldNormalExpendBudgetOrderInfoList = getEntityManager().createQuery("select normalExpendBudgetOrderInfo from NormalExpendBudgetOrderInfo normalExpendBudgetOrderInfo where normalExpendBudgetOrderInfo.taskOrderId = " + oldTaskOrder.getTaskOrderId()).getResultList();
				if (oldNormalExpendBudgetOrderInfoList != null && oldNormalExpendBudgetOrderInfoList.size() > 0) {
					budgetYear = oldNormalExpendBudgetOrderInfoList.get(0).getYear();
					for (NormalExpendBudgetOrderInfo oldNormalExpendBudgetOrderInfo : oldNormalExpendBudgetOrderInfoList) {
						oldNormalExpendBudgetOrderInfo.setIsNew(false);
						getEntityManager().merge(oldNormalExpendBudgetOrderInfo);
					}
				}
				for (Object key : budgetProject.keySet()) {
					if (!taskOrderFlag) {
						UserInfo userInfo = getEntityManager().find(UserInfo.class, sessionToken.getUserInfoId());
						List<Object> processInfoList = getEntityManager().createNativeQuery("select process_info_id from process_info where deleted = 0 and process_type = 1 and project_process_type = 2 and dept_id = " + userInfo.getYsDepartmentInfo().getTheId()).getResultList();// 获取当前登录人所属部门的常规项目流程的常规支出预算流程
						if (processInfoList != null && processInfoList.size() > 0) {
							List<Object> processStepInfoList = getEntityManager().createNativeQuery("select process_step_info_id from process_step_info where step_index = 1 and process_info_id = " + processInfoList.get(0).toString()).getResultList();// 获取流程配置中的第一步
							if (processStepInfoList != null && processStepInfoList.size() > 0) {
								int processStepInfoId = Integer.parseInt(processStepInfoList.get(0).toString());
								newTaskOrder.setProcessStepInfoId(processStepInfoId);
								getEntityManager().persist(newTaskOrder);// 提前缓存对象
								List<Object> processStepUserList = getEntityManager().createNativeQuery("select user_id from process_step_user where type = 0 and process_step_info_id = " + processStepInfoId).getResultList();// 获取流程配置中的第一步的处理人
								if (processStepInfoList != null && processStepInfoList.size() > 0) {// 步骤配置中有用户
									for (Object processStepUser : processStepUserList) {
										TaskUser taskUser = new TaskUser();
										taskUser.setTaskOrderId(newTaskOrder.getTaskOrderId());
										taskUser.setUserId((Integer) processStepUser);
										taskUser.setHandleFlg(false);// 未处理
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
					NormalExpendBudgetOrderInfo nboRoot = new NormalExpendBudgetOrderInfo();
					nboRoot.setTaskOrderId(newTaskOrder.getTaskOrderId());
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
							NormalExpendBudgetOrderInfo nboLeaf = new NormalExpendBudgetOrderInfo();
							nboLeaf.setTaskOrderId(newTaskOrder.getTaskOrderId());
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
		StringBuffer dataSql = new StringBuffer();
		dataSql.append(" select");
		dataSql.append(" normal_expend_budget_order_info.task_order_id,");
		dataSql.append(" normal_expend_budget_order_info.`year`,");
		dataSql.append(" ys_convention_project.multilevel,");
		dataSql.append(" normal_expend_budget_order_info.normal_project_id,");
		dataSql.append(" normal_expend_budget_order_info.sub_project_id,");
		dataSql.append(" ys_convention_project.the_value as main_project_name,");
		dataSql.append(" ys_convention_project_extend.the_value as sub_project_name,");
		dataSql.append(" normal_expend_budget_order_info.project_amount,");
		dataSql.append(" normal_expend_budget_order_info.formula,");
		dataSql.append(" normal_expend_budget_order_info.remark");
		dataSql.append(" from normal_expend_budget_order_info");
		dataSql.append(" left join ys_convention_project on ys_convention_project.the_id = normal_expend_budget_order_info.normal_project_id");
		dataSql.append(" left join ys_convention_project_extend on ys_convention_project_extend.the_id = normal_expend_budget_order_info.sub_project_id");
		dataSql.append(" where normal_expend_budget_order_info.is_new = 1 and normal_expend_budget_order_info.task_order_id = ").append(taskOrder.getTaskOrderId());
		dataSql.insert(0, "select * from (").append(") as recordset");// 解决找不到列
		List<Object[]> dataList = getEntityManager().createNativeQuery(dataSql.toString()).getResultList();
		System.out.println(dataSql.toString());
		if (dataList != null && dataList.size() > 0) {
			for (Object[] data : dataList) {
				JSONObject row = new JSONObject();
				boolean isRoot = null == data[4];
				row.accumulate("isRoot", isRoot);
				row.accumulate("multilevel", data[2]);
				row.accumulate("projectId", data[3]);
				row.accumulate("subId", data[4]);
				row.accumulate("projectName", isRoot ? data[5] : data[6]);
				row.accumulate("totalAmount", data[7]);
				row.accumulate("formula", data[8]);
				row.accumulate("remark", data[9]);
				resultSet.add(row);
			}
		}
		System.out.println(resultSet);
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
