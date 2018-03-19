package utils;

import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class ImageUtils {
    public static final Scalr.Method quality = Scalr.Method.SPEED;

    public static Dimension getImageDimension(File imgFile) throws IOException {
        int pos = imgFile.getName().lastIndexOf(".");
        if (pos == -1) throw new IOException("No extension for file: " + imgFile.getAbsolutePath());

        String suffix = imgFile.getName().substring(pos + 1);
        Iterator<ImageReader> iter = ImageIO.getImageReadersBySuffix(suffix);
        while(iter.hasNext()) {
            ImageReader reader = iter.next();
            try (ImageInputStream stream = new FileImageInputStream(imgFile)) {
                reader.setInput(stream);
                int width = reader.getWidth(reader.getMinIndex());
                int height = reader.getHeight(reader.getMinIndex());
                return new Dimension(width, height);
            } finally {
                reader.dispose();
            }
        }
        throw new IOException("Not a known image file: " + imgFile.getAbsolutePath());
    }

    public static BufferedImage resizeImage(File img_file, long sizeW, long sizeH, boolean crop) {
        if (!img_file.canRead()) return null;
        try {
            final java.awt.Image image2 = Toolkit.getDefaultToolkit().createImage(img_file.getAbsolutePath());
            final MediaTracker mediaTracker = new MediaTracker(new Container());
            mediaTracker.addImage(image2, 0);
            mediaTracker.waitForAll();

            final int
                    w_size = image2.getWidth(null),
                    h_size = image2.getHeight(null);

            final BufferedImage image = new BufferedImage(w_size, h_size, BufferedImage.TYPE_INT_RGB);
            final Graphics2D g2d = image.createGraphics();
            g2d.setBackground(new Color(255, 255, 255));
            g2d.setColor(new Color(255, 255, 255));
            g2d.fillRect(0, 0, w_size, h_size);
            g2d.drawImage(image2, 0, 0, null);
            g2d.dispose();

            final BufferedImage out_img, crop_img;
            if (crop) {
                if (w_size > h_size) {
                    final double
                            in_k  = ((double) w_size) / ((double) h_size),
                            out_k = ((double) sizeW) / ((double) sizeH);

                    out_img = Scalr.resize(image, quality, Scalr.Mode.FIT_TO_HEIGHT, (int) ((in_k < out_k) ? ((int)(sizeH * out_k)) : (sizeH)), Scalr.OP_ANTIALIAS);
                    final long
                            out_x = (out_img.getWidth() - sizeW) / 2,
                            out_y = (out_img.getHeight() - sizeH) / 2;

                    crop_img = Scalr.crop(out_img, (int) out_x, (int) out_y, (int) sizeW, (int) sizeH, Scalr.OP_ANTIALIAS);
                } else {
                    final double
                            in_k  = ((double) h_size) / ((double) w_size),
                            out_k = ((double) sizeH) / ((double) sizeW);

                    out_img = Scalr.resize(image, quality, Scalr.Mode.FIT_TO_WIDTH, (int) ((in_k < out_k) ? ((int)(sizeW * out_k)) : (sizeW)), Scalr.OP_ANTIALIAS);
                    final long
                            out_x = (out_img.getWidth() - sizeW) / 2,
                            out_y = (out_img.getHeight() - sizeH) / 2;

                    crop_img = Scalr.crop(out_img, (int) out_x, (int) out_y, (int) sizeW, (int) sizeH, Scalr.OP_ANTIALIAS);
                }

                return crop_img;
            } else {
                out_img = Scalr.resize(image,quality, Scalr.Mode.AUTOMATIC, (int) sizeW, (int) sizeH, Scalr.OP_ANTIALIAS);
                return out_img;
            }
        } catch (InterruptedException e) {
            return null;
        }
    }

    public static BufferedImage resizeImage(BufferedImage image2, long sizeW, long sizeH, boolean crop) {
        final int
                w_size = image2.getWidth(null),
                h_size = image2.getHeight(null);

        final BufferedImage srcImage = new BufferedImage(w_size, h_size, BufferedImage.TYPE_INT_RGB);
        final Graphics2D g2d = srcImage.createGraphics();
        g2d.setBackground(new Color(255, 255, 255));
        g2d.setColor(new Color(255, 255, 255));
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
