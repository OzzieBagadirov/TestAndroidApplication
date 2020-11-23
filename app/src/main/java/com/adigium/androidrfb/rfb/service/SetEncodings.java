package com.adigium.androidrfb.rfb.service;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

class SetEncodings {

	public int[] encodingType;

	public SetEncodings(final int[] encodings) {

		this.encodingType = encodings;
	}

	public static SetEncodings read(final InputStream inputStream) throws IOException {
		
		final DataInputStream in = new DataInputStream(inputStream);
		
		short numOfEncodings = in.readShort();
		
		int[] encodingTypes = new int[numOfEncodings];
		
		for (int i = 0 ; i < numOfEncodings ; i++) {
			
			int encoding = in.readInt();
			encodingTypes[i] = encoding;
		}
		
		return new SetEncodings(encodingTypes);
	}
}
