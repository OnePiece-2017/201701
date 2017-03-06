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
		dataSql.append(" uie1.fullname,");//6报销人
		dataSql.append(" eai.apply_time, ");//7申请时间
		dataSql.append(" eai.register_time, ");//8登记时间
		dataSql.append(" uie2.fullname as register, ");//9登记人
		dataSql.append(" eai.`comment`, eai.total_money as totalMoney ");//10  11备注
		dataSql.append(" FROM expend_apply_info eai");
		dataSql.append(" LEFT JOIN user_info ui on eai.applay_user_id=ui.user_info_id ");
		dataSql.append(" LEFT JOIN user_info_extend uie on uie.user_info_extend_id=ui.user_info_extend_id ");
		dataSql.append(" LEFT JOIN user_info ui1 on eai.reimbursementer=ui1.user_info_id ");
		dataSql.append(" LEFT JOIN user_info_extend uie1 on uie1.user_info_extend_id=ui1.user_info_extend_id ");
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
		projectSql.append(" up.the_value, ");
		projectSql.append(" expi.budget_amount, ");
		projectSql.append(" expi.budget_amount_frozen, ");
		projectSql.append(" expi.budget_amount_surplus, ");
		projectSql.append(" eap.expend_money, ");
		projectSql.append(" eap.project_id,fs.the_value as source_name ");
		projectSql.append(" FROM expend_apply_project eap ");
		projectSql.append(" LEFT JOIN expend_apply_info eai ON eap.expend_apply_info_id = eai.expend_apply_info_id ");
		projectSql.append(" LEFT JOIN usual_project up on up.the_id=eap.project_id ");
		projectSql.append(" LEFT JOIN normal_expend_plan_info expi on  expi.project_id=eap.project_id and expi.`year` = eai.`year` ");
		projectSql.append(" LEFT JOIN ys_funds_source fs on up.funds_source_id=fs.the_id ");
		projectSql.append(" where eap.deleted=0 ");
		projectSql.append(" and eai.expend_apply_info_id=").append(expendApplyInfoId);
		List<Object[]> list = getEntityManager().createNativeQuery("select * from (" + projectSql.toString() + ") as test").getResultList();
		float allMoney = 0f;
		if(list.size() > 0){
			for(Object[] obj : list){
				Object[] projectDetail = new Object[8];
				projectDetail[0] = obj[0];
				projectDetail[1] = obj[1];
				projectDetail[2] = Float.parseFloat(obj[2].toString()) - Float.parseFloat(obj[4].toString());
				projectDetail[3] = Float.parseFloat(obj[3].toString()) + Float.parseFloat(obj[4].toString());;
				projectDetail[4] = obj[4];
				allMoney += Float.parseFloat(projectDetail[4].toString());
				projectDetail[5] = obj[6];
				projectDetail[6] = "";
				projectDetail[7] = obj[5];
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
