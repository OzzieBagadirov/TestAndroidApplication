package com.adigium.androidrfb.rfb.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class VNCAuth {

	final private String password;

	private byte[] challengeData;

	private byte[] challengeResponse;

	public VNCAuth(final String password) {
		
		this.password = password;
	}

	public void sendChallenge(final OutputStream out) throws IOException {
				
		final byte[] randomChallenge =  new byte[16];
		
		long value = System.currentTimeMillis();
		
		for (int i = 0 ; i < 16 ; i++) {
		
			randomChallenge[i] = (byte) (value % 256);
			value = value / 10;
			
			if (value <= 0) {
				
				value = Long.MAX_VALUE - 1;
			}
		}
		
		out.write(randomChallenge);
		out.flush();
		
		this.challengeData = randomChallenge;
	}

	public void readChallenge(final InputStream in) throws IOException {
				
		final byte[] challengeResponse = new byte[16];
		
		in.read(challengeResponse);

		this.challengeResponse = challengeResponse;
	}

	public boolean isValid() {
		byte[] expected = DESCipher.enc(password, challengeData);

		// Note that DES encryption might result in longer byte[] array. 
		// We compare only 16 bytes as per VNC auth standard.
		for (int i = 0 ; i < 16 ; i++) {
			if (this.challengeResponse[i] != expected[i]) {
				return false;
			}
		}
		return true;
	}
}
