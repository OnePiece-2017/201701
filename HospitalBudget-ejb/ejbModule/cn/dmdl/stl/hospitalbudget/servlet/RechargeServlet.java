package cn.dmdl.stl.hospitalbudget.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class RechargeServlet
 */
public class RechargeServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("request-->" + request.toString());
		System.out.println("response-->" + response.toString());
		System.out.println(request.getParameter("userInfoId"));
		System.out.println(request.getParameter("amountOfMoney"));
		response.setContentType("application/vnd.ms-excel");
		response.getWriter().write("success");
		// response.getWriter().write("充值成功，携带参数export=1可以导出excel充值记录！");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
