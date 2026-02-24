package com.thumbnailapi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot application for the Thumbnail API service.
 * 
 * This application provides a REST API for generating image thumbnails
 * with support for preset and custom dimensions, and comprehensive error handling.
 */
@SpringBootApplication
public class ThumbnailApiApplication {

    private static final Logger logger = LogManager.getLogger(ThumbnailApiApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ThumbnailApiApplication.class, args);
        logger.info("Thumbnail API Service started successfully");
    }

}
