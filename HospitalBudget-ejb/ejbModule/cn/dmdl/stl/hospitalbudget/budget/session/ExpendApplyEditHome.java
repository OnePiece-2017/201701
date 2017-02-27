package cn.dmdl.stl.hospitalbudget.budget.session;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;











import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.admin.entity.UserInfo;
import cn.dmdl.stl.hospitalbudget.budget.entity.ExpendApplyInfo;
import cn.dmdl.stl.hospitalbudget.budget.entity.NormalExpendBudgetOrderInfo;
import cn.dmdl.stl.hospitalbudget.budget.entity.TaskOrder;
import cn.dmdl.stl.hospitalbudget.budget.entity.TaskUser;
import cn.dmdl.stl.hospitalbudget.common.session.CriterionEntityHome;
import cn.dmdl.stl.hospitalbudget.util.Assit;
import cn.dmdl.stl.hospitalbudget.util.CommonFinder;

@Name("expendApplyEditHome")
public class ExpendApplyEditHome extends CriterionEntityHome<ExpendApplyInfo>{
	private static final long serialVersionUID = 1L;
	private List<Object[]> applayUserList;//报销人列表
	private List<Object[]> fundsSourceList;//资金来源列表
	private List<Object[]> reciveCompanyList;//收款单位列表
	private List<Object[]> expendList;//选中列表
	private Integer taskOrderId;
	private Object[] projectObj;
	private Object[] departObj;
	
	private String applySn;//单据编号
	private Integer projectId;//项目Id
	private Integer year;//年份
	private Integer fundsSourceId;//资金来源id
	private Integer departmentId;;//部门id
	private Integer applyUser;//申请人id
	private String reciveCompany;//收款单位
	private String invoiceSn;//发票
	private String projectJson;//json
	
	private JSONObject saveResult;
	
	
	public void wire(){
		fundsSourceList = new ArrayList<Object[]>();
		reciveCompanyList = new ArrayList<Object[]>();
		projectJson = "";
		expendList = new ArrayList<Object[]>();
		//初始化人员
		wireBudgetPerson();
		//初始化资金来源
		Object[] sourceObj = new Object[2];
		sourceObj[0] = 1;
		sourceObj[1] = "自有资金";
		fundsSourceList.add(sourceObj);
		getAttendProject();
	}
	
