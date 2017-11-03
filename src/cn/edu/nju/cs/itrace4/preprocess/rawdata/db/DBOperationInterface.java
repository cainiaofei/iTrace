package cn.edu.nju.cs.itrace4.preprocess.rawdata.db;

import java.sql.ResultSet;

public interface DBOperationInterface {
	public void buildConnection(String driver,String dbPath);
	public boolean closeConnection();
	public void executeSql(String sql);
	public ResultSet executeQuery(String sql);
}
