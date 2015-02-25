package huck.hestia.controller.system;

import huck.hestia.HestiaController;
import huck.hestia.RequestPath;
import huck.hestia.VelocityRenderer;
import huck.hestia.VelocityRenderer.ActionFunction;
import huck.hestia.db.memory.FileDataManager;
import huck.hestia.db.memory.HestiaMemoryDB;
import huck.simplehttp.HttpRequest;
import huck.simplehttp.HttpResponse;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class SystemController implements HestiaController {
	private HestiaMemoryDB db;
	private File dbFile;
	private VelocityRenderer renderer;
	
	public SystemController(HestiaMemoryDB db, File dbFile, VelocityRenderer renderer) throws IOException {
		this.db = db;
		this.dbFile = dbFile;
		this.renderer = renderer;
	}
	
	@Override
	public HttpResponse controll(HttpRequest req, String matchPath) throws Exception {
		RequestPath path = new RequestPath(req.getRequestPath().substring(matchPath.length()));
		req.setAttribute("path", path);
		String loadedDataName = db.loadedDataName();
		req.setAttribute("loadedDataName", loadedDataName);

		ActionFunction actionFunction = null;
		switch( path.get(0) ) {
		case "reload/": actionFunction = this::reload; break;
		case "save/": actionFunction = this::save; break;
		default: notFound(req); break;
		}
		return renderer.render(req, actionFunction);
	}
	
	private String reload(HttpRequest req, HashMap<String, Object> valueMap) throws Exception {
		if( !db.isModified() || "true".equals(req.getParameter("lostchange")) ) {
			valueMap.put("filename", dbFile.getName());
			try {
				db.load(FileDataManager.getLoader(dbFile));
				String loadedDataName = db.loadedDataName();
				req.setAttribute("loadedDataName", loadedDataName);
				return "/system/load_success.html";
			} catch( Exception ex ) {
				logger().error(ex, ex);
				return "/system/load_fail.html";
			}
		} else {
			return "/system/load_check_lostchange.html";
		}
	}
	
	private String save(HttpRequest req, HashMap<String, Object> valueMap) throws Exception {
		valueMap.put("filename", db.loadedDataName());
		db.save(FileDataManager.getDumper(dbFile));
		return "/system/save_success.html";
	}
}