	@SuppressWarnings("unchecked")
	public List<Object[]> getAttendProject() {
		SimpleDateFormat sdfday = new SimpleDateFormat("yyyy-MM-dd");
		JSONArray resultSet = new JSONArray();
		StringBuffer dataSql = new StringBuffer();
		dataSql.append(" SELECT");
		dataSql.append(" eai.expend_apply_code,");//0单据编号
		dataSql.append(" eai.`year`,");//1执行年份
		dataSql.append(" ydi.the_id as depart_id,");//2报销部门
		dataSql.append(" eai.applay_user_id,");//3报销人
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
		dataSql.append(" eai.`comment`, ");//15备注
		dataSql.append(" ycp.the_id as pro_id, ");//16备注
		dataSql.append(" ycpe.the_id as sub_id, ");//17备注
		dataSql.append("ydi.the_value as depart_name,");//18备注
		dataSql.append("eai.normal_expend_budget_order_id ");//19备注
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
			Object[] obj= dataList.get(0);
			Object[]  applyObj = new Object[9]; 
			applyObj[0] = obj[7] == null ? obj[8] : obj[7];
			applyObj[1] = obj[10];
			applyObj[2] =  Double.parseDouble(obj[10].toString()) - Double.parseDouble(obj[11].toString());;
			applyObj[3] = obj[11];
			applyObj[4] = obj[9];
			try {
				applyObj[5] = sdfday.format(sdfday.parse(obj[12].toString()));
			} catch (ParseException e) {
				applyObj[5] = obj[12];
			}
			applyObj[6] = obj[15];
			applyObj[7] = "";
			applyObj[8] = obj[19];
			expendList.add(applyObj);
			projectId = obj[16] == null ? Integer.parseInt(obj[17].toString()) : Integer.parseInt(obj[16].toString());
			departmentId = Integer.parseInt(obj[2].toString());
			projectObj = new Object[2];
			projectObj[0] = projectId;
			projectObj[1] = applyObj[0];
			
			departObj = new Object[2];
			departObj[0] = departmentId;
			departObj[1] = obj[18];
			applySn = obj[0].toString();
			fundsSourceId = Integer.parseInt(obj[4].toString());
			applyUser = Integer.parseInt(obj[3].toString());
			reciveCompany = obj[5].toString();
			invoiceSn = obj[6].toString();
			year = Integer.parseInt(obj[1].toString());
		}
		return resultSet;
	}
	/**
	 * 预算年份
	 * @return
	 */
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
	private void wireBudgetPerson() {
		Map<Object, List<Object[]>> userMap = new HashMap<Object, List<Object[]>>();
		StringBuffer userSql = new StringBuffer();
		userSql.append(" select");
		userSql.append(" user_info.department_info_id,");
		userSql.append(" user_info.user_info_id,");
		userSql.append(" user_info_extend.fullname");
		userSql.append(" from user_info");
		userSql.append(" inner join user_info_extend on user_info_extend.user_info_extend_id = user_info.user_info_extend_id");
		userSql.append(" where user_info.deleted = 0 and user_info.enabled = 1 and user_info.department_info_id is not null");
		List<Object[]> userList = getEntityManager().createNativeQuery(userSql.toString()).getResultList();
		if (userList != null && userList.size() > 0) {
			for (Object[] user : userList) {
				if (userMap.get(user[0]) != null) {
					userMap.get(user[0]).add(new Object[] { user[1], user[2] });
				} else {
					List<Object[]> newList = new ArrayList<Object[]>();
					newList.add(new Object[] { user[1], user[2] });
					userMap.put(user[0], newList);
				}
			}
		}

		if (applayUserList != null) {
			applayUserList.clear();
		} else {
			applayUserList = new ArrayList<Object[]>();
		}
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
				if (userMap.get(root) != null && userMap.get(root).size() > 0) {
					applayUserList.add(new Object[] { valueMap.get(root), userMap.get(root) });
				}
				disposeLeafBudgetPerson(applayUserList, userMap, nexusMap, valueMap, 1, nexusMap.get(root));
			}
		}

	}
	
	/** 递归处理子节点BudgetPerson */
	public void disposeLeafBudgetPerson(List<Object[]> targetList, Map<Object, List<Object[]>> userMap, Map<Object, List<Object>> nexusMap, Map<Object, Object> valueMap, int indentWidth, List<Object> leafList) {
		if (leafList != null && leafList.size() > 0) {
			for (Object leaf : leafList) {
				String indentStr = "";
				for (int i = 0; i < indentWidth; i++) {
					indentStr += "　";
				}
				if (userMap.get(leaf) != null && userMap.get(leaf).size() > 0) {
					List<Object[]> userList = userMap.get(leaf);
					List<Object[]> tmpList = new ArrayList<Object[]>();
					if (userList != null && userList.size() > 0) {
						for (Object[] user : userList) {
							tmpList.add(new Object[] { user[0], indentStr + user[1] });
						}
					}
					targetList.add(new Object[] { indentStr + valueMap.get(leaf), tmpList });
				}
				disposeLeafBudgetPerson(targetList, userMap, nexusMap, valueMap, indentWidth + 1, nexusMap.get(leaf));
			}
		}
	}
	
	/**
	 * 选择项目后查询项目列表
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public void selectProject(){
		if(null == expendList){
			expendList = new ArrayList<Object[]>();
		}else{
			expendList.clear();
		}
		boolean isSubProject = false;
		StringBuffer sql = new StringBuffer();
		NormalExpendBudgetOrderInfo neboi = getEntityManager().find(NormalExpendBudgetOrderInfo.class, projectId);
		if(null != neboi.getSubProjectId()){
			isSubProject = true;
		}
		if(isSubProject){
			sql.append(" SELECT ");
			sql.append(" ycpe.the_value,");
			sql.append(" neboi.project_amount,");
			sql.append(" neboi.now_amout,");
			sql.append(" neboi.normal_expend_budget_order_id,");
			sql.append(" ycp.department_info_id ");
			sql.append(" FROM normal_expend_budget_order_info neboi ");
			sql.append(" LEFT JOIN ys_convention_project_extend ycpe ON ycpe.the_id = neboi.sub_project_id ");
			sql.append(" LEFT JOIN ys_convention_project ycp on ycp.the_id = ycpe.convention_project_id ");
			sql.append(" where neboi.normal_expend_budget_order_id=");
			sql.append(projectId);
		}else{
			sql.append(" SELECT ");
			sql.append(" ycp.the_value,");
			sql.append(" neboi.project_amount,");
			sql.append(" neboi.now_amout,");
			sql.append(" neboi.normal_expend_budget_order_id,");
			sql.append(" ycp.department_info_id ");
			sql.append(" FROM normal_expend_budget_order_info neboi ");
			sql.append(" LEFT JOIN ys_convention_project ycp ON ycp.the_id = neboi.normal_project_id ");
			sql.append(" where neboi.normal_expend_budget_order_id=");
			sql.append(projectId);
		}
		List<Object[]> list = getEntityManager().createNativeQuery(sql.toString()).getResultList();
		Object[] project = list.get(0);
		DecimalFormat df = new DecimalFormat("#.00");
		double totalMoney = project[1] == null ? 0 : Double.parseDouble(project[1].toString());
		double nowMoney = project[2] == null ? 0 : Double.parseDouble(project[2].toString());
		Object[] obj = new Object[9];
		obj[0] = project[0];
		obj[1] = df.format(Double.parseDouble(project[1].toString()));
		obj[2] = totalMoney - nowMoney == 0 ? "0.00" : df.format(totalMoney - nowMoney);
		obj[3] = Double.parseDouble(project[2].toString()) == 0 ? "0.00" : df.format(Double.parseDouble(project[2].toString()));
		obj[4] = "";
		obj[5] = "";
		obj[6] = "";
		obj[7] = "";
		obj[8] = project[3];
		departmentId = Integer.parseInt(project[4].toString());
		expendList.add(obj);
	}
	/**
	 * 提交保存
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public void save(){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		joinTransaction();
		if (saveResult != null) {
			saveResult.clear();
		} else {
			saveResult = new JSONObject();
		}
		saveResult.accumulate("invoke_result", "INVOKE_SUCCESS");
		saveResult.accumulate("message", "提交成功！");
		JSONObject jsonObject = JSONObject.fromObject(projectJson);
		JSONArray expendList = jsonObject.getJSONArray("expend_list");
		for(int i = 0;i < expendList.size();i++){
			//获取参数
			/*JSONObject project = expendList.getJSONObject(i);
			String projectInfoId = project.getString("project_id");
			String expendMoney = project.getString("expend_money");
			String expendTime = project.getString("expend_time");
			String expendComment = project.getString("expend_comment");
			UserInfo userInfo = getEntityManager().find(UserInfo.class, sessionToken.getUserInfoId());
			TaskOrder taskOrder = new TaskOrder();
			//旧订单改状态
			TaskOrder oldTaskOrder = getEntityManager().find(TaskOrder.class, taskOrderId);
			oldTaskOrder.setOrderStatus(4);
			getEntityManager().merge(oldTaskOrder);
			
			taskOrder.setOrderSn(oldTaskOrder.getOrderSn());
			taskOrder.setTaskName(userInfo.getYsDepartmentInfo().getTheValue() + "〔" + year + "〕常规支出执行");
			taskOrder.setDeptId(userInfo.getYsDepartmentInfo().getTheId());
			taskOrder.setEditUserId(sessionToken.getUserInfoId());
			taskOrder.setTaskType(4);// 常规支出执行
			taskOrder.setInsertTime(new Date());
			taskOrder.setInsertUser(sessionToken.getUserInfoId());
			taskOrder.setOrderStatus(0);// 待处理
			taskOrder.setAuditOpinion(null);
			List<Object> processInfoList = getEntityManager().createNativeQuery("select process_info_id from process_info where deleted = 0 and process_type = 1 and project_process_type = 4 and dept_id = " + userInfo.getYsDepartmentInfo().getTheId()).getResultList();// 获取当前登录人所属部门的常规项目流程的常规支出执行流程
			if (processInfoList != null && processInfoList.size() > 0) {// 有流程信息
				List<Object> processStepInfoList = getEntityManager().createNativeQuery("select process_step_info_id from process_step_info where step_index = 1 and process_info_id = " + processInfoList.get(0).toString()).getResultList();// 获取流程配置中的第一步
				if (processStepInfoList != null && processStepInfoList.size() > 0) {// 有步骤配置
					int processStepInfoId = Integer.parseInt(processStepInfoList.get(0).toString());
					taskOrder.setProcessStepInfoId(processStepInfoId);
					getEntityManager().persist(taskOrder);// 执行持久化
					List<Object> processStepUserList = getEntityManager().createNativeQuery("select user_id from process_step_user where type = 0 and process_step_info_id = " + processStepInfoId).getResultList();// 获取流程配置中的第一步的处理人
					if (processStepInfoList != null && processStepInfoList.size() > 0) {// 步骤配置中有用户
						for (Object processStepUser : processStepUserList) {
							TaskUser taskUser = new TaskUser();
							taskUser.setTaskOrderId(taskOrder.getTaskOrderId());
							taskUser.setUserId((Integer) processStepUser);
							taskUser.setHandleFlg(false);// 未处理
							getEntityManager().persist(taskUser);
						}
					} else {
						saveResult.element("invoke_result", "INVOKE_FAILURE");
						saveResult.element("message", "操作失败！请先完善科室[" + userInfo.getYsDepartmentInfo().getTheValue() + "]的步骤配置的用户信息！");
						return ;
					}
				} else {
					saveResult.element("invoke_result", "INVOKE_FAILURE");
					saveResult.element("message", "操作失败！请先完善科室[" + userInfo.getYsDepartmentInfo().getTheValue() + "]的步骤配置！");
					return ;
				}
			} else {
				saveResult.element("invoke_result", "INVOKE_FAILURE");
				saveResult.element("message", "操作失败！请先完善科室[" + userInfo.getYsDepartmentInfo().getTheValue() + "]的流程信息！");
				return ;
			}
			List<ExpendApplyInfo> eaiList = getEntityManager().createQuery("select expendApplyInfo from ExpendApplyInfo expendApplyInfo where expendApplyInfo.taskOrderId=" + taskOrderId).getResultList();
			ExpendApplyInfo eai = eaiList.get(0);
			eai.setExpendApplyCode(applySn);
			eai.setTaskOrderId(taskOrder.getTaskOrderId());
			eai.setOrderSn(taskOrder.getOrderSn());
			eai.setYear(year.toString());
			eai.setDepartmentInfoId(departmentId);
			eai.setApplyUserId(applyUser);
			eai.setFundsSource(fundsSourceId);
			eai.setReciveCompany(reciveCompany);
			eai.setInvoiceNum(invoiceSn);
			eai.setNormalExpendBudgetOrderId(Integer.parseInt(projectInfoId));
			eai.setExpendMoney(Float.parseFloat(expendMoney));
			eai.setUpdateUser(sessionToken.getUserInfoId());
			eai.setUpdateTime(new Date());
			eai.setDeleted(false);
			try {
				eai.setExpendTime(sdf.parse(expendTime));
			} catch (ParseException e) {
				saveResult.element("invoke_result", "INVOKE_FAILURE");
				saveResult.element("message", "操作失败！时间保存失败！");
				e.printStackTrace();
				return ;
			}
			eai.setComment(expendComment);
			getEntityManager().merge(eai);
			//执行申请后再减掉金额
			NormalExpendBudgetOrderInfo neboi = getEntityManager().find(NormalExpendBudgetOrderInfo.class, Integer.parseInt(projectInfoId));
			if(null == neboi.getSubProjectId()){
				neboi.setNowAmount(neboi.getNowAmount() - Float.parseFloat(expendMoney));
				getEntityManager().merge(neboi);
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
					parentProject.setNowAmount(parentProject.getNowAmount() - Float.parseFloat(expendMoney));
					getEntityManager().merge(parentProject);
				}
			}*/
			
			
		}
		getEntityManager().flush();
		raiseAfterTransactionSuccessEvent();
		return ;
	}
	
	/**
	 * 删除申请列表
	 * @return
	 */
	public void clearList(){
		expendList.clear();
	}
	
	

	public List<Object[]> getApplayUserList() {
		return applayUserList;
	}

	public void setApplayUserList(List<Object[]> applayUserList) {
		this.applayUserList = applayUserList;
	}

	public List<Object[]> getFundsSourceList() {
		return fundsSourceList;
	}

	public void setFundsSourceList(List<Object[]> fundsSourceList) {
		this.fundsSourceList = fundsSourceList;
	}

	public List<Object[]> getReciveCompanyList() {
		return reciveCompanyList;
	}

	public void setReciveCompanyList(List<Object[]> reciveCompanyList) {
		this.reciveCompanyList = reciveCompanyList;
	}



	public List<Object[]> getExpendList() {
		return expendList;
	}

	public void setExpendList(List<Object[]> expendList) {
		this.expendList = expendList;
	}

	public Integer getProjectId() {
		return projectId;
	}

	public void setProjectId(Integer projectId) {
		this.projectId = projectId;
	}

	public String getApplySn() {
		return applySn;
	}

	public void setApplySn(String applySn) {
		this.applySn = applySn;
	}

	public Integer getYear() {
		return year;
	}

	public void setYear(Integer year) {
		this.year = year;
	}

	public Integer getFundsSourceId() {
		return fundsSourceId;
	}

	public void setFundsSourceId(Integer fundsSourceId) {
		this.fundsSourceId = fundsSourceId;
	}

	public Integer getDepartmentId() {
		return departmentId;
	}

	public void setDepartmentId(Integer departmentId) {
		this.departmentId = departmentId;
	}

	public Integer getApplyUser() {
		return applyUser;
	}

	public void setApplyUser(Integer applyUser) {
		this.applyUser = applyUser;
	}

	public String getReciveCompany() {
		return reciveCompany;
	}

	public void setReciveCompany(String reciveCompany) {
		this.reciveCompany = reciveCompany;
	}

	public String getInvoiceSn() {
		return invoiceSn;
	}

	public void setInvoiceSn(String invoiceSn) {
		this.invoiceSn = invoiceSn;
	}

	public String getProjectJson() {
		return projectJson;
	}

	public void setProjectJson(String projectJson) {
		this.projectJson = projectJson;
	}

	public JSONObject getSaveResult() {
		return saveResult;
	}

	public void setSaveResult(JSONObject saveResult) {
		this.saveResult = saveResult;
	}

	public Integer getTaskOrderId() {
		return taskOrderId;
	}

	public void setTaskOrderId(Integer taskOrderId) {
		this.taskOrderId = taskOrderId;
	}

	public Object[] getProjectObj() {
		return projectObj;
	}

	public void setProjectObj(Object[] projectObj) {
		this.projectObj = projectObj;
	}

	public Object[] getDepartObj() {
		return departObj;
	}

	public void setDepartObj(Object[] departObj) {
		this.departObj = departObj;
	}
	
	
}
