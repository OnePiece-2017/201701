package cn.dmdl.stl.hospitalbudget.budget.session;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.budget.entity.ExpendApplyInfo;
import cn.dmdl.stl.hospitalbudget.budget.entity.ExpendApplyProject;
import cn.dmdl.stl.hospitalbudget.budget.entity.ExpendConfirmInfo;
import cn.dmdl.stl.hospitalbudget.budget.entity.ExpendConfirmProject;
import cn.dmdl.stl.hospitalbudget.budget.entity.NormalBudgetCollectionInfo;
import cn.dmdl.stl.hospitalbudget.budget.entity.NormalExpendBudgetOrderInfo;
import cn.dmdl.stl.hospitalbudget.budget.entity.ProcessStepInfo;
import cn.dmdl.stl.hospitalbudget.budget.entity.TaskOrder;
import cn.dmdl.stl.hospitalbudget.budget.entity.TaskUser;
import cn.dmdl.stl.hospitalbudget.common.session.CriterionEntityHome;
import cn.dmdl.stl.hospitalbudget.util.CommonFinder;

@Name("expendExecutHome")
public class ExpendExecutHome extends CriterionEntityHome<Object> {

	private static final long serialVersionUID = 1L;
	private Integer taskOrderId;
	private Integer handResult;//处理结果
	private String handSay;//处理意见
	private JSONObject saveResult;
	private Object[] expendApplyInfo = new Object[14];//公共头部
	private List<Object[]> projectList;//项目列表
	private String setpName;//步骤名称
	
	
	/**
	 * 初始化
	 * @param oldTaskOrderId
	 * @param newTaskOrderId
	 */
	public void wire(){
		
		setpName = "";
		getAttendProject();
		queryStepName();
		
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
		if (handResult != null) {
			try {
				joinTransaction();
				TaskOrder oldTaskOrder = getEntityManager().find(TaskOrder.class, taskOrderId);
				TaskOrder newTaskOrder = new TaskOrder();
				newTaskOrder.setOrderSn(oldTaskOrder.getOrderSn());
				newTaskOrder.setTaskName(oldTaskOrder.getTaskName());
				newTaskOrder.setDeptId(oldTaskOrder.getDeptId());
				newTaskOrder.setEditUserId(oldTaskOrder.getEditUserId());
				newTaskOrder.setTaskType(oldTaskOrder.getTaskType());
				newTaskOrder.setInsertTime(new Date());
				newTaskOrder.setInsertUser(sessionToken.getUserInfoId());
				List<ExpendApplyInfo> oldExpendApplyInfoList = getEntityManager().createQuery("select expendApplyInfo from ExpendApplyInfo expendApplyInfo where expendApplyInfo.taskOrderId = " + oldTaskOrder.getTaskOrderId()).getResultList();
				if (handResult != 1) {// 不通过
					oldTaskOrder.setOrderStatus(2);
					newTaskOrder.setOrderStatus(3);
					getEntityManager().persist(newTaskOrder);// 提前缓存对象
					if (oldExpendApplyInfoList != null && oldExpendApplyInfoList.size() > 0) {
						for (ExpendApplyInfo expendApplyInfo : oldExpendApplyInfoList) {
							expendApplyInfo.setTaskOrderId(newTaskOrder.getTaskOrderId());// 跟踪订单信息
							ProcessStepInfo currentProcessStepInfo = getEntityManager().find(ProcessStepInfo.class, oldTaskOrder.getProcessStepInfoId());
							List<ProcessStepInfo> nextProcessStepInfoList = getEntityManager().createQuery("select processStepInfo from ProcessStepInfo processStepInfo where processStepInfo.processInfo.processInfoId = " + currentProcessStepInfo.getProcessInfo().getProcessInfoId() + " and processStepInfo.stepIndex = " + (currentProcessStepInfo.getStepIndex() - 1)).getResultList();
							if (nextProcessStepInfoList != null && nextProcessStepInfoList.size() > 0) {// 有上一步 是不是初始状态
								expendApplyInfo.setExpendApplyStatus(0);
							}
							getEntityManager().merge(expendApplyInfo);
						}
					}
					//updateProjectTaskOrderId(oldTaskOrder.getTaskOrderId(), newTaskOrder.getTaskOrderId());
				} else {// 通过
					oldTaskOrder.setOrderStatus(1);
					newTaskOrder.setOrderStatus(0);
					ProcessStepInfo currentProcessStepInfo = getEntityManager().find(ProcessStepInfo.class, oldTaskOrder.getProcessStepInfoId());
					List<ProcessStepInfo> nextProcessStepInfoList = getEntityManager().createQuery("select processStepInfo from ProcessStepInfo processStepInfo where processStepInfo.processInfo.processInfoId = " + currentProcessStepInfo.getProcessInfo().getProcessInfoId() + " and processStepInfo.stepIndex = " + (currentProcessStepInfo.getStepIndex() + 1)).getResultList();
					if (nextProcessStepInfoList != null && nextProcessStepInfoList.size() > 0) {// 有下一步
						ProcessStepInfo nextProcessStepInfo = nextProcessStepInfoList.get(0);
						newTaskOrder.setProcessStepInfoId(nextProcessStepInfo.getProcessStepInfoId());
						getEntityManager().persist(newTaskOrder);// 提前缓存对象
						if (oldExpendApplyInfoList != null && oldExpendApplyInfoList.size() > 0) {
							for (ExpendApplyInfo expendApplyInfo : oldExpendApplyInfoList) {
								expendApplyInfo.setTaskOrderId(newTaskOrder.getTaskOrderId());// 跟踪订单信息
								expendApplyInfo.setExpendApplyStatus(1);
								getEntityManager().merge(expendApplyInfo);
							}
						}
						//updateProjectTaskOrderId(oldTaskOrder.getTaskOrderId(), newTaskOrder.getTaskOrderId());
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
						//如果通过则修改申请单状态
						if (oldExpendApplyInfoList != null && oldExpendApplyInfoList.size() > 0) {
							for (ExpendApplyInfo expendApplyInfo : oldExpendApplyInfoList) {
								expendApplyInfo.setExpendApplyStatus(2);
								getEntityManager().merge(expendApplyInfo);
							}
						}
						//生成确认单和确认项目单
						ExpendConfirmInfo eci = new ExpendConfirmInfo();
						eci.setExpend_apply_info_id(oldExpendApplyInfoList.get(0).getExpendApplyInfoId());
						eci.setInsertUser(sessionToken.getUserInfoId());
						eci.setInsertTime(new Date());
						eci.setTotalMoney(oldExpendApplyInfoList.get(0).getTotalMoney());
						eci.setConfirm_status(0);
						eci.setYear(oldExpendApplyInfoList.get(0).getYear());
						getEntityManager().persist(eci);
						
						String eapSql = "select expendApplyProject from ExpendApplyProject expendApplyProject where expendApplyProject.deleted=0 and expendApplyProject.expendApplyInfoId=" + oldExpendApplyInfoList.get(0).getExpendApplyInfoId();
						List<ExpendApplyProject> eapList = getEntityManager().createQuery(eapSql).getResultList();
						for(ExpendApplyProject eap : eapList){
							ExpendConfirmProject efp = new ExpendConfirmProject();
							efp.setExpend_confirm_info_id(eci.getExpend_confirm_info_id());
							efp.setExpend_apply_project_id(eap.getExpendApplyProjectId());
							efp.setProjectId(eap.getProjectId());
							getEntityManager().persist(efp);
						}
					}
				}
				List<TaskUser> taskUserList = getEntityManager().createQuery("select taskUser from TaskUser taskUser where taskOrderId = " + oldTaskOrder.getTaskOrderId() + " and userId = " + sessionToken.getUserInfoId()).getResultList();
				if (taskUserList != null && taskUserList.size() > 0) {
					TaskUser taskUser = taskUserList.get(0);
					taskUser.setHandleFlg(true);
					getEntityManager().merge(taskUser);
				}
				oldTaskOrder.setAuditOpinion(handSay);
				getEntityManager().merge(oldTaskOrder);
				getEntityManager().flush();
			} catch (Exception e) {
				e.printStackTrace();
				saveResult.element("invoke_result", "INVOKE_FAILURE");
				saveResult.element("message", "操作失败！" + e.getMessage());
			}
		} else {
			saveResult.element("invoke_result", "INVOKE_FAILURE");
			saveResult.element("message", "提交失败！参数为空。");
		}
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> getAttendProject() {
		projectList = new ArrayList<Object[]>();
		CommonFinder commonFinder = new CommonFinder();
		SimpleDateFormat sdfday = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat sdfs = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		JSONArray resultSet = new JSONArray();
		StringBuffer dataSql = new StringBuffer();
		dataSql.append(" SELECT");
		dataSql.append(" eai.`year`,");//0执行年份
		dataSql.append(" uie.fullname as applay_name,");//1申请人
		dataSql.append(" eai.expend_apply_code,");//2单据编号
		dataSql.append(" eai.recive_company,");//3收款单位
		dataSql.append(" eai.invoice_num,");//4发票号
		dataSql.append(" eai.summary,");//5摘要
		dataSql.append(" uie1.fullname,");//6报销人
		dataSql.append(" eai.apply_time, ");//7申请时间
		dataSql.append(" eai.register_time, ");//8登记时间
		dataSql.append(" uie2.fullname as register, ");//9登记人
		dataSql.append(" eai.`comment`, eai.total_money as totalMoney ");//10  11备注
		dataSql.append(" FROM expend_apply_info eai");
		dataSql.append(" LEFT JOIN user_info ui on eai.applay_user_id=ui.user_info_id ");
		dataSql.append(" LEFT JOIN user_info_extend uie on uie.user_info_extend_id=ui.user_info_extend_id ");
		dataSql.append(" LEFT JOIN user_info ui1 on eai.reimbursementer=ui1.user_info_id ");
		dataSql.append(" LEFT JOIN user_info_extend uie1 on uie1.user_info_extend_id=ui1.user_info_extend_id ");
		dataSql.append(" LEFT JOIN user_info ui2 ON eai.register = ui2.user_info_id ");
		dataSql.append(" LEFT JOIN user_info_extend uie2 ON uie2.user_info_extend_id = ui2.user_info_extend_id ");
		dataSql.append(" where eai.deleted=0 and eai.task_order_id=").append(taskOrderId);
		dataSql.insert(0, "select * from (").append(") as recordset");// 解决找不到列
		List<Object[]> dataList = getEntityManager().createNativeQuery(dataSql.toString()).getResultList();
		if (dataList != null && dataList.size() > 0) {
			expendApplyInfo = dataList.get(0);
			if(null != expendApplyInfo[7] && !expendApplyInfo[7].toString().equals("")){
				try {
					expendApplyInfo[7] = sdfday.format(sdfday.parse(expendApplyInfo[7].toString()));
				} catch (ParseException e) {
					
				}
			}
			if(null != expendApplyInfo[8] && !expendApplyInfo[8].toString().equals("")){
				try {
					expendApplyInfo[8] = sdfday.format(sdfday.parse(expendApplyInfo[8].toString()));
				} catch (ParseException e) {
					
				}
			}
		}
		
		//查询项目列表
		StringBuffer projectSql = new StringBuffer();
		projectSql.append(" SELECT ");
		projectSql.append(" up.the_value, ");
		projectSql.append(" expi.budget_amount, ");
		projectSql.append(" expi.budget_amount_frozen, ");
		projectSql.append(" expi.budget_amount_surplus, ");
		projectSql.append(" eap.expend_money, ");
		projectSql.append(" eap.project_id,fs.the_value as source_name ");
		projectSql.append(" FROM expend_apply_project eap ");
		projectSql.append(" LEFT JOIN expend_apply_info eai ON eap.expend_apply_info_id = eai.expend_apply_info_id ");
		projectSql.append(" LEFT JOIN routine_project up on up.the_id=eap.project_id ");
		projectSql.append(" LEFT JOIN normal_expend_plan_info expi on  expi.project_id=eap.project_id and expi.`year` = eai.`year` ");
		projectSql.append(" LEFT JOIN ys_funds_source fs on up.funds_source_id=fs.the_id ");
		projectSql.append(" where eap.deleted=0 ");
		projectSql.append(" and eai.task_order_id=").append(taskOrderId);
		List<Object[]> list = getEntityManager().createNativeQuery("select * from (" + projectSql.toString() + ") as test").getResultList();
		float allMoney = 0f;
		if(list.size() > 0){
			for(Object[] obj : list){
				Object[] projectDetail = new Object[8];
				projectDetail[0] = obj[0];
				projectDetail[1] = obj[1];
				projectDetail[2] = Float.parseFloat(obj[2].toString()) - Float.parseFloat(obj[4].toString());
				projectDetail[3] = Float.parseFloat(obj[3].toString()) + Float.parseFloat(obj[4].toString());;
				projectDetail[4] = obj[4];
				allMoney += Float.parseFloat(projectDetail[4].toString());
				projectDetail[5] = obj[6];
				projectDetail[6] = "";
				projectDetail[7] = obj[5];
				projectList.add(projectDetail);
			}
		}
		return resultSet;
	}
	
	/**
	 * 查询步骤名称
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public void queryStepName(){
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT	process_step_info.step_name ");
		sql.append(" FROM task_order ");
		sql.append(" LEFT JOIN process_step_info ON process_step_info.process_step_info_id = task_order.process_step_info_id ");
		sql.append(" where task_order.task_order_id= ");
		sql.append(taskOrderId);
		List<Object> list = getEntityManager().createNativeQuery(sql.toString()).getResultList();
		if(list.size() > 0){
			setpName = list.get(0).toString();
		}
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


	public Object[] getExpendApplyInfo() {
		return expendApplyInfo;
	}

	public void setExpendApplyInfo(Object[] expendApplyInfo) {
		this.expendApplyInfo = expendApplyInfo;
	}

	public String getSetpName() {
		return setpName;
	}

	public void setSetpName(String setpName) {
		this.setpName = setpName;
	}

	public Integer getHandResult() {
		return handResult;
	}

	public void setHandResult(Integer handResult) {
		this.handResult = handResult;
	}

	public String getHandSay() {
		return handSay;
	}

	public void setHandSay(String handSay) {
		this.handSay = handSay;
	}

	public List<Object[]> getProjectList() {
		return projectList;
	}

	public void setProjectList(List<Object[]> projectList) {
		this.projectList = projectList;
	}

	
	
}
