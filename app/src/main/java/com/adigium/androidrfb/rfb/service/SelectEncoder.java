package com.adigium.androidrfb.rfb.service;

import android.util.Log;

import com.adigium.androidrfb.rfb.encoding.EncodingInterface;
import com.adigium.androidrfb.rfb.encoding.Encodings;
import com.adigium.androidrfb.rfb.encoding.RawEncoder;

class SelectEncoder {

	public static EncodingInterface selectEncoder(
			EncodingInterface lastEncoder
			, final int[] clientEncodings
			, final int[] preferredEncodings
			) {
				
		// Reuse previously used encoder, if already set,
		// and VNC client supports it.
		if (lastEncoder != null && containsEncoding(lastEncoder.getType(), clientEncodings)) {
			
			return lastEncoder;
		}
		
		// Use RFB service preferred encoding type list.
		if (preferredEncodings != null) {
						
			// Look if some of preferred encodings is present in VNC client supported list.
			for (int encoding : preferredEncodings) {
				
				if (containsEncoding(encoding, clientEncodings)) {
					
					lastEncoder = Encodings.newInstance((byte) encoding);
					
					if (lastEncoder == null) {
						
						lastEncoder = new RawEncoder();
					}
					
					Log.i("SelectEncoder", String.format("Selected preferred encoder: '%s'.", lastEncoder.getClass().getSimpleName()));
					
					return lastEncoder;
				}
			}
		}
		
		// Finally, use client list of supported encoding types.
		for (int encoding : clientEncodings) {

			lastEncoder = Encodings.newInstance((byte) encoding);
			
			if (lastEncoder != null) {
				
				Log.i("SelectEncoder", String.format("Selected encoder by client: '%s'.", lastEncoder.getClass().getSimpleName()));
				
				return lastEncoder;
			}
		}
		
		Log.i("SelectEncoder", String.format("Selected fall-back encoder: '%s'.", RawEncoder.class.getSimpleName()));
		
		// Fall-back, raw encoder return. If all of above fails to return result.
		return new RawEncoder();
	}

	public static boolean containsEncoding(final int type, final int[] encodings) {
	
		if (encodings == null) {
			
			return false;
		}
		
		for (int encodingType : encodings) {
			
			if (encodingType == type) {
				
				return true;
			}
		}
		
		return false;
	}
}
