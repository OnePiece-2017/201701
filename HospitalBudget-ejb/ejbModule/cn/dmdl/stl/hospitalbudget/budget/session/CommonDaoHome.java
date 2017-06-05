package cn.dmdl.stl.hospitalbudget.budget.session;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.common.session.CriterionEntityHome;

/**
 * 公共数据查询类
 * @author HASEE
 *
 */
@Name("commonDaoHome")
public class CommonDaoHome extends CriterionEntityHome<Object>{
	private static final long serialVersionUID = 1L;
	
	/**
	 * 获取前后一年的年份，用于各页面筛选条件
	 * @return
	 */
	public List<Object[]> getRecentYearList() {
		List<Object[]> list = new ArrayList<Object[]>();
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, -1);
		for (int i = 0; i < 3; i++) {
			list.add(new Object[] { calendar.get(Calendar.YEAR), calendar.get(Calendar.YEAR) + "年" });
			calendar.add(Calendar.YEAR, +1);
		}
		return list;
	}
	
	/**
	 * 获取页面资金来源
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Object[]> getFundsSource() {
		List<Object[]> list = new ArrayList<Object[]>();
		String dataSql = "select the_id, the_value from ys_funds_source where deleted = 0";
		List<Object[]> dataList = getEntityManager().createNativeQuery(dataSql).getResultList();
		if (dataList != null && dataList.size() > 0) {
			for (Object[] data : dataList) {
				list.add(new Object[] { data[0], data[1] });
			}
		}
		return list;
	}
	
	/**
	 * 获取项目性质
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Object[]> getProjectNature(){
		List<Object[]> list = new ArrayList<Object[]>();
		String dataSql = "select the_id, the_value from ys_project_nature where deleted = 0";
		List<Object[]> dataList = getEntityManager().createNativeQuery(dataSql).getResultList();
		if (dataList != null && dataList.size() > 0) {
			for (Object[] data : dataList) {
				list.add(new Object[] { data[0], data[1] });
			}
		}
		return list;
	}
	
	/**
	 * 获取全部科室目录
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Object[]> getDepartmentInfoList() {
		List<Object[]> list = new ArrayList<Object[]>();
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
				list.add(new Object[] { root, valueMap.get(root) });
				disposeLeaf(list, nexusMap, valueMap, 1, nexusMap.get(root));
			}
		}
		return list;
	}
	
	
	/**
	 * 根据用户信息获得其权限范围内科室
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Object[]> getDepartmentInfoListByUserId() {
		Integer departmentInfoId = sessionToken.getDepartmentInfoId();
		if(null == departmentInfoId){
			return this.getDepartmentInfoList();
		}else{
			List<Object[]> list = new ArrayList<Object[]>();
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
				if(rootList.contains(departmentInfoId)){//是顶级科室
					for (Object root : rootList) {
						if(root.toString().equals(departmentInfoId.toString())){
							list.add(new Object[] { root, valueMap.get(root) });
							disposeLeaf(list, nexusMap, valueMap, 1, nexusMap.get(root));
						}
					}
				}else if(nexusMap.containsKey(departmentInfoId)){
					for(Object rootKey : nexusMap.keySet()){
						if(null != rootKey){
							if(nexusMap.get(rootKey).contains(departmentInfoId)){
								disposeLeaf(list, nexusMap, valueMap, 1, nexusMap.get(rootKey));
							}
						}
					}
				}else{
					list.add(new Object[] { departmentInfoId, valueMap.get(departmentInfoId) });
				}
			}
			return list;
		}
	}
	/**
	 * 递归处理多级项目的展示逻辑
	 * @param targetList
	 * @param nexusMap
	 * @param valueMap
	 * @param indentWidth
	 * @param leafList
	 */
	private void disposeLeaf(List<Object[]> targetList, Map<Object, List<Object>> nexusMap, Map<Object, Object> valueMap, int indentWidth, List<Object> leafList) {
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

	public String getDepartmentInfoListByUserIdWhereCondition() {
		StringBuffer result = new StringBuffer();
		List<Object[]> list = getDepartmentInfoListByUserId();
		if (list != null && list.size() > 0) {
			for (int i = 0; i < list.size(); i++) {
				result.append(list.get(i)[0]);
				if (i < list.size() - 1) {
					result.append(", ");
				}
			}
		}
		return result.toString();
	}
	
	/**
	 * 获取上一年下达的收入预算
	 * @param year
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Double> getLastYearTotalIncomeBudget(String year) {
		Map<String, Double> map = new HashMap<String, Double>();
		int lastYear = Integer.parseInt(year) - 1;
		String dataSql = "SELECT ipi.dept_id,SUM(ipi.budget_amount) as total_amount from ys_income_plan_info ipi where ipi.year = '" + lastYear + "' GROUP BY ipi.dept_id";
		List<Object[]> dataList = getEntityManager().createNativeQuery(dataSql).getResultList();
		for(Object[] obj : dataList){
			map.put(obj[0].toString(), Double.parseDouble(obj[1].toString()));
		}
		return map;
	}

	/**
	 * 获取上一年瞎打的支出预算
	 * @param year
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Double> getLastYearTotalExpendBudget(String year) {
		Map<String, Double> map = new HashMap<String, Double>();
		int lastYear = Integer.parseInt(year) - 1;
		String dataSql = "SELECT ipi.dept_id,SUM(ipi.budget_amount) as total_amount from normal_expend_plan_info ipi where ipi.year = '" + lastYear + "' GROUP BY ipi.dept_id";
		List<Object[]> dataList = getEntityManager().createNativeQuery(dataSql).getResultList();
		for(Object[] obj : dataList){
			map.put(obj[0].toString(), Double.parseDouble(obj[1].toString()));
		}
		return map;
	}
	
}
