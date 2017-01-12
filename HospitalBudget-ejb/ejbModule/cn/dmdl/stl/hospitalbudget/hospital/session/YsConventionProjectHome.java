package cn.dmdl.stl.hospitalbudget.hospital.session;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.common.session.CriterionEntityHome;
import cn.dmdl.stl.hospitalbudget.hospital.entity.YsConventionProject;
import cn.dmdl.stl.hospitalbudget.hospital.entity.YsConventionProjectExtend;
import cn.dmdl.stl.hospitalbudget.hospital.entity.YsConventionProjectUser;
import cn.dmdl.stl.hospitalbudget.hospital.entity.YsDepartmentInfo;
import cn.dmdl.stl.hospitalbudget.util.Assit;

@Name("ysConventionProjectHome")
public class YsConventionProjectHome extends CriterionEntityHome<YsConventionProject> {

	private static final long serialVersionUID = 1L;
	private boolean isFirstTime;// 首次标记（提交表单后返回错误状态重载页面控件，如下拉框。。。）
	private List<Object[]> departmentInfoList;// 科室select
	private Integer departmentInfoId;// 科室id
	private List<Object[]> budgetPersonList;// 预算相关人员list 0科室名称 1该科室下的人员list
	private String budgetPersonIds;// 预算相关人员ids
	private String subItemData;// 子项目数据（回显get保存set）

	/** 拉取项目总金额 */
	@SuppressWarnings("unchecked")
	public BigDecimal pullTotalAmount(Integer projectId) {
		BigDecimal totalAmount = new BigDecimal("0.00");
		if (projectId != null) {
			YsConventionProject ysConventionProject = getEntityManager().find(YsConventionProject.class, projectId);
			if (ysConventionProject != null) {
				if (ysConventionProject.isMultilevel()) {
					List<YsConventionProjectExtend> ysConventionProjectExtendList = getEntityManager().createQuery("select ysConventionProjectExtend from YsConventionProjectExtend ysConventionProjectExtend where ysConventionProjectExtend.ysConventionProject.theId = " + projectId).getResultList();
					if (ysConventionProjectExtendList != null && ysConventionProjectExtendList.size() > 0) {
						for (YsConventionProjectExtend ysConventionProjectExtend : ysConventionProjectExtendList) {
							totalAmount = totalAmount.add(ysConventionProjectExtend.getTotalAmount());
						}
					}
				} else {
					return ysConventionProject.getTotalAmount();
				}
			}
		}
		return totalAmount;
	}

	/** 拉取预算相关人员 */
	@SuppressWarnings("unchecked")
	public String pullBudgetPerson(Integer projectId) {
		String result = "";
		if (projectId != null) {
			StringBuffer dataSql = new StringBuffer();
			dataSql.append(" select");
			dataSql.append(" IFNULL(GROUP_CONCAT(user_info_extend.fullname), '') as result");
			dataSql.append(" from ys_convention_project_user");
			dataSql.append(" inner join user_info on user_info.user_info_id = ys_convention_project_user.user_info_id");
			dataSql.append(" inner join user_info_extend on user_info_extend.user_info_extend_id = user_info.user_info_extend_id");
			dataSql.append(" where ys_convention_project_user.convention_project_id = ").append(projectId);
			List<Object> dataList = getEntityManager().createNativeQuery(dataSql.toString()).getResultList();
			if (dataList != null && dataList.size() > 0) {
				result = dataList.get(0).toString();
			}
		}
		return result;
	}

