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

}
