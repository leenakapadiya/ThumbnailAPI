package com.thumbnailapi.service;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.thumbnailapi.model.ThumbnailMetadata;

/**
 * Unit tests for ThumbnailGenerator.
 */
class ThumbnailGeneratorTest {

    private ThumbnailGenerator thumbnailGenerator;
    private byte[] testImageBytes;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() throws IOException {
        // Create a simple test image (100x100 PNG)
        BufferedImage testImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = testImage.createGraphics();
        g2d.setColor(Color.RED);
        g2d.fillRect(0, 0, 100, 100);
        g2d.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(testImage, "png", baos);
        testImageBytes = baos.toByteArray();

        thumbnailGenerator = new ThumbnailGenerator();
    }

    @Test
    void testGenerateSingleThumbnail() {
        List<Dimension> dimensions = List.of(new Dimension(50, 50));
        
        List<ThumbnailMetadata> thumbnails = (List<ThumbnailMetadata>) thumbnailGenerator.generateThumbnails(
            testImageBytes, "PNG", dimensions);
        
        assertEquals(1, thumbnails.size());
        ThumbnailMetadata metadata = thumbnails.get(0);
        assertEquals(50, metadata.width());
        assertEquals(50, metadata.height());
        assertEquals("PNG", metadata.format());
        assertGreater(metadata.fileSizeBytes(), 0);
        assertGreater(metadata.processingTimeMs(), 0);
    }

    @Test
    void testGenerateMultipleThumbnails() {
        List<Dimension> dimensions = List.of(
            new Dimension(150, 150),
            new Dimension(300, 300),
            new Dimension(600, 600)
        );
        
        List<ThumbnailMetadata> thumbnails = (List<ThumbnailMetadata>) thumbnailGenerator.generateThumbnails(
            testImageBytes, "PNG", dimensions);
        
        assertEquals(3, thumbnails.size());
        
        // Verify all thumbnails were generated with correct dimensions
        assertTrue(thumbnails.stream().anyMatch(t -> t.width() == 150 && t.height() == 150));
        assertTrue(thumbnails.stream().anyMatch(t -> t.width() == 300 && t.height() == 300));
        assertTrue(thumbnails.stream().anyMatch(t -> t.width() == 600 && t.height() == 600));
    }

    @Test
    void testThumbnailMetadataAccuracy() {
        List<Dimension> dimensions = List.of(new Dimension(150, 150));
        
        List<ThumbnailMetadata> thumbnails = (List<ThumbnailMetadata>) thumbnailGenerator.generateThumbnails(
            testImageBytes, "PNG", dimensions);
        
        ThumbnailMetadata metadata = thumbnails.get(0);
        
        // Verify metadata fields
        assertEquals("small", metadata.size());
        assertEquals(150, metadata.width());
        assertEquals(150, metadata.height());
        assertEquals("PNG", metadata.format());
        assertNotNull(metadata.timestamp());
        assertGreater(metadata.fileSizeBytes(), 0);
        assertGreater(metadata.processingTimeMs(), 0);
    }

    @Test
    void testThumbnailFormatPreservation() {
        List<Dimension> dimensions = List.of(new Dimension(75, 75));
        
        List<ThumbnailMetadata> thumbnails = (List<ThumbnailMetadata>) thumbnailGenerator.generateThumbnails(
            testImageBytes, "JPEG", dimensions);
        
        assertEquals(1, thumbnails.size());
        assertEquals("JPEG", thumbnails.get(0).format());
    }

    private static void assertGreater(long actual, long expected) {
        assertTrue(actual > expected, "Expected " + actual + " to be greater than " + expected);
    }
}
