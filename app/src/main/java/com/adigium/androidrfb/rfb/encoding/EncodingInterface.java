package com.adigium.androidrfb.rfb.encoding;

import com.adigium.androidrfb.rfb.service.SetPixelFormat;

public interface EncodingInterface {

	public byte[] encode(final int[] image, final int width, final int height, final SetPixelFormat pixelFormat);

	public int getType();
}
