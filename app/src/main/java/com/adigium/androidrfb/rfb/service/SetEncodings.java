package com.adigium.androidrfb.rfb.service;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

class SetEncodings {

	public int[] encodingType;
	
	/**
	 * Create new SetEncodings object.
	 * 
	 * @param 
	 */
	public SetEncodings(final int[] encodings) {

		this.encodingType = encodings;
	}
	
	/**
	 * Sets the encoding types in which pixel data can be sent by the server. The order of the
	 * encoding types given in this message is a hint by the client as to its preference (the first
	 * encoding specified being most preferred). The server may or may not choose to make
	 * use of this hint. Pixel data may always be sent in raw encoding even if not specified
	 * explicitly here.
	 * <p>
	 * 
	 * @param inputStream		-	{@link InputStream} to read raw data from
	 * 
	 * @return	instance of {@link SetEncodings} message
	 * 
	 * @throws IOException	if connections breaks
	 */
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
