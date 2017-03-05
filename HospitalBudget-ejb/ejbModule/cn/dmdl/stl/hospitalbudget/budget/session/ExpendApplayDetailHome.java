package cn.dmdl.stl.hospitalbudget.budget.session;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.budget.entity.ExpendApplyInfo;
import cn.dmdl.stl.hospitalbudget.common.session.CriterionEntityHome;
import cn.dmdl.stl.hospitalbudget.util.CommonFinder;

@Name("expendApplayDetailHome")
public class ExpendApplayDetailHome extends CriterionEntityHome<Object> {

	private static final long serialVersionUID = 1L;
	private Integer expendApplayProjectId;
	private JSONObject saveResult;
	private Object[] expendApplyInfo = new Object[16];
	private List<Object[]> executeList;
	private List<Object[]> projectList;//项目列表
	
	/**
	 * 初始化
	 * @param oldTaskOrderId
	 * @param newTaskOrderId
	 */
	public void wire(){
		executeList = new ArrayList<Object[]>();
		getAttendProject();
	}
	


	@SuppressWarnings("unchecked")
	public List<Object[]> getAttendProject() {
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
		dataSql.append(" eai.finace_account_name,");//3账务账名
		dataSql.append(" eai.recive_company,");//4收款单位
		dataSql.append(" eai.invoice_num,");//5发票号
		dataSql.append(" eai.summary,");//6摘要
		dataSql.append(" uie1.fullname,");//7报销人
		dataSql.append(" eai.apply_time, ");//8申请时间
		dataSql.append(" eai.register_time, ");//9登记时间
		dataSql.append(" uie2.fullname as register, ");//10登记人
		dataSql.append(" eai.`comment`, eai.finace_account_name as totalMoney ");//11  12备注
		dataSql.append(" FROM expend_apply_info eai");
		dataSql.append(" LEFT JOIN expend_apply_project eap on eai.expend_apply_info_id= eap.expend_apply_info_id ");
		dataSql.append(" LEFT JOIN user_info ui on eai.applay_user_id=ui.user_info_id ");
		dataSql.append(" LEFT JOIN user_info_extend uie on uie.user_info_extend_id=ui.user_info_extend_id ");
		dataSql.append(" LEFT JOIN user_info ui1 on eai.reimbursementer=ui1.user_info_id ");
		dataSql.append(" LEFT JOIN user_info_extend uie1 on uie1.user_info_extend_id=ui1.user_info_extend_id ");
		dataSql.append(" LEFT JOIN user_info ui2 ON eai.register = ui2.user_info_id ");
		dataSql.append(" LEFT JOIN user_info_extend uie2 ON uie2.user_info_extend_id = ui2.user_info_extend_id ");
		dataSql.append(" where eai.deleted=0 and eap.expend_apply_project_id=").append(expendApplayProjectId);
		dataSql.insert(0, "select * from (").append(") as recordset");// 解决找不到列
		List<Object[]> dataList = getEntityManager().createNativeQuery(dataSql.toString()).getResultList();
		if (dataList != null && dataList.size() > 0) {
			expendApplyInfo = dataList.get(0);
			if(null != expendApplyInfo[9] && !expendApplyInfo[9].toString().equals("")){
				try {
					expendApplyInfo[9] = sdfday.format(sdfday.parse(expendApplyInfo[9].toString()));
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
		projectSql.append(" eap.budget_append_expend, ");//5
		projectSql.append(" eap.budget_adjustment_append, ");
		projectSql.append(" eap.budget_adjestment_cut, ");
		projectSql.append(" eap.append_budget_paid, ");
		projectSql.append(" eap.append_budget_money, ");
		projectSql.append(" eap.expend_time, ");//10
		projectSql.append(" eap.`comment`,eap.project_id ");
		projectSql.append(" FROM expend_apply_project eap ");
		projectSql.append(" LEFT JOIN expend_apply_info eai ON eap.expend_apply_info_id = eai.expend_apply_info_id ");
		projectSql.append(" LEFT JOIN usual_project up on up.the_id=eap.project_id ");
		projectSql.append(" LEFT JOIN normal_expend_plan_info expi on  expi.project_id=eap.project_id and expi.`year` = eai.`year` ");
		projectSql.append(" where eap.deleted=0 ");
		projectSql.append(" and eai.task_order_id=").append(taskOrderId);
		List<Object[]> list = getEntityManager().createNativeQuery(projectSql.toString()).getResultList();
		float allMoney = 0f;
		if(list.size() > 0){
			for(Object[] obj : list){
				Object[] projectDetail = new Object[14];
				projectDetail[0] = obj[0];
				projectDetail[1] = obj[1];
				projectDetail[2] = Float.parseFloat(obj[2].toString()) - Float.parseFloat(obj[4].toString());
				projectDetail[3] = Float.parseFloat(obj[3].toString()) + Float.parseFloat(obj[4].toString());;
				projectDetail[4] = obj[4];
				allMoney += Float.parseFloat(projectDetail[4].toString());
				try {
					projectDetail[5] = sdfday.format(sdfday.parse(obj[10].toString()));
				} catch (ParseException e) {
					projectDetail[5] = obj[10].toString();
				}
				projectDetail[6] = obj[11] == null ? "" : obj[11].toString();
				projectDetail[7] = "";
				projectDetail[8] = obj[12];
				projectDetail[9] = obj[5];
				projectDetail[10] = obj[6];
				projectDetail[11] = obj[7];
				projectDetail[12] = obj[8];
				projectDetail[13] = obj[9];
				projectList.add(projectDetail);
			}
		}
		expendApplyInfo[12] = allMoney;
		return resultSet;
	}
	
	public String getOrderStatusName(Object obj){
		String name = "";
		int status = Integer.parseInt(obj.toString());
		if(status == 0){
			name = "申请";
		}else if(status == 1){
			name = "通过";
		}else if(status == 2){
			name = "不通过";
		}else if(status == 3){
			name = "驳回待处理";
		}else if(status == 4){
			name = "驳回重新提交";
		}else if(status == 9){
			name = "完成";
		}
		return name;
	}
	public JSONArray getTableData() {
		return null;
	}


	public JSONObject getSaveResult() {
		return saveResult;
	}


	public Object[] getExpendApplyInfo() {
		return expendApplyInfo;
	}

	public void setExpendApplyInfo(Object[] expendApplyInfo) {
		this.expendApplyInfo = expendApplyInfo;
	}


	



	public Integer getExpendApplayProjectId() {
		return expendApplayProjectId;
	}



	public void setExpendApplayProjectId(Integer expendApplayProjectId) {
		this.expendApplayProjectId = expendApplayProjectId;
	}



	public List<Object[]> getExecuteList() {
		return executeList;
	}



	public void setExecuteList(List<Object[]> executeList) {
		this.executeList = executeList;
	}



	public List<Object[]> getProjectList() {
		return projectList;
	}



	public void setProjectList(List<Object[]> projectList) {
		this.projectList = projectList;
	}

	
	
}
