package cn.dmdl.stl.hospitalbudget.budget.session;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.faces.context.FacesContext;
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
import org.jboss.seam.annotations.Name;

import cn.dmdl.stl.hospitalbudget.common.session.CriterionEntityHome;
import cn.dmdl.stl.hospitalbudget.hospital.entity.YsDepartmentInfo;
import cn.dmdl.stl.hospitalbudget.util.Assit;
import cn.dmdl.stl.hospitalbudget.util.DataSourceManager;
import cn.dmdl.stl.hospitalbudget.util.HospitalConstant;

@Name("ysBudgetCollectionDeptHome")
public class YsBudgetCollectionDeptHome extends CriterionEntityHome<Object> {
	private String year;
	private String deptIds;
	private JSONObject budgetCollectionInfo;
	private String budgetCollectionDeptIds;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


/*	public JSONObject pageInitCollectionInfo(){
		
		return filterBudgetCollectionInfo();
	}*/
	
	/**
	 * 按照年份和部门id筛选汇总信息
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public void filterBudgetCollectionInfo(){
		if (budgetCollectionInfo != null) {
			budgetCollectionInfo.clear();
		} else {
			budgetCollectionInfo = new JSONObject();
		}
		String[] depteIdArr = {};
		if(null != deptIds && !deptIds.isEmpty() && !"null".equals(deptIds)){
			depteIdArr = deptIds.split(",");
		}
		//收入预算
		//获取收入预算部门
		Map<Integer, YsDepartmentInfo> incomeDeptMap = getIncomDeptMap(); 
		StringBuilder incomeSql = new StringBuilder();
		incomeSql.append("select bcd.budget_collection_dept_id, ");
		incomeSql.append("bcd.`year`, ");
		incomeSql.append("di.`the_value` as dept_name, ");
		incomeSql.append("SUM(IF(ici.bottom_level = 1,ici.project_amount,0)) as total_amount, ");
		incomeSql.append("bcd.`status`, ");
		incomeSql.append("bcd.dept_id ");
		incomeSql.append("FROM hospital_budget.ys_budget_collection_dept bcd ");
		incomeSql.append("INNER JOIN hospital_budget.ys_income_collection_info ici on bcd.budget_collection_dept_id = ici.budget_collection_dept_id ");
		incomeSql.append("and ici.`year` = '").append(year).append("' and ici.delete=0 ");
		incomeSql.append("INNER JOIN hospital_budget.ys_department_info di ON bcd.dept_id = di.the_id ");
		incomeSql.append("where bcd.`year` = '").append(year).append("' ");
		incomeSql.append("AND bcd.budget_type = 1 ");//收入
		if(depteIdArr.length > 0){
			incomeSql.append("AND bcd.dept_id in (").append(deptIds).append(") ");
		}
		incomeSql.append("GROUP BY bcd.dept_id ");
		System.out.println(incomeSql);
		incomeSql.insert(0, "select * from (").append(") t ");
		//获得上一年下达的预算数据
		CommonDaoHome commonDaoHome = new CommonDaoHome();
		Map<String, Double> lastYearIncomeAmountMap = commonDaoHome.getLastYearTotalIncomeBudget(year);
		//按科室加载编制数据
		double totalIncome = 0;
		List<Object[]> incomeCollectionList = getEntityManager().createNativeQuery(incomeSql.toString()).getResultList();
		JSONArray incomeJsonArr = new JSONArray();
		for(Object[] incomeObjArr : incomeCollectionList){
			JSONObject incomeJson = new JSONObject();
			incomeJson.element("budget_collection_dept_id", incomeObjArr[0]);
			incomeJson.element("year", incomeObjArr[1]);
			incomeJson.element("dept_name", incomeObjArr[2]);
			incomeJson.element("total_amount", incomeObjArr[3]);
			incomeJson.element("with_last_year_num", "--");
			incomeJson.element("with_last_year_percent", "--");
			if(null != incomeObjArr[3] && !incomeObjArr[3].toString().isEmpty()){
				double thisYearTotalAmount = Double.parseDouble(incomeObjArr[3].toString());
				totalIncome += thisYearTotalAmount;
				if(lastYearIncomeAmountMap.containsKey(incomeObjArr[5].toString())){
					double lastYearTotalAmount = lastYearIncomeAmountMap.get(incomeObjArr[5].toString());
					incomeJson.element("with_last_year_num", thisYearTotalAmount - lastYearTotalAmount);
					incomeJson.element("with_last_year_percent", Assit.formatDouble2((thisYearTotalAmount - lastYearTotalAmount)/lastYearTotalAmount*100) + "%");
				}
			}
			incomeJson.element("status", incomeObjArr[4]);
			incomeJson.element("status_name", decodeStatus(incomeObjArr[4].toString()));
			if(incomeDeptMap.containsKey(incomeObjArr[5])){
				incomeDeptMap.remove(incomeObjArr[5]);
			}
			incomeJsonArr.add(incomeJson);
		}
		budgetCollectionInfo.element("income", incomeJsonArr);
		budgetCollectionInfo.element("total_income", totalIncome);
		//加载未提交编制的科室
		JSONArray incomeUnfinishedJsonArr = new JSONArray();
		for(Integer unFinishedDeptId : incomeDeptMap.keySet()){
			JSONObject incomeJson = new JSONObject();
			incomeJson.element("dept_id", incomeDeptMap.get(unFinishedDeptId).getTheId());
			incomeJson.element("dept_name", incomeDeptMap.get(unFinishedDeptId).getTheValue());
			incomeJson.element("year", year);
			incomeJson.element("status_name", "未提交");
			if(depteIdArr.length > 0){//有筛选部门
				if(Arrays.binarySearch(depteIdArr, incomeDeptMap.get(unFinishedDeptId).getTheId().toString()) > -1){//只添加筛选的部门
					incomeUnfinishedJsonArr.add(incomeJson);
				}
			}else{//未筛选部门
				incomeUnfinishedJsonArr.add(incomeJson);
			}
			
		}
		budgetCollectionInfo.element("income_unfinished", incomeUnfinishedJsonArr);
		
		//支出预算
		//获取支出预算部门
		Map<Integer, YsDepartmentInfo> expendDeptMap = getExpendDeptMap(); 
		StringBuilder expendSql = new StringBuilder();
		expendSql.append("select bcd.budget_collection_dept_id, ");
		expendSql.append("bcd.`year`, ");
		expendSql.append("di.`the_value` as dept_name, ");
		expendSql.append("SUM(IF(ici.bottom_level = 1,ici.project_amount,0)) as total_amount, ");
		expendSql.append("bcd.`status`, ");
		expendSql.append("bcd.dept_id ");
		expendSql.append("FROM hospital_budget.ys_budget_collection_dept bcd ");
		expendSql.append("INNER JOIN hospital_budget.ys_expend_collection_info ici on bcd.budget_collection_dept_id = ici.budget_collection_dept_id ");
		expendSql.append("and ici.`year` = '").append(year).append("' and ici.delete=0 ");
		expendSql.append("INNER JOIN hospital_budget.ys_department_info di ON bcd.dept_id = di.the_id ");
		expendSql.append("where bcd.`year` = '").append(year).append("' ");
		expendSql.append("AND bcd.budget_type = 2 ");//支出
		if(depteIdArr.length > 0){
			expendSql.append("AND bcd.dept_id in (").append(deptIds).append(") ");
		}
		expendSql.append("GROUP BY bcd.dept_id ");
		expendSql.insert(0, "select * from (").append(") t ");
		System.out.println(expendSql);
		//TODO 获得上一年下达的预算数据
		Map<String, Double> lastYearExpendAmountMap = commonDaoHome.getLastYearTotalExpendBudget(year);
		//按科室加载编制数据
		double totalExpend = 0;
		List<Object[]> expendCollectionList = getEntityManager().createNativeQuery(expendSql.toString()).getResultList();
		JSONArray expendJsonArr = new JSONArray();
		for(Object[] expendObjArr : expendCollectionList){
			JSONObject expendJson = new JSONObject();
			expendJson.element("budget_collection_dept_id", expendObjArr[0]);
			expendJson.element("year", expendObjArr[1]);
			expendJson.element("dept_name", expendObjArr[2]);
			expendJson.element("total_amount", Double.parseDouble(expendObjArr[3].toString())/10000);
			//默认无数据
			expendJson.element("with_last_year_num", "--");
			expendJson.element("with_last_year_percent", "--");
			if(null != expendObjArr[3] && !expendObjArr[3].toString().isEmpty()){
				double thisYearTotalAmount = Double.parseDouble(expendObjArr[3].toString());
				totalExpend += thisYearTotalAmount;
				if(lastYearExpendAmountMap.containsKey(expendObjArr[5].toString())){
					double lastYearTotalAmount = lastYearExpendAmountMap.get(expendObjArr[5].toString());
					expendJson.element("with_last_year_num", thisYearTotalAmount - lastYearTotalAmount);
					expendJson.element("with_last_year_percent", Assit.formatDouble2((thisYearTotalAmount - lastYearTotalAmount)/lastYearTotalAmount*100) + "%");
				}
			}
			expendJson.element("status", expendObjArr[4]);
			expendJson.element("status_name", decodeStatus(expendObjArr[4].toString()));
			if(expendDeptMap.containsKey(expendObjArr[5])){
				expendDeptMap.remove(expendObjArr[5]);
			}
			expendJsonArr.add(expendJson);
		}
		budgetCollectionInfo.element("expend", expendJsonArr);
		budgetCollectionInfo.element("total_expend", totalExpend/10000);
		//加载未提交编制的科室
		JSONArray expendUnfinishedJsonArr = new JSONArray();
		for(Integer unFinishedDeptId : expendDeptMap.keySet()){
			JSONObject expendJson = new JSONObject();
			expendJson.element("dept_id", expendDeptMap.get(unFinishedDeptId).getTheId());
			expendJson.element("dept_name", expendDeptMap.get(unFinishedDeptId).getTheValue());
			expendJson.element("year", year);
			expendJson.element("status_name", "未提交");
			if(depteIdArr.length > 0){//有筛选部门
				if(Arrays.binarySearch(depteIdArr, expendDeptMap.get(unFinishedDeptId).getTheId().toString()) > -1){//只添加筛选的部门
					expendUnfinishedJsonArr.add(expendJson);
				}
			}else{//未筛选部门
				expendUnfinishedJsonArr.add(expendJson);
			}
			
		}
		budgetCollectionInfo.element("expend_unfinished", expendUnfinishedJsonArr);
		System.out.println(budgetCollectionInfo);
	}


	/**
	 * 获取支出预算部门
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Map<Integer, YsDepartmentInfo> getExpendDeptMap() {
		Map<Integer, YsDepartmentInfo> incomDeptMap = new HashMap<Integer, YsDepartmentInfo>();
		List<YsDepartmentInfo> YsDepartmentInfoList = getEntityManager().createQuery("select ysDepartmentInfo from YsDepartmentInfo ysDepartmentInfo where ysDepartmentInfo.budgetAttribute is not null and ysDepartmentInfo.deleted=0 ").getResultList();
		for(YsDepartmentInfo ysDepartmentInfo : YsDepartmentInfoList){
			if(ysDepartmentInfo.getBudgetAttribute().indexOf("2") > -1){
				incomDeptMap.put(ysDepartmentInfo.getTheId(), ysDepartmentInfo);
			}
		}
		return incomDeptMap;
	}


	/**
	 * 获取收入预算部门
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Map<Integer, YsDepartmentInfo> getIncomDeptMap() {
		Map<Integer, YsDepartmentInfo> incomDeptMap = new HashMap<Integer, YsDepartmentInfo>();
		List<YsDepartmentInfo> YsDepartmentInfoList = getEntityManager().createQuery("select ysDepartmentInfo from YsDepartmentInfo ysDepartmentInfo where ysDepartmentInfo.budgetAttribute is not null and ysDepartmentInfo.deleted=0 ").getResultList();
		for(YsDepartmentInfo ysDepartmentInfo : YsDepartmentInfoList){
			if(ysDepartmentInfo.getBudgetAttribute().indexOf("1") > -1){
				incomDeptMap.put(ysDepartmentInfo.getTheId(), ysDepartmentInfo);
			}
		}
		return incomDeptMap;
	}

	
	/**
	 * @return
	 */
	public JSONObject submitCollection(){
		JSONObject result = new JSONObject();
		Connection connection = DataSourceManager.open(DataSourceManager.BY_JDBC_DEFAULT);
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			connection.setAutoCommit(false);
			//收入预算下达
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT bcd.dept_id, ");
			sql.append("bcd.`year`, ");
			sql.append("ici.generic_project_id, ");
			sql.append("ici.routine_project_id, ");
			sql.append("ici.project_amount, ");
			sql.append("ici.bottom_level ");
			sql.append("FROM ys_budget_collection_dept bcd ");
			sql.append("INNER JOIN ys_income_collection_info ici ON bcd.budget_collection_dept_id = ici.budget_collection_dept_id AND ici.delete = 0 ");
			sql.append("WHERE bcd.status = 0 AND bcd.budget_type = 1 AND ici.bottom_level = 1 ");
			sql.append("AND bcd.budget_collection_dept_id in (").append(budgetCollectionDeptIds).append(")");
			preparedStatement = connection.prepareStatement(sql.toString());
			resultSet = preparedStatement.executeQuery();
			while(resultSet.next()){
				preparedStatement = connection.prepareStatement("INSERT INTO `ys_income_plan_info` (`dept_id`, `year`, `routine_project_id`, `generic_project_id`, `budget_amount`, `insert_user`, `insert_time`) VALUES "
						+ "(?, ?, ?, ?, ?, ?, NOW())");
				preparedStatement.setInt(1, resultSet.getInt("dept_id"));
				preparedStatement.setString(2, resultSet.getString("year"));
				preparedStatement.setString(3, resultSet.getString("routine_project_id"));
				preparedStatement.setString(4, resultSet.getString("generic_project_id"));
				preparedStatement.setDouble(5, resultSet.getDouble("project_amount"));
				preparedStatement.setInt(6, sessionToken.getUserInfoId());
				preparedStatement.executeUpdate();
			}
			
			//支出预算下达
			StringBuilder sql2 = new StringBuilder();
			sql2.append("SELECT bcd.dept_id, ");
			sql2.append("bcd.`year`, ");
			sql2.append("ici.generic_project_id, ");
			sql2.append("ici.routine_project_id, ");
			sql2.append("ici.project_amount, ");
			sql2.append("ici.bottom_level ");
			sql2.append("FROM ys_budget_collection_dept bcd ");
			sql2.append("INNER JOIN ys_expend_collection_info ici ON bcd.budget_collection_dept_id = ici.budget_collection_dept_id AND ici.delete = 0 ");
			sql2.append("WHERE bcd.status = 0 AND bcd.budget_type = 2 AND ici.bottom_level = 1 ");
			sql2.append("AND bcd.budget_collection_dept_id in (").append(budgetCollectionDeptIds).append(")");
			preparedStatement = connection.prepareStatement(sql2.toString());
			resultSet = preparedStatement.executeQuery();
			while(resultSet.next()){
				preparedStatement = connection.prepareStatement("INSERT INTO `normal_expend_plan_info` (`dept_id`, `year`, `project_id`, `generic_project_id`, `budget_amount`, `budget_amount_surplus`,  `insert_user`, `insert_time`) VALUES "
						+ "(?, ?, ?, ?, ?, ?, ?, NOW())");
				preparedStatement.setInt(1, resultSet.getInt("dept_id"));
				preparedStatement.setString(2, resultSet.getString("year"));
				preparedStatement.setString(3, resultSet.getString("routine_project_id"));
				preparedStatement.setString(4, resultSet.getString("generic_project_id"));
				preparedStatement.setDouble(5, resultSet.getDouble("project_amount"));
				preparedStatement.setDouble(6, resultSet.getDouble("project_amount"));
				preparedStatement.setInt(7, sessionToken.getUserInfoId());
				preparedStatement.executeUpdate();
			}
			preparedStatement = connection.prepareStatement("update ys_budget_collection_dept set status = 1 where budget_collection_dept_id in (" + budgetCollectionDeptIds + ")");
			preparedStatement.executeUpdate();
			result.element("invoke_result", "ok");
			connection.commit();
			connection.setAutoCommit(true);
		} catch (Exception e) {
			result.element("invoke_result", "fail");
			result.element("invoke_message", "下达失败！");
			try {
				connection.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			DataSourceManager.close(connection, preparedStatement, resultSet);
		}
		return result;
	}
	

