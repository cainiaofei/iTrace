package cn.edu.nju.cs.refactor.util;

import java.sql.ResultSet;

public interface DBProcess extends Tool{
	public void buildConnection(String driver,String dbPath);
	public boolean closeConnection();
	public void executeSql(String sql);
	public ResultSet executeQuery(String sql);
	public void setCommit(boolean flag);
	public void commit();
}
