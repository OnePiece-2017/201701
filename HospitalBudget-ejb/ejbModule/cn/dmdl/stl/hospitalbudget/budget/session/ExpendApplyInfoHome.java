package cn.dmdl.stl.hospitalbudget.budget.session;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.admin.entity.UserInfo;
import cn.dmdl.stl.hospitalbudget.budget.entity.ExpendApplyInfo;
import cn.dmdl.stl.hospitalbudget.budget.entity.ExpendApplyProject;
import cn.dmdl.stl.hospitalbudget.budget.entity.ExpendConfirmInfo;
import cn.dmdl.stl.hospitalbudget.budget.entity.ExpendConfirmProject;
import cn.dmdl.stl.hospitalbudget.budget.entity.GenericProject;
import cn.dmdl.stl.hospitalbudget.budget.entity.NormalExpendPlantInfo;
import cn.dmdl.stl.hospitalbudget.budget.entity.ProcessStepInfo;
import cn.dmdl.stl.hospitalbudget.budget.entity.RoutineProject;
import cn.dmdl.stl.hospitalbudget.budget.entity.TaskOrder;
import cn.dmdl.stl.hospitalbudget.budget.entity.TaskUser;
import cn.dmdl.stl.hospitalbudget.budget.entity.YsExpandApplyProcessLog;
import cn.dmdl.stl.hospitalbudget.common.session.CriterionEntityHome;
import cn.dmdl.stl.hospitalbudget.hospital.entity.YsDepartmentInfo;
import cn.dmdl.stl.hospitalbudget.hospital.entity.YsProjectNature;
import cn.dmdl.stl.hospitalbudget.util.Assit;
import cn.dmdl.stl.hospitalbudget.util.NumberUtil;
import cn.dmdl.stl.hospitalbudget.util.SqlServerJDBCUtil;

@Name("expendApplyInfoHome")
public class ExpendApplyInfoHome extends CriterionEntityHome<ExpendApplyInfo>{
	private static final long serialVersionUID = 1L;
	private List<Object[]> departmentInfoList;// 科室select
	private List<Object[]> applayUserList;//报销人列表
	private List<Object[]> fundsSourceList;//资金来源列表
	private List<Object[]> reciveCompanyList;//收款单位列表
	private List<Object[]> projectList;//主项目列表
	private List<Object[]> expendList;//选中列表
	private List<Object[]> contractList;//合同列表
	private JSONObject contractJson;
	
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
	private Double allMoney;//总金额
	private String applyTime;//申请时间
	private String registTime;//登记时间
	private Integer register;//登记人
	private JSONObject saveResult;
	private String expendAllMoney;//总支出金额
	private List<Object> companyList;//收款单位列表
	private Integer contractId;//合同id
	
