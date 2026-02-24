package com.thumbnailapi.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Component;

/**
 * Detects image format and reads image properties.
 */
@Component
public class ImageFormatDetector {

    /**
     * Detects the format of an image from its byte content.
     * 
     * @param imageBytes the image file bytes
     * @return the detected format (e.g., "JPEG", "PNG")
     */
    public String detectFormat(byte[] imageBytes) {
        if (imageBytes.length >= 3 && imageBytes[0] == (byte) 0xFF && 
            imageBytes[1] == (byte) 0xD8 && imageBytes[2] == (byte) 0xFF) {
            return "JPEG";
        }

        if (imageBytes.length >= 8 && imageBytes[0] == (byte) 0x89 && 
            imageBytes[1] == 0x50 && imageBytes[2] == 0x4E && imageBytes[3] == 0x47) {
            return "PNG";
        }

        if (imageBytes.length >= 6 && imageBytes[0] == 0x47 && 
            imageBytes[1] == 0x49 && imageBytes[2] == 0x46) {
            return "GIF";
        }

        if (imageBytes.length >= 2 && imageBytes[0] == 0x42 && imageBytes[1] == 0x4D) {
            return "BMP";
        }

        if (imageBytes.length >= 12 && imageBytes[0] == 0x52 && imageBytes[1] == 0x49 && 
            imageBytes[2] == 0x46 && imageBytes[3] == 0x46 && imageBytes[8] == 0x57 && 
            imageBytes[9] == 0x45 && imageBytes[10] == 0x42 && imageBytes[11] == 0x50) {
            return "WEBP";
        }

        if (imageBytes.length >= 4 && ((imageBytes[0] == 0x49 && imageBytes[1] == 0x49 && 
            imageBytes[2] == 0x2A && imageBytes[3] == 0x00) ||
            (imageBytes[0] == 0x4D && imageBytes[1] == 0x4D && 
            imageBytes[2] == 0x00 && imageBytes[3] == 0x2A))) {
            return "TIFF";
        }

        return "UNKNOWN";
    }

    /**
     * Reads image dimensions without loading entire image.
     * 
     * @param imageBytes the image file bytes
     * @return ImageDimensions containing width and height
     * @throws IOException if image cannot be read
     */
    public ImageDimensions readImageDimensions(byte[] imageBytes) throws IOException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes)) {
            BufferedImage image = ImageIO.read(bais);
            if (image == null) {
                throw new IOException("Unable to read image");
            }
            return new ImageDimensions(image.getWidth(), image.getHeight());
        }
    }

    /**
     * Records image dimensions.
     */
    public record ImageDimensions(int width, int height) {
    }
}
