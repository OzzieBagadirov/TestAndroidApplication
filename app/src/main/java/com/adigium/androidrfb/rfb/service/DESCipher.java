package com.adigium.androidrfb.rfb.service;

import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;


class DESCipher {

	private DESCipher() { }
	

	public static void enc(final String key
			, final InputStream in
			, final OutputStream out) {
		
		try {
			
			final SecretKey desKey = buildVNCAuthKey(key);
			final Cipher cipher = Cipher.getInstance("DES"); // DESede/CBC/NoPadding
			
			cipher.init(Cipher.ENCRYPT_MODE, desKey);
			final CipherInputStream cipherIn = new CipherInputStream(in, cipher);
			
			doCopy(cipherIn, out);
		} catch (final Exception ex) {
			
			Log.e("DESCiphier", "DES encryption failed.", ex);
		}
	}

	public static void dec(final String key
			, final InputStream in
			, final OutputStream out) {
		
		try {
			
			final SecretKey desKey = buildVNCAuthKey(key);
			final Cipher cipher = Cipher.getInstance("DES"); // DES/ECB/PKCS5Padding for SunJCE
			
			cipher.init(Cipher.DECRYPT_MODE, desKey);
			final CipherOutputStream cipherOut = new CipherOutputStream(out, cipher);
			
			doCopy(in, cipherOut);
		} catch (final Exception ex) {
			
			Log.e("DESCipher", "DES encryption failed.", ex);
		}
	}
	

	public static byte[] enc(final String key
			, final byte[] data) {
	
		final ByteArrayOutputStream bOut = new ByteArrayOutputStream();
		final ByteArrayInputStream in = new ByteArrayInputStream(data);
		
		enc(key, in, bOut);
		
		return bOut.toByteArray();
	}
	

	public static byte[] dec(final String key
			, final byte[] encrypted) {
	
		final ByteArrayOutputStream bOut = new ByteArrayOutputStream();
		final ByteArrayInputStream in = new ByteArrayInputStream(encrypted);
		
		dec(key, in, bOut);
		
		return bOut.toByteArray();
	}
	

	public static SecretKey buildVNCAuthKey(final String password) throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException {
		
		byte[] key = password.getBytes();
		
		for (int i = 0 ; i < key.length ; i++) {
			
			key[i] = reverseByte(key[i]);
		}
		
		final DESKeySpec dks = new DESKeySpec(key);
		final SecretKeyFactory skf = SecretKeyFactory.getInstance("DES");
		final SecretKey desKey = skf.generateSecret(dks);
		
		return desKey;		
	}
	

    public static byte reverseByte(byte b) {
    	
        int bi = 0xFF & b, res = 0, count = 8;
        for ( ; bi != 0; count--, bi >>>= 1)
            res = (res << 1) | (bi & 1);

        res <<= count;
        
        return (byte) (0xFF & res);
    }


	public static void doCopy(final InputStream in, final OutputStream out) throws IOException {
		
		final byte[] bytes = new byte[64];
		
		int numBytes;
		
		while ((numBytes = in.read(bytes)) != -1) {
			
			out.write(bytes, 0, numBytes);
		}
		
		out.flush();
		out.close();
		in.close();
	}

}
