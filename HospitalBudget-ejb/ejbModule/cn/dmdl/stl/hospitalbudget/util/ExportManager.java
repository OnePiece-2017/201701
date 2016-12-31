package cn.dmdl.stl.hospitalbudget.util;

import java.util.Calendar;

/**
 * 导出管理器
 */
public abstract class ExportManager {

	private String name;// 名称
	private String suffix;// 后缀
	private String filename;// 文件名

	/**
	 * 保存
	 * 
	 * @return 操作结果
	 */
	public abstract String save();

	/**
	 * 生成时间戳
	 * 
	 * @return 根据现在时刻生成一个时间戳
	 */
	private static String generateTimestamp() {
		Calendar rightNow = Calendar.getInstance();
		StringBuffer timestamp = new StringBuffer();
		timestamp.append(rightNow.get(Calendar.YEAR));
		timestamp.append(rightNow.get(Calendar.MONTH) + 1 > 9 ? rightNow.get(Calendar.MONTH) + 1 : "0" + (rightNow.get(Calendar.MONTH) + 1));
		timestamp.append(rightNow.get(Calendar.DATE) > 9 ? rightNow.get(Calendar.DATE) : "0" + rightNow.get(Calendar.DATE));
		timestamp.append(rightNow.get(Calendar.HOUR_OF_DAY) > 9 ? rightNow.get(Calendar.HOUR_OF_DAY) : "0" + rightNow.get(Calendar.HOUR_OF_DAY));
		timestamp.append(rightNow.get(Calendar.MINUTE) > 9 ? rightNow.get(Calendar.MINUTE) : "0" + rightNow.get(Calendar.MINUTE));
		timestamp.append(rightNow.get(Calendar.SECOND) > 9 ? rightNow.get(Calendar.SECOND) : "0" + rightNow.get(Calendar.SECOND));
		return timestamp.toString();
	}

	/**
	 * 获取名称
	 * 
	 * @return 旧的名称
	 */
	public String getName() {
		return name;
	}

	/**
	 * 设置名称
	 * 
	 * @param name
	 *            新的名称
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 获取后缀
	 * 
	 * @return 旧的后缀
	 */
	public String getSuffix() {
		return suffix;
	}

	/**
	 * 设置后缀
	 * 
	 * @param suffix
	 *            新的后缀
	 */
	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

	/**
	 * 获取文件名
	 * 
	 * @return 根据文件和后缀生成一个文件名
	 */
	public String getFilename() {
		filename = (name != null ? name : generateTimestamp()) + (suffix != null ? suffix : "_NoSuffix");
		return filename;
	}

}
