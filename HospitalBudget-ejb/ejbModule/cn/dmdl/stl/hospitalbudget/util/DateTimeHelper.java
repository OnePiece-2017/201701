package cn.dmdl.stl.hospitalbudget.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

public class DateTimeHelper {

	private static Logger logger = Logger.getLogger(DateTimeHelper.class);
	public static final String PATTERN_DATE_TIME = "yyyy-MM-dd HH:mm:ss";
	public static final String PATTERN_DATE_HM = "yyyy-MM-dd HH:mm";
	public static final String PATTERN_DATE = "yyyy-MM-dd";
	public static final String PATTERN_CN_DATE = "yyyy年MM月dd日";
	public static final String PATTERN_DATE_YM = "yyyy-MM";
	public static final String PATTERN_CN_DATE_YM = "yyyy年MM月";
	public static final String PATTERN_DATE_BEGIN = "yyyy-MM-dd 00:00:00";
	public static final String PATTERN_DATE_END = "yyyy-MM-dd 23:59:59";
	public static final String PATTERN_DATE_YW = "yyyy-ww";
	public static final String PATTERN_CN_DATE_YW = "yyyy年第ww周";
	public static final String PATTERN_DATE_TIME_FULL = "yyyy-MM-dd HH:mm:ss.SSS";

	public static String dateToStr(Date date, String pattern) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		return simpleDateFormat.format(date);
	}

	public static Date strToDate(String source, String pattern) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		try {
			return simpleDateFormat.parse(source);
		} catch (ParseException e) {
			logger.error("strToDate(String source, String pattern)-->", e);
		}
		return null;
	}

}
