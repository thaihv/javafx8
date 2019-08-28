package com.uitgis.plugin.tilegenerator;
public class BackendDBMSInfo {

	public static final int DBMS_ORACLE = 1;
	public static final int DBMS_TIBERO = 2;
	public static final int DBMS_MSSQL = 3;
	
	public int mDbmsType;
	public String mJdbcUrl;
	public String mJdbcDriver;
	
}