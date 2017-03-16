package cn.dmdl.stl.hospitalbudget.boot;

import static org.jboss.seam.ScopeType.APPLICATION;
import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;

/**
 * 数据源加载器
 */
@Name(value = "dataSourceLoader")
@Scope(value = APPLICATION)
@Install(precedence = BUILT_IN)
@Startup(depends = { "configureCache", "projectInfoLoader" })
public class DataSourceLoader {

	private static Logger logger = Logger.getLogger(DataSourceLoader.class);

	@Create
	public void init() {
		if (ConfigureCache.dataSourceMap != null) {
			ConfigureCache.dataSourceMap.clear();
		} else {
			ConfigureCache.dataSourceMap = new HashMap<String, JSONObject>();
		}
		try {
			String path = getClass().getResource(".").getPath();
			String pathname = path + "../resources/" + "data_source.xml";
			File xmlfile = new File(pathname);
			SAXReader reader = new SAXReader();
			Document document = reader.read(xmlfile);
			Element rootElement = document.getRootElement();
			List<?> leafList = rootElement.elements("connection");
			if (leafList != null && leafList.size() > 0) {
				for (Object leaf : leafList) {
					Element leafElement = (Element) leaf;
					if ("true".equals(leafElement.attribute("enabled").getText()) && leafElement.attribute("for").getText().equals(ConfigureCache.getProjectValue("datasource"))) {
						JSONObject connection = new JSONObject();
						connection.accumulate("key", leafElement.attribute("key").getText());
						connection.accumulate("username", leafElement.element("username").getText());
						connection.accumulate("password", leafElement.element("password").getText());
						String url = leafElement.element("url").getText();
						Element parameterElement = leafElement.element("parameter");
						if (parameterElement != null) {
							List<?> itemList = parameterElement.elements("item");
							if (itemList != null && itemList.size() > 0) {
								url += "?";
								for (int i = 0; i < itemList.size(); i++) {
									Element itemElement = (Element) itemList.get(i);
									url += itemElement.getText();
									if (i < itemList.size() - 1) {
										url += "&";
									}
								}
							}
						}
						connection.accumulate("url", url);
						ConfigureCache.pushDataSource(connection.getString("key"), connection);
					}
				}
			}
		} catch (Exception e) {
			logger.error("init", e);
		}
	}

}
