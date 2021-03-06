package cn.dmdl.stl.hospitalbudget.budget.session;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.common.session.CriterionNativeQuery;

@Name("departmentalBudgetList")
public class DepartmentalBudgetList extends CriterionNativeQuery<Object[]> {

	private static final long serialVersionUID = 1L;
	private List<Object[]> beginYear;// 预算年份select
	private List<Object[]> fundsSourceList;// 资金来源list
	private List<Object[]> departmentInfoList;// 科室select
	private String beginYearParam;
	private String beginMonth;
	private String beginDay;
	private String isFlag;
	private String departmentInfoId;
	private String fundsSourceId;

	@In(create = true)
	CommonDaoHome commonDaoHome;

	@Override
	protected Query createQuery() {
		StringBuffer sql = new StringBuffer();
		sql.append(" select nepi.normal_expend_plan_id,if(ydi.the_value is null ,ydi1.the_value,ydi.the_value) as department_value,if(nepi.project_id is null,gp.the_value,rp.the_value) as the_value,");
		sql.append(" if(nepi.project_id is null,nepi.generic_project_id,nepi.project_id)as the_id,if(nepi.project_id is null,2,1) as type,");
		sql.append(" sum(nepi.budget_amount) as budget_amount ,sum(nepi.budget_amount_frozen) as budget_amount_frozen ,sum(nepi.budget_amount_surplus),");
		sql.append(" round(sum(nepi.budget_amount_frozen) / sum(nepi.budget_amount) * 100,2), nepi.year");
		sql.append(" from normal_expend_plan_info nepi");
		sql.append(" left join generic_project gp on gp.the_id = nepi.generic_project_id");
		sql.append(" left join routine_project rp on rp.the_id = nepi.project_id");
		sql.append(" left join ys_department_info ydi on ydi.the_id = gp.department_info_id");
		sql.append(" left join ys_department_info ydi1 on ydi1.the_id = rp.department_info_id");
		sql.append(" where 1 = 1");
//		String wc = commonDaoHome.getDepartmentInfoListByUserIdWhereCondition();
//		if (wc != null && !"".equals(wc)) {
//			sql.append(" and routine_project.department_info_id in (" + wc + ")");
//		}
		
		if(null != beginYearParam && !"".equals(beginYearParam)){
			sql.append(" and nepi.year = ").append(beginYearParam);
		}
		if(null != departmentInfoId && !"".equals(departmentInfoId)){
			sql.append(" and (gp.department_info_id = ").append(departmentInfoId);
			sql.append(" or rp.department_info_id = ").append(departmentInfoId).append(") ");
		}
		sql.append(" group by department_value");
		sql.insert(0, "select * from (").append(") as recordset");
		System.out.println(beginYearParam+"---------"+departmentInfoId+"--------------"+fundsSourceId);
		setEjbql(sql.toString());
		return super.createQuery();
	}
	
	public void wire() {
		wireBudgetYearList();
		wireFundsSource();// 资金来源list
		wireDepartmentInfo();// 主管科室list
	}
	
	/** 预算年份 */
	public void wireBudgetYearList() {
		if (beginYear != null) {
			beginYear.clear();
		} else {
			beginYear = new ArrayList<Object[]>();
		}
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, -1);
		for (int i = 0; i < 3; i++) {
			beginYear.add(new Object[] { calendar.get(Calendar.YEAR), calendar.get(Calendar.YEAR) + "年" });
			calendar.add(Calendar.YEAR, +1);
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

	public DepartmentalBudgetList() {
		setEjbql("");
		setAttribute("");
	}

	public String getBeginMonth() {
		return beginMonth;
	}

	public void setBeginMonth(String beginMonth) {
		this.beginMonth = beginMonth;
	}

	public String getBeginDay() {
		return beginDay;
	}

	public void setBeginDay(String beginDay) {
		this.beginDay = beginDay;
	}

	public String getIsFlag() {
		return isFlag;
	}

	public void setIsFlag(String isFlag) {
		this.isFlag = isFlag;
	}

	public List<Object[]> getBeginYear() {
		return beginYear;
	}

	public void setBeginYear(List<Object[]> beginYear) {
		this.beginYear = beginYear;
	}

	public List<Object[]> getFundsSourceList() {
		return fundsSourceList;
	}

	public void setFundsSourceList(List<Object[]> fundsSourceList) {
		this.fundsSourceList = fundsSourceList;
	}

	public List<Object[]> getDepartmentInfoList() {
		return departmentInfoList;
	}

	public void setDepartmentInfoList(List<Object[]> departmentInfoList) {
		this.departmentInfoList = departmentInfoList;
	}

	public String getDepartmentInfoId() {
		return departmentInfoId;
	}

	public void setDepartmentInfoId(String departmentInfoId) {
		this.departmentInfoId = departmentInfoId;
	}

	public String getFundsSourceId() {
		return fundsSourceId;
	}

	public void setFundsSourceId(String fundsSourceId) {
		this.fundsSourceId = fundsSourceId;
	}

	public String getBeginYearParam() {
		return beginYearParam;
	}

	public void setBeginYearParam(String beginYearParam) {
		this.beginYearParam = beginYearParam;
	}

}
