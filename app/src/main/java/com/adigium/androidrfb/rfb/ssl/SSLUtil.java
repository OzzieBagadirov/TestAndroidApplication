package com.adigium.androidrfb.rfb.ssl;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

public class SSLUtil {

	final public static String KEYSTORE_TYPE_JKS = "JKS", KEYSTORE_TYPE_PKCS12 = "PKCS12";

	public static SSLServerSocketFactory newInstance(final String keystoreType
			, final InputStream keystoreFile, final String password) throws KeyManagementException, KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException, CertificateException, IOException {
		
		final KeyStore keyStore = KeyStore.getInstance(keystoreType);
		keyStore.load(keystoreFile, password.toCharArray());

		final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		keyManagerFactory.init(keyStore, password.toCharArray());

		final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm()); 
		trustManagerFactory.init(keyStore);

		final SSLContext sslContext = SSLContext.getInstance("TLS"); 
		final TrustManager[] trustManagers = trustManagerFactory.getTrustManagers(); 
		sslContext.init(keyManagerFactory.getKeyManagers(), trustManagers, null);


		return sslContext.getServerSocketFactory();
	}

	public static SSLSocketFactory newSocketInstance(final String keystoreType
			, final InputStream keystoreFile, final String password) throws KeyManagementException, KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException, CertificateException, IOException {

		final KeyStore keyStore = KeyStore.getInstance(keystoreType);
		keyStore.load(keystoreFile, password.toCharArray());

		final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		keyManagerFactory.init(keyStore, password.toCharArray());

		final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		trustManagerFactory.init(keyStore);

		final SSLContext sslContext = SSLContext.getInstance("TLS");
		final TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
		sslContext.init(keyManagerFactory.getKeyManagers(), trustManagers, null);

		return sslContext.getSocketFactory();
	}
}
