package cn.dmdl.stl.hospitalbudget.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import cn.dmdl.stl.hospitalbudget.util.Helper;
import cn.dmdl.stl.hospitalbudget.util.HttpServletInvokeFeatures;
import cn.dmdl.stl.hospitalbudget.util.HttpServletRule;

public class GeneralServlet extends HttpServlet implements HttpServletRule, HttpServletInvokeFeatures {

	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(GeneralServlet.class);
	/** 仓库数据 */
	public static Map<Object, Object> repositoryData = new HashMap<Object, Object>();

	/** 获取项目简介 */
	public static final String GAIN_PROJECT_PROFILE = "gain_project_profile";
	/** 获取知识库数据 */
	public static final String GAIN_REPOSITORY_DATA = "gain_repository_data";

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		JSONObject args = JSONObject.fromObject(request.getParameter("args"));
		String key = args.getString("key");
		String value = args.getString("value");

		JSONObject result = new JSONObject();
		result.accumulate("invoke_result", INVOKE_SUCCESS);
		if (GAIN_PROJECT_PROFILE.equals(key.toLowerCase())) {
			gainProjectProfile(result, value);
		} else if (GAIN_REPOSITORY_DATA.equals(key.toLowerCase())) {
			gainRepositoryData(result, value);
		} else {
			result.element("invoke_result", INVOKE_FAILURE);
			result.element("invoke_message", "失败！无效的key");
			logger.error("失败！无效的key");
		}

		response.setContentType(CONTENT_TYPE_JSON);
		String callback = request.getParameter("callback");
		StringBuffer printStr = new StringBuffer(result.toString());
		if (callback != null && !"".equals(callback)) {
			printStr.insert(0, "(").append(")").insert(0, callback);
		}
		response.getWriter().print(printStr.toString());
		response.getWriter().flush();
		response.getWriter().close();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	private void gainProjectProfile(JSONObject result, String value) {
		StringBuffer projectProfile = new StringBuffer();
		projectProfile.append("声明：愿……中国的软件产业蓬勃发展！").append("value[").append(value).append("]");
		if (projectProfile != null && !"".equals(projectProfile.toString())) {
			result.accumulate("data", projectProfile.toString());
		} else {
			result.element("invoke_result", INVOKE_FAILURE);
			result.element("invoke_message", "失败！获取项目简介");
			logger.error("gainProjectProfile");
		}
	}

	private void gainRepositoryData(JSONObject result, String value) {
		if (value != null && !"".equals(value)) {
			result.accumulate("data", Helper.getInstance().grabRepositoryData(Integer.valueOf(value)));
		} else {
			result.element("invoke_result", INVOKE_FAILURE);
			result.element("invoke_message", "获取知识库数据失败！");
			logger.error("获取知识库数据失败！");
		}
	}

}
