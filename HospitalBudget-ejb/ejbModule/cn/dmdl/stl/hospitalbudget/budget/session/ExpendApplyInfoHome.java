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
import cn.dmdl.stl.hospitalbudget.budget.entity.ExpendApplyProject;
import cn.dmdl.stl.hospitalbudget.budget.entity.NormalExpendBudgetOrderInfo;
import cn.dmdl.stl.hospitalbudget.budget.entity.TaskOrder;
import cn.dmdl.stl.hospitalbudget.budget.entity.TaskUser;
import cn.dmdl.stl.hospitalbudget.common.session.CriterionEntityHome;
import cn.dmdl.stl.hospitalbudget.util.Assit;

@Name("expendApplyInfoHome")
public class ExpendApplyInfoHome extends CriterionEntityHome<ExpendApplyInfo>{
	private static final long serialVersionUID = 1L;
	private List<Object[]> departmentInfoList;// 科室select
	private List<Object[]> applayUserList;//报销人列表
	private List<Object[]> fundsSourceList;//资金来源列表
	private List<Object[]> reciveCompanyList;//收款单位列表
	private List<Object[]> projectList;//主项目列表
	private List<Object[]> expendList;//选中列表
	
	private String applySn;//单据编号
	private Integer projectId;//项目Id
	private Integer year;//年份
	private Integer fundsSourceId;//资金来源id
	private Integer departmentId;;//部门id
	private Integer applyUser;//申请人id
	private String reciveCompany;//收款单位
	private String invoiceSn;//发票
	private String finaceAccountName;//财务账名
	private String summary;//摘要
	private Integer applayer;//申请人
	private String totalMoney;//已到账金额
	private String usedMoney;//已使用金额
	private String canUseMoney;//可使用金额
	private String projectJson;//json
	
