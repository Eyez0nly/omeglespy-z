/**
 * 
 */
package org.darkimport.omeglespy_z;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;

import javax.imageio.ImageIO;

/**
 * @author user
 * 
 */
public class Image2Ascii {
	/**
	 * takes the grayscale value as parameter
	 * 
	 * Create a new string and assign to it a string based on the grayscale
	 * value. If the grayscale value is very high, the pixel is very bright and
	 * assign characters such as . and , that do not appear very dark. If the
	 * grayscale value is very lowm the pixel is very dark, assign characters
	 * such as # and @ which appear very dark.
	 * 
	 * @param g
	 * @return
	 */
	public static String returnStrPos(final double g) {
		String str = " ";

		if (g >= 230) {
			str = " ";
		} else if (g >= 200) {
			str = ".";
		} else if (g >= 180) {
			str = "*";
		} else if (g >= 160) {
			str = ":";
		} else if (g >= 130) {
			str = "o";
		} else if (g >= 100) {
			str = "&";
		} else if (g >= 70) {
			str = "8";
		} else if (g >= 50) {
			str = "#";
		} else {
			str = "@";
		}

		return str; // return the character
	}

	/**
	 * same method as above, except it reverses the darkness of the pixel. A
	 * dark pixel is given a light character and vice versa.
	 * 
	 * @param g
	 * @return
	 */
	public static String returnStrNeg(final double g) {
		String str = " ";

		if (g >= 230) {
			str = "@";
		} else if (g >= 200) {
			str = "#";
		} else if (g >= 180) {
			str = "8";
		} else if (g >= 160) {
			str = "&";
		} else if (g >= 130) {
			str = "o";
		} else if (g >= 100) {
			str = ":";
		} else if (g >= 70) {
			str = "*";
		} else if (g >= 50) {
			str = ".";
		} else {
			str = " ";
		}

		return str;
	}

	/**
	 * @param recaptchaImageLocation
	 * @return
	 * @throws IOException
	 */
	static String assciiFyImage(final URL recaptchaImageLocation) throws IOException {
		final BufferedImage recaptchaImage = ImageIO.read(recaptchaImageLocation);
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		final PrintStream pOut = new PrintStream(out);
	
		// iterate through the rows of the image
		for (int y = 0; y < recaptchaImage.getHeight(); y++) {
			// iterate through each individual pixel of the row
			for (int x = 0; x < recaptchaImage.getWidth(); x++) {
				// The color of the current pixel
				final Color pixelColor = new Color(recaptchaImage.getRGB(x, y));
				// Calculate the grayscale value of the pixel.
				// value is 30% of the red value, 59% of the green value and
				// 11% of the blue value.
				final double gValue = pixelColor.getRed() * 0.2989 + pixelColor.getBlue() * 0.5870
						+ pixelColor.getGreen() * 0.1140;
				// print the appropriate character according to the darkness
				// of the pixel.
				pOut.print(returnStrNeg(gValue));
			}
			pOut.println();
		}
	
		// Now write each line to the console individually
		String ascii_fiedImage = new String(out.toByteArray());
		return ascii_fiedImage;
	}
}
