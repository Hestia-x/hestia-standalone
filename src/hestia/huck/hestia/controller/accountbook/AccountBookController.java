package huck.hestia.controller.accountbook;

import huck.hestia.HestiaController;
import huck.hestia.RequestPath;
import huck.hestia.VelocityRenderer;
import huck.hestia.db.HestiaDB;
import huck.simplehttp.HttpRequest;
import huck.simplehttp.HttpResponse;

import java.io.IOException;

public class AccountBookController implements HestiaController {
	public AccountBookController(HestiaDB<?,?> db, VelocityRenderer renderer) throws IOException {
	}
	
	@Override
	public HttpResponse controll(HttpRequest req, String matchPath) throws Exception {
		RequestPath path = new RequestPath(req.getRequestPath().substring(matchPath.length()));
		req.setAttribute("path", path);
		if( 0 == path.size() ) {
			return redirectTo(matchPath + "asset/");
		}
		notFound(req);
		return null;
	}
}

