package com.adigium.androidrfb.rfb.service;

import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;

import com.adigium.androidrfb.rfb.encoding.Encodings;
import com.adigium.androidrfb.rfb.ssl.SSLUtil;

class RFBConfig {

	private String password;

	private int[] preferredEncodings;

	private SSLSocketFactory sslSocketFactory;
	private SSLServerSocketFactory sslServerSocketFactory;

	public String getPassword() {
		
		return password;
	}

	public void setPassword(String password) {
		
		this.password = password;
	}

	public int[] getPreferredEncodings() {
		
		return preferredEncodings;
	}

	public void setPreferredEncodings(int[] preferredEncodings) {
		
		this.preferredEncodings = preferredEncodings;
	}

	public void setSSLServerSocketFactory(final SSLServerSocketFactory sslServerSocketFactory) {
		
		this.sslServerSocketFactory = sslServerSocketFactory;
	}


	public void setSSLSocketFactory(final SSLSocketFactory sslSocketFactory) {

		this.sslSocketFactory = sslSocketFactory;
	}

	public SSLServerSocketFactory getSSLServerSocketFactory() {
		
		return this.sslServerSocketFactory;
	}

	public SSLSocketFactory getSSLSocketFactory() {

		return this.sslSocketFactory;
	}
}
