package cn.dmdl.stl.hospitalbudget.budget.session;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.List;

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

import cn.dmdl.stl.hospitalbudget.common.session.CriterionNativeQuery;
import cn.dmdl.stl.hospitalbudget.util.HospitalConstant;

/**
 * @author HASEE
 *
 */
@Name("disburseStatisticalList")
public class DisburseStatisticalList extends CriterionNativeQuery<Object[]> {

	private static final long serialVersionUID = 1L;
	private String beginYearParam;
	private String fundsSourceId;
	private String departmentIds;
	private String projectName;
	private String startTime;
	private String endTime;//截止时间

	@In(create = true)
	CommonDaoHome commonDaoHome;

	@Override
	protected Query createQuery() {
		String sql = this.getQuerySql();

		setEjbql(sql);
		return super.createQuery();
	}
	
	/**
	 * 根据页面条件拼接sql
	 * @return
	 */
	/*private String getQuerySql(){
		StringBuilder sql = new StringBuilder();
		sql.append("select temp.normal_expend_plan_id, ");
		sql.append("temp.pval, ");
		sql.append("temp.dval,   ");
		sql.append("temp.project_type,   ");
		sql.append("temp.budget_amount, ");
		sql.append("ifnull(temp.expend_money, 0), ");
		sql.append("ifnull(temp.execute_percent, 0) from ( ");
		sql.append("select test.normal_expend_plan_id, ");
		sql.append("test.pval, ");
		sql.append("test.dval,   ");
		sql.append("'项目' as project_type, ");
		sql.append("cast(test.budget_amount as decimal(18, 2)) as budget_amount, ");
		sql.append("cast(sum(test.confirm_money) as decimal(18,2)) as expend_money, ");
		sql.append("cast(sum(test.confirm_money)/test.budget_amount*100 as decimal(18,2)) as execute_percent from ( ");
		sql.append("SELECT temp_expend.confirm_money,di.the_value as dval,gp.the_value as pval, ");
		sql.append("di.the_id,nepi.budget_amount,nepi.generic_project_id,nepi.normal_expend_plan_id ");
		sql.append("FROM normal_expend_plan_info nepi ");
		sql.append("LEFT JOIN ( ");
		sql.append("select ecp.confirm_money, ecp.generic_project_id, ecp.project_id from hospital_budget.expend_confirm_info eci  ");
		sql.append("INNER JOIN hospital_budget.expend_confirm_project ecp ON eci.expend_confirm_info_id = ecp.expend_confirm_info_id ");
		sql.append("WHERE eci.deleted = 0 AND ecp.deleted = 0 ");
		sql.append("AND eci.confirm_status = 1 ");
		if(null != startTime && !"".equals(startTime)){
			sql.append("AND eci.confirm_time >= '").append(startTime).append(" 00:00:00' ");
		}
		if(null != endTime && !"".equals(endTime)){
			sql.append("AND eci.confirm_time <= '").append(endTime).append(" 23:59:59' ");
		}
		sql.append(") temp_expend ON temp_expend.generic_project_id = nepi.generic_project_id ");
		sql.append("INNER JOIN generic_project gp ON gp.the_id = nepi.generic_project_id ");
		sql.append("LEFT JOIN ys_department_info di ON di.the_id = nepi.dept_id ");
		sql.append("where 1=1 ");
		if(null != beginYearParam && !"".equals(beginYearParam)){
			sql.append(" and nepi.`year` = '").append(beginYearParam).append("' ");
		}
		if(null != projectName && !"".equals(projectName)){
			sql.append(" and gp.the_value like '%").append(projectName).append("%' ");
		}
		if(null != departmentIds && !"".equals(departmentIds)){
			sql.append(" and di.the_id in (").append(departmentIds).append(") ");
		}else{
			sql.append(" and di.the_id = ").append(sessionToken.getDepartmentInfoId()).append(" ");
		}
		sql.append(") as test ");
		sql.append("group by test.generic_project_id ");
		sql.append("union all ");
		sql.append("select test.normal_expend_plan_id, ");
		sql.append("test.pval, ");
		sql.append("test.dval, ");
		sql.append("'常规项目' as project_type, ");
		sql.append("cast(test.budget_amount as decimal(18, 2)) as budget_amount, ");
		sql.append("cast(sum(test.confirm_money) as decimal(18,2)) as expend_money, ");
		sql.append("cast(sum(test.confirm_money)/test.budget_amount*100 as decimal(18,2)) as execute_percent from ( ");
		sql.append("SELECT temp_expend.confirm_money,di.the_value as dval,rp.the_value as pval, ");
		sql.append("di.the_id,nepi.budget_amount,nepi.project_id,nepi.normal_expend_plan_id ");
		sql.append("FROM normal_expend_plan_info nepi ");
		sql.append("LEFT JOIN ( ");
		sql.append("select ecp.confirm_money, ecp.generic_project_id, ecp.project_id from hospital_budget.expend_confirm_info eci  ");
		sql.append("INNER JOIN hospital_budget.expend_confirm_project ecp ON eci.expend_confirm_info_id = ecp.expend_confirm_info_id ");
		sql.append("WHERE eci.deleted = 0 AND ecp.deleted = 0 ");
		sql.append("AND eci.confirm_status = 1 ");
		if(null != startTime && !"".equals(startTime)){
			sql.append("AND eci.confirm_time >= '").append(startTime).append(" 00:00:00' ");
		}
		if(null != endTime && !"".equals(endTime)){
			sql.append("AND eci.confirm_time <= '").append(endTime).append(" 23:59:59' ");
		}
		sql.append(") temp_expend ON temp_expend.project_id = nepi.project_id ");
		sql.append("INNER JOIN routine_project rp ON rp.the_id = nepi.project_id ");
		sql.append("LEFT JOIN ys_department_info di ON di.the_id = nepi.dept_id ");
		sql.append("where 1=1 ");
		if(null != beginYearParam && !"".equals(beginYearParam)){
			sql.append(" and nepi.`year` = '").append(beginYearParam).append("' ");
		}
		if(null != projectName && !"".equals(projectName)){
			sql.append(" and rp.the_value like '%").append(projectName).append("%' ");
		}
		if(null != departmentIds && !"".equals(departmentIds)){
			sql.append(" and di.the_id in (").append(departmentIds).append(") ");
		}else{
			sql.append(" and di.the_id = ").append(sessionToken.getDepartmentInfoId()).append(" ");
		}
		sql.append(") as test ");
		sql.append("group by test.project_id ");
		sql.append(") temp ORDER BY temp.dval ");
		System.out.println(beginYearParam+"----"+departmentIds+"----"+projectName+ "----" + endTime);
		System.out.println(sql);
		return sql.toString();
	}*/
	
	
	
	
	private String getQuerySql(){
		if(null == beginYearParam || "".equals(beginYearParam)){
			Calendar date = Calendar.getInstance();
	        String year = String.valueOf(date.get(Calendar.YEAR));
			beginYearParam = year;
		}
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT p.the_id as project_id, ");
		sql.append("p.the_value as project_name, ");
		sql.append("di.the_value as dept_name, ");
		sql.append("1 as project_type, ");
		sql.append("CAST(nepi.budget_amount AS DECIMAL (18, 2)) as budget_amount, ");
		sql.append("CAST(nepi.budget_amount - temp_apply.expend_money AS DECIMAL (18, 2)) as can_use_money, ");
		sql.append("CAST(temp_confirm.confirm_money AS DECIMAL (18, 2)) as confirm_money, ");
		sql.append("CAST(temp_confirm.confirm_money/nepi.budget_amount*100 AS DECIMAL (18, 2)) as execute_percent, ");
		sql.append("CAST(MAX(CASE temp_month_apply.expend_month WHEN '01' THEN temp_month_apply.expend_money ELSE 0 END) AS DECIMAL(18,2)) AS jan_expend_money, ");
		sql.append("CAST(MAX(CASE temp_month_apply.expend_month WHEN '02' THEN temp_month_apply.expend_money ELSE 0 END) AS DECIMAL(18,2)) AS feb_expend_money, ");
		sql.append("CAST(MAX(CASE temp_month_apply.expend_month WHEN '03' THEN temp_month_apply.expend_money ELSE 0 END) AS DECIMAL(18,2)) AS mar_expend_money, ");
		sql.append("CAST(MAX(CASE temp_month_apply.expend_month WHEN '04' THEN temp_month_apply.expend_money ELSE 0 END) AS DECIMAL(18,2)) AS apr_expend_money, ");
		sql.append("CAST(MAX(CASE temp_month_apply.expend_month WHEN '05' THEN temp_month_apply.expend_money ELSE 0 END) AS DECIMAL(18,2)) AS may_expend_money, ");
		sql.append("CAST(MAX(CASE temp_month_apply.expend_month WHEN '06' THEN temp_month_apply.expend_money ELSE 0 END) AS DECIMAL(18,2)) AS jun_expend_money, ");
		sql.append("CAST(MAX(CASE temp_month_apply.expend_month WHEN '07' THEN temp_month_apply.expend_money ELSE 0 END) AS DECIMAL(18,2)) AS jul_expend_money, ");
		sql.append("CAST(MAX(CASE temp_month_apply.expend_month WHEN '08' THEN temp_month_apply.expend_money ELSE 0 END) AS DECIMAL(18,2)) AS aug_expend_money, ");
		sql.append("CAST(MAX(CASE temp_month_apply.expend_month WHEN '09' THEN temp_month_apply.expend_money ELSE 0 END) AS DECIMAL(18,2)) AS sep_expend_money, ");
		sql.append("CAST(MAX(CASE temp_month_apply.expend_month WHEN '10' THEN temp_month_apply.expend_money ELSE 0 END) AS DECIMAL(18,2)) AS oct_expend_money, ");
		sql.append("CAST(MAX(CASE temp_month_apply.expend_month WHEN '11' THEN temp_month_apply.expend_money ELSE 0 END) AS DECIMAL(18,2)) AS nov_expend_money, ");
		sql.append("CAST(MAX(CASE temp_month_apply.expend_month WHEN '12' THEN temp_month_apply.expend_money ELSE 0 END) AS DECIMAL(18,2)) AS dec_expend_money "); 
		sql.append("from hospital_budget.normal_expend_plan_info nepi ");
		sql.append("INNER JOIN hospital_budget.routine_project p ON nepi.project_id = p.the_id ");
		sql.append("INNER JOIN hospital_budget.ys_department_info di ON p.department_info_id = di.the_id ");
		sql.append("LEFT JOIN ( ");
		sql.append("SELECT sum(ecp.confirm_money) as confirm_money,ecp.project_id FROM hospital_budget.expend_confirm_info eci ");
		sql.append("LEFT JOIN hospital_budget.expend_confirm_project ecp ON eci.expend_confirm_info_id = ecp.expend_confirm_info_id ");
		sql.append("WHERE eci.deleted = 0 ");
		sql.append("AND ecp.deleted = 0 ");
		sql.append("AND eci.confirm_status = 1 ");
		sql.append("AND eci.`year` = '").append(beginYearParam).append("' ");
		if(null != startTime && !"".equals(startTime)){
			sql.append("AND eci.confirm_time >= '").append(startTime).append(" 00:00:00' ");
		}
		if(null != endTime && !"".equals(endTime)){
			sql.append("AND eci.confirm_time <= '").append(endTime).append(" 23:59:59' ");
		}
		sql.append("GROUP BY ecp.project_id ");
		sql.append(") temp_confirm ON temp_confirm.project_id = nepi.project_id ");
		sql.append("LEFT JOIN ( ");
		sql.append("SELECT sum(eap.expend_money) as expend_money,eap.project_id FROM hospital_budget.expend_apply_info eai ");
		sql.append("INNER JOIN hospital_budget.expend_apply_project eap ON eai.expend_apply_info_id = eap.expend_apply_info_id ");
		sql.append("WHERE eai.deleted = 0 ");
		sql.append("AND eap.deleted = 0 ");
		sql.append("AND eai.expend_apply_status in (").append(HospitalConstant.getExpendApplyValidStatus()).append(") ");
		sql.append("AND eai.`year` = '").append(beginYearParam).append("' ");
		sql.append("GROUP BY eap.project_id ");
		sql.append(") temp_apply ON temp_apply.project_id = nepi.project_id ");
		sql.append("LEFT JOIN ( ");
		sql.append("SELECT DATE_FORMAT(eai.apply_time,'%m') AS expend_month, sum(eap.expend_money) AS expend_money, eap.project_id ");
		sql.append("FROM hospital_budget.expend_apply_info eai ");
		sql.append("INNER JOIN hospital_budget.expend_apply_project eap ON eai.expend_apply_info_id = eap.expend_apply_info_id ");
		sql.append("INNER JOIN hospital_budget.routine_project p ON eap.project_id = p.the_id ");
		sql.append("WHERE eai.deleted = 0 ");
		sql.append("AND eap.deleted = 0 ");
		sql.append("AND eai.expend_apply_status IN (").append(HospitalConstant.getExpendApplyValidStatus()).append(") ");
		sql.append("AND eai.`year` = '").append(beginYearParam).append("' ");
		sql.append("GROUP BY eap.project_id,DATE_FORMAT(eai.apply_time,'%m') ");
		sql.append(") temp_month_apply ON temp_month_apply.project_id = nepi.project_id ");
		sql.append("WHERE 1=1 ");
		sql.append("AND nepi.`year` = '").append(beginYearParam).append("' ");
		if(null != departmentIds && !"".equals(departmentIds)){
			sql.append("AND p.department_info_id in (").append(departmentIds).append(") ");
		}else{
			sql.append("AND p.department_info_id = ").append(sessionToken.getDepartmentInfoId()).append(" ");
		}
		if(null != projectName && !"".equals(projectName)){
			sql.append("AND p.the_value LIKE '%").append(projectName).append("%' ");
		}
		sql.append("GROUP BY p.the_id ");
		sql.append("UNION ALL ");
		sql.append("SELECT p.the_id as project_id, ");
		sql.append("p.the_value as project_name, ");
		sql.append("di.the_value as dept_name, ");
		sql.append("2 as project_type, ");
		sql.append("CAST(nepi.budget_amount AS DECIMAL (18, 2)) as budget_amount, ");
		sql.append("CAST(nepi.budget_amount - temp_apply.expend_money AS DECIMAL (18, 2)) as can_use_money, ");
		sql.append("CAST(temp_confirm.confirm_money AS DECIMAL (18, 2)) as confirm_money, ");
		sql.append("CAST(temp_confirm.confirm_money/nepi.budget_amount*100 AS DECIMAL (18, 2)) as execute_percent, ");
		sql.append("CAST(MAX(CASE temp_month_apply.expend_month WHEN '01' THEN temp_month_apply.expend_money ELSE 0 END) AS DECIMAL(18,2)) AS jan_expend_money, ");
		sql.append("CAST(MAX(CASE temp_month_apply.expend_month WHEN '02' THEN temp_month_apply.expend_money ELSE 0 END) AS DECIMAL(18,2)) AS feb_expend_money, ");
		sql.append("CAST(MAX(CASE temp_month_apply.expend_month WHEN '03' THEN temp_month_apply.expend_money ELSE 0 END) AS DECIMAL(18,2)) AS mar_expend_money, ");
		sql.append("CAST(MAX(CASE temp_month_apply.expend_month WHEN '04' THEN temp_month_apply.expend_money ELSE 0 END) AS DECIMAL(18,2)) AS apr_expend_money, ");
		sql.append("CAST(MAX(CASE temp_month_apply.expend_month WHEN '05' THEN temp_month_apply.expend_money ELSE 0 END) AS DECIMAL(18,2)) AS may_expend_money, ");
		sql.append("CAST(MAX(CASE temp_month_apply.expend_month WHEN '06' THEN temp_month_apply.expend_money ELSE 0 END) AS DECIMAL(18,2)) AS jun_expend_money, ");
		sql.append("CAST(MAX(CASE temp_month_apply.expend_month WHEN '07' THEN temp_month_apply.expend_money ELSE 0 END) AS DECIMAL(18,2)) AS jul_expend_money, ");
		sql.append("CAST(MAX(CASE temp_month_apply.expend_month WHEN '08' THEN temp_month_apply.expend_money ELSE 0 END) AS DECIMAL(18,2)) AS aug_expend_money, ");
		sql.append("CAST(MAX(CASE temp_month_apply.expend_month WHEN '09' THEN temp_month_apply.expend_money ELSE 0 END) AS DECIMAL(18,2)) AS sep_expend_money, ");
		sql.append("CAST(MAX(CASE temp_month_apply.expend_month WHEN '10' THEN temp_month_apply.expend_money ELSE 0 END) AS DECIMAL(18,2)) AS oct_expend_money, ");
		sql.append("CAST(MAX(CASE temp_month_apply.expend_month WHEN '11' THEN temp_month_apply.expend_money ELSE 0 END) AS DECIMAL(18,2)) AS nov_expend_money, ");
		sql.append("CAST(MAX(CASE temp_month_apply.expend_month WHEN '12' THEN temp_month_apply.expend_money ELSE 0 END) AS DECIMAL(18,2)) AS dec_expend_money "); 
		sql.append("from hospital_budget.normal_expend_plan_info nepi ");
		sql.append("INNER JOIN hospital_budget.generic_project p ON nepi.generic_project_id = p.the_id ");
		sql.append("INNER JOIN hospital_budget.ys_department_info di ON p.department_info_id = di.the_id ");
		sql.append("LEFT JOIN ( ");
		sql.append("SELECT sum(ecp.confirm_money) as confirm_money,ecp.generic_project_id FROM hospital_budget.expend_confirm_info eci ");
		sql.append("LEFT JOIN hospital_budget.expend_confirm_project ecp ON eci.expend_confirm_info_id = ecp.expend_confirm_info_id ");
		sql.append("WHERE eci.deleted = 0 ");
		sql.append("AND ecp.deleted = 0 ");
		sql.append("AND eci.confirm_status = 1 ");
		sql.append("AND eci.`year` = '").append(beginYearParam).append("' ");
		if(null != startTime && !"".equals(startTime)){
			sql.append("AND eci.confirm_time >= '").append(startTime).append(" 00:00:00' ");
		}
		if(null != endTime && !"".equals(endTime)){
			sql.append("AND eci.confirm_time <= '").append(endTime).append(" 23:59:59' ");
		}
		sql.append("GROUP BY ecp.generic_project_id ");
		sql.append(") temp_confirm ON temp_confirm.generic_project_id = nepi.generic_project_id ");
		sql.append("LEFT JOIN ( ");
		sql.append("SELECT sum(eap.expend_money) as expend_money,eap.generic_project_id FROM hospital_budget.expend_apply_info eai ");
		sql.append("INNER JOIN hospital_budget.expend_apply_project eap ON eai.expend_apply_info_id = eap.expend_apply_info_id ");
		sql.append("WHERE eai.deleted = 0 ");
		sql.append("AND eap.deleted = 0 ");
		sql.append("AND eai.expend_apply_status in (").append(HospitalConstant.getExpendApplyValidStatus()).append(") ");
		sql.append("AND eai.`year` = '").append(beginYearParam).append("' ");
		sql.append("GROUP BY eap.generic_project_id ");
		sql.append(") temp_apply ON temp_apply.generic_project_id = nepi.generic_project_id ");
		sql.append("LEFT JOIN ( ");
		sql.append("SELECT DATE_FORMAT(eai.apply_time,'%m') AS expend_month, sum(eap.expend_money) AS expend_money, eap.generic_project_id ");
		sql.append("FROM hospital_budget.expend_apply_info eai ");
		sql.append("INNER JOIN hospital_budget.expend_apply_project eap ON eai.expend_apply_info_id = eap.expend_apply_info_id ");
		sql.append("INNER JOIN hospital_budget.generic_project p ON eap.generic_project_id = p.the_id ");
		sql.append("WHERE eai.deleted = 0 ");
		sql.append("AND eap.deleted = 0 ");
		sql.append("AND eai.expend_apply_status IN (").append(HospitalConstant.getExpendApplyValidStatus()).append(") ");
		sql.append("AND eai.`year` = '").append(beginYearParam).append("' ");
		sql.append("GROUP BY eap.generic_project_id,DATE_FORMAT(eai.apply_time,'%m') ");
		sql.append(") temp_month_apply ON temp_month_apply.generic_project_id = nepi.generic_project_id ");
		sql.append("WHERE 1=1 ");
		sql.append("AND nepi.`year` = '").append(beginYearParam).append("' ");
		if(null != departmentIds && !"".equals(departmentIds)){
			sql.append("AND p.department_info_id in (").append(departmentIds).append(") ");
		}else{
			sql.append("AND p.department_info_id = ").append(sessionToken.getDepartmentInfoId()).append(" ");
		}
		if(null != projectName && !"".equals(projectName)){
			sql.append("AND p.the_value LIKE '%").append(projectName).append("%' ");
		}
		sql.append("GROUP BY p.the_id ");
		System.out.println(beginYearParam+"----"+departmentIds+"----"+projectName+ "----" + endTime);
		System.out.println(sql);
		String resultSql = "select * from (" + sql.toString() + ") t ";
		return resultSql;
	}
	
