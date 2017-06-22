package cn.dmdl.stl.hospitalbudget.execute.session;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;
import net.sf.json.util.JSONUtils;

import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.common.session.CriterionEntityHome;
import cn.dmdl.stl.hospitalbudget.execute.entity.EarnConfirm;
import cn.dmdl.stl.hospitalbudget.execute.entity.EarnConfirmProduct;
import cn.dmdl.stl.hospitalbudget.hospital.entity.YsDepartmentInfo;
import cn.dmdl.stl.hospitalbudget.util.DateTimeHelper;

@Name("earnConfirmHome")
public class EarnConfirmHome extends CriterionEntityHome<EarnConfirm> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6918699271602683443L;
	private boolean first;// 首次标记（提交表单后返回错误状态重载页面控件，如下拉框。。。）
	private Integer id;
	private List<Object[]> departmentInfoList;// 科室select
	private Integer departmentInfoId;// 科室id
	private List<Object[]> budgetPersonList;// 预算相关人员list 0科室名称 1该科室下的人员list
	private String budgetPersonIds;// 预算相关人员ids
	private String postingDate;// 开始预算年份
	private String registerDate;// 关闭项目支出时间
	private List<Object[]> fundsSourceList;// 资金来源list
	private Integer fundsSourceId;// 资金来源id
	private JSONObject personJson;// 人员json
	private JSONObject subprojectInfoJson;// 子项目（后台-->前台）
	private List<Object[]> productList;// 资金来源list
	private String receiptsCode; //单据编号
	private String reimbursementMan; //报销人
	private String voucherCode;	//凭证号
	private String chequeCode; 	//支票号
	private String paymentUnit; //付款单位
	private String invoiceCode; //发票号
	private float totalAmount; //总金额
	private String registerMan; //报销人
	private Integer process_type; //付款方式
	private String subprojectInfo;// 
	
	
	public List<Object[]> getFundsSourceList() {
		return fundsSourceList;
	}

	public void setFundsSourceList(List<Object[]> fundsSourceList) {
		this.fundsSourceList = fundsSourceList;
	}

	public void setEarnConfirmEarnConfirmId(Integer id) {
		setId(id);
	}

	public Integer getEarnConfirmEarnConfirmId() {
		return (Integer) getId();
	}

	@Override
	protected EarnConfirm createInstance() {
		EarnConfirm earnConfirm = new EarnConfirm();
		return earnConfirm;
	}

	public void load() {
		if (isIdDefined()) {
			wire();
		}
	}

	public void wire() {
		getInstance();
		wireDepartmentInfo();// 主管科室
		wireBudgetPerson();// 预算相关人员
		wireFundsSource();
//		wirePersonJson();// 人员json
		wireProduct();//项目list
		System.out.println("id-----------"+this.getEarnConfirmEarnConfirmId());
		
		if(null != this.getEarnConfirmEarnConfirmId()){
			
			EarnConfirm earnConfirm = getEntityManager().find(EarnConfirm.class, this.getEarnConfirmEarnConfirmId());
			setReceiptsCode(earnConfirm.getReceiptsCode());
			setDepartmentInfoId(earnConfirm.getYsDepartmentInfo().getTheId());
			setRegisterMan(earnConfirm.getRegisterMan());
			setVoucherCode(earnConfirm.getVoucherCode());
			setPaymentUnit(earnConfirm.getPaymentUnit());
			setInvoiceCode(earnConfirm.getInvoiceCode());
			setChequeCode(earnConfirm.getChequeCode());
			setTotalAmount(earnConfirm.getTotalAmount());
			setPostingDate(DateTimeHelper.dateToStr(earnConfirm.getPostingDate(), DateTimeHelper.PATTERN_DATE));
			setRegisterDate(DateTimeHelper.dateToStr(earnConfirm.getRegisterDate(), DateTimeHelper.PATTERN_DATE));
			
			JSONObject subprojectInfoJson = new JSONObject();
			
			JSONObject itemValue = new JSONObject();
			itemValue.accumulate("id", "42");
			itemValue.accumulate("name", "我去");
			itemValue.accumulate("subprojectBudgetaryAmount", "100");
			itemValue.accumulate("subprojectBudgetaryAmountAffirm", "20");
			itemValue.accumulate("subprojectBudgetaryYear", "30"); 
			itemValue.accumulate("type", 2);
//			itemValue.accumulate("pid", null);
//			itemValue.accumulate("compiler", );
//			itemValue.accumulate("executor", );
			itemValue.accumulate("description", "haha");
			subprojectInfoJson.accumulate(id.toString(), itemValue);
//			List<Object[]> prj2ndList = getEntityManager().createNativeQuery("select * from earn_confirm_product where earn_confirm_id = "+earnConfirm.getEarnConfirmId()).getResultList();
//			if (prj2ndList != null && prj2ndList.size() > 0) {
//				for (Object[] prj2nd : prj2ndList) {
//					Object id = prj2nd[0];
//					JSONObject itemValue = new JSONObject();
//					itemValue.accumulate("id", id);
//					itemValue.accumulate("name", prj2nd[1]);
//					itemValue.accumulate("pid", null);
//					itemValue.accumulate("level", 2);
////					itemValue.accumulate("compiler", );
////					itemValue.accumulate("executor", );
//					itemValue.accumulate("description", prj2nd[2]);
//					subprojectInfoJson.accumulate(id.toString(), itemValue);
////					parasitic3rdPlus(prj3rdPlusList, theCompilerMap, theExecutorMap, subprojectInfoJson, id);
//				}
//			}
			this.subprojectInfoJson = subprojectInfoJson;// 避免后台toString()到前台使用JSON.parse解析含有回车符文本报错
			personJson = new JSONObject();
			first = false;
		}else{
			first = true;
			personJson = new JSONObject();
			this.subprojectInfoJson = new JSONObject();
		}
		
	}
	
	@Override
	public String persist() {
		System.out.println("单据编号-----------"+receiptsCode);
		instance.setReceiptsCode(receiptsCode);
		instance.setYsDepartmentInfo(getEntityManager().find(YsDepartmentInfo.class, departmentInfoId));
		instance.setReimbursementMan(reimbursementMan);
		instance.setVoucherCode(voucherCode);
		instance.setPaymentUnit(paymentUnit);
		instance.setInvoiceCode(invoiceCode);
		instance.setChequeCode(chequeCode);
		instance.setTotalAmount(totalAmount);
		System.out.println("------------"+process_type);
		instance.setPostingDate(DateTimeHelper.strToDate(postingDate, DateTimeHelper.PATTERN_DATE));
		instance.setRegisterMan(registerMan);
		instance.setRegisterDate(DateTimeHelper.strToDate(registerDate, DateTimeHelper.PATTERN_DATE));
		instance.setProcess_type(1);
		instance.setInsertTime(new Date());
		instance.setState(false);
		getEntityManager().persist(instance);
		
		System.out.println("--------json-----"+subprojectInfo);
		
		
		JSONObject subprojectInfoAll = JSONObject.fromObject(subprojectInfo);
		for (Object key : subprojectInfoAll.keySet()) {
			JSONObject subprojectInfoOne = subprojectInfoAll.getJSONObject(key.toString());
			Integer pid = JSONUtils.isNull(subprojectInfoOne.get("id")) ? null :subprojectInfoOne.getInt("id");
			String amountAffirm = subprojectInfoOne.getString("subprojectBudgetaryAmountAffirm");
			Integer type = subprojectInfoOne.getInt("type");
			//Double amount = subprojectInfoOne.getDouble("amount");
//			String executor = subprojectInfoOne.getString("executor");
//			String description = subprojectInfoOne.getString("description");
			if (null != pid) {
				EarnConfirmProduct earnConfirmProduct = new EarnConfirmProduct();
				earnConfirmProduct.setEarnConfirmId(instance.getEarnConfirmId());
				earnConfirmProduct.setConfirmAmount(Float.valueOf(amountAffirm));
				if(type == 1){
					earnConfirmProduct.setGenericProjectId(pid);//一般项目ID
				}
				if(type == 2){
					earnConfirmProduct.setRoutineProjectId(pid);//常规项目ID
				}
				getEntityManager().persist(earnConfirmProduct);
			}
		}
		
		joinTransaction();
		getEntityManager().flush();
		raiseAfterTransactionSuccessEvent();
		return "persisted";
	}
	
	@Override
	public String update() {
		System.out.println("单据编号-----------"+receiptsCode);
		instance.setReceiptsCode(receiptsCode);
		instance.setYsDepartmentInfo(getEntityManager().find(YsDepartmentInfo.class, departmentInfoId));
		instance.setReimbursementMan(reimbursementMan);
		instance.setVoucherCode(voucherCode);
		instance.setPaymentUnit(paymentUnit);
		instance.setInvoiceCode(invoiceCode);
		instance.setChequeCode(chequeCode);
		instance.setTotalAmount(totalAmount);
		System.out.println("------------"+process_type);
		instance.setPostingDate(DateTimeHelper.strToDate(postingDate, DateTimeHelper.PATTERN_DATE));
		instance.setRegisterMan(registerMan);
		instance.setRegisterDate(DateTimeHelper.strToDate(registerDate, DateTimeHelper.PATTERN_DATE));
		instance.setProcess_type(1);
		instance.setInsertTime(new Date());
		instance.setState(false);
		getEntityManager().persist(instance);
		System.out.println("--------update-----"+subprojectInfo);
		return "updated";
	}
	@SuppressWarnings("unchecked")
	private void wireFundsSource() {
		if (fundsSourceList != null) {
			fundsSourceList.clear();
		} else {
			fundsSourceList = new ArrayList<Object[]>();
		}
		fundsSourceList.add(new Object[] { "", "请选择" });
		String dataSql = "select the_id, the_value from ys_funds_source where deleted = 0";
		List<Object[]> dataList = getEntityManager().createNativeQuery(dataSql).getResultList();
		if (dataList != null && dataList.size() > 0) {
			for (Object[] data : dataList) {
				fundsSourceList.add(new Object[] { data[0], data[1] });
			}
		}
//		fundsSourceId = fundsSourceId != null ? fundsSourceId : (instance.getYsFundsSource() != null ? instance.getYsFundsSource().getTheId() : null);
	}
	
	@SuppressWarnings("unchecked")
	private void wireProduct() {
		if (productList != null) {
			productList.clear();
		} else {
			productList = new ArrayList<Object[]>();
		}
		productList.add(new Object[] { "", "请选择" });
		StringBuffer sql = new StringBuffer();
		sql.append(" select ipi.generic_project_id,gp.the_value,yfs.the_value, ");
		sql.append(" ipi.routine_project_id,rp.the_value,yfs1.the_value ");
		sql.append(" from ys_income_plan_info ipi ");
		sql.append(" left join generic_project gp on gp.the_id=ipi.generic_project_id ");
		sql.append(" left join routine_project rp on rp.the_id = ipi.routine_project_id ");
		sql.append(" left join generic_project_executor gpe on gpe.project_id = ipi.generic_project_id ");
		sql.append(" left join routine_project_executor rpe on rpe.project_id = ipi.routine_project_id ");
		sql.append(" left join ys_funds_source yfs on yfs.the_id = gp.funds_source_id ");
		sql.append(" left join ys_funds_source yfs1 on yfs1.the_id = rp.funds_source_id ");
		sql.append(" where gpe.user_info_id = ? or rpe.user_info_id = ? ");
		System.out.println("user--------------"+sessionToken.getUserInfoId());
//		String dataSql = "select the_id, the_value from ys_general_project where deleted = 0";
		List<Object[]> dataList = getEntityManager().createNativeQuery(sql.toString()).setParameter(1, sessionToken.getUserInfoId()).setParameter(2, sessionToken.getUserInfoId()).getResultList();
		if (dataList != null && dataList.size() > 0) {
			for (Object[] data : dataList) {
				if(null != data[0]){
					productList.add(new Object[] { data[0], data[1], 1, data[2]});//一般项目
				}else{
					productList.add(new Object[] { data[3], data[4], 2, data[5]});//常规项目
				}
			}
		}
//		fundsSourceId = fundsSourceId != null ? fundsSourceId : (instance.getYsFundsSource() != null ? instance.getYsFundsSource().getTheId() : null);
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

		if (budgetPersonList != null) {
			budgetPersonList.clear();
		} else {
			budgetPersonList = new ArrayList<Object[]>();
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
					budgetPersonList.add(new Object[] { valueMap.get(root), userMap.get(root) });
				}
				disposeLeafBudgetPerson(budgetPersonList, userMap, nexusMap, valueMap, 1, nexusMap.get(root));
			}
		}

	}

	@SuppressWarnings("unchecked")
	private void wireDepartmentInfo() {
		if (departmentInfoList != null) {
			departmentInfoList.clear();
		} else {
			departmentInfoList = new ArrayList<Object[]>();
		}
		departmentInfoList.add(new Object[] { "", "请选择" });
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
	
//	@SuppressWarnings("unchecked")
//	private void wirePersonJson() {
//		if (personJson != null) {
//			personJson.clear();
//		} else {
//			personJson = new JSONObject();
//		}
//		StringBuffer dataSql = new StringBuffer();
//		dataSql.append(" select");
//		dataSql.append(" user_info.user_info_id,");
//		dataSql.append(" user_info_extend.fullname");
//		dataSql.append(" from user_info");
//		dataSql.append(" inner join user_info_extend on user_info_extend.user_info_extend_id = user_info.user_info_extend_id");
//		dataSql.append(" where user_info.deleted = 0");
//		List<Object[]> dataList = getEntityManager().createNativeQuery(dataSql.toString()).getResultList();
//		if (dataList != null && dataList.size() > 0) {
//			for (Object[] data : dataList) {
//				personJson.accumulate(data[0].toString(), data[1].toString());
//			}
//		}
//	}

	public boolean isWired() {
		return true;
	}

	public List<Object[]> getDepartmentInfoList() {
		return departmentInfoList;
	}

	public Integer getDepartmentInfoId() {
		return departmentInfoId;
	}

	public void setDepartmentInfoId(Integer departmentInfoId) {
		this.departmentInfoId = departmentInfoId;
	}

	public List<Object[]> getBudgetPersonList() {
		return budgetPersonList;
	}

	public String getBudgetPersonIds() {
		return budgetPersonIds;
	}

	public void setBudgetPersonIds(String budgetPersonIds) {
		this.budgetPersonIds = budgetPersonIds;
	}

	public Integer getFundsSourceId() {
		return fundsSourceId;
	}

	public void setFundsSourceId(Integer fundsSourceId) {
		this.fundsSourceId = fundsSourceId;
	}

	public String getPostingDate() {
		return postingDate;
	}

	public void setPostingDate(String postingDate) {
		this.postingDate = postingDate;
	}

	public String getRegisterDate() {
		return registerDate;
	}

	public void setRegisterDate(String registerDate) {
		this.registerDate = registerDate;
	}

	public JSONObject getPersonJson() {
		return personJson;
	}

	public void setPersonJson(JSONObject personJson) {
		this.personJson = personJson;
	}

	public JSONObject getSubprojectInfoJson() {
		return subprojectInfoJson;
	}

	public void setSubprojectInfoJson(JSONObject subprojectInfoJson) {
		this.subprojectInfoJson = subprojectInfoJson;
	}

	public List<Object[]> getProductList() {
		return productList;
	}

	public void setProductList(List<Object[]> productList) {
		this.productList = productList;
	}

	public String getReimbursementMan() {
		return reimbursementMan;
	}

	public void setReimbursementMan(String reimbursementMan) {
		this.reimbursementMan = reimbursementMan;
	}

	public String getVoucherCode() {
		return voucherCode;
	}

	public void setVoucherCode(String voucherCode) {
		this.voucherCode = voucherCode;
	}

	public String getChequeCode() {
		return chequeCode;
	}

	public void setChequeCode(String chequeCode) {
		this.chequeCode = chequeCode;
	}

	public String getPaymentUnit() {
		return paymentUnit;
	}

	public void setPaymentUnit(String paymentUnit) {
		this.paymentUnit = paymentUnit;
	}

	public String getInvoiceCode() {
		return invoiceCode;
	}

	public void setInvoiceCode(String invoiceCode) {
		this.invoiceCode = invoiceCode;
	}

	public float getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(float totalAmount) {
		this.totalAmount = totalAmount;
	}

	public String getRegisterMan() {
		return registerMan;
	}

	public void setRegisterMan(String registerMan) {
		this.registerMan = registerMan;
	}

	public Integer getProcess_type() {
		return process_type;
	}

	public void setProcess_type(Integer process_type) {
		this.process_type = process_type;
	}


	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getReceiptsCode() {
		return receiptsCode;
	}

	public void setReceiptsCode(String receiptsCode) {
		this.receiptsCode = receiptsCode;
	}

	public String getSubprojectInfo() {
		return subprojectInfo;
	}

	public void setSubprojectInfo(String subprojectInfo) {
		this.subprojectInfo = subprojectInfo;
	}

	public boolean getFirst() {
		return first;
	}

	public void setFirst(boolean first) {
		this.first = first;
	}
}
