package huck.common.jdbc;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class DAOResultSet {
	private ArrayList<HashMap<String, Object>> result = new ArrayList<HashMap<String, Object>>();
	private int cursor;

	public DAOResultSet(ResultSet rs) throws SQLException {
		ResultSetMetaData meta = rs.getMetaData();
		while( rs.next() ) {
			HashMap<String,Object> row = new HashMap<String,Object>();
			for( int i=1; i<=meta.getColumnCount(); i++ ) {
				row.put(meta.getColumnLabel(i), rs.getObject(i));
			}
			result.add(row);
		}
		cursor = -1;
	}

	public boolean next() {
		return ++cursor < result.size();
	}

	public Object get(String name) {
		return result.get(cursor).get(name);
	}

	public Date getDate(String name) {
		Object obj = get(name);
		if( null == obj ) {
			return null;
		} else if( obj instanceof Date ) {
			return (Date)obj;
		} else {
			return null;
		}
	}
	public String getString(String name) {
		Object obj = get(name);
		if( null == obj ) {
			return null;
		} else if( obj instanceof String ) {
			return (String)obj;
		} else {
			return obj.toString();
		}
	}
	public Long getLong(String name) {
		Object obj = get(name);
		if( null == obj ) {
			return null;
		} else if( obj instanceof Byte ) {
			return (long)(Byte)obj;
		} else if( obj instanceof Short ) {
			return (long)(Short)obj;
		} else if( obj instanceof Integer ) {
			return (long)(Integer)obj;
		} else if( obj instanceof Long ) {
			return (Long)obj;
		} else {
			return Long.parseLong(obj.toString());
		}
	}
	public Integer getInt(String name) {
		Object obj = get(name);
		if( null == obj ) {
			return null;
		} else if( obj instanceof Byte ) {
			return (int)(Byte)obj;
		} else if( obj instanceof Short ) {
			return (int)(Short)obj;
		} else if( obj instanceof Integer ) {
			return (Integer)obj;
		} else {
			return Integer.parseInt(obj.toString());
		}
	}

	public int size() {
		return result.size();
	}
}
