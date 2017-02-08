package cn.dmdl.stl.hospitalbudget.boot;

import static org.jboss.seam.ScopeType.APPLICATION;
import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;

import cn.dmdl.stl.hospitalbudget.admin.entity.Dictionary;
import cn.dmdl.stl.hospitalbudget.util.Assit;
import cn.dmdl.stl.hospitalbudget.util.DataSourceManager;
import cn.dmdl.stl.hospitalbudget.util.MD5;

/**
 * 配置加载器
 */
@Name(value = "configureLoader")
@Scope(value = APPLICATION)
@Install(precedence = BUILT_IN)
@Startup(depends = { "configureCache", "projectInfoLoader", "dataSourceLoader" })
public class ConfigureLoader {

	private static Logger logger = Logger.getLogger(ConfigureLoader.class);

	@Create
	public void init() {
		logger.info("init");
		initKeyValuePairsMap();
		initMessageInfoMap();
		initSystemSettingsMap();
		initDictionary();
		initUserInfoIdMD5Map();
		initVersionMap();
	}

	/** 初始化键-值对集合 */
	private void initKeyValuePairsMap() {
		logger.info("initKeyValuePairsMap");
		if (ConfigureCache.keyValuePairsMap != null) {
			ConfigureCache.keyValuePairsMap.clear();
		} else {
			ConfigureCache.keyValuePairsMap = new HashMap<String, String>();
		}
		try {
			String path = getClass().getResource(".").getPath();
			logger.info("path-->" + path);
			String pathname = path + "../resources/" + "key_value_pairs.xml";
			logger.info("pathname-->" + pathname);
			File xmlfile = new File(pathname);
			SAXReader reader = new SAXReader();
			Document document = reader.read(xmlfile);
			Element rootElement = document.getRootElement();
			List<?> leafList = rootElement.elements("item");
			if (leafList != null && leafList.size() > 0) {
				for (Object leaf : leafList) {
					Element leafElement = (Element) leaf;
					if ("true".equals(leafElement.attribute("enabled").getText()) && leafElement.attribute("for").getText().equals(ConfigureCache.getProjectValue(leafElement.attribute("key").getText()))) {
						ConfigureCache.keyValuePairsMap.put(leafElement.attribute("key").getText(), leafElement.element("value").getText());
						logger.info(Assit.wrapStr(leafElement.attribute("key").getText(), leafElement.element("value").getText()));
					}
				}
			}
		} catch (Exception e) {
			logger.error("initKeyValuePairsMap", e);
		}
	}

