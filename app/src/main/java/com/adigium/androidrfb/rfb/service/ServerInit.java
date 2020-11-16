package com.adigium.androidrfb.rfb.service;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

class ServerInit {

	/**
	 * Write server init. message.
	 * 
	 * @param outputStream				-	instance of {@link OutputStream} where to write, typically obtained from {@link Socket#getOutputStream()} method
	 * @param width						-	screen or region of screen width, in pixel
	 * @param height					-	screen or region of screen height, in pixel
	 * 
	 * @throws IOException	if connection is broken
	 */
	public static void send(final OutputStream outputStream, final short width, final short height) throws IOException {
		
		final DataOutputStream out = new DataOutputStream(outputStream);
		
		out.writeShort(width);
		out.writeShort(height);
		
		final SetPixelFormat pixelFormat = SetPixelFormat.default32bit(); // Default should be ok.
		SetPixelFormat.write(outputStream, pixelFormat);

		final String title = 
				String.format("%s\\%s [%s %s]"
						, InetAddress.getLocalHost().getHostName()
						, System.getProperty("user.name")
						, System.getProperty("os.name")
						, System.getProperty("os.arch")
						);
		
		out.writeInt(title.length());
		out.write(title.getBytes());			

		out.flush();
	}
	
}
