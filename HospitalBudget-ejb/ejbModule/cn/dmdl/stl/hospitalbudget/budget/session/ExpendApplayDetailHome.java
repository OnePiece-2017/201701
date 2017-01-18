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
	private Integer expendApplayId;
	private JSONObject saveResult;
	private Object[] expendApplyInfo = new Object[16];
	private List<Object[]> executeList;
	
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
	public void getAttendProject() {
		CommonFinder commonFinder = new CommonFinder();
		SimpleDateFormat sdfday = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat sdfs = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		JSONArray resultSet = new JSONArray();
		Object[] expendFirst = new Object[4];
		StringBuffer dataSql = new StringBuffer();
		dataSql.append(" SELECT");
		dataSql.append(" eai.expend_apply_code,");//0单据编号
		dataSql.append(" eai.`year`,");//1执行年份
		dataSql.append(" ydi.the_value as depart_name,");//2报销部门
		dataSql.append(" uie1.fullname as apply_name,");//3报销人
		dataSql.append(" eai.funds_source,");//4资金来源
		dataSql.append(" eai.recive_company,");//5收款单位
		dataSql.append(" eai.invoice_num,");//6发票号
		dataSql.append(" ycp.the_value as project_name,");//7项目名称
		dataSql.append(" ycpe.the_value as sub_project_name,");//8子项目名称
		dataSql.append(" eai.expend_money,");//9支出金额
		dataSql.append(" neboi.project_amount,");//10年度预算金额
		dataSql.append(" neboi.now_amout,");//11可支出金额
		dataSql.append(" eai.expend_time,");//12支出时间
		dataSql.append(" eai.insert_time, ");//13申请时间
		dataSql.append(" uie2.fullname  as insert_name,");//14提交人
		dataSql.append(" eai.`comment`  ");//15备注
		dataSql.append(" FROM expend_apply_info eai");
		dataSql.append(" LEFT JOIN normal_expend_budget_order_info neboi ON eai.normal_expend_budget_order_id = neboi.normal_expend_budget_order_id ");
		dataSql.append(" LEFT JOIN ys_convention_project ycp on ycp.the_id=neboi.normal_project_id ");
		dataSql.append(" left join ys_convention_project_extend ycpe on ycpe.the_id = neboi.sub_project_id ");
		dataSql.append(" LEFT JOIN ys_department_info ydi on eai.department_info_id=ydi.the_id ");
		dataSql.append(" LEFT JOIN user_info ui on eai.applay_user_id=ui.user_info_id ");
		dataSql.append(" LEFT JOIN user_info_extend uie1 on uie1.user_info_extend_id=ui.user_info_extend_id ");
		dataSql.append(" LEFT JOIN user_info ui1 on ui1.user_info_id=eai.insert_user ");
		dataSql.append(" LEFT JOIN user_info_extend uie2 on uie2.user_info_extend_id=ui1.user_info_extend_id ");
		dataSql.append(" where eai.deleted=0 and eai.expend_apply_info_id=").append(expendApplayId);
		dataSql.insert(0, "select * from (").append(") as recordset");// 解决找不到列
		List<Object[]> dataList = getEntityManager().createNativeQuery(dataSql.toString()).getResultList();
		if (dataList != null && dataList.size() > 0) {
			expendApplyInfo = dataList.get(0);
			expendApplyInfo[4] = commonFinder.getMoneySource(Integer.parseInt(expendApplyInfo[4].toString()));
			expendApplyInfo[11] = Double.parseDouble(expendApplyInfo[9].toString()) + Double.parseDouble(expendApplyInfo[11].toString());
			try {
				expendApplyInfo[12] =  sdfday.format(sdfday.parse(expendApplyInfo[12].toString()));
				expendApplyInfo[13] =  sdfs.format(sdfs.parse(expendApplyInfo[13].toString()));
			} catch (ParseException e) {
				e.printStackTrace();
			}
			expendFirst[0] = expendApplyInfo[13];
			expendFirst[1] = expendApplyInfo[14];
			expendFirst[2] = "提交申请";
		}

		//查询步骤
		//1查询订单编号
		ExpendApplyInfo expendApplyInfo = getEntityManager().find(ExpendApplyInfo.class, expendApplayId);
		String orderSn = expendApplyInfo.getOrderSn();
		//2根据订单编号查询订单流程
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


	public Integer getExpendApplayId() {
		return expendApplayId;
	}

	public void setExpendApplayId(Integer expendApplayId) {
		this.expendApplayId = expendApplayId;
	}



	public List<Object[]> getExecuteList() {
		return executeList;
	}



	public void setExecuteList(List<Object[]> executeList) {
		this.executeList = executeList;
	}

	
	
}
