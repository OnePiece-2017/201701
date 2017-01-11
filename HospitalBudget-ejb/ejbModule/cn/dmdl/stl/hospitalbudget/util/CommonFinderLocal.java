package cn.dmdl.stl.hospitalbudget.util;

public interface CommonFinderLocal {

	public void remove();

	public void destroy();

	/** 获取类简介 */
	public String gainClassIntroduction();

	/** 获取项目流程类型名称 */
	public String gainProjectProcessTypeName(int type);

}
