package com.adigium.androidrfb.rfb.encoding;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import com.adigium.androidrfb.rfb.service.SetPixelFormat;

public class HextileEncoder implements EncodingInterface {

	public final static byte MASK_RAW = 0b00000001
			, MASK_BACKGROUND_SPECIFIED = 0b00000010
			, MASK_FOREGROUND_SPECIFIED = 0b00000100
			, MASK_ANY_SUBRECT = 0b00001000
			, MASK_SUBRECTS_COLOURED = 0b00010000
			;
	
	private final RawEncoder rawEncoder;
	
	public HextileEncoder() {
		
		this.rawEncoder = new RawEncoder();
	}

	@Override
	public byte[] encode(final int[] image, final int width, final int height, final SetPixelFormat pixelFormat) {

		final List<Tile> tiles = Tile.build(image, width, height);

		final ByteArrayOutputStream bOut = new ByteArrayOutputStream();
		final DataOutputStream out = new DataOutputStream(bOut);
		
		for (final Tile tile : tiles) {
			
			try {
				
				final Integer singlePixelValue = singlePixelTile(tile);
				
				if (singlePixelValue != null) {
					
					// When mask byte has background bit set,
					// tile consists of single colour. All pixels have same value. 
					byte subencodingMask = MASK_BACKGROUND_SPECIFIED;
					
					out.write(subencodingMask);
					
					// Use pixel transform routing with pixel format provided. This covers case when
					// VNC client requests 8-bit color mode, while source image is 32-bit color image.					
					final byte bitsPerPixel = pixelFormat.bitsPerPixel;

					
					if (bitsPerPixel == 8) {
					
						out.writeByte(PixelTransform.transform(singlePixelValue, pixelFormat));
					}
					else if (bitsPerPixel == 16) {
						
						out.writeShort(PixelTransform.transform(singlePixelValue, pixelFormat));
					}
					else if (bitsPerPixel == 32) {
						
						out.writeInt(PixelTransform.transform(singlePixelValue, pixelFormat));
					}
					else {
						
						Log.e("HextileEncoder","Unsupported bits per pixel value: " + bitsPerPixel);
						
						break;
					}
				}
				else {
					
					byte subencodingMask = MASK_RAW;
					
					// If tile contains more colours, then use raw encoding of tile. 
					out.write(subencodingMask);
					out.write(rawEncoder.encode(tile.raw(), tile.width, tile.height, pixelFormat));
				}
			}
			catch (final IOException ex) {
				
				Log.e("HextileEncoder","Hextile encoding problem.", ex);
			}
		}
		
		return bOut.toByteArray();
	}

	@Override
	public int getType() {
		
		return Encodings.HEXTILE;
	}


	private Integer singlePixelTile(final Tile tile) {
		
		if (tile == null || tile.raw() == null) {
		
			return null;
		}
		
		final int[] raw = tile.raw();
		
		int firstPixel = raw[0];
		
		for (int i = 1 ; i < raw.length ; i++) {
			
			if (raw[i] != firstPixel) {
				
				return null;
			}
		}
		
		return firstPixel;
	}
}
