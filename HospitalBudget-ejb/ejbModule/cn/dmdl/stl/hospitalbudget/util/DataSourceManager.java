package cn.dmdl.stl.hospitalbudget.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import cn.dmdl.stl.hospitalbudget.boot.ConfigureCache;

/**
 * 数据源管理器
 */
public class DataSourceManager {

	private static Logger logger = Logger.getLogger(DataSourceManager.class);

	/** jdbc 默认 */
	public static final int BY_JDBC_DEFAULT = 1;

	/** Naming 默认 */
	public static final int BY_NAMING_DEFAULT = 2;

	/** Naming 远程 */
	public static final int BY_NAMING_REMOTE = 3;

	private static DataSource dataSourceNamingDefault = null;

	/** 打开 */
	public static Connection open(String key) {
		Connection connection = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			JSONObject connJson = ConfigureCache.pullDataSource(key);
			connection = DriverManager.getConnection(connJson.getString("url"), connJson.getString("username"), connJson.getString("password"));
		} catch (ClassNotFoundException e) {
			logger.error("找不到com.mysql.jdbc.Driver类", e);
		} catch (SQLException e) {
			logger.error("获取数据库连接失败", e);
		}
		return connection;
	}

	/** 打开 */
	public static Connection open(int by) {
		// TODO: 目前僅支持手動指定數據庫名稱
		// jdbc - com.mysql.jdbc.JDBC4PreparedStatement
		// naming - org.jboss.resource.adapter.jdbc.jdk6.WrappedPreparedStatementJDK6
		Connection connection = null;
		if (by == BY_JDBC_DEFAULT) {
			connection = open("hospital_budget");
		} else if (by == BY_NAMING_DEFAULT) {
			if (null == dataSourceNamingDefault) {
				try {
					Context context = new InitialContext();
					dataSourceNamingDefault = (DataSource) context.lookup("java:/HospitalBudgetDatasource");
				} catch (NamingException e) {
					logger.error("无效的JNDI name", e);
				}
			}
			try {
				connection = dataSourceNamingDefault.getConnection();
			} catch (SQLException e) {
				logger.error("open-->BY_NAMING_DEFAULT-->", e);
			}
		}
		return connection;
	}

	/** 关闭 */
	public static void close(Connection connection, PreparedStatement preparedStatement, ResultSet resultSet) {
		try {
			if (resultSet != null) {
				resultSet.close();
			}
			if (preparedStatement != null) {
				preparedStatement.close();
			}
			if (connection != null) {
				connection.close();
			}
		} catch (SQLException e) {
			logger.error("关闭资源失败", e);
		}
	}

	public static void setParams(PreparedStatement pstm, List<Object> params) throws SQLException {
		// TODO 注意版权争议问题
		for (int i = 0; i < params.size(); i++) {
			Object param = params.get(i);
			if (param instanceof Integer) {
				Integer data = (Integer) param;
				pstm.setInt(i + 1, data);
			} else if (param instanceof Long) {
				Long data = (Long) param;
				pstm.setLong(i + 1, data);
			} else if (param instanceof Float) {
				Float data = (Float) param;
				pstm.setFloat(i + 1, data);
			} else if (param instanceof Double) {
				Double data = (Double) param;
				pstm.setDouble(i + 1, data);
			} else if (param instanceof String) {
				String data = (String) param;
				pstm.setString(i + 1, data);
			} else if (param instanceof Date) {
				Date data = (Date) param;
				pstm.setTimestamp(i + 1, new Timestamp(data.getTime()));
			} else if (param instanceof Timestamp) {
				Timestamp data = (Timestamp) param;
				pstm.setTimestamp(i + 1, data);
			} else if (param instanceof java.sql.Date) {
				java.sql.Date data = (java.sql.Date) param;
				pstm.setDate(i + 1, data);
			} else {
				pstm.setObject(i + 1, param);
			}
		}
	}

}
