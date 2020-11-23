package com.adigium.androidrfb.rfb.encoding;

import android.content.Context;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.adigium.androidrfb.R;
import com.adigium.androidrfb.rfb.service.SetPixelFormat;


public class RichCursorEncoder implements EncodingInterface {
	public static Context context;

	@Override
	public byte[] encode(int[] image, int width, int height, SetPixelFormat pixelFormat) {
		
		try {
			
			final String bitmaskName = "cursor_encoding_bitmask.raw"
					, pixelName = "cursor_encoding_pixels.raw";
			
			final InputStream inputStream1 =
					context.getResources().openRawResource(R.raw.cursor_encoding_pixels);
			
			if (inputStream1 == null) {
				
				Log.e("CURSOR_ENCODER","Resource '" + pixelName + "' not found on class path.");
				
				return null;
			}
	
			final InputStream inputStream2 =
					context.getResources().openRawResource(R.raw.cursor_encoding_bitmask);
			
			if (inputStream2 == null) {
				
				Log.e("CURSOR_ENCODER", "Resource '" + bitmaskName + "' not found on class path.");
				
				return null;
			}
			
			final ByteArrayOutputStream bOut = new ByteArrayOutputStream();
			
			int encodedPixelsLength = 1296, bitmaskLength = 54;
			final byte[] encodedPixels = new byte[encodedPixelsLength];
			final byte[] bitmask       = new byte[bitmaskLength];
			
			int len = inputStream1.read(encodedPixels);
			if (len != encodedPixelsLength) {
				
				Log.e("CURSOR_ENCODER","Unable to read completely cursor pixels.");
				
				return null;
			}
			
			len = inputStream2.read(bitmask);
			if (len != bitmaskLength) {
				
				Log.e("CURSOR_ENCODER","Unable to read completely bitmask value.");
				
				return null;
			}		
			
			bOut.write(encodedPixels);
			bOut.write(bitmask);
			
			if (bOut.size() != encodedPixelsLength + bitmaskLength) {
				
				Log.e("CURSOR_ENCODER","Resulting cursor buffer length is unexpected: " + bOut.size() + ". Expected is: " + String.valueOf(encodedPixelsLength + bitmaskLength));
				
				return null;
			}
			
			return bOut.toByteArray();
		} catch (final IOException ex) {
			
			Log.e("CURSOR_ENCODER","Cursor data reading failed.", ex);
			return null;
		}
	}

	@Override
	public int getType() {

		return Encodings.RICH_CURSOR;
	}

}
