package cn.dmdl.stl.hospitalbudget.execute.session;

import java.util.Arrays;

import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.common.session.CriterionEntityQuery;
import cn.dmdl.stl.hospitalbudget.execute.entity.EarnConfirm;

@Name("earnConfirmList")
public class EarnConfirmList extends CriterionEntityQuery<EarnConfirm> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8623215853309271622L;

	private static final String EJBQL = "select earnConfirm from EarnConfirm earnConfirm where earnConfirm.state = 0 ";

	private static final String[] RESTRICTIONS = { "lower(earnConfirm.receiptsCode) like lower(concat(#{earnConfirmList.earnConfirm.receiptsCode},'%'))", "lower(earnConfirm.reimbursementMan) like lower(concat(#{earnConfirmList.earnConfirm.reimbursementMan},'%'))", "lower(earnConfirm.voucherCode) like lower(concat(#{earnConfirmList.earnConfirm.voucherCode},'%'))", "lower(earnConfirm.paymentUnit) like lower(concat(#{earnConfirmList.earnConfirm.paymentUnit},'%'))", "lower(earnConfirm.invoiceCode) like lower(concat(#{earnConfirmList.earnConfirm.invoiceCode},'%'))", "lower(earnConfirm.chequeCode) like lower(concat(#{earnConfirmList.earnConfirm.chequeCode},'%'))", "lower(earnConfirm.remark) like lower(concat(#{earnConfirmList.earnConfirm.remark},'%'))", };

	private EarnConfirm earnConfirm = new EarnConfirm();

	public EarnConfirmList() {
		setEjbql(EJBQL);
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
		setMaxResults(25);
	}

	public EarnConfirm getEarnConfirm() {
		return earnConfirm;
	}
}
