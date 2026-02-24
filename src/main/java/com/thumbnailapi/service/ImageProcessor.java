package com.thumbnailapi.service;

import java.awt.Dimension;
import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.thumbnailapi.exception.InvalidImageException;
import com.thumbnailapi.model.ThumbnailMetadata;
import com.thumbnailapi.model.ThumbnailResponse;
import com.thumbnailapi.util.DimensionParser;
import com.thumbnailapi.util.ImageFormatDetector;
import com.thumbnailapi.util.ImageValidator;

/**
 * Main service for processing image upload and thumbnail generation requests.
 * 
 * Orchestrates validation, dimension parsing, and thumbnail generation.
 */
@Service
public class ImageProcessor {

    private static final Logger logger = LogManager.getLogger(ImageProcessor.class);

    private final ImageValidator imageValidator;
    private final ImageFormatDetector formatDetector;
    private final DimensionParser dimensionParser;
    private final ThumbnailGenerator thumbnailGenerator;

    public ImageProcessor(ImageValidator imageValidator,
                         ImageFormatDetector formatDetector,
                         DimensionParser dimensionParser,
                         ThumbnailGenerator thumbnailGenerator) {
        this.imageValidator = imageValidator;
        this.formatDetector = formatDetector;
        this.dimensionParser = dimensionParser;
        this.thumbnailGenerator = thumbnailGenerator;
    }

    /**
     * Processes an uploaded image and generates thumbnails.
     * 
     * @param file the uploaded image file
     * @param sizesParam comma-separated string of thumbnail sizes
     * @return ThumbnailResponse containing original image info and thumbnail metadata
     * @throws various exceptions for validation failures
     */
    public ThumbnailResponse processImage(MultipartFile file, String sizesParam) {
        long startTime = System.currentTimeMillis();
        
        logger.info("Processing image upload: {} ({})", file.getOriginalFilename(), file.getSize());

        // Validate
        imageValidator.validate(file);
        logger.debug("Image validation completed");

        try {
            // Read file bytes
            byte[] imageBytes = file.getBytes();
            
            // Detect format
            String format = formatDetector.detectFormat(imageBytes);
            logger.debug("Detected image format: {}", format);
            
            // Get original dimensions
            ImageFormatDetector.ImageDimensions dimensions = formatDetector.readImageDimensions(imageBytes);
            logger.debug("Original image dimensions: {}x{}", dimensions.width(), dimensions.height());
            
            // Parse target dimensions
            List<Dimension> targetDimensions = (List<Dimension>) dimensionParser.parseDimensions(sizesParam);
            logger.debug("Generated {} thumbnail sizes", targetDimensions.size());
            
            // Generate thumbnails
            List<ThumbnailMetadata> thumbnails = (List<ThumbnailMetadata>) thumbnailGenerator.generateThumbnails(
                imageBytes,
                format,
                targetDimensions
            );
            
            long totalProcessingTime = System.currentTimeMillis() - startTime;
            logger.info("Image processing completed in {}ms. Generated {} thumbnails",
                totalProcessingTime, thumbnails.size());
            
            // Build response
            return ThumbnailResponse.builder()
                .originalFilename(file.getOriginalFilename())
                .originalFormat(format)
                .originalDimensions(dimensions.width(), dimensions.height())
                .originalFileSizeBytes(file.getSize())
                .thumbnails(thumbnails)
                .build();

        } catch (IOException e) {
            logger.error("Failed to process image: {}", file.getOriginalFilename(), e);
            throw new InvalidImageException("Failed to process image: " + e.getMessage(), e);
        }
    }
}
