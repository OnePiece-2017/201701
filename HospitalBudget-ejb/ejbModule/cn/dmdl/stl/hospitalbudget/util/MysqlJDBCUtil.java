package cn.dmdl.stl.hospitalbudget.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MysqlJDBCUtil {
	private final static String DRIVER = "com.mysql.jdbc.Driver";        // 驱动
    private final static String URL = "jdbc:mysql://127.0.0.1:3306/bfh_budget";        // 连接地址
    private final static String USERNAME = "nseer";        // 账号
    private final static String PASSWORD = "BTSnseer#.wl";        // 密码     
    
/*    private final static String URL = "jdbc:mysql://127.0.0.1:3306/bfh_budget";        // 连接地址
    private final static String USERNAME = "root";        // 账号
    private final static String PASSWORD = "tiaozhan14";        // 密码
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
}
