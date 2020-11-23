package com.adigium.androidrfb.rfb.service;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

class ClientCutText {
	
	public String text;
	

	public ClientCutText(final String text) {

		this.text = text;
	}

	public static ClientCutText read(final InputStream inputStream) throws IOException {
		
		final DataInputStream in = new DataInputStream(inputStream);
		
		in.readByte(); // 3 padding bytes.
		in.readByte();
		in.readByte();
		
		int len = in.readInt();
		byte[] buff = new byte[len];
		
		in.read(buff);
		
		return new ClientCutText(new String(buff));
	}
}
