package cn.dmdl.stl.hospitalbudget.servlet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase.FileSizeLimitExceededException;
import org.apache.commons.fileupload.FileUploadBase.SizeLimitExceededException;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;

import cn.dmdl.stl.hospitalbudget.boot.ConfigureCache;
import cn.dmdl.stl.hospitalbudget.util.Assit;
import cn.dmdl.stl.hospitalbudget.util.DataSourceManager;
import cn.dmdl.stl.hospitalbudget.util.DateTimeHelper;
import cn.dmdl.stl.hospitalbudget.util.HttpServletInvokeFeatures;
import cn.dmdl.stl.hospitalbudget.util.HttpServletRule;
import cn.dmdl.stl.hospitalbudget.util.MD5;

public class FileUploadServlet extends HttpServlet implements HttpServletRule, HttpServletInvokeFeatures {

	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(FileUploadServlet.class);
	public static final int MIN_SIZE_THRESHOLD = 0;
	public static final int MAX_SIZE_THRESHOLD = 1024 * 1024 * 10;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		JSONObject result = new JSONObject();
		result.accumulate("invoke_result", INVOKE_SUCCESS);
		boolean isMultipart = ServletFileUpload.isMultipartContent(request);// 检查是否有文件上传请求
		if (isMultipart) {
			int sizeThreshold = DiskFileItemFactory.DEFAULT_SIZE_THRESHOLD;// simple form field或uploaded file字节数大于阈值时放repository 否则放内存
			Integer sizeThresholdUsr = Integer.parseInt(ConfigureCache.getValue("fileupload.size_threshold"));
			if (sizeThresholdUsr != null && sizeThresholdUsr >= MIN_SIZE_THRESHOLD && sizeThresholdUsr <= MAX_SIZE_THRESHOLD)
				sizeThreshold = sizeThresholdUsr;
			File repository = null;// 临时文件目录
			String tmpdirUsr = ConfigureCache.getValue("fileupload.repository");
			String tmpdirDef = System.getProperty("java.io.tmpdir");
			if (tmpdirUsr != null && !"".equals(tmpdirUsr))
				repository = new File(tmpdirUsr);
			if ((null == repository || repository.isFile() || (!repository.isFile() && !repository.isDirectory())) && tmpdirDef != null && !"".equals(tmpdirDef))
				repository = new File(tmpdirDef);
			if (repository != null && repository.isDirectory()) {
				DiskFileItemFactory factory = new DiskFileItemFactory(sizeThreshold, repository);
				ServletFileUpload upload = new ServletFileUpload(factory);
				long fileSizeMax = Long.parseLong(ConfigureCache.getValue("fileupload.maximum_size_of_a_single_uploaded_file"));// 单个文件上传阈值
				fileSizeMax = 1024L * 1024 * 1024;// TODO: 仅供测试
				upload.setFileSizeMax(fileSizeMax);
				long sizeMax = Long.parseLong(ConfigureCache.getValue("fileupload.maximum_allowed_size_of_a_complete_request"));// 整个请求阈值
				sizeMax = 1024L * 1024 * 1024;// TODO: 仅供测试
				upload.setSizeMax(sizeMax);
				try {
					List<FileItem> items = upload.parseRequest(request);// 解析请求
					JSONArray info = new JSONArray();// 信息（主要指规则的表单字段）
					JSONArray data = new JSONArray();// 数据（主要指文件上传）
					// 处理已上传的项目
					Iterator iter = items.iterator();
					while (iter.hasNext()) {
						FileItem item = (FileItem) iter.next();
						if (item.isFormField())
							info.add(processFormField(item));
						else
							data.add(processUploadedFile(item));
					}
					result.accumulate("info", info);
					result.accumulate("data", data);
				} catch (FileSizeLimitExceededException e) {
					result.element("invoke_result", INVOKE_FAILURE);
					result.element("invoke_message", "单个文件上传超出阈值！" + upload.getFileSizeMax() + "字节");
					logger.error("单个文件上传超出阈值！" + upload.getFileSizeMax() + "字节");
				} catch (SizeLimitExceededException e) {
					result.element("invoke_result", INVOKE_FAILURE);
					result.element("invoke_message", "整个请求超出阈值！" + upload.getSizeMax() + "字节");
					logger.error("整个请求超出阈值！" + upload.getSizeMax() + "字节");
				} catch (FileUploadException e) {
					result.element("invoke_result", INVOKE_FAILURE);
					result.element("invoke_message", "文件上传失败！");
					logger.error("文件上传失败！" + e.getMessage());
				}
			} else {
				result.element("invoke_result", INVOKE_FAILURE);
				result.element("invoke_message", "无法定位临时文件夹！");
				logger.error("无法定位临时文件夹！");
			}
		} else {
			result.element("invoke_result", INVOKE_FAILURE);
			result.element("invoke_message", "无文件上传请求！");
			logger.error("无文件上传请求！");
		}

