package huck.hestia;

import huck.hestia.controller.DefaultController;
import huck.hestia.controller.StaticResourceController;
import huck.hestia.controller.accountbook.AccountBookController;
import huck.hestia.controller.accountbook.AssetController;
import huck.hestia.controller.accountbook.FlowController;
import huck.hestia.controller.accountbook.SlipController;
import huck.hestia.controller.accountbook.SlipUpdateController;
import huck.hestia.controller.system.SystemController;
import huck.hestia.db.memory.FileDataManager;
import huck.hestia.db.memory.HestiaMemoryDB;
import huck.simplehttp.HttpException;
import huck.simplehttp.HttpProcessor;
import huck.simplehttp.HttpRequest;
import huck.simplehttp.HttpResponse;

import java.io.File;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class HestiaHttpProcessor implements HttpProcessor {
	private HashMap<String, HestiaController> controllerMap;
	
	private Logger logger() {
		return Logger.getLogger("hestia");
	}
	public HestiaHttpProcessor(String dbFileName, int moneyScale) throws Exception {
		logger().info("[Load] Data");
		HestiaMemoryDB db = new HestiaMemoryDB();
		File dbFile = new File(dbFileName);
		db.load(FileDataManager.getLoader(dbFile));
		logger().info("[Load] Finish Loading");
		
		VelocityRenderer renderer = new VelocityRenderer(moneyScale);

		controllerMap = new HashMap<>();
		controllerMap.put("/", new DefaultController(db));
		
		controllerMap.put("/account_book/", new AccountBookController(db, renderer));
		controllerMap.put("/account_book/asset/", new AssetController(db, renderer));
		controllerMap.put("/account_book/flow/", new FlowController(db, renderer));
		controllerMap.put("/account_book/slip/", new SlipController(db, renderer));
		controllerMap.put("/account_book/slipform/", new SlipUpdateController(db, renderer));
		
		controllerMap.put("/system/", new SystemController(db, dbFile, renderer));
		
		controllerMap.put("/css/", new StaticResourceController());
		controllerMap.put("/fonts/", new StaticResourceController());
		controllerMap.put("/js/", new StaticResourceController());
		
		logger().info("Hestia is ready.");
		logger().info("Welcome to Hestia!");
	}
	
	
	@Override
	public HttpResponse process(HttpRequest req) throws HttpException, Exception {
		String path = req.getRequestPath();
		if( path.contains("/../") ) {
			throw new HttpException(HttpResponse.Status.NOT_FOUND, "NOT FOUND : " + path);
		}
		
		String[] pathElements = path.split("\\/");
		ArrayList<String> lookupPathList = new ArrayList<>();
		String nPath = "";
		for( String pathElement : pathElements ) {
			nPath += pathElement + "/";
			lookupPathList.add(nPath);
		}
		if( !path.endsWith("/") ) {
			lookupPathList.remove(lookupPathList.size()-1);
			lookupPathList.add(path);
		}
		Collections.reverse(lookupPathList);
		if( lookupPathList.isEmpty() ) {
			lookupPathList.add("/");
		}
		
		for( String lookupPath : lookupPathList ) {
			HestiaController controller = controllerMap.get(lookupPath);
			if( null != controller ) {
				return controller.controll(req, lookupPath);
			}
		}
		throw new HttpException(HttpResponse.Status.NOT_FOUND, "NOT FOUND: " + path);
	}
	
	private static class PostFormProcessor implements WritableByteChannel {
		private HttpRequest req;
		private boolean open;
		private	byte[] body;
		private int position;
		
		public PostFormProcessor(HttpRequest req) {
			this.req = req;
			this.open = true;
			this.body = new byte[req.getContentLength()];
			this.position = 0;
		}
		@Override
		public void close() throws IOException {
			if( open ) {
				open = false;
				Map<String, String> paramMap = HttpRequest.parseQueryString(new String(body, 0, position, "UTF-8"));
				req.setAttribute("bodyParam", paramMap);
			}
		}
		@Override
		public boolean isOpen() {
			return open;
		}
		@Override
		public int write(ByteBuffer src) throws IOException {
			if( open ) {
				if( position == body.length ) {
					throw new BufferOverflowException();
				}
				int len = Math.min(body.length-position, src.remaining());
				src.get(body, position, len);
				position += len;
				return len;
			}
			throw new IOException("closed");
		}
	}

	@Override
	public WritableByteChannel getBodyProcessor(HttpRequest req) {
		if( "POST".equalsIgnoreCase(req.getMethod()) && "application/x-www-form-urlencoded".equalsIgnoreCase(req.getHeader("Content-Type"))) {
			return new PostFormProcessor(req);
		}
		return null;
	}

}
