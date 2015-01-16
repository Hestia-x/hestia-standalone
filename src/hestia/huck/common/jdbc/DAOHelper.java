package huck.common.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DAOHelper implements AutoCloseable {
	public DAOHelper(DBConnectionManager dbConnMgr, boolean autoCommit) throws SQLException {
		Connection conn = null;
		Statement st = null;
		ResultSet rs = null;
		try {
			conn = dbConnMgr.getConnection();
			st = conn.createStatement();
			rs = st.executeQuery("SELECT 1");
			try{rs.close();}catch(Exception ignore){};
			try{st.close();}catch(Exception ignore){};
		} catch( Exception ex ) {
			if(null!=rs) try{rs.close();}catch(Exception ignore){};
			if(null!=st) try{st.close();}catch(Exception ignore){};
			if(null!=conn) try{conn.close();}catch(Exception ignore){};			
			conn = dbConnMgr.getConnection();
		}

		connection = conn;
		commit = true;
		conn.setAutoCommit(!autoCommit);
	}

	private boolean commit;
	private Connection connection;


	public final void commit() throws SQLException {
		Connection conn = connection;
		if( null != conn ) {
			conn.commit();
			commit = true;
		}
	}

	@Override
	public final void close() {
		Connection conn = connection;
		if( null != conn ) {
			try {
				if( !conn.getAutoCommit() ) {
					if( !commit ) {
						conn.rollback();
					}
					conn.setAutoCommit(true);
				}
			} catch( Exception ignore ) {
			}
			try {
				conn.close();
			} catch( Exception ignore ) {
			}
			connection = null;
		}
	}


	public final int executeUpdate(String sql, Object... var) throws SQLException {
		Connection conn = connection;
		PreparedStatement st = conn.prepareStatement(sql);

		try {
			if( null != var ) {
				int idx = 1;
				for( Object obj : var ) {
					st.setObject(idx++, obj);
				}
			}

			int result = st.executeUpdate();
			commit = false;
			return result;
		} finally {
			try{st.close();}catch(Exception ignore){}
		}
	}

	public final DAOResultSet executeQuery(String sql, Object... var) throws SQLException {
		Connection conn = connection;
		PreparedStatement st = conn.prepareStatement(sql);
		try {
			if( null != var ) {
				int idx = 1;
				for( Object obj : var ) {
					st.setObject(idx++, obj);
				}
			}

			ResultSet rs = st.executeQuery();
			try {
				return new DAOResultSet(rs);
			} finally {
				try{rs.close();}catch(Exception ignore){}
			}
		} finally {
			try{st.close();}catch(Exception ignore){}
		}
	}
}
