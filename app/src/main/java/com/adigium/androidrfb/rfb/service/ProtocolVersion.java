package com.adigium.androidrfb.rfb.service;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;

class ProtocolVersion {

	public static final String ver = "RFB 003.008\n";
	
	//Should be 3.3, 3.7 or 3.8.

	public int major, minor;

	public ProtocolVersion(int major, int minor) {
		
		this.major = major;
		this.minor = minor;
	}
	

	public static void sendProtocolVersion(final OutputStream out) throws IOException {
		
		out.write(ver.getBytes());
		out.flush();
	}
	

	public static ProtocolVersion readProtocolVersion(final InputStream in) throws IOException {
		
		byte[] buff = new byte[12];
		
		in.read(buff);

		Log.d("ProtocolVersion", Arrays.toString(buff));

		if (buff[0] == 'R' && buff[1] == 'F' && buff[2] == 'B' && buff[3] == ' '
			&& buff[4] == '0' && buff[5] == '0' && buff[6] == '3' && buff[7] == '.'
			&& buff[8] == '0' && buff[9] == '0' && (buff[10] >= '3' || buff[10] <= '8') && buff[11] == '\n'
				) {
			
			return new ProtocolVersion(3, buff[10] - '0');
		}
		
		throw new IOException("Unsupported RFB version value: " + Arrays.toString(buff));
	}
	
	@Override
	public String toString() {
		return String.format("%d.%d", this.major, this.minor);
	}
}
