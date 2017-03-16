package cn.dmdl.stl.hospitalbudget.admin.session;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.common.session.CriterionEntityHome;
import cn.dmdl.stl.hospitalbudget.util.CommonToolLocal;

@Name("quickStartHome")
public class QuickStartHome extends CriterionEntityHome<Object> {

	private static final long serialVersionUID = 1L;

	@In(create = true)
	private CommonToolLocal commonTool;

	private String configure;// 配置

	@Override
	public String update() {
		// 清理旧的配置
		commonTool.deleteIntermediate("quick_start", "user_info_id = " + sessionToken.getUserInfoId());
		// 创建新的配置
		if (configure != null && !"".equals(configure)) {
			String[] idArr = configure.split(",");
			if (idArr != null && idArr.length > 0) {
				for (String id : idArr) {
					commonTool.insertIntermediate("quick_start", new String[] { "user_info_id", "menu_info_id" }, new Object[] { sessionToken.getUserInfoId(), id });
				}
			}
		}
		setMessage("保存成功！");
		return "updated";
	}

	public void wire() {
		getInstance();
		if (firstTime) {
			// 获取配置
			configure = commonTool.selectIntermediateAsIds("quick_start", "menu_info_id", "user_info_id = " + sessionToken.getUserInfoId());

			firstTime = false;
		}
	}

	public String getConfigure() {
		return configure;
	}

	public void setConfigure(String configure) {
		this.configure = configure;
	}

}
