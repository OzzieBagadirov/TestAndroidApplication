package com.adigium.androidrfb.rfb.service;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

class FramebufferUpdateRequest {

	public byte incremental;

	public short xPosition, yPosition, width, height;

	public FramebufferUpdateRequest(final byte incremental
			, final short xPosition, final short yPosition
			, final short width, final short height
			) {

		this.incremental = incremental;
		this.xPosition = xPosition;
		this.yPosition = yPosition;
		this.width = width;
		this.height = height;		
	}

	public static FramebufferUpdateRequest read(final InputStream inputStream) throws IOException {
		
		final DataInputStream in = new DataInputStream(inputStream);
		
		byte incremental = in.readByte();
		short xPosition = in.readShort();
		short yPosition = in.readShort();
		short width = in.readShort();
		short height = in.readShort();
		
		return new FramebufferUpdateRequest(incremental, xPosition, yPosition, width, height);
	}
}
