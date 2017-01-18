package cn.dmdl.stl.hospitalbudget.budget.session;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.budget.entity.ExpendApplyInfo;
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
	private Object[] expendApplyInfo;
	private String setpName;//步骤名称
	
	
	/**
	 * 初始化
	 * @param oldTaskOrderId
	 * @param newTaskOrderId
	 */
	public void wire(){
		expendApplyInfo = new Object[16];
		setpName = "";
		getAttendProject();
		queryStepName();
		
	}
	
	@SuppressWarnings("unchecked")
	private void updateProjectTaskOrderId(int oldTaskOrderId, int newTaskOrderId) {
		List<ExpendApplyInfo> oldExpendApplyInfoList = getEntityManager().createQuery("select expendApplyInfo from ExpendApplyInfo expendApplyInfo where expendApplyInfo.taskOrderId = " + oldTaskOrderId).getResultList();
		if (oldExpendApplyInfoList != null && oldExpendApplyInfoList.size() > 0) {
			joinTransaction();
			for (ExpendApplyInfo expendApplyInfo : oldExpendApplyInfoList) {
				expendApplyInfo.setTaskOrderId(newTaskOrderId);// 跟踪订单信息
				NormalExpendBudgetOrderInfo neboi = getEntityManager().find(NormalExpendBudgetOrderInfo.class,expendApplyInfo.getNormalExpendBudgetOrderId());
				/*neboi.setNowAmount(neboi.getNowAmount() + expendApplyInfo.getExpendMoney());
				getEntityManager().merge(expendApplyInfo);
				getEntityManager().merge(neboi);*/
				//不通过加上金额
				neboi.setNowAmount(neboi.getNowAmount() + expendApplyInfo.getExpendMoney());
				getEntityManager().merge(neboi);
				if(null == neboi.getSubProjectId()){
					
				}else{
					String hql = "select normalExpendBudgetOrderInfo from NormalExpendBudgetOrderInfo normalExpendBudgetOrderInfo where normalExpendBudgetOrderInfo.orderSn ='" + neboi.getOrderSn() + "'";
					List<NormalExpendBudgetOrderInfo> neboiList = getEntityManager().createQuery(hql).getResultList();
					NormalExpendBudgetOrderInfo parentProject = null;
					for(NormalExpendBudgetOrderInfo par : neboiList){
						if(null == par.getSubProjectId()){
							parentProject = par;
							break;
						}
					}
					if(null != parentProject){
						parentProject.setNowAmount(parentProject.getNowAmount() + expendApplyInfo.getExpendMoney());
						getEntityManager().merge(parentProject);
					}
				}
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
				if (handResult != 1) {// 不通过
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
						List<Object[]> summaryList = getEntityManager().createNativeQuery("select eai.`year`,eai.expend_money from expend_apply_info eai  where  eai.task_order_id= " + oldTaskOrder.getTaskOrderId()).getResultList();
						if (summaryList != null && summaryList.size() > 0) {
							Object[] summary = summaryList.get(0);
							normalBudgetCollectionInfo.setYear(summary[0].toString());
							normalBudgetCollectionInfo.setBudgetAmount(Double.parseDouble(new DecimalFormat("#.##").format(Double.parseDouble(summary[1].toString()))));
						}
						normalBudgetCollectionInfo.setAmountType(4);
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
				oldTaskOrder.setAuditOpinion(handSay);
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
	public List<Object[]> getAttendProject() {
		CommonFinder commonFinder = new CommonFinder();
		SimpleDateFormat sdfday = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat sdfs = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		JSONArray resultSet = new JSONArray();
		StringBuffer dataSql = new StringBuffer();
		dataSql.append(" SELECT");
		dataSql.append(" eai.expend_apply_code,");//0单据编号
		dataSql.append(" eai.`year`,");//1执行年份
		dataSql.append(" ydi.the_value as depart_name,");//2报销部门
		dataSql.append(" uie1.fullname as apply_name,");//3报销人
		dataSql.append(" eai.funds_source,");//4资金来源
		dataSql.append(" eai.recive_company,");//5收款单位
		dataSql.append(" eai.invoice_num,");//6发票号
		dataSql.append(" ycp.the_value as project_name,");//7项目名称
		dataSql.append(" ycpe.the_value as sub_project_name,");//8子项目名称
		dataSql.append(" eai.expend_money,");//9支出金额
		dataSql.append(" neboi.project_amount,");//10年度预算金额
		dataSql.append(" neboi.now_amout,");//11可支出金额
		dataSql.append(" eai.expend_time,");//12支出时间
		dataSql.append(" eai.insert_time, ");//13申请时间
		dataSql.append(" uie2.fullname  as insert_name,");//14提交人
		dataSql.append(" eai.`comment`  ");//15备注
		dataSql.append(" FROM expend_apply_info eai");
		dataSql.append(" LEFT JOIN normal_expend_budget_order_info neboi ON eai.normal_expend_budget_order_id = neboi.normal_expend_budget_order_id ");
		dataSql.append(" LEFT JOIN ys_convention_project ycp on ycp.the_id=neboi.normal_project_id ");
		dataSql.append(" left join ys_convention_project_extend ycpe on ycpe.the_id = neboi.sub_project_id ");
		dataSql.append(" LEFT JOIN ys_department_info ydi on eai.department_info_id=ydi.the_id ");
		dataSql.append(" LEFT JOIN user_info ui on eai.applay_user_id=ui.user_info_id ");
		dataSql.append(" LEFT JOIN user_info_extend uie1 on uie1.user_info_extend_id=ui.user_info_extend_id ");
		dataSql.append(" LEFT JOIN user_info ui1 on ui1.user_info_id=eai.insert_user ");
		dataSql.append(" LEFT JOIN user_info_extend uie2 on uie2.user_info_extend_id=ui1.user_info_extend_id ");
		dataSql.append(" where eai.deleted=0 and eai.task_order_id=").append(taskOrderId);
		dataSql.insert(0, "select * from (").append(") as recordset");// 解决找不到列
		List<Object[]> dataList = getEntityManager().createNativeQuery(dataSql.toString()).getResultList();
		if (dataList != null && dataList.size() > 0) {
			expendApplyInfo = dataList.get(0);
			expendApplyInfo[4] = commonFinder.getMoneySource(Integer.parseInt(expendApplyInfo[4].toString()));
			expendApplyInfo[11] = Double.parseDouble(expendApplyInfo[9].toString()) + Double.parseDouble(expendApplyInfo[11].toString());
			try {
				expendApplyInfo[12] =  sdfday.format(sdfday.parse(expendApplyInfo[12].toString()));
				expendApplyInfo[13] =  sdfs.format(sdfs.parse(expendApplyInfo[13].toString()));
			} catch (ParseException e) {
				e.printStackTrace();
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

	
	
}
