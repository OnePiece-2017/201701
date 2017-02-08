package cn.dmdl.stl.hospitalbudget.util;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Helper
 * 
 * <pre>
 * 注意：已应用单例模式、序列号
 */
public class Helper implements Serializable {

	private static final long serialVersionUID = 1L;
	private static volatile Helper instance = null;
	private static Map<Integer, Long> timeConsumingMap = new HashMap<Integer, Long>();
	private static final int TIME_CONSUMING_MAX_SIZE = Integer.MAX_VALUE;

	private Helper() {
	}

	private static synchronized void initHelper() {
		if (null == instance)
			instance = new Helper();
	}

	public static Helper getInstance() {
		if (null == instance)
			initHelper();
		return instance;
	}

	public synchronized Integer installTimeConsuming() {
		Integer timeConsumingKey = null;
		if (timeConsumingMap.size() < TIME_CONSUMING_MAX_SIZE) {
			Random random = new Random();
			random.setSeed(new Date().getTime());
			while (timeConsumingMap.containsKey(timeConsumingKey = random.nextInt(TIME_CONSUMING_MAX_SIZE)))
				;
			timeConsumingMap.put(timeConsumingKey, new Date().getTime());
		}
		return timeConsumingKey;
	}

	public Long uninstallTimeConsuming(Integer timeConsumingKey) {
		return uninstallTimeConsuming(timeConsumingKey, new Date().getTime());
	}

	private synchronized Long uninstallTimeConsuming(Integer timeConsumingKey, long getOutMilliseconds) {
		Long getInMilliseconds = timeConsumingMap.get(timeConsumingKey);
		timeConsumingMap.remove(timeConsumingKey);
		if (getInMilliseconds != null)
			return getOutMilliseconds - getInMilliseconds;
		return null;
	}

}
