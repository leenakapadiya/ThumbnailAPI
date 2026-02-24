package com.thumbnailapi.exception;

/**
 * Exception thrown when an image format is not supported by the service.
 */
public class UnsupportedFormatException extends RuntimeException {

    public UnsupportedFormatException(String message) {
        super(message);
    }

    public UnsupportedFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
