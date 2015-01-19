package huck.hestia.controller;

import huck.hestia.HestiaController;
import huck.hestia.db.HestiaDB;
import huck.simplehttp.HttpException;
import huck.simplehttp.HttpRequest;
import huck.simplehttp.HttpResponse;

import java.io.IOException;

public class DefaultController implements HestiaController {
	public DefaultController(HestiaDB db) throws IOException {
	}
	
	@Override
	public HttpResponse controll(HttpRequest req, String matchPath) throws HttpException, Exception {
		if( "/".equals(req.getRequestPath()) ) {
			return this.redirectTo("/account_book/view/asset/");
		} else {
			notFound(req);
			return null;
		}
	}
}
