package huck.hestia.controller.accountbook;

import huck.hestia.HestiaController;
import huck.hestia.RequestPath;
import huck.hestia.VelocityRenderer;
import huck.hestia.db.HestiaDB;
import huck.hestia.db.Slip;
import huck.simplehttp.HttpRequest;
import huck.simplehttp.HttpResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class SlipUpdateController implements HestiaController {
	private HestiaDB<?,?> db;
	private VelocityRenderer renderer;
	
	public SlipUpdateController(HestiaDB<?,?> db, VelocityRenderer renderer) throws IOException {
		this.db = db;
		this.renderer = renderer;
	}
	
	@Override
	public HttpResponse controll(HttpRequest req, String matchPath) throws Exception {
		String loadedDataName = db.loadedDataName();
		if( null == loadedDataName ) {
			return redirectTo("/system/load/");
		} else {
			req.setAttribute("loadedDataName", loadedDataName);
		}
		RequestPath path = new RequestPath(req.getRequestPath().substring(matchPath.length()));
		req.setAttribute("path", path);
		
		HashMap<String, Object> originData = null;
		HashMap<String, Object> editingData = null;
		HashMap<String, String> errorData = null;
		
		switch( path.size() ) {
		case 0:
			break;
		case 1:
			int slipId;
			try {
				slipId = Integer.parseInt(path.get(0));
			} catch( NumberFormatException ex ) {
				notFound(req);
				return null;	
			}
			List<Slip> slipList = db.retrieveSlipList(a->a.id()==slipId);
			if( slipList.isEmpty() ) {
				notFound(req);
				return null;
			}
			originData = createFormDataFromSlip(slipList.get(0));
		default:
			notFound(req);
			return null;
		}

		if( "post".equals(req.getMethod().toLowerCase())) {
			editingData = new HashMap<>();
			errorData = new HashMap<>();
			Slip slip = processSave(req, originData, editingData, errorData);
			if( null != slip ) {
				return redirectTo("/account_book/slip/"+slip.id());
			}
			if( errorData.isEmpty() ) {
				errorData = null;
			}
			if( editingData.isEmpty() ) {
				editingData = null;
			}
		}
		HashMap<String, Object> valueMap = new HashMap<String, Object>();
		return renderer.render("/account_book/slip_form.html", req, valueMap);
	}
	
	private HashMap<String, Object> createFormDataFromSlip(Slip slip) {
		return null;
	}

	private Slip processSave(HttpRequest req, HashMap<String, Object> originData, HashMap<String, Object> editingData, HashMap<String, String> errorData) throws Exception {
		return null;
	}
}

