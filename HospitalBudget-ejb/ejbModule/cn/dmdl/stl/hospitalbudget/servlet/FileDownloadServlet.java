package cn.dmdl.stl.hospitalbudget.servlet;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

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
		String name = args.getString("name");
		// String type = args.getString("type");
		// String size = args.getString("size");
		String md5Hex = args.getString("md5Hex");
		if (md5Hex != null && !"".equals(md5Hex)) {
			File file = new File(ConfigureCache.getValue("storage.path.root") + md5Hex);
			if (file != null && file.exists() && file.isFile()) {
				Map<Integer, String> performanceMap = new HashMap<Integer, String>();
				performanceMap.put(1, "标准");
				performanceMap.put(2, "急速");
				performanceMap.put(3, "狂飙");
				performanceMap.put(4, "秒杀");
				int performance = Integer.parseInt(ConfigureCache.getValue("fileupload.download_performance"));
				if (performanceMap.containsKey(performance)) {
					response.reset();
					response.addHeader("Content-Disposition", "attachment;filename=" + new String(("\"" + name + "\"").getBytes(), "iso-8859-1"));// 允许文件名包含空格
					response.addHeader("Content-Length", "" + file.length());
					Helper helper = Helper.getInstance();
					Integer timeConsumingKey = helper.installTimeConsuming();
					logger.info("正在下载" + Assit.wrapStr(performanceMap.get(performance), timeConsumingKey, file.getPath(), Assit.explainByte(file.length())));
					if (1 == performance) {
						FileInputStream fileInputStream = new FileInputStream(file);
						BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
						byte[] readBuffer = new byte[1024 * 128];
						int readLength = 0;
						OutputStream outputStream = response.getOutputStream();
						while ((readLength = bufferedInputStream.read(readBuffer)) > 0)
							outputStream.write(readBuffer, 0, readLength);
						bufferedInputStream.close();
						fileInputStream.close();
						outputStream.close();
					} else if (2 == performance) {
						FileInputStream fileInputStream = new FileInputStream(file);
						BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
						byte[] readBuffer = new byte[1024 * 512];
						int readLength = 0;
						OutputStream outputStream = response.getOutputStream();
						while ((readLength = bufferedInputStream.read(readBuffer)) > 0)
							outputStream.write(readBuffer, 0, readLength);
						bufferedInputStream.close();
						fileInputStream.close();
						outputStream.close();
					} else if (3 == performance) {
					} else if (4 == performance) {
					}
					Long timeConsumingValue = helper.uninstallTimeConsuming(timeConsumingKey);
					logger.info("完成下载" + Assit.wrapStr(performanceMap.get(performance), timeConsumingKey, file.getPath(), Assit.explainByte(file.length()), timeConsumingValue != null ? Assit.explainTime(timeConsumingValue) : null));
				} else {
					disposeExceptionRedirect(response, name, "未指定下载性能！");
					logger.error("未指定下载性能！");
				}
			} else {
				disposeExceptionRedirect(response, name, "啊哦！链接错误没找到文件，请打开正确的文件链接！");
				logger.error("啊哦！链接错误没找到文件，请打开正确的文件链接！");
			}
		} else {
			disposeExceptionRedirect(response, name, "无效的md5Hex！");
			logger.error("无效的md5Hex！");
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	protected void disposeExceptionRedirect(HttpServletResponse response, String targetFile, String detailMessage) {
		try {
			Helper helper = Helper.getInstance();
			JSONObject repositoryDataValue = new JSONObject();
			repositoryDataValue.accumulate("targetFile", targetFile);
			repositoryDataValue.accumulate("detailMessage", detailMessage);
			Integer repositoryDataKey = helper.createRepositoryData(1000L * 60, repositoryDataValue);
			String contextRoot = ConfigureCache.getValue("context.root");
			String redirect = (!"/".equals(contextRoot) ? contextRoot : "") + "/sg-fileupload/page/Exception.seam?invoke_result=" + (repositoryDataKey != null ? INVOKE_SUCCESS : INVOKE_FAILURE) + "&repositoryDataKey=" + repositoryDataKey;
			response.sendRedirect(redirect);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
