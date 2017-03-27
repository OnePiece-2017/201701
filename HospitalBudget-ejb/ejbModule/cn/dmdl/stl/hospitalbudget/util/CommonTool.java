package cn.dmdl.stl.hospitalbudget.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Credentials;
import org.jboss.seam.security.Identity;

import cn.dmdl.stl.hospitalbudget.admin.entity.Dictionary;
import cn.dmdl.stl.hospitalbudget.boot.ConfigureCache;

@Stateful
@Name("commonTool")
public class CommonTool implements CommonToolLocal {

	@In(create = true)
	SessionToken sessionToken;

	@Logger
	private Log log;

	@In
	Identity identity;

	@In
	Credentials credentials;

	@PersistenceContext
	EntityManager entityManager;

	@In
	protected FacesMessages facesMessages;

	@Remove
	public void remove() {
	}

	@Destroy
	public void destroy() {
	}

	public String abbr(Object source, int length) {
		String abbreviation = "";
		if (source != null && !"".equals(source.toString())) {
			if (source.toString().length() <= length) {
				abbreviation = source.toString();
			} else if (length < 1) {
				abbreviation = source.toString().substring(0, length);
			} else {
				abbreviation = source.toString().substring(0, length - 1) + "…";
			}
		}
		return abbreviation;
	}

	public List<Dictionary> getDictionaryByPid(int pid) {
		List<Dictionary> dictionaryList = new ArrayList<Dictionary>();
		if (ConfigureCache.dictionaryList != null) {
			for (Dictionary dictionary : ConfigureCache.dictionaryList) {
				if (dictionary.getDictionary() != null && dictionary.getDictionary().getTheId() == pid) {
					dictionaryList.add(dictionary);
				}
			}
		}
		return dictionaryList;
	}

	public String getDictionaryValueByIds(String ids) {
		StringBuffer result = new StringBuffer();
		if (ids != null && !"".equals(ids)) {
			String[] arr = ids.split(",");
			for (int i = 0, len = arr.length; i < len; ++i) {
				Dictionary dictionary = ConfigureCache.dictionaryMap.get(Integer.valueOf(arr[i]));
				if (dictionary != null && dictionary.getTheValue() != null && !"".equals(dictionary.getTheValue())) {
					result.append(dictionary.getTheValue());
					if (i < len - 1) {
						result.append(",");
					}
				}
			}
		}
		return result.toString();
	}

	public Object[] genA4jRepeatValue(int length) {
		return new Object[length > 0 ? length : 0];
	}

	public List<?> sqlQuery(String sql) {
		return entityManager.createNativeQuery(sql).getResultList();
	}

	public List<?> hqlQuery(String hql) {
		return entityManager.createQuery(hql).getResultList();
	}

	public boolean insertIntermediate(String table, String[] columns, Object[] values) {
		List<Object[]> valuesList = new ArrayList<Object[]>();
		valuesList.add(values);
		return insertIntermediateBatch(table, columns, valuesList);
	}

	public boolean insertIntermediateBatch(String table, String[] columns, List<Object[]> valuesList) {
		boolean result = false;
		try {
			if (valuesList != null && valuesList.size() > 0) {
				for (Object[] values : valuesList) {
					if (values != null && values.length > 0) {
						entityManager.createNativeQuery("INSERT INTO " + table + " (" + Arrays.toString(columns).replace("[", "").replace("]", "") + ") VALUES (" + Assit.surroundContentsAgile("'", "'", true, values) + ")").executeUpdate();
					}
				}
			}
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public boolean deleteIntermediate(String table, String where) {
		boolean result = false;
		try {
			entityManager.createNativeQuery("DELETE FROM " + table + (where != null && !"".equals(where) ? " WHERE " + where : "")).executeUpdate();
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> selectIntermediate(String table, String[] columns, String where) {
		List<Object[]> result = null;
		try {
			result = entityManager.createNativeQuery("SELECT " + Arrays.toString(columns).replace("[", "").replace("]", "") + " FROM " + table + (where != null && !"".equals(where) ? " WHERE " + where : "")).getResultList();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public String selectIntermediateAsIds(String table, String column, String where) {
		return selectIntermediateAsIds(table, column, where, ",");
	}

	public String selectIntermediateAsIds(String table, String column, String where, String separator) {
		StringBuffer result = new StringBuffer();
		List<Object[]> list = selectIntermediate(table, new String[] { column }, where);
		if (list != null && list.size() > 0) {
			for (int i = 0, size = list.size(); i < size; i++) {
				result.append(list.get(i));
				if (i < size - 1) {
					result.append(separator);
				}
			}
		}
		return result.toString();
	}

	public boolean updateIntermediate(String table, Map<String, Object> columnToValue, String where) {
		boolean result = false;
		try {
			StringBuffer setPart = new StringBuffer();
			if (columnToValue != null && columnToValue.size() > 0) {
				for (String column : columnToValue.keySet()) {
					setPart.append(column).append(" = ").append(columnToValue.get(column) != null ? "'" + columnToValue.get(column) + "'" : null).append(", ");
				}
				if (!"".equals(setPart.toString())) {
					setPart.delete(setPart.length() - 2, setPart.length());
					entityManager.createNativeQuery("UPDATE " + table + " SET " + setPart.toString() + (where != null && !"".equals(where) ? " WHERE " + where : "")).executeUpdate();
				}
			}

			result = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

}
