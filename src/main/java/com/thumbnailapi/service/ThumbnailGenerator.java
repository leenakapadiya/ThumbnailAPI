package com.thumbnailapi.service;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.imgscalr.Scalr;
import org.springframework.stereotype.Service;

import com.thumbnailapi.exception.InvalidImageException;
import com.thumbnailapi.model.ThumbnailMetadata;

/**
 * Service for generating image thumbnails.
 * 
 * Handles resizing of images to specified dimensions while maintaining
 * quality and aspect ratios.
 */
@Service
public class ThumbnailGenerator {

    private static final Logger logger = LogManager.getLogger(ThumbnailGenerator.class);

    public ThumbnailGenerator() {
    }

    /**
     * Generates thumbnails for an image at multiple dimensions.
     * 
     * @param imageBytes the original image bytes
     * @param imageFormat the format of the original image
     * @param dimensions list of dimensions to generate
     * @return list of ThumbnailMetadata for each generated thumbnail
     * @throws InvalidImageException if thumbnail generation fails
     */
    public List<ThumbnailMetadata> generateThumbnails(byte[] imageBytes, 
                                                     String imageFormat,
                                                     List<Dimension> dimensions) {
        List<ThumbnailMetadata> thumbnails = new ArrayList<>();

        try {
            BufferedImage originalImage = readImage(imageBytes);
            Map<String, String> sizeNameMapping = buildSizeNameMapping(dimensions);

            for (Dimension dimension : dimensions) {
                long startTime = System.currentTimeMillis();
                
                BufferedImage thumbnail = resizeImage(originalImage, dimension);
                byte[] thumbnailBytes = encodeImage(thumbnail, imageFormat);
                
                long processingTime = System.currentTimeMillis() - startTime;
                
                String sizeName = sizeNameMapping.get(dimension.width + "x" + dimension.height);
                
                ThumbnailMetadata metadata = ThumbnailMetadata.create(
                    sizeName,
                    thumbnail.getWidth(),
                    thumbnail.getHeight(),
                    imageFormat,
                    thumbnailBytes.length,
                    processingTime
                );

                thumbnails.add(metadata);
                logger.debug("Generated thumbnail: {} ({}x{}) in {}ms",
                    sizeName, thumbnail.getWidth(), thumbnail.getHeight(), processingTime);
            }

        } catch (IOException e) {
            logger.error("Failed to generate thumbnails", e);
            throw new InvalidImageException("Failed to generate thumbnails: " + e.getMessage(), e);
        }

        return thumbnails;
    }

    /**
     * Reads image from byte array.
     */
    private BufferedImage readImage(byte[] imageBytes) throws IOException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes)) {
            BufferedImage image = ImageIO.read(bais);
            if (image == null) {
                throw new IOException("Unable to read image data");
            }
            return image;
        }
    }

    /**
     * Resizes image to specified dimension, maintaining aspect ratio.
     */
    private BufferedImage resizeImage(BufferedImage originalImage, Dimension targetDimension) {
        int targetWidth = targetDimension.width;
        int targetHeight = targetDimension.height;

        return Scalr.resize(originalImage, Scalr.Method.QUALITY, Scalr.Mode.AUTOMATIC,
            targetWidth, targetHeight, Scalr.OP_ANTIALIAS);
    }

    /**
     * Encodes BufferedImage to bytes in specified format.
     */
    private byte[] encodeImage(BufferedImage image, String format) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            String imageFormat = determineImageFormat(format);
            if (!ImageIO.write(image, imageFormat, baos)) {
                throw new IOException("Failed to encode image as " + imageFormat);
            }
            return baos.toByteArray();
        }
    }

    /**
     * Determines the ImageIO format string from image format.
     */
    private String determineImageFormat(String format) {
        return switch (format.toUpperCase()) {
            case "JPEG", "JPG" -> "jpg";
            case "PNG" -> "png";
            case "GIF" -> "gif";
            case "BMP" -> "bmp";
            case "WEBP" -> "webp";
            case "TIFF" -> "tiff";
            default -> "jpg";
        };
    }

    /**
     * Builds a mapping of dimension strings to preset names.
     */
    private Map<String, String> buildSizeNameMapping(List<Dimension> dimensions) {
        Map<String, String> mapping = new HashMap<>();
        
        for (Dimension dim : dimensions) {
            String key = dim.width + "x" + dim.height;
            String name = mapDimensionToName(dim.width, dim.height);
            mapping.put(key, name);
        }
        
        return mapping;
    }

    /**
     * Maps dimension to preset name or uses custom format.
     */
    private String mapDimensionToName(int width, int height) {
        if (width == 150 && height == 150) return "small";
        if (width == 300 && height == 300) return "medium";
        if (width == 600 && height == 600) return "large";
        return width + "x" + height;
    }
}
