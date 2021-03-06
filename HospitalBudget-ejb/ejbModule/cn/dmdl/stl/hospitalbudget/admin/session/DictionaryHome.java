package cn.dmdl.stl.hospitalbudget.admin.session;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.admin.entity.Dictionary;
import cn.dmdl.stl.hospitalbudget.boot.ConfigureLoader;
import cn.dmdl.stl.hospitalbudget.common.session.CriterionEntityHome;
import cn.dmdl.stl.hospitalbudget.util.Assit;

@Name("dictionaryHome")
public class DictionaryHome extends CriterionEntityHome<Dictionary> {

	private static final long serialVersionUID = 1L;
	private List<Object[]> theParentList;// 父级list
	private Integer theParentValue;// 父级id
	private JSONObject dataIssue;

	public void invokeIssue() {
		if (dataIssue != null) {
			dataIssue.clear();
		} else {
			ConfigureLoader.initDictionary();
			dataIssue = new JSONObject();
			dataIssue.accumulate("message", "发布成功！");
		}
	}

	@Override
	public String persist() {
		setMessage("");

		if (Assit.getResultSetSize("select the_id from dictionary where deleted = 0 and the_value = '" + instance.getTheValue() + "'") > 0) {
			setMessage("此名称太受欢迎,请更换一个");
			return null;
		}

		joinTransaction();
		if (theParentValue != null) {
			instance.setDictionary(getEntityManager().find(Dictionary.class, theParentValue));
		} else {
			instance.setDictionary(null);
		}
		instance.setInsertTime(new Date());
		instance.setInsertUser(sessionToken.getUserInfoId());
		getEntityManager().persist(instance);
		getEntityManager().flush();
		raiseAfterTransactionSuccessEvent();
		return "persisted";
	}

	@Override
	public String update() {
		setMessage("");

		if (Assit.getResultSetSize("select the_id from dictionary where deleted = 0 and the_value = '" + instance.getTheValue() + "' and the_id != " + instance.getTheId()) > 0) {
			setMessage("此名称太受欢迎,请更换一个");
			return null;
		}

		joinTransaction();
		if (theParentValue != null) {
			instance.setDictionary(getEntityManager().find(Dictionary.class, theParentValue));
		} else {
			instance.setDictionary(null);
		}
		instance.setUpdateTime(new Date());
		instance.setUpdateUser(sessionToken.getUserInfoId());
		getEntityManager().flush();
		raiseAfterTransactionSuccessEvent();
		return "updated";
	}

	@Override
	public String remove() {
		setMessage("");

		getInstance();

		if (Assit.getResultSetSize("select the_id from dictionary where deleted = 0 and the_pid = " + instance.getTheId()) > 0) {
			setMessage("该项已被使用，操作被取消");
			return "failure";
		}

		joinTransaction();
		instance.setDeleted(true);
		getEntityManager().flush();
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
		String dataSql = "select the_id, the_pid, the_value from dictionary where deleted = 0";
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
		List<Dictionary> prepareRemoveList = getEntityManager().createQuery("select dictionary from Dictionary dictionary where dictionary.theId in (" + prepareRemoveIds + ")").getResultList();
		if (prepareRemoveList != null && prepareRemoveList.size() > 0) {
			joinTransaction();
			for (Dictionary dictionary : prepareRemoveList) {
				dictionary.setDeleted(true);
			}
			getEntityManager().flush();
			raiseAfterTransactionSuccessEvent();
		}
		return "removed";
	}

	public void setDictionaryTheId(Integer id) {
		setId(id);
	}

	public Integer getDictionaryTheId() {
		return (Integer) getId();
	}

	@Override
	protected Dictionary createInstance() {
		Dictionary dictionary = new Dictionary();
		return dictionary;
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
		String dataSql = "select the_id, the_pid, the_value from dictionary where deleted = 0";
		if (isManaged()) {
			dataSql += " and the_id != " + instance.getTheId();
		}
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
		theParentValue = theParentValue != null ? theParentValue : (instance.getDictionary() != null ? instance.getDictionary().getTheId() : null);
	}

	public boolean isWired() {
		return true;
	}

	public Dictionary getDefinedInstance() {
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

	public JSONObject getDataIssue() {
		return dataIssue;
	}

	public void setDataIssue(JSONObject dataIssue) {
		this.dataIssue = dataIssue;
	}

}
