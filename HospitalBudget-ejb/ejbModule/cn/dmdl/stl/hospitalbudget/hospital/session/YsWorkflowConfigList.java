package cn.dmdl.stl.hospitalbudget.hospital.session;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.common.session.CriterionEntityQuery;
import cn.dmdl.stl.hospitalbudget.hospital.entity.YsWorkflowConfig;

@Name("ysWorkflowConfigList")
public class YsWorkflowConfigList extends CriterionEntityQuery<YsWorkflowConfig> {

	private static final long serialVersionUID = 1L;
	private static final String EJBQL = "select ysWorkflowConfig from YsWorkflowConfig ysWorkflowConfig where 0 = 0";
	private YsWorkflowConfig ysWorkflowConfig = new YsWorkflowConfig();
	private String keyword;// 关键词
	private Map<Integer, String> projectTypesMap;
	private Map<Integer, String> applicableDepartmentMap;

	public String gainProjectTypesLabel(String ids) {
		String result = "";
		if (ids != null && !"".equals(ids)) {
			String[] arr = ids.split(",");
			for (int i = 0; i < arr.length; i++) {
				String label = projectTypesMap.get(Integer.valueOf(arr[i]));
				if (label != null && !"".equals(label)) {
					result += label;
					if (i < arr.length - 1) {
						result += ",";
					}
				}
			}
		}
		return result;
	}

	public String gainApplicableDepartmentLabel(String ids) {
		String result = "";
		if (ids != null && !"".equals(ids)) {
			String[] arr = ids.split(",");
			for (int i = 0; i < arr.length; i++) {
				String label = applicableDepartmentMap.get(Integer.valueOf(arr[i]));
				if (label != null && !"".equals(label)) {
					result += label;
					if (i < arr.length - 1) {
						result += ",";
					}
				}
			}
		}
		return result;
	}

	@Override
	protected Query createQuery() {
		String sql = EJBQL;
		if (keyword != null && !"".equals(keyword)) {
			sql += " and (ysWorkflowConfig.workflowName like '%" + keyword + "%')";
		}
		setEjbql(sql);
		return super.createQuery();
	}

	@SuppressWarnings("unchecked")
	public YsWorkflowConfigList() {
		setEjbql(EJBQL);
		setAttribute("ysWorkflowConfig.workflowId");

		if (projectTypesMap != null) {
			projectTypesMap.clear();
		} else {
			projectTypesMap = new HashMap<Integer, String>();
		}
		if (applicableDepartmentMap != null) {
			applicableDepartmentMap.clear();
		} else {
			applicableDepartmentMap = new HashMap<Integer, String>();
		}
		List<Object[]> dataDictList = getEntityManager().createNativeQuery("select dict_id, dict_type, dict_value from data_dict where dict_type in (1, 2)").getResultList();
		if (dataDictList != null && dataDictList.size() > 0) {
			for (Object[] dataDict : dataDictList) {
				int dictType = Integer.valueOf(dataDict[1].toString());
				if (1 == dictType) {
					projectTypesMap.put(Integer.valueOf(dataDict[0].toString()), dataDict[2].toString());
				} else if (2 == dictType) {
					applicableDepartmentMap.put(Integer.valueOf(dataDict[0].toString()), dataDict[2].toString());
				}
			}
		}
	}

	public YsWorkflowConfig getYsWorkflowConfig() {
		return ysWorkflowConfig;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
}
