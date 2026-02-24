package com.thumbnailapi.controller;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for ThumbnailController.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ThumbnailControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private byte[] testImageBytes;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() throws IOException {
        // Create a test image (200x200 PNG)
        BufferedImage testImage = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = testImage.createGraphics();
        g2d.setColor(Color.BLUE);
        g2d.fillRect(0, 0, 200, 200);
        g2d.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(testImage, "png", baos);
        testImageBytes = baos.toByteArray();
    }

    @Test
    void testGenerateThumbnailsWithDefaultSizes() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.png",
            "image/png",
            testImageBytes
        );

        mockMvc.perform(multipart("/api/v1/thumbnails").file(file))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.original_filename").value("test.png"))
            .andExpect(jsonPath("$.original_format").value("PNG"))
            .andExpect(jsonPath("$.original_width").value(200))
            .andExpect(jsonPath("$.original_height").value(200))
            .andExpect(jsonPath("$.thumbnails.length()").value(3));
    }

    @Test
    void testGenerateThumbnailsWithCustomSizes() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.png",
            "image/png",
            testImageBytes
        );

        mockMvc.perform(multipart("/api/v1/thumbnails")
                .file(file)
                .param("sizes", "small,500x500"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.thumbnails.length()").value(2));
    }

    @Test
    void testGenerateThumbnailsWithInvalidFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.txt",
            "text/plain",
            "this is not an image".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/thumbnails").file(file))
            .andExpect(status().isUnsupportedMediaType())
            .andExpect(jsonPath("$.error").value("Unsupported Format"));
    }

    @Test
    void testGenerateThumbnailsWithEmptyFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.png",
            "image/png",
            new byte[0]
        );

        mockMvc.perform(multipart("/api/v1/thumbnails").file(file))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Invalid Image"));
    }

    @Test
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/thumbnails/health"))
            .andExpect(status().isOk());
    }

    @Test
    void testInfoEndpoint() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/thumbnails/info"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Thumbnail API Service"))
            .andExpect(jsonPath("$.version").value("1.0.0"));
    }
}
