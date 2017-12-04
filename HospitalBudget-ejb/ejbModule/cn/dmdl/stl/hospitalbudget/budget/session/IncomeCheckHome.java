package cn.dmdl.stl.hospitalbudget.budget.session;

import java.io.IOException;
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

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.common.session.CriterionEntityHome;
import cn.dmdl.stl.hospitalbudget.util.Assit;
import cn.dmdl.stl.hospitalbudget.util.CommonDaoUtil;
import cn.dmdl.stl.hospitalbudget.util.DataSourceManager;
import cn.dmdl.stl.hospitalbudget.util.HospitalConstant;

@Name("incomeCheckHome")
public class IncomeCheckHome  extends CriterionEntityHome<Object>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String year;
	private String deptIds;
	private String fundsSourceId;
	private String incomeDraftInfo;
	
	/**
	 * 获取编制信息
	 * @return
	 */
	public JSONObject getDraftInfo(){
		Map<String, String> projectNatureMap = CommonDaoUtil.getProjectNatureMap();
		JSONObject draftInfojson = new JSONObject();
		StringBuilder sql = new StringBuilder();
		sql.append("select ed.ys_income_draft_id, ");
		sql.append("rp.the_value as project_name, ");
		sql.append("1 as is_usual, ");
		sql.append("di.the_value as dept_name, ");
		sql.append("ed.project_source, ");
		sql.append("ed.project_nature, ");
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
		sql.append("from ys_income_draft ed ");
		sql.append("INNER JOIN routine_project rp ON ed.routine_project_id = rp.the_id AND rp.funds_source_id = ").append(fundsSourceId).append(" ");
		sql.append("INNER JOIN ys_department_info di ON rp.department_info_id = di.the_id  ");
		sql.append("INNER JOIN ys_income_draft_process edp ON ed.ys_income_draft_id = edp.ys_income_draft_id ");
		sql.append("AND edp.user_id = ").append(sessionToken.getUserInfoId()).append(" ");
		sql.append("WHERE ed.delete = 0 AND ed.year = '").append(year).append("' ");
		sql.append("GROUP BY ed.ys_income_draft_id ");
		sql.append("UNION ");
		sql.append("select ed.ys_income_draft_id, ");
		sql.append("rp.the_value as project_name, ");
		sql.append("2 as is_usual, ");
		sql.append("di.the_value as dept_name, ");
		sql.append("ed.project_source, ");
		sql.append("ed.project_nature, ");
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
		sql.append("from ys_income_draft ed ");
		sql.append("INNER JOIN generic_project rp ON ed.generic_project_id = rp.the_id AND rp.funds_source_id = ").append(fundsSourceId).append(" ");
		sql.append("INNER JOIN ys_department_info di ON rp.department_info_id = di.the_id  ");
		sql.append("INNER JOIN ys_income_draft_process edp ON ed.ys_income_draft_id = edp.ys_income_draft_id ");
		sql.append("AND edp.user_id = ").append(sessionToken.getUserInfoId()).append(" ");
		sql.append("WHERE ed.delete = 0 AND ed.year = '").append(year).append("' AND ed.status = ").append(HospitalConstant.DRAFTSTATUS_AUDIT).append(" ");
		sql.append("GROUP BY ed.ys_income_draft_id ");
		sql.insert(0, "select * from (").append(") t order by t.top_level_project_id,t.bottom_level ");
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
				json.element("ys_income_draft_id", resultSet.getInt("ys_income_draft_id"));
				json.element("project_name", resultSet.getString("project_name"));
				json.element("is_usual", resultSet.getInt("is_usual"));
				json.element("dept_name", resultSet.getString("dept_name"));
				json.element("project_nature", projectNatureMap.get(resultSet.getString("project_nature")));
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
		JSONArray arr = JSONArray.fromObject(incomeDraftInfo);
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
				boolean hasNext = false;
				while(resultSet.next()){
					hasNext = true;
					if(resultSet.isFirst()){
						goAheadDraftProcess(connection, preparedStatement, draftIdList, draftIds, stepId);
					}
					//添加新的任务执行人
					for(Integer draftId : draftIdList){
						preparedStatement = connection.prepareStatement("INSERT INTO `ys_income_draft_process` (`ys_income_draft_id`, `user_id`, `process_step_info_id`) VALUES (?, ?, ?)");
						preparedStatement.setInt(1, draftId);
						preparedStatement.setInt(2, resultSet.getInt("user_id"));
						preparedStatement.setInt(3, resultSet.getInt("process_step_info_id"));
						preparedStatement.executeUpdate();
					}
				}
				if(!hasNext){//没有下一步人审核，进入汇总
					//查询出当前的编制数据
					sql.delete(0, sql.length());
					sql.append("select ed.`year`, ");
					sql.append("1 as is_usual, ");
					sql.append("ed.project_source, ");
					sql.append("ed.project_nature, ");
					sql.append("ed.top_level_project_id, ");
					sql.append("ed.routine_project_id as project_id, ");
					sql.append("ed.project_amount, ");
					sql.append("ed.formula_remark, ");
					sql.append("ed.attachment, ");
					sql.append("rp.bottom_level, ");
					sql.append("rp.department_info_id ");
					sql.append("from ys_income_draft ed ");
					sql.append("INNER JOIN routine_project rp ON ed.routine_project_id = rp.the_id  ");
					sql.append("WHERE ed.ys_income_draft_id in (").append(draftIds).append(") ");
					sql.append("UNION ");
					sql.append("select ed.`year`, ");
					sql.append("2 as is_usual, ");
					sql.append("ed.project_source, ");
					sql.append("ed.project_nature, ");
					sql.append("ed.top_level_project_id, ");
					sql.append("ed.generic_project_id as project_id, ");
					sql.append("ed.project_amount, ");
					sql.append("ed.formula_remark, ");
					sql.append("ed.attachment, ");
					sql.append("rp.bottom_level, ");
					sql.append("rp.department_info_id ");
					sql.append("from ys_income_draft ed ");
					sql.append("INNER JOIN generic_project rp ON ed.generic_project_id = rp.the_id  ");
					sql.append("WHERE ed.ys_income_draft_id in (").append(draftIds).append(") ");
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
						map.put("project_nature", resultSet.getString("project_nature"));
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
					//更新审核状态
					preparedStatement = connection.prepareStatement("update ys_income_draft set `status` = ? where ys_income_draft_id in (" + draftIds + ")");
					preparedStatement.setInt(1, HospitalConstant.DRAFTSTATUS_FINISH);
					preparedStatement.executeUpdate();
					String deptIds = Assit.formatIdsSet(deptIdSet);
					//检查有没有已经下达的数据
					preparedStatement = connection.prepareStatement("select 1 from ys_budget_collection_dept bcd where bcd.`year` = ? "
							+ "AND bcd.dept_id in (" + deptIds + ") AND budget_type = ? AND bcd.`status` = ?");
					preparedStatement.setString(1, year);
					preparedStatement.setInt(2, HospitalConstant.COLLECTIONTYPE_INCOME);
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
					sql.append("select t1.dept_id,MAX(t.version) as max_version from ys_income_collection_info_log t  ");
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
					preparedStatement.setInt(2, HospitalConstant.COLLECTIONTYPE_INCOME);
					preparedStatement.setInt(3, HospitalConstant.COLLECTIONSTATUS_WAIT);
					preparedStatement.setInt(4, HospitalConstant.COLLECTIONSTATUS_TAKEBACK);
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
						preparedStatement.setInt(2, HospitalConstant.COLLECTIONTYPE_INCOME);
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
						preparedStatement = connection.prepareStatement("INSERT INTO ys_income_collection_info(budget_collection_dept_id, "
								+ "year, project_source, project_nature, routine_project_id, bottom_level, project_amount, formula_remark, attachment, insert_time, insert_user, version) "
								+ "VALUES (?,?,?,?,?,?,?,?,?,now(),?,?)");
						List<Object> parmList = new ArrayList<Object>();
						parmList.add(collectionDeptIdMap.get(routineInfo.get("department_info_id")));
						parmList.add(year);
						parmList.add(routineInfo.get("project_source"));
						parmList.add(routineInfo.get("project_nature"));
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
						preparedStatement = connection.prepareStatement("INSERT INTO ys_income_collection_info(budget_collection_dept_id, "
								+ "year, project_source, project_nature, generic_project_id, bottom_level, project_amount, formula_remark, attachment, insert_time, insert_user, version) "
								+ "VALUES (?,?,?,?,?,?,?,?,?,now(),?,?)");
						List<Object> parmList = new ArrayList<Object>();
						parmList.add(collectionDeptIdMap.get(gemerocInfo.get("department_info_id")));
						parmList.add(year);
						parmList.add(gemerocInfo.get("project_source"));
						parmList.add(gemerocInfo.get("project_nature"));
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
					
					goAheadDraftProcess(connection, preparedStatement, draftIdList, draftIds, stepId);
					
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
	 * 通过处理步骤数据
	 * @param connection
	 * @param preparedStatement
	 * @param draftIdList
	 * @param draftIds
	 * @param stepId
	 * @throws SQLException
	 */
	public void goAheadDraftProcess(Connection connection, PreparedStatement preparedStatement, List<Integer> draftIdList, String draftIds, int stepId) throws SQLException{
		//先入日志表
		for(Integer draftId : draftIdList){
			preparedStatement = connection.prepareStatement("INSERT INTO `ys_income_draft_process_log` (`ys_income_draft_id`, `user_id`, `process_step_info_id`) VALUES (?, ?, ?)");
			preparedStatement.setInt(1, draftId);
			preparedStatement.setInt(2, sessionToken.getUserInfoId());
			preparedStatement.setInt(3, stepId);
			preparedStatement.executeUpdate();
		}
		//删除流程处理人表数据
		preparedStatement = connection.prepareStatement("delete from ys_income_draft_process where ys_income_draft_id in (" + draftIds + ")");
		preparedStatement.executeUpdate();
	}
	
	
	/**
	 * 退回处理步骤数据
	 * @param connection
	 * @param preparedStatement
	 * @param draftIdList
	 * @param draftIds
	 * @param stepId
	 * @throws SQLException
	 */
	public void returnDraftProcess(Connection connection, PreparedStatement preparedStatement, List<Integer> draftIdList, String draftIds, int stepId) throws SQLException{
		//先入日志表
		for(Integer draftId : draftIdList){
			preparedStatement = connection.prepareStatement("INSERT INTO `ys_income_draft_process_log` (`ys_income_draft_id`, `user_id`, `process_step_info_id`, `operate_type`) VALUES (?, ?, ?, ?)");
			preparedStatement.setInt(1, draftId);
			preparedStatement.setInt(2, sessionToken.getUserInfoId());
			preparedStatement.setInt(3, stepId);
			preparedStatement.setInt(4, 1);//不通过
			preparedStatement.executeUpdate();
		}
		//删除流程处理人表数据
		preparedStatement = connection.prepareStatement("delete from ys_income_draft_process where ys_income_draft_id in (" + draftIds + ")");
		preparedStatement.executeUpdate();
	}
	
	/**
	 * 预算追回
	 * @return
	 */
	public JSONObject takeBackDraft(){
		JSONObject result = new JSONObject();
		Connection connection = DataSourceManager.open(DataSourceManager.BY_JDBC_DEFAULT);
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			preparedStatement = connection.prepareStatement("select budget_collection_dept_id,dept_id,`status` from ys_budget_collection_dept where `year` = ? AND budget_type = ? AND dept_id in (" + deptIds + ")");
			preparedStatement.setString(1, year);
			preparedStatement.setInt(2, HospitalConstant.COLLECTIONTYPE_INCOME);
			resultSet = preparedStatement.executeQuery();
			Set<Integer> collectionDeptIdSet = new HashSet<Integer>();
			boolean takeBackFlas = false;//是否可被追回追回
			while(resultSet.next()){
				if(resultSet.getInt("status") == 1){
					result.element("isok", "err");
					result.element("message", "您的部门预算已经下达，不能追回！");
					return result;
				}else if(resultSet.getInt("status") == 2){
					result.element("isok", "err");
					result.element("message", "您的部门预算已经被追回！");
					return result;
				}else if(resultSet.getInt("status") == 0){
					takeBackFlas = true;
					collectionDeptIdSet.add(resultSet.getInt("budget_collection_dept_id"));
				}
			}
			if(takeBackFlas){
				StringBuilder sql = new StringBuilder();
				connection.setAutoCommit(false);
				for(Integer collectionDeptId : collectionDeptIdSet){
					//修改汇总表状态
					preparedStatement = connection.prepareStatement("UPDATE ys_budget_collection_dept set `status` = ? where budget_collection_dept_id = ?");
					preparedStatement.setInt(1, HospitalConstant.COLLECTIONSTATUS_TAKEBACK);
					preparedStatement.setInt(2, collectionDeptId);
					preparedStatement.executeUpdate();
					//插入历史版本
					sql.delete(0, sql.length());
					sql.append("INSERT INTO `ys_income_collection_info_log` ");
					sql.append("(`budget_collection_dept_id`, `year`, `project_source`, `project_nature`, `routine_project_id`, `generic_project_id`, ");
					sql.append("`bottom_level`, `project_amount`, `formula_remark`, `attachment`, `insert_time`, ");
					sql.append("`insert_user`, `version`, `delete`) ");
					sql.append("SELECT budget_collection_dept_id, year, project_source, project_nature, routine_project_id, generic_project_id, ");
					sql.append("bottom_level, project_amount, formula_remark, attachment, insert_time, ");
					sql.append("insert_user, version, `delete` FROM ys_income_collection_info where budget_collection_dept_id = ? ");
					preparedStatement = connection.prepareStatement(sql.toString());
					preparedStatement.setInt(1, collectionDeptId);
					preparedStatement.executeUpdate();
					//删除当前版本信息
					preparedStatement = connection.prepareStatement("delete from ys_income_collection_info where budget_collection_dept_id = ?");
					preparedStatement.setInt(1, collectionDeptId);
					preparedStatement.executeUpdate();
				}
				//将预算编制改为追回状态
				sql.delete(0, sql.length());
				sql.append("SELECT ed.ys_income_draft_id FROM ys_income_draft ed  ");
				sql.append("INNER JOIN generic_project t ON ed.generic_project_id = t.the_id ");
				sql.append("WHERE ed.`year` = ? AND t.department_info_id in (").append(deptIds).append(") ");
				sql.append("UNION ");
				sql.append("SELECT ed.ys_income_draft_id FROM ys_income_draft ed  ");
				sql.append("INNER JOIN routine_project t ON ed.routine_project_id = t.the_id ");
				sql.append("WHERE ed.`year` = ? AND t.department_info_id in (").append(deptIds).append(") ");
				preparedStatement = connection.prepareStatement(sql.toString());
				preparedStatement.setString(1, year);
				preparedStatement.setString(2, year);
				resultSet = preparedStatement.executeQuery();
				Set<Integer> incomeDraftIdSet = new HashSet<Integer>();
				while(resultSet.next()){
					incomeDraftIdSet.add(resultSet.getInt("ys_income_draft_id"));
				}
				//更新编制表
				if(incomeDraftIdSet.size() > 0){
					String incomeDraftIds = Assit.formatIdsSet(incomeDraftIdSet);
					preparedStatement = connection.prepareStatement("update ys_income_draft set `status` = ? where ys_income_draft_id in (" + incomeDraftIds + ")");
					preparedStatement.setInt(1, HospitalConstant.DRAFTSTATUS_TAKEBACK);
					preparedStatement.executeUpdate();
					//清除审核中的任务
					preparedStatement = connection.prepareStatement("DELETE FROM ys_income_draft_process where ys_income_draft_id in (" + incomeDraftIds + ")");
					preparedStatement.executeUpdate();
				}
				connection.commit();
				connection.setAutoCommit(true);
			}else{
				result.element("isok", "err");
				result.element("message", "没有可被追回的预算！");
				return result;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			DataSourceManager.close(connection, preparedStatement, resultSet);
		}
		result.element("isok", "ok");
		result.element("message", "追回成功！");
		return result;
	}
	
	/**
	 * 退回编制
	 * @return
	 */
	public JSONObject returnDraft(){
		JSONObject result = new JSONObject();
		Connection connection = DataSourceManager.open(DataSourceManager.BY_JDBC_DEFAULT);
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		System.out.println(incomeDraftInfo);
		JSONArray arr = JSONArray.fromObject(incomeDraftInfo);
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
		try {
			connection.setAutoCommit(false);
			for(Integer stepId : draftInfoMap.keySet()){
				List<Integer> draftIdList = draftInfoMap.get(stepId);
				String draftIds = Assit.formatIdsList(draftIdList);
				preparedStatement = connection.prepareStatement("update ys_income_draft set `status` = ? where ys_income_draft_id in (" + draftIds + ")");
				preparedStatement.setInt(1, HospitalConstant.DRAFTSTATUS_RETURN);
				preparedStatement.executeUpdate();
				returnDraftProcess(connection, preparedStatement, draftIdList, draftIds, stepId);
				
			}
			connection.commit();
			connection.setAutoCommit(true);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			DataSourceManager.close(connection, preparedStatement, resultSet);
		}
		result.element("isok", "ok");
		result.element("message", "退回成功！");
		return result;
	}
	/**
	 * 导出
	 */
	public void expExcel(){
		FacesContext ctx = FacesContext.getCurrentInstance();
		ctx.responseComplete();
		HttpServletResponse response = (HttpServletResponse) ctx
				.getExternalContext().getResponse();
		Workbook workbook = new HSSFWorkbook();
		// 标题字体
		HSSFCellStyle colTitleStyle = (HSSFCellStyle) workbook
				.createCellStyle();
		HSSFFont titleFont = (HSSFFont) workbook.createFont();
		titleFont.setFontName("宋体");
		titleFont.setFontHeightInPoints((short) 16);// 字体大小
		colTitleStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		colTitleStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		colTitleStyle.setFont(titleFont);
		colTitleStyle.setBorderBottom((short) 1);
		colTitleStyle.setBorderLeft((short) 1);
		colTitleStyle.setBorderRight((short) 1);
		colTitleStyle.setBorderTop((short) 1);
		colTitleStyle.setFillBackgroundColor(HSSFColor.YELLOW.index);
		// 普通单元格样式
		HSSFCellStyle colStyle = (HSSFCellStyle) workbook.createCellStyle();
		HSSFFont colFont = (HSSFFont) workbook.createFont();
		colFont.setFontName("宋体");
		colFont.setFontHeightInPoints((short) 11);// 字体大小
		colStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		colStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		colStyle.setFont(colFont);
		colStyle.setBorderBottom((short) 1);
		colStyle.setBorderLeft((short) 1);
		colStyle.setBorderRight((short) 1);
		colStyle.setBorderTop((short) 1);
		colStyle.setFillForegroundColor(HSSFColor.YELLOW.index);

		int count = 1;
		String headStr = "支出审核表";
		Sheet sheet = workbook.createSheet();
		workbook.setSheetName(count - 1, headStr);
		int rowIndex = 0;
		int colIndex = 0;

		CellRangeAddress range = null;

		// 第一行
		HSSFRow rowTitle = (HSSFRow) sheet.createRow(rowIndex);
		rowTitle.setHeightInPoints(30f);
		HSSFCell cellTitle = rowTitle.createCell(colIndex);
		cellTitle.setCellType(HSSFCell.CELL_TYPE_STRING);
		cellTitle.setCellValue("支出审核");
		cellTitle.setCellStyle(colTitleStyle);
		range = new CellRangeAddress(0, 0, 0, 6);
		sheet.addMergedRegion(range);
		excelAddBorder(1, range, sheet, workbook);
		
		rowIndex ++;
		HSSFRow row1 = (HSSFRow) sheet.createRow(rowIndex);
		row1.setHeightInPoints(20f);
		HSSFCell nameCol = row1.createCell(colIndex);
		nameCol.setCellType(HSSFCell.CELL_TYPE_STRING);
		nameCol.setCellValue("项目名称");
		nameCol.setCellStyle(colStyle);
		nameCol.getSheet().setColumnWidth(
				nameCol.getColumnIndex(), 35 * 120);
		colIndex++;

		HSSFCell natureCol = row1.createCell(colIndex);
		natureCol.setCellType(HSSFCell.CELL_TYPE_STRING);
		natureCol.setCellValue("项目性质");
		natureCol.setCellStyle(colStyle);
		natureCol.getSheet().setColumnWidth(natureCol.getColumnIndex(),
				35 * 120);
		colIndex++;

		HSSFCell symbolCol = row1.createCell(colIndex);
		symbolCol.setCellType(HSSFCell.CELL_TYPE_STRING);
		symbolCol.setCellValue("批准文号");
		symbolCol.setCellStyle(colStyle);
		symbolCol.getSheet().setColumnWidth(symbolCol.getColumnIndex(),
				35 * 120);
		colIndex++;

		HSSFCell moneyCol = row1.createCell(colIndex);
		moneyCol.setCellType(HSSFCell.CELL_TYPE_STRING);
		moneyCol.setCellValue("项目金额（万元）");
		moneyCol.setCellStyle(colStyle);
		moneyCol.getSheet().setColumnWidth(moneyCol.getColumnIndex(),
				35 * 120);
		colIndex++;
		
		HSSFCell addMoneyCol = row1.createCell(colIndex);
		addMoneyCol.setCellType(HSSFCell.CELL_TYPE_STRING);
		addMoneyCol.setCellValue("与上一年预算同比增减（万元）");
		addMoneyCol.setCellStyle(colStyle);
		addMoneyCol.getSheet().setColumnWidth(addMoneyCol.getColumnIndex(),
				35 * 200);
		colIndex++;
		
		HSSFCell addPercentCol = row1.createCell(colIndex);
		addPercentCol.setCellType(HSSFCell.CELL_TYPE_STRING);
		addPercentCol.setCellValue("与上一年预算同比增率");
		addPercentCol.setCellStyle(colStyle);
		addPercentCol.getSheet().setColumnWidth(addPercentCol.getColumnIndex(),
				35 * 200);
		colIndex++;
		
		HSSFCell commentCol = row1.createCell(colIndex);
		commentCol.setCellType(HSSFCell.CELL_TYPE_STRING);
		commentCol.setCellValue("金额额计算依据及备注");
		commentCol.setCellStyle(colStyle);
		commentCol.getSheet().setColumnWidth(commentCol.getColumnIndex(),
				35 * 200);
		colIndex++;
		
		JSONArray draftInfoArray = new JSONArray();
		StringBuilder sql = new StringBuilder();
		sql.append("select ed.ys_income_draft_id, ");
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
		sql.append("from ys_income_draft ed ");
		sql.append("INNER JOIN routine_project rp ON ed.routine_project_id = rp.the_id AND rp.funds_source_id = ").append(fundsSourceId).append(" ");
		sql.append("INNER JOIN ys_department_info di ON rp.department_info_id = di.the_id  ");
		sql.append("INNER JOIN ys_income_draft_process edp ON ed.ys_income_draft_id = edp.ys_income_draft_id ");
		sql.append("AND edp.user_id = ").append(sessionToken.getUserInfoId()).append(" ");
		sql.append("WHERE ed.delete = 0 AND ed.year = '").append(year).append("' ");
		sql.append("GROUP BY ed.ys_income_draft_id ");
		sql.append("UNION ");
		sql.append("select ed.ys_income_draft_id, ");
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
		sql.append("from ys_income_draft ed ");
		sql.append("INNER JOIN generic_project rp ON ed.generic_project_id = rp.the_id AND rp.funds_source_id = ").append(fundsSourceId).append(" ");
		sql.append("INNER JOIN ys_department_info di ON rp.department_info_id = di.the_id  ");
		sql.append("INNER JOIN ys_income_draft_process edp ON ed.ys_income_draft_id = edp.ys_income_draft_id ");
		sql.append("AND edp.user_id = ").append(sessionToken.getUserInfoId()).append(" ");
		sql.append("WHERE ed.delete = 0 AND ed.year = '").append(year).append("' AND ed.status = ").append(HospitalConstant.DRAFTSTATUS_AUDIT).append(" ");
		sql.append("GROUP BY ed.ys_income_draft_id ");
		sql.insert(0, "select * from (").append(") t order by t.top_level_project_id,t.bottom_level ");
		Connection connection = DataSourceManager.open(DataSourceManager.BY_JDBC_DEFAULT);
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			preparedStatement = connection.prepareStatement(sql.toString());
			resultSet = preparedStatement.executeQuery();
			double totalAmount = 0;
			while(resultSet.next()){
				JSONObject json = new JSONObject();
				json.element("ys_income_draft_id", resultSet.getInt("ys_income_draft_id"));
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
			
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}finally{
			DataSourceManager.close(connection, preparedStatement, resultSet);
		}
		
		
		for(int i = 0;i < draftInfoArray.size(); i++){
			JSONObject json = draftInfoArray.getJSONObject(i);
			rowIndex ++;
			int col = 0;
			row1 = (HSSFRow) sheet.createRow(rowIndex);
			row1.setHeightInPoints(20f);
			nameCol = row1.createCell(col);
			nameCol.setCellType(HSSFCell.CELL_TYPE_STRING);
			nameCol.setCellValue(json.get("project_name").toString());
			nameCol.setCellStyle(colStyle);
			nameCol.getSheet().setColumnWidth(
					nameCol.getColumnIndex(), 35 * 120);
			col++;
			
			natureCol = row1.createCell(col);
			natureCol.setCellType(HSSFCell.CELL_TYPE_STRING);
			natureCol.setCellValue("1".equals(json.get("is_usual").toString()) ? "常规" : "新增");
			natureCol.setCellStyle(colStyle);
			natureCol.getSheet().setColumnWidth(
					natureCol.getColumnIndex(), 35 * 120);
			col++;
			
			symbolCol = row1.createCell(col);
			symbolCol.setCellType(HSSFCell.CELL_TYPE_STRING);
			symbolCol.setCellValue(json.get("project_source").toString());
			symbolCol.setCellStyle(colStyle);
			symbolCol.getSheet().setColumnWidth(
					symbolCol.getColumnIndex(), 35 * 120);
			col++;
			
			moneyCol = row1.createCell(col);
			moneyCol.setCellType(HSSFCell.CELL_TYPE_STRING);
			moneyCol.setCellValue(json.get("project_amount").toString());
			moneyCol.setCellStyle(colStyle);
			moneyCol.getSheet().setColumnWidth(
					moneyCol.getColumnIndex(), 35 * 120);
			col++;
			
			addMoneyCol = row1.createCell(col);
			addMoneyCol.setCellType(HSSFCell.CELL_TYPE_STRING);
			addMoneyCol.setCellValue(json.get("with_last_year_num").toString());
			addMoneyCol.setCellStyle(colStyle);
			addMoneyCol.getSheet().setColumnWidth(
					addMoneyCol.getColumnIndex(), 35 * 200);
			col++;
			
			addPercentCol = row1.createCell(col);
			addPercentCol.setCellType(HSSFCell.CELL_TYPE_STRING);
			addPercentCol.setCellValue(json.get("with_last_year_percent").toString());
			addPercentCol.setCellStyle(colStyle);
			addPercentCol.getSheet().setColumnWidth(
					addPercentCol.getColumnIndex(), 35 * 200);
			col++;
			
			commentCol = row1.createCell(col);
			commentCol.setCellType(HSSFCell.CELL_TYPE_STRING);
			commentCol.setCellValue(json.get("formula_remark").toString());
			commentCol.setCellStyle(colStyle);
			commentCol.getSheet().setColumnWidth(
					commentCol.getColumnIndex(), 35 * 200);
			col++;
			
		}
		
		response.setContentType("application/vnd.ms-excel");
		try {
			response.setHeader("Content-Disposition", "attachment;filename="
					+ new String("预算审核.xls".getBytes(),
							"iso-8859-1"));
			workbook.write(response.getOutputStream());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/** * 添加excel合并后边框属性 */
	public void excelAddBorder(int arg0, CellRangeAddress range, Sheet sheet,
			Workbook workbook) {
		RegionUtil.setBorderBottom(1, range, sheet, workbook);
		RegionUtil.setBorderLeft(1, range, sheet, workbook);
		RegionUtil.setBorderRight(1, range, sheet, workbook);
		RegionUtil.setBorderTop(1, range, sheet, workbook);
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
	public String getincomeDraftInfo() {
		return incomeDraftInfo;
	}
	public void setincomeDraftInfo(String incomeDraftInfo) {
		this.incomeDraftInfo = incomeDraftInfo;
	}

}
