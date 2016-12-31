package cn.dmdl.stl.hospitalbudget.hospital.session;

import javax.persistence.Query;

import net.sf.json.JSONObject;

import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.common.session.CriterionEntityQuery;
import cn.dmdl.stl.hospitalbudget.hospital.entity.YsGeneralProject;

@Name("ysGeneralProjectList")
public class YsGeneralProjectList extends CriterionEntityQuery<YsGeneralProject> {

	private static final long serialVersionUID = 1L;
	private static final String EJBQL = "select ysGeneralProject from YsGeneralProject ysGeneralProject where ysGeneralProject.deleted = 0";
	private YsGeneralProject ysGeneralProject = new YsGeneralProject();
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
			YsGeneralProject ysGeneralProject = getEntityManager().find(YsGeneralProject.class, theId);
			ysGeneralProject.setTheState(theState != 1 ? 1 : 2);
			getEntityManager().merge(ysGeneralProject);
			getEntityManager().flush();
		} catch (Exception e) {
			invokeData4setState.element("message", "操作失败！");
		}
	}

	@Override
	protected Query createQuery() {
		String sql = EJBQL;
		if (keyword != null && !"".equals(keyword)) {
			sql += " and ysGeneralProject.theValue like '%" + keyword + "%'";
		}
		setEjbql(sql);
		return super.createQuery();
	}

	public YsGeneralProjectList() {
		setEjbql(EJBQL);
		setAttribute("ysGeneralProject.theId");
	}

	public YsGeneralProject getYsGeneralProject() {
		return ysGeneralProject;
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
