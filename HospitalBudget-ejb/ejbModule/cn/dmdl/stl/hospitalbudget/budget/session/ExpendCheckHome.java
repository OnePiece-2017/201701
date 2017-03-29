package cn.dmdl.stl.hospitalbudget.budget.session;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.common.session.CriterionEntityHome;
import cn.dmdl.stl.hospitalbudget.util.DataSourceManager;

@Name("expendCheckHome")
public class ExpendCheckHome extends CriterionEntityHome<Object>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String year;
	private String deptIds;
	private String fundsSourceId;
	private String expandDraftInfo;
	
	/**
	 * 获取编制信息
	 * @return
	 */
	public JSONObject getDraftInfo(){
		JSONObject draftInfojson = new JSONObject();
		StringBuilder sql = new StringBuilder();
		sql.append("select ed.ys_expand_draft_id, ");
		sql.append("rp.the_value as project_name, ");
		sql.append("1 as is_usual, ");
		sql.append("di.the_value as dept_name, ");
		sql.append("ed.project_source, ");
		sql.append("ed.project_amount, ");
		sql.append("ed.with_last_year_num, ");
		sql.append("ed.with_last_year_percent, ");
		sql.append("ed.formula_remark, ");
		sql.append("ed.attachment, ");
		sql.append("ed.status, ");
		sql.append("rp.the_id as project_id, ");
		sql.append("rp.the_pid, ");
		sql.append("rp.bottom_level, ");
		sql.append("rp.top_level_project_id, ");
		sql.append("edp.process_step_info_id ");
		sql.append("from ys_expand_draft ed ");
		sql.append("INNER JOIN routine_project rp ON ed.routine_project_id = rp.the_id AND rp.funds_source_id = ").append(fundsSourceId).append(" ");
		sql.append("INNER JOIN ys_department_info di ON rp.department_info_id = di.the_id  ");
		sql.append("INNER JOIN ys_expand_draft_process edp ON ed.ys_expand_draft_id = edp.ys_expand_draft_id ");
		sql.append("AND edp.user_id = ").append(sessionToken.getUserInfoId()).append(" ");
		sql.append("WHERE ed.delete = 0 AND ed.year = '").append(year).append("' ");
		sql.append("GROUP BY ed.ys_expand_draft_id ");
		sql.append("UNION ");
		sql.append("select ed.ys_expand_draft_id, ");
		sql.append("rp.the_value as project_name, ");
		sql.append("2 as is_usual, ");
		sql.append("di.the_value as dept_name, ");
		sql.append("ed.project_source, ");
		sql.append("ed.project_amount, ");
		sql.append("ed.with_last_year_num, ");
		sql.append("ed.with_last_year_percent, ");
		sql.append("ed.formula_remark, ");
		sql.append("ed.attachment, ");
		sql.append("ed.status, ");
		sql.append("rp.the_id as project_id, ");
		sql.append("rp.the_pid, ");
		sql.append("rp.bottom_level, ");
		sql.append("rp.top_level_project_id, ");
		sql.append("edp.process_step_info_id ");
		sql.append("from ys_expand_draft ed ");
		sql.append("INNER JOIN generic_project rp ON ed.generic_project_id = rp.the_id AND rp.funds_source_id = ").append(fundsSourceId).append(" ");
		sql.append("INNER JOIN ys_department_info di ON rp.department_info_id = di.the_id  ");
		sql.append("INNER JOIN ys_expand_draft_process edp ON ed.ys_expand_draft_id = edp.ys_expand_draft_id ");
		sql.append("AND edp.user_id = ").append(sessionToken.getUserInfoId()).append(" ");
		sql.append("WHERE ed.delete = 0 AND ed.year = '").append(year).append("' ");
		sql.append("GROUP BY ed.ys_expand_draft_id ");
		sql.insert(0, "select * from (").append(") t order by t.ys_expand_draft_id ");
		System.out.println(sql);
		Connection connection = DataSourceManager.open(DataSourceManager.BY_JDBC_DEFAULT);
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			preparedStatement = connection.prepareStatement(sql.toString());
			resultSet = preparedStatement.executeQuery();
			JSONArray draftInfoArray = new JSONArray();
			double totalAmount = 0;
			while(resultSet.next()){
				JSONObject json = new JSONObject();
				json.element("ys_expand_draft_id", resultSet.getInt("ys_expand_draft_id"));
				json.element("project_name", resultSet.getString("project_name"));
				json.element("is_usual", resultSet.getInt("is_usual"));
				json.element("dept_name", resultSet.getString("dept_name"));
				json.element("project_source", URLDecoder.decode(resultSet.getString("project_source"), "utf-8"));
				json.element("project_amount", resultSet.getDouble("project_amount"));
				json.element("with_last_year_num", resultSet.getDouble("with_last_year_num"));
				json.element("with_last_year_percent", resultSet.getDouble("with_last_year_percent"));
				json.element("formula_remark", URLDecoder.decode(resultSet.getString("formula_remark"), "utf-8"));
				json.element("attachment", resultSet.getString("attachment"));
				json.element("status", resultSet.getInt("status"));
				if(resultSet.getInt("is_usual") == 1){
					json.element("project_id", "10" + resultSet.getInt("project_id"));//为了多级表格展示处理
					json.element("the_pid", "10" + resultSet.getInt("the_pid"));
					json.element("top_level_project_id", "r" + resultSet.getInt("top_level_project_id"));
				}else{
					json.element("project_id", "20" + resultSet.getInt("project_id"));
					json.element("the_pid", "20" + resultSet.getInt("the_pid"));
					json.element("top_level_project_id", "g" + resultSet.getInt("top_level_project_id"));
				}
				if(resultSet.getInt("the_pid") == 0){
					json.element("is_root", 1);
				}
				json.element("bottom_level", resultSet.getInt("bottom_level"));
				if(resultSet.getInt("bottom_level") == 1){
					totalAmount += resultSet.getDouble("project_amount");
				}
				json.element("process_step_info_id", resultSet.getInt("process_step_info_id"));
				draftInfoArray.add(json);
			}
			draftInfojson.element("draft_info", draftInfoArray);
			draftInfojson.element("total_amount", totalAmount);
			System.out.println(draftInfojson);
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}finally{
			DataSourceManager.close(connection, preparedStatement, resultSet);
		}
		return draftInfojson;
	}
	
	
	/**
	 * 审核通过
	 * @return
	 */
	public JSONObject goAhead(){
		JSONArray arr = JSONArray.fromObject(expandDraftInfo);
		JSONObject draftInfo = new JSONObject();
		for(Object obj : arr){
			JSONObject json = JSONObject.fromObject(obj);
			draftInfo.accumulate(json.getString("step_id"), json.getInt("draft_id"));
		}
		System.out.println(draftInfo);
		JSONObject result = new JSONObject();
		return result;
	}
	
	
	public String getYear() {
		return year;
	}
	public void setYear(String year) {
		this.year = year;
	}
	public String getDeptIds() {
		return deptIds;
	}
	public void setDeptIds(String deptIds) {
		this.deptIds = deptIds;
	}
	public String getFundsSourceId() {
		return fundsSourceId;
	}
	public void setFundsSourceId(String fundsSourceId) {
		this.fundsSourceId = fundsSourceId;
	}
	public String getExpandDraftInfo() {
		return expandDraftInfo;
	}
	public void setExpandDraftInfo(String expandDraftInfo) {
		this.expandDraftInfo = expandDraftInfo;
	}

	
	

}
