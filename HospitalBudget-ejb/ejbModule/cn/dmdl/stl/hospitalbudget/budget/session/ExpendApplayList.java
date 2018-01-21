package cn.dmdl.stl.hospitalbudget.budget.session;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

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
import cn.dmdl.stl.hospitalbudget.util.SessionToken;
import cn.dmdl.stl.hospitalbudget.util.SqlServerJDBCUtil;

@Name("expendApplayList")
public class ExpendApplayList extends CriterionNativeQuery<Object[]> {

	private static final long serialVersionUID = 1L;
	
	@In
	protected SessionToken sessionToken;
	private List<Object[]> expendApplyInfoList;
	private String searchKey;//搜索条件
	
	private static final int FINA_ROLE_ID = 3;//财务人员角色id
	private static final int DIRECTOR_ROLE_ID = 4;//主任角色id
	
	@SuppressWarnings("unchecked")
	public void wire(){
		
//		boolean privateRole = false;//角色不属于财务 和主任（领导的）
//		String roleSql = "select role_info.role_info_id,user_info.department_info_id,ydi.the_value from user_info LEFT JOIN role_info on role_info.role_info_id=user_info.role_info_id "
//				+ "LEFT JOIN ys_department_info ydi on user_info.department_info_id=ydi.the_id where user_info.user_info_id=" + sessionToken.getUserInfoId();
//		List<Object[]> roleList = getEntityManager().createNativeQuery("select * from (" + roleSql + ") as test").getResultList();
//		int roleId = Integer.parseInt(roleList.get(0)[0].toString());//角色id
//		
//		if(Integer.valueOf(roleId) != 1 && Integer.valueOf(roleId) != FINA_ROLE_ID && Integer.valueOf(roleId) != DIRECTOR_ROLE_ID){
//			privateRole = true;
//		}
//		expendApplyInfoList = this.createQuery().getResultList();
		
		//从新耗材系统获取支出数据
		this.syncNewConsumables();
		
		//在老耗材系统获取支出数据
		this.syncOldConsumables();
		
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
				expendApplyInfo.setTotalMoney(totalMoney.floatValue());
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
						Float budg_year_money = Float.parseFloat(nepiList.get(0)[2].toString());//年预算
						Float budg_year_out_money = Float.parseFloat(nepiList.get(0)[3].toString());//已支出
						Float disbursable_money = Float.parseFloat(nepiList.get(0)[4].toString());//可支出
						
						ExpendApplyProject eap = new ExpendApplyProject();//申请项目
						eap.setExpendApplyInfoId(expendApplyInfo.getExpendApplyInfoId());
						if(null != nepiList.get(0)[1]){
							eap.setProjectId(Integer.valueOf(nepiList.get(0)[1].toString()));
						}else{
							eap.setGenericProjectId(Integer.valueOf(nepiList.get(0)[2].toString()));
						}
						eap.setExpendBeforFrozen(budg_year_out_money);
						eap.setExpendBeforSurplus(disbursable_money);
						eap.setExpendMoney(totalMoney.floatValue());
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
					eci.setTotalMoney(0f);
					eci.setConfirm_status(0);
					eci.setYear(expendApplyInfo.getYear());
					eci.setDeleted(false);
					getEntityManager().persist(eci);
					
					
					if(null != nepiList &&nepiList.size() > 0){
						Float budg_year_money = Float.parseFloat(nepiList.get(0)[2].toString());//年预算
						Float budg_year_out_money = Float.parseFloat(nepiList.get(0)[3].toString());//已支出
						Float disbursable_money = Float.parseFloat(nepiList.get(0)[4].toString());//可支出
						
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
						eap.setExpendMoney(totalMoney.floatValue());
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
				Float totalMoney = rs.getFloat("this_payment_money");//支出
				Float budg_year_money = rs.getFloat("budg_year_money");//年预算
				Float budg_year_out_money = rs.getFloat("budg_year_out_money");//已支出
				Float disbursable_money = rs.getFloat("disbursable_money");//可支出
				String year = rs.getString("budg_year");//预算nian
				String userSql = "select a.user_info_id from user_info a LEFT JOIN user_info_extend b "
						+ " on b.user_info_extend_id=a.user_info_extend_id "
						+ " where b.fullname ='"+oper+"'";
				String budgCode = rs.getString("budg_code");
				
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
				expendApplyInfo.setExplendSource(1);
				getEntityManager().persist(expendApplyInfo);
				
				String nepiSql = "select normal_expend_plan_id,project_id,generic_project_id from normal_expend_plan_info where normal_expend_plan_id=" + budgCode;
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
					eci.setTotalMoney(0f);
					eci.setConfirm_status(0);
					eci.setYear(expendApplyInfo.getYear());
					eci.setDeleted(false);
					getEntityManager().persist(eci);
					
					
					if(null != nepiList &&nepiList.size() > 0){
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
		if(null != searchKey && !searchKey.equals("")){
			sql.append(" and (eai.expend_apply_code = '%").append(searchKey).append("%' eai.finace_account_name LIKE '%").append(searchKey).append("%')");
		}
		sql.append(" order by eai.insert_time desc ");
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
	
	
}
