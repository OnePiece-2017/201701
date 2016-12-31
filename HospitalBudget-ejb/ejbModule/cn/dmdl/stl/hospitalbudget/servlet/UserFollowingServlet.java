package cn.dmdl.stl.hospitalbudget.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import cn.dmdl.stl.hospitalbudget.boot.ConfigureCache;

/**
 * Servlet implementation class RechargeServlet
 */
public class UserFollowingServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(UserFollowingServlet.class);

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO: 别忘记！防黑机制 判断更加严格 稍有不对就返回错误 拒绝登录
		JSONObject args = JSONObject.fromObject(request.getParameter("args"));
		logger.info("args->" + args);

		String userInfoIdMD5 = args.getString("userInfoIdMD5");
		Integer loginInfoId = ConfigureCache.userInfoIdMD5Map.get(userInfoIdMD5);

		String callback = request.getParameter("callback");
		JSONObject result = new JSONObject();

		result.accumulate("version", ConfigureCache.versionMap.get(loginInfoId));

		response.setCharacterEncoding("utf-8");
		response.setContentType("application/json; charset=utf-8");// RFC4627 - The application/json Media Type for JavaScript Object Notation (JSON) // JSON 文件的文件类型是 ".json" // JSON 文本的 MIME 类型是 "application/json"
		String temp = callback + "(" + result.toString() + ")";
		response.getWriter().print(temp.toString());
		response.getWriter().close();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
