package utils;

import java.awt.*;
import java.awt.image.BufferedImage;

import org.imgscalr.Scalr;

public class ImageUtils {
	private static final Scalr.Method quality = Scalr.Method.SPEED;
	private static final Color COLOR_WHITE = new Color(255, 255, 255);

	public static BufferedImage resizeImage(BufferedImage image2, long sizeW, long sizeH, boolean crop) {
		final int
				w_size = image2.getWidth(null),
				h_size = image2.getHeight(null);

		final BufferedImage srcImage = new BufferedImage(w_size, h_size, BufferedImage.TYPE_INT_RGB);
		final Graphics2D g2d = srcImage.createGraphics();
		g2d.setBackground(COLOR_WHITE);
		g2d.setColor(COLOR_WHITE);
		g2d.fillRect(0, 0, w_size, h_size);
		g2d.drawImage(image2, 0, 0, null);
		g2d.dispose();

		final BufferedImage out_img, crop_img;
		if (crop) {
			if (srcImage.getWidth() > srcImage.getHeight()) {
				final double
						in_k  = ((double) srcImage.getWidth()) / ((double) srcImage.getHeight()),
						out_k = ((double) sizeW) / ((double) sizeH);

				out_img = Scalr.resize(srcImage, quality, Scalr.Mode.FIT_TO_HEIGHT, (int) ((in_k < out_k) ? ((int)(sizeH * out_k)) : (sizeH)), Scalr.OP_ANTIALIAS);
				final long
						out_x = (out_img.getWidth() - sizeW) / 2,
						out_y = (out_img.getHeight() - sizeH) / 2;

				crop_img = Scalr.crop(out_img, (int) out_x, (int) out_y, (int) sizeW, (int) sizeH, Scalr.OP_ANTIALIAS);
			} else {
				final double
						in_k  = ((double) srcImage.getHeight()) / ((double) srcImage.getWidth()),
						out_k = ((double) sizeH) / ((double) sizeW);

				out_img = Scalr.resize(srcImage, quality, Scalr.Mode.FIT_TO_WIDTH, (int) ((in_k < out_k) ? ((int)(sizeW * out_k)) : (sizeW)), Scalr.OP_ANTIALIAS);
				final long
						out_x = (out_img.getWidth() - sizeW) / 2,
						out_y = (out_img.getHeight() - sizeH) / 2;

				crop_img = Scalr.crop(out_img, (int) out_x, (int) out_y, (int) sizeW, (int) sizeH, Scalr.OP_ANTIALIAS);
			}

			return crop_img;
		} else {
			out_img = Scalr.resize(srcImage,quality, Scalr.Mode.AUTOMATIC, (int) sizeW, (int) sizeH, Scalr.OP_ANTIALIAS);
			return out_img;
		}
	}
}
