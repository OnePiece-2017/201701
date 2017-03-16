package cn.dmdl.stl.hospitalbudget.util;

import java.util.List;

import cn.dmdl.stl.hospitalbudget.admin.entity.Dictionary;

/**
 * 常用工具
 * <p>
 * 范例:
 * 
 * <pre>
 * <code>@In(create = true)
 * private CommonToolLocal commonTool;
 * public void wire() {
 * 	commonTool.toString();
 * }</code>
 * </pre>
 */

public interface CommonToolLocal {

	public void remove();

	public void destroy();

	/**
	 * 缩写
	 * 
	 * @param source
	 * @param length
	 * @return
	 */
	public String abbr(Object source, int length);

	/**
	 * 根据父id获取字典列表
	 * 
	 * @param pid
	 * @return
	 */
	public List<Dictionary> getDictionaryByPid(int pid);

	/**
	 * 根据ids获取字典值
	 * 
	 * @param ids
	 * @return
	 */
	public String getDictionaryValueByIds(String ids);

	/**
	 * generate http://richfaces.org/a4j a:repeat value
	 * 
	 * @param length
	 * @return
	 */
	public Object[] genA4jRepeatValue(int length);

	/**
	 * sql查询
	 * 
	 * @param sql
	 * @return
	 */
	public List<?> sqlQuery(String sql);

	/**
	 * hql查询
	 * 
	 * @param hql
	 * @return
	 */
	public List<?> hqlQuery(String hql);

	/**
	 * 插入中间表数据
	 * 
	 * @param table
	 *            表名称
	 * @param columns
	 *            列1, 列2,...
	 * @param values
	 *            值1, 值2,....
	 * @return 成功或失败
	 */
	public boolean insertIntermediate(String table, String[] columns, Object[] values);

	/**
	 * 插入中间表数据（批量）
	 * 
	 * @param table
	 *            表名称
	 * @param columns
	 *            列1, 列2,...
	 * @param values
	 *            List的值1, 值2,....
	 * @return 成功或失败
	 */
	/**
	 * 
	 * @param table
	 * @param columns
	 * @param values
	 * @return
	 */
	public boolean insertIntermediateBatch(String table, String[] columns, List<Object[]> valuesList);

	/**
	 * 删除中间表数据
	 * 
	 * @param table
	 *            表名称
	 * @param where
	 *            标准
	 * @return 成功或失败
	 */
	public boolean deleteIntermediate(String table, String where);

	/**
	 * 
	 * @param table
	 *            表名称
	 * @param columns
	 *            列1, 列2,...
	 * @param where
	 *            标准
	 * @return
	 */
	public List<Object[]> selectIntermediate(String table, String[] columns, String where);

	/**
	 * 选择中间表数据（ids）
	 * 
	 * @param table
	 *            表名称
	 * @param column
	 *            列名称
	 * @param where
	 *            标准
	 * @return 以逗号分隔的值
	 */
	public String selectIntermediateAsIds(String table, String column, String where);

	/**
	 * 选择中间表数据（ids），可指定分隔符
	 * 
	 * @param table
	 *            表名称
	 * @param column
	 *            列名称
	 * @param where
	 *            标准
	 * @param separator
	 *            值的分隔符
	 * @return 以指定分隔符分隔的值
	 */
	public String selectIntermediateAsIds(String table, String column, String where, String separator);

}
