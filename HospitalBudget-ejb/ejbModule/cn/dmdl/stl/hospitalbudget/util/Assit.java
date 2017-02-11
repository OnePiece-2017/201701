package cn.dmdl.stl.hospitalbudget.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

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
		StringBuffer result = new StringBuffer();
		if (contents != null && contents.length > 0)
			for (int i = 0; i < contents.length; i++) {
				result.append("[").append(contents[i]).append("]");
				if (i < contents.length - 1)
					result.append(",");
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

	/**
	 * 创建指定字节数的文件，不同于org.apache.commons.io.FileUtils.touch(File)
	 * 
	 * <pre>
	 * 10G大约需要50s
	 * 
	 * <pre>
	 * String pathname = &quot;D:/.temp/newFile/test.txt&quot;;
	 * new File(pathname).delete();
	 * newFile(pathname, 1024L * 1024 * 1024 * 10);
	 */
	public static void newFile(String pathname, long length) {
		System.out.println("准备创建大小为" + 0 + "GB?（" + length + "字节）的文件！");
		long maxLength = 1024L * 1024 * 1024 * 10;
		Date beginDate = new Date();
		System.out.println("开始时间：" + DateTimeHelper.dateToStr(beginDate, DateTimeHelper.PATTERN_DATE_TIME_FULL));
		String result = null;
		if (pathname != null && !"".equals(pathname))
			if (length > -1 && length <= maxLength)
				try {
					File file = new File(pathname);
					if (file.exists())
						result = file.isFile() ? "文件已存在！" : "文件夹已存在！";
					else if (!file.createNewFile())
						result = "创建文件失败！";
					else {
						FileOutputStream fileOutputStream = new FileOutputStream(file);
						BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
						byte[][] bufferGroup = new byte[20][];
						for (int i = bufferGroup.length - 1; i > -1; i--)
							bufferGroup[i] = new byte[1024 * (i + 1)];
						long writed = 0, remaining = length;
						int bufferIndex = bufferGroup.length - 1;
						while (remaining > 0) {
							for (int i = bufferIndex; i > -1; i--)
								if (remaining >= bufferGroup[i].length) {
									bufferIndex = i;
									break;
								} else if (0 == i) {
									bufferIndex = 0;
									bufferGroup[0] = new byte[(int) remaining];
								}
							bufferedOutputStream.write(bufferGroup[bufferIndex]);
							writed += bufferGroup[bufferIndex].length;
							remaining = length - writed;
						}
						bufferedOutputStream.close();
						fileOutputStream.close();
					}
				} catch (Exception e) {
					result = e.getMessage();
					e.printStackTrace();
				}
			else
				result = "字节数为0到" + maxLength + "";
		else
			result = "无效的文件路径！";
		Date endDate = new Date();
		System.out.println("结束时间：" + DateTimeHelper.dateToStr(endDate, DateTimeHelper.PATTERN_DATE_TIME_FULL));
		System.out.println(result != null ? result : "恭喜你！创建文件成功！");
		System.out.println("耗时：" + (endDate.getTime() - beginDate.getTime()) + "毫秒");
		File file = new File(pathname);
		if (null == result && (!file.exists() || !file.isFile() || file.length() != length))
			System.out.println("啊哦！文件异常！");
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

	public static void main(String[] args) {
		String pathname = "D:/.trash/256M.txt";
		new File(pathname).delete();
		newFile(pathname, 1024L * 1024 * 256);

		System.out.println(generateMilliseconds(30, 23, 59, 59, 999));
		System.out.println(explainTime(generateMilliseconds(30, 23, 59, 59, 999)));

		System.out.println(generateByte(0, 1023, 1023, 1023, 1023, 1023, 1023));
		System.out.println(explainByte(generateByte(0, 1023, 1023, 1023, 1023, 1023, 1023)));
	}

}