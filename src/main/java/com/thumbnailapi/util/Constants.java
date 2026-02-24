package com.thumbnailapi.util;

import java.util.Set;

/**
 * Constants used throughout the Thumbnail API application.
 */
public final class Constants {

    private Constants() {
        throw new AssertionError("Cannot instantiate constants class");
    }

    // File size limits
    public static final long MAX_FILE_SIZE_BYTES = 20 * 1024 * 1024; // 20MB

    // Image format constants
    public static final Set<String> SUPPORTED_FORMATS = Set.of(
        "JPEG", "JPG", "PNG", "WEBP", "GIF", "BMP", "TIFF"
    );

    public static final Set<String> SUPPORTED_MIME_TYPES = Set.of(
        "image/jpeg",
        "image/png",
        "image/webp",
        "image/gif",
        "image/bmp",
        "image/tiff"
    );

    // Preset thumbnail dimensions
    public static final int PRESET_SMALL = 150;
    public static final int PRESET_MEDIUM = 300;
    public static final int PRESET_LARGE = 600;

    // Dimension validation
    public static final int MIN_DIMENSION = 16;
    public static final int MAX_DIMENSION = 2000;

    // API endpoints
    public static final String API_VERSION = "v1";
    public static final String BASE_API_PATH = "/api/" + API_VERSION;

    // Response constants
    public static final String SUCCESS_RESPONSE = "success";
    public static final String ERROR_RESPONSE = "error";

}
