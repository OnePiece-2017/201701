package cn.dmdl.stl.hospitalbudget.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.log4j.Logger;

/**
 * Servlet Filter implementation class LogFilter
 */
public class LogFilter implements Filter {

	private static Logger logger = Logger.getLogger(LogFilter.class);

	/**
	 * Default constructor.
	 */
	public LogFilter() {
		logger.info("LogFilter");
	}

	/**
	 * @see Filter#destroy()
	 */
	public void destroy() {
		logger.info("destroy");
	}

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		chain.doFilter(request, response);
	}

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {
		logger.info("init");
	}

}
