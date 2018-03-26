package cn.dmdl.stl.hospitalbudget.util;

import java.text.DecimalFormat;

import org.jboss.seam.annotations.Name;

@Name("numberUtil")
public class NumberUtil {
	
	/**
	 * 字符串转double保留两位小数
	 * @param str
	 * @return
	 */
	public static Object double2Str(Object str){
		if(null == str || "".equals(str)){
			return str;
		}
		if(Double.parseDouble(str.toString()) == 0){
			return "0.00";
		}
		Double d1 = Double.parseDouble(str.toString());
		DecimalFormat df = new DecimalFormat("#.00"); 
		return df.format(d1);
	}
	
	public static String formatDouble2(Object str){
		if(null == str || "".equals(str)){
			return "0.00";
		}
		if(Double.parseDouble(str.toString()) == 0){
			return "0.00";
		}
		Double d1 = Double.parseDouble(str.toString());
		DecimalFormat df = new DecimalFormat("#.00"); 
		return df.format(d1);
	}
	
	
	public static void main(String[] args){
		System.out.println(formatDouble2(0));
	}
	
	
	public static String transferNum(Object str){
		if(null == str || "".equals(str)){
			return "0.00";
		}
		if(Double.parseDouble(str.toString()) == 0){
			return "0.00";
		}
		Double d1 = Double.parseDouble(str.toString());
		DecimalFormat df = new DecimalFormat("###,###,###.00"); 
		return df.format(d1);
	}
}
