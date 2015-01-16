package huck.common.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBConnectionManager {
	private String url;
	private String user;
	private String password;
	private String database;

	public DBConnectionManager(String url) {
		this.url = url;
		this.user = null;
		this.password = null;
	}
	
	public DBConnectionManager(String url, String database) {
		this.url = url;
		this.user = null;
		this.password = null;
		this.database = database;
	}

	public DBConnectionManager(String url, String user, String password) {
		this.url = url;
		this.user = user;
		this.password = password;
	}
	public Connection getConnection() throws SQLException {
		Connection conn;
		if( null == user ) {
			conn = DriverManager.getConnection(url);
		} else {
			conn = DriverManager.getConnection(url, user, password);
		}
		if( null != database ) {
			try (Statement st = conn.createStatement()) {
				st.execute("USE "+database);
			}
		}
		return conn;
	}
}
