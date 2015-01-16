package huck.hestia;

import huck.simplehttp.HttpRequest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class HttpUtil {
	public static String getString(HttpRequest req, String name, boolean required) throws Exception {
		String value = req.getParameter(name);
		if( null != value ) {
			value = value.trim();
			if( 0 < value.length() ) {
				return value;
			}
		}
		if( required ) {
			throw new Exception("parameter required: " + name);
		} else {
			return null;
		}
	}
	
	public static Integer getInt(HttpRequest req, String name, boolean required) throws Exception {
		String value = getString(req, name, required);
		if( null != value ) {
			try {
				return Integer.parseInt(value);
			} catch( NumberFormatException ex ) {
				throw new Exception("invalid parameter: " + name);
			}			
		}
		if( required ) {
			throw new Exception("parameter required: " + name);
		} else {
			return null;
		}
	}
	
	public static LocalDate getLocalDate(HttpRequest req, String name, boolean required) throws Exception {
		String value = getString(req, name, required);
		if( null != value ) {
			try {
				LocalDate date = LocalDate.parse(value, DateTimeFormatter.BASIC_ISO_DATE);
				return date;
			} catch( DateTimeParseException ex ) {
				throw new Exception("invalid parameter: " + name);
			}
		}
		if( required ) {
			throw new Exception("parameter required: " + name);
		} else {
			return null;
		}
	}
	
	public static String createStickQueryString(HttpRequest req, String... names) {
		StringBuffer buf = new StringBuffer();
		for( String name : names ) {
			String value = req.getParameter(name);
			if( null == value ) continue;
			buf.append(name).append("=").append(value).append("&");
		}
		buf.deleteCharAt(buf.length()-1);
		return buf.toString();
	}
}
