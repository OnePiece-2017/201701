package cn.dmdl.stl.hospitalbudget.servlet;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import cn.dmdl.stl.hospitalbudget.boot.ConfigureCache;
import cn.dmdl.stl.hospitalbudget.util.Assit;
import cn.dmdl.stl.hospitalbudget.util.Helper;
import cn.dmdl.stl.hospitalbudget.util.HttpServletInvokeFeatures;
import cn.dmdl.stl.hospitalbudget.util.HttpServletRule;

public class FileDownloadServlet extends HttpServlet implements HttpServletRule, HttpServletInvokeFeatures {

	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(FileDownloadServlet.class);

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		JSONObject args = JSONObject.fromObject(request.getParameter("args"));
		System.out.println(args);
		String name = args.getString("name");
		// String type = args.getString("type");
		// String size = args.getString("size");
		String md5Hex = args.getString("md5Hex");
		if (md5Hex != null && !"".equals(md5Hex)) {
			File file = new File(ConfigureCache.getValue("storage.path.root") + md5Hex);
			if (file != null && file.exists() && file.isFile()) {
				int performance = 0;// 下载模式切换（性能递增）
				for (int i = 0; i < 2; i++)
					if (i > 0)
						performance = 2;
				if (1 == performance) {
					Helper helper = Helper.getInstance();
					Integer timeConsumingKey = helper.installTimeConsuming();
					logger.info("启动耗时统计" + Assit.wrapStr("timeConsumingKey", timeConsumingKey));
					logger.info("开始下载[低速][" + file.getPath() + "]");
					response.reset();
					response.addHeader("Content-Disposition", "attachment;filename=" + new String(("\"" + name + "\"").getBytes(), "iso-8859-1"));// 允许文件名包含空格
					response.addHeader("Content-Length", "" + file.length());
					FileInputStream fileInputStream = new FileInputStream(file);
					BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
					byte[] readBuffer = new byte[1024];
					int readLength = 0;
					OutputStream outputStream = response.getOutputStream();
					while ((readLength = bufferedInputStream.read(readBuffer)) > 0)
						outputStream.write(readBuffer, 0, readLength);
					bufferedInputStream.close();
					outputStream.close();
					logger.info("结束下载[低速][" + file.getPath() + "]");
					logger.info("停止耗时统计" + Assit.wrapStr("timeConsumingKey", timeConsumingKey, helper.uninstallTimeConsuming(timeConsumingKey)));
				} else if (2 == performance) {
					Helper helper = Helper.getInstance();
					Integer timeConsumingKey = helper.installTimeConsuming();
					logger.info("启动耗时统计" + Assit.wrapStr("timeConsumingKey", timeConsumingKey));
					logger.info("开始下载[高速][" + file.getPath() + "]");
					response.reset();
					response.addHeader("Content-Disposition", "attachment;filename=" + new String(("\"" + name + "\"").getBytes(), "iso-8859-1"));// 允许文件名包含空格
					response.addHeader("Content-Length", "" + file.length());
					FileInputStream fileInputStream = new FileInputStream(file);
					BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
					BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(response.getOutputStream());
					byte[][] bufferGroup = new byte[20][];
					for (int i = bufferGroup.length - 1; i > -1; i--)
						bufferGroup[i] = new byte[1024 * (i + 1)];
					int bufferIndex = bufferGroup.length - 1;
					while (bufferedInputStream.available() > 0) {
						for (int i = bufferIndex; i > -1; i--)
							if (bufferedInputStream.available() >= bufferGroup[i].length) {
								bufferIndex = i;
								break;
							} else if (0 == i) {
								bufferIndex = 0;
								bufferGroup[0] = new byte[(int) bufferedInputStream.available()];
							}
						bufferedInputStream.read(bufferGroup[bufferIndex]);
						bufferedOutputStream.write(bufferGroup[bufferIndex]);
					}
					bufferedInputStream.close();
					fileInputStream.close();
					bufferedOutputStream.close();
					logger.info("结束下载[高速][" + file.getPath() + "]");
					logger.info("停止耗时统计" + Assit.wrapStr("timeConsumingKey", timeConsumingKey, helper.uninstallTimeConsuming(timeConsumingKey)));
				} else {
					disposeExceptionRedirect(response, name, "未指定下载模式！", "invalid_performance", 1000 * 60);
					logger.error("未指定下载模式！");
				}
			} else {
				disposeExceptionRedirect(response, name, "啊哦！链接错误没找到文件，请打开正确的文件链接！", "file_not_exists", 1000 * 60);
				logger.error("啊哦！链接错误没找到文件，请打开正确的文件链接！");
			}
		} else {
			disposeExceptionRedirect(response, name, "无效的md5Hex！", "invalid_md5hex", 1000 * 60);
			logger.error("无效的md5Hex！");
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	protected void disposeExceptionRedirect(HttpServletResponse response, String targetFile, String detailMessage, String repositoryDataKey, long validityDuration) {
		try {
			String dataResultString = null;
			JSONObject argsJson = new JSONObject();
			argsJson.accumulate("key", "push_repository_data");
			JSONObject valueJson = new JSONObject();
			valueJson.accumulate("repositoryDataKey", repositoryDataKey);
			valueJson.accumulate("validityDuration", validityDuration);// 有效期
			JSONObject repositoryDataValue = new JSONObject();
			repositoryDataValue.accumulate("targetFile", targetFile);
			repositoryDataValue.accumulate("detailMessage", detailMessage);
			valueJson.accumulate("repositoryDataValue", repositoryDataValue);
			argsJson.accumulate("value", valueJson);
			String contextRoot = ConfigureCache.getValue("context.root");
			URL url = new URL(ConfigureCache.getValue("domain.root") + (!"/".equals(contextRoot) ? contextRoot : "") + "/GeneralServlet");
			HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
			httpURLConnection.setRequestMethod("POST");
			httpURLConnection.setRequestProperty("Proxy-Connection", "Keep-Alive");
			httpURLConnection.setDoOutput(true);
			OutputStream outputStream = httpURLConnection.getOutputStream();
			DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
			String data = "args=" + argsJson.toString();
			dataOutputStream.write(data.getBytes());
			dataOutputStream.flush();
			dataOutputStream.close();
			InputStream inputStream = httpURLConnection.getInputStream();
			DataInputStream dataInputStream = new DataInputStream(inputStream);
			byte dataBuffer[] = new byte[dataInputStream.available()];
			dataInputStream.read(dataBuffer);
			dataResultString = new String(dataBuffer);
			httpURLConnection.disconnect();
			System.out.println("dataResultString:" + dataResultString);
			String redirect = (!"/".equals(contextRoot) ? contextRoot : "") + "/sg-fileupload/page/Exception.seam?repositoryDataKey=" + repositoryDataKey;
			response.sendRedirect(redirect);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
