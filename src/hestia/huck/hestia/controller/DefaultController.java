package huck.hestia.controller;

import java.io.IOException;

import huck.hestia.HestiaController;
import huck.hestia.VelocityRenderer;
import huck.hestia.db.HestiaDB;
import huck.simplehttp.HttpException;
import huck.simplehttp.HttpRequest;
import huck.simplehttp.HttpResponse;

public class DefaultController implements HestiaController {
	private VelocityRenderer renderer;
	public DefaultController(HestiaDB db, VelocityRenderer renderer) throws IOException {
		this.renderer = renderer;
	}
	
	@Override
	public HttpResponse controll(HttpRequest req) throws HttpException, Exception {
		return renderer.render(req.getRequestPath(), req, null);
	}
}
