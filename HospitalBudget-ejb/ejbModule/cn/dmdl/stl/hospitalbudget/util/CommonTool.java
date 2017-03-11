package cn.dmdl.stl.hospitalbudget.util;

import java.util.ArrayList;
import java.util.List;

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

	/** 缩写 */
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

	/** 根据父id获取字典列表 */
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

	/** 根据ids获取字典值 */
	public String getDictionaryValueByIds(String ids) {
		String result = "";
		if (ids != null && !"".equals(ids)) {
			String[] arr = ids.split(",");
			for (int i = 0; i < arr.length; i++) {
				Dictionary dictionary = ConfigureCache.dictionaryMap.get(Integer.valueOf(arr[i]));
				if (dictionary != null && dictionary.getTheValue() != null && !"".equals(dictionary.getTheValue())) {
					result += dictionary.getTheValue();
					if (i < arr.length - 1) {
						result += ",";
					}
				}
			}
		}
		return result;
	}

	/** generate http://richfaces.org/a4j a:repeat value */
	public Object[] genA4jRepeatValue(int length) {
		return new Object[length > 0 ? length : 0];
	}

	/** 生成select option集合 */
	@SuppressWarnings("unchecked")
	public List<Object[]> genA4jRepeatValueForSelectOption(String sql) {
		return entityManager.createNativeQuery(sql).getResultList();
	}

}
