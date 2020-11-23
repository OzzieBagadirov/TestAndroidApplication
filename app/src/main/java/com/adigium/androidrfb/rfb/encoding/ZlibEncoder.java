package com.adigium.androidrfb.rfb.encoding;

import java.nio.ByteBuffer;
import java.util.zip.Deflater;

import com.adigium.androidrfb.rfb.service.SetPixelFormat;


public class ZlibEncoder implements EncodingInterface {

	final private Deflater deflater;

	final private RawEncoder rawEncoder;

	/**
	 * Create new zlib encoder.
	 * 
	 */
	public ZlibEncoder() {

		this.deflater = new Deflater();

		this.rawEncoder = new RawEncoder();
	}

	/**
	 * Zlib will just compress raw encoded image.
	 */
	@Override
	public byte[] encode(final int[] image, final int width, final int height, final SetPixelFormat pixelFormat) {

		final byte[] raw = rawEncoder.encode(image, width, height, pixelFormat);
		deflater.setInput(raw);
		
		final byte[] buff = new byte[2 * raw.length];

		// Seems that we need to invoke deflate() method with FULL_FLUSH arg.
		int count = deflater.deflate(buff, 0, buff.length, Deflater.FULL_FLUSH);

		final int length = 4 + count;
		final byte[] result = new byte[length];

		final ByteBuffer buffer = ByteBuffer.wrap(result);
		buffer.putInt(count);
		buffer.put(buff, 0, count);

		return result;
	}

	@Override
	public int getType() {

		return Encodings.ZLIB;
	}

}
