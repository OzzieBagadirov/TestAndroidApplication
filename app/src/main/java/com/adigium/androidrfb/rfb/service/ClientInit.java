package com.adigium.androidrfb.rfb.service;

import java.io.IOException;
import java.io.InputStream;

public class ClientInit {

	public final boolean sharedDesktop;

	public ClientInit(final boolean sharedDesktop) {
		
		this.sharedDesktop = sharedDesktop;
	}

	public static ClientInit readClientInit(final InputStream in) throws IOException {
		
		int sharedDesktop = in.read();
		
		return new ClientInit(sharedDesktop > 0);
	}
}
