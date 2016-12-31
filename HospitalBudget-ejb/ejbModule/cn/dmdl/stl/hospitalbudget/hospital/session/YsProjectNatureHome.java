package cn.dmdl.stl.hospitalbudget.hospital.session;

import java.util.Date;

import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.common.session.CriterionEntityHome;
import cn.dmdl.stl.hospitalbudget.hospital.entity.YsProjectNature;
import cn.dmdl.stl.hospitalbudget.util.Assit;

@Name("ysProjectNatureHome")
public class YsProjectNatureHome extends CriterionEntityHome<YsProjectNature> {

	private static final long serialVersionUID = 1L;

	@Override
	public String persist() {
		setMessage("");

		if (Assit.getResultSetSize("select the_id from ys_project_nature where deleted = 0 and the_value = '" + instance.getTheValue() + "'") > 0) {
			setMessage("此名称太受欢迎,请更换一个");
			return null;
		}

		instance.setInsertTime(new Date());
		instance.setInsertUser(sessionToken.getUserInfoId());
		getEntityManager().persist(instance);
		getEntityManager().flush();
		raiseAfterTransactionSuccessEvent();
		return "persisted";
	}

	@Override
	public String update() {
		setMessage("");

		if (Assit.getResultSetSize("select the_id from ys_project_nature where deleted = 0 and the_value = '" + instance.getTheValue() + "' and the_id != " + instance.getTheId()) > 0) {
			setMessage("此名称太受欢迎,请更换一个");
			return null;
		}

		joinTransaction();
		instance.setUpdateTime(new Date());
		instance.setUpdateUser(sessionToken.getUserInfoId());
		getEntityManager().flush();
		raiseAfterTransactionSuccessEvent();
		return "updated";
	}

	@Override
	public String remove() {
		setMessage("");

		getInstance();

		instance.setDeleted(true);
		getEntityManager().flush();
		raiseAfterTransactionSuccessEvent();
		return "removed";
	}

	public void setYsProjectNatureTheId(Integer id) {
		setId(id);
	}

	public Integer getYsProjectNatureTheId() {
		return (Integer) getId();
	}

	@Override
	protected YsProjectNature createInstance() {
		YsProjectNature ysProjectNature = new YsProjectNature();
		return ysProjectNature;
	}

	public void load() {
		if (isIdDefined()) {
			wire();
		}
	}

	public void wire() {
		getInstance();
	}

	public boolean isWired() {
		return true;
	}

	public YsProjectNature getDefinedInstance() {
		return isIdDefined() ? getInstance() : null;
	}

}
