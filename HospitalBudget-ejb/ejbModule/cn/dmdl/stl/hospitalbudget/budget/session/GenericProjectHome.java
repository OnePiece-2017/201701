package cn.dmdl.stl.hospitalbudget.budget.session;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.budget.entity.GenericProject;
import cn.dmdl.stl.hospitalbudget.budget.entity.GenericProjectCompiler;
import cn.dmdl.stl.hospitalbudget.budget.entity.GenericProjectExecutor;
import cn.dmdl.stl.hospitalbudget.common.session.CriterionEntityHome;
import cn.dmdl.stl.hospitalbudget.hospital.entity.YsDepartmentInfo;
import cn.dmdl.stl.hospitalbudget.hospital.entity.YsFundsSource;

@Name("genericProjectHome")
public class GenericProjectHome extends CriterionEntityHome<GenericProject> {

	private static final long serialVersionUID = 1L;
	private boolean isFirstTime;// 首次标记（提交表单后返回错误状态重载页面控件，如下拉框。。。）
	private List<Object[]> departmentInfoList;// 科室select
	private Integer departmentInfoId;// 科室id
	private List<Object[]> fundsSourceList;// 资金来源list
	private Integer fundsSourceId;// 资金来源id
	private List<Object[]> budgetPersonList;// 预算相关人员list 0科室名称 1该科室下的人员list
	private String budgetPersonCompilerIds;// 预算相关人员ids 编制人
	private String budgetPersonExecutorIds;// 预算相关人员ids 执行人
	private JSONObject personJson;// 人员json
	private String subprojectInfo;// 子项目（前台-->后台）
	private JSONObject subprojectInfoJson;// 子项目（后台-->前台）

	@Override
	public String persist() {
		setMessage("");

		joinTransaction();
		// 处理一级项目
		if (departmentInfoId != null) {
			instance.setYsDepartmentInfo(getEntityManager().find(YsDepartmentInfo.class, departmentInfoId));
		} else {
			instance.setYsDepartmentInfo(null);
		}
		if (fundsSourceId != null) {
			instance.setYsFundsSource(getEntityManager().find(YsFundsSource.class, fundsSourceId));
		} else {
			instance.setYsFundsSource(null);
		}
		instance.setTheLevel(1);// 级别为1
		instance.setBottomLevel(true);// 假定最底级
		instance.setInsertTime(new Date());
		instance.setInsertUser(sessionToken.getUserInfoId());
		getEntityManager().persist(instance);
		instance.setTopLevelProjectId(instance.getTheId());// 顶级条目的顶级项目id为自身
		if (budgetPersonCompilerIds != null && !"".equals(budgetPersonCompilerIds)) {
			String[] idArr = budgetPersonCompilerIds.split(",");
			if (idArr != null && idArr.length > 0) {
				for (String id : idArr) {
					GenericProjectCompiler theCompiler = new GenericProjectCompiler();
					theCompiler.setProjectId(instance.getTheId());
					theCompiler.setUserInfoId(Integer.valueOf(id));
					getEntityManager().persist(theCompiler);
				}
			}
		}
		if (budgetPersonExecutorIds != null && !"".equals(budgetPersonExecutorIds)) {
			String[] idArr = budgetPersonExecutorIds.split(",");
			if (idArr != null && idArr.length > 0) {
				for (String id : idArr) {
					GenericProjectExecutor theExecutor = new GenericProjectExecutor();
					theExecutor.setProjectId(instance.getTheId());
					theExecutor.setUserInfoId(Integer.valueOf(id));
					getEntityManager().persist(theExecutor);
				}
			}
		}
		// 处理二级项目
		JSONObject subprojectInfoAll = JSONObject.fromObject(subprojectInfo);
		for (Object key : subprojectInfoAll.keySet()) {
			JSONObject subprojectInfoOne = subprojectInfoAll.getJSONObject(key.toString());
			Object pid = subprojectInfoOne.get("pid");
			String name = subprojectInfoOne.getString("name");
			Double amount = subprojectInfoOne.getDouble("amount");
			String executor = subprojectInfoOne.getString("executor");
			String description = subprojectInfoOne.getString("description");
			if (JSONNull.getInstance().equals(pid)) {
				GenericProject instance2nd = new GenericProject();
				instance2nd.setTheValue(name);
				instance2nd.setGenericProject(instance);
				instance.setBottomLevel(false);// 驳回假定最底级
				instance2nd.setTheLevel(2);// 级别为2
				instance2nd.setBottomLevel(true);// 假定最底级
				instance2nd.setBudgetAmount(amount);
				instance2nd.setTheDescription(description);
				instance2nd.setProjectType(instance.getProjectType());// 继承顶级项目类型
				instance2nd.setYsFundsSource(instance.getYsFundsSource());// 继承顶级资金来源
				instance2nd.setYsDepartmentInfo(instance.getYsDepartmentInfo());// 继承顶级主管科室
				instance2nd.setTopLevelProjectId(instance.getTheId());// 绑定顶级项目id
				getEntityManager().persist(instance2nd);
				if (executor != null && !"".equals(executor)) {
					String[] idArr = executor.split(",");
					if (idArr != null && idArr.length > 0) {
						for (String id : idArr) {
							GenericProjectExecutor theExecutor = new GenericProjectExecutor();
							theExecutor.setProjectId(instance2nd.getTheId());
							theExecutor.setUserInfoId(Integer.valueOf(id));
							getEntityManager().persist(theExecutor);
						}
					}
				}
				persist3rdPlus(subprojectInfoAll, Integer.parseInt(key.toString()), instance2nd);
			}
		}
		getEntityManager().flush();
		raiseAfterTransactionSuccessEvent();
		return "persisted";
	}

