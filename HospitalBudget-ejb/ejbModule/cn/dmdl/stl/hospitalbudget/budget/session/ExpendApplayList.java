package cn.dmdl.stl.hospitalbudget.budget.session;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.persistence.Query;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.budget.entity.ExpendApplyInfo;
import cn.dmdl.stl.hospitalbudget.budget.entity.ExpendApplyProject;
import cn.dmdl.stl.hospitalbudget.budget.entity.ExpendConfirmInfo;
import cn.dmdl.stl.hospitalbudget.budget.entity.ExpendConfirmProject;
import cn.dmdl.stl.hospitalbudget.budget.entity.RoutineProject;
import cn.dmdl.stl.hospitalbudget.budget.entity.YsExpandApplyProcessLog;
import cn.dmdl.stl.hospitalbudget.common.session.CriterionNativeQuery;
import cn.dmdl.stl.hospitalbudget.util.MysqlJDBCUtil;
import cn.dmdl.stl.hospitalbudget.util.NumberUtil;
import cn.dmdl.stl.hospitalbudget.util.SessionToken;
import cn.dmdl.stl.hospitalbudget.util.SqlServerJDBCUtil;

@Name("expendApplayList")
public class ExpendApplayList extends CriterionNativeQuery<Object[]> {

	private static final long serialVersionUID = 1L;
	
	@In
	protected SessionToken sessionToken;
	private List<Object[]> expendApplyInfoList;
	private String searchKey = "";//搜索条件
	//搜索
	private List<Object[]> departList;//科室列表
	private Integer departmentId;//科室id
	private List<Object[]> applayUserList;//申请人列表
	private Integer applyUser;//申请编制人
	private String applyTime;//申请时间
	private String applyEndTime;//申请结束时间
	private List<Object[]> statusList;
	private Integer status;
	
	private static final int FINA_ROLE_ID = 3;//财务人员角色id
	private static final int DIRECTOR_ROLE_ID = 4;//主任角色id
	
	@SuppressWarnings("unchecked")
	public void wire(){
		Integer depId = sessionToken.getDepartmentInfoId();
		if(null != depId && depId != 187){
			departmentId = depId;
		}
		//初始化科室列表
		wireDepartmentInfo();
		System.out.println(departList);
		//初始化申请人列表
		wireBudgetPerson();
		
		//初始化statusList
		statusList = initStatusList();
		
		//从新耗材系统获取支出数据
		/*this.syncNewConsumables();
		
		//在老耗材系统获取支出数据
		this.syncOldConsumables();*/
		
	}
	
	
	
