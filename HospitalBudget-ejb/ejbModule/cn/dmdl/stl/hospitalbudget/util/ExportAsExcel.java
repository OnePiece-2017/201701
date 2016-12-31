package cn.dmdl.stl.hospitalbudget.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.util.CellRangeAddress;

/**
 * 导出为Excel
 * <p>
 * 默认为 Microsoft Office Excel 2003 升级版将实现2007/2010/2013
 * <p>
 * 自定义样式，自定义模板，异常处理机制，尚未开发。
 * <p>
 */
public class ExportAsExcel extends ExportManager {

	public final static byte TYPE_EXCEL_2003 = 1;// Microsoft Excel 工作表 (.xls) - Microsoft Office Excel 2003
	public final static byte TYPE_EXCEL_2007 = 2;// Microsoft Excel 工作表 (.xlsx) - Microsoft Office Excel 2007
	private final static short MIN_SHEET_CAPACITY = 1;// 最小工作表数量
	private final static short MAX_SHEET_CAPACITY = 256;// 最大工作表数量
	private byte type;// 类型
	private short sheetCapacity;// 工作表数量
	private String[] sheetName;// 工作表名称
	private String[] titleCellValue;// 大标题
	private List<String[]> headerCellValue;// 列名称
	private List<List<Object[]>> dataCellValue;// 列数据
	private boolean autoSheetName;// 自动设置工作表名称
	private boolean mostSuitableWidth;// 最适合的列宽

	public ExportAsExcel(String name, byte type) {
		super.setName(name);
		if (type == TYPE_EXCEL_2003) {
			super.setSuffix(".xls");
		} else if (type == TYPE_EXCEL_2007) {
			super.setSuffix(".xlsx");
		} else {
			super.setSuffix(null);
		}
		this.type = type;
		sheetCapacity = MIN_SHEET_CAPACITY;// 默认工作表数量
		autoSheetName = true;// 默认为自动
		mostSuitableWidth = true;// 默认为最适合的列宽
	}

	public ExportAsExcel(String name) {
		this(name, TYPE_EXCEL_2003);
	}

	public ExportAsExcel(byte type) {
		this(null, type);
	}

	/**
	 * 导出为Excel
	 * <p>
	 * 范例:
	 * <p>
	 * <blockquote>
	 * 
	 * <pre>
	 * ExportAsExcel excel = new ExportAsExcel();
	 * excel.setSheetCapacity((short) 2);
	 * excel.setSheetName(new String[]{"烟草类","酒水类","酒水类"});
	 * excel.setTitleCellValue(new String[]{"名烟", "名酒"});
	 * List&ltString[]> headerCellValue = new ArrayList&ltString[]>();
	 * headerCellValue.add(new String[]{"品名", "焦油量", "参考价格(包)"});
	 * headerCellValue.add(new String[]{"品名", "类型", "酒精度", "参考价格"});
	 * excel.setHeaderCellValue(headerCellValue);
	 * List&ltList&ltObject[]>> dataCellValue = new ArrayList&ltList&ltObject[]>>();
	 * List&ltObject[]> e1 = new ArrayList&ltObject[]>();
	 * e1.add(new String[]{"黄鹤楼(软短1916)", "10mg", "￥100"});
	 * e1.add(new String[]{"中华(软)", "11mg", "￥65"});
	 * e1.add(new String[]{"南京(九五)", "11mg", "￥100"});
	 * dataCellValue.add(e1);
	 * List&ltObject[]> e2 = new ArrayList&ltObject[]>();
	 * e2.add(new String[]{"52°五粮液500ml", "白酒", "52%Vol", "￥1519.00/瓶"});
	 * e2.add(new String[]{"40°英国皇家礼炮21年700ml", "洋酒", "40%Vol", "￥1348.00/瓶"});
	 * dataCellValue.add(e2);
	 * excel.setDataCellValue(dataCellValue);
	 * System.out.println("Return --> " + excel.save());
	 * </pre>
	 * 
	 * </blockquote>
	 */
	public ExportAsExcel() {
		this(TYPE_EXCEL_2003);
	}

	public byte getType() {
		return type;
	}

	public void setType(byte type) {
		this.type = type;
	}

	public short getSheetCapacity() {
		return sheetCapacity;
	}

	public void setSheetCapacity(short sheetCapacity) {
		this.sheetCapacity = sheetCapacity;
	}

	public String[] getSheetName() {
		return sheetName;
	}

	public void setSheetName(String[] sheetName) {
		this.sheetName = sheetName;
	}

	public String[] getTitleCellValue() {
		return titleCellValue;
	}

