package com.adigium.androidrfb.rfb.encoding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Tile {

	private final int[] pixels;
	
	/**
	 * Currently tiles are limited to 16x16 pixel.
	 */
	public final short width = 16, height = 16;
	
	public final short xPos, yPos;
	

	public Tile(final int[] pixels, final short xPos, final short yPos) {
	
		this.pixels = pixels;
		
		this.xPos = xPos;
		this.yPos = yPos;
	}
	

	public int[] raw() {
		
		return this.pixels;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + height;
		result = prime * result + Arrays.hashCode(pixels);
		result = prime * result + width;
		result = prime * result + xPos;
		result = prime * result + yPos;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Tile other = (Tile) obj;
		if (height != other.height)
			return false;
		if (!Arrays.equals(pixels, other.pixels))
			return false;
		if (width != other.width)
			return false;
		if (xPos != other.xPos)
			return false;
		if (yPos != other.yPos)
			return false;
		return true;
	}

	@Override
	public String toString() {
		
		return String.format("%s-[%d-%d]", Tile.class.getSimpleName(), this.xPos, this.yPos);
	}
	

	public static List<Tile> build(final int[] image
			, final int width, final int height) {
				
		final List<Tile> tiles = new ArrayList<>();
		
		// Scan over image, and create a copy of 16x16 tiles.
		int x = 0, y = 0;
		while (x < width && y < height) {
		
			final int[] tileData = new int[256]; // Use always 16x16,
													// even if bottom part of image is not divisible by 16.

			final int firstLine = x + y * width;	// Offset in source image, at which pixel tile begins. 
			for (int i = 0 ; i < 16 ; i++) {
			
				int destPos = i * 16;
				int line = firstLine + (i * width);
				System.arraycopy(image, line, tileData, destPos, 16);
			}
			
			tiles.add(new Tile(tileData, (short) x, (short) y));
			
			x = x + 16;
			
			if (x + 16 > width) {
				
				x = 0;
				
				y = y + 16;
				
				if (y + 16 > height) {
					
					break;
				}
			}			
		}
		
		return tiles;
	}
}
