package cn.dmdl.stl.hospitalbudget.admin.session;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.admin.entity.UserInfo;
import cn.dmdl.stl.hospitalbudget.common.session.CriterionEntityQuery;
import cn.dmdl.stl.hospitalbudget.util.DateTimeHelper;
import cn.dmdl.stl.hospitalbudget.util.ExportAsExcel;
import cn.dmdl.stl.hospitalbudget.util.GlobalConstant;

@Name("userInfoList")
public class UserInfoList extends CriterionEntityQuery<UserInfo> {

	private static final long serialVersionUID = 1L;
	private static final String EJBQL = "select userInfo from UserInfo userInfo where userInfo.deleted = 0";
	private UserInfo userInfo = new UserInfo();
	private String keyword;// 关键词

	// 可以携带参数 1 xls 2 doc...
	@SuppressWarnings("unchecked")
	public void export() {
		ExportAsExcel excel = new ExportAsExcel("用户管理");
		excel.setSheetCapacity((short) 1);
		excel.setSheetName(new String[] { "用户管理" });
		excel.setTitleCellValue(new String[] { "用户管理" });
		List<String[]> headerCellValue = new ArrayList<String[]>();
		headerCellValue.add(new String[] { "ID", "用户名", "角色", "昵称", "姓名", "性别", "生日", "手机", "电话", "邮箱", "地址", "已启用" });
		excel.setHeaderCellValue(headerCellValue);
		List<List<Object[]>> dataCellValue = new ArrayList<List<Object[]>>();
		List<Object[]> e1 = new ArrayList<Object[]>();

		StringBuffer sql = new StringBuffer();
		sql.append(" select");
		sql.append(" user_info.user_info_id,");
		sql.append(" user_info.username,");
		sql.append(" role_info.role_name,");
		sql.append(" user_info_extend.nickname,");
		sql.append(" user_info_extend.fullname,");
		sql.append(" user_info_extend.sex,");
		sql.append(" user_info_extend.birthday,");
		sql.append(" user_info_extend.cellphone,");
		sql.append(" user_info_extend.telephone,");
		sql.append(" user_info_extend.mail,");
		sql.append(" user_info_extend.address,");
		sql.append(" user_info.enabled");
		sql.append(" from user_info");
		sql.append(" inner join role_info on role_info.role_info_id = user_info.role_info_id and role_info.deleted = 0");
		sql.append(" inner join user_info_extend on user_info_extend.user_info_extend_id = user_info.user_info_extend_id and user_info_extend.deleted = 0");
		sql.append(" where user_info.deleted = 0");
		sql.append(" and role_info.role_info_id != " + GlobalConstant.ROLE_OF_ROOT);
		if (keyword != null && !"".equals(keyword)) {
			sql.append(" and (user_info.username like '%" + keyword + "%'");
			sql.append(" or user_info_extend.nickname like '%" + keyword + "%'");
			sql.append(" or user_info_extend.fullname like '%" + keyword + "%'");
			sql.append(" )");
		}
		sql.append(" order by user_info.user_info_id desc");
		sql.insert(0, "select * from (").append(") as resultset");

		List<Object[]> resultList = getEntityManager().createNativeQuery(sql.toString()).getResultList();
		if (resultList != null) {
			for (Object[] result : resultList) {
				String[] rowArr = new String[result.length];
				rowArr[0] = result[0] != null ? result[0].toString() : "";
				rowArr[1] = result[1] != null ? result[1].toString() : "";
				rowArr[2] = result[2] != null ? result[2].toString() : "";
				rowArr[3] = result[3] != null ? result[3].toString() : "";
				rowArr[4] = result[4] != null ? result[4].toString() : "";
				if (result[5] != null && !"".equals(result[5].toString())) {
					rowArr[5] = Integer.valueOf(result[5].toString()) == 1 ? "男" : "女";
				} else {
					rowArr[5] = "";
				}
				if (result[6] != null && !"".equals(result[6].toString())) {
					rowArr[6] = DateTimeHelper.dateToStr(DateTimeHelper.strToDate(result[6].toString(), DateTimeHelper.PATTERN_DATE), DateTimeHelper.PATTERN_DATE);
				} else {
					rowArr[6] = "";
				}
				rowArr[7] = result[7] != null ? result[7].toString() : "";
				rowArr[8] = result[8] != null ? result[8].toString() : "";
				rowArr[9] = result[9] != null ? result[9].toString() : "";
				rowArr[10] = result[10] != null ? result[10].toString() : "";
				if (result[11] != null) {
					rowArr[11] = Boolean.valueOf(result[11].toString()) ? "是" : "否";
				} else {
					rowArr[11] = "";
				}
				e1.add(rowArr);
			}
		}
		dataCellValue.add(e1);
		excel.setDataCellValue(dataCellValue);
		excel.save();
	}

	@Override
	protected Query createQuery() {
		StringBuffer sql = new StringBuffer(EJBQL);
		sql.append(" and userInfo.roleInfo.roleInfoId != " + GlobalConstant.ROLE_OF_ROOT);
		if (keyword != null && !"".equals(keyword)) {
			sql.append(" and (userInfo.username like '%" + keyword + "%'");
			sql.append(" or userInfo.userInfoExtend.nickname like '%" + keyword + "%'");
			sql.append(" or userInfo.userInfoExtend.fullname like '%" + keyword + "%'");
			sql.append(" )");
		}
		setEjbql(sql.toString());
		return super.createQuery();
	}

	public UserInfoList() {
		setEjbql(EJBQL);
		setAttribute("userInfo.userInfoId");
	}

	public UserInfo getUserInfo() {
		return userInfo;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
}
