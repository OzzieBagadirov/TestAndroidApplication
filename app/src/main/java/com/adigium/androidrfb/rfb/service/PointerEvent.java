package com.adigium.androidrfb.rfb.service;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

class PointerEvent {
	public byte buttonMask;

	public short xPos, yPos;

	public PointerEvent(final short xPos, final short yPos, final byte buttonMask) {

		this.xPos = xPos;
		this.yPos = yPos;
		this.buttonMask = buttonMask;
	}

	public static PointerEvent read(final InputStream inputStream) throws IOException {
		
		final DataInputStream in = new DataInputStream(inputStream);
		
		byte buttonMask = in.readByte();
		short x = in.readShort();
		short y = in.readShort();
		
		return new PointerEvent(x, y, buttonMask);
	}
}
