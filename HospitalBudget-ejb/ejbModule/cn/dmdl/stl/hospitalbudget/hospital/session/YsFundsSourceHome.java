package cn.dmdl.stl.hospitalbudget.hospital.session;

import java.util.Date;

import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.common.session.CriterionEntityHome;
import cn.dmdl.stl.hospitalbudget.hospital.entity.YsFundsSource;
import cn.dmdl.stl.hospitalbudget.util.Assit;

@Name("ysFundsSourceHome")
public class YsFundsSourceHome extends CriterionEntityHome<YsFundsSource> {

	private static final long serialVersionUID = 1L;

	@Override
	public String persist() {
		setMessage("");

		if (Assit.getResultSetSize("select the_id from ys_funds_source where deleted = 0 and the_value = '" + instance.getTheValue() + "'") > 0) {
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

		if (Assit.getResultSetSize("select the_id from ys_funds_source where deleted = 0 and the_value = '" + instance.getTheValue() + "' and the_id != " + instance.getTheId()) > 0) {
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

	public void setYsFundsSourceTheId(Integer id) {
		setId(id);
	}

	public Integer getYsFundsSourceTheId() {
		return (Integer) getId();
	}

	@Override
	protected YsFundsSource createInstance() {
		YsFundsSource ysFundsSource = new YsFundsSource();
		return ysFundsSource;
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

	public YsFundsSource getDefinedInstance() {
		return isIdDefined() ? getInstance() : null;
	}

}