	/**
	 * 导出
	 */
	@SuppressWarnings("unchecked")
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
		String headStr = "综合查询";
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
		cellTitle.setCellValue("综合查询");
		cellTitle.setCellStyle(colTitleStyle);
		range = new CellRangeAddress(0, 0, 0, 7);
		sheet.addMergedRegion(range);
		excelAddBorder(1, range, sheet, workbook);
		
		rowIndex ++;
		HSSFRow row1 = (HSSFRow) sheet.createRow(rowIndex);
		row1.setHeightInPoints(20f);
		
		HSSFCell numCol = row1.createCell(colIndex);
		numCol.setCellType(HSSFCell.CELL_TYPE_STRING);
		numCol.setCellValue("ID");
		numCol.setCellStyle(colStyle);
		numCol.getSheet().setColumnWidth(numCol.getColumnIndex(),
				35 * 120);
		colIndex++;
		
		HSSFCell nameCol = row1.createCell(colIndex);
		nameCol.setCellType(HSSFCell.CELL_TYPE_STRING);
		nameCol.setCellValue("项目名称");
		nameCol.setCellStyle(colStyle);
		nameCol.getSheet().setColumnWidth(
				nameCol.getColumnIndex(), 35 * 120);
		colIndex++;

		HSSFCell departCol = row1.createCell(colIndex);
		departCol.setCellType(HSSFCell.CELL_TYPE_STRING);
		departCol.setCellValue("科室");
		departCol.setCellStyle(colStyle);
		departCol.getSheet().setColumnWidth(departCol.getColumnIndex(),
				35 * 200);
		colIndex++;
		
		
		HSSFCell natureCol = row1.createCell(colIndex);
		natureCol.setCellType(HSSFCell.CELL_TYPE_STRING);
		natureCol.setCellValue("项目类型");
		natureCol.setCellStyle(colStyle);
		natureCol.getSheet().setColumnWidth(natureCol.getColumnIndex(),
				35 * 120);
		colIndex++;

