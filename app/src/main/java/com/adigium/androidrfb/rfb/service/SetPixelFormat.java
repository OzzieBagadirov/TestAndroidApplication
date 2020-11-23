package com.adigium.androidrfb.rfb.service;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SetPixelFormat {

	public byte bitsPerPixel, depth, bigEndianFlag, trueColorFlag;
	
	public short redMax, greenMax, blueMax;
	
	public byte redShift, greenShift, blueShift;

	public SetPixelFormat(byte bitsPerPixel, byte depth, byte bigEndianFlag, byte trueColorFlag, short redMax,
			short greenMax, short blueMax, byte redShift, byte greenShift, byte blueShift) {

		this.bitsPerPixel = bitsPerPixel;
		this.depth = depth;
		this.bigEndianFlag = bigEndianFlag;
		this.trueColorFlag = trueColorFlag;
		this.redMax = redMax;
		this.greenMax = greenMax;
		this.blueMax = blueMax;
		this.redShift = redShift;
		this.greenShift = greenShift;
		this.blueShift = blueShift;
	}

	public SetPixelFormat() {
		
		this.bitsPerPixel = (byte)32;
		this.depth = bitsPerPixel;
		this.bigEndianFlag = 0;
		this.trueColorFlag = 1;
		
		this.redMax   = (short) 255;
		this.greenMax = (short) 255;
		this.blueMax  = (short) 255;
		
		this.redShift   = 16;
		this.greenShift = 8;
		this.blueShift  = 0;

		if (this.bitsPerPixel == 24) {

			/*
			 * VNC viewers do not support color mode of 24-bits.
			 */

			this.bitsPerPixel = 32;
			this.depth = this.bitsPerPixel;
		}

		if (this.bitsPerPixel == 16) {
			
			/*
			 * Just in case that display is a 16-bit color mode.
			 * Use appropriate maximum color values and bit positions at which 
			 * each color value begins.
			 */
			
			this.redMax   = (byte) 0x1F;
			this.greenMax = (byte) 0x3F;
			this.blueMax  = (byte) 0x1F;
			
			this.redShift   = 11;
			this.greenShift = 5;
			this.blueShift  = 0;
		}
	}
	
	public static void write(final OutputStream outputStream, final SetPixelFormat message) throws IOException {
		
		final DataOutputStream out = new DataOutputStream(outputStream);
		
		out.write(message.bitsPerPixel);
		out.write(message.depth);
		out.write(message.bigEndianFlag);
		out.write(message.trueColorFlag);
		out.writeShort(message.redMax);
		out.writeShort(message.greenMax);
		out.writeShort(message.blueMax);
		out.write(message.redShift);
		out.write(message.greenShift);
		out.write(message.blueShift);
		out.write(new byte[]{0, 0, 0}); // Padding.		
	}
	
	public static SetPixelFormat read(final InputStream inputStream) throws IOException {
		
		final DataInputStream in = new DataInputStream(inputStream);
		
		final SetPixelFormat setPixelFormat = new SetPixelFormat();
		
		setPixelFormat.bitsPerPixel = in.readByte();
		setPixelFormat.depth = in.readByte();
		setPixelFormat.bigEndianFlag = in.readByte();
		setPixelFormat.trueColorFlag = in.readByte();


		setPixelFormat.redMax = in.readShort();
		setPixelFormat.greenMax = in.readShort();
		setPixelFormat.blueMax = in.readShort();

		setPixelFormat.blueShift = in.readByte();
		setPixelFormat.greenShift = in.readByte();
		setPixelFormat.redShift = in.readByte();

		in.read(new byte[3]); // Padding.
		
		return setPixelFormat;
	}

	public static SetPixelFormat default32bit() {

		return new SetPixelFormat(
				(byte) 32			// bits per pixel
				, (byte) 24			// depth
				, (byte) 0			// big endian
				, (byte) 1			// true color
				, (short) 255, (short) 255, (short) 255	// red, green, blue max.
				, (byte) 16, (byte) 8, (byte) 0	// red, green, blue shift.
				);
	}
}