		String contextRoot = ConfigureCache.getValue("context.root");
		String code = "";
		if (INVOKE_SUCCESS.equals(result.getString("invoke_result"))) {// TODO: 请求失败是否需要插入数据库——待定
			synchronized (this) {
				Connection connection = DataSourceManager.open(DataSourceManager.BY_JDBC_DEFAULT);
				PreparedStatement preparedStatement = null;
				ResultSet resultSet = null;
				try {
					int newTheId = 0;
					preparedStatement = connection.prepareStatement("select ifnull(max(the_id), 0) + 1 as new_the_id from file_upload_history");
					resultSet = preparedStatement.executeQuery();
					if (resultSet.next()) {
						newTheId = resultSet.getInt(1);
						code = MD5.getMD5Alpha(newTheId);
					}
					Integer userInfoId = null;
					JSONArray info = result.getJSONArray("info");
					if (info != null && info.size() > 0)
						for (int i = 0; i < info.size(); i++) {
							JSONObject eJson = info.getJSONObject(i);
							if ("clientData".equals(eJson.getString("name"))) {
								JSONObject valueJson = eJson.getJSONObject("value");
								if (valueJson != null)
									if (valueJson.getString("userInfoId") != null && !"".equals(valueJson.getString("userInfoId")))
										userInfoId = Integer.valueOf(valueJson.getString("userInfoId"));
								break;
							}
						}
					StringBuffer insertSql = new StringBuffer("insert into file_upload_history (the_id, the_key, the_value, insert_time, insert_user)");
					insertSql.append(" values ('").append(newTheId).append("', '").append(code).append("', '").append(result.toString()).append("', '").append(DateTimeHelper.dateToStr(new Date(), DateTimeHelper.PATTERN_DATE_TIME));
					insertSql.append("', ").append(userInfoId != null ? "'" + userInfoId + "'" : null).append(")");
					preparedStatement = connection.prepareStatement(insertSql.toString());
					preparedStatement.executeUpdate();
				} catch (SQLException e) {
					logger.error("FileUploadServlet", e);
				} finally {
					DataSourceManager.close(connection, preparedStatement, resultSet);
				}
			}
		} else {
			// TODO
		}
		String redirect = (!"/".equals(contextRoot) ? contextRoot : "") + "/sg-fileupload/page/Model.seam?invoke_result=" + result.getString("invoke_result") + "&code=" + code;
		response.sendRedirect(redirect);
	}

	// 处理一个规则的表单字段
	private JSONObject processFormField(FileItem item) {
		logger.info("processFormField-->start");
		JSONObject result = null;
		String name = item.getFieldName();
		if (name != null && !"".equals(name)) {
			result = new JSONObject();
			result.accumulate("name", name);
			String value = null;
			try {
				value = item.getString("UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			result.accumulate("value", value);
		}
		logger.info("processFormField-->stop");
		return result;
	}

	// 处理一个文件上传
	private JSONObject processUploadedFile(FileItem item) {
		// TODO: 代码还没写完，继续写吧try的地方
		logger.info("processUploadedFile-->start");
		JSONObject result = null;
		if (item != null && item.getName() != null && !"".equals(item.getName())) {// 忽略无真实文件的表单请求（未选择文件提交表单时文件名为空）
			result = new JSONObject();
			String fileName = item.getName();
			result.accumulate("name", fileName);
			result.accumulate("type", item.getContentType());
			result.accumulate("size", item.getSize());
			result.accumulate("icon", null);
			try {
				InputStream inputStream = item.getInputStream();
				String md5Hex = DigestUtils.md5Hex(inputStream);
				inputStream.close();
				result.accumulate("md5Hex", md5Hex);
				synchronized (this) {
					if (0 == Assit.getResultSetSize("select the_id from file_upload_storage where binary the_key = '" + md5Hex + "'")) {
						File uploadedFile = new File(ConfigureCache.getValue("storage.path.root") + md5Hex);
						item.write(uploadedFile);
						Connection connection = DataSourceManager.open(DataSourceManager.BY_JDBC_DEFAULT);
						PreparedStatement preparedStatement = null;
						ResultSet resultSet = null;
						try {
							StringBuffer insertSql = new StringBuffer("insert into file_upload_storage (the_key, the_value, insert_time, insert_user)");
							insertSql.append(" values ('").append(md5Hex).append("', '").append(result.toString()).append("', '").append(DateTimeHelper.dateToStr(new Date(), DateTimeHelper.PATTERN_DATE_TIME)).append("', null").append(")");
							preparedStatement = connection.prepareStatement(insertSql.toString());
							preparedStatement.executeUpdate();
						} catch (Exception e) {
							result.element("invoke_result", INVOKE_FAILURE);
							result.element("invoke_message", "插入存储表失败！");
							logger.error("processUploadedFile", e);
						} finally {
							DataSourceManager.close(connection, preparedStatement, resultSet);
						}
					}
					item.delete();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			result.accumulate("url", ConfigureCache.getValue("storage.url.root") + fileName);
		}
		logger.info("processUploadedFile-->stop");
		return result;
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
