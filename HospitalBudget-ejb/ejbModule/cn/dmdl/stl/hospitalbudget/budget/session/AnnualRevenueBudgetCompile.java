package cn.dmdl.stl.hospitalbudget.budget.session;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.common.session.CriterionEntityHome;
import cn.dmdl.stl.hospitalbudget.util.GlobalConstant;

@Name("annualRevenueBudgetCompile")
public class AnnualRevenueBudgetCompile extends CriterionEntityHome<Object> {
	private static final long serialVersionUID = 1L;

	public List<Object[]> getBudgetYearList() {
		List<Object[]> list = new ArrayList<Object[]>();
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, -1);
		for (int i = 0; i < 3; i++) {
			list.add(new Object[] { calendar.get(Calendar.YEAR), calendar.get(Calendar.YEAR) + "年" });
			calendar.add(Calendar.YEAR, +1);
		}
		return list;
	}

	@SuppressWarnings("unchecked")
	public JSONArray getCandidateProject() {
		JSONArray resultSet = new JSONArray();
		String dataSql = "select the_id, the_type, the_state, the_value, multilevel, total_amount, department_info_id from ys_convention_project where deleted = 0";
		if (GlobalConstant.ROLE_OF_ROOT != sessionToken.getRoleInfoId()) {
			dataSql += " and the_id in (select convention_project_id from ys_convention_project_user where user_info_id = " + sessionToken.getUserInfoId() + ")";
		}
		Map<Object, BigDecimal> multilevelProjectTotalAmountMap = new HashMap<Object, BigDecimal>();
		Map<Object, JSONArray> subItemArrMap = new HashMap<Object, JSONArray>();
		List<Object[]> dataList = getEntityManager().createNativeQuery(dataSql).getResultList();
		if (dataList != null && dataList.size() > 0) {
			List<Object> multilevelProjectList = new ArrayList<Object>();
			for (Object[] data : dataList) {
				if ((Boolean) data[4]) {
					multilevelProjectList.add(data[0]);
				}
			}
			if (multilevelProjectList != null && multilevelProjectList.size() > 0) {
				List<Object[]> projectExtendList = getEntityManager().createNativeQuery("select convention_project_id, the_id, the_value, total_amount, the_description from ys_convention_project_extend").getResultList();
				if (projectExtendList != null && projectExtendList.size() > 0) {
					for (Object[] projectExtend : projectExtendList) {
						JSONObject subItem = new JSONObject();
						subItem.accumulate("theId", projectExtend[1]);
						subItem.accumulate("theValue", projectExtend[2]);
						subItem.accumulate("totalAmount", projectExtend[3]);
						subItem.accumulate("theDescription", projectExtend[4]);
						if (subItemArrMap.get(projectExtend[0]) != null) {
							subItemArrMap.get(projectExtend[0]).add(subItem);
						} else {
							JSONArray subItemArr = new JSONArray();
							subItemArr.add(subItem);
							subItemArrMap.put(projectExtend[0], subItemArr);
						}
						BigDecimal augend = new BigDecimal(projectExtend[3].toString());
						if (multilevelProjectTotalAmountMap.get(projectExtend[0]) != null) {
							multilevelProjectTotalAmountMap.put(projectExtend[0], multilevelProjectTotalAmountMap.get(projectExtend[0]).add(augend));
						} else {
							multilevelProjectTotalAmountMap.put(projectExtend[0], augend);
						}
					}
				}
			}
		}

		if (dataList != null && dataList.size() > 0) {
			List<Object[]> departmentList = getEntityManager().createNativeQuery("select the_id, the_value from ys_department_info where deleted = 0").getResultList();
			Map<Object, Object> departmentMap = new HashMap<Object, Object>();
			if (departmentList != null && departmentList.size() > 0) {
				for (Object[] department : departmentList) {
					departmentMap.put(department[0], department[1]);
				}
			}
			for (Object[] data : dataList) {
				JSONObject row = new JSONObject();
				row.accumulate("projectId", data[0]);
				row.accumulate("projectName", data[3]);
				row.accumulate("projectType", Integer.parseInt(data[1].toString()) == 1 ? "收入预算" : "支出预算");
				row.accumulate("departmentName", departmentMap.get(data[6]));
				boolean multilevel = (Boolean) data[4];
				row.accumulate("multilevel", multilevel);
				if (multilevel) {
					row.accumulate("totalAmount", multilevelProjectTotalAmountMap.get(data[0]).toString());// 显示完整的数据，而不是科学记数法
					row.accumulate("subItemArr", subItemArrMap.get(data[0]));
				} else {
					row.accumulate("totalAmount", data[5]);
					row.accumulate("subItemArr", new JSONArray());
				}
				row.accumulate("theState", Integer.parseInt(data[1].toString()) == 1 ? "已打开" : "已关闭");
				resultSet.add(row);
			}
		}
		return resultSet;
	}
}
