package cn.dmdl.stl.hospitalbudget.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import cn.dmdl.stl.hospitalbudget.boot.ConfigureCache;

public class SqlServerJDBCUtil {
	private final static String DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";        // 驱动
//    private final static String URL = "jdbc:sqlserver://10.193.7.229:1433;databaseName=HMMIS_BUDG";        // 连接地址
    private final static String URL = "jdbc:sqlserver://10.193.28.18:1433;databaseName=HMMIS_BUDG";        // 连接地址
    private final static String USERNAME = "budg";        // 账号
    private final static String PASSWORD = "budg!@#$";        // 密码        
   
   /* private final static String URL = "jdbc:sqlserver://127.0.0.1:1433;databaseName=HMMIS_BUDG";        // 连接地址
    private final static String USERNAME = "sa";        // 账号
    private final static String PASSWORD = "120823";        // 密码        
*/   
   
    public static Connection GetConnection(){
    	Connection conn = null;
        try {
        	Class.forName(DRIVER);
            conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }
   
   /**
    　* 关闭所有JDBC操作对象
    　* @param conn        连接对象
    　* @param pstmt        执行对象
    　* @param rs        结果集
    　*/
   public static void CloseAll(Connection conn, PreparedStatement pstmt, ResultSet rs){
	   if(null!=conn){
		   try {
			   conn.close();
		   } catch (SQLException e) {
               e.printStackTrace();
		   }
	   }
	   if(null!=pstmt){
		   try {
			   conn.close();
		   } catch (SQLException e) {
			   e.printStackTrace();
		   }
	   }
	   if(null!=rs){
		   try {
			   conn.close();
		   } catch (SQLException e) {
			   e.printStackTrace();
		   }
	   }
   }
   
   
   /**
    * 计算
    * @param year
    * @param frozenMoney
    */
   public static void calculateOldBudg(String budgCode,Double frozenMoney){
	   String sql = "update HMMIS_BUDG.dbo.budg_type_dict set budg_year_frozen = "+frozenMoney
			   +" where budg_code="+budgCode  + " and budg_hospital_code = '" + ConfigureCache.getValue("hospital.source") + "' ";
	   String querySql = "select * from HMMIS_BUDG.dbo.budg_type_dict where budg_code =" +budgCode + " and budg_hospital_code = '" + ConfigureCache.getValue("hospital.source") + "' ";
	   Connection conn = GetConnection();
	   PreparedStatement ps = null;
	   ResultSet rs = null;
	   try {
		   	/*ps = conn.prepareStatement(querySql);
		   	rs = ps.executeQuery();
		   	if(rs.next()){*/
		   		ps = conn.prepareStatement(sql);
				ps.executeUpdate();
		   	/*}*/
			
		}catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			CloseAll(conn, ps, rs);
		}
   }
   
