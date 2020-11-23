package com.adigium.androidrfb.rfb.tight;

import com.adigium.androidrfb.rfb.encoding.EncodingInterface;
import com.adigium.androidrfb.rfb.encoding.Encodings;
import com.adigium.androidrfb.rfb.service.SetPixelFormat;

public class TightEncoder implements EncodingInterface {

	public static boolean USE_BASIC_COMPRESSION = false;
	
	private final BasicCompression basicCompression;
	
//	private final JpegCompression jpegCompression;
	
	public TightEncoder() {
	
		this.basicCompression = new BasicCompression();
//		this.jpegCompression = new JpegCompression();
	}
	
	@Override
	public byte[] encode(final int[] image, final int width, final int height
			, final SetPixelFormat pixelFormat) {

		// If basic compression is enforced by flag value, then use it
		// regardless of input pixel format information.
		if (TightEncoder.USE_BASIC_COMPRESSION == true) {
			
			return this.basicCompression.encode(image, width, height, pixelFormat);
		}
		
		// If pixel format allows JPEG compression, then use it.
//		if (pixelFormat.bitsPerPixel == 32
//				&& pixelFormat.depth == 24) {
//
//			return this.jpegCompression.encode(image, width, height, pixelFormat);
//		}
		
		// Otherwise, fall-back to basic compression.
		return this.basicCompression.encode(image, width, height, pixelFormat);		
	}

	@Override
	public int getType() {

		return Encodings.TIGHT;
	}
}
