package cn.dmdl.stl.hospitalbudget.boot;

import static org.jboss.seam.ScopeType.APPLICATION;
import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;

import cn.dmdl.stl.hospitalbudget.admin.entity.Dictionary;
import cn.dmdl.stl.hospitalbudget.util.Assit;

/**
 * 配置缓存
 */
@Name(value = "configureCache")
@Scope(value = APPLICATION)
@Install(precedence = BUILT_IN)
@Startup
public class ConfigureCache {

	private static Logger logger = Logger.getLogger(ConfigureCache.class);
	private static String projectName;// Seam Web Project - Project name
	private static Map<String, Boolean> executingMap;// 执行中集合
	public static Map<String, String> projectInfoMap;// 項目信息集合
	public static Map<String, String> keyValuePairsMap;// 键-值对集合
	public static Map<String, JSONObject> dataSourceMap;// 数据源集合
	public static Map<String, String> messageInfoMap;// 消息集合
	public static Map<String, String> systemSettingsMap;// 系统设置集合
	public static Map<Integer, Dictionary> dictionaryMap;// 字典集合
	public static List<Dictionary> dictionaryList;// 字典集合
	public static Map<String, Integer> userInfoIdMD5Map;// 用户id集合
	public static Map<Integer, String> versionMap;// 版本集合

	@Create
	public void init() {
		if (ConfigureCache.executingMap != null) {
			ConfigureCache.executingMap.clear();
		} else {
			ConfigureCache.executingMap = new HashMap<String, Boolean>();
		}

		String path = getClass().getResource(".").getPath();
		projectName = path.substring(1 + "-ear.ear".length() + path.lastIndexOf("-ear.ear"), path.lastIndexOf("-ejb.jar"));
		logger.info(Assit.surroundContents("Seam Web Project - Project name", projectName));
	}

	/** 获取项目名称 */
	public static String getProjectName() {
		return projectName;
	}

	/** 获取执行状态 */
	public static boolean isExecuting(String executingKey) {
		return executingMap.get(executingKey) != null ? executingMap.get(executingKey) : false;
	}

	/** 获取执行状态 */
	public static boolean isExecuting(Class<?> executingClass) {
		return isExecuting(executingClass.getName());
	}

	/** 设置执行状态 */
	public static void setExecuting(String executingKey, boolean executingValue) {
		if (executingValue) {
			executingMap.put(executingKey, true);
		} else {
			executingMap.remove(executingKey);
		}
	}

	/** 设置执行状态 */
	public static void setExecuting(Class<?> executingClass, boolean executingValue) {
		setExecuting(executingClass.getName(), executingValue);
	}

	/** 获取項目信息数据 */
	public static String getProjectValue(String key) {
		return projectInfoMap.get(key);
	}

	/** 获取值数据 */
	public static String getValue(String key) {
		return keyValuePairsMap.get(key);
	}

	/** 推送数据源 */
	public static void pushDataSource(String key, JSONObject connection) {
		dataSourceMap.put(key, connection);
	}

	/** 拉取数据源 */
	public static JSONObject pullDataSource(String key) {
		return dataSourceMap.get(key);
	}

	/** 移除数据源 */
	public static void removeDataSource(String key) {
		dataSourceMap.remove(key);
	}

	/** 获取消息数据 */
	public static String getMessageValue(String messageName) {
		return messageInfoMap.get(messageName);
	}

	/** 获取系统设置数据 */
	public static String getSettingsValue(String key) {
		return systemSettingsMap.get(key);
	}
}
