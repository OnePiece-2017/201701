package cn.dmdl.stl.hospitalbudget.budget.session;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.common.session.CriterionEntityHome;
import cn.dmdl.stl.hospitalbudget.util.Assit;
import cn.dmdl.stl.hospitalbudget.util.DataSourceManager;
import cn.dmdl.stl.hospitalbudget.util.HospitalConstant;

@Name("expendAdjustmentHome")
public class ExpendAdjustmentHome  extends CriterionEntityHome<Object>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String year;
	private String deptIds;
	private String fundsSourceId;
	private String adjuestDataStr;
	
	/**
	 * 获取全部待调整数据
	 * @return
	 */
	public JSONObject getAllAdjustInfo(){
		JSONObject result = new JSONObject();
		//获取用户可编制的项目
		Set<Integer> genericProjectIdSet = getGenericProjectIdsByUserId(sessionToken.getUserInfoId());
		Set<Integer> routineProjectIdSet = getRoutineProjectIdsByUserId(sessionToken.getUserInfoId());
		StringBuilder sql = new StringBuilder();
		Connection connection = DataSourceManager.open(DataSourceManager.BY_JDBC_DEFAULT);
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			//先获取项目数据
			JSONArray oldArr = new JSONArray();
			JSONArray newArr = new JSONArray();
			if(routineProjectIdSet.size() > 0){
				String routineProjectIds = Assit.formatIdsSet(routineProjectIdSet);
				sql.append("select b.the_value as project_name, ");
				sql.append("b.the_id as project_id, ");
				sql.append("b.the_pid, ");
				sql.append("b.bottom_level, ");
				sql.append("b.top_level_project_id, ");
				sql.append("c.the_value as dept_name, ");
				sql.append("a.budget_amount, ");
				sql.append("a.budget_amount_frozen, ");
				sql.append("a.budget_amount_surplus ");
				sql.append("from normal_expend_plan_info a ");
				sql.append("INNER JOIN routine_project b ON a.project_id = b.the_id AND b.funds_source_id = ? ");
				sql.append("INNER JOIN ys_department_info c ON a.dept_id = c.the_id ");
				sql.append("WHERE a.`year` = ? AND a.project_id in (").append(routineProjectIds).append(") ");
				preparedStatement = connection.prepareStatement(sql.toString());
				preparedStatement.setString(1, fundsSourceId);
				preparedStatement.setString(2, year);
				resultSet = preparedStatement.executeQuery();
//				JSONArray oldRoutineProjectArr = new JSONArray();
				while(resultSet.next()){
					routineProjectIdSet.remove(resultSet.getInt("project_id"));
					JSONObject json = new JSONObject();
					json.element("project_name", resultSet.getString("project_name"));
					json.element("dept_name", resultSet.getString("dept_name"));
					json.element("budget_amount", resultSet.getDouble("budget_amount"));
					json.element("budget_amount_surplus", resultSet.getDouble("budget_amount_surplus"));
					json.element("project_id", "10" + resultSet.getInt("project_id"));//为了多级表格展示处理
					json.element("the_pid", "10" + resultSet.getInt("the_pid"));
					json.element("top_level_project_id", "r" + resultSet.getInt("top_level_project_id"));
					json.element("is_usual", 1);
					if(resultSet.getInt("the_pid") == 0){
						json.element("is_root", 1);
					}
					json.element("bottom_level", resultSet.getInt("bottom_level"));
					json.element("data_the_id", resultSet.getString("project_id"));
					json.element("data_top_level_project_id", resultSet.getInt("top_level_project_id"));
					oldArr.add(json);
//					oldRoutineProjectArr.add(json);
				}
//				result.element("old_routine_project", oldRoutineProjectArr);
//				result.element("new_routine_project", getRoutineProjectInfoByIdSet(routineProjectIdSet));
				newArr.addAll(getRoutineProjectInfoByIdSet(routineProjectIdSet));
			}
			
			if(genericProjectIdSet.size() > 0){
				sql.delete(0, sql.length());
				String genericProjectIds = Assit.formatIdsSet(genericProjectIdSet);
				sql.append("select b.the_value as project_name, ");
				sql.append("b.the_id as project_id, ");
				sql.append("b.the_pid, ");
				sql.append("b.bottom_level, ");
				sql.append("b.top_level_project_id, ");
				sql.append("c.the_value as dept_name, ");
				sql.append("a.budget_amount, ");
				sql.append("a.budget_amount_frozen, ");
				sql.append("a.budget_amount_surplus ");
				sql.append("from normal_expend_plan_info a ");
				sql.append("INNER JOIN generic_project b ON a.generic_project_id = b.the_id AND b.funds_source_id = ? ");
				sql.append("INNER JOIN ys_department_info c ON a.dept_id = c.the_id ");
				sql.append("WHERE a.`year` = ? AND a.generic_project_id in (").append(genericProjectIds).append(") ");
				preparedStatement = connection.prepareStatement(sql.toString());
				preparedStatement.setString(1, fundsSourceId);
				preparedStatement.setString(2, year);
				resultSet = preparedStatement.executeQuery();
//				JSONArray oldGenericProjectArr = new JSONArray();
				while(resultSet.next()){
					genericProjectIdSet.remove(resultSet.getInt("project_id"));
					JSONObject json = new JSONObject();
					json.element("project_name", resultSet.getString("project_name"));
					json.element("dept_name", resultSet.getString("dept_name"));
					json.element("budget_amount", resultSet.getDouble("budget_amount"));
					json.element("budget_amount_surplus", resultSet.getDouble("budget_amount_surplus"));
					json.element("project_id", "20" + resultSet.getInt("project_id"));
					json.element("the_pid", "20" + resultSet.getInt("the_pid"));
					json.element("top_level_project_id", "g" + resultSet.getInt("top_level_project_id"));
					json.element("is_usual", 2);
					if(resultSet.getInt("the_pid") == 0){
						json.element("is_root", 1);
					}
					json.element("bottom_level", resultSet.getInt("bottom_level"));
					json.element("data_the_id", resultSet.getString("project_id"));
					json.element("data_top_level_project_id", resultSet.getInt("top_level_project_id"));
					oldArr.add(json);
//					oldGenericProjectArr.add(json);
				}
//				result.element("old_generic_project", oldGenericProjectArr);
//				result.element("new_generic_project", getGenericProjectInfoByIdSet(genericProjectIdSet));
				newArr.addAll(getGenericProjectInfoByIdSet(genericProjectIdSet));
				
			}
			result.element("old_info", oldArr);
			result.element("new_info", newArr);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			DataSourceManager.close(connection, preparedStatement, resultSet);
		}
		System.out.println(result);
		return result;
	}
	
	
	
	/**
	 * 获取项目信息
	 * @param genericProjectIdSet
	 * @return
	 */
	private JSONArray getGenericProjectInfoByIdSet(Set<Integer> genericProjectIdSet) {
		Connection connection = DataSourceManager.open(DataSourceManager.BY_JDBC_DEFAULT);
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		JSONArray genericProjectArr = new JSONArray();
		try {
			//先获取项目数据
			if(genericProjectIdSet.size() > 0){
				String genericProjectIds = Assit.formatIdsSet(genericProjectIdSet);
				StringBuilder sql = new StringBuilder();
				sql.append("select a.the_id as project_id, ");
				sql.append("a.the_pid, ");
				sql.append("a.bottom_level, ");
				sql.append("a.top_level_project_id, ");
				sql.append("a.the_value as project_name, ");
				sql.append("b.the_value as dept_name  ");
				sql.append("from generic_project a  ");
				sql.append("INNER JOIN ys_department_info b ON a.department_info_id = b.the_id  ");
				sql.append("where a.deleted = 0 AND a.the_id in (").append(genericProjectIds).append(") ");
				preparedStatement = connection.prepareStatement(sql.toString());
				resultSet = preparedStatement.executeQuery();
				while(resultSet.next()){
					JSONObject json = new JSONObject();
					json.element("project_name", resultSet.getString("project_name"));
					json.element("dept_name", resultSet.getString("dept_name"));
					json.element("project_id", "10" + resultSet.getInt("project_id"));//为了多级表格展示处理
					json.element("the_pid", "10" + resultSet.getInt("the_pid"));
					json.element("top_level_project_id", "r" + resultSet.getInt("top_level_project_id"));
					json.element("data_the_id", resultSet.getString("project_id"));
					json.element("data_top_level_project_id", resultSet.getInt("top_level_project_id"));
					json.element("is_usual", 2);
					if(resultSet.getInt("the_pid") == 0){
						json.element("is_root", 1);
					}
					json.element("bottom_level", resultSet.getInt("bottom_level"));
					genericProjectArr.add(json);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			DataSourceManager.close(connection, preparedStatement, resultSet);
		}
		return genericProjectArr;
	}



	/**
	 * 获取常规项目信息
	 * @param genericProjectIdSet
	 * @return
	 */
	private JSONArray getRoutineProjectInfoByIdSet(Set<Integer> routineProjectIdSet) {
		Connection connection = DataSourceManager.open(DataSourceManager.BY_JDBC_DEFAULT);
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		JSONArray genericProjectArr = new JSONArray();
		try {
			//先获取项目数据
			if(routineProjectIdSet.size() > 0){
				String routineProjectIds = Assit.formatIdsSet(routineProjectIdSet);
				StringBuilder sql = new StringBuilder();
				sql.append("select a.the_id as project_id, ");
				sql.append("a.the_pid, ");
				sql.append("a.bottom_level, ");
				sql.append("a.top_level_project_id, ");
				sql.append("a.the_value as project_name, ");
				sql.append("b.the_value as dept_name  ");
				sql.append("from routine_project a  ");
				sql.append("INNER JOIN ys_department_info b ON a.department_info_id = b.the_id  ");
				sql.append("where a.deleted = 0 AND a.the_id in (").append(routineProjectIds).append(") ");
				preparedStatement = connection.prepareStatement(sql.toString());
				resultSet = preparedStatement.executeQuery();
				while(resultSet.next()){
					JSONObject json = new JSONObject();
					json.element("project_name", resultSet.getString("project_name"));
					json.element("dept_name", resultSet.getString("dept_name"));
					json.element("project_id", "10" + resultSet.getInt("project_id"));//为了多级表格展示处理
					json.element("the_pid", "10" + resultSet.getInt("the_pid"));
					json.element("top_level_project_id", "r" + resultSet.getInt("top_level_project_id"));
					json.element("data_the_id", resultSet.getString("project_id"));
					json.element("data_top_level_project_id", resultSet.getInt("top_level_project_id"));
					json.element("is_usual", 1);
					if(resultSet.getInt("the_pid") == 0){
						json.element("is_root", 1);
					}
					json.element("bottom_level", resultSet.getInt("bottom_level"));
					genericProjectArr.add(json);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			DataSourceManager.close(connection, preparedStatement, resultSet);
		}
		return genericProjectArr;
	}



	/**
	 * 根据用户id获取可编制的常规项目
	 * @param userInfoId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Set<Integer> getRoutineProjectIdsByUserId(Integer userInfoId) {
		List<Object> list = getEntityManager().createNativeQuery("select project_id from routine_project_compiler t where t.user_info_id = " + userInfoId).getResultList();
		Set<Integer> set = new HashSet<Integer>();
		for(Object obj : list){
			set.add(Integer.parseInt(obj.toString()));
		}
		return set;
	}

	/**
	 * 根据用户id获取用户可编制的项目
	 * @param userInfoId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Set<Integer> getGenericProjectIdsByUserId(Integer userInfoId) {
		List<Object> list = getEntityManager().createNativeQuery("select project_id from generic_project_compiler t where t.user_info_id = " + userInfoId).getResultList();
		Set<Integer> set = new HashSet<Integer>();
		for(Object obj : list){
			set.add(Integer.parseInt(obj.toString()));
		}
		return set;
	}
	
	/**
	 * 预算调整
	 * @return
	 */
	public JSONObject adjuestBudget(){
		JSONObject result = new JSONObject();
		JSONArray adjustInfoArr = JSONArray.fromObject(adjuestDataStr);
		Set<Integer> genericProjectIdSet = new HashSet<Integer>();
		Set<Integer> routineProjectIdSet = new HashSet<Integer>();
		for(Object obj : adjustInfoArr){
			JSONObject json = JSONObject.fromObject(obj);
			if(json.getInt("is_usual") == 1){//常规项目
				routineProjectIdSet.add(json.getInt("data_the_id"));
			}else{
				genericProjectIdSet.add(json.getInt("data_the_id"));
			}
		}
		//校验是否存在未完成的调整数据
		Connection connection = DataSourceManager.open(DataSourceManager.BY_JDBC_DEFAULT);
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			StringBuilder sql = new StringBuilder();
			//校验业务
			if(routineProjectIdSet.size() > 0){
				String routineProjectIds = Assit.formatIdsSet(routineProjectIdSet);
				sql.delete(0, sql.length());
				sql.append("select 1 ");
				sql.append("from ys_expand_draft t ");
				sql.append("where t.`year` = ? AND t.draft_type = ? AND t.`status`= ? AND t.routine_project_id in (").append(routineProjectIds).append(") and t.`delete`=0 ");
				preparedStatement = connection.prepareStatement(sql.toString());
				preparedStatement.setString(1, year);
				preparedStatement.setInt(2, HospitalConstant.DRAFT_TYPE_ADJUSTMENT);
				preparedStatement.setInt(3, HospitalConstant.DRAFTSTATUS_AUDIT);
				resultSet = preparedStatement.executeQuery();
				while(resultSet.next()){
					result.element("isok", "err");
					result.element("message", "存在正在审核中的预算调整！");
					return result;
				}
			}
			if(genericProjectIdSet.size() > 0){
				String genericProjectIds = Assit.formatIdsSet(genericProjectIdSet);
				sql.delete(0, sql.length());
				sql.append("select 1 ");
				sql.append("from ys_expand_draft t ");
				sql.append("where t.`year` = ? AND t.draft_type = ? AND t.`status`= ? AND t.generic_project_id in (").append(genericProjectIds).append(") and t.`delete`=0 ");
				preparedStatement = connection.prepareStatement(sql.toString());
				preparedStatement.setString(1, year);
				preparedStatement.setInt(2, HospitalConstant.DRAFT_TYPE_ADJUSTMENT);
				preparedStatement.setInt(3, HospitalConstant.DRAFTSTATUS_AUDIT);
				resultSet = preparedStatement.executeQuery();
				while(resultSet.next()){
					result.element("isok", "err");
					result.element("message", "存在正在审核中的预算调整！");
					return result;
				}
			}
			
			//入库处理
			connection.setAutoCommit(false);
			StringBuilder sql2 = new StringBuilder();
			sql2.append("INSERT INTO `ys_expand_draft` (`year`, `project_source`, `top_level_project_id`, `routine_project_id`, `generic_project_id`, ");
			sql2.append("`project_amount`, `formula_remark`, ");
			sql2.append("`insert_time`, `insert_user`, `status`, `draft_type`)  ");
			sql2.append("VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), ?, ?, ?) ");
			for(Object obj : adjustInfoArr){
				JSONObject json = JSONObject.fromObject(obj);
				List<Object> paramList = new ArrayList<Object>();
				preparedStatement = connection.prepareStatement(sql2.toString(), PreparedStatement.RETURN_GENERATED_KEYS);
				paramList.add(year);
				paramList.add(URLEncoder.encode(json.getString("project_source"),"UTF-8"));
				paramList.add(json.getInt("data_top_level_project_id"));
				if(json.getInt("is_usual") == 1){//常规
					paramList.add(json.getInt("data_the_id"));
					paramList.add(null);
				}else{//项目
					paramList.add(null);
					paramList.add(json.getInt("data_the_id"));
				}
				paramList.add(json.getDouble("adjuest_amount"));
				paramList.add(URLEncoder.encode(json.getString("project_remark"),"UTF-8"));
				paramList.add(sessionToken.getUserInfoId());
				paramList.add(HospitalConstant.DRAFTSTATUS_AUDIT);
				paramList.add(HospitalConstant.DRAFT_TYPE_ADJUSTMENT);
				System.out.println(paramList);
				DataSourceManager.setParams(preparedStatement, paramList);
				preparedStatement.executeUpdate();
				resultSet = preparedStatement.getGeneratedKeys();
				Integer draftId = null;
				if (resultSet.next()) {
					draftId = resultSet.getInt(1);
					if(json.getInt("is_usual") == 1){//常规
						sql.delete(0, sql.length());
						sql.append("select si.process_step_info_id,su.user_id from process_info a ");
						sql.append("INNER JOIN process_step_info si ON a.process_info_id = si.process_info_id ");
						sql.append("INNER JOIN process_step_user su ON si.process_step_info_id = su.process_step_info_id AND su.type = 0  ");
						sql.append("INNER JOIN routine_project t ON a.dept_id = t.department_info_id  AND t.the_id = ? ");
						sql.append("where si.step_index = 1 AND a.process_type = ? AND a.project_process_type = ? ");
						preparedStatement = connection.prepareStatement(sql.toString());
						preparedStatement.setInt(1, json.getInt("data_the_id"));
						preparedStatement.setInt(2, HospitalConstant.PROCESS_TYPE_NORMAL);
						preparedStatement.setInt(3, HospitalConstant.PROCESS_EXPEND_DRAFT);
						resultSet = preparedStatement.executeQuery();
						while(resultSet.next()){
							//添加新的任务执行人
							preparedStatement = connection.prepareStatement("INSERT INTO `ys_expand_draft_process` (`ys_expand_draft_id`, `user_id`, `process_step_info_id`) VALUES (?, ?, ?)");
							preparedStatement.setInt(1, draftId);
							preparedStatement.setInt(2, resultSet.getInt("user_id"));
							preparedStatement.setInt(3, resultSet.getInt("process_step_info_id"));
							preparedStatement.executeUpdate();
						}
					}else{
						sql.delete(0, sql.length());
						sql.append("select si.process_step_info_id,su.user_id from process_info a ");
						sql.append("INNER JOIN process_step_info si ON a.process_info_id = si.process_info_id ");
						sql.append("INNER JOIN process_step_user su ON si.process_step_info_id = su.process_step_info_id AND su.type = 0  ");
						sql.append("INNER JOIN generic_project t ON a.dept_id = t.department_info_id  AND t.the_id = ? ");
						sql.append("where si.step_index = 1 AND a.process_type = ? AND a.project_process_type = ? ");
						preparedStatement = connection.prepareStatement(sql.toString());
						preparedStatement.setInt(1, json.getInt("data_the_id"));
						preparedStatement.setInt(2, HospitalConstant.PROCESS_TYPE_PROJECT);
						preparedStatement.setInt(3, HospitalConstant.PROCESS_EXPEND_DRAFT);
						resultSet = preparedStatement.executeQuery();
						while(resultSet.next()){
							//添加新的任务执行人
							preparedStatement = connection.prepareStatement("INSERT INTO `ys_expand_draft_process` (`ys_expand_draft_id`, `user_id`, `process_step_info_id`) VALUES (?, ?, ?)");
							preparedStatement.setInt(1, draftId);
							preparedStatement.setInt(2, resultSet.getInt("user_id"));
							preparedStatement.setInt(3, resultSet.getInt("process_step_info_id"));
							preparedStatement.executeUpdate();
						}
					}
				}
				
			}
			connection.commit();
			connection.setAutoCommit(true);
		} catch (SQLException e) {
			result.element("isok", "err");
			result.element("message", "系统错误！");
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			result.element("isok", "err");
			result.element("message", "系统错误！");
			e.printStackTrace();
		} finally{
			DataSourceManager.close(connection, preparedStatement, resultSet);
		}
		result.element("isok", "ok");
		result.element("message", "提交成功！");
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
	public String getAdjuestDataStr() {
		return adjuestDataStr;
	}
	public void setAdjuestDataStr(String adjuestDataStr) {
		this.adjuestDataStr = adjuestDataStr;
	}
	
	

}
