package cn.dmdl.stl.hospitalbudget.hospital.session;

import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.common.session.CriterionEntityHome;
import cn.dmdl.stl.hospitalbudget.hospital.entity.YsWorkflowConfig;

@Name("ysWorkflowConfigHome")
public class YsWorkflowConfigHome extends CriterionEntityHome<YsWorkflowConfig> {

	private static final long serialVersionUID = 1L;
	private List<Object[]> projectTypesList;// 项目类型select
	private List<Object[]> applicableDepartmentList;// 适用部门select
	private String projectTypes;// 项目类型value
	private String applicableDepartment;// 适用部门value

	@Override
	public String persist() {
		setMessage("");

		instance.setProjectTypes(projectTypes);
		instance.setApplicableDepartment(applicableDepartment);
		getEntityManager().persist(instance);
		getEntityManager().flush();
		raiseAfterTransactionSuccessEvent();
		return "persisted";
	}

	@Override
	public String update() {
		setMessage("");

		instance.setProjectTypes(projectTypes);
		instance.setApplicableDepartment(applicableDepartment);
		joinTransaction();
		getEntityManager().flush();
		raiseAfterTransactionSuccessEvent();
		return "updated";
	}

	@Override
	public String remove() {
		setMessage("");

		getEntityManager().remove(getInstance());
		getEntityManager().flush();
		raiseAfterTransactionSuccessEvent();
		return "removed";
	}

	public void setYsWorkflowConfigWorkflowId(Integer id) {
		setId(id);
	}

	public Integer getYsWorkflowConfigWorkflowId() {
		return (Integer) getId();
	}

	@Override
	protected YsWorkflowConfig createInstance() {
		YsWorkflowConfig ysWorkflowConfig = new YsWorkflowConfig();
		return ysWorkflowConfig;
	}

	public void load() {
		if (isIdDefined()) {
			wire();
		}
	}

	@SuppressWarnings("unchecked")
	public void wire() {
		getInstance();

		if (projectTypesList != null) {
			projectTypesList.clear();
		} else {
			projectTypesList = new ArrayList<Object[]>();
		}
		if (applicableDepartmentList != null) {
			applicableDepartmentList.clear();
		} else {
			applicableDepartmentList = new ArrayList<Object[]>();
		}
		List<Object[]> dataDictList = getEntityManager().createNativeQuery("select dict_id, dict_type, dict_value from data_dict where dict_type in (1, 2)").getResultList();
		if (dataDictList != null && dataDictList.size() > 0) {
			for (Object[] dataDict : dataDictList) {
				int dictType = Integer.valueOf(dataDict[1].toString());
				if (1 == dictType) {
					projectTypesList.add(new Object[] { Integer.valueOf(dataDict[0].toString()), dataDict[2].toString() });
				} else if (2 == dictType) {
					applicableDepartmentList.add(new Object[] { Integer.valueOf(dataDict[0].toString()), dataDict[2].toString() });
				}
			}
		}
		if (isManaged()) {
			projectTypes = projectTypes != null ? projectTypes : instance.getProjectTypes();
			applicableDepartment = applicableDepartment != null ? applicableDepartment : instance.getApplicableDepartment();
		}
	}

	public boolean isWired() {
		return true;
	}

	public YsWorkflowConfig getDefinedInstance() {
		return isIdDefined() ? getInstance() : null;
	}

	public List<Object[]> getProjectTypesList() {
		return projectTypesList;
	}

	public void setProjectTypesList(List<Object[]> projectTypesList) {
		this.projectTypesList = projectTypesList;
	}

	public List<Object[]> getApplicableDepartmentList() {
		return applicableDepartmentList;
	}

	public void setApplicableDepartmentList(List<Object[]> applicableDepartmentList) {
		this.applicableDepartmentList = applicableDepartmentList;
	}

	public String getProjectTypes() {
		return projectTypes;
	}

	public void setProjectTypes(String projectTypes) {
		this.projectTypes = projectTypes;
	}

	public String getApplicableDepartment() {
		return applicableDepartment;
	}

	public void setApplicableDepartment(String applicableDepartment) {
		this.applicableDepartment = applicableDepartment;
	}

}
