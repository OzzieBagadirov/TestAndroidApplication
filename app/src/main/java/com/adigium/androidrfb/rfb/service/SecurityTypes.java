package com.adigium.androidrfb.rfb.service;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;


public class SecurityTypes {

	public final static int NONE = 1, VNC_AUTH = 2;

	public final int securityType;

	public SecurityTypes(int type) {
		
		this.securityType = type;
	}

	public static void send(final OutputStream out, final byte[] securityTypes) throws IOException {
		
		final int numberOfSecurityTypes = securityTypes.length;
		
		out.write(numberOfSecurityTypes);
		out.write(securityTypes);
		out.flush();
	}

	public static SecurityTypes read(final InputStream in) throws IOException {
				
		int securityType = in.read();

		return new SecurityTypes(securityType);
	}

	public static void sendSecurityResult(final OutputStream out, final String failureReason) throws IOException {
		
		byte[] ok = {0, 0, 0, 0};
		byte[] failure = {0, 0, 0, 1}; // Little-endian or Big-endian byte order ?
		
		if (failureReason == null) {
			
			out.write(ok);
		}
		else {
			
			final DataOutputStream dOut = new DataOutputStream(out);
			
			dOut.write(failure);
			dOut.writeInt(failureReason.length()); // Big-endian (network) byte order if DataOutputStream is used.
			dOut.write(failureReason.getBytes());
		}
		
		out.flush();
	}
	
}
