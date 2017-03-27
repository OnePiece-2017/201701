package cn.dmdl.stl.hospitalbudget.budget.session;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;

import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.common.session.CriterionEntityHome;
import cn.dmdl.stl.hospitalbudget.common.session.CriterionNativeQuery;
import cn.dmdl.stl.hospitalbudget.util.CommonFinder;

@Name("printExpendConfirm")
public class PrintExpendConfirm extends CriterionEntityHome<Object>{
	
	private static final long serialVersionUID = 4802442131669677231L;
	
	
	private Integer confirmid;
	private Object[] expendApplyInfo = new Object[16];
	
	
	
	@SuppressWarnings("unchecked")
	public void wire(){
		Object[] expendFirst = new Object[4];
		SimpleDateFormat sdfday = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat sdfs = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		StringBuffer dataSql = new StringBuffer();
		dataSql.append(" SELECT");
		dataSql.append(" ysdi.the_value as depart_name,");//0操作人部门
		dataSql.append(" eai.total_money,");//1总金额
		dataSql.append(" eai.recive_company,");//2单位
		dataSql.append(" eai.apply_time,");//3申请时间
		dataSql.append(" eai.summary,");//4发票数量
		dataSql.append(" eai.invoice_num,");//5发票号
		dataSql.append(" rp.the_value as project_name,");//6项目名
		dataSql.append(" gp.the_value as generic_name, ");//7常规项目名
		dataSql.append(" eai.comment ");//7常规项目名
		dataSql.append(" FROM expend_apply_info eai");
		dataSql.append(" LEFT JOIN user_info ui ON eai.insert_user = ui.user_info_id ");
		dataSql.append(" LEFT JOIN user_info_extend uie ON uie.user_info_extend_id = ui.user_info_extend_id ");
		dataSql.append(" LEFT JOIN ys_department_info ysdi ON ui.department_info_id = ysdi.the_id ");
		dataSql.append(" LEFT JOIN expend_apply_project eap on eap.expend_apply_info_id=eai.expend_apply_info_id ");
		dataSql.append(" LEFT JOIN routine_project rp on eap.project_id=rp.the_id  ");
		dataSql.append(" LEFT JOIN generic_project gp on gp.the_id=eap.generic_project_id ");
		dataSql.append(" where eai.deleted=0 and eai.expend_apply_info_id=").append(confirmid);
		dataSql.insert(0, "select * from (").append(") as recordset");// 解决找不到列
		List<Object[]> dataList = getEntityManager().createNativeQuery(dataSql.toString()).getResultList();
		if (dataList != null && dataList.size() > 0) {
			expendApplyInfo = dataList.get(0);
			try {
				expendApplyInfo[3] = sdfday.format(sdfs.parse(expendApplyInfo[3].toString()));
			} catch (ParseException e) {
				
			}
		}
		String project = "";
		for(Object[] obj : dataList){
			if(null == obj[6]){
				project += obj[7].toString() + "、";
			}
			if(null == obj[7]){
				project += obj[6].toString() + "、";
			}
		}
		expendApplyInfo[6] = project.subSequence(0, project.length()-1);
	}



	public Object[] getExpendApplyInfo() {
		return expendApplyInfo;
	}



	public void setExpendApplyInfo(Object[] expendApplyInfo) {
		this.expendApplyInfo = expendApplyInfo;
	}



	public Integer getConfirmid() {
		return confirmid;
	}

	public void setConfirmid(Integer confirmid) {
		this.confirmid = confirmid;
	}
	
	
}