	/** 初始化消息集合 */
	public static void initMessageInfoMap() {
		logger.info("initMessageInfoMap");
		if (ConfigureCache.messageInfoMap != null) {
			ConfigureCache.messageInfoMap.clear();
		} else {
			ConfigureCache.messageInfoMap = new HashMap<String, String>();
		}
		Connection connection = DataSourceManager.open(DataSourceManager.BY_JDBC_DEFAULT);
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			preparedStatement = connection.prepareStatement("select message_name, message_value from message_info where deleted = 0");
			resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				ConfigureCache.messageInfoMap.put(resultSet.getString("message_name"), resultSet.getString("message_value"));
				logger.info(Assit.wrapStr(resultSet.getString("message_name"), resultSet.getString("message_value")));
			}
		} catch (SQLException e) {
			logger.error("initMessageInfoMap", e);
		} finally {
			DataSourceManager.close(connection, preparedStatement, resultSet);
		}
	}

	/** 初始化系统设置集合 */
	public static void initSystemSettingsMap() {
		logger.info("initSystemSettingsMap");
		if (ConfigureCache.systemSettingsMap != null) {
			ConfigureCache.systemSettingsMap.clear();
		} else {
			ConfigureCache.systemSettingsMap = new HashMap<String, String>();
		}
		Connection connection = DataSourceManager.open(DataSourceManager.BY_JDBC_DEFAULT);
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			preparedStatement = connection.prepareStatement("select the_key, the_value from system_settings");
			resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				ConfigureCache.systemSettingsMap.put(resultSet.getString("the_key"), resultSet.getString("the_value"));
				logger.info(Assit.wrapStr(resultSet.getString("the_key"), resultSet.getString("the_value")));
			}
		} catch (SQLException e) {
			logger.error("initSystemSettingsMap", e);
		} finally {
			DataSourceManager.close(connection, preparedStatement, resultSet);
		}
	}

	/** 初始化字典集合 */
	public static void initDictionary() {
		logger.info("initDictionary");
		if (ConfigureCache.dictionaryMap != null) {
			ConfigureCache.dictionaryMap.clear();
		} else {
			ConfigureCache.dictionaryMap = new HashMap<Integer, Dictionary>();
		}
		if (ConfigureCache.dictionaryList != null) {
			ConfigureCache.dictionaryList.clear();
		} else {
			ConfigureCache.dictionaryList = new ArrayList<Dictionary>();
		}
		Connection connection = DataSourceManager.open(DataSourceManager.BY_JDBC_DEFAULT);
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			preparedStatement = connection.prepareStatement("select dictionary.the_id, dictionary.the_pid, dictionary.the_value, dictionary.the_description from dictionary");
			resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				Dictionary dictionary = new Dictionary();
				dictionary.setTheId(resultSet.getInt("the_id"));
				dictionary.setTheValue(resultSet.getString("the_value"));
				dictionary.setTheDescription(resultSet.getString("the_description"));
				ConfigureCache.dictionaryMap.put(dictionary.getTheId(), dictionary);
			}
			resultSet = preparedStatement.executeQuery("select dictionary.the_id, dictionary.the_pid from dictionary");
			while (resultSet.next()) {
				Dictionary dictionary = ConfigureCache.dictionaryMap.get(resultSet.getInt("the_id"));
				dictionary.setDictionary(ConfigureCache.dictionaryMap.get(resultSet.getInt("the_pid")));// if the value is SQL NULL, the value returned is 0
				ConfigureCache.dictionaryList.add(dictionary);
			}
		} catch (SQLException e) {
			logger.error("initDictionary", e);
		} finally {
			DataSourceManager.close(connection, preparedStatement, resultSet);
		}
	}

	/** 初始化用户id集合 */
	private void initUserInfoIdMD5Map() {
		logger.info("initUserInfoIdMD5Map");
		if (ConfigureCache.userInfoIdMD5Map != null) {
			ConfigureCache.userInfoIdMD5Map.clear();
		} else {
			ConfigureCache.userInfoIdMD5Map = new HashMap<String, Integer>();
		}
		Connection connection = DataSourceManager.open(DataSourceManager.BY_JDBC_DEFAULT);
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			preparedStatement = connection.prepareStatement("select user_info_id from user_info where deleted = 0 and enabled = 1");
			resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				ConfigureCache.userInfoIdMD5Map.put(MD5.getMD5Alpha(resultSet.getInt(1)), resultSet.getInt(1));
				logger.info(Assit.wrapStr(MD5.getMD5Alpha(resultSet.getInt(1)), resultSet.getInt(1)));
			}
		} catch (SQLException e) {
			logger.error("initUserInfoIdMD5Map", e);
		} finally {
			DataSourceManager.close(connection, preparedStatement, resultSet);
		}
	}

	/** 初始化版本集合 */
	private void initVersionMap() {
		logger.info("initVersionMap");
		if (ConfigureCache.versionMap != null) {
			ConfigureCache.versionMap.clear();
		} else {
			ConfigureCache.versionMap = new HashMap<Integer, String>();
		}
		// TODO: jdbc查询version_info表
		ConfigureCache.versionMap.put(1, "ultimate");
		ConfigureCache.versionMap.put(2, "professional");
		ConfigureCache.versionMap.put(3, "trial");
		logger.info(Assit.wrapStr(1, "ultimate"));
		logger.info(Assit.wrapStr(2, "professional"));
		logger.info(Assit.wrapStr(3, "trial"));
	}

}
