package cn.dmdl.stl.hospitalbudget.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import cn.dmdl.stl.hospitalbudget.util.Assit;
import cn.dmdl.stl.hospitalbudget.util.DataSourceManager;
import cn.dmdl.stl.hospitalbudget.util.DateTimeHelper;
import cn.dmdl.stl.hospitalbudget.util.HttpServletInvokeFeatures;
import cn.dmdl.stl.hospitalbudget.util.HttpServletRule;
import cn.dmdl.stl.hospitalbudget.util.MD5;

public class FileUploadAssistServlet extends HttpServlet implements HttpServletRule, HttpServletInvokeFeatures {

	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(FileUploadAssistServlet.class);
	/** 获取最近上传 */
	public static final String GAIN_RECENTLY_UPLOADED = "gain_recently_uploaded";
	/** 获取指定上传 */
	public static final String GAIN_SPECIFIED_UPLOADED = "gain_specified_uploaded";
	/** 保存数据 */
	public static final String SAVE_DATA = "save_data";

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		JSONObject args = JSONObject.fromObject(request.getParameter("args"));
		String assistKey = args.getString("assist_key");
		String assistValue = args.getString("assist_value");
		logger.info("args->" + args);
		logger.info("assistKey->" + assistKey);
		logger.info("assistValue->" + assistValue);

		JSONObject result = new JSONObject();
		result.accumulate("invoke_result", INVOKE_SUCCESS);
		if (GAIN_RECENTLY_UPLOADED.equals(assistKey.toLowerCase())) {
			gainRecentlyUploaded(result, assistValue);
		} else if (GAIN_SPECIFIED_UPLOADED.equals(assistKey.toLowerCase())) {
			gainSpecifiedUploaded(result, assistValue);
		} else if (SAVE_DATA.equals(assistKey.toLowerCase())) {
			saveData(result, assistValue);
		} else {
			result.element("invoke_result", INVOKE_FAILURE);
			result.element("invoke_message", "失败！无效的assistKey");
			logger.error("失败！无效的assistKey");
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

	private synchronized void saveData(JSONObject result, String assistValue) {
		Connection connection = DataSourceManager.open(DataSourceManager.BY_JDBC_DEFAULT);
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			if (assistValue != null && !"".equals(assistValue)) {
				JSONObject assistValueJson = JSONObject.fromObject(assistValue);
				System.out.println(assistValueJson);
				JSONArray items = assistValueJson.getJSONArray("items");
				boolean rebuild = assistValueJson.getBoolean("rebuild");
				String source = assistValueJson.getString("source");
				if (!rebuild) {
					preparedStatement = connection.prepareStatement("delete from file_upload_information where binary the_key = '" + source + "'");
					preparedStatement.executeUpdate();
				}
				// batch insert
				if (items != null && items.size() > 0) {
					for (Object object : items) {
						System.out.println("object-->" + object);
						JSONObject dataRow = JSONObject.fromObject(object);
						System.out.println(dataRow);
						StringBuffer insertSql = new StringBuffer("insert into file_upload_information (the_key, the_value, insert_time, insert_user)");
						insertSql.append(" values ('").append(source).append("', '").append(dataRow.toString()).append("', '").append(DateTimeHelper.dateToStr(new Date(), DateTimeHelper.PATTERN_DATE_TIME)).append("', null").append(")");
						preparedStatement = connection.prepareStatement(insertSql.toString());
						preparedStatement.executeUpdate();
					}
				}
			}
		} catch (Exception e) {
			result.element("invoke_result", INVOKE_FAILURE);
			result.element("invoke_message", "失败！保存数据");
			logger.error("gainRecentlyUploaded", e);
		} finally {
			DataSourceManager.close(connection, preparedStatement, resultSet);
		}
	}

	private synchronized void gainSpecifiedUploaded(JSONObject result, String assistValue) {
		Connection connection = DataSourceManager.open(DataSourceManager.BY_JDBC_DEFAULT);
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			JSONObject data = new JSONObject();
			JSONArray items = new JSONArray();
			if (Assit.getResultSetSize("select the_id from file_upload_information where binary the_key = '" + (assistValue != null ? assistValue : "") + "'") > 0) {
				data.accumulate("source", assistValue);
				data.accumulate("rebuild", false);
				preparedStatement = connection.prepareStatement("select the_value from file_upload_information where binary the_key = '" + (assistValue != null ? assistValue : "") + "' and the_value <> ''");
				resultSet = preparedStatement.executeQuery();
				while (resultSet.next()) {
					items.add(JSONObject.fromObject(resultSet.getString(1)));
				}
			} else {
				preparedStatement = connection.prepareStatement("select ifnull(max(the_id), 0) + 1 as new_the_id from file_upload_information");
				resultSet = preparedStatement.executeQuery();
				int theId = 0;
				String theKey = null;
				if (resultSet.next()) {
					theId = resultSet.getInt(1);
					theKey = MD5.getMD5Alpha(theId);
				}
				if (theKey != null && !"".equals(theKey)) {
					StringBuffer insertSql = new StringBuffer("insert into file_upload_information (the_id, the_key, the_value, insert_time, insert_user)");
					insertSql.append(" values ('").append(theId).append("', '").append(theKey).append("', '', '").append(DateTimeHelper.dateToStr(new Date(), DateTimeHelper.PATTERN_DATE_TIME)).append("', null").append(")");
					preparedStatement = connection.prepareStatement(insertSql.toString());
					preparedStatement.executeUpdate();
					data.accumulate("source", theKey);
					data.accumulate("rebuild", true);
				} else {
					result.element("invoke_result", INVOKE_FAILURE);
					result.element("invoke_message", "生成the_key失败！");
					logger.error("生成the_key失败！");
				}
			}
			data.accumulate("items", items);
			result.accumulate("data", data);
		} catch (Exception e) {
			result.element("invoke_result", INVOKE_FAILURE);
			result.element("invoke_message", "失败！获取指定上传");
			logger.error("gainRecentlyUploaded", e);
		} finally {
			DataSourceManager.close(connection, preparedStatement, resultSet);
		}
	}

	private void gainRecentlyUploaded(JSONObject result, String assistValue) {
		Connection connection = DataSourceManager.open(DataSourceManager.BY_JDBC_DEFAULT);
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			String theValue = null;
			preparedStatement = connection.prepareStatement("select the_value from file_upload_history where binary the_key = '" + assistValue + "'");
			resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				theValue = resultSet.getString(1);
			}
			if (theValue != null && !"".equals(theValue)) {
				JSONObject theValueJson = JSONObject.fromObject(theValue);
				if (INVOKE_SUCCESS.equals(theValueJson.getString("invoke_result"))) {
					JSONArray data = theValueJson.getJSONArray("data");
					result.accumulate("data", data);
					System.out.println("data>>" + data.toString());
				} else {
					result.element("invoke_result", INVOKE_FAILURE);
					result.element("invoke_message", "失败！无效的key");
					logger.error("无效的key");
				}
			} else {
				result.element("invoke_result", INVOKE_FAILURE);
				result.element("invoke_message", "失败！无效的key");
				logger.error("无效的key");
			}
		} catch (SQLException e) {
			result.element("invoke_result", INVOKE_FAILURE);
			result.element("invoke_message", "失败！获取最近上传");
			logger.error("gainRecentlyUploaded", e);
		} finally {
			DataSourceManager.close(connection, preparedStatement, resultSet);
		}
	}

}
