package cn.dmdl.stl.hospitalbudget.util;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import org.apache.log4j.Logger;

import net.sf.json.JSONObject;

public class HttpUtil {

	private static Logger logger = Logger.getLogger(HttpUtil.class);

	/**
	 * post方法调用
	 * @param uri
	 * @param param
	 * @return
	 */
	public static String postMethod(String uri, String param){
		logger.info("POST method invoke!"+uri);
		HttpURLConnection connection = null;
		OutputStreamWriter osw = null;
		InputStreamReader isr = null;
		Long start = System.currentTimeMillis();
		JSONObject paramJson = JSONObject.fromObject(param);
		try {
			//创建URL访问连接
			URL url = new URL(uri);
			connection = (HttpURLConnection) url.openConnection();
			//数据传输方式
			connection.setRequestMethod("POST");
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setUseCaches(false);
			connection.setReadTimeout(10000);
			connection.setConnectTimeout(10000);
			
			connection.setRequestProperty("Content-type", "application/json");
			for(Object obj : paramJson.keySet()){
				connection.setRequestProperty(obj.toString(), paramJson.getString(obj.toString()));
			}
			
			connection.connect();

			osw = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
			osw.write(param);
			osw.flush();

			//System.out.println(connection.getHeaderFields());

			isr = new InputStreamReader(connection.getInputStream(), "UTF-8");

			int c = -1;
			StringBuilder sb = new StringBuilder();
			while ((c = isr.read()) >= 0) {
				sb.append((char) c);
			}

			return sb.toString();

		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}finally {
			try {
				if (null != osw)
					osw.close();

				if (null != isr)
					isr.close();

			} catch (IOException e) {
				e.printStackTrace();
			}

			connection.disconnect();
			Long end = System.currentTimeMillis();
			logger.info("post end uri:"+uri+" time:"+(end-start));
		}
	}

	/**
	 * get方法调用
	 * @param uri
	 * @return
	 */
	public static String getMethod(String uri){

		//logger.info("GET method invoke!");
		HttpURLConnection connection = null;
		InputStreamReader isr = null;
		Long start = System.currentTimeMillis();
		try {
			//创建URL访问连接
			URL url = new URL(uri);
			connection = (HttpURLConnection) url.openConnection();
			//数据传输方式
			connection.setRequestMethod("GET");
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setUseCaches(false);
			connection.setReadTimeout(10000);
			connection.setConnectTimeout(10000);
			connection.connect();
			//[STDOUT] {null=[HTTP/1.1 200 OK], Date=[Wed, 15 Oct 2014 13:59:40 GMT], Vary=[Accept-Encoding], Content-Length=[1503], X-UA-Compatible=[IE=EmulateIE8], Keep-Alive=[timeout=5, max=100], Set-Cookie=[JSESSIONID=C7DD084C2257CC62379F8E634C202004.jboss2; Path=/], Connection=[Keep-Alive], Content-Type=[text/xml;charset=utf-8], Server=[Apache/2.2.21 (Unix) DAV/2 mod_jk/1.2.31], X-Powered-By=[Servlet 2.5; JBoss-5.0/JBossWeb-2.1], Cache-Control=[no-cache]}
			Map map = connection.getHeaderFields();
			isr = new InputStreamReader(connection.getInputStream(), "UTF-8");
			//isr = new InputStreamReader(connection.getInputStream(), "gbk");
			//sun.net.www.protocol.http.HttpURLConnection:http://green.acsm.cn/newbk/dastring?realPlantId=4099,4071,4093,4095,4070,4088,3907,3374,4085,4088,3833,4053,3905,4094,4072,4078,4080,4082,3382,4086,4057,4056,4134,4137,4136,4133,4138,4144,4145,4174,4182,4280,4281
			int c = -1;
			StringBuilder sb = new StringBuilder();
			while ((c = isr.read()) >= 0) {
				sb.append((char) c);
			}

			return sb.toString();

		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}finally {
			try {
				if (null != isr)
					isr.close();

			} catch (IOException e) {
				e.printStackTrace();
			}

			connection.disconnect();
			Long end = System.currentTimeMillis();
			logger.info("get end uri:"+uri+" time:"+(end-start));
		}
	}


}
