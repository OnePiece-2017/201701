package cn.dmdl.stl.hospitalbudget.admin.session;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.admin.entity.MenuInfo;
import cn.dmdl.stl.hospitalbudget.common.session.CriterionEntityHome;
import cn.dmdl.stl.hospitalbudget.util.Assit;

@Name("menuInfoHome")
public class MenuInfoHome extends CriterionEntityHome<MenuInfo> {

	private static final long serialVersionUID = 1L;
	private List<Object[]> theParentList;// 父级list
	private Integer theParentValue;// 父级id
	private boolean isFirstTime;// 首次标记（提交表单后返回错误状态重载页面控件，如下拉框。。。）

	/** 重建顺序（必须在flush之后调用） */
	@SuppressWarnings("unchecked")
	private void rebuildDisplayOrder(Integer[] pid) {
		if (pid != null && pid.length > 0) {
			String dataSql = "select menuInfo from MenuInfo menuInfo where menuInfo.deleted = 0";
			dataSql += " and (";
			for (int i = 0; i < pid.length; i++) {
				if (i > 0) {
					dataSql += " or ";
				}
				if (pid[i] != null) {
					dataSql += "menuInfo.menuInfo.theId = " + pid[i];
				} else {
					dataSql += "menuInfo.menuInfo is null";
				}
			}
			dataSql += ")";
			List<MenuInfo> menuInfoList = getEntityManager().createQuery(dataSql).getResultList();
			if (menuInfoList != null && menuInfoList.size() > 0) {
				Collections.sort(menuInfoList, new Comparator<MenuInfo>() {
					public int compare(MenuInfo o1, MenuInfo o2) {
						if (o1.getDisplayOrder() < o2.getDisplayOrder()) {
							return -1;
						} else {
							return 1;
						}
					}
				});
				int displayOrder = 1;
				for (MenuInfo menuInfo : menuInfoList) {
					menuInfo.setDisplayOrder(displayOrder++);
					getEntityManager().merge(menuInfo);
				}
				getEntityManager().flush();
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public String persist() {
		setMessage("");

		joinTransaction();
		if ("".equals(instance.getTabTitle())) {
			instance.setTabTitle(null);
		}
		if ("".equals(instance.getTabUrl())) {
			instance.setTabUrl(null);
		}
		if ("".equals(instance.getIconSrc())) {
			instance.setIconSrc(null);
		}
		if ("".equals(instance.getPermissionKey())) {
			instance.setPermissionKey(null);
		}
		int newDisplayOrder = 1;
		List<Object> newDisplayOrderList = getEntityManager().createNativeQuery("select ifnull(max(display_order), 0) + 1 as new_display_order from menu_info where deleted = 0 and the_pid" + (theParentValue != null ? " = " + theParentValue : " is null")).getResultList();
		if (newDisplayOrderList != null) {
			newDisplayOrder = Integer.parseInt(newDisplayOrderList.get(0).toString());
		}
		instance.setDisplayOrder(newDisplayOrder);
		if (theParentValue != null) {// 靠后执行
			instance.setMenuInfo(getEntityManager().find(MenuInfo.class, theParentValue));
		} else {
			instance.setMenuInfo(null);
		}
		getEntityManager().persist(instance);
		getEntityManager().flush();
		raiseAfterTransactionSuccessEvent();
		return "persisted";
	}

	@SuppressWarnings("unchecked")
	@Override
	public String update() {
		setMessage("");

		Integer oldPid = instance.getMenuInfo() != null ? instance.getMenuInfo().getTheId() : null;
		boolean pidChanged = false;
		if (oldPid != null) {
			if (!oldPid.equals(theParentValue)) {// 检查父级是否发生变化（Integer的比较，详见jdk源码）
				pidChanged = true;
			}
		} else if (theParentValue != null) {
			pidChanged = true;
		}
		if (pidChanged) {
			int newDisplayOrder = 1;
			List<Object> newDisplayOrderList = getEntityManager().createNativeQuery("select ifnull(max(display_order), 0) + 1 as new_display_order from menu_info where deleted = 0 and the_pid" + (theParentValue != null ? " = " + theParentValue : " is null")).getResultList();
			if (newDisplayOrderList != null) {
				newDisplayOrder = Integer.parseInt(newDisplayOrderList.get(0).toString());
			}
			instance.setDisplayOrder(newDisplayOrder);
		}

		joinTransaction();
		if ("".equals(instance.getTabTitle())) {
			instance.setTabTitle(null);
		}
		if ("".equals(instance.getTabUrl())) {
			instance.setTabUrl(null);
		}
		if ("".equals(instance.getIconSrc())) {
			instance.setIconSrc(null);
		}
		if ("".equals(instance.getPermissionKey())) {
			instance.setPermissionKey(null);
		}
		if (theParentValue != null) {// 靠后执行
			instance.setMenuInfo(getEntityManager().find(MenuInfo.class, theParentValue));
		} else {
			instance.setMenuInfo(null);
		}
		getEntityManager().flush();
		if (pidChanged) {
			rebuildDisplayOrder(new Integer[] { oldPid });
		}
		raiseAfterTransactionSuccessEvent();
		return "updated";
	}

	@Override
	public String remove() {
		setMessage("");

		getInstance();

		if (Assit.getResultSetSize("select the_id from menu_info where deleted = 0 and the_pid = " + instance.getTheId()) > 0) {
			setMessage("该项已被使用，操作被取消");
			return "failure";
		}

		joinTransaction();
		instance.setDeleted(true);
		getEntityManager().flush();
		rebuildDisplayOrder(new Integer[] { instance.getMenuInfo() != null ? instance.getMenuInfo().getTheId() : null });
		raiseAfterTransactionSuccessEvent();
		return "removed";
	}

	/** 递归查找待删除的子节点 */
	private void removeLeaf(Map<Object, List<Object>> nexusMap, StringBuffer prepareRemoveIds, List<Object> leafList) {
		if (leafList != null && leafList.size() > 0) {
			for (Object leaf : leafList) {
				prepareRemoveIds.append(",").append(leaf);
				removeLeaf(nexusMap, prepareRemoveIds, nexusMap.get(leaf));
			}
		}
	}

	/** 删除（含子级） */
	@SuppressWarnings("unchecked")
	public String removeIncludeLeaf() {
		setMessage("");

		getInstance();

		// 待删除的子节点id集合
		StringBuffer prepareRemoveIds = new StringBuffer();
		prepareRemoveIds.append(instance.getTheId());
		Map<Object, List<Object>> nexusMap = new HashMap<Object, List<Object>>();// 上下级关系集合
		String dataSql = "select the_id, the_pid, the_value from menu_info where deleted = 0";
		List<Object[]> dataList = getEntityManager().createNativeQuery(dataSql).getResultList();
		if (dataList != null && dataList.size() > 0) {
			for (Object[] data : dataList) {
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
		List<Object> rootList = nexusMap.get(instance.getTheId());
		if (rootList != null && rootList.size() > 0) {
			for (Object root : rootList) {
				prepareRemoveIds.append(",").append(root);
				removeLeaf(nexusMap, prepareRemoveIds, nexusMap.get(root));
			}
		}
		List<MenuInfo> prepareRemoveList = getEntityManager().createQuery("select menuInfo from MenuInfo menuInfo where menuInfo.theId in (" + prepareRemoveIds + ")").getResultList();
		if (prepareRemoveList != null && prepareRemoveList.size() > 0) {
			joinTransaction();
			for (MenuInfo menuInfo : prepareRemoveList) {
				menuInfo.setDeleted(true);
			}
			getEntityManager().flush();
			raiseAfterTransactionSuccessEvent();
		}
		rebuildDisplayOrder(new Integer[] { instance.getMenuInfo() != null ? instance.getMenuInfo().getTheId() : null });
		return "removed";
	}

	public void setMenuInfoTheId(Integer id) {
		setId(id);
	}

	public Integer getMenuInfoTheId() {
		return (Integer) getId();
	}

	@Override
	protected MenuInfo createInstance() {
		MenuInfo menuInfo = new MenuInfo();
		return menuInfo;
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
	public void wire() {
		getInstance();

		if (theParentList != null) {
			theParentList.clear();
		} else {
			theParentList = new ArrayList<Object[]>();
		}

		theParentList.add(new Object[] { "", "无" });
		Map<Object, List<Object>> nexusMap = new HashMap<Object, List<Object>>();// 上下级关系集合
		Map<Object, Object> valueMap = new HashMap<Object, Object>();// 值集合
		String dataSql = "select the_id, the_pid, the_value from menu_info where deleted = 0";
		if (isManaged()) {
			dataSql += " and the_id != " + instance.getTheId();
		}
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

		if (!isFirstTime) {
			theParentValue = instance.getMenuInfo() != null ? instance.getMenuInfo().getTheId() : null;
			isFirstTime = true;
		}
	}

	public boolean isWired() {
		return true;
	}

	public MenuInfo getDefinedInstance() {
		return isIdDefined() ? getInstance() : null;
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
