package cn.dmdl.stl.hospitalbudget.util;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Credentials;
import org.jboss.seam.security.Identity;

@Stateful
@Name("commonFinder")
public class CommonFinder implements CommonFinderLocal {

	@In(create = true)
	SessionToken sessionToken;

	@Logger
	private Log log;

	@In
	Identity identity;

	@In
	Credentials credentials;

	@PersistenceContext
	EntityManager entityManager;

	@In
	protected FacesMessages facesMessages;

	@Remove
	public void remove() {
	}

	@Destroy
	public void destroy() {
	}

	/** 获取类简介 */
	public String gainClassIntroduction() {
		String result = "这个类主要针对副本项目的业务逻辑相关联的常用查询结果返回";
		return result;
	}

	/** 获取项目流程类型名称 */
	public String gainProjectProcessTypeName(int type) {
		String result = "";
		if (type == 1) {
			result = "常规收入预算";
		} else if (type == 2) {
			result = "常规支出预算";
		} else if (type == 3) {
			result = "常规收入执行";
		} else if (type == 4) {
			result = "常规支出执行";
		}
		return result;
	}

	/** 获取任务类型名称 */
	public String gainTaskTypeName(int type) {
		String result = "";
		if (type == 1) {
			result = "常规预算收入";
		} else if (type == 2) {
			result = "常规预算支出";
		}
		return result;
	}

	/** 获取订单状态名称 */
	public String gainOrderStatusName(int type) {
		String result = "";
		if (type == 0) {
			result = "待处理";
		} else if (type == 1) {
			result = "已通过";
		} else if (type == 2) {
			result = "已退回";
		} else if (type == 3) {
			result = "退单待处理";
		} else if (type == 4) {
			result = "退单已处理";
		} else if (type == 9) {
			result = "订单已完成";
		}
		return result;
	}

	public String getMoneySource(int type) {
		String result = "";
		if(type == 1){
			result = "自有资金";
		}
		return result;
	}

}