	private String comment ;//备注
	private Float allMoney;//总金额
	private String applyTime;//申请时间
	private String registTime;//登记时间
	private Integer register;//登记人
	private JSONObject saveResult;
	@SuppressWarnings("unchecked")
	public void wire(){
		projectList = new ArrayList<Object[]>();
		reciveCompanyList = new ArrayList<Object[]>();
		applySn = "";
		projectId = -1;
		fundsSourceId = -1;
		departmentId = -1;
		applyUser = -1;
		reciveCompany = "";
		invoiceSn = "";
		projectJson = "";
		expendList = new ArrayList<Object[]>();
		Calendar calendar = Calendar.getInstance();
		year = calendar.get(Calendar.YEAR);
		
		
		//初始化人员
		wireBudgetPerson();
		
	}
	
	
	/**
	 * 弹出模态框初始化
	 */
	@SuppressWarnings("unchecked")
	public void modalInit(){
		fundsSourceList = new ArrayList<Object[]>();
		//初始化科室
		wireDepartmentInfo();
		//初始化资金来源
		String sourceSql = "select yfs.the_id,yfs.the_value from ys_funds_source yfs where yfs.deleted=0";
		List<Object[]> list = getEntityManager().createNativeQuery(sourceSql).getResultList();
		if(list.size() > 0){
			fundsSourceList = list;
		}
		//查询当前登陆人为项目预算人的支出项目
		StringBuffer projectSql = new StringBuffer();
		projectSql.append(" SELECT ");
		projectSql.append(" neboi.normal_expend_budget_order_id, ");
		projectSql.append(" ycp.the_value ");
		projectSql.append(" FROM normal_expend_budget_order_info neboi ");
		projectSql.append(" LEFT JOIN ys_convention_project ycp ON neboi.normal_project_id = ycp.the_id ");
		projectSql.append(" LEFT JOIN ys_convention_project_user ycpu on ycp.the_id = ycpu.convention_project_id ");
		projectSql.append(" where  neboi.sub_project_id is  null and neboi.is_new=1  and ycpu.user_info_id =");
		projectSql.append(sessionToken.getUserInfoId());
		projectSql.append(" UNION all  ");
		projectSql.append(" SELECT  ");
		projectSql.append(" neboi.normal_expend_budget_order_id,");
		projectSql.append(" ycpe.the_value ");
		projectSql.append(" FROM normal_expend_budget_order_info neboi ");
		projectSql.append(" LEFT JOIN ys_convention_project_extend ycpe ON neboi.sub_project_id= ycpe.the_id ");
		projectSql.append(" LEFT JOIN ys_convention_project ycp on ycp.the_id = ycpe.convention_project_id ");
		projectSql.append(" LEFT JOIN ys_convention_project_user ycpu ON ycp.the_id = ycpu.convention_project_id ");
		projectSql.append(" WHERE neboi.is_new = 1 AND ycpu.user_info_id =  ");
		projectSql.append(sessionToken.getUserInfoId());
		List<Object[]> list1 = getEntityManager().createNativeQuery(projectSql.toString()).getResultList();
		if(list1.size() > 0){
			projectList = list1;
		}
		totalMoney = "";//已到账金额
		usedMoney = "";//已使用金额
		canUseMoney = "";//可使用金额
		departmentId = -1;
		projectId = -1;
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
	
	/**
	 * 获取科室
	 */
	@SuppressWarnings("unchecked")
	private void wireDepartmentInfo() {
		if (departmentInfoList != null) {
			departmentInfoList.clear();
		} else {
			departmentInfoList = new ArrayList<Object[]>();
		}
		departmentInfoList.add(new Object[] { -1, "请选择" });
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
		/*if(null == expendList){
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
		expendList.add(obj);*/
	}
	
	/**
	 * 添加项目提交
	 */
	@SuppressWarnings("unchecked")
	public void addProject(){
		if(null == expendList){
			expendList = new ArrayList<Object[]>();
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
		Object[] obj = new Object[14];
		obj[0] = project[0];
		obj[1] = df.format(Double.parseDouble(project[1].toString()));
		obj[2] = totalMoney - nowMoney == 0 ? "0.00" : df.format(totalMoney - nowMoney);
		obj[3] = Double.parseDouble(project[2].toString()) == 0 ? "0.00" : df.format(Double.parseDouble(project[2].toString()));
		obj[4] = "";
		obj[5] = "";
		obj[6] = "";
		obj[7] = "";
		obj[8] = project[3];
		obj[9] = "";
		obj[10] = "";
		obj[11] = "";
		obj[12] = "";
		obj[13] = "";
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
		//申请单
		ExpendApplyInfo expendApplyInfo = new ExpendApplyInfo();
		expendApplyInfo.setExpendApplyCode(applySn);
		expendApplyInfo.setFinaceAccountName(finaceAccountName);
		expendApplyInfo.setYear(year.toString());
		expendApplyInfo.setApplyUserId(applyUser);
		expendApplyInfo.setReciveCompany(reciveCompany);
		expendApplyInfo.setInvoiceNum(invoiceSn);
		expendApplyInfo.setInsertUser(sessionToken.getUserInfoId());
		expendApplyInfo.setInsertTime(new Date());
		expendApplyInfo.setDeleted(false);
		expendApplyInfo.setSummary(summary);
		expendApplyInfo.setReimbursementer(applayer);
		try {
			expendApplyInfo.setApplyTime(sdf.parse(applyTime));
		} catch (ParseException e1) {
			saveResult.element("invoke_result", "INVOKE_FAILURE");
			saveResult.element("message", "操作失败！时间保存失败！");
			return ;
		}
		if(null != registTime && !registTime.equals("")){
			try {
				expendApplyInfo.setRegistTime(sdf.parse(registTime));
			} catch (ParseException e1) {
				saveResult.element("invoke_result", "INVOKE_FAILURE");
				saveResult.element("message", "操作失败！时间保存失败！");
				return ;
			}
		}
		
		if(null != comment && !comment.equals("")){
			expendApplyInfo.setComment(comment);
		}
		if(null != register && register != 0){
			expendApplyInfo.setRegister(register);
		}
		
		//配置流程信息
		UserInfo userInfo = getEntityManager().find(UserInfo.class, sessionToken.getUserInfoId());
		TaskOrder taskOrder = new TaskOrder();
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
				taskOrder.setOrderSn("");// 准备持久化（必填字段）
				getEntityManager().persist(taskOrder);// 执行持久化
				taskOrder.setOrderSn("CGZX-" + year + "-" + Assit.fillZero(taskOrder.getTaskOrderId(), (short) 9));// 生成订单号
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
		expendApplyInfo.setTaskOrderId(taskOrder.getTaskOrderId());
		expendApplyInfo.setOrderSn(taskOrder.getOrderSn());
		getEntityManager().persist(expendApplyInfo);
		if (saveResult != null) {
			saveResult.clear();
		} else {
			saveResult = new JSONObject();
		}
		saveResult.accumulate("invoke_result", "INVOKE_SUCCESS");
		saveResult.accumulate("message", "提交成功！");
		JSONObject jsonObject = JSONObject.fromObject(projectJson);
		JSONArray expendList = jsonObject.getJSONArray("expend_list");
		float allMoney = 0f;
		for(int i = 0;i < expendList.size();i++){
			//获取参数
			JSONObject project = expendList.getJSONObject(i);
			String projectInfoId = project.getString("project_id");
			String expendMoney = project.getString("expend_money");
			String expendTime = project.getString("expend_time");
			String expendComment = project.getString("expend_comment");
			String addMoney = project.getString("add_money");
			String budgetAdd = project.getString("budget_add");
			String budgetCut = project.getString("budget_cut");
			String appendPaid = project.getString("append_paid");
			String appendCanpay = project.getString("append_canpay");
			allMoney += Float.parseFloat(expendMoney);
			//保存支出申请单详细列表
			ExpendApplyProject eap = new ExpendApplyProject();
			eap.setExpendApplyInfoId(expendApplyInfo.getExpendApplyInfoId());
			eap.setNormalExpendBudgetOrderId(projectId);
			eap.setExpendMoney(Float.parseFloat(expendMoney));
			if(!addMoney.equals("")){
				eap.setBudgetAppendExpend(Float.parseFloat(addMoney));
			}
			if(!budgetAdd.equals("")){
				eap.setBudgetAdjustmentAppend(Float.parseFloat(budgetAdd));
			}
			if(!budgetCut.equals("")){
				eap.setBudgetAdjestmentCut(Float.parseFloat(budgetCut));
			}
			if(!appendPaid.equals("")){
				eap.setAppendBudgetPaid(Float.parseFloat(appendPaid));
			}
			if(!appendCanpay.equals("")){
				eap.setAppendBudgetCanCut(Float.parseFloat(appendCanpay));
			}
			try {
				eap.setExpendTime(sdf.parse(expendTime));
			} catch (ParseException e) {
				saveResult.element("invoke_result", "INVOKE_FAILURE");
				saveResult.element("message", "操作失败！时间保存失败！");
				return ;
			}
			eap.setComment(expendComment);
			eap.setInsertUser(sessionToken.getUserInfoId());
			eap.setInsertTime(new Date());
			eap.setDeleted(false);
			getEntityManager().persist(eap);
			expendApplyInfo.setTotalMoney(allMoney);
			getEntityManager().merge(expendApplyInfo);
			
			//to-do 预算下达金额减去支出金额
			
			//执行申请后再减掉金额
			NormalExpendBudgetOrderInfo neboi = getEntityManager().find(NormalExpendBudgetOrderInfo.class, Integer.parseInt(projectInfoId));
			neboi.setNowAmount(neboi.getNowAmount() - Double.parseDouble(expendMoney));
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
					parentProject.setNowAmount(parentProject.getNowAmount() -  Double.parseDouble(expendMoney));
					getEntityManager().merge(parentProject);
				}
			}
			
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
	
	/**
	 * 部门下拉列表改变
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public void selectDepart(){
		projectList = new ArrayList<Object[]>();
		projectId = -1;
		if(null != departmentId && -1 != departmentId){
			//查询当前登陆人为项目预算人的支出项目
			StringBuffer projectSql = new StringBuffer();
			projectSql.append(" SELECT ");
			projectSql.append(" neboi.normal_expend_budget_order_id, ");
			projectSql.append(" ycp.the_value ");
			projectSql.append(" FROM normal_expend_budget_order_info neboi ");
			projectSql.append(" LEFT JOIN ys_convention_project ycp ON neboi.normal_project_id = ycp.the_id ");
			projectSql.append(" LEFT JOIN ys_convention_project_user ycpu on ycp.the_id = ycpu.convention_project_id ");
			projectSql.append(" where  neboi.sub_project_id is  null and neboi.is_new=1  and ycpu.user_info_id =");
			projectSql.append(sessionToken.getUserInfoId());
			projectSql.append(" and ycp.department_info_id =");
			projectSql.append(departmentId);
			projectSql.append(" UNION all  ");
			projectSql.append(" SELECT  ");
			projectSql.append(" neboi.normal_expend_budget_order_id,");
			projectSql.append(" ycpe.the_value ");
			projectSql.append(" FROM normal_expend_budget_order_info neboi ");
			projectSql.append(" LEFT JOIN ys_convention_project_extend ycpe ON neboi.sub_project_id= ycpe.the_id ");
			projectSql.append(" LEFT JOIN ys_convention_project ycp on ycp.the_id = ycpe.convention_project_id ");
			projectSql.append(" LEFT JOIN ys_convention_project_user ycpu ON ycp.the_id = ycpu.convention_project_id ");
			projectSql.append(" WHERE neboi.is_new = 1 AND ycpu.user_info_id =  ");
			projectSql.append(sessionToken.getUserInfoId());
			projectSql.append(" and ycp.department_info_id =");
			projectSql.append(departmentId);
			List<Object[]> list = getEntityManager().createNativeQuery(projectSql.toString()).getResultList();
			if(list.size() > 0){
				projectList = list;
			}
		}
	}
	
	public List<Object[]> getDepartmentInfoList() {
		return departmentInfoList;
	}

	public void setDepartmentInfoList(List<Object[]> departmentInfoList) {
		this.departmentInfoList = departmentInfoList;
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


	public List<Object[]> getProjectList() {
		return projectList;
	}

	public void setProjectList(List<Object[]> projectList) {
		this.projectList = projectList;
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

	public String getFinaceAccountName() {
		return finaceAccountName;
	}

	public void setFinaceAccountName(String finaceAccountName) {
		this.finaceAccountName = finaceAccountName;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public Integer getApplayer() {
		return applayer;
	}

	public void setApplayer(Integer applayer) {
		this.applayer = applayer;
	}

	public String getTotalMoney() {
		return totalMoney;
	}

	public void setTotalMoney(String totalMoney) {
		this.totalMoney = totalMoney;
	}

	public String getUsedMoney() {
		return usedMoney;
	}

	public void setUsedMoney(String usedMoney) {
		this.usedMoney = usedMoney;
	}

	public String getCanUseMoney() {
		return canUseMoney;
	}

	public void setCanUseMoney(String canUseMoney) {
		this.canUseMoney = canUseMoney;
	}


	public String getComment() {
		return comment;
	}


	public void setComment(String comment) {
		this.comment = comment;
	}


	public Float getAllMoney() {
		return allMoney;
	}


	public void setAllMoney(Float allMoney) {
		this.allMoney = allMoney;
	}


	public String getApplyTime() {
		return applyTime;
	}


	public void setApplyTime(String applyTime) {
		this.applyTime = applyTime;
	}


	public String getRegistTime() {
		return registTime;
	}


	public void setRegistTime(String registTime) {
		this.registTime = registTime;
	}


	public Integer getRegister() {
		return register;
	}


	public void setRegister(Integer register) {
		this.register = register;
	}
	
	
}
