package cn.dmdl.stl.hospitalbudget.admin.session;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.admin.entity.MessageInfo;
import cn.dmdl.stl.hospitalbudget.common.session.CriterionEntityQuery;

@Name("messageInfoList")
public class MessageInfoList extends CriterionEntityQuery<MessageInfo> {

	private static final long serialVersionUID = 1L;
	private static final String EJBQL = "select messageInfo from MessageInfo messageInfo where messageInfo.deleted = 0";
	private MessageInfo messageInfo = new MessageInfo();
	private String keyword;// 关键词

	@Override
	protected Query createQuery() {
		String sql = EJBQL;
		if (keyword != null && !"".equals(keyword)) {
			sql += " and (messageInfo.messageName like '%" + keyword + "%' or messageInfo.messageValue like '%" + keyword + "%')";
		}
		setEjbql(sql);
		return super.createQuery();
	}

	public MessageInfoList() {
		setEjbql(EJBQL);
		setAttribute("messageInfo.messageInfoId");
	}

	public MessageInfo getMessageInfo() {
		return messageInfo;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
}
