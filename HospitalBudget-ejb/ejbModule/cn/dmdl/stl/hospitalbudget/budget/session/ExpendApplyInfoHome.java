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
import cn.dmdl.stl.hospitalbudget.budget.entity.ExpendConfirmInfo;
import cn.dmdl.stl.hospitalbudget.budget.entity.ExpendConfirmProject;
import cn.dmdl.stl.hospitalbudget.budget.entity.NormalExpendPlantInfo;
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
	
	private Integer expendApplyInfoId;//编辑使用-申请单号
	private String applySn;//单据编号
	private Integer projectId;//项目Id
	private Integer projectType;//项目类型（常规项目2，项目1）
	private Integer year;//年份
	private Integer fundsSourceId;//资金来源id
	private Integer departmentId;;//部门id
	private Integer applyUser;//申请人id
	private String reciveCompany;//收款单位
	private String invoiceSn;//发票
	private String summary;//摘要
	private String totalMoney;//已到账金额
	private String usedMoney;//已使用金额
	private String canUseMoney;//可使用金额
	private String projectJson;//json
	private String reimbur;//报销人
	private String comment ;//备注
	private Float allMoney;//总金额
	private String applyTime;//申请时间
	private String registTime;//登记时间
	private Integer register;//登记人
	private JSONObject saveResult;
	private String expendAllMoney;//总支出金额
	private List<Object> companyList;//收款单位列表
	@SuppressWarnings("unchecked")
	public void wire(){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
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
		expendAllMoney = "";
		expendList = new ArrayList<Object[]>();
		Calendar calendar = Calendar.getInstance();
		year = calendar.get(Calendar.YEAR);
		if(null == expendApplyInfoId){
			expendApplyInfoId = 0;
		}
		//查询当前登陆人的科室
		String companySql = "select eai.recive_company from expend_apply_info eai LEFT JOIN user_info ui on ui.user_info_id=eai.insert_user where ui.department_info_id=" + sessionToken.getDepartmentInfoId();
		companyList = new ArrayList<Object>();
		List<Object> oldCompany = getEntityManager().createNativeQuery(companySql).getResultList();
		if(oldCompany.size() > 0){
			companyList = oldCompany;
		}
		//初始化人员
		wireBudgetPerson();
		
		if(null != expendApplyInfoId && expendApplyInfoId != 0){
			ExpendApplyInfo eai = getEntityManager().find(ExpendApplyInfo.class, expendApplyInfoId);
			year = Integer.parseInt(eai.getYear());
			applyUser = eai.getApplyUserId();
			applySn = eai.getExpendApplyCode();
			reciveCompany = eai.getReciveCompany();
			invoiceSn = eai.getInvoiceNum();
			summary = eai.getSummary();
			if(null != eai.getReimbursementer()){
				reimbur = eai.getReimbursementer();
			}else{
				reimbur = "";
			}
			if(null != eai.getRegister()){
				register = eai.getRegister();
			}else{
				register = 0;
			}
			applyTime = sdf.format(eai.getApplyTime());
			if(null != eai.getRegistTime() && !eai.getRegistTime().equals("")){
				registTime = sdf.format(eai.getRegistTime());
			}else{
				registTime = "";
			}
			if(null != eai.getComment()){
				comment = eai.getComment();
			}else{
				comment = "";
			}
			
			//项目列表
			StringBuffer projectSql = new StringBuffer();
			projectSql.append(" SELECT ");
			projectSql.append(" up.the_value as routine_name, ");//0
			projectSql.append(" expi.budget_amount as budget_amount1, ");//1
			projectSql.append(" expi.budget_amount_frozen as befor_frozen1,");//2
			projectSql.append(" expi.budget_amount_surplus as befor_surplus1, ");//3
			projectSql.append(" eap.expend_money as expend_money1, ");//4
			projectSql.append(" eap.project_id as routine_project, ");//5
			projectSql.append(" fs.the_value as source_name, ");//6
			projectSql.append(" 1, ");//7
			projectSql.append(" gp.the_value as generic_name, ");//8
			projectSql.append(" nepi.budget_amount as budget_amount2, ");//9
			projectSql.append(" nepi.budget_amount_frozen as befor_frozen2, ");//10
			projectSql.append(" nepi.budget_amount_surplus as befor_surplus2, ");//11
			projectSql.append(" eap.expend_money as expend_money2, ");//12
			projectSql.append(" eap.generic_project_id, ");//13
			projectSql.append(" fs2.the_value as source_name2, ");//14
			projectSql.append(" 2 ");//15
			projectSql.append(" FROM expend_apply_project eap ");
			projectSql.append(" LEFT JOIN expend_apply_info eai ON eap.expend_apply_info_id = eai.expend_apply_info_id ");
			projectSql.append(" LEFT JOIN routine_project up on up.the_id=eap.project_id ");
			projectSql.append(" LEFT JOIN normal_expend_plan_info expi on  expi.project_id=eap.project_id and expi.`year` = eai.`year` ");
			projectSql.append(" LEFT JOIN ys_funds_source fs on up.funds_source_id=fs.the_id ");
			
			projectSql.append(" LEFT JOIN generic_project gp ON gp.the_id = eap.generic_project_id ");
			projectSql.append(" LEFT JOIN normal_expend_plan_info nepi ON nepi.generic_project_id = eap.generic_project_id AND nepi.`year` = eai.`year` ");
			projectSql.append(" LEFT JOIN ys_funds_source fs2 ON gp.funds_source_id = fs2.the_id ");
			projectSql.append(" where eap.deleted=0 ");
			projectSql.append(" and eai.expend_apply_info_id=").append(expendApplyInfoId);
			System.out.println(projectSql);
			List<Object[]> list = getEntityManager().createNativeQuery("select * from (" + projectSql.toString() + ") as test").getResultList();
			float allMoney = 0f;
			if(list.size() > 0){
				for(Object[] obj : list){
					Object[] projectDetail = new Object[9];
					if(null != obj[5] && null == obj[13]){
						projectDetail[0] = obj[0];
						projectDetail[1] = obj[1];
						projectDetail[2] = obj[2];
						projectDetail[3] = obj[3];
						projectDetail[4] = obj[4];
						allMoney += Float.parseFloat(obj[4].toString());
						projectDetail[5] = obj[6].toString();
						projectDetail[6] = "";
						projectDetail[7] = obj[5];
						projectDetail[8] = obj[7];
						expendList.add(projectDetail);
					}else{
						projectDetail[0] = obj[8];
						projectDetail[1] = obj[9];
						projectDetail[2] = obj[10];
						projectDetail[3] = obj[11];
						projectDetail[4] = obj[12];
						allMoney += Float.parseFloat(obj[12].toString());
						projectDetail[5] = obj[14].toString();
						projectDetail[6] = "";
						projectDetail[7] = obj[13];
						projectDetail[8] = obj[15];
						expendList.add(projectDetail);
					}
				}
				expendAllMoney = allMoney + "";
			}
		}
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
		//查询当前登陆人为项目支出人的项目
		projectList = queryProjectByUser(year,-1,-1);
		
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
	public void selectProject(){
		if(null != projectId && -1 != projectId){
			if(projectType == 1){
				StringBuffer sql = new StringBuffer();
				sql.append(" SELECT ");
				sql.append(" nepi.dept_id, ");
				sql.append(" fs.the_id, ");
				sql.append(" nepi.budget_amount, ");
				sql.append(" nepi.budget_amount_frozen, ");
				sql.append(" nepi.budget_amount_surplus ");
				sql.append(" FROM normal_expend_plan_info nepi ");
				sql.append(" LEFT JOIN routine_project rp ON nepi.project_id = rp.the_id ");
				sql.append(" LEFT JOIN ys_funds_source fs on rp.funds_source_id=fs.the_id ");
				sql.append(" WHERE nepi.project_id= ").append(projectId);
				@SuppressWarnings("unchecked")
				List<Object[]> list = getEntityManager().createNativeQuery(sql.toString()).getResultList();
				if(list.size() > 0){
					Object[] obj = list.get(0);
					if(null == departmentId || departmentId == -1){
						departmentId = Integer.parseInt(obj[0].toString());
					}
					if(null == fundsSourceId || fundsSourceId == -1){
						fundsSourceId = Integer.parseInt(obj[1].toString());
					}
					if(null != obj[2]){
						totalMoney = obj[2].toString();
					}
					if(null != obj[3]){
						usedMoney = obj[3].toString();
					}
					if(null != obj[4]){
						canUseMoney = obj[4].toString();
					}
				}
			}else if(projectType == 2){
				StringBuffer sql = new StringBuffer();
				sql.append(" SELECT ");
				sql.append(" nepi.dept_id, ");
				sql.append(" fs.the_id, ");
				sql.append(" nepi.budget_amount, ");
				sql.append(" nepi.budget_amount_frozen, ");
				sql.append(" nepi.budget_amount_surplus ");
				sql.append(" FROM normal_expend_plan_info nepi ");
				sql.append(" LEFT JOIN generic_project rp ON nepi.generic_project_id = rp.the_id ");
				sql.append(" LEFT JOIN ys_funds_source fs on rp.funds_source_id=fs.the_id ");
				sql.append(" WHERE nepi.generic_project_id= ").append(projectId);
				@SuppressWarnings("unchecked")
				List<Object[]> list = getEntityManager().createNativeQuery(sql.toString()).getResultList();
				if(list.size() > 0){
					Object[] obj = list.get(0);
					if(null == departmentId || departmentId == -1){
						departmentId = Integer.parseInt(obj[0].toString());
					}
					if(null == fundsSourceId || fundsSourceId == -1){
						fundsSourceId = Integer.parseInt(obj[1].toString());
					}
					if(null != obj[2]){
						totalMoney = obj[2].toString();
					}
					if(null != obj[3]){
						usedMoney = obj[3].toString();
					}
					if(null != obj[4]){
						canUseMoney = obj[4].toString();
					}
				}
			}
		}else{
			totalMoney = "";//已到账金额
			usedMoney = "";//已使用金额
			canUseMoney = "";//可使用金额
		}
	}
	
	/**
	 * 添加项目提交
	 */
	@SuppressWarnings("unchecked")
	public void addProject(){
		if(null == expendList){
			expendList = new ArrayList<Object[]>();
		}
		StringBuffer sql = new StringBuffer();
		if(projectType == 1){
			sql.append(" SELECT ");
			sql.append(" up.the_value, ");
			sql.append(" nepi.budget_amount, ");
			sql.append(" nepi.budget_amount_frozen, ");
			sql.append(" nepi.budget_amount_surplus,up.the_id,fs.the_value as source_name ");
			sql.append(" FROM routine_project up ");
			sql.append(" LEFT JOIN normal_expend_plan_info nepi ON up.the_id = nepi.project_id ");
			sql.append(" LEFT JOIN ys_funds_source fs on up.funds_source_id=fs.the_id ");
			sql.append(" WHERE up.the_id = ").append(projectId);
			List<Object[]> list = getEntityManager().createNativeQuery("select * from (" + sql.toString() + ") as test").getResultList();
			Object[] project = list.get(0);
			DecimalFormat df = new DecimalFormat("#.00");
			Object[] obj = new Object[9];
			obj[0] = project[0];
			obj[1] =  Double.parseDouble(project[1].toString()) == 0 ? "0.00" : df.format(Double.parseDouble(project[1].toString()));
			obj[2] = Double.parseDouble(project[2].toString()) == 0 ? "0.00" : df.format(Double.parseDouble(project[2].toString()));
			obj[3] = Double.parseDouble(project[3].toString()) == 0 ? "0.00" : df.format(Double.parseDouble(project[3].toString()));
			obj[4] = "";
			obj[5] = project[5];
			obj[6] = "";
			obj[7] = project[4];
			obj[8] = 1;//项目类型
			expendList.add(obj);
		}else if(projectType == 2){
			sql.append(" SELECT ");
			sql.append(" up.the_value, ");
			sql.append(" nepi.budget_amount, ");
			sql.append(" nepi.budget_amount_frozen, ");
			sql.append(" nepi.budget_amount_surplus,up.the_id,fs.the_value as source_name ");
			sql.append(" FROM generic_project up ");
			sql.append(" LEFT JOIN normal_expend_plan_info nepi ON up.the_id = nepi.generic_project_id ");
			sql.append(" LEFT JOIN ys_funds_source fs on up.funds_source_id=fs.the_id ");
			sql.append(" WHERE up.the_id = ").append(projectId);
			List<Object[]> list = getEntityManager().createNativeQuery("select * from (" + sql.toString() + ") as test").getResultList();
			Object[] project = list.get(0);
			DecimalFormat df = new DecimalFormat("#.00");
			Object[] obj = new Object[9];
			obj[0] = project[0];
			obj[1] =  Double.parseDouble(project[1].toString()) == 0 ? "0.00" : df.format(Double.parseDouble(project[1].toString()));
			obj[2] = Double.parseDouble(project[2].toString()) == 0 ? "0.00" : df.format(Double.parseDouble(project[2].toString()));
			obj[3] = Double.parseDouble(project[3].toString()) == 0 ? "0.00" : df.format(Double.parseDouble(project[3].toString()));
			obj[4] = "";
			obj[5] = project[5];
			obj[6] = "";
			obj[7] = project[4];
			obj[8] = 2;//常规项目类型
			expendList.add(obj);
		}
		
	}
	/**
	 * 提交保存
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String save(){
		if (saveResult != null) {
			saveResult.clear();
		} else {
			saveResult = new JSONObject();
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		joinTransaction();
		//申请单
		ExpendApplyInfo expendApplyInfo = new ExpendApplyInfo();
		expendApplyInfo.setExpendApplyCode(applySn);
		expendApplyInfo.setYear(year.toString());
		expendApplyInfo.setApplyUserId(applyUser);
		expendApplyInfo.setReciveCompany(reciveCompany);
		expendApplyInfo.setInvoiceNum(invoiceSn);
		expendApplyInfo.setInsertUser(sessionToken.getUserInfoId());
		expendApplyInfo.setInsertTime(new Date());
		expendApplyInfo.setDeleted(false);
		expendApplyInfo.setSummary(summary);
		expendApplyInfo.setReimbursementer(reimbur);
		expendApplyInfo.setExpendApplyStatus(0);
		try {
			expendApplyInfo.setApplyTime(sdf.parse(applyTime));
		} catch (ParseException e1) {
			saveResult.element("invoke_result", "INVOKE_FAILURE");
			saveResult.element("message", "操作失败！时间保存失败！");
			return "no";
		}
		if(null != registTime && !registTime.equals("")){
			try {
				expendApplyInfo.setRegistTime(sdf.parse(registTime));
			} catch (ParseException e1) {
				saveResult.element("invoke_result", "INVOKE_FAILURE");
				saveResult.element("message", "操作失败！时间保存失败！");
				return  "no";
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
					return  "no";
				}
			} else {
				saveResult.element("invoke_result", "INVOKE_FAILURE");
				saveResult.element("message", "操作失败！请先完善科室[" + userInfo.getYsDepartmentInfo().getTheValue() + "]的步骤配置！");
				return  "no";
			}
		} else {
			saveResult.element("invoke_result", "INVOKE_FAILURE");
			saveResult.element("message", "操作失败！请先完善科室[" + userInfo.getYsDepartmentInfo().getTheValue() + "]的流程信息！");
			return  "no";
		}
		expendApplyInfo.setTaskOrderId(taskOrder.getTaskOrderId());
		expendApplyInfo.setOrderSn(taskOrder.getOrderSn());
		getEntityManager().persist(expendApplyInfo);
		//生成确认单
		ExpendConfirmInfo eci = new ExpendConfirmInfo();
		eci.setExpend_apply_info_id(expendApplyInfo.getExpendApplyInfoId());
		eci.setInsertUser(sessionToken.getUserInfoId());
		eci.setInsertTime(new Date());
		eci.setTotalMoney(0f);
		eci.setConfirm_status(0);
		eci.setYear(expendApplyInfo.getYear());
		eci.setDeleted(false);
		getEntityManager().persist(eci);
		
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
		//生成申请项目单和项目确认单
		for(int i = 0;i < expendList.size();i++){
			//获取参数
			JSONObject project = expendList.getJSONObject(i);
			String projectInfoId = project.getString("project_id");
			String expendMoney = project.getString("expend_money");
			String frozenMoney = project.getString("frozen_money");
			String surplusMoney = project.getString("project_surplus");
			String projectType = project.getString("project_type");
			allMoney += Float.parseFloat(expendMoney);
			//保存支出申请单详细列表
			ExpendApplyProject eap = new ExpendApplyProject();
			eap.setExpendApplyInfoId(expendApplyInfo.getExpendApplyInfoId());
			//如果是1，则为项目       2：常规项目
			if("1".equals(projectType)){
				eap.setProjectId(Integer.parseInt(projectInfoId));
			}else if("2".equals(projectType)){
				eap.setGenericProjectId(Integer.parseInt(projectInfoId));
			}
			
			eap.setExpendBeforFrozen(Float.parseFloat(frozenMoney));
			eap.setExpendBeforSurplus(Float.parseFloat(surplusMoney));
			eap.setExpendMoney(Float.parseFloat(expendMoney));
			eap.setInsertUser(sessionToken.getUserInfoId());
			eap.setInsertTime(new Date());
			eap.setDeleted(false);
			getEntityManager().persist(eap);
			
			
			//项目确认单
			ExpendConfirmProject efp = new ExpendConfirmProject();
			efp.setExpend_confirm_info_id(eci.getExpend_confirm_info_id());
			efp.setExpend_apply_project_id(eap.getExpendApplyProjectId());
			efp.setExpendBeforFrozen(eap.getExpendBeforFrozen());
			efp.setExpendBeforSurplus(eap.getExpendBeforSurplus());
			efp.setProjectId(eap.getProjectId());
			efp.setGenericProjectId(eap.getGenericProjectId());
			efp.setDeleted(false);
			getEntityManager().persist(efp);
			//to-do 预算下达金额减去支出金额
			
			//执行申请后再减掉金额
			/*StringBuffer moneySql = new StringBuffer();
			if("1".equals(projectType)){
				moneySql.append(" select normalExpendPlantInfo from NormalExpendPlantInfo normalExpendPlantInfo where normalExpendPlantInfo.projectId=");
				moneySql.append(projectInfoId).append(" and normalExpendPlantInfo.year= '").append(year).append("'");
			}else if("2".equals(projectType)){
				moneySql.append(" select normalExpendPlantInfo from NormalExpendPlantInfo normalExpendPlantInfo where normalExpendPlantInfo.genericProjectId=");
				moneySql.append(projectInfoId).append(" and normalExpendPlantInfo.year= '").append(year).append("'");
			}
			
			List<NormalExpendPlantInfo> normalExpendPlanInfoList = getEntityManager().createQuery(moneySql.toString()).getResultList();
			NormalExpendPlantInfo nxpi = normalExpendPlanInfoList.get(0);
			nxpi.setBudgetAmountFrozen(nxpi.getBudgetAmountFrozen() + Float.parseFloat(expendMoney));
			nxpi.setBudgetAmountSurplus(nxpi.getBudgetAmountSurplus() - Float.parseFloat(expendMoney));
			getEntityManager().merge(nxpi);*/
		}
		//保存申请单和确认单的总金额
		expendApplyInfo.setTotalMoney(allMoney);
		getEntityManager().merge(expendApplyInfo);
		eci.setTotalMoney(expendApplyInfo.getTotalMoney());
		getEntityManager().merge(eci);
		getEntityManager().flush();
		raiseAfterTransactionSuccessEvent();
		return "ok";
	}
	
	/**
	 * 提交保存
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String update(){
		if (saveResult != null) {
			saveResult.clear();
		} else {
			saveResult = new JSONObject();
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		joinTransaction();
		//申请单
		ExpendApplyInfo expendApplyInfo = getEntityManager().find(ExpendApplyInfo.class, expendApplyInfoId);
		//记录是否为驳回状态
		boolean isReturnFlag = false;
		if(expendApplyInfo.getExpendApplyStatus() == 2){
			isReturnFlag = true;
		}
		expendApplyInfo.setExpendApplyCode(applySn);
		expendApplyInfo.setYear(year.toString());
		expendApplyInfo.setApplyUserId(applyUser);
		expendApplyInfo.setReciveCompany(reciveCompany);
		expendApplyInfo.setInvoiceNum(invoiceSn);
		expendApplyInfo.setInsertUser(sessionToken.getUserInfoId());
		expendApplyInfo.setInsertTime(new Date());
		expendApplyInfo.setDeleted(false);
		expendApplyInfo.setSummary(summary);
		expendApplyInfo.setReimbursementer(reimbur);
		expendApplyInfo.setExpendApplyStatus(0);
		try {
			expendApplyInfo.setApplyTime(sdf.parse(applyTime));
		} catch (ParseException e1) {
			saveResult.element("invoke_result", "INVOKE_FAILURE");
			saveResult.element("message", "操作失败！时间保存失败！");
			return "no";
		}
		if(null != registTime && !registTime.equals("")){
			try {
				expendApplyInfo.setRegistTime(sdf.parse(registTime));
			} catch (ParseException e1) {
				saveResult.element("invoke_result", "INVOKE_FAILURE");
				saveResult.element("message", "操作失败！时间保存失败！");
				return "no";
			}
		}
		
		if(null != comment && !comment.equals("")){
			expendApplyInfo.setComment(comment);
		}
		if(null != register && register != 0){
			expendApplyInfo.setRegister(register);
		}
		getEntityManager().merge(expendApplyInfo);
		//如果为驳回状态就重新创建一个确认单，如果不是驳回状态则修改确认单、
		ExpendConfirmInfo eci = null;
		if(isReturnFlag){
			eci = new ExpendConfirmInfo();
			eci.setExpend_apply_info_id(expendApplyInfo.getExpendApplyInfoId());
			eci.setInsertUser(sessionToken.getUserInfoId());
			eci.setInsertTime(new Date());
			eci.setTotalMoney(0f);
			eci.setConfirm_status(0);
			eci.setYear(expendApplyInfo.getYear());
			eci.setDeleted(false);
			getEntityManager().persist(eci);
		}else{
			String confrimInfoSql = "select expendConfirmInfo from ExpendConfirmInfo expendConfirmInfo where expendConfirmInfo.deleted = 0 and expendConfirmInfo.expend_apply_info_id = " + expendApplyInfo.getExpendApplyInfoId();
			List<ExpendConfirmInfo> confirmList = getEntityManager().createQuery(confrimInfoSql).getResultList();
			eci = confirmList.get(0);
			eci.setExpend_apply_info_id(expendApplyInfo.getExpendApplyInfoId());
			eci.setInsertUser(sessionToken.getUserInfoId());
			eci.setInsertTime(new Date());
			eci.setTotalMoney(0f);
			eci.setConfirm_status(0);
			eci.setYear(expendApplyInfo.getYear());
			eci.setDeleted(false);
			getEntityManager().merge(eci);
		}
		saveResult.accumulate("invoke_result", "INVOKE_SUCCESS");
		saveResult.accumulate("message", "提交成功！");
		JSONObject jsonObject = JSONObject.fromObject(projectJson);
		JSONArray expendList = jsonObject.getJSONArray("expend_list");
		float allMoney = 0f;
		
		//查询编辑前的项目列表
		String oldApplySql = "select expendApplyProject from ExpendApplyProject expendApplyProject where expendApplyProject.deleted = 0 and expendApplyProject.expendApplyInfoId=" + expendApplyInfoId;
		List<ExpendApplyProject> oldList = getEntityManager().createQuery(oldApplySql).getResultList();
		//将编辑前的支出的钱还原回去，重新减掉一遍
		/*StringBuffer oldPlant = new StringBuffer();
		oldPlant.append(" SELECT ");
		oldPlant.append(" expi.normal_expend_plan_id, ");
		oldPlant.append(" expi.project_id, ");
		oldPlant.append(" eap.expend_money,1 ");
		oldPlant.append(" FROM normal_expend_plan_info expi ");
		oldPlant.append(" LEFT JOIN expend_apply_project eap ON expi.project_id = eap.project_id ");
		oldPlant.append(" LEFT JOIN expend_apply_info eai ON eai.expend_apply_info_id = eap.expend_apply_info_id ");
		oldPlant.append(" AND eai.`year` = expi.`year` ");
		oldPlant.append(" WHERE eap.deleted = 0 ");
		oldPlant.append(" and eai.expend_apply_info_id= ").append(expendApplyInfoId);
		oldPlant.append(" union all ");
		oldPlant.append(" SELECT ");
		oldPlant.append(" expi.normal_expend_plan_id, ");
		oldPlant.append(" expi.generic_project_id, ");
		oldPlant.append(" eap.expend_money,2 ");
		oldPlant.append(" FROM normal_expend_plan_info expi ");
		oldPlant.append(" LEFT JOIN expend_apply_project eap ON expi.generic_project_id = eap.generic_project_id ");
		oldPlant.append(" LEFT JOIN expend_apply_info eai ON eai.expend_apply_info_id = eap.expend_apply_info_id ");
		oldPlant.append(" AND eai.`year` = expi.`year` ");
		oldPlant.append(" WHERE eap.deleted = 0 ");
		oldPlant.append(" and eai.expend_apply_info_id= ").append(expendApplyInfoId);
		
		List<Object[]> oldPlantList = getEntityManager().createNativeQuery(oldPlant.toString()).getResultList();
		for(Object[] expendPlant : oldPlantList){
			NormalExpendPlantInfo normalExpendPlanInfo = getEntityManager().find(NormalExpendPlantInfo.class, Integer.parseInt(expendPlant[0].toString()));
			normalExpendPlanInfo.setBudgetAmountFrozen(normalExpendPlanInfo.getBudgetAmountFrozen() - Float.parseFloat(expendPlant[2].toString()));
			normalExpendPlanInfo.setBudgetAmountSurplus(normalExpendPlanInfo.getBudgetAmountSurplus() + Float.parseFloat(expendPlant[2].toString()));
			getEntityManager().merge(normalExpendPlanInfo);
		}*/
		//编辑的id
		List<Integer> existProject = new ArrayList<Integer>();
		for(int i = 0;i < expendList.size();i++){
			
			boolean editFlag = true;
			//获取参数
			JSONObject project = expendList.getJSONObject(i);
			String projectInfoId = project.getString("project_id");
			String projectType = project.getString("project_type");
			String frozenMoney = project.getString("frozen_money");
			String surplusMoney = project.getString("project_surplus");
			String expendMoney = project.getString("expend_money");
			allMoney += Float.parseFloat(expendMoney);
			//编辑支出申请单详细列表
			ExpendApplyProject eap = null;
			if(null != oldList && oldList.size() > 0 && oldList.size() >= (i+1)){
				eap = oldList.get(i);
				existProject.add(eap.getExpendApplyProjectId());
				editFlag = true;
			}else{
				eap = new ExpendApplyProject();
				editFlag = false;
			}
			if("1".equals(projectType)){
				eap.setProjectId(Integer.parseInt(projectInfoId));
			}else{
				eap.setGenericProjectId(Integer.parseInt(projectInfoId));
			}
			eap.setExpendApplyInfoId(expendApplyInfo.getExpendApplyInfoId());
			eap.setExpendBeforFrozen(Float.parseFloat(frozenMoney));
			eap.setExpendBeforSurplus(Float.parseFloat(surplusMoney));
			eap.setExpendMoney(Float.parseFloat(expendMoney));
			eap.setDeleted(false);
			if(!editFlag){
				eap.setInsertUser(sessionToken.getUserInfoId());
				eap.setInsertTime(new Date());
				getEntityManager().persist(eap);
			}else{
				eap.setUpdateUser(sessionToken.getUserInfoId());
				eap.setUpdateTime(new Date());
				getEntityManager().merge(eap);
			}
			
			//如果为驳回状态就重新创建一个确认单，如果不是驳回状态则修改确认单、
			ExpendConfirmProject efp = null;
			if(isReturnFlag){
				efp = new ExpendConfirmProject();
				efp.setExpend_confirm_info_id(eci.getExpend_confirm_info_id());
				efp.setExpend_apply_project_id(eap.getExpendApplyProjectId());
				if("1".equals(projectType)){
					efp.setProjectId(eap.getProjectId());
				}else{
					efp.setGenericProjectId(eap.getProjectId());
				}
				efp.setExpendBeforFrozen(eap.getExpendBeforFrozen());
				efp.setExpendBeforSurplus(eap.getExpendBeforSurplus());
				efp.setDeleted(false);
				getEntityManager().persist(efp);
			}else{
				if(editFlag){
					String efpHql = "select expendConfirmProject from ExpendConfirmProject expendConfirmProject where expendConfirmProject.expend_apply_project_id=" + eap.getExpendApplyProjectId(); 
					List<ExpendConfirmProject> efpList = getEntityManager().createQuery(efpHql).getResultList();
					efp = efpList.get(0);
					efp.setExpend_confirm_info_id(eci.getExpend_confirm_info_id());
					efp.setExpend_apply_project_id(eap.getExpendApplyProjectId());
					if("1".equals(projectType)){
						efp.setProjectId(eap.getProjectId());
					}else{
						efp.setGenericProjectId(eap.getGenericProjectId());
					}
					efp.setDeleted(false);
					efp.setExpendBeforFrozen(eap.getExpendBeforFrozen());
					efp.setExpendBeforSurplus(eap.getExpendBeforSurplus());
					getEntityManager().merge(efp);
				}else{
					efp = new ExpendConfirmProject();
					efp.setExpend_confirm_info_id(eci.getExpend_confirm_info_id());
					efp.setExpend_apply_project_id(eap.getExpendApplyProjectId());
					if("1".equals(projectType)){
						efp.setProjectId(eap.getProjectId());
					}else{
						efp.setGenericProjectId(eap.getProjectId());
					}
					efp.setDeleted(false);
					efp.setExpendBeforFrozen(eap.getExpendBeforFrozen());
					efp.setExpendBeforSurplus(eap.getExpendBeforSurplus());
					getEntityManager().persist(efp);
				}
			}
			
			//to-do 预算下达金额减去支出金额
			//执行申请后再减掉金额
			/*StringBuffer moneySql = new StringBuffer();
			if("1".equals(projectType)){
				moneySql.append(" select normalExpendPlantInfo from NormalExpendPlantInfo normalExpendPlantInfo where normalExpendPlantInfo.projectId=");
				moneySql.append(projectInfoId).append(" and normalExpendPlantInfo.year= '").append(year).append("'");
			}else{
				moneySql.append(" select normalExpendPlantInfo from NormalExpendPlantInfo normalExpendPlantInfo where normalExpendPlantInfo.genericProjectId=");
				moneySql.append(projectInfoId).append(" and normalExpendPlantInfo.year= '").append(year).append("'");
			}
			
			List<NormalExpendPlantInfo> normalExpendPlanInfoList = getEntityManager().createQuery(moneySql.toString()).getResultList();
			NormalExpendPlantInfo nxpi = normalExpendPlanInfoList.get(0);
			nxpi.setBudgetAmountFrozen(nxpi.getBudgetAmountFrozen() + Float.parseFloat(expendMoney));
			nxpi.setBudgetAmountSurplus(nxpi.getBudgetAmountSurplus() - Float.parseFloat(expendMoney));
			getEntityManager().merge(nxpi);*/
			
		}
		expendApplyInfo.setTotalMoney(allMoney);
		getEntityManager().merge(expendApplyInfo);
		eci.setTotalMoney(expendApplyInfo.getTotalMoney());
		getEntityManager().merge(eci);
		for(ExpendApplyProject oldEap : oldList){	
			boolean existflag = false;
			for(Integer id : existProject){
				if(id.intValue() == oldEap.getExpendApplyProjectId().intValue()){
					existflag = true;
				}
			}
			if(!existflag){
				oldEap.setDeleted(true);
				getEntityManager().merge(oldEap);
			}
		}
		getEntityManager().flush();
		raiseAfterTransactionSuccessEvent();
		expendApplyInfoId = 0;
		return "ok";
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
	public void selectDepart(){
		projectList = new ArrayList<Object[]>();
		//查询当前登陆人为项目预算人的支出项目
		projectList = queryProjectByUser(year,departmentId,fundsSourceId);
		//是否当前选择的项目在集合中
		boolean flag = false;
		for(Object[] obj : projectList){
			if(Integer.parseInt(obj[1].toString()) == projectId){
				flag = true;
			}
		}
		if(!flag){
			totalMoney = "";//已到账金额
			usedMoney = "";//已使用金额
			canUseMoney = "";//可使用金额
		}
	}
	
	/**
	 * 资金来源下拉列表改变
	 * @return
	 */
	public void selectFoundsSource(){
		projectList = new ArrayList<Object[]>();
		//查询当前登陆人为项目预算人的支出项目
		projectList = queryProjectByUser(year,departmentId,fundsSourceId);
		//是否当前选择的项目在集合中
		boolean flag = false;
		for(Object[] obj : projectList){
			if(Integer.parseInt(obj[1].toString()) == projectId){
				flag = true;
			}
		}
		if(!flag){
			totalMoney = "";//已到账金额
			usedMoney = "";//已使用金额
			canUseMoney = "";//可使用金额
		}
	}
	//删除
	@SuppressWarnings("unchecked")
	public String remove(){
		joinTransaction();
		if(null != expendApplyInfoId && expendApplyInfoId != 0){
			ExpendApplyInfo eai = getEntityManager().find(ExpendApplyInfo.class, expendApplyInfoId);
			eai.setDeleted(true);
			getEntityManager().merge(eai);
			
			String projectSql = "select expendApplyProject from ExpendApplyProject expendApplyProject where expendApplyProject.deleted = 0 and expendApplyProject.expendApplyInfoId=" + expendApplyInfoId;
			List<ExpendApplyProject> projectList = getEntityManager().createQuery(projectSql).getResultList();
			for(ExpendApplyProject eap : projectList){
				eap.setDeleted(true);
				getEntityManager().merge(eap);
			}
			//将编辑前的支出的钱还原回去，重新减掉一遍
			StringBuffer oldPlant = new StringBuffer();
			oldPlant.append(" SELECT ");
			oldPlant.append(" expi.normal_expend_plan_id, ");
			oldPlant.append(" expi.project_id, ");
			oldPlant.append(" eap.expend_money ");
			oldPlant.append(" FROM normal_expend_plan_info expi ");
			oldPlant.append(" LEFT JOIN expend_apply_project eap ON expi.project_id = eap.project_id ");
			oldPlant.append(" LEFT JOIN expend_apply_info eai ON eai.expend_apply_info_id = eap.expend_apply_info_id ");
			oldPlant.append(" AND eai.`year` = expi.`year` ");
			oldPlant.append(" WHERE eap.deleted = 0 ");
			oldPlant.append(" and eai.expend_apply_info_id= ").append(expendApplyInfoId);
			oldPlant.append(" union all ");
			oldPlant.append(" SELECT ");
			oldPlant.append(" expi.normal_expend_plan_id, ");
			oldPlant.append(" expi.generic_project_id, ");
			oldPlant.append(" eap.expend_money ");
			oldPlant.append(" FROM normal_expend_plan_info expi ");
			oldPlant.append(" LEFT JOIN expend_apply_project eap ON expi.generic_project_id = eap.generic_project_id ");
			oldPlant.append(" LEFT JOIN expend_apply_info eai ON eai.expend_apply_info_id = eap.expend_apply_info_id ");
			oldPlant.append(" AND eai.`year` = expi.`year` ");
			oldPlant.append(" WHERE eap.deleted = 0 ");
			oldPlant.append(" and eai.expend_apply_info_id= ").append(expendApplyInfoId);
			List<Object[]> oldPlantList = getEntityManager().createNativeQuery(oldPlant.toString()).getResultList();
			for(Object[] expendPlant : oldPlantList){
				NormalExpendPlantInfo normalExpendPlanInfo = getEntityManager().find(NormalExpendPlantInfo.class, Integer.parseInt(expendPlant[0].toString()));
				normalExpendPlanInfo.setBudgetAmountFrozen(normalExpendPlanInfo.getBudgetAmountFrozen() - Float.parseFloat(expendPlant[2].toString()));
				normalExpendPlanInfo.setBudgetAmountSurplus(normalExpendPlanInfo.getBudgetAmountSurplus() + Float.parseFloat(expendPlant[2].toString()));
				getEntityManager().merge(normalExpendPlanInfo);
			}
		}
		
		getEntityManager().flush();
		raiseAfterTransactionSuccessEvent();
		expendApplyInfoId = 0;
		return "ok";
	}
	
	//查询该登陆人为支出人的收入项目
	@SuppressWarnings("unchecked")
	public List<Object[]> queryProjectByUser(int year,int departId,int sourceId){
		List<Object[]> projectList = new ArrayList<Object[]>();
		StringBuffer projectSql = new StringBuffer();
		projectSql.append(" SELECT ");
		projectSql.append(" nepi.normal_expend_plan_id, ");
		projectSql.append(" rp.the_id, ");
		projectSql.append(" rp.the_value, ");
		projectSql.append(" 1 ");
		projectSql.append(" FROM normal_expend_plan_info nepi ");
		projectSql.append(" LEFT JOIN routine_project rp ON nepi.project_id = rp.the_id ");
		projectSql.append(" LEFT JOIN routine_project_executor rpe ON rpe.project_id = rp.the_id ");
		projectSql.append(" WHERE rpe.user_info_id =  ");
		projectSql.append(sessionToken.getUserInfoId());
		projectSql.append(" and nepi.`year` =");
		projectSql.append(year);
		if(-1 != departId){
			projectSql.append(" and nepi.dept_id= ").append(departId);
		}
		if(-1 != sourceId){
			projectSql.append(" and rp.funds_source_id =  ").append(sourceId);
		}
		projectSql.append(" union all ");
		projectSql.append(" SELECT ");
		projectSql.append(" nepi.normal_expend_plan_id, ");
		projectSql.append(" gp.the_id, ");
		projectSql.append(" gp.the_value, ");
		projectSql.append(" 2 ");
		projectSql.append(" FROM normal_expend_plan_info nepi ");
		projectSql.append(" LEFT JOIN generic_project gp ON nepi.generic_project_id = gp.the_id ");
		projectSql.append(" LEFT JOIN generic_project_executor gpe ON gpe.project_id = gp.the_id ");
		projectSql.append(" WHERE gpe.user_info_id =  ");
		projectSql.append(sessionToken.getUserInfoId());
		projectSql.append(" and nepi.`year` =");
		projectSql.append(year);
		if(-1 != departId){
			projectSql.append(" and nepi.dept_id= ").append(departId);
		}
		if(-1 != sourceId){
			projectSql.append(" and gp.funds_source_id =  ").append(sourceId);
		}
		List<Object[]> list = getEntityManager().createNativeQuery(projectSql.toString()).getResultList();
		
		if(list.size() > 0 ){
			//如果列表中已经有了就移除掉
			for(Object[] obj : list){
				boolean flag = false;
				for(Object[] exist : expendList){
					if(exist[7].toString().equals(obj[1].toString()) && exist[8].toString().equals(obj[3].toString())){
						flag = true;
					}
				}
				if(!flag){
					projectList.add(obj);	
				}
			}
		}
		return projectList;
	}
	
	//递归查询当前登陆人所在部门的额最高部门
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

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
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

	public Integer getExpendApplyInfoId() {
		return expendApplyInfoId;
	}

	public void setExpendApplyInfoId(Integer expendApplyInfoId) {
		this.expendApplyInfoId = expendApplyInfoId;
	}

	public String getExpendAllMoney() {
		return expendAllMoney;
	}

	public void setExpendAllMoney(String expendAllMoney) {
		this.expendAllMoney = expendAllMoney;
	}

	public Integer getProjectType() {
		return projectType;
	}

	public void setProjectType(Integer projectType) {
		this.projectType = projectType;
	}

	public String getReimbur() {
		return reimbur;
	}

	public void setReimbur(String reimbur) {
		this.reimbur = reimbur;
	}

	public List<Object> getCompanyList() {
		return companyList;
	}

	public void setCompanyList(List<Object> companyList) {
		this.companyList = companyList;
	}

}
