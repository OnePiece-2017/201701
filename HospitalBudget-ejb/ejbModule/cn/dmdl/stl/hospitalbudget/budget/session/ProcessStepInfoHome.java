package cn.dmdl.stl.hospitalbudget.budget.session;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.budget.entity.ProcessInfo;
import cn.dmdl.stl.hospitalbudget.budget.entity.ProcessStepInfo;
import cn.dmdl.stl.hospitalbudget.budget.entity.ProcessStepUser;
import cn.dmdl.stl.hospitalbudget.common.session.CriterionEntityHome;
import cn.dmdl.stl.hospitalbudget.util.Assit;

@Name("processStepInfoHome")
public class ProcessStepInfoHome extends CriterionEntityHome<ProcessStepInfo> {

	private static final long serialVersionUID = 1L;
	private boolean isFirstTime;// 首次标记（提交表单后返回错误状态重载页面控件，如下拉框。。。）
	private Integer processInfoId;// 流程信息id
	private List<Object[]> stepUserList;// 员工list 0科室名称 1该科室下的人员list
	private String stepUserIds;// 员工ids

	@SuppressWarnings("unchecked")
	@Override
	public String persist() {
		setMessage("");

		if (Assit.getResultSetSize("select process_step_info_id from process_step_info where process_info_id = " + processInfoId + " and step_name = '" + instance.getStepName() + "'") > 0) {
			setMessage("此步骤名称太受欢迎,请更换一个");
			return null;
		}

		joinTransaction();
		getEntityManager().persist(instance);
		if (stepUserIds != null && !"".equals(stepUserIds)) {
			String[] idArr = stepUserIds.split(",");
			if (idArr != null && idArr.length > 0) {
				for (String id : idArr) {
					ProcessStepUser processStepUser = new ProcessStepUser();
					processStepUser.setProcessStepInfoId(instance.getProcessStepInfoId());
					processStepUser.setUserId(Integer.valueOf(id));
					processStepUser.setType(0);// 0-处理人 1-通知人
					getEntityManager().persist(processStepUser);
				}
			}
		}
		int newDisplayOrder = 1;
		List<Object> newDisplayOrderList = getEntityManager().createNativeQuery("select ifnull(max(step_index), 0) + 1 as new_display_order from process_step_info where process_info_id = " + processInfoId).getResultList();
		if (newDisplayOrderList != null) {
			newDisplayOrder = Integer.parseInt(newDisplayOrderList.get(0).toString());
		}
		instance.setStepIndex(newDisplayOrder);
		getEntityManager().flush();
		raiseAfterTransactionSuccessEvent();
		return "persisted";
	}

	@Override
	public String update() {
		setMessage("");

		if (Assit.getResultSetSize("select process_step_info_id from process_step_info where process_info_id = " + processInfoId + " and step_name = '" + instance.getStepName() + "' and process_step_info_id != " + instance.getProcessStepInfoId()) > 0) {
			setMessage("此步骤名称太受欢迎,请更换一个");
			return null;
		}

		joinTransaction();
		// 清理中间表
		getEntityManager().createNativeQuery("delete from process_step_user where process_step_info_id = " + instance.getProcessStepInfoId()).executeUpdate();
		if (stepUserIds != null && !"".equals(stepUserIds)) {
			String[] idArr = stepUserIds.split(",");
			if (idArr != null && idArr.length > 0) {
				for (String id : idArr) {
					ProcessStepUser processStepUser = new ProcessStepUser();
					processStepUser.setProcessStepInfoId(instance.getProcessStepInfoId());
					processStepUser.setUserId(Integer.valueOf(id));
					processStepUser.setType(0);// 0-处理人 1-通知人
					getEntityManager().persist(processStepUser);
				}
			}
		}
		getEntityManager().flush();
		raiseAfterTransactionSuccessEvent();
		return "updated";
	}

	@Override
	public String remove() {
		setMessage("");

		getInstance();

		joinTransaction();
		// 清理中间表
		getEntityManager().createNativeQuery("delete from process_step_user where process_step_info_id = " + instance.getProcessStepInfoId()).executeUpdate();
		getEntityManager().remove(instance);
		getEntityManager().flush();
		rebuildDisplayOrder();
		return "removed";
	}

	/** 重建顺序（必须在flush之后调用） */
	@SuppressWarnings("unchecked")
	private void rebuildDisplayOrder() {
		String dataSql = "select processStepInfo from ProcessStepInfo processStepInfo where processStepInfo.processInfo.processInfoId = " + processInfoId + " order by processStepInfo.stepIndex asc";
		List<ProcessStepInfo> dataList = getEntityManager().createQuery(dataSql).getResultList();
		if (dataList != null && dataList.size() > 0) {
			Collections.sort(dataList, new Comparator<ProcessStepInfo>() {
				public int compare(ProcessStepInfo o1, ProcessStepInfo o2) {
					if (o1.getStepIndex() < o2.getStepIndex()) {
						return -1;
					} else {
						return 1;
					}
				}
			});
			int displayOrder = 1;
			for (ProcessStepInfo processStepInfo : dataList) {
				processStepInfo.setStepIndex(displayOrder++);
				getEntityManager().merge(processStepInfo);
			}
			getEntityManager().flush();
		}
	}

	public void setProcessStepInfoProcessStepInfoId(Integer id) {
		setId(id);
	}

	public Integer getProcessStepInfoProcessStepInfoId() {
		return (Integer) getId();
	}

	@Override
	protected ProcessStepInfo createInstance() {
		ProcessStepInfo processStepInfo = new ProcessStepInfo();
		return processStepInfo;
	}

	public void load() {
		if (isIdDefined()) {
			wire();
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

		if (stepUserList != null) {
			stepUserList.clear();
		} else {
			stepUserList = new ArrayList<Object[]>();
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
					stepUserList.add(new Object[] { valueMap.get(root), userMap.get(root) });
				}
				disposeLeafBudgetPerson(stepUserList, userMap, nexusMap, valueMap, 1, nexusMap.get(root));
			}
		}

	}

	@SuppressWarnings("unchecked")
	public void wire() {
		getInstance();
		instance.setProcessInfo(getEntityManager().find(ProcessInfo.class, processInfoId));

		wireBudgetPerson();// 预算相关人员

		if (!isFirstTime) {

			if (isManaged()) {
				List<Object> stepUserIdsList = getEntityManager().createNativeQuery("select IFNULL(GROUP_CONCAT(user_id), '') as result from process_step_user where process_step_info_id = " + instance.getProcessStepInfoId()).getResultList();
				if (stepUserIdsList != null && stepUserIdsList.size() > 0) {
					stepUserIds = stepUserIdsList.get(0).toString();
				}
			}

			isFirstTime = true;
		}

	}

	public boolean isWired() {
		return true;
	}

	public ProcessStepInfo getDefinedInstance() {
		return isIdDefined() ? getInstance() : null;
	}

	public Integer getProcessInfoId() {
		return processInfoId;
	}

	public void setProcessInfoId(Integer processInfoId) {
		this.processInfoId = processInfoId;
	}

	public List<Object[]> getStepUserList() {
		return stepUserList;
	}

	public String getStepUserIds() {
		return stepUserIds;
	}

	public void setStepUserIds(String stepUserIds) {
		this.stepUserIds = stepUserIds;
	}

}
