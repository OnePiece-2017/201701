package cn.dmdl.stl.hospitalbudget.budget.session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.common.session.CriterionNativeQuery;
import cn.dmdl.stl.hospitalbudget.util.SessionToken;

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
		
		boolean privateRole = false;//角色不属于财务 和主任（领导的）
		String roleSql = "select role_info.role_info_id,user_info.department_info_id,ydi.the_value from user_info LEFT JOIN role_info on role_info.role_info_id=user_info.role_info_id "
				+ "LEFT JOIN ys_department_info ydi on user_info.department_info_id=ydi.the_id where user_info.user_info_id=" + sessionToken.getUserInfoId();
		List<Object[]> roleList = getEntityManager().createNativeQuery("select * from (" + roleSql + ") as test").getResultList();
		int roleId = Integer.parseInt(roleList.get(0)[0].toString());//角色id
		
		if(Integer.valueOf(roleId) != 1 && Integer.valueOf(roleId) != FINA_ROLE_ID && Integer.valueOf(roleId) != DIRECTOR_ROLE_ID){
			privateRole = true;
		}
		expendApplyInfoList = this.createQuery().getResultList();
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
		sql.append(" eai.`comment`, ");//9备注
		sql.append(" eai.expend_apply_status ");//10状态
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
		sql.insert(0, "select * from (").append(") as recordset");
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
