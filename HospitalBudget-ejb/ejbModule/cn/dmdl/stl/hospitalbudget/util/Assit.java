package cn.dmdl.stl.hospitalbudget.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.mysql.jdbc.JDBC4PreparedStatement;

public class Assit {

	private static Logger logger = Logger.getLogger(Assit.class);

	/** 获取ResultSet总记录数 */
	public static int getResultSetSize(ResultSet resultSet) {
		int size = 0;
		if (resultSet != null) {
			try {
				if (!resultSet.isClosed() && resultSet.getType() == ResultSet.TYPE_FORWARD_ONLY) {
					boolean isBeforeFirst = resultSet.isBeforeFirst();
					boolean isAfterLast = resultSet.isAfterLast();
					int currentRowNumber = 0;
					if (!isAfterLast && !isBeforeFirst) {
						currentRowNumber = resultSet.getRow();
					}
					resultSet.last();
					int totalRow = resultSet.getRow();
					if (isBeforeFirst) {
						resultSet.beforeFirst();
					} else if (isAfterLast) {
						resultSet.afterLast();
					} else {
						resultSet.absolute(currentRowNumber);
					}
					size = totalRow;
				}
			} catch (SQLException e) {
				logger.error("getResultSetSize(ResultSet resultSet)-->", e);
			}
		}
		return size;
	}

	/** 获取ResultSet总记录数 */
	public static int getResultSetSize(String sql) {
		// TODO: preparedStatement.getMaxRows()
		int size = 0;
		if (sql != null && !"".equals(sql)) {
			Connection connection = DataSourceManager.open(DataSourceManager.BY_JDBC_DEFAULT);
			PreparedStatement preparedStatement = null;
			ResultSet resultSet = null;
			try {
				preparedStatement = connection.prepareStatement(sql);
				resultSet = preparedStatement.executeQuery();
				size = getResultSetSize(resultSet);
			} catch (SQLException e) {
				logger.error("getResultSetSize(String sql)-->", e);
			} finally {
				DataSourceManager.close(connection, preparedStatement, resultSet);
			}
		}
		return size;
	}

	/**
	 * 拼接字符串
	 * 
	 * @param separator
	 *            分隔符
	 * @param contents
	 *            内容，数组的每个元素都将被toString(),null则原样输出
	 * @return 拼接后的字符串
	 */
	public static String joinToStr(Object separator, Object... contents) {
		String result = "";
		if (contents != null && contents.length > 0) {
			for (int i = 0; i < contents.length; i++) {
				result += (contents[i] != null ? contents[i].toString() : null);
				if (i < contents.length - 1) {
					result += separator != null ? separator : "";
				}
			}
		}
		return result;
	}

	/**
	 * 包裹字符串
	 * 
	 * @param contents
	 *            内容，数组的每个元素都将被toString(),null则原样输出
	 * @return 包裹后的字符串
	 */
	public static String wrapStr(Object... contents) {
		String result = "";
		if (contents != null && contents.length > 0) {
			for (int i = 0; i < contents.length; i++) {
				result += "[" + (contents[i] != null ? contents[i].toString() : null) + "]";
				if (i < contents.length - 1) {
					result += ",";
				}
			}
		}
		return result;
	}

	/** 获取预处理前sql */
	public static String getSqlBeforePrepared(PreparedStatement preparedStatement) {
		if (preparedStatement != null) {
			return ((JDBC4PreparedStatement) preparedStatement).getPreparedSql();
		}
		return null;
	}

	/** 获取预处理后sql */
	public static String getSqlAfterPrepared(PreparedStatement preparedStatement) {
		if (preparedStatement != null) {
			return preparedStatement.toString().substring(preparedStatement.toString().indexOf(":") + 2);
		}
		return null;
	}

}