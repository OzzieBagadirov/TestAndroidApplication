package com.adigium.androidrfb.rfb.tight;

class CompactLength {

	public static byte[] calc(int length) {
		
		if (length < 128) {
			
			return new byte[]{ (byte) length };
		}
		else if (length >= 128 && length <= 16383) {
			
			byte b1 = (byte) ((length & 0b01111111) | 0b10000000);
			byte b2 = (byte) ((length >> 7) & 0b01111111);
			
			return new byte[] {b1, b2};
		}

		byte b1 = (byte) ((length & 0b01111111) | 0b10000000);
		byte b2 = (byte) (((length >> 7) & 0b01111111) | 0b10000000);
		byte b3 = (byte) (length >> 14);
		
		return new byte[] {b1, b2, b3};
	}

}
