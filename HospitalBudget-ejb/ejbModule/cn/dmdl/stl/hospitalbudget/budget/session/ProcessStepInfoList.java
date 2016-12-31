package cn.dmdl.stl.hospitalbudget.budget.session;

import java.util.List;

import javax.persistence.Query;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.budget.entity.ProcessStepInfo;
import cn.dmdl.stl.hospitalbudget.common.session.CriterionEntityQuery;

@Name("processStepInfoList")
public class ProcessStepInfoList extends CriterionEntityQuery<ProcessStepInfo> {

	private static final long serialVersionUID = 1L;

	private static final String EJBQL = "select processStepInfo from ProcessStepInfo processStepInfo where 0 = 0";
	private ProcessStepInfo processStepInfo = new ProcessStepInfo();
	private String keyword;// 关键词
	private Integer processInfoId;// 流程信息id
	private JSONArray sortableEcho4displayOrder;// sortable回显数据
	private String sortableArgs4displayOrder;// sortable参数（推荐使用ids）
	private JSONObject sortableResult4displayOrder;// sortable返回结果

	/** sortable回显数据初始化 */
	@SuppressWarnings("unchecked")
	public void sortableInit4displayOrder() {
		if (sortableEcho4displayOrder != null) {
			sortableEcho4displayOrder.clear();
		} else {
			sortableEcho4displayOrder = new JSONArray();
		}
		List<ProcessStepInfo> processStepInfoList = getEntityManager().createQuery(EJBQL + " and processStepInfo.processInfo.processInfoId = " + processInfoId + " order by processStepInfo.stepIndex asc").getResultList();
		if (processStepInfoList != null && processStepInfoList.size() > 0) {
			for (ProcessStepInfo processStepInfo : processStepInfoList) {
				JSONObject sortableEcho = new JSONObject();
				sortableEcho.accumulate("value", processStepInfo.getProcessStepInfoId());
				sortableEcho.accumulate("label", processStepInfo.getStepName());
				sortableEcho4displayOrder.add(sortableEcho);
			}
		}
	}

	/** sortable排序实现 */
	public void sortableInvoke4displayOrder() {
		if (sortableResult4displayOrder != null) {
			sortableResult4displayOrder.clear();
		} else {
			sortableResult4displayOrder = new JSONObject();
		}
		sortableResult4displayOrder.accumulate("message", "排序成功！");
		if (sortableArgs4displayOrder != null && !"".equals(sortableArgs4displayOrder)) {
			String[] args = sortableArgs4displayOrder.split(",");
			if (args != null && args.length > 0) {
				for (int i = 0; i < args.length; i++) {
					ProcessStepInfo processStepInfo = getEntityManager().find(ProcessStepInfo.class, Integer.valueOf(args[i]));
					processStepInfo.setStepIndex(i + 1);
					getEntityManager().merge(processStepInfo);
				}
				getEntityManager().flush();
			}
		} else {
			sortableResult4displayOrder.element("message", "排序失败！参数为空。");
		}
	}

	/** 拉取员工 */
	@SuppressWarnings("unchecked")
	public String pullUser(Integer processStepInfoId, Integer type) {
		String result = "";
		if (processStepInfoId != null && type != null) {
			StringBuffer dataSql = new StringBuffer();
			dataSql.append(" select");
			dataSql.append(" IFNULL(GROUP_CONCAT(user_info_extend.fullname), '') as result");
			dataSql.append(" from process_step_user");
			dataSql.append(" inner join user_info on user_info.user_info_id = process_step_user.user_id");
			dataSql.append(" inner join user_info_extend on user_info_extend.user_info_extend_id = user_info.user_info_extend_id");
			dataSql.append(" where process_step_user.process_step_info_id = ").append(processStepInfoId).append(" and process_step_user.type = ").append(type);
			List<Object> dataList = getEntityManager().createNativeQuery(dataSql.toString()).getResultList();
			if (dataList != null && dataList.size() > 0) {
				result = dataList.get(0).toString();
			}
		}
		return result;
	}

	@Override
	protected Query createQuery() {
		String sql = EJBQL;
		if (processInfoId != null) {
			sql += " and processStepInfo.processInfo.processInfoId = " + processInfoId;
		} else {
			sql += " and 0 <> 0";
		}
		if (keyword != null && !"".equals(keyword)) {
			sql += " and processStepInfo.stepName like '%" + keyword + "%'";
		}
		setEjbql(sql);
		return super.createQuery();
	}

	public ProcessStepInfoList() {
		setEjbql(EJBQL);
		setAttribute("processStepInfo.stepIndex");
		setOrderDirection(DIR_ASC);
	}

	public ProcessStepInfo getProcessStepInfo() {
		return processStepInfo;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public Integer getProcessInfoId() {
		return processInfoId;
	}

	public void setProcessInfoId(Integer processInfoId) {
		this.processInfoId = processInfoId;
	}

	public String getSortableArgs4displayOrder() {
		return sortableArgs4displayOrder;
	}

	public void setSortableArgs4displayOrder(String sortableArgs4displayOrder) {
		this.sortableArgs4displayOrder = sortableArgs4displayOrder;
	}

	public JSONArray getSortableEcho4displayOrder() {
		return sortableEcho4displayOrder;
	}

	public JSONObject getSortableResult4displayOrder() {
		return sortableResult4displayOrder;
	}

}
