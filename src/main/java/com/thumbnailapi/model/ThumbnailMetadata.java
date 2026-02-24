package com.thumbnailapi.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Metadata for a single generated thumbnail.
 * 
 * Contains information about the generated thumbnail including its dimensions,
 * file size, format, and processing metrics.
 */
public record ThumbnailMetadata(
    @JsonProperty("size")
    String size,
    
    @JsonProperty("width")
    int width,
    
    @JsonProperty("height")
    int height,
    
    @JsonProperty("format")
    String format,
    
    @JsonProperty("file_size_bytes")
    long fileSizeBytes,
    
    @JsonProperty("timestamp")
    String timestamp,
    
    @JsonProperty("processing_time_ms")
    long processingTimeMs
) {
    /**
     * Creates a ThumbnailMetadata record with automatic timestamp generation.
     *
     * @param size the size identifier (e.g., "small", "medium", "500x500")
     * @param width the width of the thumbnail in pixels
     * @param height the height of the thumbnail in pixels
     * @param format the image format (e.g., "PNG", "JPEG")
     * @param fileSizeBytes the file size in bytes
     * @param processingTimeMs the processing time in milliseconds
     * @return a new ThumbnailMetadata instance with current timestamp
     */
    public static ThumbnailMetadata create(String size, int width, int height, 
                                          String format, long fileSizeBytes, 
                                          long processingTimeMs) {
        String timestamp = LocalDateTime.now()
            .format(DateTimeFormatter.ISO_DATE_TIME);
        return new ThumbnailMetadata(size, width, height, format, fileSizeBytes, 
                                    timestamp, processingTimeMs);
    }
}
