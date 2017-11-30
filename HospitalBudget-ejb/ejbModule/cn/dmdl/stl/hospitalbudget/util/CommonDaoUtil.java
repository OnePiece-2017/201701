package cn.dmdl.stl.hospitalbudget.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class CommonDaoUtil {
	
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

}
