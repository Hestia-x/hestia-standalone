package huck.hestia;

import huck.simplehttp.HttpException;
import huck.simplehttp.HttpRequest;
import huck.simplehttp.HttpResponse;

public interface HestiaController {
	public HttpResponse controll(HttpRequest req, String matchPath) throws HttpException, Exception;
	
	default HttpResponse redirectTo(String path) {
		HttpResponse res = new HttpResponse(HttpResponse.Status.MOVED_TEMPORARILY);
		res.setHeader("Location", path);
		return res;
	}
	
	default void notFound(HttpRequest req) throws HttpException {
		throw new HttpException(HttpResponse.Status.NOT_FOUND, "Not Found: " + req.getRequestPath());
	}
}
