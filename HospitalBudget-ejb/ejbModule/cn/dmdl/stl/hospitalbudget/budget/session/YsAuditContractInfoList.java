package cn.dmdl.stl.hospitalbudget.budget.session;

import cn.dmdl.stl.hospitalbudget.budget.entity.*;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;
import java.util.Arrays;

@Name("ysAuditContractInfoList")
public class YsAuditContractInfoList extends EntityQuery<YsAuditContractInfo> {

	private static final String EJBQL = "select ysAuditContractInfo from YsAuditContractInfo ysAuditContractInfo";

	private static final String[] RESTRICTIONS = {
			"lower(ysAuditContractInfo.year) like lower(concat(#{ysAuditContractInfoList.ysAuditContractInfo.year},'%'))",
			"lower(ysAuditContractInfo.auditTitle) like lower(concat(#{ysAuditContractInfoList.ysAuditContractInfo.auditTitle},'%'))",
			"lower(ysAuditContractInfo.auditRemark) like lower(concat(#{ysAuditContractInfoList.ysAuditContractInfo.auditRemark},'%'))",
			"lower(ysAuditContractInfo.attachment) like lower(concat(#{ysAuditContractInfoList.ysAuditContractInfo.attachment},'%'))", };

	private YsAuditContractInfo ysAuditContractInfo = new YsAuditContractInfo();

	public YsAuditContractInfoList() {
		setEjbql(EJBQL);
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
		setMaxResults(25);
	}

	public YsAuditContractInfo getYsAuditContractInfo() {
		return ysAuditContractInfo;
	}
}
