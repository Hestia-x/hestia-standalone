package huck.hestia;

import huck.nugget.config.Config;
import huck.nugget.config.ConfigParser;
import huck.simplehttp.HttpServer;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

public class Hestia {
	private static Logger logger() {
		return Logger.getLogger("hestia");
	}
	
	public static void main(String... args) throws Exception {
		logger().info("Load Config");
		StringBuffer configBuf = new StringBuffer();
		try( InputStreamReader configR = new InputStreamReader(new FileInputStream("config.txt"), "UTF-8") ) {
			char[] tmp = new char[1024];
			int readLen;
			while(0 < (readLen=configR.read(tmp)) ) {
				configBuf.append(tmp, 0, readLen);
			}
		}
		Config config = ConfigParser.parseConfig(configBuf);
		String portStr = config.getValue("port");
		String dbFileName = config.getValue("data_file");
		String moneyScaleStr = config.getValue("money_scale");
		
		if( null == dbFileName ) {
			throw new Exception("need data_file");
		}
		if( null == moneyScaleStr ) {
			throw new Exception("need money_scale");
		}
		if( null == portStr ) {
			throw new Exception("need port");
		}
		
		int moneyScale;
		try {
			moneyScale = Integer.parseInt(moneyScaleStr);
		} catch( NumberFormatException ex ) {
			throw new Exception("money_scale must be integer: " + moneyScaleStr);
		}
		if( 1 > moneyScale ) {
			throw new Exception("invalid money_scale: " + moneyScale);
		}
		int moneyScale10 = 0;
		while( 1 < moneyScale ) {
			if( 0 != moneyScale % 10 ) {
				throw new Exception("invalid money_scale: " + moneyScale);
			}
			moneyScale = moneyScale / 10;
			moneyScale10 += 1;
		}
		
		int port;
		try {
			port = Integer.parseInt(portStr);
		} catch( NumberFormatException ex ) {
			throw new Exception("port must be integer: " + portStr);
		}
		
		logger().info("[Load] port: " + port);
		logger().info("[Load] data_file: " + dbFileName);
		logger().info("[Load] money_scale: " + moneyScaleStr);
		
		AtomicBoolean stopSignal = new AtomicBoolean(false);
		HestiaHttpProcessor processor = new HestiaHttpProcessor(dbFileName, moneyScale10);
		HttpServer server = new HttpServer(processor, 4, new InetSocketAddress(port));
		logger().info("http://" + Inet4Address.getLocalHost().getHostAddress() + ":" + port + "/");
		server.runServer(stopSignal);
	}
}
