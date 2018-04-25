package cn.dmdl.stl.hospitalbudget.budget.session;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import cn.dmdl.stl.hospitalbudget.util.DataSourceManager;
import cn.dmdl.stl.hospitalbudget.util.HospitalConstant;

@Name("disburseStatisticalList")
public class DisburseStatisticalList extends CriterionNativeQuery<Object[]> {

	private static final long serialVersionUID = 1L;
	private List<Object[]> beginYear;// 预算年份select
	private List<Object[]> fundsSourceList;// 资金来源list
	private List<Object[]> departmentInfoList;// 科室select
	private String beginYearParam;
	private String beginMonth;
	private String beginDay;
	private String isFlag;
	private String departmentInfoId;
	private String fundsSourceId;
	private String departmentIds;
	private String projectName;

	@In(create = true)
	CommonDaoHome commonDaoHome;

	@Override
	protected Query createQuery() {
		boolean privateRole = false;//角色不属于财务 和主任（领导的）
		String roleSql = "select role_info.role_info_id,user_info.department_info_id,ydi.the_value from user_info LEFT JOIN role_info on role_info.role_info_id=user_info.role_info_id LEFT JOIN ys_department_info ydi on "
				+ "user_info.department_info_id=ydi.the_id where user_info.user_info_id=" + sessionToken.getUserInfoId();
		
		List<Object[]> roleList = getEntityManager().createNativeQuery(roleSql).getResultList();
		int roleId = Integer.parseInt(roleList.get(0)[0].toString());//角色id
		
		if(Integer.valueOf(roleId) != 1 && Integer.valueOf(roleId) != 2 ){
			privateRole = true;
		}
		
		StringBuffer sql = new StringBuffer();
		sql.append(" select nepi.normal_expend_plan_id,if(nepi.project_id is null,gp.the_value,rp.the_value) as the_value,");
		sql.append(" if(nepi.project_id is null,nepi.generic_project_id,nepi.project_id)as the_id,if(nepi.project_id is null,2,1) as typet,");
		sql.append(" FORMAT(nepi.budget_amount,2) as budget_amount,");
		sql.append(" FORMAT(if(nepi.project_id IS NULL,sum(eap1.expend_money),sum(eap.expend_money)),2) AS budget_amount_frozen,");
		sql.append(" FORMAT(nepi.budget_amount - if(nepi.project_id IS NULL,sum(eap1.expend_money),sum(eap.expend_money)),2) as budget_amount_surplus,");
		sql.append(" round(if(nepi.project_id IS NULL,sum(eap1.expend_money),sum(eap.expend_money)) / nepi.budget_amount * 100,2) as roundnum ");
		sql.append(" from normal_expend_plan_info nepi");
		sql.append(" left join generic_project gp on gp.the_id = nepi.generic_project_id");
		sql.append(" left join routine_project rp on rp.the_id = nepi.project_id");
		sql.append(" LEFT JOIN expend_apply_project eap on eap.project_id=nepi.project_id ");
		sql.append(" LEFT JOIN expend_apply_project eap1 on eap1.generic_project_id=nepi.generic_project_id ");
		sql.append(" left join expend_apply_info eai on eap.expend_apply_info_id=eai.expend_apply_info_id ");
		sql.append(" LEFT JOIN expend_apply_info eai1 on eap1.expend_apply_info_id=eai1.expend_apply_info_id ");
		sql.append(" where 1 = 1 and (eap.deleted=0 or eap1.deleted=0)	");
		sql.append(" and (eai.deleted=0 or eai1.deleted=0) ");
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
		if(null != departmentInfoId && !"".equals(departmentInfoId)){
			sql.append(" and (gp.department_info_id = ").append(departmentInfoId);
			sql.append(" or rp.department_info_id = ").append(departmentInfoId).append(") ");
		}
		if(null != departmentIds && !"".equals(departmentIds)){
			sql.append(" and (gp.department_info_id in (").append(departmentIds).append(") ");
			sql.append(" or rp.department_info_id in (").append(departmentIds).append(")) ");
		}
		if(null != projectName && !"".equals(projectName)){
			sql.append(" and (gp.the_value like '%").append(projectName).append("%' ");
			sql.append(" or rp.the_value like '%").append(projectName).append("%') ");
		}
		sql.append(" group by nepi.normal_expend_plan_id ");
		//sql.insert(0, "select * from (").append(") as recordset");
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
		
		HSSFCell addMoneyCol = row1.createCell(colIndex);
		addMoneyCol.setCellType(HSSFCell.CELL_TYPE_STRING);
		addMoneyCol.setCellValue("可支出金额");
		addMoneyCol.setCellStyle(colStyle);
		addMoneyCol.getSheet().setColumnWidth(addMoneyCol.getColumnIndex(),
				35 * 200);
		colIndex++;
		
		HSSFCell addPercentCol = row1.createCell(colIndex);
		addPercentCol.setCellType(HSSFCell.CELL_TYPE_STRING);
		addPercentCol.setCellValue("执行率");
		addPercentCol.setCellStyle(colStyle);
		addPercentCol.getSheet().setColumnWidth(addPercentCol.getColumnIndex(),
				35 * 200);
		colIndex++;
		
		
		
		
		boolean privateRole = false;//角色不属于财务 和主任（领导的）
		String roleSql = "select role_info.role_info_id,user_info.department_info_id,ydi.the_value from user_info LEFT JOIN role_info on role_info.role_info_id=user_info.role_info_id LEFT JOIN ys_department_info ydi on "
				+ "user_info.department_info_id=ydi.the_id where user_info.user_info_id=" + sessionToken.getUserInfoId();
		
		List<Object[]> roleList = getEntityManager().createNativeQuery(roleSql).getResultList();
		int roleId = Integer.parseInt(roleList.get(0)[0].toString());//角色id
		
		if(Integer.valueOf(roleId) != 1 && Integer.valueOf(roleId) != 2){
			privateRole = true;
		}
		
		StringBuffer sql = new StringBuffer();
		/*sql.append(" select nepi.normal_expend_plan_id,if(nepi.project_id is null,gp.the_value,rp.the_value) as the_value,");
		sql.append(" if(nepi.project_id is null,nepi.generic_project_id,nepi.project_id)as the_id,if(nepi.project_id is null,2,1) as type,");
		sql.append(" FORMAT(nepi.budget_amount,2) as budget_amount,");
		sql.append(" FORMAT(nepi.budget_amount_frozen,2) as budget_amount_frozen,");
		sql.append(" FORMAT(nepi.budget_amount_surplus,2) as budget_amount_surplus,");
		sql.append(" round(nepi.budget_amount_frozen / nepi.budget_amount * 100,2), ");
		sql.append(" if (nepi.project_id IS NULL, y1.the_value, y2.the_value) as depart_name ");
		sql.append(" from normal_expend_plan_info nepi");
		sql.append(" left join generic_project gp on gp.the_id = nepi.generic_project_id");
		sql.append(" left join routine_project rp on rp.the_id = nepi.project_id");
		sql.append(" LEFT JOIN ys_department_info y1 on gp.department_info_id=y1.the_id ");
		sql.append(" LEFT JOIN ys_department_info y2 on rp.department_info_id=y2.the_id ");
//		sql.append(" left join ys_department_info ydi on ydi.the_id = gp.department_info_id");
//		sql.append(" left join ys_department_info ydi1 on ydi1.the_id = rp.department_info_id");
		sql.append(" where 1 = 1");*/
		
		
		sql.append(" select nepi.normal_expend_plan_id,if(nepi.project_id is null,gp.the_value,rp.the_value) as the_value,");
		sql.append(" if(nepi.project_id is null,nepi.generic_project_id,nepi.project_id)as the_id,if(nepi.project_id is null,2,1) as typet,");
		sql.append(" FORMAT(nepi.budget_amount,2) as budget_amount,");
		sql.append(" FORMAT(if(nepi.project_id IS NULL,sum(eap1.expend_money),sum(eap.expend_money)),2) AS budget_amount_frozen,");
		sql.append(" FORMAT(nepi.budget_amount - if(nepi.project_id IS NULL,sum(eap1.expend_money),sum(eap.expend_money)),2) as budget_amount_surplus,");
		sql.append(" round(if(nepi.project_id IS NULL,sum(eap1.expend_money),sum(eap.expend_money)) / nepi.budget_amount * 100,2) as roundnum, ");
		sql.append(" if (nepi.project_id IS NULL, y1.the_value, y2.the_value) as depart_name ");
		sql.append(" from normal_expend_plan_info nepi");
		sql.append(" left join generic_project gp on gp.the_id = nepi.generic_project_id");
		sql.append(" left join routine_project rp on rp.the_id = nepi.project_id");
		sql.append(" LEFT JOIN expend_apply_project eap on eap.project_id=nepi.project_id ");
		sql.append(" LEFT JOIN expend_apply_project eap1 on eap1.generic_project_id=nepi.generic_project_id ");
		sql.append(" LEFT JOIN ys_department_info y1 on gp.department_info_id=y1.the_id ");
		sql.append(" LEFT JOIN ys_department_info y2 on rp.department_info_id=y2.the_id ");
		sql.append(" left join expend_apply_info eai on eap.expend_apply_info_id=eai.expend_apply_info_id ");
		sql.append(" LEFT JOIN expend_apply_info eai1 on eap1.expend_apply_info_id=eai1.expend_apply_info_id ");
		sql.append(" where 1 = 1 and (eap.deleted=0 or eap1.deleted=0)	");
		sql.append(" and (eai.deleted=0 or eai1.deleted=0) ");
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
		if(null != departmentInfoId && !"".equals(departmentInfoId)){
			sql.append(" and (gp.department_info_id = ").append(departmentInfoId);
			sql.append(" or rp.department_info_id = ").append(departmentInfoId).append(") ");
		}
		if(null != departmentIds && !"".equals(departmentIds)){
			sql.append(" and (gp.department_info_id in (").append(departmentIds).append(") ");
			sql.append(" or rp.department_info_id in (").append(departmentIds).append(")) ");
		}
		if(null != projectName && !"".equals(projectName)){
			sql.append(" and (gp.the_value like '%").append(projectName).append("%' ");
			sql.append(" or rp.the_value like '%").append(projectName).append("%') ");
		}
		sql.append(" group by nepi.normal_expend_plan_id ");
		//sql.insert(0, "select * from (").append(") as recordset");
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
			departCol.setCellValue(obj[8].toString());
			departCol.setCellStyle(colStyle);
			departCol.getSheet().setColumnWidth(
					departCol.getColumnIndex(), 35 * 120);
			col++;
			
			natureCol = row1.createCell(col);
			natureCol.setCellType(HSSFCell.CELL_TYPE_STRING);
			natureCol.setCellValue(obj[2].toString().equals("1") ? "一般项目" : "常规项目");
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
			
			addMoneyCol = row1.createCell(col);
			addMoneyCol.setCellType(HSSFCell.CELL_TYPE_STRING);
			addMoneyCol.setCellValue(obj[6].toString());
			addMoneyCol.setCellStyle(colStyle);
			addMoneyCol.getSheet().setColumnWidth(
					addMoneyCol.getColumnIndex(), 35 * 200);
			col++;
			
			addPercentCol = row1.createCell(col);
			addPercentCol.setCellType(HSSFCell.CELL_TYPE_STRING);
			addPercentCol.setCellValue(obj[7].toString());
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

	public String getBeginMonth() {
		return beginMonth;
	}

	public void setBeginMonth(String beginMonth) {
		this.beginMonth = beginMonth;
	}

	public String getBeginDay() {
		return beginDay;
	}

	public void setBeginDay(String beginDay) {
		this.beginDay = beginDay;
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
	
	

}
