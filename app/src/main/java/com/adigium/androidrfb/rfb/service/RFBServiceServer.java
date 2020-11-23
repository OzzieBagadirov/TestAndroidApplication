package com.adigium.androidrfb.rfb.service;

import android.util.Log;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.SSLServerSocketFactory;

import com.adigium.androidrfb.rfb.encoding.Encodings;
import com.adigium.androidrfb.rfb.screen.ScreenCapture;
import com.adigium.androidrfb.rfb.ssl.SSLUtil;

class RFBServiceServer implements Runnable {

	public final static int DEFAULT_PORT = 60000;

	private int port;
	
	private ServerSocket socket = null;
	
	private boolean running;
	
	private final List<ClientHandler> clientHandlers;
	
	private RFBConfig rfbConfig;
	

	public RFBServiceServer() {
		this(DEFAULT_PORT);
	}
	

	public RFBServiceServer(final int port) {

		this.port = port;
		
		this.running = false;
		
		this.clientHandlers = new ArrayList<>();
		
		this.rfbConfig = new RFBConfig();
	}

	public void setPassword(final String pwd) {
		
		this.rfbConfig.setPassword(pwd);
	}

	public void setPreferredEncodings(final int[] encodings) {
	
		this.rfbConfig.setPreferredEncodings(encodings);
	}

	public void enableSSL(final String keyFilePath, final String password) {
		
		final String keystoreType;
		
		if (keyFilePath.endsWith(".pfx") || keyFilePath.endsWith(".p12")) {
			
			keystoreType = SSLUtil.KEYSTORE_TYPE_PKCS12;
		}
		else {
			
			keystoreType = SSLUtil.KEYSTORE_TYPE_JKS;
		}
		
		try {
		
			final InputStream in = new FileInputStream(keyFilePath);
		
			final SSLServerSocketFactory factory = SSLUtil.newInstance(keystoreType, in, password);
			this.rfbConfig.setSSLServerSocketFactory(factory);
			
			Log.i("RFBService", "Default SSL cipher suite: " + Arrays.toString(factory.getDefaultCipherSuites()));
			Log.i("RFBService", "Supported SSL cipher suite: " + Arrays.toString(factory.getSupportedCipherSuites()));
		} catch (final Exception ex) {
			
			Log.e("RFBService","Unable to initialize SSL encryption layer. SSL is disabled.", ex);
		}
	}

	public void disableSSL() {
		
		this.rfbConfig.setSSLServerSocketFactory(null);
	}

	public boolean isRunning() {
		
		return this.running;
	}

	public void start(int screenWidth, int screenHeight) {

		ScreenCapture.screenHeight = screenHeight;
		ScreenCapture.screenWidth = screenWidth;

		final Thread thread = new Thread(this, this.toString());
		thread.start();		
	}


	public void terminate() {
	
		try {
		
			this.running = false;
			
			this.socket.close(); // Do not accept new connections.
			
			for (final ClientHandler handler : this.clientHandlers) {
				
				handler.terminate(); // terminate currently open sessions.
			}
			
			this.clientHandlers.clear();
		} catch (final IOException exception) {

			Log.e("RFBService","Unable to terminate RFB service socket.", exception);
		}
	}
	

	public List<ClientHandler> getClientHandlers() {
		
		return new ArrayList<>(this.clientHandlers);
	}

	public void connect(final String hostname, final int port) throws UnknownHostException, IOException {
		
		final Socket socket = new Socket(hostname, port);
		final ClientHandler clientHandler = new ClientHandler(socket, this.rfbConfig);
		
		final Thread clientThread = new Thread(clientHandler
				, String.format("%s-[%s:%d]", ClientHandler.class.getSimpleName(), hostname, port));
		clientThread.start();
	}


	public void run() {
		
		//
		// Prepare server socket, bind to TCP port (eg. 5900).
		//
		
		if (this.socket == null) {
			
			try {
			
				final SSLServerSocketFactory sslFactory = this.rfbConfig.getSSLServerSocketFactory();
				
				if (sslFactory != null) {
					
					//
					// Secure TCP communication with SSL layer.
					//
					
					this.socket = sslFactory.createServerSocket(this.port);
				}
				else {
				
					//
					// Use plain TCP communication if SSL is not defined or not available.
					//
					
					this.socket = new ServerSocket(this.port, 1, InetAddress.getLocalHost());
				}
				
				Log.i(
						"RFBService",
						String.format("RFB service (VNC server) started at TCP port '%s'.",
								this.socket.getInetAddress())
						);
			} catch (final IOException exception) {

				Log.e(
						"RFBService",
						String.format("Unable to open TCP port '%d'. RFB service terminated."
								, this.port)
						, exception
						);
				
				return;
			}
		}
		
		this.running = true;
		
		//
		// Start accepting client connections.
		//
		
		while (this.running) {
			
			try {
			
				//
				// Handle each client connection in separate thread, and 
				// store each client handler object into list. Later it 
				// will be used to terminate all client connections.
				//

				Log.d("RFBService", "Listening for connections");
				
				final Socket clientSocket = this.socket.accept();

				Log.d("RFBService", "New connection accepted " + clientSocket.getInetAddress());
				
				final ClientHandler handler = new ClientHandler(clientSocket, this.rfbConfig);
				
				final Thread clientThread = new Thread(handler, handler.toString());
				clientThread.start();
				
				this.clientHandlers.add(handler);
			} catch (final IOException exception) {
				
				if (this.running == true) {
					
					Log.e("RFBService", "Problem occured while waiting for client connection.", exception);
				}
			}
		}
		
		this.running = false;
	}
	
	@Override
	public String toString() {
		return String.format("%s-[:%d]", RFBService.class.getSimpleName(), this.port);
	}
}
