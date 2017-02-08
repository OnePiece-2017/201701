package cn.dmdl.stl.hospitalbudget.util;

public abstract interface HttpServletRule {

	// ContentType

	/**
	 * <pre>
	 * RFC4627 - The application/json Media Type for JavaScript Object Notation (JSON)
	 * JSON 文件的文件类型是 ".json"
	 * JSON 文本的 MIME 类型是 "application/json"
	 */
	public static final String CONTENT_TYPE_JSON = "application/json; charset=utf-8";

	/** excel */
	public static final String CONTENT_TYPE_EXCEL = "application/vnd.ms-excel";

	/** html */
	public static final String CONTENT_TYPE_HTML = "text/html;charset=UTF-8";

	// ...

}