	/**
	 * 获取下达属性描述
	 * @param object
	 * @return
	 */
	private String decodeStatus(String status) {
		String statusName = null;
		switch(Integer.parseInt(status)) { 
			case HospitalConstant.COLLECTIONSTATUS_WAIT :
				statusName = "待处理"; 
				break; 
			case HospitalConstant.COLLECTIONSTATUS_FINISH : 
				statusName = "已下达"; 
				break;  
			case HospitalConstant.COLLECTIONSTATUS_TAKEBACK : 
				statusName = "被追回"; 
				break; 
			default: 
				statusName = "无";
				break; 
		} 
		return statusName;
		
	}
	
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
		String headStr = "汇总表";
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
		cellTitle.setCellValue("预算汇总");
		cellTitle.setCellStyle(colTitleStyle);
		range = new CellRangeAddress(0, 0, 0, 3);
		sheet.addMergedRegion(range);
		excelAddBorder(1, range, sheet, workbook);
		
		rowIndex ++;
		HSSFRow row1 = (HSSFRow) sheet.createRow(rowIndex);
		row1.setHeightInPoints(20f);
		HSSFCell deptCol = row1.createCell(colIndex);
		deptCol.setCellType(HSSFCell.CELL_TYPE_STRING);
		deptCol.setCellValue("科室");
		deptCol.setCellStyle(colStyle);
		deptCol.getSheet().setColumnWidth(
				deptCol.getColumnIndex(), 35 * 160);
		colIndex++;

