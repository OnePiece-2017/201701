package cn.dmdl.stl.hospitalbudget.boot;

import static org.jboss.seam.ScopeType.APPLICATION;
import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;

/**
 * 項目信息加载器
 */
@Name(value = "projectInfoLoader")
@Scope(value = APPLICATION)
@Install(precedence = BUILT_IN)
@Startup(depends = "configureCache")
public class ProjectInfoLoader {

	private static Logger logger = Logger.getLogger(ProjectInfoLoader.class);

	@Create
	public void init() {
		if (ConfigureCache.projectInfoMap != null) {
			ConfigureCache.projectInfoMap.clear();
		} else {
			ConfigureCache.projectInfoMap = new HashMap<String, String>();
		}
		String path = getClass().getResource(".").getPath();
		String pathname = path.substring(0, path.lastIndexOf(ConfigureCache.getProjectName() + "-ear.ear")) + ConfigureCache.getProjectName() + "-project_info.properties";
		File profile = new File(pathname);
		Properties properties = new Properties();
		FileInputStream fileInputStream;
		try {
			fileInputStream = new FileInputStream(profile);
			properties.load(fileInputStream);
			fileInputStream.close();
		} catch (Exception e) {
			logger.error("init", e);
		}
		if (properties != null && properties.size() > 0) {
			for (Object key : properties.keySet()) {
				ConfigureCache.projectInfoMap.put(key.toString(), properties.getProperty(key.toString()));
			}
		}
	}

}
