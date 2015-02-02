package huck.simplehttp;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public final class HttpRequest {
	// connection data
	public int getPort() {
		return port;
	}
	
	// request line
	public String getMethod() {
		return method;
	}
	public String getRequestURI() {
		return requestURI;
	}
	public String getVersion() {
		return version;
	}
	
	// request line parsed
	public String getHost() {
		return host;
	}
	public String getRequestPath() {
		return requestPath;
	}
	public String getQueryString() {
		return queryString;
	}	
	public String getParameter(String name) {
		return paramMap.get(name);
	}

	// from header
	public int getContentLength() {
		return contentLength;
	}	
	public List<String> getHeaderList(String name) {
		return header.get(name.toLowerCase());	
	}
	public String getHeader(String name) {
		List<String> valueList = headerFindMap.get(name.toLowerCase());
		if( null != valueList && !valueList.isEmpty() ) {
			return valueList.get(0);
		} else {
			return null;
		}
	}
	public Map<String, List<String>> getHeaderMap() {
		return header;
	}	
	
	// cookies
	public List<String> getCookieList(String name) {
		return cookie.get(name);	
	}
	public String getCookie(String name) {
		List<String> valueList = cookie.get(name);
		if( null != valueList && !valueList.isEmpty() ) {
			return valueList.get(0);
		} else {
			return null;
		}
	}
	public Map<String, List<String>> getCookieMap() {
		return cookie;
	}
	
	// attributes
	public Object getAttribute(String name) {
		return attribute.get(name);
	}	
	public Object setAttribute(String name, Object value) {
		return attribute.put(name, value);
	}

	private int port;
	private String method;
	private String requestURI;
	private String version;
	
	private String host;
	private String requestPath;
	private String queryString;
	private Map<String, String> paramMap;
	
	private int contentLength;
	private Map<String, List<String>> header;
	private Map<String, List<String>> headerFindMap;
	private Map<String, List<String>> cookie;
	
	private HashMap<String, Object> attribute;
	
	public HttpRequest(HttpRequestData parseData) throws UnsupportedEncodingException {
		this.port = parseData.port;
		this.method = parseData.method;
		this.requestURI = parseData.uri;
		this.version = parseData.version;
		
		this.host = parseData.host;
		this.requestPath = parseData.path;
		this.queryString = parseData.queryString;
		this.paramMap = parseQueryString(queryString);
		
		this.contentLength = parseData.contentLength;
		
		this.header = null;
		this.headerFindMap = null;
		this.cookie = null;
		this.attribute = new HashMap<>();
		
		this.header = new HashMap<String, List<String>>();
		this.headerFindMap = new HashMap<String, List<String>>();
		for( Map.Entry<String, ArrayList<String>> entry : parseData.header.entrySet() ) {
			this.header.put(entry.getKey(), Collections.unmodifiableList(entry.getValue()));
			this.headerFindMap.put(entry.getKey().toLowerCase(), Collections.unmodifiableList(entry.getValue()));
		}
		this.header = Collections.unmodifiableMap(this.header);
		this.headerFindMap = Collections.unmodifiableMap(this.headerFindMap);
		
		this.cookie = new HashMap<String, List<String>>();
		for( Map.Entry<String, ArrayList<String>> entry : parseData.cookie.entrySet() ) {
			this.cookie.put(entry.getKey(), Collections.unmodifiableList(entry.getValue()));
		}
		this.cookie = Collections.unmodifiableMap(this.cookie);
	}
	
	public static Map<String, String> parseQueryString(String queryString) throws UnsupportedEncodingException {
		HashMap<String, String> result = new HashMap<>();
		if( null == queryString || queryString.isEmpty() ) {
			return result;
		}
		String[] params = queryString.split("&");		
		for( String param : params ) {
			if( null == param || param.isEmpty() ) {
				continue;
			}
			int delimIdx = param.indexOf("=");
			String name;
			String value;
			if( 0 > delimIdx ) {
				name = param;
				value = "";
			} else if (delimIdx > 0) {
				name = param.substring(0, delimIdx);
				value = param.substring(delimIdx + 1);
			} else { // == 0
				continue;
			}
			name = URLDecoder.decode(name, "UTF-8");
			value = URLDecoder.decode(value, "UTF-8");
			result.put(name, value);
		}
		return Collections.unmodifiableMap(result);
	}
	
}
