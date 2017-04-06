package cn.dmdl.stl.hospitalbudget.temp;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.common.session.FakeEntityQuery;

@Name("testFEQList")
public class TestFEQList extends FakeEntityQuery {

	private static final long serialVersionUID = 1L;

	@Override
	protected Query createQuery() {
		setFakeEntityColumns("the_id", "the_key", "the_value");
		StringBuffer sql = new StringBuffer("select the_id, the_key, the_value from message_info where deleted = 0");
		if (keyword != null && !"".equals(keyword)) {
			sql.append(" and (0 <> 0");
			sql.append(" or the_key like '%" + keyword + "%'");// 键
			sql.append(" or the_value like '%" + keyword + "%'");// 值
			sql.append(" )");
		}
		sql.insert(0, "select * from (").append(") as recordset");
		setEjbql(sql.toString());
		return super.createQuery();
	}

	public TestFEQList() {
		setEjbql("");
		setAttribute("");
	}

}
