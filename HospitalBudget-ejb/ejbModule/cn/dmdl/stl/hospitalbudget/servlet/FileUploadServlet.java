package cn.dmdl.stl.hospitalbudget.servlet;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;

import cn.dmdl.stl.hospitalbudget.boot.ConfigureCache;

/**
 * Servlet implementation class FileUploadServlet
 */
public class FileUploadServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(FileUploadServlet.class);

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// ref --> tiy.general.Test.main(String[])
		try {
			// Check that we have a file upload request
			boolean isMultipart = ServletFileUpload.isMultipartContent(request);
			if (isMultipart) {

				// Create a factory for disk-based file items
				DiskFileItemFactory factory = new DiskFileItemFactory();

				// Set factory constraints
				factory.setSizeThreshold(1024 * 1);// 超过该字节则将文件保存到repository
				factory.setRepository(new File(ConfigureCache.getPathValue("fileupload.repository")));

				// Create a new file upload handler
				ServletFileUpload upload = new ServletFileUpload(factory);

				// Set overall request size constraint
				upload.setSizeMax(1024 * 1024 * 100);// 最大上传字节

				// Parse the request
				List<FileItem> items = upload.parseRequest(request);

				String succeed = "";

				// Process the uploaded items
				Iterator iter = items.iterator();
				while (iter.hasNext()) {
					FileItem item = (FileItem) iter.next();
					if (item.isFormField()) {
						processFormField(item);
					} else {
						succeed += "<img src=\"" + processUploadedFile(item) + "\"/><br/>";
					}
				}
				response.setCharacterEncoding("utf-8");
				response.setContentType("text/html;charset=UTF-8");
				response.getWriter().print("succeed<br/>" + succeed);
			} else {
				response.setCharacterEncoding("utf-8");
				response.setContentType("text/html;charset=UTF-8");
				response.getWriter().print("failed");

			}
		} catch (FileUploadException e) {
			e.printStackTrace();
		}
	}

	// Process a regular form field
	private void processFormField(FileItem item) {
		logger.info("processFormField=======>begin");
		String name = item.getFieldName();
		String value = item.getString();
		logger.info("name: " + name);
		logger.info("value: " + value);
		System.out.println("value: " + value);// System.out.println&logger.info中文亂碼
		logger.info("processFormField=======>end");
	}

	// Process a file upload
	private String processUploadedFile(FileItem item) {
		logger.info("processUploadedFile=======>begin");
		String fieldName = item.getFieldName();
		String fileName = item.getName();
		String contentType = item.getContentType();
		boolean isInMemory = item.isInMemory();
		long sizeInBytes = item.getSize();
		logger.info("fieldName: " + fieldName);
		logger.info("fileName: " + fileName);
		logger.info("contentType: " + contentType);
		logger.info("isInMemory: " + isInMemory);
		logger.info("sizeInBytes: " + sizeInBytes);

		try {
			File uploadedFile = new File(ConfigureCache.getPathValue("storage.root") + fileName);
			item.write(uploadedFile);
		} catch (Exception e) {
			e.printStackTrace();
		}

		logger.info("processUploadedFile=======>end");
		return "http://127.0.0.1/upload/" + fileName;
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