	@Override
	public String persist() {
		setMessage("");

		if (Assit.getResultSetSize("select the_id from ys_convention_project where deleted = 0 and the_value = '" + instance.getTheValue() + "'") > 0) {
			setMessage("此名称太受欢迎,请更换一个");
			return null;
		}

		joinTransaction();
		if (departmentInfoId != null) {
			instance.setYsDepartmentInfo(getEntityManager().find(YsDepartmentInfo.class, departmentInfoId));
		} else {
			instance.setYsDepartmentInfo(null);
		}
		instance.setTheState(2);// 默认为关闭状态
		instance.setInsertTime(new Date());
		instance.setInsertUser(sessionToken.getUserInfoId());
		getEntityManager().persist(instance);
		if (budgetPersonIds != null && !"".equals(budgetPersonIds)) {
			String[] idArr = budgetPersonIds.split(",");
			if (idArr != null && idArr.length > 0) {
				for (String id : idArr) {
					YsConventionProjectUser ysConventionProjectUser = new YsConventionProjectUser();
					ysConventionProjectUser.setConventionProjectId(instance.getTheId());
					ysConventionProjectUser.setUserInfoId(Integer.valueOf(id));
					getEntityManager().persist(ysConventionProjectUser);
				}
			}
		}
		if (instance.isMultilevel()) {
			instance.setTotalAmount(null);
			JSONArray dataItemArr = JSONArray.fromObject(subItemData);
			if (dataItemArr != null && dataItemArr.size() > 0) {
				for (int i = 0; i < dataItemArr.size(); i++) {
					JSONObject dataItem = dataItemArr.getJSONObject(i);
					YsConventionProjectExtend ysConventionProjectExtend = new YsConventionProjectExtend();
					ysConventionProjectExtend.setYsConventionProject(instance);
					ysConventionProjectExtend.setTheValue(dataItem.get("theValue") != null ? dataItem.get("theValue").toString() : "");
					ysConventionProjectExtend.setTotalAmount(new BigDecimal(dataItem.get("totalAmount") != null ? dataItem.get("totalAmount").toString() : ""));
					ysConventionProjectExtend.setTheDescription(dataItem.get("theDescription") != null ? dataItem.get("theDescription").toString() : "");
					getEntityManager().persist(ysConventionProjectExtend);
				}
			}
		}
		getEntityManager().flush();
		getEntityManager().clear();// 实体属性中有BigDecimal
		raiseAfterTransactionSuccessEvent();
		return "persisted";
	}

	@Override
	public String update() {
		setMessage("");

		if (Assit.getResultSetSize("select the_id from ys_convention_project where deleted = 0 and the_value = '" + instance.getTheValue() + "' and the_id != " + instance.getTheId()) > 0) {
			setMessage("此名称太受欢迎,请更换一个");
			return null;
		}

		joinTransaction();
		if (departmentInfoId != null) {
			instance.setYsDepartmentInfo(getEntityManager().find(YsDepartmentInfo.class, departmentInfoId));
		} else {
			instance.setYsDepartmentInfo(null);
		}
		// 清理中间表
		getEntityManager().createNativeQuery("delete from ys_convention_project_user where convention_project_id = " + instance.getTheId()).executeUpdate();
		if (budgetPersonIds != null && !"".equals(budgetPersonIds)) {
			String[] idArr = budgetPersonIds.split(",");
			if (idArr != null && idArr.length > 0) {
				for (String id : idArr) {
					YsConventionProjectUser ysConventionProjectUser = new YsConventionProjectUser();
					ysConventionProjectUser.setConventionProjectId(instance.getTheId());
					ysConventionProjectUser.setUserInfoId(Integer.valueOf(id));
					getEntityManager().persist(ysConventionProjectUser);
				}
			}
		}
		instance.setUpdateTime(new Date());
		instance.setUpdateUser(sessionToken.getUserInfoId());

		getEntityManager().createNativeQuery("delete from ys_convention_project_extend where convention_project_id = " + instance.getTheId()).executeUpdate();
		if (instance.isMultilevel()) {
			instance.setTotalAmount(null);
			JSONArray dataItemArr = JSONArray.fromObject(subItemData);
			if (dataItemArr != null && dataItemArr.size() > 0) {
				for (int i = 0; i < dataItemArr.size(); i++) {
					JSONObject dataItem = dataItemArr.getJSONObject(i);
					YsConventionProjectExtend ysConventionProjectExtend = new YsConventionProjectExtend();
					ysConventionProjectExtend.setYsConventionProject(instance);
					ysConventionProjectExtend.setTheValue(dataItem.get("theValue") != null ? dataItem.get("theValue").toString() : "");
					ysConventionProjectExtend.setTotalAmount(new BigDecimal(dataItem.get("totalAmount") != null ? dataItem.get("totalAmount").toString() : ""));
					ysConventionProjectExtend.setTheDescription(dataItem.get("theDescription") != null ? dataItem.get("theDescription").toString() : "");
					getEntityManager().persist(ysConventionProjectExtend);
				}
			}
		}
		getEntityManager().flush();
		getEntityManager().clear();// 实体属性中有BigDecimal
		raiseAfterTransactionSuccessEvent();
		return "updated";
	}

