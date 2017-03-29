package cn.dmdl.stl.hospitalbudget.execute.session;


import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;

import cn.dmdl.stl.hospitalbudget.execute.entity.EarnConfirmProduct;

import java.util.Arrays;

@Name("earnConfirmProductList")
public class EarnConfirmProductList extends EntityQuery<EarnConfirmProduct> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4858248276151968022L;

	private static final String EJBQL = "select earnConfirmProduct from EarnConfirmProduct earnConfirmProduct";

	private static final String[] RESTRICTIONS = { "lower(earnConfirmProduct.remark) like lower(concat(#{earnConfirmProductList.earnConfirmProduct.remark},'%'))", "lower(earnConfirmProduct.fileUrl) like lower(concat(#{earnConfirmProductList.earnConfirmProduct.fileUrl},'%'))", };

	private EarnConfirmProduct earnConfirmProduct = new EarnConfirmProduct();

	public EarnConfirmProductList() {
		setEjbql(EJBQL);
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
		setMaxResults(25);
	}

	public EarnConfirmProduct getEarnConfirmProduct() {
		return earnConfirmProduct;
	}
}
