package cn.dmdl.stl.hospitalbudget.admin.session;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.admin.entity.Dictionary;
import cn.dmdl.stl.hospitalbudget.common.session.CriterionEntityQuery;

@Name("dictionaryList")
public class DictionaryList extends CriterionEntityQuery<Dictionary> {

	private static final long serialVersionUID = 1L;
	private static final String EJBQL = "select dictionary from Dictionary dictionary where dictionary.deleted = 0";
	private Dictionary dictionary = new Dictionary();
	private String keyword;// 关键词

	@Override
	protected Query createQuery() {
		String sql = EJBQL;
		if (keyword != null && !"".equals(keyword)) {
			sql += " and dictionary.theValue like '%" + keyword + "%'";
		}
		setEjbql(sql);
		return super.createQuery();
	}

	public DictionaryList() {
		setEjbql(EJBQL);
		setAttribute("dictionary.theId");
	}

	public Dictionary getDictionary() {
		return dictionary;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
}
