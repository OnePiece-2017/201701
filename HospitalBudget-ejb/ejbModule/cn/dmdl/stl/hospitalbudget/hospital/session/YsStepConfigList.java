package cn.dmdl.stl.hospitalbudget.hospital.session;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.common.session.CriterionEntityQuery;
import cn.dmdl.stl.hospitalbudget.hospital.entity.YsStepConfig;

@Name("ysStepConfigList")
public class YsStepConfigList extends CriterionEntityQuery<YsStepConfig> {

	private static final long serialVersionUID = 1L;
	private static final String EJBQL = "select ysStepConfig from YsStepConfig ysStepConfig where 0 = 0";
	private YsStepConfig ysStepConfig = new YsStepConfig();
	private String keyword;// 关键词
	private Integer workflowId;
	private Map<Integer, String> targetMap;
	private JSONArray stepSortableItems;
	private String sortStepIds;// 排序顺序
	private JSONObject dataSortStep;// 排序结果

	public void invokeSortStep() {
		if (dataSortStep != null) {
			dataSortStep.clear();
		} else {
			if (sortStepIds != null && !"".equals(sortStepIds)) {
				String[] sortStepIdArr = sortStepIds.split(",");
				if (sortStepIdArr != null && sortStepIdArr.length > 0) {
					for (int i = 0; i < sortStepIdArr.length; i++) {
						YsStepConfig ysStepConfig = getEntityManager().find(YsStepConfig.class, Integer.parseInt(sortStepIdArr[i]));
						ysStepConfig.setStepOrder(i + 1);
						getEntityManager().merge(ysStepConfig);
					}
					getEntityManager().flush();
					dataSortStep = new JSONObject();
					dataSortStep.accumulate("message", "排序成功！");
				}
			}
		}
	}

	public String gainTargetLabel(String ids) {
		String result = "";
		if (ids != null && !"".equals(ids)) {
			String[] arr = ids.split(",");
			for (int i = 0; i < arr.length; i++) {
				String label = targetMap.get(Integer.valueOf(arr[i]));
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

	@SuppressWarnings("unchecked")
	@Override
	protected Query createQuery() {
		String sql = EJBQL;
		if (keyword != null && !"".equals(keyword)) {
			sql += " and ysStepConfig.stepName like '%" + keyword + "%'";
		}
		if (workflowId != null) {
			sql += " and ysStepConfig.workflowId = " + workflowId;
		} else {
			sql += " and 0 <> 0";
		}
		setEjbql(sql);
		Query query = super.createQuery();

		if (stepSortableItems != null) {
			stepSortableItems.clear();
		} else {
			stepSortableItems = new JSONArray();
		}
		List<YsStepConfig> ysStepConfigs = query.getResultList();
		if (ysStepConfigs != null && ysStepConfigs.size() > 0) {
			for (YsStepConfig ysStepConfig : ysStepConfigs) {
				JSONObject item = new JSONObject();
				item.accumulate("value", ysStepConfig.getStepId());
				item.accumulate("label", ysStepConfig.getStepName());
				stepSortableItems.add(item);
			}
		}
		return query;
	}

	@SuppressWarnings("unchecked")
	public YsStepConfigList() {
		setEjbql(EJBQL);
		setAttribute("ysStepConfig.stepOrder");
		setOrderDirection(DIR_ASC);

		if (targetMap != null) {
			targetMap.clear();
		} else {
			targetMap = new HashMap<Integer, String>();
		}
		List<Object[]> dataDictList = getEntityManager().createNativeQuery("select dict_id, dict_type, dict_value from data_dict where dict_type in (3, 4)").getResultList();
		if (dataDictList != null && dataDictList.size() > 0) {
			for (Object[] dataDict : dataDictList) {
				targetMap.put(Integer.valueOf(dataDict[0].toString()), dataDict[2].toString());
			}
		}
	}

	public YsStepConfig getYsStepConfig() {
		return ysStepConfig;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public Integer getWorkflowId() {
		return workflowId;
	}

	public void setWorkflowId(Integer workflowId) {
		this.workflowId = workflowId;
	}

	public JSONArray getStepSortableItems() {
		return stepSortableItems;
	}

	public void setStepSortableItems(JSONArray stepSortableItems) {
		this.stepSortableItems = stepSortableItems;
	}

	public String getSortStepIds() {
		return sortStepIds;
	}

	public void setSortStepIds(String sortStepIds) {
		this.sortStepIds = sortStepIds;
	}

	public JSONObject getDataSortStep() {
		return dataSortStep;
	}

	public void setDataSortStep(JSONObject dataSortStep) {
		this.dataSortStep = dataSortStep;
	}

}
