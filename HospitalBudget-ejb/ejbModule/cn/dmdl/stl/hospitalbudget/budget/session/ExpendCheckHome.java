package cn.dmdl.stl.hospitalbudget.budget.session;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.common.session.CriterionEntityHome;
import cn.dmdl.stl.hospitalbudget.util.Assit;
import cn.dmdl.stl.hospitalbudget.util.DataSourceManager;
import cn.dmdl.stl.hospitalbudget.util.HospitalConstant;

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
		sql.append("WHERE ed.delete = 0 AND ed.year = '").append(year).append("' AND ed.status = ").append(HospitalConstant.DRAFTSTATUS_AUDIT).append(" ");
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
				json.element("project_amount", resultSet.getDouble("project_amount")/10000);
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
			draftInfojson.element("total_amount", totalAmount/10000);
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
		JSONObject result = new JSONObject();
		JSONArray arr = JSONArray.fromObject(expandDraftInfo);
		Map<Integer, List<Integer>> draftInfoMap = new HashMap<Integer, List<Integer>>();
		for(Object obj : arr){
			JSONObject json = JSONObject.fromObject(obj);
			if(draftInfoMap.containsKey(json.getInt("step_id"))){
				draftInfoMap.get(json.getInt("step_id")).add(json.getInt("draft_id"));
			}else{
				List<Integer> list = new ArrayList<Integer>();
				list.add(json.getInt("draft_id"));
				draftInfoMap.put(json.getInt("step_id"), list);
			}
		}
		Connection connection = DataSourceManager.open(DataSourceManager.BY_JDBC_DEFAULT);
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			for(Integer stepId : draftInfoMap.keySet()){
				//查询步骤
				int stepIndex = 0;
				int processInfoId = 0;
				preparedStatement = connection.prepareStatement("select si.process_info_id,si.step_index from process_step_info si where si.process_step_info_id = ?");
				preparedStatement.setInt(1, stepId);
				resultSet = preparedStatement.executeQuery();
				if(resultSet.next()){
					stepIndex = resultSet.getInt("step_index");
					processInfoId = resultSet.getInt("process_info_id");
				}
				List<Integer> draftIdList = draftInfoMap.get(stepId);
				String draftIds = Assit.formatIdsList(draftIdList);
				//查询下一步处理人
				StringBuilder sql = new StringBuilder();
				sql.append("select si.process_step_info_id,su.user_id from process_step_info si  ");
				sql.append("INNER JOIN process_step_user su ON si.process_step_info_id = su.process_step_info_id AND su.type = 0 ");
				sql.append("where si.process_info_id = ? and si.step_index = ? ");
				preparedStatement = connection.prepareStatement(sql.toString());
				preparedStatement.setInt(1, processInfoId);
				preparedStatement.setInt(2, stepIndex + 1);
				resultSet = preparedStatement.executeQuery();
				connection.setAutoCommit(false);
				if(resultSet.next()){//有下一步审核人
					//处理流程任务数据
					dealWithDraftProcess(connection, preparedStatement, draftIdList, draftIds, stepId);
					//添加新的任务执行人
					for(Integer draftId : draftIdList){
						preparedStatement = connection.prepareStatement("INSERT INTO `ys_expand_draft_process` (`ys_expand_draft_id`, `user_id`, `process_step_info_id`) VALUES (?, ?, ?)");
						preparedStatement.setInt(1, draftId);
						preparedStatement.setInt(2, resultSet.getInt("user_id"));
						preparedStatement.setInt(3, resultSet.getInt("process_step_info_id"));
						preparedStatement.executeUpdate();
					}
				}else{//没有下一步人审核，进入汇总
					//查询出当前的编制数据
					sql.delete(0, sql.length());
					sql.append("select ed.`year`, ");
					sql.append("1 as is_usual, ");
					sql.append("ed.project_source, ");
					sql.append("ed.top_level_project_id, ");
					sql.append("ed.routine_project_id as project_id, ");
					sql.append("ed.project_amount, ");
					sql.append("ed.formula_remark, ");
					sql.append("ed.attachment, ");
					sql.append("rp.bottom_level, ");
					sql.append("rp.department_info_id ");
					sql.append("from ys_expand_draft ed ");
					sql.append("INNER JOIN routine_project rp ON ed.routine_project_id = rp.the_id  ");
					sql.append("WHERE ed.ys_expand_draft_id in (").append(draftIds).append(") ");
					sql.append("UNION ");
					sql.append("select ed.`year`, ");
					sql.append("2 as is_usual, ");
					sql.append("ed.project_source, ");
					sql.append("ed.top_level_project_id, ");
					sql.append("ed.generic_project_id as project_id, ");
					sql.append("ed.project_amount, ");
					sql.append("ed.formula_remark, ");
					sql.append("ed.attachment, ");
					sql.append("rp.bottom_level, ");
					sql.append("rp.department_info_id ");
					sql.append("from ys_expand_draft ed ");
					sql.append("INNER JOIN generic_project rp ON ed.generic_project_id = rp.the_id  ");
					sql.append("WHERE ed.ys_expand_draft_id in (").append(draftIds).append(") ");
					preparedStatement = connection.prepareStatement(sql.toString());
					resultSet = preparedStatement.executeQuery();
					List<Map<String, Object>> routineProjectList = new ArrayList<Map<String,Object>>();
					List<Map<String, Object>> genericProjectList = new ArrayList<Map<String,Object>>();
					String year = "";
					Set<Integer> deptIdSet = new HashSet<Integer>();
					while(resultSet.next()){
						year = resultSet.getString("year");
						deptIdSet.add(resultSet.getInt("department_info_id"));
						Map<String, Object> map = new HashMap<String, Object>();
						map.put("year", resultSet.getString("year"));
						map.put("project_source", resultSet.getString("project_source"));
						map.put("top_level_project_id", resultSet.getInt("top_level_project_id"));
						map.put("project_amount", resultSet.getDouble("project_amount"));
						map.put("formula_remark", resultSet.getString("formula_remark"));
						map.put("attachment", resultSet.getString("attachment"));
						map.put("department_info_id", resultSet.getInt("department_info_id"));
						map.put("bottom_level", resultSet.getInt("bottom_level"));
						if(resultSet.getInt("is_usual") == 1){
							map.put("routine_project_id", resultSet.getString("project_id"));
							routineProjectList.add(map);
						}else{
							map.put("generic_project_id", resultSet.getString("project_id"));
							genericProjectList.add(map);
						}
					}
					String deptIds = Assit.formatIdsSet(deptIdSet);
					//检查有没有已经下达的数据
					preparedStatement = connection.prepareStatement("select 1 from ys_budget_collection_dept bcd where bcd.`year` = ? "
							+ "AND bcd.dept_id in (" + deptIds + ") AND budget_type = ? AND bcd.`status` = ?");
					preparedStatement.setString(1, year);
					preparedStatement.setInt(2, HospitalConstant.COLLECTIONTYPE_EXPEND);
					preparedStatement.setInt(3, HospitalConstant.COLLECTIONSTATUS_FINISH);
					resultSet = preparedStatement.executeQuery();
					if(resultSet.next()){
						result.element("isok", "err");
						result.element("message", "您所提交的科室预算已下达！如需修改请走调整流程！");
						return result;
					}
					
					//没有下达的数据
					//查询历史版本
					sql.delete(0, sql.length());
					sql.append("select t1.dept_id,MAX(t.version) as max_version from ys_expand_collection_info_log t  ");
					sql.append("INNER JOIN ys_budget_collection_dept t1 ON t.budget_collection_dept_id = t1.budget_collection_dept_id ");
					sql.append("where t1.`year` = ? AND t1.dept_id in (").append(deptIds).append(") GROUP BY t1.dept_id ");
					preparedStatement = connection.prepareStatement(sql.toString());
					preparedStatement.setString(1, year);
					resultSet = preparedStatement.executeQuery();
					Map<Integer,Integer> versionMap = new HashMap<Integer, Integer>();
					while(resultSet.next()){
						versionMap.put(resultSet.getInt("dept_id"), resultSet.getInt("max_version"));
					}
					preparedStatement = connection.prepareStatement("select bcd.budget_collection_dept_id,bcd.`status`,bcd.dept_id from ys_budget_collection_dept bcd where bcd.`year` = ? "
							+ "AND bcd.dept_id in (" + deptIds + ") AND budget_type = ? AND (bcd.`status` = ? or bcd.`status` = ?) group by bcd.dept_id");
					preparedStatement.setString(1, year);
					preparedStatement.setInt(2, HospitalConstant.COLLECTIONTYPE_EXPEND);
					preparedStatement.setInt(3, HospitalConstant.COLLECTIONSTATUS_WAIT);
					preparedStatement.setInt(4, HospitalConstant.COLLECTIONSTATUS_RETURN);
					resultSet = preparedStatement.executeQuery();
					Map<Integer, Integer> collectionDeptIdMap = new HashMap<Integer, Integer>();//记录部门id的汇总id
					while(resultSet.next()){
						collectionDeptIdMap.put(resultSet.getInt("dept_id"), resultSet.getInt("budget_collection_dept_id"));
						deptIdSet.remove(resultSet.getInt("dept_id"));//去掉需要插入的数据
						preparedStatement = connection.prepareStatement("UPDATE ys_budget_collection_dept t set t.`status` = 0 where t.budget_collection_dept_id = ?");
						preparedStatement.setInt(1, resultSet.getInt("budget_collection_dept_id"));
						preparedStatement.executeUpdate();
					}
					//之前没有数据的需要插入到汇总表
					for(Integer deptId : deptIdSet){
						preparedStatement = connection.prepareStatement("INSERT INTO ys_budget_collection_dept(dept_id, budget_type, year, status) VALUES (?,?,?,?)", PreparedStatement.RETURN_GENERATED_KEYS);
						preparedStatement.setInt(1, deptId);
						preparedStatement.setInt(2, HospitalConstant.COLLECTIONTYPE_EXPEND);
						preparedStatement.setString(3, year);
						preparedStatement.setInt(4, HospitalConstant.COLLECTIONSTATUS_WAIT);
						preparedStatement.executeUpdate();
						resultSet = preparedStatement.getGeneratedKeys();
						Integer collectionDeptId = null;
						if (resultSet.next()) {
							collectionDeptId = resultSet.getInt(1);
						}
						collectionDeptIdMap.put(deptId, collectionDeptId);
					}
					
					//存储详情表数据
					for(Map<String, Object> routineInfo : routineProjectList){
						preparedStatement = connection.prepareStatement("INSERT INTO ys_expend_collection_info(budget_collection_dept_id, "
								+ "year, project_source, routine_project_id, bottom_level, project_amount, formula_remark, attachment, insert_time, insert_user, version) "
								+ "VALUES (?,?,?,?,?,?,?,?,now(),?,?)");
						List<Object> parmList = new ArrayList<Object>();
						parmList.add(collectionDeptIdMap.get(routineInfo.get("department_info_id")));
						parmList.add(year);
						parmList.add(routineInfo.get("project_source"));
						parmList.add(routineInfo.get("routine_project_id"));
						parmList.add(routineInfo.get("bottom_level"));
						parmList.add(routineInfo.get("project_amount"));
						parmList.add(routineInfo.get("formula_remark"));
						parmList.add(routineInfo.get("attachment"));
						parmList.add(sessionToken.getUserInfoId());
						if(versionMap.containsKey(routineInfo.get("department_info_id"))){
							parmList.add(versionMap.get(routineInfo.get("department_info_id")) + 1);
						}else{
							parmList.add(1);
						}
						DataSourceManager.setParams(preparedStatement, parmList);
						preparedStatement.executeUpdate();
					}
					
					for(Map<String, Object> gemerocInfo : genericProjectList){
						preparedStatement = connection.prepareStatement("INSERT INTO ys_expend_collection_info(budget_collection_dept_id, "
								+ "year, project_source, generic_project_id, bottom_level, project_amount, formula_remark, attachment, insert_time, insert_user, version) "
								+ "VALUES (?,?,?,?,?,?,?,?,now(),?,?)");
						List<Object> parmList = new ArrayList<Object>();
						parmList.add(collectionDeptIdMap.get(gemerocInfo.get("department_info_id")));
						parmList.add(year);
						parmList.add(gemerocInfo.get("project_source"));
						parmList.add(gemerocInfo.get("generic_project_id"));
						parmList.add(gemerocInfo.get("bottom_level"));
						parmList.add(gemerocInfo.get("project_amount"));
						parmList.add(gemerocInfo.get("formula_remark"));
						parmList.add(gemerocInfo.get("attachment"));
						parmList.add(sessionToken.getUserInfoId());
						if(versionMap.containsKey(gemerocInfo.get("department_info_id"))){
							parmList.add(versionMap.get(gemerocInfo.get("department_info_id")) + 1);
						}else{
							parmList.add(1);
						}
						DataSourceManager.setParams(preparedStatement, parmList);
						preparedStatement.executeUpdate();
					}
					
					dealWithDraftProcess(connection, preparedStatement, draftIdList, draftIds, stepId);
					
				}
			}
			connection.commit();
			connection.setAutoCommit(true);
			result.element("isok", "ok");
			result.element("message", "审核成功");
		} catch (SQLException e) {
			e.printStackTrace();
			result.element("isok", "err");
			result.element("message", "系统错误！");
			try {
				connection.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		} finally{
			DataSourceManager.close(connection, preparedStatement, resultSet);
		}
		return result;
	}
	
	/**
	 * 处理步骤数据
	 * @param connection
	 * @param preparedStatement
	 * @param draftIdList
	 * @param draftIds
	 * @param stepId
	 * @throws SQLException
	 */
	public void dealWithDraftProcess(Connection connection, PreparedStatement preparedStatement, List<Integer> draftIdList, String draftIds, int stepId) throws SQLException{
		//先入日志表
		for(Integer draftId : draftIdList){
			preparedStatement = connection.prepareStatement("INSERT INTO `ys_expand_draft_process_log` (`ys_expand_draft_id`, `user_id`, `process_step_info_id`) VALUES (?, ?, ?)");
			preparedStatement.setInt(1, draftId);
			preparedStatement.setInt(2, sessionToken.getUserInfoId());
			preparedStatement.setInt(3, stepId);
			preparedStatement.executeUpdate();
		}
		//删除流程处理人表数据
		preparedStatement = connection.prepareStatement("delete from ys_expand_draft_process where ys_expand_draft_id in (" + draftIds + ")");
		preparedStatement.executeUpdate();
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
