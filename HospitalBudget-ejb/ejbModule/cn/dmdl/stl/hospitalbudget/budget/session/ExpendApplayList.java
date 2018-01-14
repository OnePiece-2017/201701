package cn.dmdl.stl.hospitalbudget.budget.session;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.admin.entity.UserInfo;
import cn.dmdl.stl.hospitalbudget.budget.entity.ExpendApplyInfo;
import cn.dmdl.stl.hospitalbudget.budget.entity.ExpendApplyProject;
import cn.dmdl.stl.hospitalbudget.budget.entity.ExpendConfirmInfo;
import cn.dmdl.stl.hospitalbudget.budget.entity.ExpendConfirmProject;
import cn.dmdl.stl.hospitalbudget.common.session.CriterionNativeQuery;
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
				String userSql = "select user_info_id from user_info where username='"+oper+"'";
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
				
				ExpendConfirmInfo eci = new ExpendConfirmInfo();
				eci.setExpend_apply_info_id(expendApplyInfo.getExpendApplyInfoId());
				eci.setInsertUser(userId);
				eci.setInsertTime(expendApplyInfo.getInsertTime());
				eci.setTotalMoney(0f);
				eci.setConfirm_status(0);
				eci.setYear(expendApplyInfo.getYear());
				eci.setDeleted(false);
				getEntityManager().persist(eci);
				
				String nepiSql = "select normal_expend_plan_id,project_id,generic_project_id from normal_expend_plan_info where normal_expend_plan_id=" + budgCode;
				List<Object[]> nepiList= getEntityManager().createNativeQuery(nepiSql).getResultList();
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
		
		if(Integer.valueOf(roleId) != 1 && Integer.valueOf(roleId) != FINA_ROLE_ID && Integer.valueOf(roleId) != DIRECTOR_ROLE_ID){
			privateRole = true;
		}
		UserInfo userInfo = getEntityManager().find(UserInfo.class, sessionToken.getUserInfoId());
		Integer departId = null;
		if(userInfo.getUserInfoId() != 1){
			departId = userInfo.getYsDepartmentInfo().getTheId();
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
		if(null != departId){
			sql.append(" and ui.department_info_id= ").append(departId);
		}
		
		if(privateRole){
			sql.append(" and eai.insert_user= ").append(sessionToken.getUserInfoId());
		}
		if(null != searchKey && !searchKey.equals("")){
			sql.append(" and (eai.expend_apply_code = '%").append(searchKey).append("%' eai.finace_account_name LIKE '%").append(searchKey).append("%')");
		}
		sql.insert(0, "select * from (").append(") as recordset");
		System.out.println(sql);
		setEjbql(sql.toString());
		return super.createQuery();
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
