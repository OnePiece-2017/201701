package cn.dmdl.stl.hospitalbudget.budget.session;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.budget.entity.ExpendApplyInfo;
import cn.dmdl.stl.hospitalbudget.budget.entity.ExpendApplyProject;
import cn.dmdl.stl.hospitalbudget.budget.entity.ExpendConfirmProject;
import cn.dmdl.stl.hospitalbudget.common.session.CriterionEntityHome;
import cn.dmdl.stl.hospitalbudget.util.CommonFinder;

@Name("expendConfrimDetailHome")
public class ExpendConfrimDetailHome extends CriterionEntityHome<Object> {

	private static final long serialVersionUID = 1L;
	private Integer expendConfrimProjectId;
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
		Object[] expendFirst = new Object[4];
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
		dataSql.append(" eai.`comment`, eci.total_money as totalMoney,eci.confirm_time,eai.insert_time,uie3.fullname  as insert_name ");//10  11 12备注
		dataSql.append(" FROM expend_confirm_project ecp ");
		dataSql.append(" LEFT JOIN expend_apply_project eap ON ecp.expend_apply_project_id = eap.expend_apply_project_id ");
		dataSql.append(" LEFT JOIN expend_apply_info eai on eap.expend_apply_info_id=eai.expend_apply_info_id ");
		dataSql.append(" LEFT JOIN expend_confirm_info eci on eci.expend_confirm_info_id= ecp.expend_confirm_info_id ");
		dataSql.append(" LEFT JOIN user_info ui on eai.applay_user_id=ui.user_info_id ");
		dataSql.append(" LEFT JOIN user_info_extend uie on uie.user_info_extend_id=ui.user_info_extend_id ");
		dataSql.append(" LEFT JOIN user_info ui2 ON eai.register = ui2.user_info_id ");
		dataSql.append(" LEFT JOIN user_info_extend uie2 ON uie2.user_info_extend_id = ui2.user_info_extend_id ");
		dataSql.append(" LEFT JOIN user_info ui3 ON eai.insert_user = ui3.user_info_id ");
		dataSql.append(" LEFT JOIN user_info_extend uie3 ON uie3.user_info_extend_id = ui3.user_info_extend_id ");
		dataSql.append(" where eai.deleted=0 and ecp.expend_confirm_project_id=").append(expendConfrimProjectId);
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
			try {
				expendFirst[0] = sdfs.format(sdfs.parse(expendApplyInfo[13].toString()));
			} catch (ParseException e) {
				
			}
			expendFirst[1] = expendApplyInfo[14];
			expendFirst[2] = "支出申请";
		}
		//查询流程
		ExpendConfirmProject ecp = getEntityManager().find(ExpendConfirmProject.class, expendConfrimProjectId);
		ExpendApplyProject eap = getEntityManager().find(ExpendApplyProject.class, ecp.getExpend_apply_project_id());
		ExpendApplyInfo expendApplyInfo = getEntityManager().find(ExpendApplyInfo.class, eap.getExpendApplyInfoId());
		String orderSn = expendApplyInfo.getOrderSn();
		
		//2查询流程
		executeList.add(expendFirst);
		StringBuffer  step = new StringBuffer();
		step.append("SELECT	pst.step_name,	uie.fullname,	task_order.order_status,task_order.insert_time ");
		step.append(" FROM	task_order ");
		step.append(" LEFT JOIN process_step_info pst ON task_order.process_step_info_id = pst.process_step_info_id ");
		step.append(" LEFT JOIN user_info ui ON task_order.edit_user_id = ui.user_info_id ");
		step.append(" LEFT JOIN user_info_extend uie ON uie.user_info_extend_id = ui.user_info_extend_id ");
		step.append(" WHERE	task_order.order_sn = '");
		step.append(orderSn);
		step.append("' ORDER BY task_order.insert_time ");
		List<Object[]> steplist = getEntityManager().createNativeQuery(step.toString()).getResultList();
		if(steplist.size() > 1){
			for(Object[] obj : steplist){
				Object[] stepObj = new Object[4];
				try {
					stepObj[0] = sdfs.format(sdfs.parse(obj[3].toString()));
				} catch (ParseException e) {
					stepObj[0] = obj[3];
				}
				stepObj[1] = obj[1];
				stepObj[2] = obj[0];
				stepObj[3] = getOrderStatusName(obj[2]);
				executeList.add(stepObj);
			}
		}

		//查询项目列表
		//查询项目列表
		StringBuffer projectSql = new StringBuffer();
		projectSql.append(" SELECT ");
		projectSql.append(" up.the_value, ");//0
		projectSql.append(" expi.budget_amount, ");//1
		projectSql.append(" ecp.expend_befor_frozen, ");//2
		projectSql.append(" ecp.expend_befor_surplus, ");//3
		projectSql.append(" ecp.confirm_money, ");//4
		projectSql.append(" ecp.project_id, ");//5
		projectSql.append(" fs.the_value as source_name, ");//6
		projectSql.append(" gp.the_value as generic_name, ");//7
		projectSql.append(" ecp.generic_project_id, ");//8
		projectSql.append(" fs2.the_value as source_name2, ");//9
		projectSql.append(" nepi.budget_amount as amount2,eap.attachment ");//10
		projectSql.append(" FROM expend_confirm_project ecp ");
		projectSql.append(" LEFT JOIN expend_confirm_info eci on eci.expend_confirm_info_id= ecp.expend_confirm_info_id ");
		projectSql.append(" LEFT JOIN routine_project up on up.the_id=ecp.project_id ");
		projectSql.append(" LEFT JOIN normal_expend_plan_info expi on  expi.project_id=ecp.project_id and expi.`year` = eci.`year` ");
		projectSql.append(" LEFT JOIN ys_funds_source fs on up.funds_source_id=fs.the_id ");
		projectSql.append(" LEFT JOIN generic_project gp on gp.the_id=ecp.generic_project_id");
		projectSql.append(" LEFT JOIN normal_expend_plan_info nepi ON nepi.generic_project_id = ecp.generic_project_id AND nepi.`year` = eci.`year` ");
		projectSql.append(" LEFT JOIN ys_funds_source fs2 ON gp.funds_source_id = fs2.the_id ");
		projectSql.append(" LEFT JOIN expend_apply_project eap on ecp.expend_apply_project_id=eap.expend_apply_project_id ");
		projectSql.append(" where  ");
		projectSql.append(" ecp.expend_confirm_project_id=").append(expendConfrimProjectId);
		List<Object[]> list = getEntityManager().createNativeQuery("select * from (" + projectSql.toString() + ") as test").getResultList();
		if(list.size() > 0){
			for(Object[] obj : list){
				Object[] projectDetail = new Object[9];
				if(null != obj[5] && null == obj[8]){
					projectDetail[0] = obj[0];
					BigDecimal bd1 = new BigDecimal(Float.parseFloat(obj[1].toString()));
					projectDetail[1] = bd1.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString();
					BigDecimal bd2 = new BigDecimal(Float.parseFloat(obj[2].toString()));
					projectDetail[2] = bd2.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString();
					BigDecimal bd3 = new BigDecimal(Float.parseFloat(obj[3].toString()));
					projectDetail[3] = bd3.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString();
					BigDecimal bd4 = new BigDecimal(Float.parseFloat(obj[4].toString()));
					projectDetail[4] = bd4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString();
					projectDetail[5] = obj[6];
					projectDetail[6] = "";
					projectDetail[7] = obj[5];
					projectDetail[8] = obj[11];
					projectList.add(projectDetail);
				}else{
					projectDetail[0] = obj[7];
					BigDecimal bd1 = new BigDecimal(Float.parseFloat(obj[10].toString()));
					projectDetail[1] = bd1.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString();
					BigDecimal bd2 = new BigDecimal(Float.parseFloat(obj[2].toString()));
					projectDetail[2] = bd2.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString();
					BigDecimal bd3 = new BigDecimal(Float.parseFloat(obj[3].toString()));
					projectDetail[3] = bd3.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString();
					BigDecimal bd4 = new BigDecimal(Float.parseFloat(obj[4].toString()));
					projectDetail[4] = bd4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString();
					projectDetail[5] = obj[9];
					projectDetail[6] = "";
					projectDetail[7] = obj[8];
					projectDetail[8] = obj[11];
					projectList.add(projectDetail);
				}
				
			}
		}
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


	



	


	public Integer getExpendConfrimProjectId() {
		return expendConfrimProjectId;
	}



	public void setExpendConfrimProjectId(Integer expendConfrimProjectId) {
		this.expendConfrimProjectId = expendConfrimProjectId;
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
