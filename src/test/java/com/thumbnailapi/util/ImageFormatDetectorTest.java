package com.thumbnailapi.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for ImageFormatDetector.
 */
class ImageFormatDetectorTest {

    private ImageFormatDetector formatDetector;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        formatDetector = new ImageFormatDetector();
    }

    @Test
    void testDetectJpegFormat() {
        byte[] jpegSignature = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00};
        
        String format = formatDetector.detectFormat(jpegSignature);
        
        assertEquals("JPEG", format);
    }

    @Test
    void testDetectPngFormat() {
        byte[] pngSignature = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47};
        
        String format = formatDetector.detectFormat(pngSignature);
        
        assertEquals("PNG", format);
    }

    @Test
    void testDetectGifFormat() {
        byte[] gifSignature = new byte[]{0x47, 0x49, 0x46};
        
        String format = formatDetector.detectFormat(gifSignature);
        
        assertEquals("GIF", format);
    }

    @Test
    void testDetectBmpFormat() {
        byte[] bmpSignature = new byte[]{0x42, 0x4D};
        
        String format = formatDetector.detectFormat(bmpSignature);
        
        assertEquals("BMP", format);
    }

    @Test
    void testDetectWebpFormat() {
        byte[] webpSignature = new byte[]{0x52, 0x49, 0x46, 0x46, 0x00, 0x00, 0x00, 0x00,
                                          0x57, 0x45, 0x42, 0x50};
        
        String format = formatDetector.detectFormat(webpSignature);
        
        assertEquals("WEBP", format);
    }

    @Test
    void testDetectTiffFormat() {
        byte[] tiffSignature = new byte[]{0x49, 0x49, 0x2A, 0x00};
        
        String format = formatDetector.detectFormat(tiffSignature);
        
        assertEquals("TIFF", format);
    }

    @Test
    void testDetectUnknownFormat() {
        byte[] unknownSignature = new byte[]{0x00, 0x00, 0x00, 0x00};
        
        String format = formatDetector.detectFormat(unknownSignature);
        
        assertEquals("UNKNOWN", format);
    }
}