	public void setTitleCellValue(String[] titleCellValue) {
		this.titleCellValue = titleCellValue;
	}

	public List<String[]> getHeaderCellValue() {
		return headerCellValue;
	}

	public void setHeaderCellValue(List<String[]> headerCellValue) {
		this.headerCellValue = headerCellValue;
	}

	public List<List<Object[]>> getDataCellValue() {
		return dataCellValue;
	}

	public void setDataCellValue(List<List<Object[]>> dataCellValue) {
		this.dataCellValue = dataCellValue;
	}

	public boolean isAutoSheetName() {
		return autoSheetName;
	}

	public void setAutoSheetName(boolean autoSheetName) {
		this.autoSheetName = autoSheetName;
	}

	public boolean isMostSuitableWidth() {
		return mostSuitableWidth;
	}

	public void setMostSuitableWidth(boolean mostSuitableWidth) {
		this.mostSuitableWidth = mostSuitableWidth;
	}

	@Override
	public String save() {
		if (getFilename() != null && !"".equals(getFilename())) {
			if (type == TYPE_EXCEL_2003) {
				return asExcel2003();
			} else if (type == TYPE_EXCEL_2007) {
				return asExcel2007();
			} else {
				return "Fatal error! Type not specified.";
			}
		} else {
			return "Fatal error! Filename not specified.";
		}
	}

