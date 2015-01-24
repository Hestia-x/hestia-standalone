package huck.hestia;

import huck.common.jdbc.DBConnectionManager;
import huck.hestia.controller.DefaultController;
import huck.hestia.controller.StaticResourceController;
import huck.hestia.controller.accountbook.ViewAssetController;
import huck.hestia.controller.accountbook.ViewCashflowController;
import huck.hestia.controller.accountbook.ViewController;
import huck.hestia.controller.accountbook.ViewSlipController;
import huck.hestia.db.HestiaDB;
import huck.hestia.db.memory.HestiaMemoryDB;
import huck.hestia.db.memory.LoaderMysql;
import huck.simplehttp.HttpException;
import huck.simplehttp.HttpProcessor;
import huck.simplehttp.HttpRequest;
import huck.simplehttp.HttpResponse;

import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.apache.log4j.Logger;

public class HestiaHttpProcessor implements HttpProcessor {
	private HashMap<String, HestiaController> controllerMap;
	
	private Logger logger() {
		return Logger.getLogger("hestia");
	}
	public HestiaHttpProcessor() throws Exception {
		logger().info("Load Database");
		Class.forName(org.gjt.mm.mysql.Driver.class.getName());
		String dbUrl = "jdbc:mysql://127.0.0.1:3306/account_book?characterEncoding=UTF-8";
		String dbUser = "root";
		String dbPassword = null;
		HestiaDB db = new HestiaMemoryDB(new LoaderMysql(new DBConnectionManager(dbUrl, dbUser, dbPassword)));
		
		int moneyScale = 2;

//		FileDataManager dataMgr = new FileDataManager(new File("test.data"));
//		HestiaDB db = new HestiaMemoryDB(dataMgr.getLoader());
		
		logger().info("Finish Loading");
		
		VelocityRenderer renderer = new VelocityRenderer(moneyScale);

		controllerMap = new HashMap<>();
		controllerMap.put("/", new DefaultController(db));
		controllerMap.put("/account_book/view/", new ViewController(db, renderer));
		controllerMap.put("/account_book/view/asset/", new ViewAssetController(db, renderer));
		controllerMap.put("/account_book/view/cashflow/", new ViewCashflowController(db, renderer));
		controllerMap.put("/account_book/view/slip/", new ViewSlipController(db, renderer));
		
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

	@Override
	public WritableByteChannel getBodyProcessor(HttpRequest req) {
		return null;
	}

}