	//审核
	private String checkItems;  //审核的选项
	@SuppressWarnings("unchecked")
	public void wire(){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		projectList = new ArrayList<Object[]>();
		reciveCompanyList = new ArrayList<Object[]>();
		applySn = queryNextCode();
		projectId = -1;
		fundsSourceId = -1;
		departmentId = -1;
		applyUser = -1;
		reciveCompany = "";
		invoiceSn = "";
		projectJson = "";
		expendAllMoney = "";
		expendList = new ArrayList<Object[]>();
		applyTime = sdf.format(new Date());
		reimbur = sessionToken.getUsername();
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
			projectSql.append(" 2,eap.attachment,nepi.normal_expend_plan_id  ");//15
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
			double allMoney = 0d;
			if(list.size() > 0){
				for(Object[] obj : list){
					Object[] projectDetail = new Object[11];
					if(null != obj[5] && null == obj[13]){
						double usedMoney = getExpendInfoMoneyOfEdit(1,Integer.valueOf(obj[5].toString()),expendApplyInfoId);
						projectDetail[0] = obj[0];
						BigDecimal bd1 = new BigDecimal(Double.parseDouble(obj[1].toString()));
						projectDetail[1] = bd1.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString();
						BigDecimal bd2 = new BigDecimal(Double.parseDouble(obj[2].toString()) + usedMoney);
						projectDetail[2] = bd2.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString();
						BigDecimal bd3 = new BigDecimal(Double.parseDouble(obj[3].toString()) + usedMoney);
						projectDetail[3] = bd3.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString();
						BigDecimal bd4 = new BigDecimal(Double.parseDouble(obj[4].toString()));
						projectDetail[4] = bd4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString();
						allMoney += Double.parseDouble(obj[4].toString());
						projectDetail[5] = obj[6].toString();
						projectDetail[6] = "";
						projectDetail[7] = obj[5];
						projectDetail[8] = obj[7];
						projectDetail[9] = obj[16];
						projectDetail[10] = obj[17];
						expendList.add(projectDetail);
					}else{
						double usedMoney = getExpendInfoMoneyOfEdit(2,Integer.valueOf(obj[13].toString()),expendApplyInfoId);
						projectDetail[0] = obj[8];
						BigDecimal bd1 = new BigDecimal(Double.parseDouble(obj[9].toString()));
						projectDetail[1] = bd1.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString();
						BigDecimal bd2 = new BigDecimal(Double.parseDouble(obj[10].toString()) + usedMoney);
						projectDetail[2] = bd2.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString();
						BigDecimal bd3 = new BigDecimal(Double.parseDouble(obj[11].toString()) - usedMoney);
						projectDetail[3] = bd3.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString();
						BigDecimal bd4 = new BigDecimal(Double.parseDouble(obj[12].toString()));
						projectDetail[4] = bd4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString();
						allMoney += Double.parseDouble(obj[12].toString());
						projectDetail[5] = obj[14].toString();
						projectDetail[6] = "";
						projectDetail[7] = obj[13];
						projectDetail[8] = obj[15];
						projectDetail[9] = obj[16];
						projectDetail[10] = obj[17];
						expendList.add(projectDetail);
					}
				}
				expendAllMoney = allMoney + "";
			}
		}
	}
	
	
	@SuppressWarnings("unchecked")
	public String queryNextCode(){
		String firstCode = "EP";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		String secondCode = sdf.format(new Date());
		String thirdCode;
		String sql = "select eai.expend_apply_code from expend_apply_info eai where eai.expend_apply_code like '" + firstCode + secondCode + "%' ORDER BY eai.expend_apply_info_id desc";
		List<Object> list = getEntityManager().createNativeQuery(sql).getResultList();
		if(null != list && list.size() > 0){
			String lastCode = list.get(0).toString();
			String last = lastCode.substring(10,lastCode.length());
			int lc = Integer.parseInt(last);
			if(lc >= 1000){
				thirdCode = (Integer.parseInt(last) + 1) + "";
			}else{
				thirdCode = String.format("%0" + 4 + "d", Integer.parseInt(last) + 1);
			}
		}else{
			thirdCode = "0001";
		}
		return firstCode + secondCode + thirdCode;
	}
	
	/**
	 * 弹出模态框初始化
	 */
	@SuppressWarnings("unchecked")
	public void modalInit(){
		fundsSourceList = new ArrayList<Object[]>();
		//初始化科室
		departmentInfoList = queryDepartOfExpendUser(year,-1,-1);
		//初始化资金来源
		String sourceSql = "select yfs.the_id,yfs.the_value from ys_funds_source yfs where yfs.deleted=0";
		List<Object[]> list = getEntityManager().createNativeQuery(sourceSql).getResultList();
		if(list.size() > 0){
			fundsSourceList = list;
		}
		//查询当前登陆人为项目支出人的项目
		projectList = queryProjectByUser(year,-1,-1,1);
		fundsSourceId = 1;
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
	 * 删除申请单
	 */
	public String deleteApply(){
		if(null != expendApplyInfoId){
			ExpendApplyInfo e = getEntityManager().find(ExpendApplyInfo.class, expendApplyInfoId);
			if(null != e){
				joinTransaction();
				e.setDeleted(true);
				getEntityManager().merge(e);
				getEntityManager().flush();
				raiseAfterTransactionSuccessEvent();
			}
		}
		return "removed";
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
		if(null == contractJson){
			contractJson = new JSONObject();
		}
		contractJson.element("is_audit", false);
		contractList = new ArrayList<Object[]>();
		double money = getExpendInfoMoney(projectType,projectId);
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
						BigDecimal bd1 = new BigDecimal(Double.parseDouble(obj[2].toString()));
						totalMoney = bd1.setScale(1, BigDecimal.ROUND_HALF_UP).toPlainString();
					}
					if(null != obj[3]){
						BigDecimal bd1 = new BigDecimal(Double.parseDouble(obj[3].toString()) + money);
						usedMoney = bd1.setScale(1, BigDecimal.ROUND_HALF_UP).toPlainString();
					}
					if(null != obj[4]){
						BigDecimal bd1 = new BigDecimal(Double.parseDouble(obj[4].toString()) - money);
						canUseMoney = bd1.setScale(1, BigDecimal.ROUND_HALF_UP).toPlainString();
					}
				}
			}else if(projectType == 2){
				StringBuffer sql = new StringBuffer();
				sql.append(" SELECT ");
				sql.append(" nepi.dept_id, ");
				sql.append(" fs.the_id, ");
				sql.append(" nepi.budget_amount, ");
				sql.append(" nepi.budget_amount_frozen, ");
				sql.append(" nepi.budget_amount_surplus, ");
				sql.append(" rp.is_audit, ");
				sql.append(" FROM normal_expend_plan_info nepi ");
				sql.append(" LEFT JOIN generic_project rp ON nepi.generic_project_id = rp.the_id ");
				sql.append(" LEFT JOIN ys_funds_source fs on rp.funds_source_id=fs.the_id ");
				sql.append(" WHERE nepi.generic_project_id= ").append(projectId);
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
						BigDecimal bd1 = new BigDecimal(Double.parseDouble(obj[2].toString()));
						totalMoney = bd1.setScale(1, BigDecimal.ROUND_HALF_UP).toPlainString();
					}
					if(null != obj[3]){
						BigDecimal bd1 = new BigDecimal(Double.parseDouble(obj[3].toString())+ money);
						usedMoney = bd1.setScale(1, BigDecimal.ROUND_HALF_UP).toPlainString();
					}
					if(null != obj[4]){
						BigDecimal bd1 = new BigDecimal(Double.parseDouble(obj[4].toString())- money);
						canUseMoney = bd1.setScale(1, BigDecimal.ROUND_HALF_UP).toPlainString();
						
					}
					if(null != obj[5] && Boolean.parseBoolean(obj[5].toString())){
						contractJson.element("is_audit", true);
						//查询该项目的合同
						Integer deptId = sessionToken.getDepartmentInfoId();
						if(deptId != null){
							String contractSql = "select yaci.audit_contract_info_id,yaci.audit_title from ys_audit_contract_info yaci where yaci.deleted=0 "
									+ "and yaci.dept_id=? and yaci.generic_project_id=?";
							List<Object[]> clist = getEntityManager().createNativeQuery(contractSql).setParameter(1, deptId).setParameter(2, projectId).getResultList();
							if(null != clist && clist.size() > 0){
								contractId = Integer.parseInt(clist.get(0)[0].toString());
								contractJson.element("contract_id", contractId);
								contractList = clist;
							}
						}
					}
				}
				
			}
		}else{
			totalMoney = "";//已到账金额
			usedMoney = "";//已使用金额
			canUseMoney = "";//可使用金额
		}
		
		//查询合同
		//列表
