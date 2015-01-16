package huck.hestia;

import huck.simplehttp.HttpException;
import huck.simplehttp.HttpRequest;
import huck.simplehttp.HttpResponse;

public interface HestiaController {
	public HttpResponse controll(HttpRequest req) throws HttpException, Exception;
	
	default void redirectTo(HttpResponse res, String path) {
		res.setStatus(HttpResponse.Status.MOVED_PERMANENTLY);
		res.setHeader("Location", path);
	}
	
	default void notFound() {
		
	}
}
