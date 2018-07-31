package cn.dmdl.stl.hospitalbudget.budget.session;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
	private String getQuerySql(){
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
		String headStr = "支出审核表";
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
		range = new CellRangeAddress(0, 0, 0, 6);
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
		moneyCol.setCellValue("已支出金额");
		moneyCol.setCellStyle(colStyle);
		moneyCol.getSheet().setColumnWidth(moneyCol.getColumnIndex(),
				35 * 120);
		colIndex++;
		
/*		HSSFCell addMoneyCol = row1.createCell(colIndex);
		addMoneyCol.setCellType(HSSFCell.CELL_TYPE_STRING);
		addMoneyCol.setCellValue("可支出金额");
		addMoneyCol.setCellStyle(colStyle);
		addMoneyCol.getSheet().setColumnWidth(addMoneyCol.getColumnIndex(),
				35 * 200);
		colIndex++;*/
		
		HSSFCell addPercentCol = row1.createCell(colIndex);
		addPercentCol.setCellType(HSSFCell.CELL_TYPE_STRING);
		addPercentCol.setCellValue("执行率");
		addPercentCol.setCellStyle(colStyle);
		addPercentCol.getSheet().setColumnWidth(addPercentCol.getColumnIndex(),
				35 * 200);
		colIndex++;
		
		String sql = this.getQuerySql();
		List<Object[]> list = getEntityManager().createNativeQuery(sql.toString()).getResultList();
		
		
		for(int i = 0;i < list.size(); i++){
			Object[] obj =list.get(i);
			rowIndex ++;
			int col = 0;
			row1 = (HSSFRow) sheet.createRow(rowIndex);
			row1.setHeightInPoints(20f);
			numCol = row1.createCell(col);
			numCol.setCellType(HSSFCell.CELL_TYPE_STRING);
			numCol.setCellValue(obj[0].toString());
			numCol.setCellStyle(colStyle);
			numCol.getSheet().setColumnWidth(
					natureCol.getColumnIndex(), 35 * 120);
			col++;
			
			nameCol = row1.createCell(col);
			nameCol.setCellType(HSSFCell.CELL_TYPE_STRING);
			nameCol.setCellValue(obj[1].toString());
			nameCol.setCellStyle(colStyle);
			nameCol.getSheet().setColumnWidth(
					nameCol.getColumnIndex(), 35 * 120);
			col++;
			
			departCol = row1.createCell(col);
			departCol.setCellType(HSSFCell.CELL_TYPE_STRING);
			departCol.setCellValue(obj[2].toString());
			departCol.setCellStyle(colStyle);
			departCol.getSheet().setColumnWidth(
					departCol.getColumnIndex(), 35 * 120);
			col++;
			
			natureCol = row1.createCell(col);
			natureCol.setCellType(HSSFCell.CELL_TYPE_STRING);
			natureCol.setCellValue(obj[3].toString());
			natureCol.setCellStyle(colStyle);
			natureCol.getSheet().setColumnWidth(
					natureCol.getColumnIndex(), 35 * 120);
			col++;
			
			symbolCol = row1.createCell(col);
			symbolCol.setCellType(HSSFCell.CELL_TYPE_STRING);
			symbolCol.setCellValue(obj[4].toString());
			symbolCol.setCellStyle(colStyle);
			symbolCol.getSheet().setColumnWidth(
					symbolCol.getColumnIndex(), 35 * 120);
			col++;
			
			moneyCol = row1.createCell(col);
			moneyCol.setCellType(HSSFCell.CELL_TYPE_STRING);
			moneyCol.setCellValue(obj[5].toString());
			moneyCol.setCellStyle(colStyle);
			moneyCol.getSheet().setColumnWidth(
					moneyCol.getColumnIndex(), 35 * 120);
			col++;
			
/*			addMoneyCol = row1.createCell(col);
			addMoneyCol.setCellType(HSSFCell.CELL_TYPE_STRING);
			addMoneyCol.setCellValue(obj[6].toString());
			addMoneyCol.setCellStyle(colStyle);
			addMoneyCol.getSheet().setColumnWidth(
					addMoneyCol.getColumnIndex(), 35 * 200);
			col++;*/
			
			addPercentCol = row1.createCell(col);
			addPercentCol.setCellType(HSSFCell.CELL_TYPE_STRING);
			addPercentCol.setCellValue(obj[6].toString());
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
