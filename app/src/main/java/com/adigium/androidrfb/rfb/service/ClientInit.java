package com.adigium.androidrfb.rfb.service;

import java.io.IOException;
import java.io.InputStream;

public class ClientInit {

	public final boolean sharedDesktop;
	
	/**
	 * Shared-flag is non-zero (true) if the server should try to share the desktop by leaving
	 * other clients connected, zero (false) if it should give exclusive access to this client by
	 * disconnecting all other clients.
	 * <p>
	 * 
	 * @param sharedDesktop
	 */
	public ClientInit(final boolean sharedDesktop) {
		
		this.sharedDesktop = sharedDesktop;
	}

	public static ClientInit readClientInit(final InputStream in) throws IOException {
		
		int sharedDesktop = in.read();
		
		return new ClientInit(sharedDesktop > 0);
	}
}
