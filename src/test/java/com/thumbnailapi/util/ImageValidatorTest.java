package com.thumbnailapi.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.thumbnailapi.exception.FileSizeLimitExceededException;
import com.thumbnailapi.exception.InvalidImageException;
import com.thumbnailapi.exception.UnsupportedFormatException;

/**
 * Unit tests for ImageValidator.
 */
class ImageValidatorTest {

    private ImageValidator imageValidator;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        imageValidator = new ImageValidator();
    }

    @Test
    void testValidateValidPngImage() {
        // PNG magic bytes
        byte[] pngBytes = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
        MultipartFile file = new MockMultipartFile("file", "test.png", "image/png", pngBytes);
        
        assertDoesNotThrow(() -> imageValidator.validate(file));
    }

    @Test
    void testValidateValidJpegImage() {
        // JPEG magic bytes
        byte[] jpegBytes = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00};
        MultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", jpegBytes);
        
        assertDoesNotThrow(() -> imageValidator.validate(file));
    }

    @Test
    void testValidateEmptyFile() {
        MultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[0]);
        
        var exception = assertThrows(InvalidImageException.class, () -> imageValidator.validate(file));
        assertNotNull(exception);
    }

    @Test
    void testValidateUnsupportedFormat() {
        byte[] data = "dummy text file".getBytes();
        MultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", data);
        
        var exception = assertThrows(UnsupportedFormatException.class, () -> imageValidator.validate(file));
        assertNotNull(exception);
    }

    @Test
    void testValidateFileSizeExceeded() {
        byte[] largeFile = new byte[(int) Constants.MAX_FILE_SIZE_BYTES + 1];
        MultipartFile file = new MockMultipartFile("file", "test.png", "image/png", largeFile);
        
        var exception = assertThrows(FileSizeLimitExceededException.class, () -> imageValidator.validate(file));
        assertNotNull(exception);
    }

    @Test
    void testValidateCorruptImageData() {
        // Random bytes that aren't valid image data
        byte[] data = "this is not an image".getBytes();
        MultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", data);
        
        var exception = assertThrows(InvalidImageException.class, () -> imageValidator.validate(file));
        assertNotNull(exception);
    }
}