//		String contractSql = "select yaci.audit_contract_info_id,yaci.audit_title from ys_audit_contract_info yaci where yaci.deleted=0";
//		List<Object[]> clist = getEntityManager().createNativeQuery(contractSql).getResultList();
//		if(null != clist && clist.size() > 0){
//			contractList = clist;
//		}else{
//			contractList = new ArrayList<Object[]>();
//		}
		
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
		double money1 = getExpendInfoMoney(projectType,projectId);
		if(projectType == 1){
			sql.append(" SELECT ");
			sql.append(" up.the_value, ");
			sql.append(" nepi.budget_amount, ");
			sql.append(" nepi.budget_amount_frozen, ");
			sql.append(" nepi.budget_amount_surplus,up.the_id,fs.the_value as source_name, ");
			sql.append(" nepi.normal_expend_plan_id ");
			sql.append(" FROM routine_project up ");
			sql.append(" LEFT JOIN normal_expend_plan_info nepi ON up.the_id = nepi.project_id ");
			sql.append(" LEFT JOIN ys_funds_source fs on up.funds_source_id=fs.the_id ");
			sql.append(" WHERE up.the_id = ").append(projectId);
			List<Object[]> list = getEntityManager().createNativeQuery("select * from (" + sql.toString() + ") as test").getResultList();
			Object[] project = list.get(0);
			DecimalFormat df = new DecimalFormat("#.00");
			Object[] obj = new Object[11];
			obj[0] = project[0];
			/*obj[1] =  Double.parseDouble(project[1].toString()) == 0 ? "0.00" : df.format(Double.parseDouble(project[1].toString()));
			obj[2] = Double.parseDouble(project[2].toString())+money1 == 0 ? "0.00" : df.format(Double.parseDouble(project[2].toString()) +money1);
			obj[3] = Double.parseDouble(project[3].toString())-money1 == 0 ? "0.00" : df.format(Double.parseDouble(project[3].toString()) - money1);*/
			obj[1] = NumberUtil.formatDouble2(project[1]);
			obj[2] = NumberUtil.formatDouble2(Double.parseDouble(project[2].toString())+money1);
			obj[3] = NumberUtil.formatDouble2(Double.parseDouble(project[3].toString())-money1);
			obj[4] = "";
			obj[5] = project[5];
			obj[6] = "";
			obj[7] = project[4];
			obj[8] = 1;//项目类型
			obj[10] = project[6];;//主键
			expendList.add(obj);
		}else if(projectType == 2){
			sql.append(" SELECT ");
			sql.append(" up.the_value, ");
			sql.append(" nepi.budget_amount, ");
			sql.append(" nepi.budget_amount_frozen, ");
			sql.append(" nepi.budget_amount_surplus,up.the_id,fs.the_value as source_name, ");
			sql.append(" nepi.normal_expend_plan_id ");
			sql.append(" FROM generic_project up ");
			sql.append(" LEFT JOIN normal_expend_plan_info nepi ON up.the_id = nepi.generic_project_id ");
			sql.append(" LEFT JOIN ys_funds_source fs on up.funds_source_id=fs.the_id ");
			sql.append(" WHERE up.the_id = ").append(projectId);
			List<Object[]> list = getEntityManager().createNativeQuery("select * from (" + sql.toString() + ") as test").getResultList();
			Object[] project = list.get(0);
			DecimalFormat df = new DecimalFormat("#.00");
			Object[] obj = new Object[11];
			obj[0] = project[0];
			/*obj[1] =  Double.parseDouble(project[1].toString()) == 0 ? "0.00" : df.format(Double.parseDouble(project[1].toString()));
			obj[2] = Double.parseDouble(project[2].toString())+money1 == 0 ? "0.00" : df.format(Double.parseDouble(project[2].toString())+money1);
			obj[3] = Double.parseDouble(project[3].toString())-money1 == 0 ? "0.00" : df.format(Double.parseDouble(project[3].toString())-money1);*/
			obj[1] = NumberUtil.double2Str(project[1]);
			obj[2] = NumberUtil.double2Str(Double.parseDouble(project[2].toString())+money1);
			obj[3] = NumberUtil.double2Str(Double.parseDouble(project[3].toString())-money1);
			obj[4] = "";
			obj[5] = project[5];
			obj[6] = "";
			obj[7] = project[4];
			obj[8] = 2;//常规项目类型
			obj[10] = project[6];;//主键
			expendList.add(obj);
		}
		
		System.out.println(projectJson);
		JSONObject oldJson = JSONObject.fromObject(projectJson);
		JSONArray jsonArr = oldJson.getJSONArray("expend_list");
		for(int i=0; i<jsonArr.size(); i++){
			JSONObject json = jsonArr.getJSONObject(i);
			String code = json.getString("attachment");
			String money = json.getString("expend_money");
			expendList.get(i)[4] = money;
			expendList.get(i)[9] = code;
		}
	}
	/**
	 * 提交保存
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String save(){
		String haocaiId = "";
		if (saveResult != null) {
			saveResult.clear();
		} else {
			saveResult = new JSONObject();
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		//校验申请单号是否已经存在
		String checkSql = "select * from expend_apply_info where expend_apply_code='" + applySn + "'";
		List<Object[]> checkList = getEntityManager().createNativeQuery(checkSql).getResultList();
		if(null != checkList && checkList.size() > 0){
			applySn = queryNextCode();
			saveResult.accumulate("invoke_result", "INVOKE_FAIL");
			saveResult.accumulate("message", "编码已经存在，已为您刷新编码！请重新提交");
			//saveResult.accumulate("code",applySn);
			return "";
		}
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
		
		JSONObject jsonObject = JSONObject.fromObject(projectJson);
		JSONArray expendList = jsonObject.getJSONArray("expend_list");
		//获取第一个，用来找出科室&&项目类型，来寻找流程信息
		JSONObject firstProject = expendList.getJSONObject(0);
		String tempProjectInfoId = firstProject.getString("project_id");
		String tempProjectType = firstProject.getString("project_type");
		Integer taskProjectType = Integer.valueOf(tempProjectType);
		YsDepartmentInfo taskDepart = null;
		if(tempProjectType.equals("1")){
			RoutineProject routineProject =  getEntityManager().find(RoutineProject.class,Integer.valueOf(tempProjectInfoId));
			taskDepart = routineProject.getYsDepartmentInfo();
		}else if(tempProjectType.equals("2")){
			GenericProject roject =  getEntityManager().find(GenericProject.class,Integer.valueOf(tempProjectInfoId));
			taskDepart = roject.getYsDepartmentInfo();
		}
		//配置流程信息
		UserInfo userInfo = getEntityManager().find(UserInfo.class, sessionToken.getUserInfoId());
		List<Object> processInfoList = getEntityManager().createNativeQuery("select process_info_id from process_info where deleted = 0 and process_type = "+taskProjectType+" and project_process_type = 4 and dept_id = " + taskDepart.getTheId()).getResultList();// 获取当前登录人所属部门的常规项目流程的常规支出执行流程
		if (processInfoList != null && processInfoList.size() > 0) {// 有流程信息
			List<Object> processStepInfoList = getEntityManager().createNativeQuery("select process_step_info_id from process_step_info where step_index = 1 and process_info_id = " + processInfoList.get(0).toString()).getResultList();// 获取流程配置中的第一步
			if (processStepInfoList != null && processStepInfoList.size() > 0) {// 有步骤配置
				int processStepInfoId = Integer.parseInt(processStepInfoList.get(0).toString());
				//保存申请单
				expendApplyInfo.setTaskOrderId(0);
				expendApplyInfo.setOrderSn("");
				getEntityManager().persist(expendApplyInfo);
				//保存流程
				YsExpandApplyProcessLog process = new YsExpandApplyProcessLog();
				process.setYsExpandApplyId(expendApplyInfo.getExpendApplyInfoId());
				process.setProcessStepInfoId(processStepInfoId);
				getEntityManager().persist(process);
			} else {
				saveResult.element("invoke_result", "INVOKE_FAILURE");
				saveResult.element("message", "操作失败！请先完善科室[" + userInfo.getYsDepartmentInfo().getTheValue() + "]的步骤配置！");
				return  "no";
			}
			
			Double allMoney = 0d;
			//生成申请项目单和项目确认单
			for(int i = 0;i < expendList.size();i++){
				//获取参数
				JSONObject project = expendList.getJSONObject(i);
				String projectInfoId = project.getString("project_id");
				String expendMoney = project.getString("expend_money");
				String frozenMoney = project.getString("frozen_money");
				String surplusMoney = project.getString("project_surplus");
				String projectType = project.getString("project_type");
				String attachment = project.getString("attachment");
				String nepiId = project.getString("nepiId");
				allMoney += Double.parseDouble(expendMoney);
				//如果有耗材的，得去计算剩余多少钱
				haocaiId += nepiId + ",";
				//保存支出申请单详细列表
				ExpendApplyProject eap = new ExpendApplyProject();
				eap.setExpendApplyInfoId(expendApplyInfo.getExpendApplyInfoId());
				//如果是1，则为项目       2：常规项目
				if("1".equals(projectType)){
					eap.setProjectId(Integer.parseInt(projectInfoId));
				}else if("2".equals(projectType)){
					eap.setGenericProjectId(Integer.parseInt(projectInfoId));
				}
				
				eap.setExpendBeforFrozen(Double.parseDouble(frozenMoney));
				eap.setExpendBeforSurplus(Double.parseDouble(surplusMoney));
				eap.setExpendMoney(Double.parseDouble(expendMoney));
				eap.setInsertUser(sessionToken.getUserInfoId());
				eap.setInsertTime(new Date());
				eap.setDeleted(false);
				if(null != attachment && !"undefined".equals(attachment) && !"null".equals(attachment)){
					eap.setAttachment(attachment);
				}
				getEntityManager().persist(eap);
				
				//更新冻结的钱
//				NormalExpendPlantInfo nepi = getEntityManager().find(NormalExpendPlantInfo.class, Integer.valueOf(nepiId));
//				nepi.setBudgetAmountFrozen((nepi.getBudgetAmountFrozen() == null ? 0:nepi.getBudgetAmountFrozen()) + Double.parseDouble(expendMoney));
//				nepi.setBudgetAmountSurplus(nepi.getBudgetAmountSurplus() - Double.parseDouble(expendMoney));
//				getEntityManager().merge(nepi);
			}
			//保存申请单和确认单的总金额
			expendApplyInfo.setTotalMoney(allMoney);
			getEntityManager().merge(expendApplyInfo);
			
		} else {//如果没有流程，直接进入确认
			expendApplyInfo.setTaskOrderId(0);
			expendApplyInfo.setOrderSn("");
			getEntityManager().persist(expendApplyInfo);
			
			//生成确认单
			ExpendConfirmInfo eci = new ExpendConfirmInfo();
			eci.setExpend_apply_info_id(expendApplyInfo.getExpendApplyInfoId());
			eci.setInsertUser(sessionToken.getUserInfoId());
			eci.setInsertTime(new Date());
			eci.setTotalMoney(0d);
			eci.setConfirm_status(0);
			eci.setYear(expendApplyInfo.getYear());
			eci.setDeleted(false);
			getEntityManager().persist(eci);
			
			Double allMoney = 0d;
			//生成申请项目单和项目确认单
			for(int i = 0;i < expendList.size();i++){
				//获取参数
				JSONObject project = expendList.getJSONObject(i);
				String projectInfoId = project.getString("project_id");
				String expendMoney = project.getString("expend_money");
				String frozenMoney = project.getString("frozen_money");
				String surplusMoney = project.getString("project_surplus");
				String projectType = project.getString("project_type");
				String attachment = project.getString("attachment");
				allMoney += Double.parseDouble(expendMoney);
				String nepiId = project.getString("nepiId");
				
				//如果有耗材的，得去计算剩余多少钱
				haocaiId += nepiId + ",";
				
				//保存支出申请单详细列表
				ExpendApplyProject eap = new ExpendApplyProject();
				eap.setExpendApplyInfoId(expendApplyInfo.getExpendApplyInfoId());
				//如果是1，则为项目       2：常规项目
				if("1".equals(projectType)){
					eap.setProjectId(Integer.parseInt(projectInfoId));
				}else if("2".equals(projectType)){
					eap.setGenericProjectId(Integer.parseInt(projectInfoId));
				}
				
				eap.setExpendBeforFrozen(Double.parseDouble(frozenMoney));
				eap.setExpendBeforSurplus(Double.parseDouble(surplusMoney));
				eap.setExpendMoney(Double.parseDouble(expendMoney));
				eap.setInsertUser(sessionToken.getUserInfoId());
				eap.setInsertTime(new Date());
				eap.setDeleted(false);
				if(null != attachment && !"undefined".equals(attachment) && !"null".equals(attachment)){
					eap.setAttachment(attachment);
				}
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
				efp.setConfirm_money(eap.getExpendMoney());
				getEntityManager().persist(efp);
				//to-do 预算下达金额减去支出金额
				
				//执行申请后再减掉金额
				//更新冻结的钱
//				NormalExpendPlantInfo nepi = getEntityManager().find(NormalExpendPlantInfo.class, Integer.valueOf(nepiId));
//				nepi.setBudgetAmountFrozen((nepi.getBudgetAmountFrozen() == null ? 0:nepi.getBudgetAmountFrozen()) + Double.parseDouble(expendMoney));
//				nepi.setBudgetAmountSurplus(nepi.getBudgetAmountSurplus() - Double.parseDouble(expendMoney));
//				getEntityManager().merge(nepi);
			}
			//保存申请单和确认单的总金额
			expendApplyInfo.setTotalMoney(allMoney);
			getEntityManager().merge(expendApplyInfo);
			eci.setTotalMoney(expendApplyInfo.getTotalMoney());
			getEntityManager().merge(eci);
		}
		
		//如果有耗材的，给新系统那边的钱重新计算
		String[] haocaiIds = haocaiId.substring(0, haocaiId.length()-1).split(",");
		for(String haocai : haocaiIds){
			NormalExpendPlantInfo nepi = getEntityManager().find(NormalExpendPlantInfo.class, Integer.valueOf(haocai));
			SqlServerJDBCUtil.calculateOldBudg(haocai,
					getExpendInfoMoney(nepi.getProjectId() == null ? 2:1,nepi.getProjectId() == null ? nepi.getGenericProjectId():nepi.getProjectId()));
		}
		if (saveResult != null) {
			saveResult.clear();
		} else {
			saveResult = new JSONObject();
		}
		saveResult.accumulate("invoke_result", "INVOKE_SUCCESS");
		saveResult.accumulate("message", "提交成功！");
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
		String haocaiId = "";
		if (saveResult != null) {
			saveResult.clear();
		} else {
			saveResult = new JSONObject();
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		joinTransaction();
		//申请单
		ExpendApplyInfo expendApplyInfo = getEntityManager().find(ExpendApplyInfo.class, expendApplyInfoId);
		int oldApplyStatus = expendApplyInfo.getExpendApplyStatus();
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
		//查询流程
		JSONObject jsonObject = JSONObject.fromObject(projectJson);
		JSONArray expendList = jsonObject.getJSONArray("expend_list");
		//获取第一个，用来找出科室&&项目类型，来寻找流程信息
		JSONObject firstProject = expendList.getJSONObject(0);
		String tempProjectInfoId = firstProject.getString("project_id");
		String tempProjectType = firstProject.getString("project_type");
		Integer taskProjectType = Integer.valueOf(tempProjectType);
		YsDepartmentInfo taskDepart = null;
		if(tempProjectType.equals("1")){
			RoutineProject routineProject =  getEntityManager().find(RoutineProject.class,Integer.valueOf(tempProjectInfoId));
			taskDepart = routineProject.getYsDepartmentInfo();
		}else if(tempProjectType.equals("2")){
			GenericProject roject =  getEntityManager().find(GenericProject.class,Integer.valueOf(tempProjectInfoId));
			taskDepart = roject.getYsDepartmentInfo();
		}
		Integer processStepInfoId = null;
		List<Object> processInfoList = getEntityManager().createNativeQuery("select process_info_id from process_info where deleted = 0 and process_type = "+taskProjectType+" and project_process_type = 4 and dept_id = " + taskDepart.getTheId()).getResultList();// 获取当前登录人所属部门的常规项目流程的常规支出执行流程
		if (processInfoList != null && processInfoList.size() > 0) {// 有流程信息
			List<Object> processStepInfoList = getEntityManager().createNativeQuery("select process_step_info_id from process_step_info where step_index = 1 and process_info_id = " + processInfoList.get(0).toString()).getResultList();// 获取流程配置中的第一步
			if (processStepInfoList != null && processStepInfoList.size() > 0) {// 有步骤配置
				processStepInfoId = Integer.parseInt(processStepInfoList.get(0).toString());
			}
		}
		//如果为驳回状态就重新创建一个确认单，如果不是驳回状态则修改确认单、
		ExpendConfirmInfo eci = null;
		if(oldApplyStatus == 4){
			if(processStepInfoId == null){
				eci = new ExpendConfirmInfo();
				eci.setExpend_apply_info_id(expendApplyInfo.getExpendApplyInfoId());
				eci.setInsertUser(sessionToken.getUserInfoId());
				eci.setInsertTime(new Date());
				eci.setTotalMoney(0d);
				eci.setConfirm_status(0);
				eci.setYear(expendApplyInfo.getYear());
				eci.setDeleted(false);
				getEntityManager().persist(eci);
			}
		}else{
			String confrimInfoSql = "select expendConfirmInfo from ExpendConfirmInfo expendConfirmInfo where expendConfirmInfo.deleted = 0 and expendConfirmInfo.confirm_status=0 and expendConfirmInfo.expend_apply_info_id = " + expendApplyInfo.getExpendApplyInfoId();
			List<ExpendConfirmInfo> confirmList = getEntityManager().createQuery(confrimInfoSql).getResultList();
			if(null != confirmList && confirmList.size() > 0){
				eci = confirmList.get(0);
				eci.setExpend_apply_info_id(expendApplyInfo.getExpendApplyInfoId());
				eci.setInsertUser(sessionToken.getUserInfoId());
				eci.setInsertTime(new Date());
				eci.setTotalMoney(0d);
				eci.setConfirm_status(0);
				eci.setYear(expendApplyInfo.getYear());
				eci.setDeleted(false);
				getEntityManager().merge(eci);
			}
		}
		saveResult.accumulate("invoke_result", "INVOKE_SUCCESS");
		saveResult.accumulate("message", "提交成功！");
		Double allMoney = 0d;
		//查询编辑前的项目列表
		String oldApplySql = "select expendApplyProject from ExpendApplyProject expendApplyProject where expendApplyProject.deleted = 0 and expendApplyProject.expendApplyInfoId=" + expendApplyInfoId;
		List<ExpendApplyProject> oldList = getEntityManager().createQuery(oldApplySql).getResultList();
		
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
			String attachment = project.getString("attachment");
			allMoney += Double.parseDouble(expendMoney);
			
			
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
			String nepiId = null;
			if("1".equals(projectType)){
				eap.setProjectId(Integer.parseInt(projectInfoId));
				String sql = "select nepi.normal_expend_plan_id from normal_expend_plan_info nepi where nepi.project_id=" + projectInfoId;
				List<Object> nepiObj = getEntityManager().createNativeQuery(sql).getResultList();
				if(null != nepiObj && nepiObj.size() > 0){
					nepiId = nepiObj.get(0).toString();
				}
			}else{
				eap.setGenericProjectId(Integer.parseInt(projectInfoId));
				String sql = "select nepi.normal_expend_plan_id from normal_expend_plan_info nepi where nepi.generic_project_id=" + projectInfoId;
				List<Object> nepiObj = getEntityManager().createNativeQuery(sql).getResultList();
				if(null != nepiObj && nepiObj.size() > 0){
					nepiId = nepiObj.get(0).toString();
				}
			}
			//如果有耗材的，得去计算剩余多少钱
			if(null != nepiId){
				haocaiId += nepiId + ",";
			}
			
			eap.setExpendApplyInfoId(expendApplyInfo.getExpendApplyInfoId());
			eap.setExpendBeforFrozen(Double.parseDouble(frozenMoney));
			eap.setExpendBeforSurplus(Double.parseDouble(surplusMoney));
			eap.setExpendMoney(Double.parseDouble(expendMoney));
			if(null != attachment && !"undefined".equals(attachment) && !"null".equals(attachment)){
				eap.setAttachment(attachment);
			}
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
			if(oldApplyStatus == 0 ){
				if(null != eci){
					if(editFlag){//如果是编辑的数据，则查看又没有确认单，有就更新
						String efpHql = "select expendConfirmProject from ExpendConfirmProject expendConfirmProject where expendConfirmProject.expend_apply_project_id=" + eap.getExpendApplyProjectId() + " and expendConfirmProject.expend_confirm_info_id=" + eci.getExpend_confirm_info_id(); 
						List<ExpendConfirmProject> efpList = getEntityManager().createQuery(efpHql).getResultList();
						if(null != efpList && efpList.size() > 0){
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
						}
					}else{
						if(null != eci){
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
							efp.setConfirm_money(eap.getExpendMoney());
							getEntityManager().persist(efp);
						}
					}
				}
				
			}else if(oldApplyStatus == 4){//确认驳回
				if(null != eci){
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
					efp.setConfirm_money(eap.getExpendMoney());
					getEntityManager().persist(efp);
				}
				if(null != processStepInfoId){
					YsExpandApplyProcessLog process = new YsExpandApplyProcessLog();
					process.setYsExpandApplyId(expendApplyInfo.getExpendApplyInfoId());
					process.setProcessStepInfoId(processStepInfoId);
					getEntityManager().persist(process);
				}
			}else if(oldApplyStatus == 3){//审核驳回则直接创建流程
				//保存流程
				YsExpandApplyProcessLog process = new YsExpandApplyProcessLog();
				process.setYsExpandApplyId(expendApplyInfo.getExpendApplyInfoId());
				process.setProcessStepInfoId(processStepInfoId);
				getEntityManager().persist(process);
			}
		}
		expendApplyInfo.setTotalMoney(allMoney);
		getEntityManager().merge(expendApplyInfo);
		if(null != eci){
			eci.setTotalMoney(expendApplyInfo.getTotalMoney());
			getEntityManager().merge(eci);
		}
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
		//如果有耗材的，给新系统那边的钱重新计算
		String[] haocaiIds = haocaiId.substring(0, haocaiId.length()-1).split(",");
		for(String haocai : haocaiIds){
			NormalExpendPlantInfo nepi = getEntityManager().find(NormalExpendPlantInfo.class, Integer.valueOf(haocai));
			SqlServerJDBCUtil.calculateOldBudg(haocai,
					getExpendInfoMoney(nepi.getProjectId() == null ? 2:1,nepi.getProjectId() == null ? nepi.getGenericProjectId():nepi.getProjectId()));
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
		projectList = queryProjectByUser(year,departmentId,fundsSourceId,projectType);
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
		projectList = queryProjectByUser(year,departmentId,fundsSourceId,projectType);
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
//	//删除
//	@SuppressWarnings("unchecked")
//	public String remove(){
//		joinTransaction();
//		if(null != expendApplyInfoId && expendApplyInfoId != 0){
//			ExpendApplyInfo eai = getEntityManager().find(ExpendApplyInfo.class, expendApplyInfoId);
//			eai.setDeleted(true);
//			getEntityManager().merge(eai);
//			
//			String projectSql = "select expendApplyProject from ExpendApplyProject expendApplyProject where expendApplyProject.deleted = 0 and expendApplyProject.expendApplyInfoId=" + expendApplyInfoId;
//			List<ExpendApplyProject> projectList = getEntityManager().createQuery(projectSql).getResultList();
//			for(ExpendApplyProject eap : projectList){
//				eap.setDeleted(true);
//				getEntityManager().merge(eap);
//			}
//			//将编辑前的支出的钱还原回去，重新减掉一遍
//			StringBuffer oldPlant = new StringBuffer();
//			oldPlant.append(" SELECT ");
//			oldPlant.append(" expi.normal_expend_plan_id, ");
//			oldPlant.append(" expi.project_id, ");
//			oldPlant.append(" eap.expend_money ");
//			oldPlant.append(" FROM normal_expend_plan_info expi ");
//			oldPlant.append(" LEFT JOIN expend_apply_project eap ON expi.project_id = eap.project_id ");
//			oldPlant.append(" LEFT JOIN expend_apply_info eai ON eai.expend_apply_info_id = eap.expend_apply_info_id ");
//			oldPlant.append(" AND eai.`year` = expi.`year` ");
//			oldPlant.append(" WHERE eap.deleted = 0 ");
//			oldPlant.append(" and eai.expend_apply_info_id= ").append(expendApplyInfoId);
//			oldPlant.append(" union all ");
//			oldPlant.append(" SELECT ");
//			oldPlant.append(" expi.normal_expend_plan_id, ");
//			oldPlant.append(" expi.generic_project_id, ");
//			oldPlant.append(" eap.expend_money ");
//			oldPlant.append(" FROM normal_expend_plan_info expi ");
//			oldPlant.append(" LEFT JOIN expend_apply_project eap ON expi.generic_project_id = eap.generic_project_id ");
//			oldPlant.append(" LEFT JOIN expend_apply_info eai ON eai.expend_apply_info_id = eap.expend_apply_info_id ");
//			oldPlant.append(" AND eai.`year` = expi.`year` ");
//			oldPlant.append(" WHERE eap.deleted = 0 ");
//			oldPlant.append(" and eai.expend_apply_info_id= ").append(expendApplyInfoId);
//			List<Object[]> oldPlantList = getEntityManager().createNativeQuery(oldPlant.toString()).getResultList();
//			for(Object[] expendPlant : oldPlantList){
//				NormalExpendPlantInfo normalExpendPlanInfo = getEntityManager().find(NormalExpendPlantInfo.class, Integer.parseInt(expendPlant[0].toString()));
//				normalExpendPlanInfo.setBudgetAmountFrozen(normalExpendPlanInfo.getBudgetAmountFrozen() - Double.parseDouble(expendPlant[2].toString()));
//				normalExpendPlanInfo.setBudgetAmountSurplus(normalExpendPlanInfo.getBudgetAmountSurplus() + Double.parseDouble(expendPlant[2].toString()));
//				getEntityManager().merge(normalExpendPlanInfo);
//			}
//		}
//		
//		getEntityManager().flush();
//		raiseAfterTransactionSuccessEvent();
//		expendApplyInfoId = 0;
//		return "ok";
//	}
	
	//查询该登陆人为支出人的收入项目
	@SuppressWarnings("unchecked")
	public List<Object[]> queryProjectByUser(int year,int departId,int sourceId,int projectType){
		List<Object[]> projectList = new ArrayList<Object[]>();
		StringBuffer projectSql = new StringBuffer();
		if(projectType == 1){//常规项目
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
		}else if(projectType == 2){//项目
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
	
	/**
	 * 查询当前登录人作为支出人的项目的科室
	 * @param year
	 * @param departId
	 * @param sourceId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Object[]> queryDepartOfExpendUser(int year,int departId,int sourceId){
		//固定有两个科室  信息中心和建设规划    还有他自己的
		List<Object[]> DepartList = new ArrayList<Object[]>();
		Object[] informationDep = new Object[2];
		informationDep[0] = 218;
		informationDep[1] = "信息中心";
		DepartList.add(informationDep);
		Object[] constructDep = new Object[2];
		constructDep[0] = 198;
		constructDep[1] = "规划建设处";
		DepartList.add(constructDep);
		Set<String> existSet = new HashSet<String>();
		existSet.add("218");
		existSet.add("198");
		
		//本部门
		if(1 != sessionToken.getUserInfoId() && null != sessionToken.getDepartmentInfoId()){
			YsDepartmentInfo departmentInfo = getEntityManager().find(YsDepartmentInfo.class, sessionToken.getDepartmentInfoId());
			Object[] thisDep = new Object[2];
			thisDep[0] = departmentInfo.getTheId();
			thisDep[1] = departmentInfo.getTheValue();
			DepartList.add(thisDep);
			existSet.add(departmentInfo.getTheId() + "");
		}
		StringBuffer projectSql = new StringBuffer();
		projectSql.append(" SELECT ");
		projectSql.append(" nepi.normal_expend_plan_id, ");
		projectSql.append(" di.the_id, ");
		projectSql.append(" di.the_value ");
		projectSql.append(" FROM normal_expend_plan_info nepi ");
		projectSql.append(" LEFT JOIN routine_project rp ON nepi.project_id = rp.the_id ");
		projectSql.append(" LEFT JOIN routine_project_executor rpe ON rpe.project_id = rp.the_id ");
		projectSql.append(" LEFT JOIN ys_department_info di on di.the_id=rp.department_info_id ");
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
		projectSql.append(" di.the_id, ");
		projectSql.append(" di.the_value ");
		projectSql.append(" FROM normal_expend_plan_info nepi ");
		projectSql.append(" LEFT JOIN generic_project gp ON nepi.generic_project_id = gp.the_id ");
		projectSql.append(" LEFT JOIN generic_project_executor gpe ON gpe.project_id = gp.the_id ");
		projectSql.append(" LEFT JOIN ys_department_info di on di.the_id=gp.department_info_id  ");
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
			for(Object[] obj : list){
				if(!existSet.contains(obj[1].toString())){
					Object[] tempDep = new Object[2];
					tempDep[0] = obj[1];
					tempDep[1] = obj[2];
					DepartList.add(tempDep);
					existSet.add(obj[1].toString());
				}
			}
		}
		return DepartList;
	}
	
	
	//审核
	/**
	 * 批量确认
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String confirmBatch(){
		saveResult = new JSONObject();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		saveResult.accumulate("invoke_result", "INVOKE_SUCCESS");
		saveResult.accumulate("message", "提交成功！");
		String[] ids = checkItems.split(",");
		for(int j=0;j<ids.length;j++){
			int expendConfirmId = Integer.parseInt(ids[j]);
			applyCheck(expendConfirmId,1);
		}
		return "ok";
	}
	/**
	 * 批量驳回
	 * @return
	 */
	public String projectReject(){
		saveResult = new JSONObject();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		saveResult.accumulate("invoke_result", "INVOKE_SUCCESS");
		saveResult.accumulate("message", "提交成功！");
		String[] ids = checkItems.split(",");
		for(int j=0;j<ids.length;j++){
			int expendConfirmId = Integer.parseInt(ids[j]);
			applyCheck(expendConfirmId,2);
		}
		return "ok";
	}
	
	/**
	 * 审核
	 * @param processLogId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public boolean applyCheck(Integer processLogId,int status){
		try{
			joinTransaction();
			YsExpandApplyProcessLog processLog = getEntityManager().find(YsExpandApplyProcessLog.class, processLogId);
			UserInfo userInfo = getEntityManager().find(UserInfo.class, sessionToken.getUserInfoId());
			if(status == 1){ //通过
				ProcessStepInfo processStepInfo = getEntityManager().find(ProcessStepInfo.class, processLog.getProcessStepInfoId());
				processLog.setOperateType(0);
				processLog.setUserId(userInfo.getUserInfoId());
				getEntityManager().merge(processLog);
				List<Object> processInfoList = getEntityManager().createNativeQuery("select p.process_info_id from process_step_info p where p.process_step_info_id="+processStepInfo.getProcessStepInfoId()).getResultList();// 获取流程配置中的第一步
				List<Object> processStepInfoList = getEntityManager().createNativeQuery("select process_step_info_id from process_step_info where process_info_id = " + Integer.valueOf(processInfoList.get(0).toString()) + " order by step_index asc ").getResultList();// 获取流程配置中的第一步
				int index = 0;
				for(int i=0;i<processStepInfoList.size();i++){
					if(Integer.valueOf(processStepInfoList.get(i).toString()) == processLog.getProcessStepInfoId().intValue()){
						index = i;
					}
				}
				Integer nextProcessStepId = null;
				if(index < processStepInfoList.size()-1){
					nextProcessStepId = Integer.valueOf(processStepInfoList.get(index+1).toString());
				}
				ExpendApplyInfo expendApplyInfo = getEntityManager().find(ExpendApplyInfo.class, processLog.getYsExpandApplyId());
				if(null == nextProcessStepId){//如果没有下一步，则进入确认表
					expendApplyInfo.setExpendApplyStatus(2);
					getEntityManager().merge(expendApplyInfo);
					List<ExpendApplyProject> expendApplyProjectList = getEntityManager().createQuery("select e from ExpendApplyProject e where e.expendApplyInfoId=" + expendApplyInfo.getExpendApplyInfoId() +
							" and e.deleted=0").getResultList();
					//生成确认单
					ExpendConfirmInfo eci = new ExpendConfirmInfo();
					eci.setExpend_apply_info_id(expendApplyInfo.getExpendApplyInfoId());
					eci.setInsertUser(sessionToken.getUserInfoId());
					eci.setInsertTime(new Date());
					eci.setTotalMoney(0d);
					eci.setConfirm_status(0);
					eci.setYear(expendApplyInfo.getYear());
					eci.setDeleted(false);
					getEntityManager().persist(eci);
					
					for(ExpendApplyProject project : expendApplyProjectList){
						//项目确认单
						ExpendConfirmProject efp = new ExpendConfirmProject();
						efp.setExpend_confirm_info_id(eci.getExpend_confirm_info_id());
						efp.setExpend_apply_project_id(project.getExpendApplyProjectId());
						efp.setExpendBeforFrozen(project.getExpendBeforFrozen());
						efp.setExpendBeforSurplus(project.getExpendBeforSurplus());
						efp.setProjectId(project.getProjectId());
						efp.setGenericProjectId(project.getGenericProjectId());
						efp.setDeleted(false);
						efp.setConfirm_money(project.getExpendMoney());
						getEntityManager().persist(efp);
					}
				}else{
					expendApplyInfo.setExpendApplyStatus(1);
					getEntityManager().merge(expendApplyInfo);
					//下一步
					YsExpandApplyProcessLog process = new YsExpandApplyProcessLog();
					process.setYsExpandApplyId(expendApplyInfo.getExpendApplyInfoId());
					process.setProcessStepInfoId(nextProcessStepId);
					getEntityManager().persist(process);
				}
			}else{
				processLog.setOperateType(1);
				processLog.setUserId(userInfo.getUserInfoId());
				getEntityManager().merge(processLog);
				ExpendApplyInfo expendApplyInfo = getEntityManager().find(ExpendApplyInfo.class, processLog.getYsExpandApplyId());
				expendApplyInfo.setExpendApplyStatus(3);
				getEntityManager().merge(expendApplyInfo);
//				//钱还回去
//				List<ExpendApplyProject> expendApplyProjectList = getEntityManager().createQuery("select e from ExpendApplyProject e where e.expendApplyInfoId=" + expendApplyInfo.getExpendApplyInfoId() +
//						" and e.deleted=0").getResultList();
//				for(ExpendApplyProject project : expendApplyProjectList){
//					StringBuffer moneySql = new StringBuffer();
//		            if(null == project.getGenericProjectId()){
//		                moneySql.append(" select normalExpendPlantInfo from NormalExpendPlantInfo normalExpendPlantInfo where normalExpendPlantInfo.projectId=");
//		                moneySql.append(project.getProjectId()).append(" and normalExpendPlantInfo.year= '").append(expendApplyInfo.getYear()).append("'");
//		            }else{
//		                moneySql.append(" select normalExpendPlantInfo from NormalExpendPlantInfo normalExpendPlantInfo where normalExpendPlantInfo.genericProjectId=");
//		                moneySql.append(project.getGenericProjectId()).append(" and normalExpendPlantInfo.year= '").append(expendApplyInfo.getYear()).append("'");
//		            }
//		            
//		            List<NormalExpendPlantInfo> normalExpendPlanInfoList = getEntityManager().createQuery(moneySql.toString()).getResultList();
//		            NormalExpendPlantInfo nxpi = normalExpendPlanInfoList.get(0);
//		            nxpi.setBudgetAmountFrozen(nxpi.getBudgetAmountFrozen() - project.getExpendMoney());
//		            nxpi.setBudgetAmountSurplus(nxpi.getBudgetAmountSurplus() + project.getExpendMoney());
//		            getEntityManager().merge(nxpi);
//				}
				getEntityManager().flush();
				raiseAfterTransactionSuccessEvent();
				/**
				 * 驳回的时候看有没有耗材的项目，如果有去重新计算，并且把审核意见给回去
				 */
				
				if(expendApplyInfo.getExplendSource() == 2){
					double frozenMoney = 0d;
					String sql = "select eap.project_id,eap.generic_project_id from expend_apply_project eap where eap.expend_apply_info_id=" + expendApplyInfo.getExpendApplyInfoId();
					List<Object[]> proList = getEntityManager().createNativeQuery(sql).getResultList();
					if(null != proList && proList.size() > 0){
						if(null != proList.get(0)[0]){
							frozenMoney = getExpendInfoMoney(1,Integer.valueOf(proList.get(0)[0].toString()));
						}else{
							frozenMoney = getExpendInfoMoney(2,Integer.valueOf(proList.get(0)[1].toString()));
						}
					}
					SqlServerJDBCUtil.checkReturn(expendApplyInfo.getExpendApplyCode(), frozenMoney);
				}
			}
			return true;
		}catch(Exception e){
			return false;
		}
	}
	/**
	 * 获取某个项目未完成的申请单中的钱
	 * @param projectType
	 * @param projectId
	 * @return
	 */
	public Double getExpendInfoMoney(int projectType,int projectId){
		double money = 0d;
		StringBuffer sql = new StringBuffer();
		if(projectType == 1){
			sql.append(" select sum(eap.expend_money) from expend_apply_info eai  ");
			sql.append(" LEFT JOIN expend_apply_project eap on eai.expend_apply_info_id=eap.expend_apply_info_id ");
			sql.append(" where eai.expend_apply_status in (0,1,2) ");
			sql.append(" and eap.project_id= ").append(projectId);
			sql.append(" and eai.deleted = 0 ");
			sql.append(" GROUP BY eap.project_id ");
		}else{
			sql.append(" select sum(eap.expend_money) from expend_apply_info eai  ");
			sql.append(" LEFT JOIN expend_apply_project eap on eai.expend_apply_info_id=eap.expend_apply_info_id ");
			sql.append(" where eai.expend_apply_status in (0,1,2) ");
			sql.append(" and eap.generic_project_id= ").append(projectId);
			sql.append(" and eai.deleted = 0 ");
			sql.append(" GROUP BY eap.project_id ");
		}
		
		List<Object> moneyList = getEntityManager().createNativeQuery(sql.toString()).getResultList();
		if(null != moneyList && moneyList.size() > 0){
			Object obj = moneyList.get(0);
			if(null != obj){
				money = Double.parseDouble(obj.toString());
			}
		}
		return money;
	}
	
	
	/**
	 * 获取某个项目未完成的申请单中的钱
	 * @param projectType
	 * @param projectId
	 * @return
	 */
	public Double getExpendInfoMoneyOfEdit(int projectType,int projectId,int eaiId){
		double money = 0d;
		StringBuffer sql = new StringBuffer();
		if(projectType == 1){
			sql.append(" select sum(eap.expend_money) from expend_apply_info eai  ");
			sql.append(" LEFT JOIN expend_apply_project eap on eai.expend_apply_info_id=eap.expend_apply_info_id ");
			sql.append(" where eai.expend_apply_status in (0,1) ");
			sql.append(" and eap.project_id= ").append(projectId);
			sql.append(" and eap.deleted=0 ");
			sql.append(" and eai.expend_apply_info_id !=  ").append(eaiId);
			sql.append(" GROUP BY eap.project_id ");
		}else{
			sql.append(" select sum(eap.expend_money) from expend_apply_info eai  ");
			sql.append(" LEFT JOIN expend_apply_project eap on eai.expend_apply_info_id=eap.expend_apply_info_id ");
			sql.append(" where eai.expend_apply_status in (0,1) ");
			sql.append(" and eap.generic_project_id= ").append(projectId);
			sql.append(" and eap.deleted=0 ");
			sql.append(" and eai.expend_apply_info_id !=  ").append(eaiId);
			sql.append(" GROUP BY eap.project_id ");
		}
		
		List<Object> moneyList = getEntityManager().createNativeQuery(sql.toString()).getResultList();
		if(null != moneyList && moneyList.size() > 0){
			Object obj = moneyList.get(0);
			if(null != obj){
				money = Double.parseDouble(obj.toString());
			}
		}
		return money;
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

	public Double getAllMoney() {
		return allMoney;
	}

	public void setAllMoney(Double allMoney) {
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


	public List<Object[]> getContractList() {
		return contractList;
	}


	public void setContractList(List<Object[]> contractList) {
		this.contractList = contractList;
	}


	public Integer getContractId() {
		return contractId;
	}


	public void setContractId(Integer contractId) {
		this.contractId = contractId;
	}


	public JSONObject getContractJson() {
		return contractJson;
	}


	public void setContractJson(JSONObject contractJson) {
		this.contractJson = contractJson;
	}


	public String getCheckItems() {
		return checkItems;
	}


	public void setCheckItems(String checkItems) {
		this.checkItems = checkItems;
	}


	
	
}
