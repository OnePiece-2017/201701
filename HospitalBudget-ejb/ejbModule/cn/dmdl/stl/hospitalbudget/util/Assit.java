package cn.dmdl.stl.hospitalbudget.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;

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
	 * 包围内容
	 * 
	 * @param contents
	 *            内容，数组的每个元素都将被toString(),null则原样输出
	 * @return 被包围过的内容
	 */
	public static String surroundContents(Object... contents) {
		return surroundContentsAgile("[", "]", false, contents);
	}

	/**
	 * 包围内容（灵活）
	 * 
	 * @param leftShell
	 *            左侧外壳
	 * @param rightShell
	 *            右侧外壳
	 * @param reserveNull
	 *            是否保留null值
	 * @param contents
	 *            预处理内容
	 * @return 被包围过的内容
	 */
	public static String surroundContentsAgile(String leftShell, String rightShell, boolean reserveNull, Object... contents) {
		StringBuffer result = new StringBuffer();
		if (contents != null && contents.length > 0) {
			for (int i = 0; i < contents.length; i++) {
				if (!reserveNull || contents[i] != null) {
					result.append(leftShell).append(contents[i]).append(rightShell);
				} else if (reserveNull) {
					result.append(contents[i]);
				}
				if (i < contents.length - 1) {
					result.append(",");
				}
			}
		}
		return result.toString();
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

	/** 补齐字符串（自定义空缺） */
	public static String fillChar(Object source, short width, char c) {
		StringBuffer result = new StringBuffer("");
		if (source != null) {
			if (width > 0) {
				int vacancy = width - source.toString().length();
				while (vacancy-- > 0)
					result.append(String.valueOf(c));
			}
			result.append(source.toString());
		}
		return result.toString();
	}

	/** 补齐字符串（空缺0） */
	public static String fillZero(Object source, short width) {
		return fillChar(source, width, '0');
	}

	public static String explainTime(long milliseconds) {
		StringBuffer result = new StringBuffer();
		long modTemp = milliseconds;
		long divideTemp = milliseconds;
		long radixGroup[] = new long[] { 24 * 60 * 60 * 1000, 60 * 60 * 1000, 60 * 1000, 1000, 1 };
		String labelGroup[] = new String[] { "天", "时", "分", "秒", "毫秒" };
		for (int i = 0; i < radixGroup.length; i++) {
			modTemp %= radixGroup[i];
			divideTemp /= radixGroup[i];
			result.append(divideTemp).append(labelGroup[i]);
			divideTemp = modTemp;
		}
		return result.toString();
	}

	public static long generateMilliseconds(long day, long hour, long minute, long second, long millisecond) {
		return day * 24 * 60 * 60 * 1000 + hour * 60 * 60 * 1000 + minute * 60 * 1000 + second * 1000 + millisecond * 1;
	}

	public static String explainByte(long length) {
		StringBuffer result = new StringBuffer();
		long modTemp = length;
		long divideTemp = length;
		long radixGroup[] = new long[7];
		for (int i = radixGroup.length - 1; i > -1; i--) {
			long temp = 1L;
			for (int j = 0; j < i; j++)
				temp *= 1024L;
			radixGroup[radixGroup.length - i - 1] = temp;
		}
		String labelGroup[] = new String[] { /* "DB", "NB", "BB", "YB", "ZB", */"EB", "PB", "TB", "GB", "MB", "KB", "B" };
		for (int i = 0; i < radixGroup.length; i++) {
			modTemp %= radixGroup[i];
			divideTemp /= radixGroup[i];
			result.append(divideTemp).append(labelGroup[i]);
			divideTemp = modTemp;
		}
		return result.toString();
	}

	public static long generateByte(long eb, long pb, long tb, long gb, long mb, long kb, long b) {
		long total = 0L;
		long source[] = new long[] { eb, pb, tb, gb, mb, kb, b };
		for (int i = 0; i < source.length; i++) {
			long temp = source[i];
			for (int j = i; j < source.length - 1; j++)
				temp *= 1024L;
			total += temp;
		}
		return total;
	}
	
	/**
	 * 保留两位小数
	 * @param d
	 * @return
	 */
	public static String formatDouble2(double d) {
        DecimalFormat df = new DecimalFormat("#.00");
        return df.format(d);
    }

}