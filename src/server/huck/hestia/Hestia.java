package huck.hestia;

import huck.simplehttp.HttpServer;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;

public class Hestia {
	public static void main(String... args) throws Exception {
		AtomicBoolean stopSignal = new AtomicBoolean(false);
		int port = 7077;
		HestiaHttpProcessor processor = new HestiaHttpProcessor();
		HttpServer server = new HttpServer(processor, 4, new InetSocketAddress(port));
		server.runServer(stopSignal);
	}
}
