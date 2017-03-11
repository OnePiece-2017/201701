package cn.dmdl.stl.hospitalbudget.admin.session;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.admin.entity.SystemSettings;
import cn.dmdl.stl.hospitalbudget.boot.ConfigureLoader;
import cn.dmdl.stl.hospitalbudget.common.session.CriterionEntityHome;
import cn.dmdl.stl.hospitalbudget.util.DataSourceManager;

@Name("systemSettingsHome")
public class SystemSettingsHome extends CriterionEntityHome<SystemSettings> {

	private static final long serialVersionUID = 1L;
	private Object[][] viewData;// 详情页初始化时用
	private Map<String, Object[]> modifyData;// 编辑页初始化时用
	private String args;// 编辑页发送的参数

	@SuppressWarnings("unchecked")
	public void invokeUpdate() {
		setMessage("");

		if (args != null && !"".equals(args)) {
			JSONObject argsJson = JSONObject.fromObject(args);
			List<Object[]> sourceList = getEntityManager().createNativeQuery("select the_id, the_key from system_settings").getResultList();
			if (sourceList != null && sourceList.size() > 0) {
				Connection connection = DataSourceManager.open(DataSourceManager.BY_JDBC_DEFAULT);
				PreparedStatement preparedStatement = null;
				try {
					preparedStatement = connection.prepareStatement("update system_settings set the_value = ? where the_id = ?");
					for (Object[] source : sourceList) {
						preparedStatement.setString(1, argsJson.getString(source[1].toString()));
						preparedStatement.setInt(2, Integer.parseInt(source[0].toString()));
						preparedStatement.executeUpdate();
					}
				} catch (SQLException e) {
					setMessage("操作失败！更新数据库。");
				} finally {
					DataSourceManager.close(connection, preparedStatement, null);
				}
				ConfigureLoader.initSystemSettingsMap();
			} else {
				setMessage("操作失败！远程数据丢失。");
			}
		} else {
			setMessage("操作失败！参数丢失。");
		}
	}

	public void setSystemSettingsTheId(Integer id) {
		setId(id);
	}

	public Integer getSystemSettingsTheId() {
		return (Integer) getId();
	}

	@Override
	protected SystemSettings createInstance() {
		SystemSettings systemSettings = new SystemSettings();
		return systemSettings;
	}

	public void load() {
		if (isIdDefined()) {
			wire();
		}
	}

	@SuppressWarnings("unchecked")
	public void wire() {
		getInstance();

		if (modifyData != null) {
			modifyData.clear();
		} else {
			modifyData = new HashMap<String, Object[]>();
		}
		List<SystemSettings> systemSettingsList = getEntityManager().createQuery("select systemSettings from SystemSettings systemSettings order by systemSettings.theId").getResultList();
		if (systemSettingsList != null && systemSettingsList.size() > 0) {
			for (SystemSettings systemSettings : systemSettingsList) {
				modifyData.put(systemSettings.getTheKey(), new Object[] { systemSettings.getTheLabel(), systemSettings.getTheValue() });
			}
		}
	}

	/** 详情页初始化 */
	@SuppressWarnings("unchecked")
	public void init() {
		List<Object[]> systemThemeList = getEntityManager().createNativeQuery("select the_id, the_value from system_theme where enabled = 1").getResultList();
		Map<String, Object> systemThemeMap = new HashMap<String, Object>();
		if (systemThemeList != null && systemThemeList.size() > 0) {
			for (Object[] systemTheme : systemThemeList) {
				systemThemeMap.put(systemTheme[0].toString(), systemTheme[1]);
			}
		}
		int itemSize = 2;
		List<SystemSettings> systemSettingsList = getEntityManager().createQuery("select systemSettings from SystemSettings systemSettings order by systemSettings.theId").getResultList();
		if (systemSettingsList != null && systemSettingsList.size() > 0) {
			viewData = new Object[systemSettingsList.size()][itemSize];
			int index = 0;
			for (SystemSettings systemSettings : systemSettingsList) {
				String theKey = systemSettings.getTheKey();
				String theValue = systemSettings.getTheValue();
				Object[] item = new Object[itemSize];
				item[0] = systemSettings.getTheLabel();
				if ("query_order_direction".equalsIgnoreCase(theKey)) {
					if ("asc".equalsIgnoreCase(theValue)) {
						theValue = "升序";
					} else if ("desc".equalsIgnoreCase(theValue)) {
						theValue = "降序";
					}
				} else if ("ask_on_logout".equalsIgnoreCase(theKey) || "ask_on_close_label".equalsIgnoreCase(theKey) || "reload_page_on_exist".equalsIgnoreCase(theKey)) {
					if ("true".equalsIgnoreCase(theValue)) {
						theValue = "是";
					} else if ("false".equalsIgnoreCase(theValue)) {
						theValue = "否";
					}
				} else if ("system_theme".equalsIgnoreCase(theKey)) {
					theValue = systemThemeMap.get(theValue) != null ? systemThemeMap.get(theValue).toString() : "";
				}
				item[1] = theValue;
				viewData[index] = item;
				index++;
			}
		} else {
			viewData = new Object[0][itemSize];
		}
	}

	public boolean isWired() {
		return true;
	}

	public SystemSettings getDefinedInstance() {
		return isIdDefined() ? getInstance() : null;
	}

	public Object[][] getViewData() {
		return viewData;
	}

	public Map<String, Object[]> getModifyData() {
		return modifyData;
	}

	public String getArgs() {
		return args;
	}

	public void setArgs(String args) {
		this.args = args;
	}

}
