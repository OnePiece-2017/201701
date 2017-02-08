package cn.dmdl.stl.hospitalbudget.util;

public abstract interface HttpServletInvokeFeatures {

	/** 调用失败 */
	public static final String INVOKE_FAILURE = "INVOKE_FAILURE";

	/** 调用成功 */
	public static final String INVOKE_SUCCESS = "INVOKE_SUCCESS";

	/** 调用超时 */
	public static final String INVOKE_TIMEOUT = "INVOKE_TIMEOUT";

}
