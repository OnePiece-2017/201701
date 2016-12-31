package cn.dmdl.stl.hospitalbudget.admin.session;

import java.util.Date;

import net.sf.json.JSONObject;

import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.admin.entity.MessageInfo;
import cn.dmdl.stl.hospitalbudget.boot.ConfigureLoader;
import cn.dmdl.stl.hospitalbudget.common.session.CriterionEntityHome;
import cn.dmdl.stl.hospitalbudget.util.Assit;

@Name("messageInfoHome")
public class MessageInfoHome extends CriterionEntityHome<MessageInfo> {

	private static final long serialVersionUID = 1L;
	private JSONObject dataIssue;

	public void invokeIssue() {
		if (dataIssue != null) {
			dataIssue.clear();
		} else {
			ConfigureLoader.initMessageInfoMap();
			dataIssue = new JSONObject();
			dataIssue.accumulate("message", "发布成功！");
		}
	}

	@Override
	public String persist() {
		setMessage("");

		if (Assit.getResultSetSize("select message_info_id from message_info where deleted = 0 and message_name = '" + instance.getMessageName() + "'") > 0) {
			setMessage("此名称太受欢迎,请更换一个");
			return null;
		}

		if (Assit.getResultSetSize("select message_info_id from message_info where deleted = 0 and message_value = '" + instance.getMessageValue() + "'") > 0) {
			setMessage("此数据太受欢迎,请更换一个");
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

		if (Assit.getResultSetSize("select message_info_id from message_info where deleted = 0 and message_name = '" + instance.getMessageName() + "' and message_info_id != " + instance.getMessageInfoId()) > 0) {
			setMessage("此名称太受欢迎,请更换一个");
			return null;
		}

		if (Assit.getResultSetSize("select message_info_id from message_info where deleted = 0 and message_value = '" + instance.getMessageValue() + "' and message_info_id != " + instance.getMessageInfoId()) > 0) {
			setMessage("此数据太受欢迎,请更换一个");
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

	public void setMessageInfoMessageInfoId(Integer id) {
		setId(id);
	}

	public Integer getMessageInfoMessageInfoId() {
		return (Integer) getId();
	}

	@Override
	protected MessageInfo createInstance() {
		MessageInfo messageInfo = new MessageInfo();
		return messageInfo;
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

	public MessageInfo getDefinedInstance() {
		return isIdDefined() ? getInstance() : null;
	}

	public JSONObject getDataIssue() {
		return dataIssue;
	}

	public void setDataIssue(JSONObject dataIssue) {
		this.dataIssue = dataIssue;
	}

}
