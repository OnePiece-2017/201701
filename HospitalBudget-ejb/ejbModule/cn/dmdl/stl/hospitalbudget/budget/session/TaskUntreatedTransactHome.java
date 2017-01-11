package cn.dmdl.stl.hospitalbudget.budget.session;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.budget.entity.NormalBudgetCollectionInfo;
import cn.dmdl.stl.hospitalbudget.budget.entity.ProcessStepInfo;
import cn.dmdl.stl.hospitalbudget.budget.entity.TaskOrder;
import cn.dmdl.stl.hospitalbudget.budget.entity.TaskUser;
import cn.dmdl.stl.hospitalbudget.common.session.CriterionEntityHome;

@Name("taskUntreatedTransactHome")
public class TaskUntreatedTransactHome extends CriterionEntityHome<Object> {

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
				} else {// 通过
					oldTaskOrder.setOrderStatus(1);
					newTaskOrder.setOrderStatus(0);
					ProcessStepInfo currentProcessStepInfo = getEntityManager().find(ProcessStepInfo.class, oldTaskOrder.getProcessStepInfoId());
					List<ProcessStepInfo> nextProcessStepInfoList = getEntityManager().createQuery("select processStepInfo from ProcessStepInfo processStepInfo where processStepInfo.processInfo.processInfoId = " + currentProcessStepInfo.getProcessInfo().getProcessInfoId() + " and processStepInfo.stepIndex = " + (currentProcessStepInfo.getStepIndex() + 1)).getResultList();
					if (nextProcessStepInfoList != null && nextProcessStepInfoList.size() > 0) {// 有下一步
						ProcessStepInfo nextProcessStepInfo = nextProcessStepInfoList.get(0);
						newTaskOrder.setProcessStepInfoId(nextProcessStepInfo.getProcessStepInfoId());
						getEntityManager().persist(newTaskOrder);// 提前缓存对象
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
						List<Object[]> summaryList = getEntityManager().createNativeQuery("select `year`, sum(project_amount) as budget_amount from normal_budget_order_info where is_new = 1 and sub_project_id is null and binary order_sn = '" + oldTaskOrder.getOrderSn() + "'").getResultList();
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
