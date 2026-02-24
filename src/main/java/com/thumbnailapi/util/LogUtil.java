package com.thumbnailapi.util;

/**
 * Utility methods for safe logging.
 */
public final class LogUtil {

    private LogUtil() {
        throw new AssertionError("Cannot instantiate utility class");
    }

    /**
     * Sanitizes a user-supplied string for safe inclusion in log messages.
     * Replaces newline and carriage-return characters to prevent log injection (CWE-117).
     *
     * @param value the string to sanitize; may be null
     * @return sanitized string, never null
     */
    public static String sanitizeForLog(String value) {
        if (value == null) {
            return "(null)";
        }
        return value.replaceAll("[\r\n]", "_");
    }
}