		HSSFCell symbolCol = row1.createCell(colIndex);
		symbolCol.setCellType(HSSFCell.CELL_TYPE_STRING);
		symbolCol.setCellValue("预算金额");
		symbolCol.setCellStyle(colStyle);
		symbolCol.getSheet().setColumnWidth(symbolCol.getColumnIndex(),
				35 * 120);
		colIndex++;

		HSSFCell moneyCol = row1.createCell(colIndex);
		moneyCol.setCellType(HSSFCell.CELL_TYPE_STRING);
		moneyCol.setCellValue("可支出金额");
		moneyCol.setCellStyle(colStyle);
		moneyCol.getSheet().setColumnWidth(moneyCol.getColumnIndex(),
				35 * 120);
		colIndex++;
		
		HSSFCell addMoneyCol = row1.createCell(colIndex);
		addMoneyCol.setCellType(HSSFCell.CELL_TYPE_STRING);
		addMoneyCol.setCellValue("已支出金额");
		addMoneyCol.setCellStyle(colStyle);
		addMoneyCol.getSheet().setColumnWidth(addMoneyCol.getColumnIndex(),
				35 * 200);
		colIndex++;
		
		HSSFCell addPercentCol = row1.createCell(colIndex);
		addPercentCol.setCellType(HSSFCell.CELL_TYPE_STRING);
		addPercentCol.setCellValue("执行率(%)");
		addPercentCol.setCellStyle(colStyle);
		addPercentCol.getSheet().setColumnWidth(addPercentCol.getColumnIndex(),
				35 * 200);
		colIndex++;
		
