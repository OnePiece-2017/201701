package cn.dmdl.stl.hospitalbudget.misc;

import java.util.List;

import net.sf.json.JSONArray;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityHome;

@Name("loginStatisticsCharts")
public class LoginStatisticsCharts extends EntityHome<Object> {

	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unchecked")
	public JSONArray gainHighchartsSeries() {
		JSONArray resultJson = new JSONArray();

		StringBuffer sql = new StringBuffer();
		sql.append(" select");
		sql.append(" user_info.user_info_id,");
		sql.append(" user_info.username,");
		sql.append(" count(user_info.user_info_id) as login_count");
		sql.append(" from login_info");
		sql.append(" inner join user_info on user_info.user_info_id = login_info.user_info_id and user_info.deleted = 0");
		sql.append(" inner join user_info_extend on user_info_extend.user_info_extend_id = user_info.user_info_extend_id and user_info_extend.deleted = 0");
		sql.append(" where login_info.login_time is not null and login_info.logout_time is not null");
		sql.append(" group by user_info.user_info_id");
		sql.append(" order by login_count desc");
		sql.insert(0, "select * from (").append(") as resultset");
		List<Object[]> resultList = getEntityManager().createNativeQuery(sql.toString()).getResultList();
		if (resultList != null) {
			for (Object[] result : resultList) {
				Object[] item = new Object[2];
				item[0] = result[1];
				item[1] = result[2];
				resultJson.add(item);
			}
		}
		return resultJson;
	}
}