   /**
    * 计算
    * @param year
    * @param frozenMoney
    */
   public static void checkReturn(String billCode,Double frozenMoney){
	   String sql = "update HMMIS_BUDG.dbo.budg_type_dict set budg_year_frozen = "+frozenMoney
			   +" where budg_code=?" + " and budg_hospital_code = '" + ConfigureCache.getValue("hospital.source") + "' ";
	   String projectSql = "update HMMIS_BUDG.dbo.budg_application4expenditure set state=0 ,state_date=GETDATE() "+
				"where bill_code=?";
	   String querySql = "select budg_code from HMMIS_BUDG.dbo.budg_application4expenditure where bill_code =?";
	   Connection conn = GetConnection();
	   PreparedStatement ps = null;
	   ResultSet rs = null;
	   String budgCode = "";
	   try {
		   ps =conn.prepareStatement(querySql);
		   ps.setString(1, billCode);
		   rs = ps.executeQuery();
		   while(rs.next()){
			   budgCode = rs.getString("budg_code");  
		   }
		   
		   	ps = conn.prepareStatement(projectSql);
		   	ps.setString(1, billCode);
			ps.executeUpdate();
		   
			ps = conn.prepareStatement(sql);
			ps.setString(1, budgCode);
			ps.executeUpdate();
			
		}catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			CloseAll(conn, ps, rs);
		}
   }
   
   public static void sure(String billCode,double expendMoney,String comment){
	   Connection conn = SqlServerJDBCUtil.GetConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		String updateSql = "update HMMIS_BUDG.dbo.budg_application4expenditure set state=1,state_date= ? ,state_brif =? where bill_code=? ";
		try{
			//状态修改
			java.util.Date date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()));//获取系统时间
			java.sql.Timestamp date1=new java.sql.Timestamp(date.getTime());//
			ps = conn.prepareStatement(updateSql);
			ps.setTimestamp(1, date1);
			ps.setString(2, comment);
			ps.setString(3,billCode);
			ps.executeUpdate();
			
			
			 String querySql = "select budg_code from HMMIS_BUDG.dbo.budg_application4expenditure where bill_code =?";
			 ps = conn.prepareStatement(querySql);
			 ps.setString(1, billCode);
			 rs = ps.executeQuery();
			 String budgCode = "";
			 while(rs.next()){
				 budgCode = rs.getString("budg_code");
			 }
			//钱修改
			String sql = "update HMMIS_BUDG.dbo.budg_type_dict set budg_year_out_money = (budg_year_out_money + ? ),budg_year_frozen=(budg_year_frozen-?)  where "+
					   " budg_code = ?" + " and budg_hospital_code = '" + ConfigureCache.getValue("hospital.source") + "' ";
			ps = conn.prepareStatement(sql);
			ps.setDouble(1, expendMoney);
			ps.setDouble(2, expendMoney);
			ps.setString(3, budgCode);
			ps.executeUpdate();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			SqlServerJDBCUtil.CloseAll(conn, ps, rs);
		}
   }
   
   
   public static void otherSure(double expendMoney,String nepi){
	   Connection conn = SqlServerJDBCUtil.GetConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			//钱修改
			String sql = "update HMMIS_BUDG.dbo.budg_type_dict set budg_year_out_money = (budg_year_out_money + ? ),budg_year_frozen=(budg_year_frozen-?)  where "+
					   " budg_code = ?" + " and budg_hospital_code = '" + ConfigureCache.getValue("hospital.source") + "' ";
			ps = conn.prepareStatement(sql);
			ps.setDouble(1, expendMoney);
			ps.setDouble(2, expendMoney);
			ps.setString(3, nepi);
			ps.executeUpdate();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			SqlServerJDBCUtil.CloseAll(conn, ps, rs);
		}
   }
   
   
   
   public static void sureReturn(String billCode,double expendMoney,String comment){
	   Connection conn = SqlServerJDBCUtil.GetConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		String updateSql = "update HMMIS_BUDG.dbo.budg_application4expenditure set state=0,state_date= ? ,state_brif =? where bill_code=? ";
		try{
			//状态修改
			java.util.Date date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()));//获取系统时间
			java.sql.Timestamp date1=new java.sql.Timestamp(date.getTime());//
			ps = conn.prepareStatement(updateSql);
			ps.setTimestamp(1, date1);
			ps.setString(2, comment);
			ps.setString(3,billCode);
			ps.executeUpdate();
			
			 String querySql = "select budg_code from HMMIS_BUDG.dbo.budg_application4expenditure where bill_code =?";
			 ps = conn.prepareStatement(querySql);
			 ps.setString(1, billCode);
			 rs=ps.executeQuery();
			 String budgCode = "";
			 while(rs.next()){
				 budgCode = rs.getString("budg_code");
			 }
			//钱修改
			String sql = "update HMMIS_BUDG.dbo.budg_type_dict set budg_year_frozen=(budg_year_frozen-?)  where "+
					   " budg_code = ?" + " and budg_hospital_code = '" + ConfigureCache.getValue("hospital.source") + "' ";
			ps = conn.prepareStatement(sql);
			ps.setDouble(1, expendMoney);
			ps.setString(2, budgCode);
			ps.executeUpdate();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			SqlServerJDBCUtil.CloseAll(conn, ps, rs);
		}
   }
   
   
   public static void otherReturn(double expendMoney,String nepi){
	   Connection conn = SqlServerJDBCUtil.GetConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			//钱修改
			String sql = "update HMMIS_BUDG.dbo.budg_type_dict set budg_year_frozen=(budg_year_frozen-?)  where "+
					   " budg_code = ?" + " and budg_hospital_code = '" + ConfigureCache.getValue("hospital.source") + "' ";
			ps = conn.prepareStatement(sql);
			ps.setDouble(1, expendMoney);
			ps.setString(2, nepi);
			ps.executeUpdate();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			SqlServerJDBCUtil.CloseAll(conn, ps, rs);
		}
   }
}
