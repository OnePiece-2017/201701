package cn.dmdl.stl.hospitalbudget.budget.session;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.faces.context.FacesContext;
import javax.persistence.Query;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

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

import cn.dmdl.stl.hospitalbudget.common.session.CriterionNativeQuery;
import cn.dmdl.stl.hospitalbudget.util.SessionToken;

@Name("queryExpendApplay")
public class QueryExpendApplay extends CriterionNativeQuery<Object[]> {

	private static final long serialVersionUID = 1L;
	
	@In
	protected SessionToken sessionToken;
	private List<Object[]> expendApplyInfoList;
	private List<Object[]> departList;//科室列表
	private List<Object[]> fundsSourceList;//资金来源列表
	private List<Object[]> projectList;//项目列表
	private String expendTime;//支出时间
	private String applyEndTime;//申请结束时间
	private String projectName;
	private Integer fundsSourceId;
	private Integer departId;
	
	private static final int FINA_ROLE_ID = 3;//财务人员角色id
	private static final int DIRECTOR_ROLE_ID = 4;//主任角色id
	
	@SuppressWarnings("unchecked")
	public void wire(){
		fundsSourceList = new ArrayList<Object[]>();
		departList = new ArrayList<Object[]>();
		projectList = new ArrayList<Object[]>();
		if(null == projectName){
			projectName = "";
		}
		if(null == expendTime){
			expendTime = "";
		}
		if(null == fundsSourceId){
			fundsSourceId = -1;
		}
		if(null == departId){
			departId = -1;
		}
		wireDepartmentInfo();
		//资金来源
		//初始化资金来源
		String sourceSql = "select yfs.the_id,yfs.the_value from ys_funds_source yfs where yfs.deleted=0";
		List<Object[]> list = getEntityManager().createNativeQuery(sourceSql).getResultList();
		if(list.size() > 0){
			fundsSourceList = list;
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	protected Query createQuery(){
		boolean privateRole = false;//角色不属于财务主任（领导的）
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
		sql.append(" eap.expend_apply_project_id, ");//1申请项目id
		sql.append(" eai.expend_apply_code, ");//2申请编号
		sql.append(" ysdi.the_value as depart_name, ");//3科室名称
		sql.append(" fs.the_value as source_name, ");//4资金来源
		sql.append(" up.the_value as project_name, ");//5项目名字
		sql.append(" eap.expend_money, ");//6支出金额
		sql.append(" uie.fullname, ");//7申请人名字
		sql.append(" eai.apply_time, ");//8申请提交时间
		sql.append(" ecp.expend_confirm_project_id, ");//9
		sql.append(" gp.the_value as generic_name, ");//10
		sql.append(" fs2.the_value as source_name2, ");//11
		sql.append(" ydi2.the_value as depart_name2, ");//12
		sql.append(" eap.project_id, ");//13
		sql.append(" eap.generic_project_id ");//14
		sql.append(" FROM expend_apply_project eap ");
		sql.append(" LEFT JOIN expend_apply_info eai ON eap.expend_apply_info_id = eai.expend_apply_info_id ");
		sql.append(" LEFT JOIN expend_confirm_project ecp on eap.expend_apply_project_id=ecp.expend_apply_project_id ");
		sql.append(" LEFT JOIN routine_project up ON eap.project_id = up.the_id ");
		sql.append(" LEFT JOIN ys_department_info ysdi on ysdi.the_id=up.department_info_id ");
		sql.append(" LEFT JOIN ys_funds_source fs ON fs.the_id = up.funds_source_id ");
		sql.append(" LEFT JOIN user_info ui ON ui.user_info_id = eai.applay_user_id ");
		sql.append(" LEFT JOIN user_info_extend uie ON ui.user_info_extend_id = uie.user_info_extend_id ");
		sql.append(" LEFT JOIN generic_project gp on eap.generic_project_id=gp.the_id ");
		sql.append(" LEFT JOIN ys_funds_source fs2 on fs2.the_id=gp.funds_source_id ");
		sql.append(" LEFT JOIN ys_department_info ydi2 on ydi2.the_id=gp.department_info_id ");
		sql.append(" LEFT JOIN expend_confirm_info eci on eai.expend_apply_info_id=eci.expend_apply_info_id ");
		sql.append(" where eap.deleted=0  and eai.deleted=0 and ecp.deleted=0 and eci.confirm_status=1 ");
		if(privateRole){
			sql.append(" and eai.insert_user= ").append(sessionToken.getUserInfoId());
		}
		if(Integer.valueOf(roleId) == 4){
			sql.append(" and eai.insert_user= ").append(sessionToken.getDepartmentInfoId());
		}
		if(null != projectName && !projectName.trim().equals("")){
			sql.append(" and (up.the_value like '%").append(projectName).append("%' or gp.the_value like '%").append(projectName).append("%')");
		}
		if(null != fundsSourceId && fundsSourceId != -1){
			sql.append(" and (up.funds_source_id= ").append(fundsSourceId).append(" or gp.funds_source_id=").append(fundsSourceId).append(")");
		}
		if(null != departId && departId != -1){
			sql.append(" and (up.department_info_id=").append(departId).append(" or gp.department_info_id=").append(departId).append(")");
		}
		if(null != expendTime && !expendTime.equals("")){
			sql.append(" and eai.apply_time>='").append(expendTime + " 00:00:00").append("'");
		}
		if(null != applyEndTime && !applyEndTime.equals("")){
			sql.append(" and eai.apply_time<'").append(applyEndTime + " 23:59:59").append("'");
		}
		sql.append(" order by eai.insert_time desc,eai.expend_apply_code desc");
		sql.insert(0, "select * from (").append(") as recordset");
		setEjbql(sql.toString());
		return super.createQuery();
	}

	public QueryExpendApplay() {
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
	/**
	 * 按条件查询
	 * @return
	 *//*
	@SuppressWarnings("unchecked")
	public void queryByCondition(){
		CommonFinder commonFinder = new CommonFinder();
		String roleSql = "select role_info.role_info_id from user_info LEFT JOIN role_info on role_info.role_info_id=user_info.role_info_id where user_info.user_info_id=" + sessionToken.getUserInfoId();
		List<Object> roleList = getEntityManager().createNativeQuery(roleSql).getResultList();
		Integer roleId = Integer.parseInt(roleList.get(0).toString());//角色id
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT ");
		sql.append(" eai.expend_apply_info_id, ");//0申请单id
		sql.append(" eai.expend_apply_code, ");//1申请编号
		sql.append(" eai.funds_source, ");//2资金来源
		sql.append(" ydi.the_value as depart_name, ");//3部门名字
		sql.append(" ycp.the_value as peoject_name, ");//4项目名字
		sql.append(" eai.expend_money, ");//5支出金额
		sql.append(" uie.fullname, ");//6申请人名字
		sql.append(" eai.insert_time ");//7申请提交时间
		sql.append(" FROM expend_apply_info eai ");
		sql.append(" LEFT JOIN ys_department_info ydi ON eai.department_info_id = ydi.the_id ");
		sql.append(" LEFT JOIN normal_expend_budget_order_info neboi on neboi.normal_expend_budget_order_id=eai.normal_expend_budget_order_id ");
		sql.append(" LEFT JOIN ys_convention_project ycp on ycp.the_id=neboi.normal_project_id ");
		sql.append(" LEFT JOIN user_info ui on ui.user_info_id=eai.applay_user_id ");
		sql.append(" LEFT JOIN user_info_extend uie on ui.user_info_extend_id=uie.user_info_extend_id ");
		sql.append(" where eai.deleted=0 ");
		if(roleId != 1 && roleId != FINA_ROLE_ID && roleId != DIRECTOR_ROLE_ID){
			sql.append(" eai.insert_user= ").append(sessionToken.getUserInfoId());
		}
		List<Object[]> list = getEntityManager().createNativeQuery("select * from (" + sql.toString() + ") as test").getResultList();
		if(list.size() > 0){
			expendApplyInfoList = list;
		}
		for(Object[] obj : list){
			obj[2] = commonFinder.getMoneySource(Integer.parseInt(obj[2].toString()));
		}
	}
*/
	

	/**
	 * 导出
	 */
	public void expExcel(){
		
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
		cellTitle.setCellValue("支出明细表");
		cellTitle.setCellStyle(colTitleStyle);
		range = new CellRangeAddress(0, 0, 0, 6);
		sheet.addMergedRegion(range);
		excelAddBorder(1, range, sheet, workbook);
		
		rowIndex ++;
		HSSFRow row1 = (HSSFRow) sheet.createRow(rowIndex);
		row1.setHeightInPoints(20f);
		HSSFCell deptCol = row1.createCell(colIndex);
		deptCol.setCellType(HSSFCell.CELL_TYPE_STRING);
		deptCol.setCellValue("部门");
		deptCol.setCellStyle(colStyle);
		deptCol.getSheet().setColumnWidth(
				deptCol.getColumnIndex(), 35 * 160);
		colIndex++;

		HSSFCell sourceCol = row1.createCell(colIndex);
		sourceCol.setCellType(HSSFCell.CELL_TYPE_STRING);
		sourceCol.setCellValue("资金来源");
		sourceCol.setCellStyle(colStyle);
		sourceCol.getSheet().setColumnWidth(sourceCol.getColumnIndex(),
				35 * 160);
		colIndex++;

		HSSFCell company = row1.createCell(colIndex);
		company.setCellType(HSSFCell.CELL_TYPE_STRING);
		company.setCellValue("收款单位");
		company.setCellStyle(colStyle);
		company.getSheet().setColumnWidth(company.getColumnIndex(),
				35 * 160);
		colIndex++;

		HSSFCell totalMoney = row1.createCell(colIndex);
		totalMoney.setCellType(HSSFCell.CELL_TYPE_STRING);
		totalMoney.setCellValue("单据总金额");
		totalMoney.setCellStyle(colStyle);
		totalMoney.getSheet().setColumnWidth(totalMoney.getColumnIndex(),
				35 * 90);
		colIndex++;
		
		HSSFCell reimbursement = row1.createCell(colIndex);
		reimbursement.setCellType(HSSFCell.CELL_TYPE_STRING);
		reimbursement.setCellValue("报销人");
		reimbursement.setCellStyle(colStyle);
		reimbursement.getSheet().setColumnWidth(reimbursement.getColumnIndex(),
				35 * 90);
		colIndex++;
		
		HSSFCell applyTime = row1.createCell(colIndex);
		applyTime.setCellType(HSSFCell.CELL_TYPE_STRING);
		applyTime.setCellValue("申请时间");
		applyTime.setCellStyle(colStyle);
		applyTime.getSheet().setColumnWidth(applyTime.getColumnIndex(),
				35 * 90);
		colIndex++;
		
		HSSFCell applyStatus = row1.createCell(colIndex);
		applyStatus.setCellType(HSSFCell.CELL_TYPE_STRING);
		applyStatus.setCellValue("单据状态");
		applyStatus.setCellStyle(colStyle);
		applyStatus.getSheet().setColumnWidth(applyStatus.getColumnIndex(),
				35 * 90);
		colIndex++;
		
		List<Object[]> list = queryExportData(projectName,fundsSourceId,departId,expendTime,applyEndTime);
		for(int i = 0;i < list.size(); i++){
			Object[] obj = list.get(i);
			rowIndex ++;
			int col = 0;
			row1 = (HSSFRow) sheet.createRow(rowIndex);
			row1.setHeightInPoints(20f);
			deptCol = row1.createCell(col);
			deptCol.setCellType(HSSFCell.CELL_TYPE_STRING);
			deptCol.setCellValue(obj[0].toString());
			deptCol.setCellStyle(colStyle);
			deptCol.getSheet().setColumnWidth(
					deptCol.getColumnIndex(), 35 * 160);
			col++;
			
			sourceCol = row1.createCell(col);
			sourceCol.setCellType(HSSFCell.CELL_TYPE_STRING);
			sourceCol.setCellValue(obj[1].toString());
			sourceCol.setCellStyle(colStyle);
			sourceCol.getSheet().setColumnWidth(
					sourceCol.getColumnIndex(), 35 * 160);
			col++;
			
			company = row1.createCell(col);
			company.setCellType(HSSFCell.CELL_TYPE_STRING);
			company.setCellValue(obj[2].toString());
			company.setCellStyle(colStyle);
			company.getSheet().setColumnWidth(
					company.getColumnIndex(), 35 * 160);
			col++;
			
			totalMoney = row1.createCell(col);
			totalMoney.setCellType(HSSFCell.CELL_TYPE_STRING);
			totalMoney.setCellValue(obj[3].toString());
			totalMoney.setCellStyle(colStyle);
			totalMoney.getSheet().setColumnWidth(
					totalMoney.getColumnIndex(), 35 * 90);
			col++;
			
			reimbursement = row1.createCell(col);
			reimbursement.setCellType(HSSFCell.CELL_TYPE_STRING);
			reimbursement.setCellValue(obj[4].toString());
			reimbursement.setCellStyle(colStyle);
			reimbursement.getSheet().setColumnWidth(
					reimbursement.getColumnIndex(), 35 * 90);
			col++;
			
			applyTime = row1.createCell(col);
			applyTime.setCellType(HSSFCell.CELL_TYPE_STRING);
			applyTime.setCellValue(obj[5].toString());
			applyTime.setCellStyle(colStyle);
			applyTime.getSheet().setColumnWidth(
					applyTime.getColumnIndex(), 35 * 90);
			col++;
			
			applyStatus = row1.createCell(col);
			applyStatus.setCellType(HSSFCell.CELL_TYPE_STRING);
			applyStatus.setCellValue(obj[6].toString());
			applyStatus.setCellStyle(colStyle);
			applyStatus.getSheet().setColumnWidth(
					applyStatus.getColumnIndex(), 35 * 90);
			col++;
		}
		
		
		response.setContentType("application/vnd.ms-excel");
		try {
			response.setHeader("Content-Disposition", "attachment;filename="
					+ new String("支出明细.xls".getBytes(),
							"iso-8859-1"));
			workbook.write(response.getOutputStream());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
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
	 * 查询导出数据
	 * @param projectName
	 * @param sourceId
	 * @param departId
	 * @param time
	 */
	@SuppressWarnings("unchecked")
	public List<Object[]> queryExportData(String projectName,Integer sourceId,Integer departId,String time,String applyEndTime){
		boolean privateRole = false;//角色不属于财务主任（领导的）
		String roleSql = "select role_info.role_info_id,user_info.department_info_id,ydi.the_value from user_info LEFT JOIN role_info on role_info.role_info_id=user_info.role_info_id LEFT JOIN ys_department_info ydi on "
				+ "user_info.department_info_id=ydi.the_id where user_info.user_info_id=" + sessionToken.getUserInfoId();
		
		List<Object[]> roleList = getEntityManager().createNativeQuery(roleSql).getResultList();
		int roleId = Integer.parseInt(roleList.get(0)[0].toString());//角色id
		
		if(Integer.valueOf(roleId) != 1 && Integer.valueOf(roleId) != 2){
			privateRole = true;
		}
		//查询导出数据
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		StringBuffer routinSql = new StringBuffer();
		routinSql.append(" SELECT ");
		routinSql.append(" eap.project_id, ");
		routinSql.append(" eap.generic_project_id, ");
		routinSql.append(" di.the_value as depart_name, ");
		routinSql.append(" di1.the_value as depart_name1, ");
		routinSql.append(" fs.the_value as source, ");
		routinSql.append(" fs1.the_value as source1,");
		routinSql.append(" eai.recive_company, ");
		routinSql.append(" ecp.confirm_money, ");
		routinSql.append(" eai.reimbursementer, ");
		routinSql.append(" eai.apply_time, ");
		routinSql.append(" eci.confirm_status, ");
		routinSql.append(" rp.the_value as rp_name, ");
		routinSql.append(" gp.the_value as gp_name, ");
		routinSql.append(" rp.department_info_id as di_id, ");
		routinSql.append(" gp.department_info_id as di1_id, ");
		routinSql.append(" rp.funds_source_id as fs_id, ");
		routinSql.append(" gp.funds_source_id as fs1_id,eai.insert_user ");
		routinSql.append(" FROM expend_confirm_project ecp ");
		routinSql.append(" LEFT JOIN expend_confirm_info eci ON ecp.expend_confirm_info_id = eci.expend_confirm_info_id ");
		routinSql.append(" LEFT JOIN expend_apply_project eap on eap.expend_apply_project_id=ecp.expend_apply_project_id ");
		routinSql.append(" LEFT JOIN expend_apply_info eai on eai.expend_apply_info_id=eap.expend_apply_info_id ");
		routinSql.append(" LEFT JOIN routine_project rp on rp.the_id=eap.project_id ");
		routinSql.append(" LEFT JOIN generic_project gp on gp.the_id=eap.generic_project_id ");
		routinSql.append(" LEFT JOIN ys_department_info di on di.the_id=rp.department_info_id ");
		routinSql.append(" LEFT JOIN ys_department_info di1 on di1.the_id=gp.department_info_id ");
		routinSql.append(" LEFT JOIN ys_funds_source fs on fs.the_id=rp.funds_source_id ");
		routinSql.append(" LEFT JOIN ys_funds_source fs1 on fs1.the_id=gp.funds_source_id ");
		routinSql.append(" where eai.deleted=0 and eap.deleted=0 and ecp.deleted=0 and eci.confirm_status=1 ");
		routinSql.append(" order by eai.insert_time desc,eai.expend_apply_code desc ");
		StringBuffer sql = new StringBuffer();
		sql.append("select * from (");
		sql.append(routinSql);
		sql.append(") as test where 1=1 ");
		if(privateRole){
			sql.append(" and test.insert_user= ").append(sessionToken.getUserInfoId());
		}
		if(null != projectName && !"".equals(projectName)){
			sql.append(" and (test.rp_name like '%").append(projectName).append("%' or test.rp_name like '%").append(projectName).append("%')");
		}
		if(null != sourceId && sourceId != -1 && sourceId != 0){
			sql.append(" and (test.di_id = ").append(sourceId).append(" or test.di1_id=").append(sourceId).append(")");
		}
		if(null != departId && departId != -1 && departId != 0){
			sql.append(" and (test.fs_id=").append(departId).append(" or test.fs1_id=").append(departId).append(")");
		}
		if(null != time && !"".equals(time)){
			sql.append(" and test.apply_time>='").append(time + " 00:00:00").append("'");
		}
		if(null != applyEndTime && !applyEndTime.equals("")){
			sql.append(" and test.apply_time<='").append(applyEndTime + " 23:59:59").append("'");
		}
		List<Object[]> list = getEntityManager().createNativeQuery(sql.toString()).getResultList();
		List<Object[]> returnList = new ArrayList<Object[]>();
		if(null != list && list.size() > 0){
			for(Object[] obj : list){
				Object[] project = new Object[7];
				int status = obj[10] == null ? 0 : Integer.parseInt(obj[10].toString());
				if(null != obj[0] && !"".equals(obj[0].toString())){
					project[0] = obj[2];
					project[1] = obj[4];
					
				}else{
					project[0] = obj[3];
					project[1] = obj[5];
				}
				project[2] = obj[6];
				project[3] = obj[7] == null ? 0:obj[7];
				project[4] = obj[8];
				project[5] = "";
				try {
					if(null != obj[9]){
						project[5] = sdf.format(sdf.parse(obj[9].toString()));
					}
				} catch (ParseException e) {
					
				}
				project[6] = status == 1 ? "已通过" : "不通过";
				returnList.add(project);
			}
		}
		return returnList;
	}
	public List<Object[]> getExpendApplyInfoList() {
		return expendApplyInfoList;
	}


	public void setExpendApplyInfoList(List<Object[]> expendApplyInfoList) {
		this.expendApplyInfoList = expendApplyInfoList;
	}


	public List<Object[]> getDepartList() {
		return departList;
	}


	public void setDepartList(List<Object[]> departList) {
		this.departList = departList;
	}


	public List<Object[]> getFundsSourceList() {
		return fundsSourceList;
	}


	public void setFundsSourceList(List<Object[]> fundsSourceList) {
		this.fundsSourceList = fundsSourceList;
	}


	public List<Object[]> getProjectList() {
		return projectList;
	}


	public void setProjectList(List<Object[]> projectList) {
		this.projectList = projectList;
	}


	public String getExpendTime() {
		return expendTime;
	}


	public void setExpendTime(String expendTime) {
		this.expendTime = expendTime;
	}




	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public Integer getFundsSourceId() {
		return fundsSourceId;
	}


	public void setFundsSourceId(Integer fundsSourceId) {
		this.fundsSourceId = fundsSourceId;
	}


	public Integer getDepartId() {
		return departId;
	}


	public void setDepartId(Integer departId) {
		this.departId = departId;
	}

	public String getApplyEndTime() {
		return applyEndTime;
	}

	public void setApplyEndTime(String applyEndTime) {
		this.applyEndTime = applyEndTime;
	}
	
	
}