		String sql = this.getQuerySql();
		List<Object[]> list = getEntityManager().createNativeQuery(sql.toString()).getResultList();
		
		
		for(int i = 0;i < list.size(); i++){
			Object[] obj =list.get(i);
			rowIndex ++;
			//ID
			int col = 0;
			row1 = (HSSFRow) sheet.createRow(rowIndex);
			row1.setHeightInPoints(20f);
			numCol = row1.createCell(col);
			numCol.setCellType(HSSFCell.CELL_TYPE_STRING);
			numCol.setCellValue(null == obj[0] ? "" : obj[0].toString());
			numCol.setCellStyle(colStyle);
			numCol.getSheet().setColumnWidth(
					natureCol.getColumnIndex(), 35 * 120);
			col++;
			
			//项目名称
			nameCol = row1.createCell(col);
			nameCol.setCellType(HSSFCell.CELL_TYPE_STRING);
			nameCol.setCellValue(null == obj[1] ? "" : obj[1].toString());
			nameCol.setCellStyle(colStyle);
			nameCol.getSheet().setColumnWidth(
					nameCol.getColumnIndex(), 35 * 120);
			col++;
			
			//科室名称
			departCol = row1.createCell(col);
			departCol.setCellType(HSSFCell.CELL_TYPE_STRING);
			departCol.setCellValue(null == obj[2] ? "" : obj[2].toString());
			departCol.setCellStyle(colStyle);
			departCol.getSheet().setColumnWidth(
					departCol.getColumnIndex(), 35 * 120);
			col++;
			
			//项目类型
			
			String projectTypeStr = null == obj[3] ? "" : obj[3].toString();
			natureCol = row1.createCell(col);
			natureCol.setCellType(HSSFCell.CELL_TYPE_STRING);
			if("1".equals(projectTypeStr)){
				natureCol.setCellValue("常规项目");
			}else if("2".equals(projectTypeStr)){
				natureCol.setCellValue("项目");
			}
			natureCol.setCellStyle(colStyle);
			natureCol.getSheet().setColumnWidth(
					natureCol.getColumnIndex(), 35 * 120);
			col++;
			
			//项目预算
			symbolCol = row1.createCell(col);
			symbolCol.setCellType(HSSFCell.CELL_TYPE_STRING);
			String budgetAmountStr = null == obj[4] ? "0.00" : obj[4].toString();
			symbolCol.setCellValue(budgetAmountStr);
			symbolCol.setCellStyle(colStyle);
			symbolCol.getSheet().setColumnWidth(
					symbolCol.getColumnIndex(), 35 * 120);
			col++;
			
			//可使用金额
			moneyCol = row1.createCell(col);
			moneyCol.setCellType(HSSFCell.CELL_TYPE_STRING);
			moneyCol.setCellValue(null == obj[5] ? budgetAmountStr : obj[5].toString());
			moneyCol.setCellStyle(colStyle);
			moneyCol.getSheet().setColumnWidth(
					moneyCol.getColumnIndex(), 35 * 120);
			col++;
			
			//已支出金额
			addMoneyCol = row1.createCell(col);
			addMoneyCol.setCellType(HSSFCell.CELL_TYPE_STRING);
			addMoneyCol.setCellValue(null == obj[6] ? "0.00" : obj[6].toString());
			addMoneyCol.setCellStyle(colStyle);
			addMoneyCol.getSheet().setColumnWidth(
					addMoneyCol.getColumnIndex(), 35 * 200);
			col++;
			
			addPercentCol = row1.createCell(col);
			addPercentCol.setCellType(HSSFCell.CELL_TYPE_STRING);
			addPercentCol.setCellValue(null == obj[7] ? "0.00" : obj[7].toString());
			addPercentCol.setCellStyle(colStyle);
			addPercentCol.getSheet().setColumnWidth(
					addPercentCol.getColumnIndex(), 35 * 200);
			col++;
			
			
		}
		
