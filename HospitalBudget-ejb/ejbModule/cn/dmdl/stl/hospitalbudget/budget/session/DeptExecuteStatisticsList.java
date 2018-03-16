package cn.dmdl.stl.hospitalbudget.budget.session;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
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

import cn.dmdl.stl.hospitalbudget.common.session.CriterionNativeQuery;
import cn.dmdl.stl.hospitalbudget.util.DateTimeHelper;

@Name("deptExecuteStatisticsList")
public class DeptExecuteStatisticsList extends CriterionNativeQuery<Object[]>{
	private static final long serialVersionUID = 1L;
	private List<Object[]> beginYear;// 预算年份select
	private List<Object[]> fundsSourceList;// 资金来源list
	private List<Object[]> departmentInfoList;// 科室select
	private String beginYearParam = DateTimeHelper.getNowYear();
	private String beginMonth;
	private String endMonth;
	private String isFlag;
	private String departmentInfoId;
	private String fundsSourceId;
	private String departIds;

	@In(create = true)
	CommonDaoHome commonDaoHome;

	@Override
	protected Query createQuery() {
		boolean privateRole = false;//角色不属于财务主任 和管理员
		String roleSql = "select role_info.role_info_id,user_info.department_info_id,ydi.the_value from user_info LEFT JOIN role_info on role_info.role_info_id=user_info.role_info_id LEFT JOIN ys_department_info ydi on "
				+ "user_info.department_info_id=ydi.the_id where user_info.user_info_id=" + sessionToken.getUserInfoId();
		
		List<Object[]> roleList = getEntityManager().createNativeQuery(roleSql).getResultList();
		int roleId = Integer.parseInt(roleList.get(0)[0].toString());//角色id
		
		if(Integer.valueOf(roleId) != 1 && Integer.valueOf(roleId) != 2){
			privateRole = true;
		}
		StringBuffer sql = new StringBuffer();
		sql.append(" select di.the_value,");
		sql.append(" SUM(nepi.budget_amount) as total_mount,");
		sql.append(" SUM(nepi.budget_amount - nepi.budget_amount_surplus) as surplus,");
		sql.append(" ROUND(SUM(nepi.budget_amount - nepi.budget_amount_surplus)/SUM(nepi.budget_amount) * 100,2)  as percent ");
		sql.append(" from normal_expend_plan_info nepi");
		sql.append(" left join generic_project gp on gp.the_id = nepi.generic_project_id");
		sql.append(" left join routine_project rp on rp.the_id = nepi.project_id");
		sql.append(" INNER JOIN ys_department_info di ON nepi.dept_id = di.the_id");
//		sql.append(" left join ys_department_info ydi on ydi.the_id = gp.department_info_id");
//		sql.append(" left join ys_department_info ydi1 on ydi1.the_id = rp.department_info_id");
		sql.append(" where 1 = 1");
//		String wc = commonDaoHome.getDepartmentInfoListByUserIdWhereCondition();
//		if (wc != null && !"".equals(wc)) {
//			sql.append(" and routine_project.department_info_id in (" + wc + ")");
//		}
		
		if(null != beginYearParam && !"".equals(beginYearParam)){
			sql.append(" and nepi.year = ").append(beginYearParam);
		}
		if(privateRole){
			sql.append(" and (gp.department_info_id = ").append(sessionToken.getDepartmentInfoId());
			sql.append(" or rp.department_info_id = ").append(sessionToken.getDepartmentInfoId()).append(") ");
		}
		if(null != departIds && !"".equals(departIds)){
			sql.append(" and (gp.department_info_id in (").append(departIds).append(")");
			sql.append(" or rp.department_info_id in (").append(departIds).append(")) ");
		}
		sql.append(" GROUP BY nepi.dept_id");
		sql.insert(0, "select recordset.the_value,FORMAT(recordset.total_mount,2),FORMAT(recordset.surplus,2),recordset.percent from (").append(") as recordset");
		System.out.println(beginYearParam+"---------"+departmentInfoId+"--------------"+fundsSourceId);
		setEjbql(sql.toString());
		return super.createQuery();
	}
	
	public void wire() {
		wireBudgetYearList();
		wireFundsSource();// 资金来源list
		wireDepartmentInfo();// 主管科室list
	}
	
