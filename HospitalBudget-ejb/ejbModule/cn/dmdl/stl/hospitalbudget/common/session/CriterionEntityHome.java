package cn.dmdl.stl.hospitalbudget.common.session;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.framework.EntityHome;
import org.jboss.seam.log.Log;

import cn.dmdl.stl.hospitalbudget.util.SessionToken;

public class CriterionEntityHome<E> extends EntityHome<E> {

	private static final long serialVersionUID = 1L;

	@In
	protected SessionToken sessionToken;

	@In
	protected FacesMessages facesMessages;

	@Logger
	protected Log log;

	private String message;// 消息

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * 中断代码，插入目标代码如下：
	 * 
	 * <pre>
	 * if (interruptCode())
	 * 	return null;
	 * </pre>
	 */
	public boolean interruptCode() {
		setMessage("中断代码");
		return true;
	}

}
