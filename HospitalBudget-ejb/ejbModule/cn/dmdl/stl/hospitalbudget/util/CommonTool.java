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

	public String getProjectProcessTypeName(int type) {
		String projectProcessTypeName = "";
		if(type == 1){
			projectProcessTypeName = "常规收入预算";
		}else if(type == 2){
			projectProcessTypeName = "常规支出预算";
		}else if(type == 3){
			projectProcessTypeName = "常规收入执行";
		}else if(type == 4){
			projectProcessTypeName = "常规支出执行";
		}
		return projectProcessTypeName;
	}
	
	
}
