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

@Name("expendConfrimList")
public class ExpendConfrimList extends CriterionNativeQuery<Object[]> {

	private static final long serialVersionUID = 1L;
	
	@In
	protected SessionToken sessionToken;
	private List<Object[]> expendApplyInfoList;
	private String searchKey;//搜索条件
	private List<Object[]> departList;//科室列表
	private List<Object[]> applayUserList;//申请人列表
	
	private Integer departmentId;//科室id
	private Integer applyUser;//申请编制人
	private String applyTime;//申请时间
	private Integer confirmStatus;//确认状态 -1全部  0未确认 1已确认
	
	private static final int FINA_ROLE_ID = 3;//财务人员角色id
	private static final int DIRECTOR_ROLE_ID = 4;//主任角色id
	
	public void wire(){
		if(null == departmentId){
			departmentId = -1;
		}
		if(null == applyUser){
			applyUser = -1;
		}
		if(null == applyTime){
			applyTime = "";
		}
		if(null == confirmStatus){
			confirmStatus = -1;
		}
		//初始化科室列表
		wireDepartmentInfo();
		System.out.println(departList);
		//初始化申请人列表
		wireBudgetPerson();
		
	}
	
	@Override
	protected Query createQuery(){
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT ");
		sql.append(" eci.expend_confirm_info_id, ");//0确认单id
		sql.append(" eai.expend_apply_code, ");//1申请编号
		sql.append(" eai.`year`,");//2支出年份
		sql.append(" eai.total_money, ");//3总金额
		sql.append(" eai.recive_company, ");//4收款单位
		sql.append(" eai.invoice_num, ");//5发票号
		sql.append(" uie.fullname, ");//6申请人名字
		sql.append(" eai.apply_time, ");//7申请时间
		sql.append(" eai.summary, ");//8摘要
		sql.append(" eai.`comment`, ");//9备注
		sql.append(" eci.confirm_status ");//10状态
		sql.append(" FROM expend_confirm_info eci ");
		sql.append(" LEFT JOIN expend_apply_info eai on eci.expend_apply_info_id=eai.expend_apply_info_id ");
		sql.append(" LEFT JOIN user_info ui ON eai.applay_user_id = ui.user_info_id ");
		sql.append(" LEFT JOIN user_info_extend uie on ui.user_info_extend_id=uie.user_info_extend_id ");
		sql.append(" where eci.deleted=0");
		if(null != departmentId && departmentId != -1){
			sql.append(" ui.department_info_id= ").append(departmentId);
		}
		if(null != applyUser && applyUser != -1){
			sql.append(" and eai.applay_user_id= ").append(applyUser);
		}
		if(null != applyTime && !applyTime.equals("")){
			sql.append(" and date_format(eai.apply_time ,'%Y-%m-%d') = '").append(applyTime).append("'");
		}
		if(null != applyTime && !applyTime.equals("")){
			sql.append(" and date_format(eai.apply_time ,'%Y-%m-%d') = '").append(applyTime).append("'");
		}
		if(null != confirmStatus && confirmStatus != -1){
			sql.append(" and eci.confirm_status= ").append(confirmStatus);
		}
		sql.append(" ORDER BY eci.expend_confirm_info_id desc");
		sql.insert(0, "select * from (").append(") as recordset");
		setEjbql(sql.toString());
		return super.createQuery();
	}

	public ExpendConfrimList() {
		/*boolean privateRole = false;//角色不属于财务 和主任（领导的）
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
		sql.append(" eai.`year`,");//2支出年份
		sql.append(" eai.finace_account_name, ");//3账务账名
		sql.append(" eai.recive_company, ");//4收款单位
		sql.append(" eai.invoice_num, ");//5发票号
		sql.append(" uie.fullname, ");//6申请人名字
		sql.append(" eai.apply_time, ");//7申请时间
		sql.append(" eai.summary, ");//8摘要
		sql.append(" eai.`comment` ");//9摘要
		sql.append(" FROM expend_apply_info eai ");
		sql.append(" LEFT JOIN user_info ui ON eai.applay_user_id = ui.user_info_id ");
		sql.append(" LEFT JOIN user_info_extend uie on ui.user_info_extend_id=uie.user_info_extend_id ");
		sql.append(" where eai.deleted=0 ");
		if(privateRole){
			sql.append(" eai.insert_user= ").append(sessionToken.getUserInfoId());
		}
		if(null != searchKey && !searchKey.equals("")){
			sql.append(" and (eai.expend_apply_code = '%").append(searchKey).append("%' eai.finace_account_name LIKE '%").append(searchKey).append("%')");
		}
		sql.insert(0, "select * from (").append(") as recordset");*/
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
	public List<Object[]> getExpendApplyInfoList() {
		return expendApplyInfoList;
	}


	public void setExpendApplyInfoList(List<Object[]> expendApplyInfoList) {
		this.expendApplyInfoList = expendApplyInfoList;
	}


	public String getSearchKey() {
		return searchKey;
	}

	public void setSearchKey(String searchKey) {
		this.searchKey = searchKey;
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

	public String getApplyTime() {
		return applyTime;
	}

	public void setApplyTime(String applyTime) {
		this.applyTime = applyTime;
	}

	public Integer getConfirmStatus() {
		return confirmStatus;
	}

	public void setConfirmStatus(Integer confirmStatus) {
		this.confirmStatus = confirmStatus;
	}

	public List<Object[]> getDepartList() {
		return departList;
	}

	public void setDepartList(List<Object[]> departList) {
		this.departList = departList;
	}

	public List<Object[]> getApplayUserList() {
		return applayUserList;
	}

	public void setApplayUserList(List<Object[]> applayUserList) {
		this.applayUserList = applayUserList;
	}
	
	
}
