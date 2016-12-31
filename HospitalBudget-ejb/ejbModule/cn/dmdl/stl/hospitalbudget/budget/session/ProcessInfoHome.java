package cn.dmdl.stl.hospitalbudget.budget.session;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.budget.entity.ProcessInfo;
import cn.dmdl.stl.hospitalbudget.common.session.CriterionEntityHome;
import cn.dmdl.stl.hospitalbudget.hospital.entity.YsDepartmentInfo;
import cn.dmdl.stl.hospitalbudget.util.Assit;

@Name("processInfoHome")
public class ProcessInfoHome extends CriterionEntityHome<ProcessInfo> {

	private static final long serialVersionUID = 1L;
	private boolean isFirstTime;// 首次标记（提交表单后返回错误状态重载页面控件，如下拉框。。。）
	private List<Object[]> departmentInfoList;// 科室select
	private Integer departmentInfoId;// 科室id

	@Override
	public String persist() {
		setMessage("");

		if (Assit.getResultSetSize("select process_info_id from process_info where deleted = 0 and process_name = '" + instance.getProcessName() + "'") > 0) {
			setMessage("此流程名称太受欢迎,请更换一个");
			return null;
		}

		joinTransaction();
		if (departmentInfoId != null) {
			instance.setYsDepartmentInfo(getEntityManager().find(YsDepartmentInfo.class, departmentInfoId));
		} else {
			instance.setYsDepartmentInfo(null);
		}
		instance.setInsertTime(new Date());
		instance.setInsertUser(sessionToken.getUserInfoId());
		getEntityManager().persist(instance);
		getEntityManager().flush();
		raiseAfterTransactionSuccessEvent();
		return "persisted";
	}

	@Override
	public String update() {
		setMessage("");

		if (Assit.getResultSetSize("select process_info_id from process_info where deleted = 0 and process_name = '" + instance.getProcessName() + "' and process_info_id != " + instance.getProcessInfoId()) > 0) {
			setMessage("此流程名称太受欢迎,请更换一个");
			return null;
		}

		joinTransaction();
		if (departmentInfoId != null) {
			instance.setYsDepartmentInfo(getEntityManager().find(YsDepartmentInfo.class, departmentInfoId));
		} else {
			instance.setYsDepartmentInfo(null);
		}
		instance.setUpdateTime(new Date());
		instance.setUpdateUser(sessionToken.getUserInfoId());
		getEntityManager().flush();
		raiseAfterTransactionSuccessEvent();
		return "updated";
	}

	@Override
	public String remove() {
		setMessage("");

		getInstance();

		if (Assit.getResultSetSize("select process_step_info_id from process_step_info where process_info_id = " + instance.getProcessInfoId()) > 0) {
			setMessage("该项已被使用，操作被取消");
			return "failure";
		}

		instance.setDeleted(true);
		getEntityManager().flush();
		raiseAfterTransactionSuccessEvent();
		return "removed";
	}

	public void setProcessInfoProcessInfoId(Integer id) {
		setId(id);
	}

	public Integer getProcessInfoProcessInfoId() {
		return (Integer) getId();
	}

	@Override
	protected ProcessInfo createInstance() {
		ProcessInfo processInfo = new ProcessInfo();
		return processInfo;
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

	public void wire() {
		getInstance();

		wireDepartmentInfo();// 主管科室

		if (!isFirstTime) {
			departmentInfoId = instance.getYsDepartmentInfo() != null ? instance.getYsDepartmentInfo().getTheId() : null;

			isFirstTime = true;
		}
	}

	public boolean isWired() {
		return true;
	}

	public ProcessInfo getDefinedInstance() {
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

}
