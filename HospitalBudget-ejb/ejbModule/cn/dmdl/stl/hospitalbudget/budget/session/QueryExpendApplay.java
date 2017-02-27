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
	private Integer projectId;
	private Integer fundsSourceId;
	private Integer departId;
	
	private static final int FINA_ROLE_ID = 3;//财务人员角色id
	private static final int DIRECTOR_ROLE_ID = 4;//主任角色id
	
	@SuppressWarnings("unchecked")
	public void wire(){
		fundsSourceList = new ArrayList<Object[]>();
		departList = new ArrayList<Object[]>();
		projectList = new ArrayList<Object[]>();
		
		boolean privateRole = false;//角色不属于财务 和主任（领导的）
		String roleSql = "select role_info.role_info_id,user_info.department_info_id,ydi.the_value from user_info LEFT JOIN role_info on role_info.role_info_id=user_info.role_info_id "
				+ "LEFT JOIN ys_department_info ydi on user_info.department_info_id=ydi.the_id where user_info.user_info_id=" + sessionToken.getUserInfoId();
		List<Object[]> roleList = getEntityManager().createNativeQuery("select * from (" + roleSql + ") as test").getResultList();
		int roleId = Integer.parseInt(roleList.get(0)[0].toString());//角色id
		
		if(Integer.valueOf(roleId) != 1 && Integer.valueOf(roleId) != FINA_ROLE_ID && Integer.valueOf(roleId) != DIRECTOR_ROLE_ID){
			privateRole = true;
		}
		
		//资金来源
		Object[] obj = new Object[2];
		obj[0] = -1;
		obj[1] = "请选择";
		fundsSourceList.add(obj);
		projectList.add(obj);
		Object[] fundsobj = new Object[2];
		fundsobj[0] = 1;
		fundsobj[1] = "自有资金";
		fundsSourceList.add(fundsobj);
		StringBuffer projectSql =  new StringBuffer();
		projectSql.append(" SELECT ");
		projectSql.append(" ycp.the_id as project_id,");
		projectSql.append(" ycpe.the_id as sub_id,");
		projectSql.append(" ycp.the_value as peoject_name, ");//1项目名字
		projectSql.append(" ycpe.the_value as sub_project_name ");//2项目名字
		projectSql.append(" FROM expend_apply_info eai ");
		projectSql.append(" LEFT JOIN normal_expend_budget_order_info neboi on neboi.normal_expend_budget_order_id=eai.normal_expend_budget_order_id ");
		projectSql.append(" LEFT JOIN ys_convention_project ycp on ycp.the_id=neboi.normal_project_id ");
		projectSql.append(" LEFT JOIN user_info ui on ui.user_info_id=eai.applay_user_id ");
		projectSql.append(" LEFT JOIN ys_convention_project_extend ycpe on neboi.sub_project_id=ycpe.the_id ");
		projectSql.append(" where eai.deleted=0 ");
		if(privateRole){
			projectSql.append(" eai.insert_user= ").append(sessionToken.getUserInfoId());
			projectSql.insert(0, "select * from (").append(") as recordset");
			
			Object[] departObj = new Object[2];
			departObj[0] = roleList.get(0)[1];
			departObj[1] = roleList.get(0)[2];
			departList.add(obj);
			departList.add(departObj);
		}else{
			projectSql.insert(0, "select * from (").append(") as recordset");
			wireDepartmentInfo();
			
		}
		List<Object[]> proList = getEntityManager().createNativeQuery(projectSql.toString()).getResultList();
		if(proList.size() > 0){
			for(Object[] pro :proList){
				if(null == pro[0] && null != pro[1] && null == pro[2] && null != pro[3] ){
					Object[] pronew = new Object[2];
					pronew[0] = pro[1];
					pronew[1] = pro[3];
					projectList.add(pronew);
				}else if(null != pro[0] && null == pro[1] && null != pro[2] && null == pro[3]){
					Object[] pronew = new Object[2];
					pronew[0] = pro[0];
					pronew[1] = pro[2];
					projectList.add(pronew);
				}
			}
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
		sql.append(" eai.expend_apply_code, ");//1申请编号
		sql.append(" eai.funds_source, ");//2资金来源
		sql.append(" ydi.the_value as depart_name, ");//3部门名字
		sql.append(" ycp.the_value as peoject_name, ");//4项目名字
		sql.append(" eai.expend_money, ");//5支出金额
		sql.append(" uie.fullname, ");//6申请人名字
		sql.append(" eai.insert_time, ");//7申请提交时间
		sql.append(" ycpe.the_value as sub_project_name ");
		sql.append(" FROM expend_apply_info eai ");
		sql.append(" LEFT JOIN ys_department_info ydi ON eai.department_info_id = ydi.the_id ");
		sql.append(" LEFT JOIN normal_expend_budget_order_info neboi on neboi.normal_expend_budget_order_id=eai.normal_expend_budget_order_id ");
		sql.append(" LEFT JOIN ys_convention_project ycp on ycp.the_id=neboi.normal_project_id ");
		sql.append(" LEFT JOIN user_info ui on ui.user_info_id=eai.applay_user_id ");
		sql.append(" LEFT JOIN user_info_extend uie on ui.user_info_extend_id=uie.user_info_extend_id ");
		sql.append(" LEFT JOIN ys_convention_project_extend ycpe on neboi.sub_project_id=ycpe.the_id ");
		sql.append(" where eai.deleted=0 ");
		if(privateRole){
			sql.append(" eai.insert_user= ").append(sessionToken.getUserInfoId());
		}
		if(null != projectId && projectId != -1){
			sql.append(" and (ycp.the_id= ").append(projectId).append(" or ycpe.the_id=").append(projectId).append(")");
		}
		if(null != fundsSourceId && fundsSourceId != -1){
			sql.append(" and eai.funds_source= ").append(fundsSourceId);
		}
		if(null != departId && departId != -1){
			sql.append(" and eai.department_info_id=").append(departId);
		}
		if(null != expendTime && !expendTime.equals("")){
			sql.append(" and date_format(eai.insert_time,'%Y-%m-%d')='").append(expendTime).append("'");
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


	public Integer getProjectId() {
		return projectId;
	}


	public void setProjectId(Integer projectId) {
		this.projectId = projectId;
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
