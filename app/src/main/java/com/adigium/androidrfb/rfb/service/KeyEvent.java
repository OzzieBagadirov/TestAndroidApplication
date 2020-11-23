package com.adigium.androidrfb.rfb.service;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

class KeyEvent {

	public int key;
	
	public byte downFlag;

	public KeyEvent(final int key, final byte downFlag) {

		this.key = key;
		this.downFlag = downFlag;
	}

	public static KeyEvent read(final InputStream inputStream) throws IOException {
		
		final DataInputStream in = new DataInputStream(inputStream);
		
		byte downFlag = in.readByte();
		in.readShort(); // padding
		int key = in.readInt();
		
		return new KeyEvent(key, downFlag);
	}
}
