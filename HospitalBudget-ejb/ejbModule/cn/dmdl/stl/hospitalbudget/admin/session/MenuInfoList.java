package cn.dmdl.stl.hospitalbudget.admin.session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.admin.entity.MenuInfo;
import cn.dmdl.stl.hospitalbudget.common.session.CriterionEntityQuery;

@Name("menuInfoList")
public class MenuInfoList extends CriterionEntityQuery<MenuInfo> {

	private static final long serialVersionUID = 1L;

	private static final String EJBQL = "select menuInfo from MenuInfo menuInfo where menuInfo.deleted = 0";
	private MenuInfo menuInfo = new MenuInfo();
	private String keyword;// 关键词
	private JSONArray sortableEcho4displayOrder;// sortable回显数据
	private String sortableArgs4displayOrder;// sortable参数（推荐使用ids）
	private JSONObject sortableResult4displayOrder;// sortable返回结果
	private List<Object[]> theParentList;// 父级list
	private Integer theParentValue;// 父级id

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

	/** sortable回显数据初始化 */
	@SuppressWarnings("unchecked")
	public void sortableInit4displayOrder() {

		if (theParentList != null) {
			theParentList.clear();
		} else {
			theParentList = new ArrayList<Object[]>();
		}

		theParentList.add(new Object[] { "", "无" });
		Map<Object, List<Object>> nexusMap = new HashMap<Object, List<Object>>();// 上下级关系集合
		Map<Object, Object> valueMap = new HashMap<Object, Object>();// 值集合
		String dataSql = "select the_id, the_pid, the_value from menu_info where deleted = 0";
		dataSql += " order by display_order asc";// 注意：这个排序至关重要，避免使用Collections.sort降低效率。
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
				theParentList.add(new Object[] { root, valueMap.get(root) });
				disposeLeaf(theParentList, nexusMap, valueMap, 1, nexusMap.get(root));
			}
		}

		if (sortableEcho4displayOrder != null) {
			sortableEcho4displayOrder.clear();
		} else {
			sortableEcho4displayOrder = new JSONArray();
		}
		List<MenuInfo> menuInfoList = getEntityManager().createQuery(EJBQL + (theParentValue != null ? " and menuInfo.menuInfo.theId = " + theParentValue : " and menuInfo.menuInfo is null") + " order by menuInfo.displayOrder asc").getResultList();
		if (menuInfoList != null && menuInfoList.size() > 0) {
			for (MenuInfo menuInfo : menuInfoList) {
				JSONObject sortableEcho = new JSONObject();
				sortableEcho.accumulate("value", menuInfo.getTheId());
				sortableEcho.accumulate("label", menuInfo.getTheValue());
				sortableEcho4displayOrder.add(sortableEcho);
			}
		}
	}

	/** sortable拉取实现 */
	@SuppressWarnings("unchecked")
	public void sortablePull4displayOrder() {

		if (sortableEcho4displayOrder != null) {
			sortableEcho4displayOrder.clear();
		} else {
			sortableEcho4displayOrder = new JSONArray();
		}
		List<MenuInfo> menuInfoList = getEntityManager().createQuery(EJBQL + (theParentValue != null ? " and menuInfo.menuInfo.theId = " + theParentValue : " and menuInfo.menuInfo is null") + " order by menuInfo.displayOrder asc").getResultList();
		if (menuInfoList != null && menuInfoList.size() > 0) {
			for (MenuInfo menuInfo : menuInfoList) {
				JSONObject sortableEcho = new JSONObject();
				sortableEcho.accumulate("value", menuInfo.getTheId());
				sortableEcho.accumulate("label", menuInfo.getTheValue());
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
					MenuInfo menuInfo = getEntityManager().find(MenuInfo.class, Integer.valueOf(args[i]));
					menuInfo.setDisplayOrder(i + 1);
					getEntityManager().merge(menuInfo);
				}
				getEntityManager().flush();
			}
		} else {
			sortableResult4displayOrder.element("message", "排序失败！参数为空。");
		}
	}

	@Override
	protected Query createQuery() {
		String sql = EJBQL;
		if (keyword != null && !"".equals(keyword)) {
			sql += " and menuInfo.theValue like '%" + keyword + "%'";
		}
		setEjbql(sql);
		return super.createQuery();
	}

	public MenuInfoList() {
		setEjbql(EJBQL);
		setAttribute("menuInfo.theId");
	}

	public MenuInfo getMenuInfo() {
		return menuInfo;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
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

	public List<Object[]> getTheParentList() {
		return theParentList;
	}

	public Integer getTheParentValue() {
		return theParentValue;
	}

	public void setTheParentValue(Integer theParentValue) {
		this.theParentValue = theParentValue;
	}
}
