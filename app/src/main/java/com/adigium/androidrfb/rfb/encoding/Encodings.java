package com.adigium.androidrfb.rfb.encoding;

import com.adigium.androidrfb.rfb.tight.TightEncoder;

public class Encodings {

	public static final int RAW = 0
			, COPY_RECT = 1
			, RRE = 2
			, HEXTILE = 5
			, ZRLE = 16
			, ZLIB = 6
			, TIGHT = 7
			, RICH_CURSOR = -239;

	private Encodings() { }
	

	public static EncodingInterface newInstance(final int encodingType) {
		
		if (encodingType == RAW) {
			
			return new RawEncoder();
		}

		if (encodingType == HEXTILE) {

			return new HextileEncoder();
		}
		
		if (encodingType == ZLIB) {
		
			return new ZlibEncoder();
		}

		if (encodingType == RICH_CURSOR) {
			
			return new RichCursorEncoder();
		}

		//TODO: FIX TIGHT ENCODING
//		if (encodingType == TIGHT) {
//
//			return new TightEncoder();
//		}
		
		return null;
	}

}
