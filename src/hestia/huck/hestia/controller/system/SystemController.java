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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

public class SystemController implements HestiaController {
	private HestiaMemoryDB db;
	private File dataDir;
	private VelocityRenderer renderer;
	
	public SystemController(HestiaMemoryDB db, File dataDir, VelocityRenderer renderer) throws IOException {
		this.db = db;
		this.dataDir = dataDir;
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
		case "load/": actionFunction = this::load; break;
		default: notFound(req); break;
		}
		return renderer.render(req, actionFunction);
	}
	
	private String load(HttpRequest req, HashMap<String, Object> valueMap) throws Exception {
		RequestPath path = (RequestPath)req.getAttribute("path");
		if( 1 == path.size() ) {
			ArrayList<HashMap<String, String>> fileList = new ArrayList<>();
			File[] children = dataDir.listFiles();
			Arrays.sort(children, Comparator.comparingLong((File a)->a.lastModified()).reversed());
			for( File f : dataDir.listFiles() ) {
				if( f.isFile() ) {
					HashMap<String, String> data = new HashMap<>();
					data.put("name", f.getName());
					LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(f.lastModified()), ZoneId.of("GMT"));
					data.put("lastModified", dateTime.format(DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss")));
					long size = f.length();
					if( size < 10240 ) {
						data.put("size", size + " bytes");	
					} else if( size < 1024*1024*10 ) {
						data.put("size", (size/1024) + " KB");
					} else {
						data.put("size", (size/1024/1024) + " MB");
					}
					fileList.add(data);
				}
			}
			valueMap.put("fileList", fileList);
			return "/system/load_list.html";
		} else if( 2 == path.size() ) {
			if( !db.isModified() || "true".equals(req.getParameter("lostchange")) ) {
				String filename = path.get(1);
				valueMap.put("filename", filename);
				File file = new File(dataDir, filename);
				try {
					db.load(FileDataManager.getLoader(file));
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
		notFound(req);
		return null;
	}
}

