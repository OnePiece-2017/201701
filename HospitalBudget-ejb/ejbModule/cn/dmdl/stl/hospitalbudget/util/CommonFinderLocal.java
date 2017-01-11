package cn.dmdl.stl.hospitalbudget.util;

public interface CommonFinderLocal {

	public void remove();

	public void destroy();

	/** 获取类简介 */
	public String gainClassIntroduction();

	/** 获取项目流程类型名称 */
	public String gainProjectProcessTypeName(int type);

	/** 获取任务类型名称 */
	public String gainTaskTypeName(int type);

	/** 获取订单状态名称 */
	public String gainOrderStatusName(int type);

}
