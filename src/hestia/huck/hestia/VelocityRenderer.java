package huck.hestia;

import huck.simplehttp.HttpRequest;
import huck.simplehttp.HttpResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ResourceNotFoundException;

public class VelocityRenderer {
	private VelocityEngine ve;
	private MoneyFormatter money;
	
	public VelocityRenderer(int moneyScale) throws IOException {
		ve = new VelocityEngine();
		
		ve.addProperty("runtime.log.logsystem.class", org.apache.velocity.runtime.log.Log4JLogChute.class.getName());
		ve.addProperty("runtime.log.logsystem.log4j.logger", "velocity");
		
		ve.setProperty("input.encoding", "UTF-8");
		ve.setProperty("output.encoding", "UTF-8");
		
		ve.setProperty("resource.loader", "class");
		ve.setProperty("class.resource.loader.class", org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader.class.getName());
		
		ve.setProperty("resource.manager.logwhenfound", "true");
		
		ve.setProperty("velocimacro.library.autoreload", "true");
		ve.setProperty("velocimacro.library", "/template_resource/tiles.vm");
		
		ve.setProperty("directive.foreach.counter.name", "velocityCount");
		ve.setProperty("directive.foreach.iterator.name", "velocityHasNext");
		ve.setProperty("directive.foreach.counter.initial.value", "1");
		
		ve.init();
		
		this.money = new MoneyFormatter(moneyScale);
	}
	
	public boolean hasTemplate(String path) {
		try {
			ve.getTemplate("/template_resource" + path);
			return true;
		} catch( ResourceNotFoundException ex ) {
			return false;
		}
	}
	
	@FunctionalInterface
	public interface ActionFunction {
		String apply(HttpRequest req, HashMap<String, Object> valueMap) throws Exception;
	}
	
	public HttpResponse render(HttpRequest req, ActionFunction actionFunction) throws Exception {
		HashMap<String, Object> valueMap = new HashMap<String, Object>();
		String templatePath = actionFunction.apply(req, valueMap);
		return render(templatePath, req, valueMap);
	}
	
	public HttpResponse render(String path, HttpRequest req, HashMap<String, Object> valueMap) throws Exception {
		Template page;
		try {
			page = ve.getTemplate("/template_resource" + path);
		} catch( ResourceNotFoundException ex ) {
			HttpResponse res = new HttpResponse(HttpResponse.Status.NOT_FOUND, ("No Template: " + path).getBytes("UTF-8"));
			res.setHeader("Content-Type", "text/plain; charset=utf-8");
			return res;
		}

		VelocityContext wrapContext = new VelocityContext(valueMap);
		wrapContext.put("__req", req);
		wrapContext.put("__money", money);
		wrapContext.put("__tool", new Tools());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Writer wr = new OutputStreamWriter(out, "UTF-8");
		page.merge(wrapContext, wr);
		wr.flush();
		
		HttpResponse res = new HttpResponse(HttpResponse.Status.OK, out.toByteArray());
		res.setHeader("Content-Type", "text/html; charset=UTF-8");
		return res;
	}
	
	public static class Tools {
		public String dateFormat(LocalDateTime dateTime, String pattern) {
			return dateTime.format(DateTimeFormatter.ofPattern(pattern));
		}
	}
	public static class MoneyFormatter {
		private int scale;
		public MoneyFormatter(int moneyScale) {
			this.scale = moneyScale;
		}
		public String f(Integer value) {
			if( null == value ) {
				return f((Long)null);
			} else {
				return f((long)(int)value);
			}
		}
		public String f(Long value) {
			if( null == value ) {
				return null;
			}
			boolean minus = false;
			if( 0 > value ) {
				value = 0 - value;
				minus = true;
			}
			StringBuffer result = new StringBuffer();
			int point = scale;
			int comma = point+3;
			while( value > 0 || point >= 0 ) {
				if( 0 == point ) {
					result.append('.');
				}
				if( 0 == comma ) {
					result.append(',');
					comma = 3;
				}
				result.append(value % 10);
				value /= 10;
				point--;
				comma--;
			}
			if( minus ) {
				result.append('-');
			}
			return result.reverse().toString();
		}
		public String r(Integer value) {
			if( null == value ) {
				return r((Long)null);
			} else {
				return r((long)(int)value);
			}
		}
		public String r(Long value) {
			return null==value?null:f(0-value);
		}
	}
}