	/** 处理三级项目+ */
	public void persist3rdPlus(JSONObject subprojectInfoAll, int pid, GenericProject parentInstance) {
		for (Object key : subprojectInfoAll.keySet()) {
			JSONObject subprojectInfoOne = subprojectInfoAll.getJSONObject(key.toString());
			Object pid3rdPlus = subprojectInfoOne.get("pid");
			if (!JSONNull.getInstance().equals(pid3rdPlus) && Integer.parseInt(pid3rdPlus.toString()) == pid) {
				String name = subprojectInfoOne.getString("name");
				Double amount = subprojectInfoOne.getDouble("amount");
				String executor = subprojectInfoOne.getString("executor");
				String description = subprojectInfoOne.getString("description");
				GenericProject instance3rdPlus = new GenericProject();
				instance3rdPlus.setTheValue(name);
				instance3rdPlus.setGenericProject(parentInstance);
				parentInstance.setBottomLevel(false);// 驳回假定最底级
				instance3rdPlus.setTheLevel(parentInstance.getTheLevel() + 1);// 级别为3+
				instance3rdPlus.setBottomLevel(true);// 假定最底级
				instance3rdPlus.setBudgetAmount(amount);
				instance3rdPlus.setTheDescription(description);
				instance3rdPlus.setProjectType(instance.getProjectType());// 继承顶级项目类型
				instance3rdPlus.setYsFundsSource(instance.getYsFundsSource());// 继承顶级资金来源
				instance3rdPlus.setYsDepartmentInfo(instance.getYsDepartmentInfo());// 继承顶级主管科室
				instance3rdPlus.setTopLevelProjectId(instance.getTheId());// 绑定顶级项目id
				getEntityManager().persist(instance3rdPlus);
				if (executor != null && !"".equals(executor)) {
					String[] idArr = executor.split(",");
					if (idArr != null && idArr.length > 0) {
						for (String id : idArr) {
							GenericProjectExecutor theExecutor = new GenericProjectExecutor();
							theExecutor.setProjectId(instance3rdPlus.getTheId());
							theExecutor.setUserInfoId(Integer.valueOf(id));
							getEntityManager().persist(theExecutor);
						}
					}
				}
				persist3rdPlus(subprojectInfoAll, Integer.parseInt(key.toString()), instance3rdPlus);
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public String update() {
		setMessage("");

		joinTransaction();
		// 处理一级项目
		if (departmentInfoId != null) {
			instance.setYsDepartmentInfo(getEntityManager().find(YsDepartmentInfo.class, departmentInfoId));
		} else {
			instance.setYsDepartmentInfo(null);
		}
		if (fundsSourceId != null) {
			instance.setYsFundsSource(getEntityManager().find(YsFundsSource.class, fundsSourceId));
		} else {
			instance.setYsFundsSource(null);
		}
		// 处理一级项目-清理旧数据
		getEntityManager().createNativeQuery("delete from generic_project_compiler where project_id = " + instance.getTheId()).executeUpdate();
		getEntityManager().createNativeQuery("delete from generic_project_executor where project_id = " + instance.getTheId()).executeUpdate();
		if (budgetPersonCompilerIds != null && !"".equals(budgetPersonCompilerIds)) {
			String[] idArr = budgetPersonCompilerIds.split(",");
			if (idArr != null && idArr.length > 0) {
				for (String id : idArr) {
					GenericProjectCompiler theCompiler = new GenericProjectCompiler();
					theCompiler.setProjectId(instance.getTheId());
					theCompiler.setUserInfoId(Integer.valueOf(id));
					getEntityManager().persist(theCompiler);
				}
			}
		}
		if (budgetPersonExecutorIds != null && !"".equals(budgetPersonExecutorIds)) {
			String[] idArr = budgetPersonExecutorIds.split(",");
			if (idArr != null && idArr.length > 0) {
				for (String id : idArr) {
					GenericProjectExecutor theExecutor = new GenericProjectExecutor();
					theExecutor.setProjectId(instance.getTheId());
					theExecutor.setUserInfoId(Integer.valueOf(id));
					getEntityManager().persist(theExecutor);
				}
			}
		}
		instance.setTheLevel(1);// 级别为1
		instance.setBottomLevel(true);// 假定最底级
		instance.setUpdateTime(new Date());
		instance.setUpdateUser(sessionToken.getUserInfoId());
		getEntityManager().merge(instance);
		instance.setTopLevelProjectId(instance.getTheId());// 顶级条目的顶级项目id为自身（修复老数据）
		// 处理二级项目-清理旧数据
		List<Object[]> nexusList = getEntityManager().createNativeQuery("select the_id, the_pid from generic_project").getResultList();
		List<Object> subProjectIdList = new ArrayList<Object>();
		findSubProjectIds(nexusList, subProjectIdList, instance.getTheId());// 填充子项目id列表
		String subProjectIds = subProjectIdList.toString().substring(1, subProjectIdList.toString().length() - 1);// 处理id列表
		if (subProjectIds != null && !"".equals(subProjectIds)) {
			getEntityManager().createNativeQuery("delete from generic_project where the_id in (" + subProjectIds + ")").executeUpdate();
			getEntityManager().createNativeQuery("delete from generic_project_executor where project_id in (" + subProjectIds + ")").executeUpdate();
		}
		// 处理二级项目-创建新数据
		JSONObject subprojectInfoAll = JSONObject.fromObject(subprojectInfo);
		for (Object key : subprojectInfoAll.keySet()) {
			JSONObject subprojectInfoOne = subprojectInfoAll.getJSONObject(key.toString());
			Object pid = subprojectInfoOne.get("pid");
			String name = subprojectInfoOne.getString("name");
			Double amount = subprojectInfoOne.getDouble("amount");
			String executor = subprojectInfoOne.getString("executor");
			String description = subprojectInfoOne.getString("description");
			if (JSONNull.getInstance().equals(pid)) {
				GenericProject instance2nd = new GenericProject();
				instance2nd.setTheValue(name);
				instance2nd.setGenericProject(instance);
				instance.setBottomLevel(false);// 驳回假定最底级
				instance2nd.setTheLevel(2);// 级别为2
				instance2nd.setBottomLevel(true);// 假定最底级
				instance2nd.setBudgetAmount(amount);
				instance2nd.setTheDescription(description);
				instance2nd.setProjectType(instance.getProjectType());// 继承顶级项目类型
				instance2nd.setYsFundsSource(instance.getYsFundsSource());// 继承顶级资金来源
				instance2nd.setYsDepartmentInfo(instance.getYsDepartmentInfo());// 继承顶级主管科室
				instance2nd.setTopLevelProjectId(instance.getTheId());// 绑定顶级项目id
				getEntityManager().persist(instance2nd);
				if (executor != null && !"".equals(executor)) {
					String[] idArr = executor.split(",");
					if (idArr != null && idArr.length > 0) {
						for (String id : idArr) {
							GenericProjectExecutor theExecutor = new GenericProjectExecutor();
							theExecutor.setProjectId(instance2nd.getTheId());
							theExecutor.setUserInfoId(Integer.valueOf(id));
							getEntityManager().persist(theExecutor);
						}
					}
				}
				persist3rdPlus(subprojectInfoAll, Integer.parseInt(key.toString()), instance2nd);
			}
		}
		getEntityManager().flush();
		raiseAfterTransactionSuccessEvent();
		return "updated";
	}

	public void findSubProjectIds(List<Object[]> nexusList, List<Object> outputList, Object pid) {
		if (nexusList != null && nexusList.size() > 0) {
			for (Object[] nexus : nexusList) {
				if (pid.equals(nexus[1])) {
					outputList.add(nexus[0]);
					findSubProjectIds(nexusList, outputList, nexus[0]);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public String remove() {
		setMessage("");

		getInstance();

		joinTransaction();
		// 处理一级项目-清理旧数据
		getEntityManager().createNativeQuery("delete from generic_project_compiler where project_id = " + instance.getTheId()).executeUpdate();
		getEntityManager().createNativeQuery("delete from generic_project_executor where project_id = " + instance.getTheId()).executeUpdate();
		// 处理二级项目-清理旧数据
		List<Object[]> nexusList = getEntityManager().createNativeQuery("select the_id, the_pid from generic_project").getResultList();
		List<Object> subProjectIdList = new ArrayList<Object>();
		findSubProjectIds(nexusList, subProjectIdList, instance.getTheId());// 填充子项目id列表
		String subProjectIds = subProjectIdList.toString().substring(1, subProjectIdList.toString().length() - 1);// 处理id列表
		if (subProjectIds != null && !"".equals(subProjectIds)) {
			getEntityManager().createNativeQuery("delete from generic_project where the_id in (" + subProjectIds + ")").executeUpdate();
			getEntityManager().createNativeQuery("delete from generic_project_executor where project_id in (" + subProjectIds + ")").executeUpdate();
		}
		getEntityManager().remove(instance);
		getEntityManager().flush();
		raiseAfterTransactionSuccessEvent();
		return "removed";
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
		fundsSourceId = fundsSourceId != null ? fundsSourceId : (instance.getYsFundsSource() != null ? instance.getYsFundsSource().getTheId() : null);
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
	private void wirePersonJson() {
		if (personJson != null) {
			personJson.clear();
		} else {
			personJson = new JSONObject();
		}
		StringBuffer dataSql = new StringBuffer();
		dataSql.append(" select");
		dataSql.append(" user_info.user_info_id,");
		dataSql.append(" user_info_extend.fullname");
		dataSql.append(" from user_info");
		dataSql.append(" inner join user_info_extend on user_info_extend.user_info_extend_id = user_info.user_info_extend_id");
		dataSql.append(" where user_info.deleted = 0");
		List<Object[]> dataList = getEntityManager().createNativeQuery(dataSql.toString()).getResultList();
		if (dataList != null && dataList.size() > 0) {
			for (Object[] data : dataList) {
				personJson.accumulate(data[0].toString(), data[1].toString());
			}
		}
	}

	private void parasitic3rdPlus(List<Object[]> prj3rdPlusList, Map<Object, Object> theExecutorMap, JSONObject host, Object pid) {
		if (prj3rdPlusList != null && prj3rdPlusList.size() > 0) {
			for (Object[] prj3rdPlus : prj3rdPlusList) {
				if (pid.equals(prj3rdPlus[1])) {
					Object id = prj3rdPlus[0];
					JSONObject itemValue = new JSONObject();
					itemValue.accumulate("id", id);
					itemValue.accumulate("name", prj3rdPlus[2]);
					itemValue.accumulate("pid", host.getJSONObject(pid.toString()).get("id"));
					itemValue.accumulate("level", host.getJSONObject(pid.toString()).getInt("level") + 1);
					itemValue.accumulate("amount", prj3rdPlus[3]);
					itemValue.accumulate("executor", theExecutorMap.get(id));
					itemValue.accumulate("description", prj3rdPlus[4]);
					host.accumulate(id.toString(), itemValue);
					parasitic3rdPlus(prj3rdPlusList, theExecutorMap, host, id);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void wire() {
		getInstance();

		wireDepartmentInfo();// 主管科室list
		wireFundsSource();// 资金来源list
		wireBudgetPerson();// 预算人员list
		wirePersonJson();// 人员json

		if (!isFirstTime) {
			departmentInfoId = instance.getYsDepartmentInfo() != null ? instance.getYsDepartmentInfo().getTheId() : null;

			if (isManaged()) {
				// 加载一级项目编制人
				List<Object> budgetPersonCompilerIdsList = getEntityManager().createNativeQuery("select IFNULL(GROUP_CONCAT(user_info_id), '') as result from generic_project_compiler where project_id = " + instance.getTheId()).getResultList();
				if (budgetPersonCompilerIdsList != null && budgetPersonCompilerIdsList.size() > 0) {
					budgetPersonCompilerIds = budgetPersonCompilerIdsList.get(0).toString();
				}
				// 加载一级项目执行人
				List<Object> budgetPersonExecutorIdsList = getEntityManager().createNativeQuery("select IFNULL(GROUP_CONCAT(user_info_id), '') as result from generic_project_executor where project_id = " + instance.getTheId()).getResultList();
				if (budgetPersonExecutorIdsList != null && budgetPersonExecutorIdsList.size() > 0) {
					budgetPersonExecutorIds = budgetPersonExecutorIdsList.get(0).toString();
				}
				// 加载二级项目信息
				JSONObject subprojectInfoJson = new JSONObject();
				List<Object[]> theExecutorList = getEntityManager().createNativeQuery("select project_id, IFNULL(GROUP_CONCAT(user_info_id), '') as result from generic_project_executor group by project_id").getResultList();
				Map<Object, Object> theExecutorMap = new HashMap<Object, Object>();
				if (theExecutorList != null && theExecutorList.size() > 0) {
					for (Object[] theExecutor : theExecutorList) {
						theExecutorMap.put(theExecutor[0], theExecutor[1]);
					}
				}
				List<Object[]> prj3rdPlusList = getEntityManager().createNativeQuery("select the_id, the_pid, the_value, budget_amount, the_description from generic_project").getResultList();
				List<Object[]> prj2ndList = getEntityManager().createNativeQuery("select the_id, the_value, budget_amount, the_description from generic_project where the_pid = " + instance.getTheId()).getResultList();
				if (prj2ndList != null && prj2ndList.size() > 0) {
					for (Object[] prj2nd : prj2ndList) {
						Object id = prj2nd[0];
						JSONObject itemValue = new JSONObject();
						itemValue.accumulate("id", id);
						itemValue.accumulate("name", prj2nd[1]);
						itemValue.accumulate("pid", null);
						itemValue.accumulate("level", 2);
						itemValue.accumulate("amount", prj2nd[2]);
						itemValue.accumulate("executor", theExecutorMap.get(id));
						itemValue.accumulate("description", prj2nd[3]);
						subprojectInfoJson.accumulate(id.toString(), itemValue);
						parasitic3rdPlus(prj3rdPlusList, theExecutorMap, subprojectInfoJson, id);
					}
				}
				this.subprojectInfoJson = subprojectInfoJson;// 避免后台toString()到前台使用JSON.parse解析含有回车符文本报错
			} else {
				this.subprojectInfoJson = new JSONObject();
			}

			isFirstTime = true;
		}
	}

	public void setGenericProjectTheId(Integer id) {
		setId(id);
	}

	public Integer getGenericProjectTheId() {
		return (Integer) getId();
	}

	@Override
	protected GenericProject createInstance() {
		GenericProject genericProject = new GenericProject();
		return genericProject;
	}

	public void load() {
		if (isIdDefined()) {
			wire();
		}
	}

	public boolean isWired() {
		return true;
	}

	public GenericProject getDefinedInstance() {
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

	public List<Object[]> getFundsSourceList() {
		return fundsSourceList;
	}

	public Integer getFundsSourceId() {
		return fundsSourceId;
	}

	public void setFundsSourceId(Integer fundsSourceId) {
		this.fundsSourceId = fundsSourceId;
	}

	public List<Object[]> getBudgetPersonList() {
		return budgetPersonList;
	}

	public String getBudgetPersonCompilerIds() {
		return budgetPersonCompilerIds;
	}

	public void setBudgetPersonCompilerIds(String budgetPersonCompilerIds) {
		this.budgetPersonCompilerIds = budgetPersonCompilerIds;
	}

	public String getBudgetPersonExecutorIds() {
		return budgetPersonExecutorIds;
	}

	public void setBudgetPersonExecutorIds(String budgetPersonExecutorIds) {
		this.budgetPersonExecutorIds = budgetPersonExecutorIds;
	}

	public JSONObject getPersonJson() {
		return personJson;
	}

	public String getSubprojectInfo() {
		return subprojectInfo;
	}

	public void setSubprojectInfo(String subprojectInfo) {
		this.subprojectInfo = subprojectInfo;
	}

	public JSONObject getSubprojectInfoJson() {
		return subprojectInfoJson;
	}

}
