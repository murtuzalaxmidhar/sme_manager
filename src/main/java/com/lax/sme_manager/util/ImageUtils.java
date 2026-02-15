package com.lax.sme_manager.util;

import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(ImageUtils.class);

    /**
     * Processes a signature image: removes white background and sets opacity.
     */
    public static Image processSignature(String path, double opacity, boolean removeBackground) {
        try {
            File file = new File(path);
            if (!file.exists())
                return null;

            BufferedImage bimg = ImageIO.read(file);
            if (bimg == null) {
                LOGGER.error("Failed to read image at path: {}", path);
                return null;
            }
            int width = bimg.getWidth();
            int height = bimg.getHeight();

            WritableImage wimg = new WritableImage(width, height);
            PixelWriter pw = wimg.getPixelWriter();

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int argb = bimg.getRGB(x, y);
                    int r = (argb >> 16) & 0xFF;
                    int g = (argb >> 8) & 0xFF;
                    int b = argb & 0xFF;

                    // Basic white removal logic
                    if (removeBackground && r > 240 && g > 240 && b > 240) {
                        pw.setColor(x, y, Color.TRANSPARENT);
                    } else {
                        Color baseColor = Color.rgb(r, g, b, opacity);
                        pw.setColor(x, y, baseColor);
                    }
                }
            }
            return wimg;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
