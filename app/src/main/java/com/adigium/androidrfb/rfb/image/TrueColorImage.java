package com.adigium.androidrfb.rfb.image;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import com.adigium.androidrfb.rfb.encoding.RawEncoder;
import com.adigium.androidrfb.rfb.screen.ScreenCapture;

public class TrueColorImage {

	public int[] raw;
	
	public final int width, height;

	public TrueColorImage(final int[] raw, final int width, final int height) {
	
		this.raw = raw;
		
		this.width = width;
		this.height = height;
	}

		
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + height;
		result = prime * result + Arrays.hashCode(raw);
		result = prime * result + width;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TrueColorImage other = (TrueColorImage) obj;
		if (height != other.height)
			return false;
		if (!Arrays.equals(raw, other.raw))
			return false;
		if (width != other.width)
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TrueColorImage [width=");
		builder.append(width);
		builder.append(", height=");
		builder.append(height);
		builder.append("]");
		return builder.toString();
	}

//	public static BufferedImage toBufferedImage(final TrueColorImage trueColorImage) {
//
//		if (trueColorImage == null) {
//
//			return null;
//		}
//
//		final int width = trueColorImage.width,
//				height = trueColorImage.height;
//
//		final BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
//		final int[] array = ((DataBufferInt) bufferedImage.getRaster().getDataBuffer()).getData();
//		System.arraycopy(trueColorImage.raw, 0, array, 0, array.length);
//
//		return bufferedImage;
//	}

	public TrueColorImage toBGR() {

		int[] src = this.raw;

		int[] res = new int[src.length];

		for (int i = 0 ; i < src.length ; i++) {

			int pixel = src[i];

			// Convert from ARGB into BGR.
			byte b = ((byte) (pixel & 0xFF)); // Blue component.
			pixel = pixel >> 8;

			byte g = ((byte) (pixel & 0xFF)); // Green component.
			pixel = pixel >> 8;

			byte r = ((byte) (pixel & 0xFF)); // Red component.

			res[i] = ((r & 0xFF)) | ((g & 0xFF) << 8) | ((b & 0xFF) << 16);
		}

		this.raw = res;
		return this;
	}

	public static byte[] toBGR(final TrueColorImage trueColorImage) {

		int[] src = trueColorImage.raw;

		final byte[] raw = new byte[src.length * 3];

		final ByteBuffer dstBuffer = ByteBuffer.wrap(raw);

		for (int i = 0 ; i < src.length ; i++) {

			int pixel = src[i];

			// Convert from ARGB into BGR.
			dstBuffer.put( (byte) (pixel & 0xFF)); // Blue component.
			pixel = pixel >> 8;

			dstBuffer.put( (byte) (pixel & 0xFF)); // Green component.
			pixel = pixel >> 8;

			dstBuffer.put( (byte) (pixel & 0xFF)); // Red component.
			pixel = pixel >> 8;
		}

		return raw;
	}
}
