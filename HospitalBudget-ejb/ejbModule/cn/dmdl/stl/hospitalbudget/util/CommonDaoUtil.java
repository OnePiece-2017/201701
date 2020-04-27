package cn.dmdl.stl.hospitalbudget.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


public class CommonDaoUtil {
	
	/**
	 * 获取项目性质的map
	 * @return
	 */
	public static Map<String, String> getProjectNatureMap(){
		Map<String, String> map = new HashMap<String, String>();
		Connection connection = DataSourceManager.open(DataSourceManager.BY_JDBC_DEFAULT);
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			preparedStatement = connection.prepareStatement("select the_id, the_value from ys_project_nature where deleted = 0");
			resultSet = preparedStatement.executeQuery();
			while(resultSet.next()){
				map.put(resultSet.getString("the_id"), resultSet.getString("the_value"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			DataSourceManager.close(connection, preparedStatement, resultSet);
		}
		return map;
	}
	
	/**
	 * 获取项目性质列表
	 * @return
	 */
	public static JSONArray getProjectNatureList(){
		JSONArray projectNatureArr = new JSONArray();
		Connection connection = DataSourceManager.open(DataSourceManager.BY_JDBC_DEFAULT);
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			preparedStatement = connection.prepareStatement("select the_id, the_value from ys_project_nature where deleted = 0");
			resultSet = preparedStatement.executeQuery();
			while(resultSet.next()){
				JSONObject json = new JSONObject();
				json.element("id", resultSet.getString("the_id"));
				json.element("name", resultSet.getString("the_value"));
				projectNatureArr.add(json);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			DataSourceManager.close(connection, preparedStatement, resultSet);
		}
		return projectNatureArr;
	}
	
	/**
	 * 判断角色是否有本科室的数据权限
	 */
	public static boolean hasLocalDepartmentDataFlag(Integer userId){
		Connection connection = DataSourceManager.open(DataSourceManager.BY_JDBC_DEFAULT);
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			preparedStatement = connection.prepareStatement("select ri.department_data_flag from hospital_budget.user_info ui INNER JOIN hospital_budget.role_info ri ON ui.role_info_id = ri.role_info_id where ui.user_info_id = ?");
			preparedStatement.setInt(1, userId);
			resultSet = preparedStatement.executeQuery();
			while(resultSet.next()){
				if(resultSet.getInt("department_data_flag") == 1){//具备本科数据权限
					return true;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			DataSourceManager.close(connection, preparedStatement, resultSet);
		}
		return false;
	}
	
	/**
	 * 判断角色是否有所有科室的数据权限
	 */
	public static boolean hasAllDepartmentDataFlag(Integer userId){
		Connection connection = DataSourceManager.open(DataSourceManager.BY_JDBC_DEFAULT);
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			preparedStatement = connection.prepareStatement("select ri.role_info_id from hospital_budget.user_info ui INNER JOIN hospital_budget.role_info ri ON ui.role_info_id = ri.role_info_id where ui.user_info_id = ?");
			preparedStatement.setInt(1, userId);
			resultSet = preparedStatement.executeQuery();
			while(resultSet.next()){
				if(resultSet.getInt("role_info_id") == 1 || resultSet.getInt("role_info_id") == 2){//具备全科数据权限  暂时写死，1是系统管理员 2是财务主任
					return true;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			DataSourceManager.close(connection, preparedStatement, resultSet);
		}
		return false;
	}
}
