package cn.dmdl.stl.hospitalbudget.budget.session;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.budget.entity.NormalBudgetCollectionInfo;
import cn.dmdl.stl.hospitalbudget.budget.entity.NormalBudgetOrderInfo;
import cn.dmdl.stl.hospitalbudget.budget.entity.ProcessStepInfo;
import cn.dmdl.stl.hospitalbudget.budget.entity.TaskOrder;
import cn.dmdl.stl.hospitalbudget.budget.entity.TaskUser;
import cn.dmdl.stl.hospitalbudget.common.session.CriterionEntityHome;

@Name("transactRevenueHome")
public class TransactRevenueHome extends CriterionEntityHome<Object> {

	private static final long serialVersionUID = 1L;
	private Integer taskOrderId;
	private String saveArgs;
	private JSONObject saveResult;

	@SuppressWarnings("unchecked")
	private void updateProjectTaskOrderId(int oldTaskOrderId, int newTaskOrderId) {
		List<NormalBudgetOrderInfo> oldNormalBudgetOrderInfoList = getEntityManager().createQuery("select normalBudgetOrderInfo from NormalBudgetOrderInfo normalBudgetOrderInfo where normalBudgetOrderInfo.taskOrderId = " + oldTaskOrderId).getResultList();
		if (oldNormalBudgetOrderInfoList != null && oldNormalBudgetOrderInfoList.size() > 0) {
			joinTransaction();
			for (NormalBudgetOrderInfo normalBudgetOrderInfo : oldNormalBudgetOrderInfoList) {
				normalBudgetOrderInfo.setTaskOrderId(newTaskOrderId);// 跟踪订单信息
				getEntityManager().merge(normalBudgetOrderInfo);
			}
			getEntityManager().flush();
		}
	}

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
				int transactResult = dataInfo.getInt("transactResult");
				TaskOrder oldTaskOrder = getEntityManager().find(TaskOrder.class, taskOrderId);
				TaskOrder newTaskOrder = new TaskOrder();
				newTaskOrder.setOrderSn(oldTaskOrder.getOrderSn());
				newTaskOrder.setTaskName(oldTaskOrder.getTaskName());
				newTaskOrder.setDeptId(oldTaskOrder.getDeptId());
				newTaskOrder.setEditUserId(oldTaskOrder.getEditUserId());
				newTaskOrder.setTaskType(oldTaskOrder.getTaskType());
				newTaskOrder.setInsertTime(new Date());
				newTaskOrder.setInsertUser(sessionToken.getUserInfoId());
				if (transactResult != 1) {// 不通过
					oldTaskOrder.setOrderStatus(2);
					newTaskOrder.setOrderStatus(3);
					getEntityManager().persist(newTaskOrder);// 提前缓存对象
					updateProjectTaskOrderId(oldTaskOrder.getTaskOrderId(), newTaskOrder.getTaskOrderId());
				} else {// 通过
					oldTaskOrder.setOrderStatus(1);
					newTaskOrder.setOrderStatus(0);
					ProcessStepInfo currentProcessStepInfo = getEntityManager().find(ProcessStepInfo.class, oldTaskOrder.getProcessStepInfoId());
					List<ProcessStepInfo> nextProcessStepInfoList = getEntityManager().createQuery("select processStepInfo from ProcessStepInfo processStepInfo where processStepInfo.processInfo.processInfoId = " + currentProcessStepInfo.getProcessInfo().getProcessInfoId() + " and processStepInfo.stepIndex = " + (currentProcessStepInfo.getStepIndex() + 1)).getResultList();
					if (nextProcessStepInfoList != null && nextProcessStepInfoList.size() > 0) {// 有下一步
						ProcessStepInfo nextProcessStepInfo = nextProcessStepInfoList.get(0);
						newTaskOrder.setProcessStepInfoId(nextProcessStepInfo.getProcessStepInfoId());
						getEntityManager().persist(newTaskOrder);// 提前缓存对象
						updateProjectTaskOrderId(oldTaskOrder.getTaskOrderId(), newTaskOrder.getTaskOrderId());
						String processStepUserSql = "select IFNULL(GROUP_CONCAT(user_id), '') as result from process_step_user where type = 0 and process_step_info_id = " + nextProcessStepInfo.getProcessStepInfoId();
						List<Object> processStepUserList = getEntityManager().createNativeQuery(processStepUserSql).getResultList();
						String[] processStepUserArr = processStepUserList.get(0).toString().split(",");
						for (String processStepUser : processStepUserArr) {
							TaskUser taskUser = new TaskUser();
							taskUser.setTaskOrderId(newTaskOrder.getTaskOrderId());
							taskUser.setUserId(Integer.parseInt(processStepUser));
							taskUser.setHandleFlg(false);
							getEntityManager().persist(taskUser);
						}
					} else {
						oldTaskOrder.setOrderStatus(9);
						NormalBudgetCollectionInfo normalBudgetCollectionInfo = new NormalBudgetCollectionInfo();
						normalBudgetCollectionInfo.setOrderSn(oldTaskOrder.getOrderSn());
						normalBudgetCollectionInfo.setDeptId(oldTaskOrder.getDeptId());
						List<Object[]> summaryList = getEntityManager().createNativeQuery("select `year`, sum(project_amount) as budget_amount from normal_budget_order_info where is_new = 1 and sub_project_id is null and task_order_id = " + oldTaskOrder.getTaskOrderId()).getResultList();
						if (summaryList != null && summaryList.size() > 0) {
							Object[] summary = summaryList.get(0);
							normalBudgetCollectionInfo.setYear(summary[0].toString());
							normalBudgetCollectionInfo.setBudgetAmount(Double.parseDouble(new DecimalFormat("#.##").format(Double.parseDouble(summary[1].toString()))));
						}
						if (oldTaskOrder.getTaskType() == 1) {
							normalBudgetCollectionInfo.setAmountType(1);
						} else if (oldTaskOrder.getTaskType() == 2) {
							normalBudgetCollectionInfo.setAmountType(2);
						}
						normalBudgetCollectionInfo.setSubmitFlag(false);
						normalBudgetCollectionInfo.setInsertTime(new Date());
						normalBudgetCollectionInfo.setInsertUser(sessionToken.getUserInfoId());
						getEntityManager().persist(normalBudgetCollectionInfo);
					}
				}
				List<TaskUser> taskUserList = getEntityManager().createQuery("select taskUser from TaskUser taskUser where taskOrderId = " + oldTaskOrder.getTaskOrderId() + " and userId = " + sessionToken.getUserInfoId()).getResultList();
				if (taskUserList != null && taskUserList.size() > 0) {
					TaskUser taskUser = taskUserList.get(0);
					taskUser.setHandleFlg(true);
					getEntityManager().merge(taskUser);
				}
				oldTaskOrder.setAuditOpinion(dataInfo.getString("transactOpinion"));
				getEntityManager().merge(oldTaskOrder);
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
		dataSql.append(" normal_budget_order_info.task_order_id,");
		dataSql.append(" normal_budget_order_info.`year`,");
		dataSql.append(" ys_convention_project.multilevel,");
		dataSql.append(" normal_budget_order_info.normal_project_id,");
		dataSql.append(" normal_budget_order_info.sub_project_id,");
		dataSql.append(" ys_convention_project.the_value as main_project_name,");
		dataSql.append(" ys_convention_project_extend.the_value as sub_project_name,");
		dataSql.append(" normal_budget_order_info.project_amount,");
		dataSql.append(" normal_budget_order_info.formula,");
		dataSql.append(" normal_budget_order_info.remark");
		dataSql.append(" from normal_budget_order_info");
		dataSql.append(" left join ys_convention_project on ys_convention_project.the_id = normal_budget_order_info.normal_project_id");
		dataSql.append(" left join ys_convention_project_extend on ys_convention_project_extend.the_id = normal_budget_order_info.sub_project_id");
		dataSql.append(" where normal_budget_order_info.is_new = 1 and normal_budget_order_info.task_order_id = ").append(taskOrder.getTaskOrderId());
		dataSql.insert(0, "select * from (").append(") as recordset");// 解决找不到列
		List<Object[]> dataList = getEntityManager().createNativeQuery(dataSql.toString()).getResultList();
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
