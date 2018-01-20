package cn.dmdl.stl.hospitalbudget.util;

import java.sql.Connection;
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

import org.apache.log4j.Logger;


public class OldBudgetJDBCUtil {
	private static Logger logger = Logger.getLogger(OldBudgetJDBCUtil.class);
	private static DataSource dataSource = null;

	public static Connection getConnection() {

		try {
			if (null == dataSource) {
				Context ctx = new InitialContext();
				dataSource = (DataSource) ctx.lookup("java:/OldBudgetDatasource");
			}
			return dataSource.getConnection();
		} catch (NamingException e) {
			logger.error("Create jdbc datasource fail!\n" + e.getMessage());
			return null;
		} catch (SQLException e) {
			logger.error("Create jdbc datasource fail!\n" + e.getMessage());
			return null;
		}
	}
	
	/**
	 * 关闭数据库资源
	 * @param conn Connection
	 * @param pstm PreparedStatement
	 * @param rs ResultSet
	 */
	public static void closeResource(Connection conn, PreparedStatement pstm, ResultSet rs){
		try {
			if (rs != null)
				rs.close();
			if (pstm != null)
				pstm.close();
			if (conn != null)
				conn.close();
		} catch (SQLException e) {
			logger.error("Close resource fail!\n" , e);
		}
	}
	
	public static void setParams(PreparedStatement pstm, List<Object> params) throws SQLException {
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
				if(data.isEmpty()){
					pstm.setObject(i + 1, null);
				}else{
					pstm.setString(i + 1, data);
				}
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
