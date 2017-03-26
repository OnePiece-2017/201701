package cn.dmdl.stl.hospitalbudget.budget.session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.common.session.CriterionNativeQuery;
import cn.dmdl.stl.hospitalbudget.util.SessionToken;

@Name("queryExpendApplay")
public class QueryExpendApplay extends CriterionNativeQuery<Object[]> {

	private static final long serialVersionUID = 1L;
	
	@In
	protected SessionToken sessionToken;
	private List<Object[]> expendApplyInfoList;
	private List<Object[]> departList;//科室列表
	private List<Object[]> fundsSourceList;//资金来源列表
	private List<Object[]> projectList;//项目列表
	private String expendTime;//支出时间
	private String projectName;
	private Integer fundsSourceId;
	private Integer departId;
	
	private static final int FINA_ROLE_ID = 3;//财务人员角色id
	private static final int DIRECTOR_ROLE_ID = 4;//主任角色id
	
	@SuppressWarnings("unchecked")
	public void wire(){
		fundsSourceList = new ArrayList<Object[]>();
		departList = new ArrayList<Object[]>();
		projectList = new ArrayList<Object[]>();
		if(null == projectName){
			projectName = "";
		}
		if(null == expendTime){
			expendTime = "";
		}
		if(null == fundsSourceId){
			fundsSourceId = -1;
		}
		if(null == departId){
			departId = -1;
		}
		wireDepartmentInfo();
		//资金来源
		//初始化资金来源
		String sourceSql = "select yfs.the_id,yfs.the_value from ys_funds_source yfs where yfs.deleted=0";
		List<Object[]> list = getEntityManager().createNativeQuery(sourceSql).getResultList();
		if(list.size() > 0){
			fundsSourceList = list;
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	protected Query createQuery(){
		boolean privateRole = false;//角色不属于财务 和主任（领导的）
		String roleSql = "select role_info.role_info_id,user_info.department_info_id,ydi.the_value from user_info LEFT JOIN role_info on role_info.role_info_id=user_info.role_info_id LEFT JOIN ys_department_info ydi on "
				+ "user_info.department_info_id=ydi.the_id where user_info.user_info_id=" + sessionToken.getUserInfoId();
		
		List<Object[]> roleList = getEntityManager().createNativeQuery(roleSql).getResultList();
		int roleId = Integer.parseInt(roleList.get(0)[0].toString());//角色id
		
		if(Integer.valueOf(roleId) != 1 && Integer.valueOf(roleId) != FINA_ROLE_ID && Integer.valueOf(roleId) != DIRECTOR_ROLE_ID){
			privateRole = true;
		}
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT ");
		sql.append(" eai.expend_apply_info_id, ");//0申请单id
		sql.append(" eap.expend_apply_project_id, ");//1申请项目id
		sql.append(" eai.expend_apply_code, ");//2申请编号
		sql.append(" ysdi.the_value as depart_name, ");//3科室名称
		sql.append(" fs.the_value as source_name, ");//4资金来源
		sql.append(" up.the_value as project_name, ");//5项目名字
		sql.append(" eap.expend_money, ");//6支出金额
		sql.append(" uie.fullname, ");//7申请人名字
		sql.append(" eai.apply_time, ");//8申请提交时间
		sql.append(" ecp.expend_confirm_project_id, ");//9
		sql.append(" gp.the_value as generic_name, ");//10
		sql.append(" fs2.the_value as source_name2, ");//11
		sql.append(" ydi2.the_value as depart_name2, ");//12
		sql.append(" eap.project_id, ");//13
		sql.append(" eap.generic_project_id ");//14
		sql.append(" FROM expend_apply_project eap ");
		sql.append(" LEFT JOIN expend_apply_info eai ON eap.expend_apply_info_id = eai.expend_apply_info_id ");
		sql.append(" LEFT JOIN expend_confirm_project ecp on eap.expend_apply_project_id=ecp.expend_apply_project_id ");
		sql.append(" LEFT JOIN routine_project up ON eap.project_id = up.the_id ");
		sql.append(" LEFT JOIN ys_department_info ysdi on ysdi.the_id=up.department_info_id ");
		sql.append(" LEFT JOIN ys_funds_source fs ON fs.the_id = up.funds_source_id ");
		sql.append(" LEFT JOIN user_info ui ON ui.user_info_id = eai.applay_user_id ");
		sql.append(" LEFT JOIN user_info_extend uie ON ui.user_info_extend_id = uie.user_info_extend_id ");
		sql.append(" LEFT JOIN generic_project gp on eap.generic_project_id=gp.the_id ");
		sql.append(" LEFT JOIN ys_funds_source fs2 on fs2.the_id=gp.funds_source_id ");
		sql.append(" LEFT JOIN ys_department_info ydi2 on ydi2.the_id=gp.department_info_id ");
		sql.append(" where eap.deleted=0 and ecp.deleted=0 ");
		if(privateRole){
			sql.append(" and eap.insert_user= ").append(sessionToken.getUserInfoId());
		}
		if(null != projectName && !projectName.trim().equals("")){
			sql.append(" and (up.the_value like '").append(projectName).append("') or gp.the_value like '").append(projectName).append("')");
		}
		if(null != fundsSourceId && fundsSourceId != -1){
			sql.append(" and (up.funds_source_id= ").append(fundsSourceId).append(" or gp.funds_source_id=").append(fundsSourceId).append(")");
		}
		if(null != departId && departId != -1){
			sql.append(" and (up.department_info_id").append(departId).append(" or gp.department_info_id=").append(departId).append(")");
		}
		if(null != expendTime && !expendTime.equals("")){
			sql.append(" and date_format(eai.apply_time,'%Y-%m-%d')='").append(expendTime).append("'");
		}
		sql.insert(0, "select * from (").append(") as recordset");
		setEjbql(sql.toString());
		return super.createQuery();
	}

	public QueryExpendApplay() {
		setEjbql("");
		setAttribute("");
	}
	
	
	/**
	 * 获取科室
	 */
	@SuppressWarnings("unchecked")
	private void wireDepartmentInfo() {
		if (departList != null) {
			departList.clear();
		} else {
			departList = new ArrayList<Object[]>();
		}
		departList.add(new Object[] { -1, "请选择" });
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
				departList.add(new Object[] { root, valueMap.get(root) });
				disposeLeaf(departList, nexusMap, valueMap, 1, nexusMap.get(root));
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
	/**
	 * 按条件查询
	 * @return
	 *//*
	@SuppressWarnings("unchecked")
	public void queryByCondition(){
		CommonFinder commonFinder = new CommonFinder();
		String roleSql = "select role_info.role_info_id from user_info LEFT JOIN role_info on role_info.role_info_id=user_info.role_info_id where user_info.user_info_id=" + sessionToken.getUserInfoId();
		List<Object> roleList = getEntityManager().createNativeQuery(roleSql).getResultList();
		Integer roleId = Integer.parseInt(roleList.get(0).toString());//角色id
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT ");
		sql.append(" eai.expend_apply_info_id, ");//0申请单id
		sql.append(" eai.expend_apply_code, ");//1申请编号
		sql.append(" eai.funds_source, ");//2资金来源
		sql.append(" ydi.the_value as depart_name, ");//3部门名字
		sql.append(" ycp.the_value as peoject_name, ");//4项目名字
		sql.append(" eai.expend_money, ");//5支出金额
		sql.append(" uie.fullname, ");//6申请人名字
		sql.append(" eai.insert_time ");//7申请提交时间
		sql.append(" FROM expend_apply_info eai ");
		sql.append(" LEFT JOIN ys_department_info ydi ON eai.department_info_id = ydi.the_id ");
		sql.append(" LEFT JOIN normal_expend_budget_order_info neboi on neboi.normal_expend_budget_order_id=eai.normal_expend_budget_order_id ");
		sql.append(" LEFT JOIN ys_convention_project ycp on ycp.the_id=neboi.normal_project_id ");
		sql.append(" LEFT JOIN user_info ui on ui.user_info_id=eai.applay_user_id ");
		sql.append(" LEFT JOIN user_info_extend uie on ui.user_info_extend_id=uie.user_info_extend_id ");
		sql.append(" where eai.deleted=0 ");
		if(roleId != 1 && roleId != FINA_ROLE_ID && roleId != DIRECTOR_ROLE_ID){
			sql.append(" eai.insert_user= ").append(sessionToken.getUserInfoId());
		}
		List<Object[]> list = getEntityManager().createNativeQuery("select * from (" + sql.toString() + ") as test").getResultList();
		if(list.size() > 0){
			expendApplyInfoList = list;
		}
		for(Object[] obj : list){
			obj[2] = commonFinder.getMoneySource(Integer.parseInt(obj[2].toString()));
		}
	}
*/
	public List<Object[]> getExpendApplyInfoList() {
		return expendApplyInfoList;
	}


	public void setExpendApplyInfoList(List<Object[]> expendApplyInfoList) {
		this.expendApplyInfoList = expendApplyInfoList;
	}


	public List<Object[]> getDepartList() {
		return departList;
	}


	public void setDepartList(List<Object[]> departList) {
		this.departList = departList;
	}


	public List<Object[]> getFundsSourceList() {
		return fundsSourceList;
	}


	public void setFundsSourceList(List<Object[]> fundsSourceList) {
		this.fundsSourceList = fundsSourceList;
	}


	public List<Object[]> getProjectList() {
		return projectList;
	}


	public void setProjectList(List<Object[]> projectList) {
		this.projectList = projectList;
	}


	public String getExpendTime() {
		return expendTime;
	}


	public void setExpendTime(String expendTime) {
		this.expendTime = expendTime;
	}




	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public Integer getFundsSourceId() {
		return fundsSourceId;
	}


	public void setFundsSourceId(Integer fundsSourceId) {
		this.fundsSourceId = fundsSourceId;
	}


	public Integer getDepartId() {
		return departId;
	}


	public void setDepartId(Integer departId) {
		this.departId = departId;
	}
	
	
}
