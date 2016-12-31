package cn.dmdl.stl.hospitalbudget.hospital.session;

import java.util.List;

import net.sf.json.JSONObject;

import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.common.session.CriterionEntityHome;
import cn.dmdl.stl.hospitalbudget.hospital.entity.YsStepConfig;

@Name("ysStepConfigHome")
public class YsStepConfigHome extends CriterionEntityHome<YsStepConfig> {

	private static final long serialVersionUID = 1L;
	private Integer workflowId;
	private JSONObject selectJson;

	@SuppressWarnings("unchecked")
	@Override
	public String persist() {
		setMessage("");

		List<Object> stepOrderList = getEntityManager().createNativeQuery("select ifnull(max(step_order),0) + 1 from ys_step_config where workflow_id = " + workflowId).getResultList();
		if (stepOrderList != null && stepOrderList.size() > 0) {
			instance.setStepOrder(Integer.parseInt(stepOrderList.get(0).toString()));
		} else {
			instance.setStepOrder(1);
		}
		instance.setWorkflowId(workflowId);
		getEntityManager().persist(instance);
		getEntityManager().flush();
		raiseAfterTransactionSuccessEvent();
		return "persisted";
	}

	@Override
	public String update() {
		setMessage("");

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

	public void setYsStepConfigStepId(Integer id) {
		setId(id);
	}

	public Integer getYsStepConfigStepId() {
		return (Integer) getId();
	}

	@Override
	protected YsStepConfig createInstance() {
		YsStepConfig ysStepConfig = new YsStepConfig();
		return ysStepConfig;
	}

	public void load() {
		if (isIdDefined()) {
			wire();
		}
	}

	@SuppressWarnings("unchecked")
	public void wire() {
		getInstance();

		if (selectJson != null) {
			selectJson.clear();
		} else {
			selectJson = new JSONObject();
		}
		JSONObject employee = new JSONObject();
		JSONObject station = new JSONObject();
		List<Object[]> dataDictList = getEntityManager().createNativeQuery("select dict_id, dict_type, dict_value from data_dict where dict_type in (3, 4)").getResultList();
		if (dataDictList != null && dataDictList.size() > 0) {
			for (Object[] dataDict : dataDictList) {
				int dictType = Integer.valueOf(dataDict[1].toString());
				if (3 == dictType) {
					employee.accumulate(dataDict[0].toString(), dataDict[2].toString());
				} else if (4 == dictType) {
					station.accumulate(dataDict[0].toString(), dataDict[2].toString());
				}
			}
		}
		selectJson.accumulate("employee", employee);
		selectJson.accumulate("station", station);
	}

	public boolean isWired() {
		return true;
	}

	public YsStepConfig getDefinedInstance() {
		return isIdDefined() ? getInstance() : null;
	}

	public Integer getWorkflowId() {
		return workflowId;
	}

	public void setWorkflowId(Integer workflowId) {
		this.workflowId = workflowId;
	}

	public JSONObject getSelectJson() {
		return selectJson;
	}

	public void setSelectJson(JSONObject selectJson) {
		this.selectJson = selectJson;
	}

}
