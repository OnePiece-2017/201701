package cn.dmdl.stl.hospitalbudget.budget.session;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;

import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.budget.entity.ExpendApplyInfo;
import cn.dmdl.stl.hospitalbudget.common.session.CriterionEntityHome;
import cn.dmdl.stl.hospitalbudget.util.CommonFinder;

@Name("expendInfoDetail")
public class ExpendInfoDetail extends CriterionEntityHome<ExpendApplyInfo>{
	
	private Integer expendApplyInfoId;//编辑使用-申请单号
	private List<Object[]> projectList;//主项目列表
	private Object[] expendApplyInfo = new Object[14];//公共头部
	
	
	/**
	 * 初始化
	 * @param oldTaskOrderId
	 * @param newTaskOrderId
	 */
	@SuppressWarnings("unchecked")
	public void wire(){
		projectList = new ArrayList<Object[]>();
		CommonFinder commonFinder = new CommonFinder();
		SimpleDateFormat sdfday = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat sdfs = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		JSONArray resultSet = new JSONArray();
		StringBuffer dataSql = new StringBuffer();
		dataSql.append(" SELECT");
		dataSql.append(" eai.`year`,");//0执行年份
		dataSql.append(" uie.fullname as applay_name,");//1申请人
		dataSql.append(" eai.expend_apply_code,");//2单据编号
		dataSql.append(" eai.recive_company,");//3收款单位
		dataSql.append(" eai.invoice_num,");//4发票号
		dataSql.append(" eai.summary,");//5摘要
		dataSql.append(" eai.reimbursementer,");//6报销人
		dataSql.append(" eai.apply_time, ");//7申请时间
		dataSql.append(" eai.register_time, ");//8登记时间
		dataSql.append(" uie2.fullname as register, ");//9登记人
		dataSql.append(" eai.`comment`, eai.total_money as totalMoney ");//10  11备注
		dataSql.append(" FROM expend_apply_info eai");
		dataSql.append(" LEFT JOIN user_info ui on eai.applay_user_id=ui.user_info_id ");
		dataSql.append(" LEFT JOIN user_info_extend uie on uie.user_info_extend_id=ui.user_info_extend_id ");
		dataSql.append(" LEFT JOIN user_info ui2 ON eai.register = ui2.user_info_id ");
		dataSql.append(" LEFT JOIN user_info_extend uie2 ON uie2.user_info_extend_id = ui2.user_info_extend_id ");
		dataSql.append(" where eai.deleted=0 and eai.expend_apply_info_id=").append(expendApplyInfoId);
		dataSql.insert(0, "select * from (").append(") as recordset");// 解决找不到列
		List<Object[]> dataList = getEntityManager().createNativeQuery(dataSql.toString()).getResultList();
		if (dataList != null && dataList.size() > 0) {
			expendApplyInfo = dataList.get(0);
			if(null != expendApplyInfo[7] && !expendApplyInfo[7].toString().equals("")){
				try {
					expendApplyInfo[7] = sdfday.format(sdfday.parse(expendApplyInfo[7].toString()));
				} catch (ParseException e) {
					
				}
			}
			if(null != expendApplyInfo[8] && !expendApplyInfo[8].toString().equals("")){
				try {
					expendApplyInfo[8] = sdfday.format(sdfday.parse(expendApplyInfo[8].toString()));
				} catch (ParseException e) {
					
				}
			}
		}
		
		//查询项目列表
		StringBuffer projectSql = new StringBuffer();
		projectSql.append(" SELECT ");
		projectSql.append(" up.the_value as routine_name, ");//0项目名
		projectSql.append(" expi.budget_amount as budget_amount1, ");//1
		projectSql.append(" eap.expend_befor_frozen, ");//2
		projectSql.append(" eap.expend_befor_surplus, ");//3
		projectSql.append(" eap.expend_money as expend_money1, ");//4
		projectSql.append(" eap.project_id, ");//5
		projectSql.append(" fs.the_value AS source_name1, ");//6
		projectSql.append(" gp.the_value as generic_name, ");//7
		projectSql.append(" nepi2.budget_amount as generic_budget_amount2, ");//8
		projectSql.append(" nepi2.budget_amount_frozen as amount_frozen2, ");//9
		projectSql.append(" nepi2.budget_amount_surplus as amount_surplus2, ");//10
		projectSql.append(" eap.expend_money as expend_money2, ");//11
		projectSql.append(" eap.generic_project_id, ");//12
		projectSql.append(" fs2.the_value as source_name2 ");//13
		projectSql.append(" FROM expend_apply_project eap ");
		projectSql.append(" LEFT JOIN expend_apply_info eai ON eap.expend_apply_info_id = eai.expend_apply_info_id ");
		projectSql.append(" LEFT JOIN routine_project up on up.the_id=eap.project_id ");
		projectSql.append(" LEFT JOIN normal_expend_plan_info expi on  expi.project_id=eap.project_id and expi.`year` = eai.`year` ");
		projectSql.append(" LEFT JOIN ys_funds_source fs on up.funds_source_id=fs.the_id ");
		projectSql.append(" LEFT JOIN generic_project gp on gp.the_id = eap.generic_project_id ");
		projectSql.append(" LEFT JOIN normal_expend_plan_info nepi2 on nepi2.generic_project_id=eap.generic_project_id and nepi2.`year`=eai.`year` ");
		projectSql.append(" LEFT JOIN ys_funds_source fs2 ON gp.funds_source_id = fs2.the_id ");
		projectSql.append(" where eap.deleted=0 ");
		projectSql.append(" and eai.expend_apply_info_id=").append(expendApplyInfoId);
		List<Object[]> list = getEntityManager().createNativeQuery("select * from (" + projectSql.toString() + ") as test").getResultList();
		float allMoney = 0f;
		if(list.size() > 0){
			for(Object[] obj : list){
				Object[] projectDetail = new Object[8];
				if(null == obj[12] && null != obj[5]){
					projectDetail[0] = obj[0];
					projectDetail[1] = obj[1];
					projectDetail[2] = obj[2];
					projectDetail[3] = obj[3];
					projectDetail[4] = obj[4];
					allMoney += Float.parseFloat(projectDetail[4].toString());
					projectDetail[5] = obj[6];
					projectDetail[6] = "";
					projectDetail[7] = obj[5];
				}else{
					projectDetail[0] = obj[7];
					projectDetail[1] = obj[8];
					projectDetail[2] = obj[2];
					projectDetail[3] = obj[3];
					projectDetail[4] = obj[11];
					allMoney += Float.parseFloat(projectDetail[4].toString());
					projectDetail[5] = obj[13];
					projectDetail[6] = "";
					projectDetail[7] = obj[12];
				}
				projectList.add(projectDetail);
			}
		}
		
	}
	public Integer getExpendApplyInfoId() {
		return expendApplyInfoId;
	}
	public void setExpendApplyInfoId(Integer expendApplyInfoId) {
		this.expendApplyInfoId = expendApplyInfoId;
	}
	public List<Object[]> getProjectList() {
		return projectList;
	}
	public void setProjectList(List<Object[]> projectList) {
		this.projectList = projectList;
	}
	public Object[] getExpendApplyInfo() {
		return expendApplyInfo;
	}
	public void setExpendApplyInfo(Object[] expendApplyInfo) {
		this.expendApplyInfo = expendApplyInfo;
	}
	
	
	
	
	
}
