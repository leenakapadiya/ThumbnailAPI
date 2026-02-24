package com.thumbnailapi.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response object for thumbnail generation requests.
 * 
 * Contains metadata about the original image and all generated thumbnails.
 */
public record ThumbnailResponse(
    @JsonProperty("original_filename")
    String originalFilename,
    
    @JsonProperty("original_format")
    String originalFormat,
    
    @JsonProperty("original_width")
    int originalWidth,
    
    @JsonProperty("original_height")
    int originalHeight,
    
    @JsonProperty("original_file_size_bytes")
    long originalFileSizeBytes,
    
    @JsonProperty("thumbnails")
    List<ThumbnailMetadata> thumbnails
) {
    public ThumbnailResponse {
        thumbnails = thumbnails == null ? List.of() : List.copyOf(thumbnails);
    }

    /**
     * Creates a new ThumbnailResponse builder.
     *
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for ThumbnailResponse.
     */
    public static class Builder {
        private String originalFilename;
        private String originalFormat;
        private int originalWidth;
        private int originalHeight;
        private long originalFileSizeBytes;
        private List<ThumbnailMetadata> thumbnails;

        public Builder originalFilename(String filename) {
            this.originalFilename = filename;
            return this;
        }

        public Builder originalFormat(String format) {
            this.originalFormat = format;
            return this;
        }

        public Builder originalDimensions(int width, int height) {
            this.originalWidth = width;
            this.originalHeight = height;
            return this;
        }

        public Builder originalFileSizeBytes(long fileSize) {
            this.originalFileSizeBytes = fileSize;
            return this;
        }

        public Builder thumbnails(List<ThumbnailMetadata> thumbnails) {
            this.thumbnails = thumbnails == null ? List.of() : List.copyOf(thumbnails);
            return this;
        }

        public ThumbnailResponse build() {
            return new ThumbnailResponse(
                originalFilename,
                originalFormat,
                originalWidth,
                originalHeight,
                originalFileSizeBytes,
                thumbnails
            );
        }
    }
}
