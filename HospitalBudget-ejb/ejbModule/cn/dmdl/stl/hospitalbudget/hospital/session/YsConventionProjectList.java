package cn.dmdl.stl.hospitalbudget.hospital.session;

import javax.persistence.Query;

import net.sf.json.JSONObject;

import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.common.session.CriterionEntityQuery;
import cn.dmdl.stl.hospitalbudget.hospital.entity.YsConventionProject;

@Name("ysConventionProjectList")
public class YsConventionProjectList extends CriterionEntityQuery<YsConventionProject> {

	private static final long serialVersionUID = 1L;
	private static final String EJBQL = "select ysConventionProject from YsConventionProject ysConventionProject where ysConventionProject.deleted = 0";
	private YsConventionProject ysConventionProject = new YsConventionProject();
	private String keyword;// 关键词
	private String invokeArgs4setState;// 请求之前的参数
	private JSONObject invokeData4setState;// 请求之后的数据

	/** 请求方法 */
	public void invoke4setState() {
		if (invokeData4setState != null) {
			invokeData4setState.clear();
		} else {
			invokeData4setState = new JSONObject();
		}
		invokeData4setState.accumulate("message", "操作成功！");
		try {
			JSONObject invokeArgs = JSONObject.fromObject(invokeArgs4setState);
			int theId = invokeArgs.getInt("theId");
			int theState = invokeArgs.getInt("theState");
			YsConventionProject ysConventionProject = getEntityManager().find(YsConventionProject.class, theId);
			ysConventionProject.setTheState(theState != 1 ? 1 : 2);
			getEntityManager().merge(ysConventionProject);
			getEntityManager().flush();
		} catch (Exception e) {
			invokeData4setState.element("message", "操作失败！");
		}
	}

	@Override
	protected Query createQuery() {
		String sql = EJBQL;
		if (keyword != null && !"".equals(keyword)) {
			sql += " and ysConventionProject.theValue like '%" + keyword + "%'";
		}
		setEjbql(sql);
		return super.createQuery();
	}

	public YsConventionProjectList() {
		setEjbql(EJBQL);
		setAttribute("ysConventionProject.theId");
	}

	public YsConventionProject getYsConventionProject() {
		return ysConventionProject;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public String getInvokeArgs4setState() {
		return invokeArgs4setState;
	}

	public void setInvokeArgs4setState(String invokeArgs4setState) {
		this.invokeArgs4setState = invokeArgs4setState;
	}

	public JSONObject getInvokeData4setState() {
		return invokeData4setState;
	}

	public void setInvokeData4setState(JSONObject invokeData4setState) {
		this.invokeData4setState = invokeData4setState;
	}
}