		HSSFCell moneyCol = row1.createCell(colIndex);
		moneyCol.setCellType(HSSFCell.CELL_TYPE_STRING);
		moneyCol.setCellValue("预算金额");
		moneyCol.setCellStyle(colStyle);
		moneyCol.getSheet().setColumnWidth(moneyCol.getColumnIndex(),
				35 * 160);
		colIndex++;

		HSSFCell adjustMoney = row1.createCell(colIndex);
		adjustMoney.setCellType(HSSFCell.CELL_TYPE_STRING);
		adjustMoney.setCellValue("调整金额");
		adjustMoney.setCellStyle(colStyle);
		adjustMoney.getSheet().setColumnWidth(adjustMoney.getColumnIndex(),
				35 * 160);
		colIndex++;

		HSSFCell adjustPercet = row1.createCell(colIndex);
		adjustPercet.setCellType(HSSFCell.CELL_TYPE_STRING);
		adjustPercet.setCellValue("调整比例");
		adjustPercet.setCellStyle(colStyle);
		adjustPercet.getSheet().setColumnWidth(adjustPercet.getColumnIndex(),
				35 * 90);
		colIndex++;
		filterBudgetCollectionInfo();
		JSONArray incomeArray = budgetCollectionInfo.getJSONArray("expend");
		for(int i = 0;i < incomeArray.size(); i++){
			JSONObject json = incomeArray.getJSONObject(i);
			rowIndex ++;
			int col = 0;
			row1 = (HSSFRow) sheet.createRow(rowIndex);
			row1.setHeightInPoints(20f);
			deptCol = row1.createCell(col);
			deptCol.setCellType(HSSFCell.CELL_TYPE_STRING);
			deptCol.setCellValue(json.get("dept_name").toString());
			deptCol.setCellStyle(colStyle);
			deptCol.getSheet().setColumnWidth(
					deptCol.getColumnIndex(), 35 * 160);
			col++;
			
			moneyCol = row1.createCell(col);
			moneyCol.setCellType(HSSFCell.CELL_TYPE_STRING);
			moneyCol.setCellValue(json.get("total_amount").toString());
			moneyCol.setCellStyle(colStyle);
			moneyCol.getSheet().setColumnWidth(
					moneyCol.getColumnIndex(), 35 * 160);
			col++;
			
			adjustMoney = row1.createCell(col);
			adjustMoney.setCellType(HSSFCell.CELL_TYPE_STRING);
			adjustMoney.setCellValue(json.get("with_last_year_num").toString());
			adjustMoney.setCellStyle(colStyle);
			adjustMoney.getSheet().setColumnWidth(
					adjustMoney.getColumnIndex(), 35 * 160);
			col++;
			
			adjustPercet = row1.createCell(col);
			adjustPercet.setCellType(HSSFCell.CELL_TYPE_STRING);
			adjustPercet.setCellValue(json.get("with_last_year_percent").toString());
			adjustPercet.setCellStyle(colStyle);
			adjustPercet.getSheet().setColumnWidth(
					adjustPercet.getColumnIndex(), 35 * 90);
			col++;
			
		}
		rowIndex ++;
		int col = 0;
		row1 = (HSSFRow) sheet.createRow(rowIndex);
		row1.setHeightInPoints(20f);
		deptCol = row1.createCell(col);
		deptCol.setCellType(HSSFCell.CELL_TYPE_STRING);
		deptCol.setCellValue("总计：");
		deptCol.setCellStyle(colStyle);
		deptCol.getSheet().setColumnWidth(
				deptCol.getColumnIndex(), 35 * 160);
		col++;
		
