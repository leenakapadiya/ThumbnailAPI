package com.thumbnailapi.exception;

/**
 * Exception thrown when invalid dimensions are provided for thumbnail generation.
 */
public class InvalidDimensionsException extends RuntimeException {

    public InvalidDimensionsException(String message) {
        super(message);
    }

    public InvalidDimensionsException(String message, Throwable cause) {
        super(message, cause);
    }
}
