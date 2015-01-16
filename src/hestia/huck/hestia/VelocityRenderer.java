package huck.hestia;

import huck.simplehttp.HttpRequest;
import huck.simplehttp.HttpResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ResourceNotFoundException;

public class VelocityRenderer {
	private VelocityEngine ve;
	public VelocityRenderer() throws IOException {
		ve = new VelocityEngine();
		
		ve.addProperty("runtime.log.logsystem.class", org.apache.velocity.runtime.log.Log4JLogChute.class.getName());
		ve.addProperty("runtime.log.logsystem.log4j.logger", "velocity");
		
		ve.setProperty("input.encoding", "UTF-8");
		ve.setProperty("output.encoding", "UTF-8");
		
		ve.setProperty("resource.loader", "class");
		ve.setProperty("class.resource.loader.class", org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader.class.getName());
		
		ve.setProperty("resource.manager.logwhenfound", "true");
		
		ve.setProperty("velocimacro.library.autoreload", "true");
		ve.setProperty("velocimacro.library", "");
		
		ve.setProperty("directive.foreach.counter.name", "velocityCount");
		ve.setProperty("directive.foreach.iterator.name", "velocityHasNext");
		ve.setProperty("directive.foreach.counter.initial.value", "1");
		
		ve.init();
	}
	
	public boolean hasTemplate(String path) {
		try {
			ve.getTemplate("/template_resource" + path);
			return true;
		} catch( ResourceNotFoundException ex ) {
			return false;
		}
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
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Writer wr = new OutputStreamWriter(out, "UTF-8");
		page.merge(wrapContext, wr);
		wr.flush();
		
		HttpResponse res = new HttpResponse(HttpResponse.Status.OK, out.toByteArray());
		res.setHeader("Content-Type", "text/html; charset=UTF-8");
		return res;
	}
}
