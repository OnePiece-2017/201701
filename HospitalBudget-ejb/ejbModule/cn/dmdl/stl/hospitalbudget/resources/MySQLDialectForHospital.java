package cn.dmdl.stl.hospitalbudget.resources;

import java.sql.Types;

import org.hibernate.dialect.MySQL5Dialect;

public class MySQLDialectForHospital extends MySQL5Dialect{

	public MySQLDialectForHospital() {
		super();
		registerHibernateType(Types.LONGVARCHAR, 65535, "text"); 
	}
}
