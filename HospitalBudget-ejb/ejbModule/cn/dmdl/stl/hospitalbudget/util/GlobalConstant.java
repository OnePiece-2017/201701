package cn.dmdl.stl.hospitalbudget.util;

import static org.jboss.seam.ScopeType.APPLICATION;
import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;

/**
 * 全局常量
 */
@Name(value = "globalConstant")
@Scope(value = APPLICATION)
@Install(precedence = BUILT_IN)
@Startup
public class GlobalConstant implements Serializable {

	private static final long serialVersionUID = 1L;

	/** root角色 */
	public static final int ROLE_OF_ROOT = 1;

	/** 一些常用的list数组 */
	private static final List<Object[]> SOMETHING_LIST = new ArrayList<Object[]>();

	@Create
	public void init() {
		SOMETHING_LIST.add(new Object[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 });
	}

	public int getRoleOfRoot() {
		return ROLE_OF_ROOT;
	}

	public static List<Object[]> getSomethingList() {
		return SOMETHING_LIST;
	}

}