	/** 预算年份 */
	public void wireBudgetYearList() {
		if (beginYear != null) {
			beginYear.clear();
		} else {
			beginYear = new ArrayList<Object[]>();
		}
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, -1);
		for (int i = 0; i < 3; i++) {
			beginYear.add(new Object[] { calendar.get(Calendar.YEAR), calendar.get(Calendar.YEAR) + "年" });
			calendar.add(Calendar.YEAR, +1);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void wireFundsSource() {
		if (fundsSourceList != null) {
			fundsSourceList.clear();
		} else {
			fundsSourceList = new ArrayList<Object[]>();
		}
		fundsSourceList.add(new Object[] { "", "请选择" });
		String dataSql = "select the_id, the_value from ys_funds_source where deleted = 0";
		List<Object[]> dataList = getEntityManager().createNativeQuery(dataSql).getResultList();
		if (dataList != null && dataList.size() > 0) {
			for (Object[] data : dataList) {
				fundsSourceList.add(new Object[] { data[0], data[1] });
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void wireDepartmentInfo() {
		if (departmentInfoList != null) {
			departmentInfoList.clear();
		} else {
			departmentInfoList = new ArrayList<Object[]>();
		}
		departmentInfoList.add(new Object[] { "", "请选择" });
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
				departmentInfoList.add(new Object[] { root, valueMap.get(root) });
				disposeLeaf(departmentInfoList, nexusMap, valueMap, 1, nexusMap.get(root));
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
		String headStr = "执行查询表";
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
		cellTitle.setCellValue("执行查询");
		cellTitle.setCellStyle(colTitleStyle);
		range = new CellRangeAddress(0, 0, 0, 3);
		sheet.addMergedRegion(range);
		excelAddBorder(1, range, sheet, workbook);
		
		rowIndex ++;
		HSSFRow row1 = (HSSFRow) sheet.createRow(rowIndex);
		row1.setHeightInPoints(20f);
		
		
		HSSFCell nameCol = row1.createCell(colIndex);
		nameCol.setCellType(HSSFCell.CELL_TYPE_STRING);
		nameCol.setCellValue("部门名称");
		nameCol.setCellStyle(colStyle);
		nameCol.getSheet().setColumnWidth(
				nameCol.getColumnIndex(), 35 * 120);
		colIndex++;

		HSSFCell natureCol = row1.createCell(colIndex);
		natureCol.setCellType(HSSFCell.CELL_TYPE_STRING);
		natureCol.setCellValue("预算金额");
		natureCol.setCellStyle(colStyle);
		natureCol.getSheet().setColumnWidth(natureCol.getColumnIndex(),
				35 * 120);
		colIndex++;

		HSSFCell symbolCol = row1.createCell(colIndex);
		symbolCol.setCellType(HSSFCell.CELL_TYPE_STRING);
		symbolCol.setCellValue("执行金额");
		symbolCol.setCellStyle(colStyle);
		symbolCol.getSheet().setColumnWidth(symbolCol.getColumnIndex(),
				35 * 120);
		colIndex++;

		HSSFCell moneyCol = row1.createCell(colIndex);
		moneyCol.setCellType(HSSFCell.CELL_TYPE_STRING);
		moneyCol.setCellValue("执行率");
		moneyCol.setCellStyle(colStyle);
		moneyCol.getSheet().setColumnWidth(moneyCol.getColumnIndex(),
				35 * 120);
		colIndex++;
		
		
		
		boolean privateRole = false;//角色不属于财务主任 和管理员
		String roleSql = "select role_info.role_info_id,user_info.department_info_id,ydi.the_value from user_info LEFT JOIN role_info on role_info.role_info_id=user_info.role_info_id LEFT JOIN ys_department_info ydi on "
				+ "user_info.department_info_id=ydi.the_id where user_info.user_info_id=" + sessionToken.getUserInfoId();
		
		List<Object[]> roleList = getEntityManager().createNativeQuery(roleSql).getResultList();
		int roleId = Integer.parseInt(roleList.get(0)[0].toString());//角色id
		
		if(Integer.valueOf(roleId) != 1 && Integer.valueOf(roleId) != 2){
			privateRole = true;
		}
		StringBuffer sql = new StringBuffer();
		sql.append(" select di.the_value,");
		sql.append(" SUM(nepi.budget_amount) as total_mount,");
		sql.append(" SUM(nepi.budget_amount - nepi.budget_amount_surplus) as surplus,");
		sql.append(" ROUND(SUM(nepi.budget_amount - nepi.budget_amount_surplus)/SUM(nepi.budget_amount) * 100,2)  as percent ");
		sql.append(" from normal_expend_plan_info nepi");
		sql.append(" left join generic_project gp on gp.the_id = nepi.generic_project_id");
		sql.append(" left join routine_project rp on rp.the_id = nepi.project_id");
		sql.append(" INNER JOIN ys_department_info di ON nepi.dept_id = di.the_id");
//		sql.append(" left join ys_department_info ydi on ydi.the_id = gp.department_info_id");
//		sql.append(" left join ys_department_info ydi1 on ydi1.the_id = rp.department_info_id");
		sql.append(" where 1 = 1");
//		String wc = commonDaoHome.getDepartmentInfoListByUserIdWhereCondition();
//		if (wc != null && !"".equals(wc)) {
//			sql.append(" and routine_project.department_info_id in (" + wc + ")");
//		}
		
		if(null != beginYearParam && !"".equals(beginYearParam)){
			sql.append(" and nepi.year = ").append(beginYearParam);
		}
		if(privateRole){
			sql.append(" and (gp.department_info_id = ").append(sessionToken.getDepartmentInfoId());
			sql.append(" or rp.department_info_id = ").append(sessionToken.getDepartmentInfoId()).append(") ");
		}
		if(null != departIds && !"".equals(departIds)){
			sql.append(" and (gp.department_info_id in (").append(departIds).append(")");
			sql.append(" or rp.department_info_id in (").append(departIds).append(")) ");
		}
		sql.append(" GROUP BY nepi.dept_id");
		sql.insert(0, "select recordset.the_value,FORMAT(recordset.total_mount,2),FORMAT(recordset.surplus,2),recordset.percent from (").append(") as recordset");
		List<Object[]> list = getEntityManager().createNativeQuery(sql.toString()).getResultList();
		
		
		for(int i = 0;i < list.size(); i++){
			Object[] obj =list.get(i);
			rowIndex ++;
			int col = 0;
			row1 = (HSSFRow) sheet.createRow(rowIndex);
			row1.setHeightInPoints(20f);
			
			nameCol = row1.createCell(col);
			nameCol.setCellType(HSSFCell.CELL_TYPE_STRING);
			nameCol.setCellValue(obj[0].toString());
			nameCol.setCellStyle(colStyle);
			nameCol.getSheet().setColumnWidth(
					nameCol.getColumnIndex(), 35 * 120);
			col++;
			
			natureCol = row1.createCell(col);
			natureCol.setCellType(HSSFCell.CELL_TYPE_STRING);
			natureCol.setCellValue(obj[1].toString().equals("1") ? "一般项目" : "常规项目");
			natureCol.setCellStyle(colStyle);
			natureCol.getSheet().setColumnWidth(
					natureCol.getColumnIndex(), 35 * 120);
			col++;
			
			symbolCol = row1.createCell(col);
			symbolCol.setCellType(HSSFCell.CELL_TYPE_STRING);
			symbolCol.setCellValue(obj[2].toString());
			symbolCol.setCellStyle(colStyle);
			symbolCol.getSheet().setColumnWidth(
					symbolCol.getColumnIndex(), 35 * 120);
			col++;
			
			moneyCol = row1.createCell(col);
			moneyCol.setCellType(HSSFCell.CELL_TYPE_STRING);
			moneyCol.setCellValue(obj[3].toString()+"%");
			moneyCol.setCellStyle(colStyle);
			moneyCol.getSheet().setColumnWidth(
					moneyCol.getColumnIndex(), 35 * 120);
			col++;
			
			
			
		}
		
		response.setContentType("application/vnd.ms-excel");
		try {
			response.setHeader("Content-Disposition", "attachment;filename="
					+ new String("执行查询.xls".getBytes(),
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

	
	public DeptExecuteStatisticsList() {
		setEjbql("");
		setAttribute("");
	}

	public String getBeginMonth() {
		return beginMonth;
	}

	public void setBeginMonth(String beginMonth) {
		this.beginMonth = beginMonth;
	}

	

	public String getEndMonth() {
		return endMonth;
	}

	public void setEndMonth(String endMonth) {
		this.endMonth = endMonth;
	}

	public String getIsFlag() {
		return isFlag;
	}

	public void setIsFlag(String isFlag) {
		this.isFlag = isFlag;
	}

	public List<Object[]> getBeginYear() {
		return beginYear;
	}

	public void setBeginYear(List<Object[]> beginYear) {
		this.beginYear = beginYear;
	}

	public List<Object[]> getFundsSourceList() {
		return fundsSourceList;
	}

	public void setFundsSourceList(List<Object[]> fundsSourceList) {
		this.fundsSourceList = fundsSourceList;
	}

	public List<Object[]> getDepartmentInfoList() {
		return departmentInfoList;
	}

	public void setDepartmentInfoList(List<Object[]> departmentInfoList) {
		this.departmentInfoList = departmentInfoList;
	}

	public String getDepartmentInfoId() {
		return departmentInfoId;
	}

	public void setDepartmentInfoId(String departmentInfoId) {
		this.departmentInfoId = departmentInfoId;
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

	public String getDepartIds() {
		return departIds;
	}

	public void setDepartIds(String departIds) {
		this.departIds = departIds;
	}

	
}
