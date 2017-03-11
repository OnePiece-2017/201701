package cn.dmdl.stl.hospitalbudget.util;

import java.util.List;

import cn.dmdl.stl.hospitalbudget.admin.entity.Dictionary;

public interface CommonToolLocal {

	public void remove();

	public void destroy();

	public String abbr(Object source, int length);

	public List<Dictionary> getDictionaryByPid(int pid);

	public String getDictionaryValueByIds(String ids);

	public Object[] genA4jRepeatValue(int length);

	public List<?> sqlQuery(String sql);

	public List<?> hqlQuery(String hql);

}
