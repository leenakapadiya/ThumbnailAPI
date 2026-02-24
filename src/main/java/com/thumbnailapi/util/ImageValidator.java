package com.thumbnailapi.util;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.thumbnailapi.exception.FileSizeLimitExceededException;
import com.thumbnailapi.exception.InvalidImageException;
import com.thumbnailapi.exception.UnsupportedFormatException;

/**
 * Validator for image files.
 * 
 * Checks file size, format, and integrity of uploaded images.
 */
@Component
public class ImageValidator {

    private static final Logger logger = LogManager.getLogger(ImageValidator.class);

    /**
     * Validates an uploaded image file.
     * 
     * @param file the multipart file to validate
     * @throws FileSizeLimitExceededException if file exceeds size limit
     * @throws UnsupportedFormatException if file format is not supported
     * @throws InvalidImageException if file is not a valid image
     */
    public void validate(MultipartFile file) {
        validateNotNull(file);
        validateFileSize(file);
        validateMimeType(file);
        validateFileContent(file);
        logger.debug("Image validation passed for file: {}", file.getOriginalFilename());
    }

    /**
     * Validates that the file is not null.
     */
    private void validateNotNull(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidImageException("Uploaded file is empty or null");
        }
    }

    /**
     * Validates the file size.
     */
    private void validateFileSize(MultipartFile file) {
        if (file.getSize() > Constants.MAX_FILE_SIZE_BYTES) {
            String message = String.format(
                "File size %d bytes exceeds maximum allowed size of %d bytes",
                file.getSize(),
                Constants.MAX_FILE_SIZE_BYTES
            );
            logger.warn(message);
            throw new FileSizeLimitExceededException(message);
        }
    }

    /**
     * Validates the MIME type.
     */
    private void validateMimeType(MultipartFile file) {
        String mimeType = file.getContentType();
        
        if (mimeType == null || !Constants.SUPPORTED_MIME_TYPES.contains(mimeType.toLowerCase())) {
            String message = String.format(
                "Unsupported image format: %s. Supported formats: %s",
                mimeType,
                Constants.SUPPORTED_MIME_TYPES
            );
            logger.warn(message);
            throw new UnsupportedFormatException(message);
        }
    }

    /**
     * Validates that the file is a valid image by attempting to read it.
     */
    private void validateFileContent(MultipartFile file) {
        try {
            byte[] content = file.getBytes();
            if (content.length == 0) {
                throw new InvalidImageException("Image file content is empty");
            }
            validateImageSignature(content);
        } catch (IOException e) {
            throw new InvalidImageException("Failed to read image file: " + e.getMessage(), e);
        }
    }

    /**
     * Validates the image file signature (magic bytes) to ensure it's a valid image.
     */
    private void validateImageSignature(byte[] content) {
        if (content.length < 4) {
            throw new InvalidImageException("File is too small to be a valid image");
        }

        boolean isValidImage = isJpeg(content) || isPng(content) || isGif(content) || 
                              isBmp(content) || isWebp(content) || isTiff(content);

        if (!isValidImage) {
            throw new InvalidImageException("File is not a valid image or is corrupted");
        }
    }

    private boolean isJpeg(byte[] content) {
        return content.length >= 3 && content[0] == (byte) 0xFF && 
               content[1] == (byte) 0xD8 && content[2] == (byte) 0xFF;
    }

    private boolean isPng(byte[] content) {
        return content.length >= 8 && content[0] == (byte) 0x89 && 
               content[1] == 0x50 && content[2] == 0x4E && content[3] == 0x47;
    }

    private boolean isGif(byte[] content) {
        return content.length >= 6 && content[0] == 0x47 && content[1] == 0x49 && 
               content[2] == 0x46;
    }

    private boolean isBmp(byte[] content) {
        return content.length >= 2 && content[0] == 0x42 && content[1] == 0x4D;
    }

    private boolean isWebp(byte[] content) {
        return content.length >= 12 && content[0] == 0x52 && content[1] == 0x49 && 
               content[2] == 0x46 && content[3] == 0x46 && content[8] == 0x57 && 
               content[9] == 0x45 && content[10] == 0x42 && content[11] == 0x50;
    }

    private boolean isTiff(byte[] content) {
        return content.length >= 4 && ((content[0] == 0x49 && content[1] == 0x49 && 
               content[2] == 0x2A && content[3] == 0x00) ||
               (content[0] == 0x4D && content[1] == 0x4D && 
               content[2] == 0x00 && content[3] == 0x2A));
    }
}