		response.setContentType("application/vnd.ms-excel");
		try {
			response.setHeader("Content-Disposition", "attachment;filename="
					+ new String("综合查询.xls".getBytes(),
							"iso-8859-1"));
			workbook.write(response.getOutputStream());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	/**
	 * 导出按月统计表
	 */
	@SuppressWarnings("unchecked")
	public void expMonthExcel(){
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
		String headStr = "按月综合查询";
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
		cellTitle.setCellValue("按月综合查询");
		cellTitle.setCellStyle(colTitleStyle);
		range = new CellRangeAddress(0, 0, 0, 19);
		sheet.addMergedRegion(range);
		excelAddBorder(1, range, sheet, workbook);
		
		rowIndex ++;
		HSSFRow row1 = (HSSFRow) sheet.createRow(rowIndex);
		row1.setHeightInPoints(20f);
		
		HSSFCell numCol = row1.createCell(colIndex);
		numCol.setCellType(HSSFCell.CELL_TYPE_STRING);
		numCol.setCellValue("ID");
		numCol.setCellStyle(colStyle);
		numCol.getSheet().setColumnWidth(numCol.getColumnIndex(),
				35 * 120);
		colIndex++;
		
		HSSFCell nameCol = row1.createCell(colIndex);
		nameCol.setCellType(HSSFCell.CELL_TYPE_STRING);
		nameCol.setCellValue("项目名称");
		nameCol.setCellStyle(colStyle);
		nameCol.getSheet().setColumnWidth(
				nameCol.getColumnIndex(), 35 * 120);
		colIndex++;

		HSSFCell departCol = row1.createCell(colIndex);
		departCol.setCellType(HSSFCell.CELL_TYPE_STRING);
		departCol.setCellValue("科室");
		departCol.setCellStyle(colStyle);
		departCol.getSheet().setColumnWidth(departCol.getColumnIndex(),
				35 * 200);
		colIndex++;
		
		
		HSSFCell natureCol = row1.createCell(colIndex);
		natureCol.setCellType(HSSFCell.CELL_TYPE_STRING);
		natureCol.setCellValue("项目类型");
		natureCol.setCellStyle(colStyle);
		natureCol.getSheet().setColumnWidth(natureCol.getColumnIndex(),
				35 * 120);
		colIndex++;

		HSSFCell symbolCol = row1.createCell(colIndex);
		symbolCol.setCellType(HSSFCell.CELL_TYPE_STRING);
		symbolCol.setCellValue("预算金额");
		symbolCol.setCellStyle(colStyle);
		symbolCol.getSheet().setColumnWidth(symbolCol.getColumnIndex(),
				35 * 120);
		colIndex++;

		HSSFCell moneyCol = row1.createCell(colIndex);
		moneyCol.setCellType(HSSFCell.CELL_TYPE_STRING);
		moneyCol.setCellValue("可支出金额");
		moneyCol.setCellStyle(colStyle);
		moneyCol.getSheet().setColumnWidth(moneyCol.getColumnIndex(),
				35 * 120);
		colIndex++;
		
		HSSFCell addMoneyCol = row1.createCell(colIndex);
		addMoneyCol.setCellType(HSSFCell.CELL_TYPE_STRING);
		addMoneyCol.setCellValue("已支出金额");
		addMoneyCol.setCellStyle(colStyle);
		addMoneyCol.getSheet().setColumnWidth(addMoneyCol.getColumnIndex(),
				35 * 200);
		colIndex++;
		
		HSSFCell addPercentCol = row1.createCell(colIndex);
		addPercentCol.setCellType(HSSFCell.CELL_TYPE_STRING);
		addPercentCol.setCellValue("执行率(%)");
		addPercentCol.setCellStyle(colStyle);
		addPercentCol.getSheet().setColumnWidth(addPercentCol.getColumnIndex(),
				35 * 200);
		colIndex++;
		
		HSSFCell janCol = row1.createCell(colIndex);
		janCol.setCellType(HSSFCell.CELL_TYPE_STRING);
		janCol.setCellValue("一月");
		janCol.setCellStyle(colStyle);
		janCol.getSheet().setColumnWidth(addPercentCol.getColumnIndex(),
				35 * 200);
		colIndex++;
		
		HSSFCell febCol = row1.createCell(colIndex);
		febCol.setCellType(HSSFCell.CELL_TYPE_STRING);
		febCol.setCellValue("二月");
		febCol.setCellStyle(colStyle);
		febCol.getSheet().setColumnWidth(addPercentCol.getColumnIndex(),
				35 * 200);
		colIndex++;
		
		HSSFCell marCol = row1.createCell(colIndex);
		marCol.setCellType(HSSFCell.CELL_TYPE_STRING);
		marCol.setCellValue("三月");
		marCol.setCellStyle(colStyle);
		marCol.getSheet().setColumnWidth(addPercentCol.getColumnIndex(),
				35 * 200);
		colIndex++;
		
		HSSFCell aprCol = row1.createCell(colIndex);
		aprCol.setCellType(HSSFCell.CELL_TYPE_STRING);
		aprCol.setCellValue("四月");
		aprCol.setCellStyle(colStyle);
		aprCol.getSheet().setColumnWidth(addPercentCol.getColumnIndex(),
				35 * 200);
		colIndex++;
		
		HSSFCell mayCol = row1.createCell(colIndex);
		mayCol.setCellType(HSSFCell.CELL_TYPE_STRING);
		mayCol.setCellValue("五月");
		mayCol.setCellStyle(colStyle);
		mayCol.getSheet().setColumnWidth(addPercentCol.getColumnIndex(),
				35 * 200);
		colIndex++;
		
		HSSFCell junCol = row1.createCell(colIndex);
		junCol.setCellType(HSSFCell.CELL_TYPE_STRING);
		junCol.setCellValue("六月");
		junCol.setCellStyle(colStyle);
		junCol.getSheet().setColumnWidth(addPercentCol.getColumnIndex(),
				35 * 200);
		colIndex++;
		
		HSSFCell julCol = row1.createCell(colIndex);
		julCol.setCellType(HSSFCell.CELL_TYPE_STRING);
		julCol.setCellValue("七月");
		julCol.setCellStyle(colStyle);
		julCol.getSheet().setColumnWidth(addPercentCol.getColumnIndex(),
				35 * 200);
		colIndex++;
		
		HSSFCell augCol = row1.createCell(colIndex);
		augCol.setCellType(HSSFCell.CELL_TYPE_STRING);
		augCol.setCellValue("八月");
		augCol.setCellStyle(colStyle);
		augCol.getSheet().setColumnWidth(addPercentCol.getColumnIndex(),
				35 * 200);
		colIndex++;
		
		HSSFCell sepCol = row1.createCell(colIndex);
		sepCol.setCellType(HSSFCell.CELL_TYPE_STRING);
		sepCol.setCellValue("九月");
		sepCol.setCellStyle(colStyle);
		sepCol.getSheet().setColumnWidth(addPercentCol.getColumnIndex(),
				35 * 200);
		colIndex++;
		
		HSSFCell octCol = row1.createCell(colIndex);
		octCol.setCellType(HSSFCell.CELL_TYPE_STRING);
		octCol.setCellValue("十月");
		octCol.setCellStyle(colStyle);
		octCol.getSheet().setColumnWidth(addPercentCol.getColumnIndex(),
				35 * 200);
		colIndex++;
		
		HSSFCell novCol = row1.createCell(colIndex);
		novCol.setCellType(HSSFCell.CELL_TYPE_STRING);
		novCol.setCellValue("十一月");
		novCol.setCellStyle(colStyle);
		novCol.getSheet().setColumnWidth(addPercentCol.getColumnIndex(),
				35 * 200);
		colIndex++;
		
		HSSFCell decCol = row1.createCell(colIndex);
		decCol.setCellType(HSSFCell.CELL_TYPE_STRING);
		decCol.setCellValue("十二月");
		decCol.setCellStyle(colStyle);
		decCol.getSheet().setColumnWidth(addPercentCol.getColumnIndex(),
				35 * 200);
		colIndex++;
		
		String sql = this.getQuerySql();
		List<Object[]> list = getEntityManager().createNativeQuery(sql.toString()).getResultList();
		
		
		for(int i = 0;i < list.size(); i++){
			Object[] obj =list.get(i);
			rowIndex ++;
			//ID
			int col = 0;
			row1 = (HSSFRow) sheet.createRow(rowIndex);
			row1.setHeightInPoints(20f);
			numCol = row1.createCell(col);
			numCol.setCellType(HSSFCell.CELL_TYPE_STRING);
			numCol.setCellValue(null == obj[0] ? "" : obj[0].toString());
			numCol.setCellStyle(colStyle);
			numCol.getSheet().setColumnWidth(
					natureCol.getColumnIndex(), 35 * 120);
			col++;
			
			//项目名称
			nameCol = row1.createCell(col);
			nameCol.setCellType(HSSFCell.CELL_TYPE_STRING);
			nameCol.setCellValue(null == obj[1] ? "" : obj[1].toString());
			nameCol.setCellStyle(colStyle);
			nameCol.getSheet().setColumnWidth(
					nameCol.getColumnIndex(), 35 * 120);
			col++;
			
			//科室名称
			departCol = row1.createCell(col);
			departCol.setCellType(HSSFCell.CELL_TYPE_STRING);
			departCol.setCellValue(null == obj[2] ? "" : obj[2].toString());
			departCol.setCellStyle(colStyle);
			departCol.getSheet().setColumnWidth(
					departCol.getColumnIndex(), 35 * 120);
			col++;
			
			//项目类型
			
			String projectTypeStr = null == obj[3] ? "" : obj[3].toString();
			natureCol = row1.createCell(col);
			natureCol.setCellType(HSSFCell.CELL_TYPE_STRING);
			if("1".equals(projectTypeStr)){
				natureCol.setCellValue("常规项目");
			}else if("2".equals(projectTypeStr)){
				natureCol.setCellValue("项目");
			}
			natureCol.setCellStyle(colStyle);
			natureCol.getSheet().setColumnWidth(
					natureCol.getColumnIndex(), 35 * 120);
			col++;
			
			//项目预算
			symbolCol = row1.createCell(col);
			symbolCol.setCellType(HSSFCell.CELL_TYPE_STRING);
			String budgetAmountStr = null == obj[4] ? "0.00" : obj[4].toString();
			symbolCol.setCellValue(budgetAmountStr);
			symbolCol.setCellStyle(colStyle);
			symbolCol.getSheet().setColumnWidth(
					symbolCol.getColumnIndex(), 35 * 120);
			col++;
			
			//可使用金额
			moneyCol = row1.createCell(col);
			moneyCol.setCellType(HSSFCell.CELL_TYPE_STRING);
			moneyCol.setCellValue(null == obj[5] ? budgetAmountStr : obj[5].toString());
			moneyCol.setCellStyle(colStyle);
			moneyCol.getSheet().setColumnWidth(
					moneyCol.getColumnIndex(), 35 * 120);
			col++;
			
			//已支出金额
			addMoneyCol = row1.createCell(col);
			addMoneyCol.setCellType(HSSFCell.CELL_TYPE_STRING);
			addMoneyCol.setCellValue(null == obj[6] ? "0.00" : obj[6].toString());
			addMoneyCol.setCellStyle(colStyle);
			addMoneyCol.getSheet().setColumnWidth(
					addMoneyCol.getColumnIndex(), 35 * 200);
			col++;
			
			//执行率
			addPercentCol = row1.createCell(col);
			addPercentCol.setCellType(HSSFCell.CELL_TYPE_STRING);
			addPercentCol.setCellValue(null == obj[7] ? "0.00" : obj[7].toString());
			addPercentCol.setCellStyle(colStyle);
			addPercentCol.getSheet().setColumnWidth(
					addPercentCol.getColumnIndex(), 35 * 200);
			col++;
			
			//1月
			janCol = row1.createCell(col);
			janCol.setCellType(HSSFCell.CELL_TYPE_STRING);
			janCol.setCellValue(obj[8].toString());
			janCol.setCellStyle(colStyle);
			janCol.getSheet().setColumnWidth(
					janCol.getColumnIndex(), 35 * 200);
			col++;
			
			//2月
			febCol = row1.createCell(col);
			febCol.setCellType(HSSFCell.CELL_TYPE_STRING);
			febCol.setCellValue(obj[9].toString());
			febCol.setCellStyle(colStyle);
			febCol.getSheet().setColumnWidth(
					janCol.getColumnIndex(), 35 * 200);
			col++;
			
			//3月
			marCol = row1.createCell(col);
			marCol.setCellType(HSSFCell.CELL_TYPE_STRING);
			marCol.setCellValue(obj[10].toString());
			marCol.setCellStyle(colStyle);
			marCol.getSheet().setColumnWidth(
					marCol.getColumnIndex(), 35 * 200);
			col++;
			
			//4月
			aprCol = row1.createCell(col);
			aprCol.setCellType(HSSFCell.CELL_TYPE_STRING);
			aprCol.setCellValue(obj[11].toString());
			aprCol.setCellStyle(colStyle);
			aprCol.getSheet().setColumnWidth(
					aprCol.getColumnIndex(), 35 * 200);
			col++;
			
			//5月
			mayCol = row1.createCell(col);
			mayCol.setCellType(HSSFCell.CELL_TYPE_STRING);
			mayCol.setCellValue(obj[12].toString());
			mayCol.setCellStyle(colStyle);
			mayCol.getSheet().setColumnWidth(
					mayCol.getColumnIndex(), 35 * 200);
			col++;
			
			//6月
			junCol = row1.createCell(col);
			junCol.setCellType(HSSFCell.CELL_TYPE_STRING);
			junCol.setCellValue(obj[13].toString());
			junCol.setCellStyle(colStyle);
			junCol.getSheet().setColumnWidth(
					junCol.getColumnIndex(), 35 * 200);
			col++;
			
			//7月
			julCol = row1.createCell(col);
			julCol.setCellType(HSSFCell.CELL_TYPE_STRING);
			julCol.setCellValue(obj[14].toString());
			julCol.setCellStyle(colStyle);
			julCol.getSheet().setColumnWidth(
					julCol.getColumnIndex(), 35 * 200);
			col++;
			
			//8月
			augCol = row1.createCell(col);
			augCol.setCellType(HSSFCell.CELL_TYPE_STRING);
			augCol.setCellValue(obj[15].toString());
			augCol.setCellStyle(colStyle);
			augCol.getSheet().setColumnWidth(
					augCol.getColumnIndex(), 35 * 200);
			col++;
			
			//9月
			sepCol = row1.createCell(col);
			sepCol.setCellType(HSSFCell.CELL_TYPE_STRING);
			sepCol.setCellValue(obj[16].toString());
			sepCol.setCellStyle(colStyle);
			sepCol.getSheet().setColumnWidth(
					sepCol.getColumnIndex(), 35 * 200);
			col++;
			
			//10月
			octCol = row1.createCell(col);
			octCol.setCellType(HSSFCell.CELL_TYPE_STRING);
			octCol.setCellValue(obj[17].toString());
			octCol.setCellStyle(colStyle);
			octCol.getSheet().setColumnWidth(
					octCol.getColumnIndex(), 35 * 200);
			col++;
			
			//11月
			novCol = row1.createCell(col);
			novCol.setCellType(HSSFCell.CELL_TYPE_STRING);
			novCol.setCellValue(obj[18].toString());
			novCol.setCellStyle(colStyle);
			novCol.getSheet().setColumnWidth(
					novCol.getColumnIndex(), 35 * 200);
			col++;
			
			//12月
			decCol = row1.createCell(col);
			decCol.setCellType(HSSFCell.CELL_TYPE_STRING);
			decCol.setCellValue(obj[19].toString());
			decCol.setCellStyle(colStyle);
			decCol.getSheet().setColumnWidth(
					decCol.getColumnIndex(), 35 * 200);
			col++;
			
			
		}
		
		response.setContentType("application/vnd.ms-excel");
		try {
			response.setHeader("Content-Disposition", "attachment;filename="
					+ new String("按月综合查询.xls".getBytes(),
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

	public DisburseStatisticalList() {
		setEjbql("");
		setAttribute("");
	}

	public String getFundsSourceId() {
		return fundsSourceId;
	}

	public void setFundsSourceId(String fundsSourceId) {
		this.fundsSourceId = fundsSourceId;
	}

	public String getBeginYearParam() {
		return beginYearParam;
	}

	public void setBeginYearParam(String beginYearParam) {
		this.beginYearParam = beginYearParam;
	}

	public String getDepartmentIds() {
		return departmentIds;
	}

	public void setDepartmentIds(String departmentIds) {
		this.departmentIds = departmentIds;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	
}
