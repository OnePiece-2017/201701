package cn.dmdl.stl.hospitalbudget.budget.session;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


























import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.admin.entity.UserInfo;
import cn.dmdl.stl.hospitalbudget.budget.entity.ExpendApplyInfo;
import cn.dmdl.stl.hospitalbudget.budget.entity.ExpendApplyProject;
import cn.dmdl.stl.hospitalbudget.budget.entity.ExpendConfirmInfo;
import cn.dmdl.stl.hospitalbudget.budget.entity.ExpendConfirmProject;
import cn.dmdl.stl.hospitalbudget.budget.entity.NormalExpendBudgetOrderInfo;
import cn.dmdl.stl.hospitalbudget.budget.entity.NormalExpendPlantInfo;
import cn.dmdl.stl.hospitalbudget.budget.entity.TaskOrder;
import cn.dmdl.stl.hospitalbudget.budget.entity.TaskUser;
import cn.dmdl.stl.hospitalbudget.common.session.CriterionEntityHome;
import cn.dmdl.stl.hospitalbudget.util.Assit;
import cn.dmdl.stl.hospitalbudget.util.SqlServerJDBCUtil;

@Name("expendApplyConfirmHome")
public class ExpendApplyConfirmHome extends CriterionEntityHome<ExpendApplyInfo>{
	private static final long serialVersionUID = 1L;
	private Integer expendConfirmId;
	private Float totalMoney;
	private List<Object[]> projectList;//主项目列表
	private String projectJson;//json
	private String confirmTime;
	private JSONObject saveResult;
	
