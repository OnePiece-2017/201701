package cn.dmdl.stl.hospitalbudget.budget.session;

import java.util.List;

import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.common.session.CriterionEntityHome;

@Name("ysExpendCollectionInfoHome")
public class YsExpendCollectionInfoHome extends CriterionEntityHome<Object> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Integer expendCollectionDeptId;
	
	

	public Integer getExpendCollectionDeptId() {
		return expendCollectionDeptId;
	}

	public void setExpendCollectionDeptId(Integer expendCollectionDeptId) {
		this.expendCollectionDeptId = expendCollectionDeptId;
	}
	
	


}