		moneyCol = row1.createCell(col);
		moneyCol.setCellType(HSSFCell.CELL_TYPE_STRING);
		moneyCol.setCellValue(budgetCollectionInfo.get("total_expend").toString());
		moneyCol.setCellStyle(colStyle);
		moneyCol.getSheet().setColumnWidth(
				moneyCol.getColumnIndex(), 35 * 160);
		col++;
		
		adjustMoney = row1.createCell(col);
		adjustMoney.setCellType(HSSFCell.CELL_TYPE_STRING);
		adjustMoney.setCellValue("");
		adjustMoney.setCellStyle(colStyle);
		adjustMoney.getSheet().setColumnWidth(
				adjustMoney.getColumnIndex(), 35 * 160);
		col++;
		
		adjustPercet = row1.createCell(col);
		adjustPercet.setCellType(HSSFCell.CELL_TYPE_STRING);
		adjustPercet.setCellValue("");
		adjustPercet.setCellStyle(colStyle);
		adjustPercet.getSheet().setColumnWidth(
				adjustPercet.getColumnIndex(), 35 * 90);
		col++;
		
		//各个科室
		// 标题字体
		titleFont = (HSSFFont) workbook.createFont();
		titleFont.setFontName("宋体");
		titleFont.setFontHeightInPoints((short) 18);// 字体大小
		titleFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);// 加粗
		colTitleStyle.setFont(titleFont);
		
		// 普通单元格样式
		colStyle = (HSSFCellStyle) workbook.createCellStyle();
		colFont = (HSSFFont) workbook.createFont();
		colFont.setFontName("宋体");
		colFont.setFontHeightInPoints((short) 10);// 字体大小
		colFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);// 加粗
		colStyle.setFont(colFont);
		
		// 普通单元格样式
		HSSFCellStyle colStyle1 = (HSSFCellStyle) workbook.createCellStyle();
		HSSFFont colFont1 = (HSSFFont) workbook.createFont();
		colFont1.setFontName("宋体");
		colFont1.setFontHeightInPoints((short) 10);// 字体大小
		colStyle1.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		colStyle1.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		colStyle1.setFont(colFont1);
		colStyle1.setBorderBottom((short) 1);
		colStyle1.setBorderLeft((short) 1);
		colStyle1.setBorderRight((short) 1);
		colStyle1.setBorderTop((short) 1);
		colStyle1.setFillForegroundColor(HSSFColor.YELLOW.index);
		
		Map<Integer, List<Object[]>> map =  getExportList();
		Iterator<Entry<Integer, List<Object[]>>> it = map.entrySet().iterator();
		
		while(it.hasNext()){
			Entry<Integer, List<Object[]>> entry = it.next();
			List<Object[]> projectList = entry.getValue();
			count ++;
			String dept = projectList.get(0)[1].toString();
			sheet = workbook.createSheet();
			workbook.setSheetName(count - 1, dept);
			rowIndex = 0;
			colIndex = 0;

			// 第一行
			rowTitle = (HSSFRow) sheet.createRow(rowIndex);
			rowTitle.setHeightInPoints(30f);
			cellTitle = rowTitle.createCell(colIndex);
			cellTitle.setCellType(HSSFCell.CELL_TYPE_STRING);
			cellTitle.setCellValue(dept);
			cellTitle.setCellStyle(colTitleStyle);
			range = new CellRangeAddress(0, 0, 0, 8);
			sheet.addMergedRegion(range);
			excelAddBorder(1, range, sheet, workbook);
			
			rowIndex ++;
			row1 = (HSSFRow) sheet.createRow(rowIndex);
			row1.setHeightInPoints(30f);
			HSSFCell numCol = row1.createCell(colIndex);
			numCol.setCellType(HSSFCell.CELL_TYPE_STRING);
			numCol.setCellValue("序号");
			numCol.setCellStyle(colStyle);
			numCol.getSheet().setColumnWidth(
					numCol.getColumnIndex(), 35 * 30);
			colIndex++;
			
			HSSFCell projectName = row1.createCell(colIndex);
			projectName.setCellType(HSSFCell.CELL_TYPE_STRING);
			projectName.setCellValue("项目名称");
			projectName.setCellStyle(colStyle);
			projectName.getSheet().setColumnWidth(
					projectName.getColumnIndex(), 35 * 100);
			colIndex++;

			HSSFCell projectType = row1.createCell(colIndex);
			projectType.setCellType(HSSFCell.CELL_TYPE_STRING);
			projectType.setCellValue("项目性质");
			projectType.setCellStyle(colStyle);
			projectType.getSheet().setColumnWidth(projectType.getColumnIndex(),
					35 * 100);
			colIndex++;

			HSSFCell sourceCol = row1.createCell(colIndex);
			sourceCol.setCellType(HSSFCell.CELL_TYPE_STRING);
			sourceCol.setCellValue("项目来源");
			sourceCol.setCellStyle(colStyle);
			sourceCol.getSheet().setColumnWidth(sourceCol.getColumnIndex(),
					35 * 100);
			colIndex++;

			HSSFCell projectMoney = row1.createCell(colIndex);
			projectMoney.setCellType(HSSFCell.CELL_TYPE_STRING);
			projectMoney.setCellValue("项目金额");
			projectMoney.setCellStyle(colStyle);
			projectMoney.getSheet().setColumnWidth(projectMoney.getColumnIndex(),
					35 * 100);
			colIndex++;
			
			HSSFCell calculBasis = row1.createCell(colIndex);
			calculBasis.setCellType(HSSFCell.CELL_TYPE_STRING);
			calculBasis.setCellValue("计算依据");
			calculBasis.setCellStyle(colStyle);
			calculBasis.getSheet().setColumnWidth(calculBasis.getColumnIndex(),
					35 * 100);
			colIndex++;
			
			HSSFCell addMoney = row1.createCell(colIndex);
			addMoney.setCellType(HSSFCell.CELL_TYPE_STRING);
			addMoney.setCellValue("与" + (Integer.parseInt(year) - 1) + "年预算同比增减（万元）");
			addMoney.setCellStyle(colStyle);
			addMoney.getSheet().setColumnWidth(addMoney.getColumnIndex(),
					35 * 100);
			colIndex++;
			
			HSSFCell addPercent = row1.createCell(colIndex);
			addPercent.setCellType(HSSFCell.CELL_TYPE_STRING);
			addPercent.setCellValue("同比增长");
			addPercent.setCellStyle(colStyle);
			addPercent.getSheet().setColumnWidth(addPercent.getColumnIndex(),
					35 * 100);
			colIndex++;
			
			HSSFCell comment = row1.createCell(colIndex);
			comment.setCellType(HSSFCell.CELL_TYPE_STRING);
			comment.setCellValue("备注");
			comment.setCellStyle(colStyle);
			comment.getSheet().setColumnWidth(comment.getColumnIndex(),
					35 * 100);
			colIndex++;
			
			for(int i = 0;i < projectList.size(); i++){
				Object[] project = projectList.get(i);
				rowIndex ++;
				 col = 0;
				row1 = (HSSFRow) sheet.createRow(rowIndex);
				row1.setHeightInPoints(20f);
				
				numCol = row1.createCell(col);
				numCol.setCellType(HSSFCell.CELL_TYPE_STRING);
				numCol.setCellValue(i + 1);
				numCol.setCellStyle(colStyle1);
				numCol.getSheet().setColumnWidth(
						numCol.getColumnIndex(), 35 * 30);
				col++;
				
				projectName = row1.createCell(col);
				projectName.setCellType(HSSFCell.CELL_TYPE_STRING);
				projectName.setCellValue(project[0].toString());
				projectName.setCellStyle(colStyle1);
				projectName.getSheet().setColumnWidth(
						projectName.getColumnIndex(), 35 * 100);
				col++;
				
				projectType = row1.createCell(col);
				projectType.setCellType(HSSFCell.CELL_TYPE_STRING);
				projectType.setCellValue("");
				projectType.setCellStyle(colStyle1);
				projectType.getSheet().setColumnWidth(
						projectType.getColumnIndex(), 35 * 100);
				col++;
				
				sourceCol = row1.createCell(col);
				sourceCol.setCellType(HSSFCell.CELL_TYPE_STRING);
				sourceCol.setCellValue(project[2].toString());
				sourceCol.setCellStyle(colStyle1);
				sourceCol.getSheet().setColumnWidth(
						sourceCol.getColumnIndex(), 35 * 100);
				col++;
				
				
				projectMoney = row1.createCell(col);
				projectMoney.setCellType(HSSFCell.CELL_TYPE_STRING);
				projectMoney.setCellValue(project[3].toString());
				projectMoney.setCellStyle(colStyle1);
				projectMoney.getSheet().setColumnWidth(
						projectMoney.getColumnIndex(), 35 * 100);
				col++;
				
				calculBasis = row1.createCell(col);
				calculBasis.setCellType(HSSFCell.CELL_TYPE_STRING);
				calculBasis.setCellValue(project[4].toString());
				calculBasis.setCellStyle(colStyle1);
				calculBasis.getSheet().setColumnWidth(
						calculBasis.getColumnIndex(), 35 * 100);
				col++;
				
				addMoney = row1.createCell(col);
				addMoney.setCellType(HSSFCell.CELL_TYPE_STRING);
				addMoney.setCellValue(project[5] == null ? "" : project[5].toString());
				addMoney.setCellStyle(colStyle1);
				addMoney.getSheet().setColumnWidth(
						addMoney.getColumnIndex(), 35 * 100);
				col++;
				
				addPercent = row1.createCell(col);
				addPercent.setCellType(HSSFCell.CELL_TYPE_STRING);
				addPercent.setCellValue(project[6] == null ? "" : project[6].toString());
				addPercent.setCellStyle(colStyle1);
				addPercent.getSheet().setColumnWidth(
						addPercent.getColumnIndex(), 35 * 100);
				col++;
				
				comment = row1.createCell(col);
				comment.setCellType(HSSFCell.CELL_TYPE_STRING);
				comment.setCellValue(project[7] == null ? "" : project[7].toString());
				comment.setCellStyle(colStyle1);
				comment.getSheet().setColumnWidth(
						comment.getColumnIndex(), 35 * 100);
				col++;
				
			}
			rowIndex ++;
			col = 0;
			row1 = (HSSFRow) sheet.createRow(rowIndex);
			row1.setHeightInPoints(20f);
			deptCol = row1.createCell(col);
			deptCol.setCellType(HSSFCell.CELL_TYPE_STRING);
			deptCol.setCellValue("总计：");
			deptCol.setCellStyle(colStyle1);
			range = new CellRangeAddress(rowIndex, rowIndex, 0, 2);
			sheet.addMergedRegion(range);
			excelAddBorder(1, range, sheet, workbook);
			col += 3;
			
			moneyCol = row1.createCell(col);
			moneyCol.setCellType(HSSFCell.CELL_TYPE_STRING);
			moneyCol.setCellValue("");
			moneyCol.setCellStyle(colStyle1);
			col++;
			
			moneyCol = row1.createCell(col);
			moneyCol.setCellType(HSSFCell.CELL_TYPE_STRING);
			moneyCol.setCellValue(budgetCollectionInfo.get("total_expend").toString());
			moneyCol.setCellStyle(colStyle1);
			col++;
			
			adjustMoney = row1.createCell(col);
			adjustMoney.setCellType(HSSFCell.CELL_TYPE_STRING);
			adjustMoney.setCellValue("");
			adjustMoney.setCellStyle(colStyle1);
			col++;
			
			calculBasis = row1.createCell(col);
			calculBasis.setCellType(HSSFCell.CELL_TYPE_STRING);
			calculBasis.setCellValue("");
			calculBasis.setCellStyle(colStyle1);
			col++;
			
			calculBasis = row1.createCell(col);
			calculBasis.setCellType(HSSFCell.CELL_TYPE_STRING);
			calculBasis.setCellValue("");
			calculBasis.setCellStyle(colStyle1);
			col++;
			
			calculBasis = row1.createCell(col);
			calculBasis.setCellType(HSSFCell.CELL_TYPE_STRING);
			calculBasis.setCellValue("");
			calculBasis.setCellStyle(colStyle1);
			col++;
		}
		response.setContentType("application/vnd.ms-excel");
		try {
			response.setHeader("Content-Disposition", "attachment;filename="
					+ new String("预算调整.xls".getBytes(),
							"iso-8859-1"));
			workbook.write(response.getOutputStream());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	public Map<Integer, List<Object[]>> getExportList(){
		 DecimalFormat df = new DecimalFormat("#.00");
		String[] depteIdArr = {};
		if(null != deptIds && !deptIds.isEmpty() && !"null".equals(deptIds)){
			depteIdArr = deptIds.split(",");
		}
		//支出预算
		//获取支出预算部门
		Map<Integer, List<Object[]>> deptMap = new HashMap<Integer,List<Object[]>>(); 
		StringBuilder expendSql = new StringBuilder();
		expendSql.append("select bcd.budget_collection_dept_id, ");
		expendSql.append("bcd.`year`, ");
		expendSql.append("di.`the_value` as dept_name, ");
		expendSql.append("IF(ici.bottom_level = 1,ici.project_amount,0) as total_amount, ");
		expendSql.append("bcd.`status`, ");
		expendSql.append("bcd.dept_id, ");
		expendSql.append("ici.project_source,");
		expendSql.append("ici.formula_remark,");
		expendSql.append("ici.generic_project_id,");
		expendSql.append("gp.the_value as generic_name,");
		expendSql.append("ici.routine_project_id,");
		expendSql.append("rp.the_value as routine_name ");
		expendSql.append("FROM hospital_budget.ys_budget_collection_dept bcd ");
		expendSql.append("INNER JOIN hospital_budget.ys_expend_collection_info ici on bcd.budget_collection_dept_id = ici.budget_collection_dept_id ");
		expendSql.append("and ici.`year` = '").append(year).append("' and ici.delete=0 ");
		expendSql.append("INNER JOIN hospital_budget.ys_department_info di ON bcd.dept_id = di.the_id ");
		expendSql.append("LEFT JOIN routine_project rp on rp.the_id=ici.routine_project_id ");
		expendSql.append("LEFT JOIN generic_project gp on gp.the_id=ici.generic_project_id ");
		expendSql.append("where bcd.`year` = '").append(year).append("' ");
		expendSql.append("AND bcd.budget_type = 2 ");//支出
		if(depteIdArr.length > 0){
			expendSql.append("AND bcd.dept_id in (").append(deptIds).append(") ");
		}
		expendSql.insert(0, "select * from (").append(") t ");
		List<Object[]> expendCollectionList = getEntityManager().createNativeQuery(expendSql.toString()).getResultList();
	
		StringBuilder lastSql = new StringBuilder();
		lastSql.append("select bcd.budget_collection_dept_id, ");//0
		lastSql.append("bcd.`year`, ");//1
		lastSql.append("di.`the_value` as dept_name, ");//2
		lastSql.append("IF(ici.bottom_level = 1,ici.project_amount,0) as total_amount, ");//3
		lastSql.append("bcd.`status`, ");//4
		lastSql.append("bcd.dept_id, ");//5
		lastSql.append("ici.project_source,");//6
		lastSql.append("ici.formula_remark,");//7
		lastSql.append("ici.generic_project_id,");//8
		lastSql.append("gp.the_value as generic_name,");//9
		lastSql.append("ici.routine_project_id,");//10
		lastSql.append("rp.the_value as routine_name ");//11
		lastSql.append("FROM hospital_budget.ys_budget_collection_dept bcd ");
		lastSql.append("INNER JOIN hospital_budget.ys_expend_collection_info ici on bcd.budget_collection_dept_id = ici.budget_collection_dept_id ");
		lastSql.append("and ici.`year` = '").append(Integer.parseInt(year) - 1).append("' and ici.delete=0 ");
		lastSql.append("INNER JOIN hospital_budget.ys_department_info di ON bcd.dept_id = di.the_id ");
		lastSql.append("LEFT JOIN routine_project rp on rp.the_id=ici.routine_project_id ");
		lastSql.append("LEFT JOIN generic_project gp on gp.the_id=ici.generic_project_id ");
		lastSql.append("where bcd.`year` = '").append(Integer.parseInt(year) - 1).append("' ");
		lastSql.append("AND bcd.budget_type = 2 ");//支出
		if(depteIdArr.length > 0){
			lastSql.append("AND bcd.dept_id in (").append(deptIds).append(") ");
		}
		lastSql.insert(0, "select * from (").append(") t ");
		List<Object[]> lastList = getEntityManager().createNativeQuery(lastSql.toString()).getResultList();
		
		List<Object[]> list = new ArrayList<Object[]>(); 
		for(Object[] theObj : expendCollectionList){
			Object[] obj = new Object[16];
			obj[0] = theObj[0];
			obj[1] = theObj[1];
			obj[2] = theObj[2];
			obj[3] = theObj[3];
			obj[4] = theObj[4];
			obj[5] = theObj[5];
			obj[6] = theObj[6];
			obj[7] = theObj[7];
			obj[8] = theObj[8];
			obj[9] = theObj[9];
			obj[10] = theObj[10];
			obj[11] = theObj[11];
			for(Object[] lastObj : lastList){
				if(((null != theObj[8] && null != lastObj[8] && Integer.valueOf(theObj[8].toString()).intValue() == Integer.valueOf(lastObj[8].toString())) 
						|| (null != theObj[9] && null != lastObj[9] && Integer.valueOf(theObj[9].toString()).intValue() == Integer.valueOf(lastObj[9].toString()))) 
						&& Integer.valueOf(theObj[5].toString()).intValue() == Integer.valueOf(lastObj[5].toString()).intValue()){
					obj[12] = lastObj[3];
					obj[13] = Float.parseFloat(obj[12].toString()) - Float.parseFloat(obj[3].toString());
					obj[14] = df.format(Float.parseFloat(obj[13].toString()) / Float.parseFloat(obj[3].toString()));
				}
			}
			list.add(obj);
		}
		for(Object[] obj : list){
			Integer deptId = Integer.parseInt(obj[5].toString());
			List<Object[]> projectList;
			if(deptMap.get(deptId) == null){
				projectList = new ArrayList<Object[]>();
			}else{
				projectList = deptMap.get(deptId);
			}
			Object[] project = new Object[10];
			if(null == obj[8]){
				project[0] = obj[11];
				project[8] = 1;
				project[9] = obj[8];
			}else{
				project[0] = obj[9];
				project[8] = 2;
				project[9] = obj[8];
			}
			project[1] = obj[2];
			try {
				project[2] = URLDecoder.decode(obj[6].toString(), "utf-8");
				project[4] =  URLDecoder.decode(obj[7].toString(), "utf-8");
			} catch (UnsupportedEncodingException e) {
				
			}
			project[3] = obj[3];
			
			project[5] = obj[13];
			project[6] = obj[14];
			projectList.add(project);
			deptMap.put(deptId, projectList);
		}
		return deptMap;
	}
	
	/** * 添加excel合并后边框属性 */
	public void excelAddBorder(int arg0, CellRangeAddress range, Sheet sheet,
			Workbook workbook) {
		RegionUtil.setBorderBottom(1, range, sheet, workbook);
		RegionUtil.setBorderLeft(1, range, sheet, workbook);
		RegionUtil.setBorderRight(1, range, sheet, workbook);
		RegionUtil.setBorderTop(1, range, sheet, workbook);
	}

	public String getYear() {
		return year;
	}


	public void setYear(String year) {
		this.year = year;
	}


	public String getDeptIds() {
		return deptIds;
	}


	public void setDeptIds(String deptIds) {
		this.deptIds = deptIds;
	}


	public JSONObject getBudgetCollectionInfo() {
		return budgetCollectionInfo;
	}


	public void setBudgetCollectionInfo(JSONObject budgetCollectionInfo) {
		this.budgetCollectionInfo = budgetCollectionInfo;
	}


	public String getBudgetCollectionDeptIds() {
		return budgetCollectionDeptIds;
	}


	public void setBudgetCollectionDeptIds(String budgetCollectionDeptIds) {
		this.budgetCollectionDeptIds = budgetCollectionDeptIds;
	}

}
