package cn.dmdl.stl.hospitalbudget.budget.session;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.admin.entity.UserInfo;
import cn.dmdl.stl.hospitalbudget.budget.entity.NormalBudgetOrderInfo;
import cn.dmdl.stl.hospitalbudget.budget.entity.TaskOrder;
import cn.dmdl.stl.hospitalbudget.budget.entity.TaskUser;
import cn.dmdl.stl.hospitalbudget.common.session.CriterionEntityHome;
import cn.dmdl.stl.hospitalbudget.util.Assit;

@Name("annualRevenueBudgetCompile")
public class AnnualRevenueBudgetCompile extends CriterionEntityHome<Object> {
	private static final long serialVersionUID = 1L;
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
				int budgetYear = dataInfo.getInt("budgetYear");
				JSONObject budgetProject = dataInfo.getJSONObject("budgetProject");
				boolean taskOrderFlag = false;
				TaskOrder taskOrder = null;
				for (Object key : budgetProject.keySet()) {
					if (!taskOrderFlag) {
						UserInfo userInfo = getEntityManager().find(UserInfo.class, sessionToken.getUserInfoId());
						taskOrder = new TaskOrder();
						taskOrder.setTaskName(userInfo.getYsDepartmentInfo().getTheValue() + "科室　" + budgetYear + "年　常规预算");
						taskOrder.setDeptId(userInfo.getYsDepartmentInfo().getTheId());
						taskOrder.setEditUserId(sessionToken.getUserInfoId());
						taskOrder.setTaskType(1);
						taskOrder.setInsertTime(new Date());
						taskOrder.setInsertUser(sessionToken.getUserInfoId());
						taskOrder.setOrderStatus(0);
						taskOrder.setAuditOpinion(null);
						String processInfoSql = "select process_info_id from process_info where deleted = 0 and process_type = 1 and project_process_type = 1 and dept_id = " + userInfo.getYsDepartmentInfo().getTheId();
						List<Object> processInfoList = getEntityManager().createNativeQuery(processInfoSql).getResultList();
						if (processInfoList != null && processInfoList.size() > 0) {
							String processStepInfoSql = "select process_step_info_id from process_step_info where step_index = 1 and process_info_id = " + processInfoList.get(0).toString();// 忽略更多该部门的流程信息
							List<Object> processStepInfoList = getEntityManager().createNativeQuery(processStepInfoSql).getResultList();
							if (processStepInfoList != null && processStepInfoList.size() > 0) {
								taskOrder.setProcessStepInfoId(Integer.parseInt(processStepInfoList.get(0).toString()));
								// taskOrder.setOrderSn("CGYS-" + DateTimeHelper.dateToStr(new Date(), "yyyyMMddHHmmss"));
								taskOrder.setOrderSn("CGYS-" + budgetYear);
								getEntityManager().persist(taskOrder);
								String orderSn = taskOrder.getOrderSn() + "-" + Assit.fillZero(taskOrder.getTaskOrderId(), (short) 9);
								taskOrder.setOrderSn(orderSn);
								String processStepUserSql = "select IFNULL(GROUP_CONCAT(user_id), '') as result from process_step_user where type = 0 and process_step_info_id = " + processStepInfoList.get(0).toString();// 只取第一步处理人
								List<Object> processStepUserList = getEntityManager().createNativeQuery(processStepUserSql).getResultList();
								if (processStepInfoList != null && processStepInfoList.size() > 0) {
									String[] processStepUserArr = processStepUserList.get(0).toString().split(",");
									for (String processStepUser : processStepUserArr) {
										TaskUser taskUser = new TaskUser();
										taskUser.setTaskOrderId(taskOrder.getTaskOrderId());
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
					nboRoot.setOrderSn(taskOrder.getOrderSn());
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
							nboLeaf.setOrderSn(taskOrder.getOrderSn());
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

	@SuppressWarnings("unchecked")
	public JSONArray getCandidateProject() {
		JSONArray resultSet = new JSONArray();
		String dataSql = "select the_id, the_type, the_state, the_value, multilevel, total_amount, department_info_id from ys_convention_project where deleted = 0 and the_type = 1";
		if (sessionToken.getDepartmentInfoId() != null) {
			dataSql += " and department_info_id = " + sessionToken.getDepartmentInfoId();
		}
		dataSql += " and the_id in (select convention_project_id from ys_convention_project_user where user_info_id = " + sessionToken.getUserInfoId() + ")";
		Map<Object, BigDecimal> multilevelProjectTotalAmountMap = new HashMap<Object, BigDecimal>();
		Map<Object, JSONArray> subItemArrMap = new HashMap<Object, JSONArray>();
		List<Object[]> dataList = getEntityManager().createNativeQuery(dataSql).getResultList();
		if (dataList != null && dataList.size() > 0) {
			List<Object> multilevelProjectList = new ArrayList<Object>();
			for (Object[] data : dataList) {
				if ((Boolean) data[4]) {
					multilevelProjectList.add(data[0]);
				}
			}
			if (multilevelProjectList != null && multilevelProjectList.size() > 0) {
				List<Object[]> projectExtendList = getEntityManager().createNativeQuery("select convention_project_id, the_id, the_value, total_amount, the_description from ys_convention_project_extend").getResultList();
				if (projectExtendList != null && projectExtendList.size() > 0) {
					for (Object[] projectExtend : projectExtendList) {
						JSONObject subItem = new JSONObject();
						subItem.accumulate("theId", projectExtend[1]);
						subItem.accumulate("theValue", projectExtend[2]);
						subItem.accumulate("totalAmount", projectExtend[3]);
						subItem.accumulate("theDescription", projectExtend[4]);
						if (subItemArrMap.get(projectExtend[0]) != null) {
							subItemArrMap.get(projectExtend[0]).add(subItem);
						} else {
							JSONArray subItemArr = new JSONArray();
							subItemArr.add(subItem);
							subItemArrMap.put(projectExtend[0], subItemArr);
						}
						BigDecimal augend = new BigDecimal(projectExtend[3].toString());
						if (multilevelProjectTotalAmountMap.get(projectExtend[0]) != null) {
							multilevelProjectTotalAmountMap.put(projectExtend[0], multilevelProjectTotalAmountMap.get(projectExtend[0]).add(augend));
						} else {
							multilevelProjectTotalAmountMap.put(projectExtend[0], augend);
						}
					}
				}
			}
		}

		if (dataList != null && dataList.size() > 0) {
			List<Object[]> departmentList = getEntityManager().createNativeQuery("select the_id, the_value from ys_department_info where deleted = 0").getResultList();
			Map<Object, Object> departmentMap = new HashMap<Object, Object>();
			if (departmentList != null && departmentList.size() > 0) {
				for (Object[] department : departmentList) {
					departmentMap.put(department[0], department[1]);
				}
			}
			for (Object[] data : dataList) {
				JSONObject row = new JSONObject();
				row.accumulate("projectId", data[0]);
				row.accumulate("projectName", data[3]);
				row.accumulate("projectType", Integer.parseInt(data[1].toString()) == 1 ? "收入预算" : "支出预算");
				row.accumulate("departmentName", departmentMap.get(data[6]));
				boolean multilevel = (Boolean) data[4];
				row.accumulate("multilevel", multilevel);
				if (multilevel) {
					row.accumulate("totalAmount", multilevelProjectTotalAmountMap.get(data[0]).toString());// 显示完整的数据，而不是科学记数法
					row.accumulate("subItemArr", subItemArrMap.get(data[0]));
				} else {
					row.accumulate("totalAmount", data[5]);
					row.accumulate("subItemArr", new JSONArray());
				}
				row.accumulate("theState", Integer.parseInt(data[1].toString()) == 1 ? "已打开" : "已关闭");
				resultSet.add(row);
			}
		}
		return resultSet;
	}

	public JSONObject getSaveResult() {
		return saveResult;
	}

	public void setSaveArgs(String saveArgs) {
		this.saveArgs = saveArgs;
	}
}
