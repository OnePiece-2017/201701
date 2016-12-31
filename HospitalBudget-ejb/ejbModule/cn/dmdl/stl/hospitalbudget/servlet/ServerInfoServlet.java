package cn.dmdl.stl.hospitalbudget.servlet;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * Servlet implementation class ServerInfoServlet
 */
public class ServerInfoServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(ServerInfoServlet.class);

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		int type = 0;
		try {
			type = Integer.parseInt(request.getParameter("type"));
		} catch (Exception e) {
		}

		final int TYPE_REMOTE_CLOCK = 1;// 遠程時鐘
		final int TYPE_OS_INFO = 2;// 系统信息

		// 该servlet可能来自不同类型的请求，响应也会有所不同。
		if (type == TYPE_REMOTE_CLOCK) {
			response.setContentType("text/html;charset=UTF-8");
			response.getWriter().print(String.valueOf(new Date().getTime()));
			response.getWriter().close();
		} else if (type == TYPE_OS_INFO) {
			logger.info("暂不支持！");
		} else {
			logger.info("无返回！");
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
