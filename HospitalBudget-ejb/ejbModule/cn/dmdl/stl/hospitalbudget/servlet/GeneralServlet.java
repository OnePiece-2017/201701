package cn.dmdl.stl.hospitalbudget.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import cn.dmdl.stl.hospitalbudget.util.Assit;
import cn.dmdl.stl.hospitalbudget.util.HttpServletInvokeFeatures;
import cn.dmdl.stl.hospitalbudget.util.HttpServletRule;

public class GeneralServlet extends HttpServlet implements HttpServletRule, HttpServletInvokeFeatures {

	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(GeneralServlet.class);
	/** 仓库数据 */
	public static Map<Object, Object> repositoryData = new HashMap<Object, Object>();

	/** 获取项目简介 */
	public static final String GAIN_PROJECT_PROFILE = "gain_project_profile";
	/** 推送仓库数据 */
	public static final String PUSH_REPOSITORY_DATA = "push_repository_data";
	/** 拉取仓库数据 */
	public static final String PULL_REPOSITORY_DATA = "pull_repository_data";
	/** 获取仓库数据 */
	public static final String FETCH_REPOSITORY_DATA = "fetch_repository_data";

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		JSONObject args = JSONObject.fromObject(request.getParameter("args"));
		String key = args.getString("key");
		String value = args.getString("value");
		logger.info("args->" + args);
		logger.info("key->" + key);
		logger.info("value->" + value);

		JSONObject result = new JSONObject();
		result.accumulate("invoke_result", INVOKE_SUCCESS);
		if (GAIN_PROJECT_PROFILE.equals(key.toLowerCase())) {
			gainProjectProfile(result, value);
		} else if (PUSH_REPOSITORY_DATA.equals(key.toLowerCase())) {
			pushRepositoryData(result, value);
		} else if (PULL_REPOSITORY_DATA.equals(key.toLowerCase())) {
			pullRepositoryData(result, value);
		} else if (FETCH_REPOSITORY_DATA.equals(key.toLowerCase())) {
			fetchRepositoryData(result, value);
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
		projectProfile.append("声明：愿……中国的软件产业蓬勃发展！").append("value[").append(value).append("]");// TODO: query
		if (projectProfile != null && !"".equals(projectProfile.toString())) {
			result.accumulate("data", projectProfile.toString());
		} else {
			result.element("invoke_result", INVOKE_FAILURE);
			result.element("invoke_message", "失败！获取项目简介");
			logger.error("gainProjectProfile");
		}
	}

	private void pushRepositoryData(JSONObject result, String value) {
		try {
			JSONObject valueJson = JSONObject.fromObject(value);
			final Object repositoryDataKey = valueJson.get("repositoryDataKey");
			final long validityDuration = valueJson.getLong("validityDuration");
			Object repositoryDataValue = valueJson.get("repositoryDataValue");
			logger.info("pushRepositoryData add-->" + Assit.wrapStr(repositoryDataKey, repositoryDataValue));
			repositoryData.put(repositoryDataKey, repositoryDataValue);
			new Thread(new Runnable() {
				public void run() {
					try {
						Thread.sleep(validityDuration);
						logger.info("pushRepositoryData del-->" + Assit.wrapStr(repositoryDataKey));
						GeneralServlet.repositoryData.remove(repositoryDataKey);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}).start();
		} catch (Exception e) {
			result.element("invoke_result", INVOKE_FAILURE);
			result.element("invoke_message", "推送仓库数据失败！");
			logger.error("pushRepositoryData-->" + e.getMessage());
		}
	}

	private void pullRepositoryData(JSONObject result, String value) {
		try {
			result.accumulate("data", repositoryData.get(value));
		} catch (Exception e) {
			result.element("invoke_result", INVOKE_FAILURE);
			result.element("invoke_message", "拉取仓库数据失败！");
			logger.error("pullRepositoryData-->" + e.getMessage());
		}
	}

	private void fetchRepositoryData(JSONObject result, String value) {
		try {
			JSONArray items = new JSONArray();
			for (Object key : repositoryData.keySet()) {
				JSONObject item = new JSONObject();
				item.accumulate(key.toString(), repositoryData.get(key));
				items.add(item);
			}
			result.accumulate("data", items);
		} catch (Exception e) {
			result.element("invoke_result", INVOKE_FAILURE);
			result.element("invoke_message", "获取仓库数据失败！");
			logger.error("fetchRepositoryData-->" + e.getMessage());
		}
	}

}