	@Override
	public String remove() {
		setMessage("");

		getInstance();

		// 清理中间表
		getEntityManager().createNativeQuery("delete from ys_convention_project_user where convention_project_id = " + instance.getTheId()).executeUpdate();
		getEntityManager().createNativeQuery("delete from ys_convention_project_extend where convention_project_id = " + instance.getTheId()).executeUpdate();
		instance.setDeleted(true);
		getEntityManager().flush();
		raiseAfterTransactionSuccessEvent();
		return "removed";
	}

	public void setYsConventionProjectTheId(Integer id) {
		setId(id);
	}

	public Integer getYsConventionProjectTheId() {
		return (Integer) getId();
	}

	@Override
	protected YsConventionProject createInstance() {
		YsConventionProject ysConventionProject = new YsConventionProject();
		return ysConventionProject;
	}

	public void load() {
		if (isIdDefined()) {
			wire();
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

	@SuppressWarnings("unchecked")
	public void wire() {
		getInstance();

		wireDepartmentInfo();// 主管科室
		wireBudgetPerson();// 预算相关人员

		if (!isFirstTime) {
			departmentInfoId = instance.getYsDepartmentInfo() != null ? instance.getYsDepartmentInfo().getTheId() : null;

			if (isManaged()) {
				List<Object> budgetPersonIdsList = getEntityManager().createNativeQuery("select IFNULL(GROUP_CONCAT(user_info_id), '') as result from ys_convention_project_user where convention_project_id = " + instance.getTheId()).getResultList();
				if (budgetPersonIdsList != null && budgetPersonIdsList.size() > 0) {
					budgetPersonIds = budgetPersonIdsList.get(0).toString();
				}
				if (instance.isMultilevel()) {
					List<YsConventionProjectExtend> ysConventionProjectExtendList = getEntityManager().createQuery("select ysConventionProjectExtend from YsConventionProjectExtend ysConventionProjectExtend where ysConventionProjectExtend.ysConventionProject.theId = " + instance.getTheId()).getResultList();
					if (ysConventionProjectExtendList != null && ysConventionProjectExtendList.size() > 0) {
						JSONArray dataItemArr = new JSONArray();
						for (YsConventionProjectExtend ysConventionProjectExtend : ysConventionProjectExtendList) {
							JSONObject dataItem = new JSONObject();
							dataItem.accumulate("theValue", ysConventionProjectExtend.getTheValue());
							dataItem.accumulate("totalAmount", ysConventionProjectExtend.getTotalAmount().toString());
							dataItem.accumulate("theDescription", ysConventionProjectExtend.getTheDescription());
							dataItemArr.add(dataItem);
						}
						subItemData = dataItemArr.toString();
					}
				}
			}

			isFirstTime = true;
		}
	}

	public boolean isWired() {
		return true;
	}

	public YsConventionProject getDefinedInstance() {
		return isIdDefined() ? getInstance() : null;
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

	public String getSubItemData() {
		return subItemData;
	}

	public void setSubItemData(String subItemData) {
		this.subItemData = subItemData;
	}

}
