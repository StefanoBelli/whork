package logic.net;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;

import org.baswell.niossl.SSLServerSocketChannel;

public final class SSLSocketServer extends SocketServer {
	public SSLSocketServer(String listenAddress, int listenPort, ReceiveEvent receiveEvent) 
			throws IOException, NoSuchAlgorithmException {
		super(listenAddress, listenPort, receiveEvent);
		
		SSLContext sslContext = SSLContext.getInstance("TLS");
		ThreadPoolExecutor sslThreadPool = new ThreadPoolExecutor(250, 2000, 25, TimeUnit.SECONDS,
			new LinkedBlockingQueue<>());
		
		serverSocket = new SSLServerSocketChannel(serverSocket, sslContext, sslThreadPool, null);
	}
}