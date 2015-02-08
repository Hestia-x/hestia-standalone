package huck.hestia.controller;

import huck.hestia.HestiaController;
import huck.simplehttp.HttpException;
import huck.simplehttp.HttpRequest;
import huck.simplehttp.HttpResponse;

import java.net.URL;
import java.nio.channels.Channels;
import java.util.HashMap;

public class StaticResourceController implements HestiaController {
	private HashMap<String, String> mimeMap = new HashMap<>();
	public StaticResourceController() {
		mimeMap.put("css", "text/css");
		mimeMap.put("js", "text/javascript");
		mimeMap.put("png", "image/png");
	}
	
	@Override
	public HttpResponse controll(HttpRequest req, String matchPath) throws HttpException, Exception {
		URL resourceUrl = ClassLoader.getSystemResource("static_resource" + req.getRequestPath());
		if( null == resourceUrl ) {
			return null;
		}
		String path = req.getRequestPath();
		int lastSlashIdx = path.lastIndexOf('/');
		int extIdx = path.lastIndexOf('.');
		if( extIdx < lastSlashIdx ) {
			extIdx = -1;
		}
		String ext = 0 > extIdx ? "" : path.substring(extIdx+1);
		String contentType = mimeMap.get(ext);
		if( null == contentType ) {
			contentType = "application/octet-stream";
		}
		if( contentType.startsWith("text") ) {
			contentType += "; charset=utf8";
		}
		
		HttpResponse res = new HttpResponse(HttpResponse.Status.OK, ()->Channels.newChannel(resourceUrl.openStream()));
		res.setHeader("Content-Type", contentType);
		return res;
	}
}
