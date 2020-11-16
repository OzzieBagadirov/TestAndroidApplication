package com.adigium.androidrfb.rfb.screen;


public class LoadingResource {

//	public static TrueColorImage get(final int width, final int height) throws IOException {
//
//		final String resourceFilename = "git.properties";
//
//		final InputStream inputStream =
//				LoadingResource.class.getClassLoader().getResourceAsStream(resourceFilename);
//
//		if (inputStream == null) {
//
//			throw new IOException("Resource '" + resourceFilename + "' not found on class path.");
//		}
//
//		final BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
//
//		final Graphics2D graphics = newImage.createGraphics();
//		graphics.setFont(new Font(Font.MONOSPACED, Font.PLAIN, Double.valueOf(1.35f * graphics.getFont().getSize()).intValue()));
//
//		final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
//		String line;
//		int offset = 10, increment = graphics.getFontMetrics().getHeight() + 3;
//		while ( (line = reader.readLine()) != null) {
//
//			graphics.drawString(line, 10, offset);
//			offset = offset + increment;
//		}
//
//		graphics.dispose();
//
//	    final int[] colorImageBuffer = ((DataBufferInt) newImage.getRaster().getDataBuffer()).getData();
//
//	    return new TrueColorImage(colorImageBuffer, newImage.getWidth(), newImage.getHeight());
//	}
}
