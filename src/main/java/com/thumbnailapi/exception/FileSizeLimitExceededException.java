package com.thumbnailapi.exception;

/**
 * Exception thrown when an uploaded file exceeds the size limit.
 */
public class FileSizeLimitExceededException extends RuntimeException {

    public FileSizeLimitExceededException(String message) {
        super(message);
    }

    public FileSizeLimitExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}