	private ExpendApplyInfo expendApplyInfo;//申请单
	private String applyUser;//申请人
	private String reimbursementer;//报销人
	private String register;//登记人
	private String applyTime;//
	private String confirmIds;//批量确认id
	@SuppressWarnings("unchecked")
	public void wire(){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		projectList = new ArrayList<Object[]>();
		expendApplyInfo = new ExpendApplyInfo();
		if(null != expendConfirmId && expendConfirmId != 0){
			ExpendConfirmInfo eci = getEntityManager().find(ExpendConfirmInfo.class, expendConfirmId);
			expendApplyInfo = getEntityManager().find(ExpendApplyInfo.class, eci.getExpend_apply_info_id());
			applyTime = sdf.format(expendApplyInfo.getApplyTime());
			totalMoney = eci.getTotalMoney();
			StringBuffer userSql = new StringBuffer();
			userSql.append(" select uie1.fullname as applyer_name, ");
			userSql.append(" eai.reimbursementer as reimbursementer_name,uie3.fullname as register_name ");
			userSql.append(" from expend_apply_info eai  ");
			userSql.append(" LEFT JOIN user_info ui1 on eai.applay_user_id=ui1.user_info_id ");
			userSql.append(" LEFT JOIN user_info_extend uie1 on ui1.user_info_extend_id=uie1.user_info_extend_id ");
			userSql.append(" LEFT JOIN user_info ui3 on eai.register=ui3.user_info_id ");
			userSql.append(" LEFT JOIN user_info_extend uie3 on ui3.user_info_extend_id=uie3.user_info_extend_id ");
			userSql.append(" where eai.expend_apply_info_id =  ").append(expendApplyInfo.getExpendApplyInfoId());
			List<Object[]> userList = getEntityManager().createNativeQuery("select * from (" + userSql.toString() + ") as test").getResultList();
			if(userList.size() > 0){
				Object[] user = userList.get(0);
				applyUser = user[0] == null ? "" : user[0].toString();
				reimbursementer = user[1] == null ? "" : user[1].toString();
				register = user[2] == null ? "" : user[2].toString();
			}
			//项目列表
			StringBuffer projectSql = new StringBuffer();
			projectSql.append(" SELECT ");
			projectSql.append(" up.the_value as routine_name, ");//0
			projectSql.append(" expi.budget_amount as budget_amount1, ");//1
			projectSql.append(" expi.budget_amount_frozen as amount_frozen1, ");//2
			projectSql.append(" expi.budget_amount_surplus as amount_surplus1, ");//3
			projectSql.append(" eap.expend_money as expend_money1, ");//4
			projectSql.append(" ecp.expend_confirm_project_id, ");//5
			projectSql.append(" fs.the_value AS source_name1, ");//6
			projectSql.append(" gp.the_value as generic_name, ");//7
			projectSql.append(" nepi.budget_amount AS budget_amount2, ");//8
			projectSql.append(" nepi.budget_amount_frozen AS amount_frozen2, ");//9
			projectSql.append(" nepi.budget_amount_surplus AS amount_surplus2,");//10
			projectSql.append(" fs2.the_value AS source_name2,ecp.project_id,ecp.generic_project_id,eap.attachment ");//11 12 13
			projectSql.append(" FROM expend_confirm_project ecp ");
			projectSql.append(" LEFT JOIN expend_apply_project eap ON ecp.expend_apply_project_id = eap.expend_apply_project_id ");
			projectSql.append(" LEFT JOIN routine_project up ON ecp.project_id=up.the_id ");
			projectSql.append(" LEFT JOIN expend_confirm_info eci on ecp.expend_confirm_info_id=eci.expend_confirm_info_id ");
			projectSql.append(" LEFT JOIN normal_expend_plan_info expi on expi.`year`=eci.`year` and ecp.project_id=expi.project_id ");
			projectSql.append(" LEFT JOIN ys_funds_source fs ON up.funds_source_id = fs.the_id ");
			projectSql.append(" LEFT JOIN generic_project gp ON ecp.generic_project_id = gp.the_id ");
			projectSql.append(" LEFT JOIN normal_expend_plan_info nepi ON nepi.`year` = eci.`year` AND ecp.generic_project_id = nepi.generic_project_id ");
			projectSql.append(" LEFT JOIN ys_funds_source fs2 ON gp.funds_source_id = fs2.the_id ");
			projectSql.append(" where ecp.expend_confirm_info_id= ").append(expendConfirmId);
			List<Object[]> list = getEntityManager().createNativeQuery("select * from (" + projectSql.toString() + ") as test").getResultList();
			if(list.size() > 0){
				for(Object[] obj : list){
					Object[] projectDetail = new Object[14];
					if(null != obj[12] && null == obj[13]){
						projectDetail[0] = obj[0];
						projectDetail[1] = obj[1];
						projectDetail[2] = obj[2];
						projectDetail[3] = obj[3];
						projectDetail[4] = obj[4];
						projectDetail[5] = obj[6];
						projectDetail[6] = "";
						projectDetail[7] = obj[5];
						projectDetail[8] = 1;
						projectDetail[9] = obj[14];
						projectList.add(projectDetail);
					}else{
						projectDetail[0] = obj[7];
						projectDetail[1] = obj[8];
						projectDetail[2] = obj[9];
						projectDetail[3] = obj[10];
						projectDetail[4] = obj[4];
						projectDetail[5] = obj[11];
						projectDetail[6] = "";
						projectDetail[7] = obj[5];
						projectDetail[8] = 2;
						projectDetail[9] = obj[14];
						projectList.add(projectDetail);
					}
				}
			}
		}
	}
	
	
	
	
	/**
	 * 确认提交保存
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String save(){
		saveResult = new JSONObject();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		joinTransaction();
		saveResult.accumulate("invoke_result", "INVOKE_SUCCESS");
		saveResult.accumulate("message", "提交成功！");
		//确认单
		ExpendConfirmInfo eci = getEntityManager().find(ExpendConfirmInfo.class, expendConfirmId);
		eci.setTotalMoney(totalMoney);
		eci.setUpdateTime(new Date());
		eci.setConfirmUser(sessionToken.getUserInfoId());
		try {
			eci.setConfirm_time(sdf.parse(confirmTime));
		} catch (ParseException e) {
			saveResult.element("invoke_result", "INVOKE_FAILURE");
			saveResult.element("message", "操作失败！" + e.getMessage());
		}
		//确认单确认完成
		eci.setConfirm_status(1);
		getEntityManager().merge(eci);
		
		//申请单确认完成
		ExpendApplyInfo eai = getEntityManager().find(ExpendApplyInfo.class, eci.getExpend_apply_info_id());
		eai.setExpendApplyStatus(1);
		getEntityManager().merge(eai);
		if(eai.getExplendSource() == 1){
			Connection conn = SqlServerJDBCUtil.GetConnection();
			PreparedStatement ps = null;
			ResultSet rs = null;
			String updateSql = "update HMMIS_BUDG.dbo.budg_application4expenditure set state=1 and state_date= ? where bill_code=? ";
			try{
				java.util.Date date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()));//获取系统时间
				java.sql.Timestamp date1=new java.sql.Timestamp(date.getTime());//
				ps = conn.prepareStatement(updateSql);
				ps.setTimestamp(1, date1);
				ps.setString(2, eai.getExpendApplyCode());
				ps.executeUpdate();
			}catch(Exception e){
				System.out.println(e.getMessage());
			}finally{
				SqlServerJDBCUtil.CloseAll(conn, ps, rs);
			}
			
		}
		//确认单项目表
		JSONObject jsonObject = JSONObject.fromObject(projectJson);
		JSONArray expendList = jsonObject.getJSONArray("expend_list");
		float allMoney = 0f;
		for(int i = 0;i < expendList.size();i++){
			//获取参数
			JSONObject project = expendList.getJSONObject(i);
			String projectInfoId = project.getString("project_id");
			String projectType = project.getString("project_type");
			String expendMoney = project.getString("expend_money");
			//保存支出申请单详细列表
			ExpendConfirmProject ecp = getEntityManager().find(ExpendConfirmProject.class, Integer.parseInt(projectInfoId));
			ecp.setConfirm_money(Float.parseFloat(expendMoney));
			getEntityManager().merge(ecp);
			
			
			//to-do 预算下达金额减去支出金额
			
			//执行申请后再减掉金额
			StringBuffer moneySql = new StringBuffer();
			if("1".equals(projectType)){
				moneySql.append(" SELECT nepi.normal_expend_plan_id,eap.expend_money ");
				moneySql.append(" FROM expend_confirm_project ecp ");
				moneySql.append(" LEFT JOIN expend_apply_project eap on ecp.expend_apply_project_id=eap.expend_apply_project_id ");
				moneySql.append(" LEFT JOIN expend_confirm_info eci on eci.expend_confirm_info_id=ecp.expend_confirm_info_id ");
				moneySql.append(" LEFT JOIN normal_expend_plan_info nepi on nepi.`year`=eci.`year` and nepi.project_id=ecp.project_id ");
				moneySql.append(" where ecp.expend_confirm_project_id= ").append(projectInfoId);
			}else{
				moneySql.append(" SELECT nepi.normal_expend_plan_id,eap.expend_money ");
				moneySql.append(" FROM expend_confirm_project ecp ");
				moneySql.append(" LEFT JOIN expend_apply_project eap on ecp.expend_apply_project_id=eap.expend_apply_project_id ");
				moneySql.append(" LEFT JOIN expend_confirm_info eci on eci.expend_confirm_info_id=ecp.expend_confirm_info_id ");
				moneySql.append(" LEFT JOIN normal_expend_plan_info nepi on nepi.`year`=eci.`year` and nepi.generic_project_id=ecp.generic_project_id ");
				moneySql.append(" where ecp.expend_confirm_project_id= ").append(projectInfoId);
				
			}
			List<Object[]> plist = getEntityManager().createNativeQuery(moneySql.toString()).getResultList();
			NormalExpendPlantInfo nxpi = getEntityManager().find(NormalExpendPlantInfo.class, Integer.parseInt(plist.get(0)[0].toString()));
			nxpi.setBudgetAmountFrozen(nxpi.getBudgetAmountFrozen()  + Float.parseFloat(expendMoney) );
			nxpi.setBudgetAmountSurplus(nxpi.getBudgetAmountSurplus() - Float.parseFloat(expendMoney));
			getEntityManager().merge(nxpi);
		}
		getEntityManager().flush();
		raiseAfterTransactionSuccessEvent();
		return "ok";
	}


	/**
	 * 驳回提交保存
	 * @return
	 */
	public String confirmReturn(){
		saveResult = new JSONObject();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		joinTransaction();
		saveResult.accumulate("invoke_result", "INVOKE_SUCCESS");
		saveResult.accumulate("message", "驳回成功！");
		//确认单改为驳回状态并且删除
		ExpendConfirmInfo eci = getEntityManager().find(ExpendConfirmInfo.class, expendConfirmId);
		eci.setTotalMoney(totalMoney);
		eci.setUpdateTime(new Date());
		eci.setConfirmUser(sessionToken.getUserInfoId());
		try {
			eci.setConfirm_time(sdf.parse(confirmTime));
		} catch (ParseException e) {
			saveResult.element("invoke_result", "INVOKE_FAILURE");
			saveResult.element("message", "操作失败！" + e.getMessage());
		}
		eci.setConfirm_status(2);
		eci.setDeleted(true);
		getEntityManager().merge(eci);
		
		//申请单确认驳回
		ExpendApplyInfo eai = getEntityManager().find(ExpendApplyInfo.class, eci.getExpend_apply_info_id());
		eai.setExpendApplyStatus(2);
		getEntityManager().merge(eai);
		if(eai.getExplendSource() == 1){
			Connection conn = SqlServerJDBCUtil.GetConnection();
			PreparedStatement ps = null;
			ResultSet rs = null;
			String updateSql = "update HMMIS_BUDG.dbo.budg_application4expenditure set state=0 and state_date= ? where bill_code=? ";
			try{
				java.util.Date date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()));//获取系统时间
				java.sql.Timestamp date1=new java.sql.Timestamp(date.getTime());//
				ps = conn.prepareStatement(updateSql);
				ps.setTimestamp(1, date1);
				ps.setString(2, eai.getExpendApplyCode());
				ps.executeUpdate();
			}catch(Exception e){
				System.out.println(e.getMessage());
			}finally{
				SqlServerJDBCUtil.CloseAll(conn, ps, rs);
			}
			
		}
		//确认单项目表
		JSONObject jsonObject = JSONObject.fromObject(projectJson);
		JSONArray expendList = jsonObject.getJSONArray("expend_list");
		float allMoney = 0f;
		for(int i = 0;i < expendList.size();i++){
			//获取参数
			JSONObject project = expendList.getJSONObject(i);
			String projectInfoId = project.getString("project_id");
			String expendMoney = project.getString("expend_money");
			//保存支出申请单详细列表
			ExpendConfirmProject ecp = getEntityManager().find(ExpendConfirmProject.class, Integer.parseInt(projectInfoId));
			ecp.setConfirm_money(Float.parseFloat(expendMoney));
			ecp.setDeleted(true);
			getEntityManager().merge(expendApplyInfo);
		}
		getEntityManager().flush();
		raiseAfterTransactionSuccessEvent();
		return "ok";
	}

	
	/**
	 * 批量确认
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String confirmBatch(){
		joinTransaction();
		saveResult = new JSONObject();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		joinTransaction();
		saveResult.accumulate("invoke_result", "INVOKE_SUCCESS");
		saveResult.accumulate("message", "提交成功！");
		String[] ids = confirmIds.split(",");
		for(int j=0;j<ids.length;j++){
			int expendConfirmId = Integer.parseInt(ids[j]);
			
			//确认单
			ExpendConfirmInfo eci = getEntityManager().find(ExpendConfirmInfo.class, expendConfirmId);
			
			//申请单确认完成
			ExpendApplyInfo eai = getEntityManager().find(ExpendApplyInfo.class, eci.getExpend_apply_info_id());
			eai.setExpendApplyStatus(1);
			getEntityManager().merge(eai);
			
			if(eai.getExplendSource() == 1){
				Connection conn = SqlServerJDBCUtil.GetConnection();
				PreparedStatement ps = null;
				ResultSet rs = null;
				String updateSql = "update HMMIS_BUDG.dbo.budg_application4expenditure set state=1 and state_date= ? where bill_code=? ";
				try{
					java.util.Date date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()));//获取系统时间
					java.sql.Timestamp date1=new java.sql.Timestamp(date.getTime());//
					ps = conn.prepareStatement(updateSql);
					ps.setTimestamp(1, date1);
					ps.setString(2, eai.getExpendApplyCode());
					ps.executeUpdate();
				}catch(Exception e){
					System.out.println(e.getMessage());
				}finally{
					SqlServerJDBCUtil.CloseAll(conn, ps, rs);
				}
				
			}
			eci.setUpdateTime(new Date());
			eci.setTotalMoney(eai.getTotalMoney());
			eci.setConfirmUser(sessionToken.getUserInfoId());
			try {
				eci.setConfirm_time(new Date());
			} catch (Exception e) {
				saveResult.element("invoke_result", "INVOKE_FAILURE");
				saveResult.element("message", "操作失败！" + e.getMessage());
			}
			//确认单确认完成
			eci.setConfirm_status(1);
			getEntityManager().merge(eci);
			
			
			//确认单项目表
			
			String hql = "select expendConfirmProject from ExpendConfirmProject expendConfirmProject where expendConfirmProject.expend_confirm_info_id=" + expendConfirmId; 
			List<ExpendConfirmProject> expendList = getEntityManager().createQuery(hql).getResultList();
			for(int i = 0;i < expendList.size();i++){
				ExpendConfirmProject ecp = expendList.get(i);
				ExpendApplyProject eap = getEntityManager().find(ExpendApplyProject.class, ecp.getExpend_apply_project_id());
				ecp.setConfirm_money(eap.getExpendMoney());
				getEntityManager().merge(ecp);
				
				//to-do 预算下达金额减去支出金额
				
				//执行申请后再减掉金额
				StringBuffer moneySql = new StringBuffer();
				if(ecp.getProjectId() != null && ecp.getGenericProjectId() == null){
					moneySql.append(" SELECT nepi.normal_expend_plan_id,eap.expend_money ");
					moneySql.append(" FROM expend_confirm_project ecp ");
					moneySql.append(" LEFT JOIN expend_apply_project eap on ecp.expend_apply_project_id=eap.expend_apply_project_id ");
					moneySql.append(" LEFT JOIN expend_confirm_info eci on eci.expend_confirm_info_id=ecp.expend_confirm_info_id ");
					moneySql.append(" LEFT JOIN normal_expend_plan_info nepi on nepi.`year`=eci.`year` and nepi.project_id=ecp.project_id ");
					moneySql.append(" where ecp.expend_confirm_project_id= ").append(ecp.getExpend_confirm_project_id());
				}else{
					moneySql.append(" SELECT nepi.normal_expend_plan_id,eap.expend_money ");
					moneySql.append(" FROM expend_confirm_project ecp ");
					moneySql.append(" LEFT JOIN expend_apply_project eap on ecp.expend_apply_project_id=eap.expend_apply_project_id ");
					moneySql.append(" LEFT JOIN expend_confirm_info eci on eci.expend_confirm_info_id=ecp.expend_confirm_info_id ");
					moneySql.append(" LEFT JOIN normal_expend_plan_info nepi on nepi.`year`=eci.`year` and nepi.generic_project_id=ecp.generic_project_id ");
					moneySql.append(" where ecp.expend_confirm_project_id= ").append(ecp.getExpend_confirm_project_id());
					
				}
				List<Object[]> plist = getEntityManager().createNativeQuery(moneySql.toString()).getResultList();
				NormalExpendPlantInfo nxpi = getEntityManager().find(NormalExpendPlantInfo.class, Integer.parseInt(plist.get(0)[0].toString()));
				nxpi.setBudgetAmountFrozen(nxpi.getBudgetAmountFrozen()  + ecp.getConfirm_money());
				nxpi.setBudgetAmountSurplus(nxpi.getBudgetAmountSurplus() - ecp.getConfirm_money());
				getEntityManager().merge(nxpi);
			}
			
		}
		
		getEntityManager().flush();
		raiseAfterTransactionSuccessEvent();
		return "ok";
	}
	
	public List<Object[]> getProjectList() {
		return projectList;
	}




	public void setProjectList(List<Object[]> projectList) {
		this.projectList = projectList;
	}




	public ExpendApplyInfo getExpendApplyInfo() {
		return expendApplyInfo;
	}




	public void setExpendApplyInfo(ExpendApplyInfo expendApplyInfo) {
		this.expendApplyInfo = expendApplyInfo;
	}




	public String getApplyUser() {
		return applyUser;
	}




	public void setApplyUser(String applyUser) {
		this.applyUser = applyUser;
	}




	public String getReimbursementer() {
		return reimbursementer;
	}




	public void setReimbursementer(String reimbursementer) {
		this.reimbursementer = reimbursementer;
	}




	public String getRegister() {
		return register;
	}




	public void setRegister(String register) {
		this.register = register;
	}




	public Integer getExpendConfirmId() {
		return expendConfirmId;
	}




	public void setExpendConfirmId(Integer expendConfirmId) {
		this.expendConfirmId = expendConfirmId;
	}




	public Float getTotalMoney() {
		return totalMoney;
	}




	public void setTotalMoney(Float totalMoney) {
		this.totalMoney = totalMoney;
	}




	public String getApplyTime() {
		return applyTime;
	}




	public void setApplyTime(String applyTime) {
		this.applyTime = applyTime;
	}




	public String getProjectJson() {
		return projectJson;
	}




	public void setProjectJson(String projectJson) {
		this.projectJson = projectJson;
	}




	public String getConfirmTime() {
		return confirmTime;
	}




	public void setConfirmTime(String confirmTime) {
		this.confirmTime = confirmTime;
	}




	public JSONObject getSaveResult() {
		return saveResult;
	}




	public void setSaveResult(JSONObject saveResult) {
		this.saveResult = saveResult;
	}




	public String getConfirmIds() {
		return confirmIds;
	}




	public void setConfirmIds(String confirmIds) {
		this.confirmIds = confirmIds;
	}
	
	
	
	
	
}