	/**
	 * 在旧耗材系统读取数据
	 */
	private void syncOldConsumables(){

		Connection conn = MysqlJDBCUtil.GetConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		//to-do   注意：这里年份写死的
		String sql = "select t.id,t.register_time,"//入库时间
				+ "t.file_id,"//付款申请单据号
				+ "t.payee,"//收款单位
				+ "t.sum,"//本次付款金额
				+ "t.register,"//操作人
				+ "GROUP_CONCAT(gei.invoice_id) as budget_invoice,"//汇总发票号
				+ "'2018' as budget_year "//年度
				+ "FROM bfh_budget.goods_execute t "
				+ "LEFT JOIN bfh_budget.goods_execute_invoice gei ON t.file_id = gei.file_id "
				+ "where t.execute_time >= '2018-01-01' AND t.is_sync=0 GROUP BY t.id ";
		System.out.println(sql);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String updateSql = "update bfh_budget.goods_execute set is_sync=1 where id=? ";
		
		List<Integer> codeList = new ArrayList<Integer>();
		try {
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while(rs.next()){
				String expendApplyCode = rs.getString("file_id");//单号
				Date venDate = rs.getDate("register_time");//申请时间
				String oper = rs.getString("register");//操作人
				String reciveCompany = rs.getString("payee");
				String invoiceNum = rs.getString("budget_invoice");
				Double totalMoney = Double.parseDouble(rs.getString("sum").replaceAll(",",""));//支出
				String year = rs.getString("budget_year");//预算nian
				Integer oldId = rs.getInt("id");
				codeList.add(oldId);
				
				String checkSql = "select * from expend_apply_info e where e.expend_apply_code='"+expendApplyCode+"' and e.explend_source=1;";
				List<Object[]> checkList = getEntityManager().createNativeQuery(checkSql).getResultList();
				if(null != checkList && checkList.size() > 0){
					continue;
				}
				
				String userSql = "select a.user_info_id,b.fullname from hospital_budget.user_info a LEFT JOIN hospital_budget.user_info_extend b "
						+ " on b.user_info_extend_id=a.user_info_extend_id "
						+ " where b.fullname ='"+oper+"'";
				List<Object[]> userList = getEntityManager().createNativeQuery(userSql).getResultList();
				int userId = 0;
				if(null != userList && userList.size() > 0){
					userId = Integer.valueOf(userList.get(0)[0].toString());
				}
				
				//申请单
				ExpendApplyInfo expendApplyInfo = new ExpendApplyInfo();
				expendApplyInfo.setExpendApplyCode(expendApplyCode);
				expendApplyInfo.setYear(year);
				expendApplyInfo.setApplyUserId(userId);
				expendApplyInfo.setReciveCompany(reciveCompany);
				expendApplyInfo.setInvoiceNum(invoiceNum);
				expendApplyInfo.setInsertUser(userId);
				expendApplyInfo.setInsertTime(venDate);
				expendApplyInfo.setDeleted(false);
				expendApplyInfo.setSummary(invoiceNum.split(",").length + "");
				expendApplyInfo.setReimbursementer(oper);
				expendApplyInfo.setExpendApplyStatus(0);
				expendApplyInfo.setTaskOrderId(0);
				expendApplyInfo.setOrderSn("");
				expendApplyInfo.setDeleted(false);
				expendApplyInfo.setTotalMoney(totalMoney);
				expendApplyInfo.setApplyTime(venDate);
				expendApplyInfo.setExplendSource(1);
				getEntityManager().persist(expendApplyInfo);
				
				//to-do   注意：这里年份写死的，项目id  project_id=10也是写死的
				String nepiSql = "select normal_expend_plan_id,project_id,budget_amount,"
						+ "budget_amount_frozen,budget_amount_surplus "
						+ "from normal_expend_plan_info where `year`='2018' and project_id=10 ";
				List<Object[]> nepiList= getEntityManager().createNativeQuery(nepiSql).getResultList();
				
				//查询流程
				RoutineProject routineProject =  getEntityManager().find(RoutineProject.class,10);//获取耗材项目
				List<Object> processInfoList = getEntityManager().createNativeQuery("select process_info_id from process_info where deleted = 0 and process_type = 1 and project_process_type = 4 and dept_id = " + routineProject.getYsDepartmentInfo().getTheId()).getResultList();// 获取当前登录人所属部门的常规项目流程的常规支出执行流程
				if (processInfoList != null && processInfoList.size() > 0) {// 有流程信息
					List<Object> processStepInfoList = getEntityManager().createNativeQuery("select process_step_info_id from process_step_info where step_index = 1 and process_info_id = " + processInfoList.get(0).toString()).getResultList();// 获取流程配置中的第一步
					if (processStepInfoList != null && processStepInfoList.size() > 0) {// 有步骤配置
						int processStepInfoId = Integer.parseInt(processStepInfoList.get(0).toString());
						//保存流程
						YsExpandApplyProcessLog process = new YsExpandApplyProcessLog();
						process.setYsExpandApplyId(expendApplyInfo.getExpendApplyInfoId());
						process.setProcessStepInfoId(processStepInfoId);
						getEntityManager().persist(process);
					} 
					if(null != nepiList &&nepiList.size() > 0){
						Double budg_year_money = Double.parseDouble(nepiList.get(0)[2].toString());//年预算
						Double budg_year_out_money = getExpendInfoMoney(1,10);//已冻结
						Double disbursable_money = Double.parseDouble(nepiList.get(0)[4].toString());//可支出
						
						ExpendApplyProject eap = new ExpendApplyProject();//申请项目
						eap.setExpendApplyInfoId(expendApplyInfo.getExpendApplyInfoId());
						if(null != nepiList.get(0)[1]){
							eap.setProjectId(Integer.valueOf(nepiList.get(0)[1].toString()));
						}else{
							eap.setGenericProjectId(Integer.valueOf(nepiList.get(0)[2].toString()));
						}
						eap.setExpendBeforFrozen(budg_year_out_money);
						eap.setExpendBeforSurplus(disbursable_money);
						eap.setExpendMoney(totalMoney);
						eap.setInsertUser(userId);
						eap.setInsertTime(expendApplyInfo.getInsertTime());
						eap.setDeleted(false);
						getEntityManager().persist(eap);
						
						
					}
				}else{
					ExpendConfirmInfo eci = new ExpendConfirmInfo();
					eci.setExpend_apply_info_id(expendApplyInfo.getExpendApplyInfoId());
					eci.setInsertUser(userId);
					eci.setInsertTime(expendApplyInfo.getInsertTime());
					eci.setTotalMoney(0d);
					eci.setConfirm_status(0);
					eci.setYear(expendApplyInfo.getYear());
					eci.setDeleted(false);
					getEntityManager().persist(eci);
					
					
					if(null != nepiList &&nepiList.size() > 0){
						Double budg_year_money = Double.parseDouble(nepiList.get(0)[2].toString());//年预算
						Double budg_year_out_money = Double.parseDouble(nepiList.get(0)[3].toString());//已支出
						Double disbursable_money = Double.parseDouble(nepiList.get(0)[4].toString());//可支出
						
						ExpendApplyProject eap = new ExpendApplyProject();//申请项目
						ExpendConfirmProject efp = new ExpendConfirmProject();//确认项目
						eap.setExpendApplyInfoId(expendApplyInfo.getExpendApplyInfoId());
						if(null != nepiList.get(0)[1]){
							eap.setProjectId(Integer.valueOf(nepiList.get(0)[1].toString()));
						}else{
							eap.setGenericProjectId(Integer.valueOf(nepiList.get(0)[2].toString()));
						}
						eap.setExpendBeforFrozen(budg_year_out_money);
						eap.setExpendBeforSurplus(disbursable_money);
						eap.setExpendMoney(totalMoney);
						eap.setInsertUser(userId);
						eap.setInsertTime(expendApplyInfo.getInsertTime());
						eap.setDeleted(false);
						getEntityManager().persist(eap);
						
						
						efp.setExpend_confirm_info_id(eci.getExpend_confirm_info_id());
						efp.setExpend_apply_project_id(eap.getExpendApplyProjectId());
						efp.setExpendBeforFrozen(eap.getExpendBeforFrozen());
						efp.setExpendBeforSurplus(eap.getExpendBeforSurplus());
						efp.setProjectId(eap.getProjectId());
						efp.setGenericProjectId(eap.getGenericProjectId());
						efp.setDeleted(false);
						efp.setConfirm_money(eap.getExpendMoney());
						getEntityManager().persist(efp);
					}
				}
			}
			for(Integer id : codeList){
				ps = conn.prepareStatement(updateSql);
				ps.setInt(1, id);
				ps.executeUpdate();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			MysqlJDBCUtil.CloseAll(conn, ps, rs);
		}
		
	}
	
	/**
	 * 在新耗材系统内读取耗材数据
	 */
	private void syncNewConsumables(){
		Connection conn = SqlServerJDBCUtil.GetConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		//String sql = "select iow_date,bill_code,ven_code,this_payment_money,oper,main_INVOICE_NO,budg_year_money,budg_year_out_money,disbursable_money from HMMIS_BUDG.dbo.budg_application4expenditure where is_sync=0";
		String sql = "select a.iow_date,"//入库时间
				+ "a.bill_code,"//付款申请单据号
				+ "a.ven_code,"//收款单位
				+ "a.this_payment_money,"//本次付款金额
				+ "a.oper,"//操作人
				+ "a.main_INVOICE_NO,"//汇总发票号
				+ "a.budg_year_money,"//年预算金额
				+ "a.budg_year_out_money,"//已支出金额
				+ "a.disbursable_money, "//可支出金额
				+ "b.budg_year,"//年度
				+ "b.budg_code "//预算类型编码
				+ "from HMMIS_BUDG.dbo.budg_application4expenditure a,"
				+ "HMMIS_BUDG.dbo.budg_type_dict b "
				+ "where a.budg_code=b.budg_code "
				+ "and a.is_sync=0";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String updateSql = "update HMMIS_BUDG.dbo.budg_application4expenditure set is_sync=1 where bill_code=? ";
		List<String> codeList = new ArrayList<String>();
		try {
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while(rs.next()){
				String expendApplyCode = rs.getString("bill_code");//单号
				Date venDate = rs.getDate("iow_date");//申请时间
				String oper = rs.getString("oper");//操作人
				String reciveCompany = rs.getString("ven_code");
				String invoiceNum = rs.getString("main_INVOICE_NO");
				Double totalMoney = rs.getDouble("this_payment_money");//支出
				Double budg_year_money = rs.getDouble("budg_year_money");//年预算
//				Double budg_year_out_money = rs.getDouble("budg_year_out_money");//已支出
//				Double disbursable_money = rs.getDouble("disbursable_money");//可支出
				String year = rs.getString("budg_year");//预算nian
				String userSql = "select a.user_info_id from user_info a LEFT JOIN user_info_extend b "
						+ " on b.user_info_extend_id=a.user_info_extend_id "
						+ " where b.fullname ='"+oper+"'";
				String budgCode = rs.getString("budg_code");
				int projectType = 0;
				int updateProjuectId = 0;
				
				String checkSql = "select * from expend_apply_info e where e.expend_apply_code='"+expendApplyCode+"' and e.explend_source=1;";
				List<Object[]> checkList = getEntityManager().createNativeQuery(checkSql).getResultList();
				if(null != checkList && checkList.size() > 0){
					continue;
				}
				
				
				List<Object> userList = getEntityManager().createNativeQuery(userSql).getResultList();
				int userId = 0;
				if(null != userList && userList.size() > 0){
					userId = Integer.valueOf(userList.get(0).toString());
				}
				codeList.add(expendApplyCode);
				//申请单
				ExpendApplyInfo expendApplyInfo = new ExpendApplyInfo();
				expendApplyInfo.setExpendApplyCode(expendApplyCode);
				expendApplyInfo.setYear(year);
				expendApplyInfo.setApplyUserId(userId);
				expendApplyInfo.setReciveCompany(reciveCompany);
				expendApplyInfo.setInvoiceNum(invoiceNum);
				expendApplyInfo.setInsertUser(userId);
				expendApplyInfo.setInsertTime(venDate);
				expendApplyInfo.setDeleted(false);
				expendApplyInfo.setSummary("1");
				expendApplyInfo.setReimbursementer(oper);
				expendApplyInfo.setExpendApplyStatus(0);
				expendApplyInfo.setTaskOrderId(0);
				expendApplyInfo.setOrderSn("");
				expendApplyInfo.setDeleted(false);
				expendApplyInfo.setTotalMoney(totalMoney);
				expendApplyInfo.setApplyTime(venDate);
				expendApplyInfo.setExplendSource(2);
				getEntityManager().persist(expendApplyInfo);
				
				String nepiSql = "select normal_expend_plan_id,project_id,generic_project_id,budget_amount_surplus "
						+ " from normal_expend_plan_info where normal_expend_plan_id=" + budgCode;
				List<Object[]> nepiList= getEntityManager().createNativeQuery(nepiSql).getResultList();
				
				//查询流程
				RoutineProject routineProject =  getEntityManager().find(RoutineProject.class,10);//获取耗材项目
				List<Object> processInfoList = getEntityManager().createNativeQuery("select process_info_id from process_info where deleted = 0 and process_type = 1 and project_process_type = 4 and dept_id = " + routineProject.getYsDepartmentInfo().getTheId()).getResultList();// 获取当前登录人所属部门的常规项目流程的常规支出执行流程
				if (processInfoList != null && processInfoList.size() > 0) {// 有流程信息
					List<Object> processStepInfoList = getEntityManager().createNativeQuery("select process_step_info_id from process_step_info where step_index = 1 and process_info_id = " + processInfoList.get(0).toString()).getResultList();// 获取流程配置中的第一步
					if (processStepInfoList != null && processStepInfoList.size() > 0) {// 有步骤配置
						int processStepInfoId = Integer.parseInt(processStepInfoList.get(0).toString());
						//保存流程
						YsExpandApplyProcessLog process = new YsExpandApplyProcessLog();
						process.setYsExpandApplyId(expendApplyInfo.getExpendApplyInfoId());
						process.setProcessStepInfoId(processStepInfoId);
						getEntityManager().persist(process);
					}
					if(null != nepiList &&nepiList.size() > 0){
						Double budg_year_out_money = 0d;//已冻结;
						Double disbursable_money = Double.parseDouble(nepiList.get(0)[3].toString());//可支出
						ExpendApplyProject eap = new ExpendApplyProject();//申请项目
						eap.setExpendApplyInfoId(expendApplyInfo.getExpendApplyInfoId());
						if(null != nepiList.get(0)[1]){
							eap.setProjectId(Integer.valueOf(nepiList.get(0)[1].toString()));
							budg_year_out_money = getExpendInfoMoney(1,Integer.valueOf(nepiList.get(0)[1].toString()));
							projectType = 1;
							updateProjuectId = Integer.valueOf(nepiList.get(0)[1].toString());
						}else{
							eap.setGenericProjectId(Integer.valueOf(nepiList.get(0)[2].toString()));
							budg_year_out_money = getExpendInfoMoney(2,Integer.valueOf(nepiList.get(0)[2].toString()));
							projectType = 2;
							updateProjuectId = Integer.valueOf(nepiList.get(0)[2].toString());
						}
						eap.setExpendBeforFrozen(budg_year_out_money);
						eap.setExpendBeforSurplus(disbursable_money);
						eap.setExpendMoney(totalMoney);
						eap.setInsertUser(userId);
						eap.setInsertTime(expendApplyInfo.getInsertTime());
						eap.setDeleted(false);
						getEntityManager().persist(eap);
					}
					
				}else{
					ExpendConfirmInfo eci = new ExpendConfirmInfo();
					eci.setExpend_apply_info_id(expendApplyInfo.getExpendApplyInfoId());
					eci.setInsertUser(userId);
					eci.setInsertTime(expendApplyInfo.getInsertTime());
					eci.setTotalMoney(0d);
					eci.setConfirm_status(0);
					eci.setYear(expendApplyInfo.getYear());
					eci.setDeleted(false);
					getEntityManager().persist(eci);
					
					
					if(null != nepiList &&nepiList.size() > 0){
						Double budg_year_out_money = 0d;//已冻结;
						Double disbursable_money = Double.parseDouble(nepiList.get(0)[3].toString());//可支出
						ExpendApplyProject eap = new ExpendApplyProject();//申请项目
						ExpendConfirmProject efp = new ExpendConfirmProject();//确认项目
						eap.setExpendApplyInfoId(expendApplyInfo.getExpendApplyInfoId());
						if(null != nepiList.get(0)[1]){
							eap.setProjectId(Integer.valueOf(nepiList.get(0)[1].toString()));
							budg_year_out_money = getExpendInfoMoney(1,Integer.valueOf(nepiList.get(0)[1].toString()));
							projectType = 1;
							updateProjuectId = Integer.valueOf(nepiList.get(0)[1].toString());
						}else{
							eap.setGenericProjectId(Integer.valueOf(nepiList.get(0)[2].toString()));
							budg_year_out_money = getExpendInfoMoney(2,Integer.valueOf(nepiList.get(0)[2].toString()));
							projectType = 2;
							updateProjuectId = Integer.valueOf(nepiList.get(0)[2].toString());
						}
						eap.setExpendBeforFrozen(budg_year_out_money);
						eap.setExpendBeforSurplus(disbursable_money);
						eap.setExpendMoney(totalMoney);
						eap.setInsertUser(userId);
						eap.setInsertTime(expendApplyInfo.getInsertTime());
						eap.setDeleted(false);
						getEntityManager().persist(eap);
						
						
						efp.setExpend_confirm_info_id(eci.getExpend_confirm_info_id());
						efp.setExpend_apply_project_id(eap.getExpendApplyProjectId());
						efp.setExpendBeforFrozen(eap.getExpendBeforFrozen());
						efp.setExpendBeforSurplus(eap.getExpendBeforSurplus());
						efp.setProjectId(eap.getProjectId());
						efp.setGenericProjectId(eap.getGenericProjectId());
						efp.setDeleted(false);
						efp.setConfirm_money(eap.getExpendMoney());
						getEntityManager().persist(efp);
					}
				}
				 String froensql = "update HMMIS_BUDG.dbo.budg_type_dict set budg_year_frozen = "+getExpendInfoMoney(projectType,updateProjuectId)
						   +" where budg_code='" + budgCode + "'";
				 ps = conn.prepareStatement(froensql);
				 ps.executeUpdate();
			}
			
			for(String code : codeList){
				ps = conn.prepareStatement(updateSql);
				ps.setString(1, code);
				ps.executeUpdate();
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			SqlServerJDBCUtil.CloseAll(conn, ps, rs);
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	protected Query createQuery(){
		boolean privateRole = false;//角色不属于财务 和主任（领导的）
		String roleSql = "select role_info.role_info_id,user_info.department_info_id,ydi.the_value from user_info LEFT JOIN role_info on role_info.role_info_id=user_info.role_info_id LEFT JOIN ys_department_info ydi on "
				+ "user_info.department_info_id=ydi.the_id where user_info.user_info_id=" + sessionToken.getUserInfoId();
		
		List<Object[]> roleList = getEntityManager().createNativeQuery(roleSql).getResultList();
		int roleId = Integer.parseInt(roleList.get(0)[0].toString());//角色id
		
		if(Integer.valueOf(roleId) != 1 && Integer.valueOf(roleId) != 2){
			applyUser = sessionToken.getUserInfoId();
			privateRole = true;
		}
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT ");
		sql.append(" eai.expend_apply_info_id, ");//0申请单id
		sql.append(" eai.expend_apply_code, ");//1申请编号
		sql.append(" eai.`year`,");//2支出年份
		sql.append(" eai.total_money, ");//3总金额
		sql.append(" eai.recive_company, ");//4收款单位
		sql.append(" eai.invoice_num, ");//5发票号
		sql.append(" uie.fullname, ");//6申请人名字
		sql.append(" eai.apply_time, ");//7申请时间
		sql.append(" eai.summary, ");//8摘要
		sql.append(" eai.`comment`, ");//9备注
		sql.append(" eai.expend_apply_status,eai.explend_source ");//10状态
		sql.append(" FROM expend_apply_info eai ");
		sql.append(" LEFT JOIN user_info ui ON eai.applay_user_id = ui.user_info_id ");
		sql.append(" LEFT JOIN user_info_extend uie on ui.user_info_extend_id=uie.user_info_extend_id ");
		sql.append(" where eai.deleted=0 ");
		
		if(privateRole){
			sql.append(" and eai.insert_user= ").append(sessionToken.getUserInfoId());
		}
		if(null != departmentId && departmentId != -1){
			sql.append(" and ui.department_info_id= ").append(departmentId);
		}
		if(null != applyUser && applyUser != -1){
			sql.append(" and eai.applay_user_id= ").append(applyUser);
		}
		if(null != applyTime && !applyTime.equals("")){
			sql.append(" and eai.apply_time>='").append(applyTime + " 00:00:00").append("'");
		}
		if(null != applyEndTime && !applyEndTime.equals("")){
			sql.append(" and eai.apply_time<='").append(applyEndTime + " 23:59:59").append("'");
		}
		if(null != searchKey && !searchKey.equals("") ){
			sql.append(" and (eai.expend_apply_code = '%").append(searchKey).append("%' or eai.recive_company like '%").append(searchKey).append("%')");
		}
		if(null != status && status != -1 ){
			sql.append(" and eai.expend_apply_status=").append(status);
		}
		sql.append(" order by eai.insert_time desc,eai.expend_apply_code desc");
		sql.insert(0, "select * from (").append(") as recordset ");
		System.out.println(sql);
		setEjbql(sql.toString());
		return super.createQuery();
	}
	
	/**
	 * 获取申请单状态
	 * 申请单状态 0初始状态 1正在审核中 2审核完成 3审核驳回  4确认驳回 5 确认完成 
	 * @param status
	 * @return
	 */
	private String getApplyStatus(Object status){
		if(null == status && status.toString().equals("")){
			return "";
		}
		Integer st = Integer.valueOf(status.toString());
		if(st == 0){
			return "待处理";
		}else if(st == 1){
			return "正在审核";
		}else if(st == 2){
			return "审核完成";
		}else if(st == 3){
			return "审核驳回";
		}else if(st == 4){
			return "确认驳回";
		}else if(st == 5){
			return "确认完成";
		}
		return "";
	}

	public ExpendApplayList() {
		/*boolean privateRole = false;//角色不属于财务 和主任（领导的）
		String roleSql = "select role_info.role_info_id,user_info.department_info_id,ydi.the_value from user_info LEFT JOIN role_info on role_info.role_info_id=user_info.role_info_id LEFT JOIN ys_department_info ydi on "
				+ "user_info.department_info_id=ydi.the_id where user_info.user_info_id=" + sessionToken.getUserInfoId();
		
		List<Object[]> roleList = getEntityManager().createNativeQuery(roleSql).getResultList();
		int roleId = Integer.parseInt(roleList.get(0)[0].toString());//角色id
		
		if(Integer.valueOf(roleId) != 1 && Integer.valueOf(roleId) != FINA_ROLE_ID && Integer.valueOf(roleId) != DIRECTOR_ROLE_ID){
			privateRole = true;
		}
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT ");
		sql.append(" eai.expend_apply_info_id, ");//0申请单id
		sql.append(" eai.expend_apply_code, ");//1申请编号
		sql.append(" eai.`year`,");//2支出年份
		sql.append(" eai.finace_account_name, ");//3账务账名
		sql.append(" eai.recive_company, ");//4收款单位
		sql.append(" eai.invoice_num, ");//5发票号
		sql.append(" uie.fullname, ");//6申请人名字
		sql.append(" eai.apply_time, ");//7申请时间
		sql.append(" eai.summary, ");//8摘要
		sql.append(" eai.`comment` ");//9摘要
		sql.append(" FROM expend_apply_info eai ");
		sql.append(" LEFT JOIN user_info ui ON eai.applay_user_id = ui.user_info_id ");
		sql.append(" LEFT JOIN user_info_extend uie on ui.user_info_extend_id=uie.user_info_extend_id ");
		sql.append(" where eai.deleted=0 ");
		if(privateRole){
			sql.append(" eai.insert_user= ").append(sessionToken.getUserInfoId());
		}
		if(null != searchKey && !searchKey.equals("")){
			sql.append(" and (eai.expend_apply_code = '%").append(searchKey).append("%' eai.finace_account_name LIKE '%").append(searchKey).append("%')");
		}
		sql.insert(0, "select * from (").append(") as recordset");*/
		setEjbql("");
		setAttribute("");
	}
	
	/**
	 * 获取科室
	 */
	@SuppressWarnings("unchecked")
	private void wireDepartmentInfo() {
		if (departList != null) {
			departList.clear();
		} else {
			departList = new ArrayList<Object[]>();
		}
		departList.add(new Object[] { -1, "请选择" });
		Map<Object, List<Object>> nexusMap = new HashMap<Object, List<Object>>();// 上下级关系集合
		Map<Object, Object> valueMap = new HashMap<Object, Object>();// 值集合
		String dataSql = "select the_id, the_pid, the_value from ys_department_info where deleted = 0";
		List<Object[]> dataList = getEntityManager().createNativeQuery(dataSql).getResultList();
		if (dataList != null && dataList.size() > 0) {
			for (Object[] data : dataList) {
				valueMap.put(data[0], data[2]);
				List<Object> leafList = nexusMap.get(data[1]);
				if (leafList != null) {
					leafList.add(data[0]);
				} else {
					leafList = new ArrayList<Object>();
					leafList.add(data[0]);
					nexusMap.put(data[1], leafList);
				}
			}
		}
		List<Object> rootList = nexusMap.get(null);
		if (rootList != null && rootList.size() > 0) {
			for (Object root : rootList) {
				departList.add(new Object[] { root, valueMap.get(root) });
				disposeLeaf(departList, nexusMap, valueMap, 1, nexusMap.get(root));
			}
		}
	}
	
	/** 递归处理子节点 */
	public void disposeLeaf(List<Object[]> targetList, Map<Object, List<Object>> nexusMap, Map<Object, Object> valueMap, int indentWidth, List<Object> leafList) {
		if (leafList != null && leafList.size() > 0) {
			for (Object leaf : leafList) {
				String indentStr = "";
				for (int i = 0; i < indentWidth; i++) {
					indentStr += "　";
				}
				targetList.add(new Object[] { leaf, indentStr + valueMap.get(leaf) });
				disposeLeaf(targetList, nexusMap, valueMap, indentWidth + 1, nexusMap.get(leaf));
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void wireBudgetPerson() {
		Map<Object, List<Object[]>> userMap = new HashMap<Object, List<Object[]>>();
		StringBuffer userSql = new StringBuffer();
		userSql.append(" select");
		userSql.append(" user_info.department_info_id,");
		userSql.append(" user_info.user_info_id,");
		userSql.append(" user_info_extend.fullname");
		userSql.append(" from user_info");
		userSql.append(" inner join user_info_extend on user_info_extend.user_info_extend_id = user_info.user_info_extend_id");
		userSql.append(" where user_info.deleted = 0 and user_info.enabled = 1 and user_info.department_info_id is not null");
		List<Object[]> userList = getEntityManager().createNativeQuery(userSql.toString()).getResultList();
		if (userList != null && userList.size() > 0) {
			for (Object[] user : userList) {
				if (userMap.get(user[0]) != null) {
					userMap.get(user[0]).add(new Object[] { user[1], user[2] });
				} else {
					List<Object[]> newList = new ArrayList<Object[]>();
					newList.add(new Object[] { user[1], user[2] });
					userMap.put(user[0], newList);
				}
			}
		}

		if (applayUserList != null) {
			applayUserList.clear();
		} else {
			applayUserList = new ArrayList<Object[]>();
		}
		Map<Object, List<Object>> nexusMap = new HashMap<Object, List<Object>>();// 上下级关系集合
		Map<Object, Object> valueMap = new HashMap<Object, Object>();// 值集合
		String dataSql = "select the_id, the_pid, the_value from ys_department_info where deleted = 0";
		List<Object[]> dataList = getEntityManager().createNativeQuery(dataSql).getResultList();
		if (dataList != null && dataList.size() > 0) {
			for (Object[] data : dataList) {
				valueMap.put(data[0], data[2]);
				List<Object> leafList = nexusMap.get(data[1]);
				if (leafList != null) {
					leafList.add(data[0]);
				} else {
					leafList = new ArrayList<Object>();
					leafList.add(data[0]);
					nexusMap.put(data[1], leafList);
				}
			}
		}
		List<Object> rootList = nexusMap.get(null);
		if (rootList != null && rootList.size() > 0) {
			for (Object root : rootList) {
				if (userMap.get(root) != null && userMap.get(root).size() > 0) {
					applayUserList.add(new Object[] { valueMap.get(root), userMap.get(root) });
				}
				disposeLeafBudgetPerson(applayUserList, userMap, nexusMap, valueMap, 1, nexusMap.get(root));
			}
		}

	}
	
	/** 递归处理子节点BudgetPerson */
	public void disposeLeafBudgetPerson(List<Object[]> targetList, Map<Object, List<Object[]>> userMap, Map<Object, List<Object>> nexusMap, Map<Object, Object> valueMap, int indentWidth, List<Object> leafList) {
		if (leafList != null && leafList.size() > 0) {
			for (Object leaf : leafList) {
				String indentStr = "";
				for (int i = 0; i < indentWidth; i++) {
					indentStr += "　";
				}
				if (userMap.get(leaf) != null && userMap.get(leaf).size() > 0) {
					List<Object[]> userList = userMap.get(leaf);
					List<Object[]> tmpList = new ArrayList<Object[]>();
					if (userList != null && userList.size() > 0) {
						for (Object[] user : userList) {
							tmpList.add(new Object[] { user[0], indentStr + user[1] });
						}
					}
					targetList.add(new Object[] { indentStr + valueMap.get(leaf), tmpList });
				}
				disposeLeafBudgetPerson(targetList, userMap, nexusMap, valueMap, indentWidth + 1, nexusMap.get(leaf));
			}
		}
	}
	
	/**
	 * 导出
	 */
	public void expExcel(){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		FacesContext ctx = FacesContext.getCurrentInstance();
		ctx.responseComplete();
		HttpServletResponse response = (HttpServletResponse) ctx
				.getExternalContext().getResponse();
		Workbook workbook = new HSSFWorkbook();
		// 标题字体
		HSSFCellStyle colTitleStyle = (HSSFCellStyle) workbook
				.createCellStyle();
		HSSFFont titleFont = (HSSFFont) workbook.createFont();
		titleFont.setFontName("宋体");
		titleFont.setFontHeightInPoints((short) 16);// 字体大小
		colTitleStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		colTitleStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		colTitleStyle.setFont(titleFont);
		colTitleStyle.setBorderBottom((short) 1);
		colTitleStyle.setBorderLeft((short) 1);
		colTitleStyle.setBorderRight((short) 1);
		colTitleStyle.setBorderTop((short) 1);
		colTitleStyle.setFillBackgroundColor(HSSFColor.YELLOW.index);
		// 普通单元格样式
		HSSFCellStyle colStyle = (HSSFCellStyle) workbook.createCellStyle();
		HSSFFont colFont = (HSSFFont) workbook.createFont();
		colFont.setFontName("宋体");
		colFont.setFontHeightInPoints((short) 11);// 字体大小
		colStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		colStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		colStyle.setFont(colFont);
		colStyle.setBorderBottom((short) 1);
		colStyle.setBorderLeft((short) 1);
		colStyle.setBorderRight((short) 1);
		colStyle.setBorderTop((short) 1);
		colStyle.setFillForegroundColor(HSSFColor.YELLOW.index);

		int count = 1;
		String headStr = "项目分类支出表";
		Sheet sheet = workbook.createSheet();
		workbook.setSheetName(count - 1, headStr);
		int rowIndex = 0;
		int colIndex = 0;

		CellRangeAddress range = null;

		// 第一行
		HSSFRow rowTitle = (HSSFRow) sheet.createRow(rowIndex);
		rowTitle.setHeightInPoints(30f);
		HSSFCell cellTitle = rowTitle.createCell(colIndex);
		cellTitle.setCellType(HSSFCell.CELL_TYPE_STRING);
		cellTitle.setCellValue("支出申请明细表");
		cellTitle.setCellStyle(colTitleStyle);
		range = new CellRangeAddress(0, 0, 0, 6);
		sheet.addMergedRegion(range);
		excelAddBorder(1, range, sheet, workbook);
		
		rowIndex ++;
		HSSFRow row1 = (HSSFRow) sheet.createRow(rowIndex);
		row1.setHeightInPoints(20f);
		HSSFCell codeCol = row1.createCell(colIndex);
		codeCol.setCellType(HSSFCell.CELL_TYPE_STRING);
		codeCol.setCellValue("单据编号");
		codeCol.setCellStyle(colStyle);
		codeCol.getSheet().setColumnWidth(
				codeCol.getColumnIndex(), 35 * 160);
		colIndex++;

		HSSFCell yearCol = row1.createCell(colIndex);
		yearCol.setCellType(HSSFCell.CELL_TYPE_STRING);
		yearCol.setCellValue("支出年份");
		yearCol.setCellStyle(colStyle);
		yearCol.getSheet().setColumnWidth(yearCol.getColumnIndex(),
				35 * 100);
		colIndex++;

		HSSFCell company = row1.createCell(colIndex);
		company.setCellType(HSSFCell.CELL_TYPE_STRING);
		company.setCellValue("收款单位");
		company.setCellStyle(colStyle);
		company.getSheet().setColumnWidth(company.getColumnIndex(),
				35 * 260);
		colIndex++;

		HSSFCell totalMoney = row1.createCell(colIndex);
		totalMoney.setCellType(HSSFCell.CELL_TYPE_STRING);
		totalMoney.setCellValue("金额");
		totalMoney.setCellStyle(colStyle);
		totalMoney.getSheet().setColumnWidth(totalMoney.getColumnIndex(),
				35 * 120);
		colIndex++;
		
		HSSFCell reimbursement = row1.createCell(colIndex);
		reimbursement.setCellType(HSSFCell.CELL_TYPE_STRING);
		reimbursement.setCellValue("申请人");
		reimbursement.setCellStyle(colStyle);
		reimbursement.getSheet().setColumnWidth(reimbursement.getColumnIndex(),
				35 * 90);
		colIndex++;
		
		HSSFCell applyTimeCol = row1.createCell(colIndex);
		applyTimeCol.setCellType(HSSFCell.CELL_TYPE_STRING);
		applyTimeCol.setCellValue("申请时间");
		applyTimeCol.setCellStyle(colStyle);
		applyTimeCol.getSheet().setColumnWidth(applyTimeCol.getColumnIndex(),
				35 * 90);
		colIndex++;
		
		HSSFCell invoceCol = row1.createCell(colIndex);
		invoceCol.setCellType(HSSFCell.CELL_TYPE_STRING);
		invoceCol.setCellValue("发票");
		invoceCol.setCellStyle(colStyle);
		invoceCol.getSheet().setColumnWidth(invoceCol.getColumnIndex(),
				35 * 260);
		colIndex++;
		
		List<Object[]> list = queryExportData(departmentId,applyUser,applyTime,applyEndTime,searchKey,status);
		for(int i = 0;i < list.size(); i++){
			Object[] obj = list.get(i);
			rowIndex ++;
			int col = 0;
			row1 = (HSSFRow) sheet.createRow(rowIndex);
			row1.setHeightInPoints(20f);
			codeCol = row1.createCell(col);
			codeCol.setCellType(HSSFCell.CELL_TYPE_STRING);
			codeCol.setCellValue(obj[1]==null ? "":obj[1].toString());
			codeCol.setCellStyle(colStyle);
			codeCol.getSheet().setColumnWidth(
					codeCol.getColumnIndex(), 35 * 160);
			col++;
			
			yearCol = row1.createCell(col);
			yearCol.setCellType(HSSFCell.CELL_TYPE_STRING);
			yearCol.setCellValue(obj[2]==null ? "": obj[2].toString());
			yearCol.setCellStyle(colStyle);
			yearCol.getSheet().setColumnWidth(
					yearCol.getColumnIndex(), 35 * 100);
			col++;
			
			company = row1.createCell(col);
			company.setCellType(HSSFCell.CELL_TYPE_STRING);
			company.setCellValue(obj[4] == null ? "" : obj[4].toString());
			company.setCellStyle(colStyle);
			company.getSheet().setColumnWidth(
					company.getColumnIndex(), 35 * 260);
			col++;
			
			totalMoney = row1.createCell(col);
			totalMoney.setCellType(HSSFCell.CELL_TYPE_STRING);
			totalMoney.setCellValue(obj[3] == null || obj[3].toString().equals("") ? "" :NumberUtil.formatDouble2(obj[3]));
			totalMoney.setCellStyle(colStyle);
			totalMoney.getSheet().setColumnWidth(
					totalMoney.getColumnIndex(), 35 * 120);
			col++;
			
			reimbursement = row1.createCell(col);
			reimbursement.setCellType(HSSFCell.CELL_TYPE_STRING);
			reimbursement.setCellValue(obj[6] == null ? "":obj[6].toString());
			reimbursement.setCellStyle(colStyle);
			reimbursement.getSheet().setColumnWidth(
					reimbursement.getColumnIndex(), 35 * 90);
			col++;
			
			applyTimeCol = row1.createCell(col);
			applyTimeCol.setCellType(HSSFCell.CELL_TYPE_STRING);
			try{
				applyTimeCol.setCellValue(obj[7].toString());
			}catch(Exception e){
				applyTimeCol.setCellValue("");
			}
			
			applyTimeCol.setCellStyle(colStyle);
			applyTimeCol.getSheet().setColumnWidth(
					applyTimeCol.getColumnIndex(), 35 * 90);
			col++;
			
			invoceCol = row1.createCell(col);
			invoceCol.setCellType(HSSFCell.CELL_TYPE_STRING);
			invoceCol.setCellValue(obj[5]==null?"":obj[5].toString());
			invoceCol.setCellStyle(colStyle);
			invoceCol.getSheet().setColumnWidth(
					invoceCol.getColumnIndex(), 35 * 260);
			col++;
		}
		
		
		response.setContentType("application/vnd.ms-excel");
		try {
			response.setHeader("Content-Disposition", "attachment;filename="
					+ new String("支出申请明细表.xls".getBytes(),
							"iso-8859-1"));
			workbook.write(response.getOutputStream());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 查询导出数据
	 * @param projectName
	 * @param sourceId
	 * @param departId
	 * @param time
	 */
	@SuppressWarnings("unchecked")
	public List<Object[]> queryExportData(Integer departmentId,Integer applyUser,String applyTime,String applyEndTime,String searchKey,Integer status){
		boolean privateRole = false;//角色不属于财务 和主任（领导的）
		String roleSql = "select role_info.role_info_id,user_info.department_info_id,ydi.the_value from user_info LEFT JOIN role_info on role_info.role_info_id=user_info.role_info_id LEFT JOIN ys_department_info ydi on "
				+ "user_info.department_info_id=ydi.the_id where user_info.user_info_id=" + sessionToken.getUserInfoId();
		
		List<Object[]> roleList = getEntityManager().createNativeQuery(roleSql).getResultList();
		int roleId = Integer.parseInt(roleList.get(0)[0].toString());//角色id
		
		if(Integer.valueOf(roleId) != 1 && Integer.valueOf(roleId) != 2){
			applyUser = sessionToken.getUserInfoId();
			privateRole = true;
		}
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT ");
		sql.append(" eai.expend_apply_info_id, ");//0申请单id
		sql.append(" eai.expend_apply_code, ");//1申请编号
		sql.append(" eai.`year`,");//2支出年份
		sql.append(" eai.total_money, ");//3总金额
		sql.append(" eai.recive_company, ");//4收款单位
		sql.append(" eai.invoice_num, ");//5发票号
		sql.append(" uie.fullname, ");//6申请人名字
		sql.append(" DATE_FORMAT( eai.apply_time, '%Y-%m-%d'), ");//7申请时间
		sql.append(" eai.summary, ");//8摘要
		sql.append(" eai.`comment`, ");//9备注
		sql.append(" eai.expend_apply_status,eai.explend_source ");//10状态
		sql.append(" FROM expend_apply_info eai ");
		sql.append(" LEFT JOIN user_info ui ON eai.applay_user_id = ui.user_info_id ");
		sql.append(" LEFT JOIN user_info_extend uie on ui.user_info_extend_id=uie.user_info_extend_id ");
		sql.append(" where eai.deleted=0 ");
		
		if(privateRole){
			sql.append(" and eai.insert_user= ").append(sessionToken.getUserInfoId());
		}
		if(null != departmentId && departmentId != -1){
			sql.append(" and ui.department_info_id= ").append(departmentId);
		}
		if(null != applyUser && applyUser != -1){
			sql.append(" and eai.applay_user_id= ").append(applyUser);
		}
		if(null != applyTime && !applyTime.equals("")){
			sql.append(" and eai.apply_time>='").append(applyTime + " 00:00:00").append("'");
		}
		if(null != applyEndTime && !applyEndTime.equals("")){
			sql.append(" and eai.apply_time<='").append(applyEndTime + " 23:59:59").append("'");
		}
		if(null != searchKey && !searchKey.equals("") ){
			sql.append(" and (eai.expend_apply_code = '%").append(searchKey).append("%' or eai.recive_company like '%").append(searchKey).append("%')");
		}
		if(null != status && status != -1 ){
			sql.append(" and eai.expend_apply_status=").append(status);
		}
		sql.append(" order by eai.insert_time desc,eai.expend_apply_code desc");
		sql.insert(0, "select * from (").append(") as recordset ");
		List<Object[]> list = getEntityManager().createNativeQuery(sql.toString()).getResultList();
		return list;
	}
	
	/** * 添加excel合并后边框属性 */
	public void excelAddBorder(int arg0, CellRangeAddress range, Sheet sheet,
			Workbook workbook) {
		RegionUtil.setBorderBottom(1, range, sheet, workbook);
		RegionUtil.setBorderLeft(1, range, sheet, workbook);
		RegionUtil.setBorderRight(1, range, sheet, workbook);
		RegionUtil.setBorderTop(1, range, sheet, workbook);
	}
	
	
	/**
	 * 获取某个项目未完成的申请单中的钱
	 * @param projectType
	 * @param projectId
	 * @return
	 */
	public Double getExpendInfoMoney(int projectType,int projectId){
		double money = 0d;
		StringBuffer sql = new StringBuffer();
		if(projectType == 1){
			sql.append(" select sum(eap.expend_money) from expend_apply_info eai  ");
			sql.append(" LEFT JOIN expend_apply_project eap on eai.expend_apply_info_id=eap.expend_apply_info_id ");
			sql.append(" where eai.expend_apply_status in (0,1,2) ");
			sql.append(" and eap.project_id= ").append(projectId);
			sql.append(" and eai.deleted = 0 ");
			sql.append(" GROUP BY eap.project_id ");
		}else{
			sql.append(" select sum(eap.expend_money) from expend_apply_info eai  ");
			sql.append(" LEFT JOIN expend_apply_project eap on eai.expend_apply_info_id=eap.expend_apply_info_id ");
			sql.append(" where eai.expend_apply_status in (0,1,2) ");
			sql.append(" and eap.generic_project_id= ").append(projectId);
			sql.append(" and eai.deleted = 0 ");
			sql.append(" GROUP BY eap.project_id ");
		}
		
		List<Object> moneyList = getEntityManager().createNativeQuery(sql.toString()).getResultList();
		if(null != moneyList && moneyList.size() > 0){
			Object obj = moneyList.get(0);
			if(null != obj){
				money = Double.parseDouble(obj.toString());
			}
		}
		return money;
	}
	
	private List<Object[]> initStatusList(){
		List<Object[]> list = new ArrayList<Object[]>();
		list.add(new Object[]{-1,"全部"});
		list.add(new Object[]{0,"待处理"});
		list.add(new Object[]{1,"正在审核"});
		list.add(new Object[]{2,"审核完成"});
		list.add(new Object[]{3,"审核驳回"});
		list.add(new Object[]{4,"确认驳回"});
		list.add(new Object[]{5,"确认完成"});
		return list;
	}
	
	public List<Object[]> getExpendApplyInfoList() {
		return expendApplyInfoList;
	}


	public void setExpendApplyInfoList(List<Object[]> expendApplyInfoList) {
		this.expendApplyInfoList = expendApplyInfoList;
	}


	public String getSearchKey() {
		return searchKey;
	}

	public void setSearchKey(String searchKey) {
		this.searchKey = searchKey;
	}

	public List<Object[]> getDepartList() {
		return departList;
	}

	public void setDepartList(List<Object[]> departList) {
		this.departList = departList;
	}

	public Integer getDepartmentId() {
		return departmentId;
	}

	public void setDepartmentId(Integer departmentId) {
		this.departmentId = departmentId;
	}

	public List<Object[]> getApplayUserList() {
		return applayUserList;
	}

	public void setApplayUserList(List<Object[]> applayUserList) {
		this.applayUserList = applayUserList;
	}

	public Integer getApplyUser() {
		return applyUser;
	}

	public void setApplyUser(Integer applyUser) {
		this.applyUser = applyUser;
	}


	public String getApplyTime() {
		return applyTime;
	}

	public void setApplyTime(String applyTime) {
		this.applyTime = applyTime;
	}

	public String getApplyEndTime() {
		return applyEndTime;
	}

	public void setApplyEndTime(String applyEndTime) {
		this.applyEndTime = applyEndTime;
	}

	public List<Object[]> getStatusList() {
		return statusList;
	}

	public void setStatusList(List<Object[]> statusList) {
		this.statusList = statusList;
	}



	public Integer getStatus() {
		return status;
	}



	public void setStatus(Integer status) {
		this.status = status;
	}
	
	
}