	private String asExcel2003() {
		String result = null;
		if (sheetCapacity >= MIN_SHEET_CAPACITY && sheetCapacity <= MAX_SHEET_CAPACITY) {
			if ((sheetName != null && sheetName.length >= sheetCapacity) || (sheetName == null && autoSheetName)) {
				if (sheetName != null) {
					HashSet<String> sheetNameSet = new HashSet<String>();
					for (int i = 0; i < sheetCapacity; i++) {
						sheetNameSet.add(sheetName[i]);
					}
					sheetNameSet.remove(null);
					sheetNameSet.remove("");
					if (sheetNameSet.size() < sheetCapacity) {
						result = "Fatal error! Invalid sheet name. Caused by duplicate sheet name, null, \"\"";
					}
				}
				if (result == null) {
					FacesContext facesContext = FacesContext.getCurrentInstance();
					facesContext.responseComplete();
					HttpServletResponse httpServletResponse = (HttpServletResponse) facesContext.getExternalContext().getResponse();
					HSSFWorkbook hssfWorkbook = new HSSFWorkbook();

					// 大标题样式
					HSSFCellStyle titleCellStyle = hssfWorkbook.createCellStyle();
					HSSFFont titleCellFont = hssfWorkbook.createFont();
					titleCellFont.setFontName("黑体");// 指定字体
					titleCellFont.setFontHeightInPoints((short) 14);// 设置字体大小
					titleCellFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);// 加粗
					titleCellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);// 左右居中
					titleCellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);// 上下居中
					titleCellStyle.setFont(titleCellFont);

					// 标题单元格样式
					HSSFCellStyle headerCellStyle = hssfWorkbook.createCellStyle();
					HSSFFont headerCellFont = hssfWorkbook.createFont();
					headerCellFont.setFontName("黑体");// 指定字体
					headerCellFont.setFontHeightInPoints((short) 11);// 设置字体大小
					headerCellFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);// 加粗
					headerCellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);// 左右居中
					headerCellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);// 上下居中
					headerCellStyle.setFont(headerCellFont);

					// 数据单元格样式
					HSSFCellStyle dataCellStyle = hssfWorkbook.createCellStyle();
					HSSFFont dataCellFont = hssfWorkbook.createFont();
					dataCellFont.setFontName("黑体");// 指定字体
					dataCellFont.setFontHeightInPoints((short) 11);// 设置字体大小
					// dataCellFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);// 加粗
					dataCellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);// 左右居中
					dataCellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);// 上下居中
					dataCellStyle.setFont(dataCellFont);

					// 总计单元格样式
					HSSFCellStyle totalCellStyle = hssfWorkbook.createCellStyle();
					HSSFFont totalCellFont = hssfWorkbook.createFont();
					totalCellFont.setFontName("黑体");
					totalCellFont.setFontHeightInPoints((short) 11);// 字体大小
					totalCellFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);// 加粗
					totalCellStyle.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
					totalCellStyle.setFillForegroundColor(HSSFColor.GOLD.index);
					totalCellStyle.setFillPattern(HSSFCellStyle.FINE_DOTS);
					totalCellStyle.setFont(totalCellFont);

					// 清空单元格样式-重置-默认-清除样式 resetCellStyle/defaultCellStyle
					HSSFCellStyle noneCellStyle = hssfWorkbook.createCellStyle();
					noneCellStyle.setBorderBottom(HSSFCellStyle.BORDER_NONE);
					noneCellStyle.setBorderLeft(HSSFCellStyle.BORDER_NONE);
					noneCellStyle.setBorderRight(HSSFCellStyle.BORDER_NONE);
					noneCellStyle.setBorderTop(HSSFCellStyle.BORDER_NONE);
					noneCellStyle.setTopBorderColor(HSSFColor.WHITE.index);
					noneCellStyle.setBottomBorderColor(HSSFColor.WHITE.index);
					for (int sheetIx = 0; sheetIx < sheetCapacity; sheetIx++) {// 遍历工作表
						int rownum = -1;// 行索引
						int column = 0;// 列索引
						HSSFSheet hssfSheet = hssfWorkbook.createSheet();
						// 工作表名称
						if (sheetName != null) {
							hssfWorkbook.setSheetName(sheetIx, sheetName[sheetIx]);
						} else {
							hssfWorkbook.setSheetName(sheetIx, "Sheet" + (sheetIx + 1));
						}
						// 大标题
						if (titleCellValue != null && titleCellValue.length > sheetIx) {
							rownum++;
							HSSFRow hssfRow = hssfSheet.createRow(rownum);
							HSSFCell hssfCell = hssfRow.createCell(column);
							hssfCell = hssfRow.createCell(column);
							hssfCell.setCellType(HSSFCell.CELL_TYPE_STRING);
							hssfCell.setCellValue(titleCellValue[sheetIx]);
							hssfCell.setCellStyle(titleCellStyle);
							hssfCell.getSheet().setColumnWidth(hssfCell.getColumnIndex(), 25 * 150);
							// 列名称
							if (headerCellValue != null && headerCellValue.size() > sheetIx && headerCellValue.get(sheetIx) != null && headerCellValue.get(sheetIx).length > 0) {
								CellRangeAddress rangeAddress = new CellRangeAddress(rownum, rownum, 0, headerCellValue.get(sheetIx).length - 1);
								hssfSheet.addMergedRegion(rangeAddress);
							}
						}
						// 列名称
						if (headerCellValue != null && headerCellValue.size() > sheetIx && headerCellValue.get(sheetIx) != null && headerCellValue.get(sheetIx).length > 0) {
							rownum++;
							HSSFRow hssfRow = hssfSheet.createRow(rownum);
							column = 0;
							for (String header : headerCellValue.get(sheetIx)) {
								HSSFCell hssfCell = hssfRow.createCell(column);
								hssfCell.setCellType(HSSFCell.CELL_TYPE_STRING);// 根据用户指定的配置来设置单元格数据格式 升级版
								hssfCell.setCellValue(header != null ? header : "");// 测试null的情况
								hssfCell.setCellStyle(headerCellStyle);
								hssfCell.getSheet().setColumnWidth(hssfCell.getColumnIndex(), 25 * 200);// 声明属性-是否自动调整列宽-默认不自动
								column++;
							}
						}
						// 列数据
						if (dataCellValue != null && dataCellValue.size() > sheetIx && dataCellValue.get(sheetIx) != null && dataCellValue.get(sheetIx).size() > 0) {
							for (Object[] rowData : dataCellValue.get(sheetIx)) {// 遍历行
								rownum++;
								HSSFRow hssfRow = hssfSheet.createRow(rownum);
								if (rowData != null && rowData.length > 0) {
									column = 0;
									for (Object colData : rowData) {// 遍历列
										HSSFCell hssfCell = hssfRow.createCell(column);
										hssfCell.setCellType(HSSFCell.CELL_TYPE_STRING);// 根据用户指定的配置来设置单元格数据格式 升级版
										hssfCell.setCellValue(colData != null ? colData.toString() : "");
										hssfCell.setCellStyle(dataCellStyle);
										hssfCell.getSheet().setColumnWidth(hssfCell.getColumnIndex(), 25 * 200);
										column++;
									}
								}
							}
						}
					}

					httpServletResponse.setContentType("application/vnd.ms-excel");
					try {
						httpServletResponse.setHeader("Content-Disposition", "attachment;filename=" + new String(getFilename().getBytes(), "iso-8859-1"));
						hssfWorkbook.write(httpServletResponse.getOutputStream());
						hssfWorkbook.close();
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			} else {
				result = "Fatal error! Sheet name not specified.";
			}
		} else {
			result = "Fatal error! Invalid sheet capacity.";
		}
		return result;
	}

	private String asExcel2007() {
		return null;
	}

}
