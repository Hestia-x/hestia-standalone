package huck.hestia;

public class RequestPath {
	private String[] paths;
	private int offset;
	private int len;
	
	private RequestPath(String[] paths, int offset, int len) {
		this.paths = paths;
		this.offset = offset;
		this.len = len;		
	}
	
	public RequestPath(String path) {
		if( 0 == path.length() ) {
			paths = new String[0];
			offset = 0;
			len = 0;
		} else {
			paths = path.split("\\/");
			for( int i=0; i<paths.length-1; i++ ) {
				paths[i] += "/"; 
			}
			if( path.endsWith("/") ) {
				paths[paths.length-1] += "/";
			}
			offset = 0;
			len = paths.length;
		}
	}
	
	public String get(int index) {
		if( 0 > index || len <= index ) {
			return "";
		} else {
			return paths[offset+index];
		}
	}
	
	public int getInt(int index, int defaultValue) {
		String value = get(index);
		if( null != value ) {
			if( value.endsWith("/") ) {
				value = value.substring(0, value.length()-1);
			}
			try {
				return Integer.parseInt(value);
			} catch( Exception ex ) {
			}
		}
		return defaultValue;
	}
	
	public Integer getInt(int index) {
		String value = get(index);
		if( null != value ) {
			try {
				return Integer.parseInt(value);
			} catch( Exception ex ) {
			}
		}
		return null;
	}
	
	public int size() {
		return len;
	}
	
	public RequestPath slice(int begin) {
		return new RequestPath(paths, Math.min(offset+begin, paths.length), Math.max(len-begin, 0));
	}
}
