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
import javax.net.ssl.SSLSocketFactory;

import com.adigium.androidrfb.rfb.encoding.Encodings;
import com.adigium.androidrfb.rfb.screen.ScreenCapture;
import com.adigium.androidrfb.rfb.ssl.SSLUtil;

/**
 * RFB service is Java implementation of VNC server.
 * <p>
 * <b>RFB</b> stands for <i>remote frame buffer</i> protocol.
 * <p>
 * This is simple implementations, mainly to demonstrate implementation of protocol.
 * It is not efficient as other implementations, nor does it compete with them.
 *
 *
 */
public class RFBService implements Runnable {

    private String host;

    private int port;

    private Socket socket = null;

    private ClientHandler clientHandler;

    private final RFBConfig rfbConfig;

    /**
     * Create new instance with given TCP port value.
     *
     * @param port	-	TCP port on which to listen
     */
    public RFBService(final String host, final int port) {

        this.host = host;
        this.port = port;

        this.rfbConfig = new RFBConfig();
    }

    public RFBService setHost(final String host) {
        this.host = host;
        return this;
    }
    public String getHost() {
        return this.host;
    }

    public RFBService setPort(final int port) {
        this.port = port;
        return this;
    }
    public int getPort() {
        return this.port;
    }

    public RFBService setPassword(final String pwd) {
        this.rfbConfig.setPassword(pwd);
        return this;
    }

    public RFBService setPreferredEncodings(final int[] encodings) {
        this.rfbConfig.setPreferredEncodings(encodings);
        return this;
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

            final SSLSocketFactory factory = SSLUtil.newSocketInstance(keystoreType, in, password);
            this.rfbConfig.setSSLSocketFactory(factory);

            Log.i("RFBService", "Default SSL cipher suite: " + Arrays.toString(factory.getDefaultCipherSuites()));
            Log.i("RFBService", "Supported SSL cipher suite: " + Arrays.toString(factory.getSupportedCipherSuites()));
        } catch (final Exception ex) {

            Log.e("RFBService","Unable to initialize SSL encryption layer. SSL is disabled.", ex);
        }
    }

    /**
     * Disable SSL communication with VNC clients.
     * <p>
     * Note that this method should be invoked before TCP socket is already in listening mode,
     * eg. before {@link #start} ()} method, to take effect.
     */
    public void disableSSL() {
        this.rfbConfig.setSSLServerSocketFactory(null);
    }

    public boolean isRunning() {
        return clientHandler != null && this.clientHandler.isRunning();
    }

    public void start(int screenWidth, int screenHeight) {

        ScreenCapture.screenHeight = screenHeight;
        ScreenCapture.screenWidth = screenWidth;

        final Thread thread = new Thread(this, this.toString());
        thread.start();
    }

    public void terminate() {
        try {
            this.socket.close();
            this.clientHandler.terminate();
        } catch (final IOException exception) {
            Log.e("RFBService","Unable to terminate RFB service socket.", exception);
        }
    }

    public ClientHandler getClientHandler() {
        return this.clientHandler;
    }


    public void run() {
        if (this.socket == null) {
            try {
                final SSLSocketFactory sslFactory = this.rfbConfig.getSSLSocketFactory();

                this.socket = sslFactory != null ? sslFactory.createSocket(host, port) : new Socket(host, port);

                this.clientHandler = new ClientHandler(this.socket, this.rfbConfig);

                final Thread clientThread = new Thread(clientHandler, "Client");
                clientThread.start();

                Log.i("RFBService", String.format("RFB service (VNC c) started at TCP address '%s'.", this.socket.getInetAddress()));

            } catch (final IOException exception) {
                Log.e("RFBService", String.format("Unable to open TCP port '%d'. RFB service terminated.", this.port), exception);
            }
        }
    }

    @Override
    public String toString() {

        return String.format("%s-[:%d]", RFBService.class.getSimpleName(), this.port);
    }
}
