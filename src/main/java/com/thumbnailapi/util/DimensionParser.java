package com.thumbnailapi.util;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.thumbnailapi.exception.InvalidDimensionsException;

/**
 * Parses and validates thumbnail dimensions.
 * 
 * Supports preset sizes (small, medium, large) and custom dimensions (e.g., 500x500).
 */
@Component
public class DimensionParser {

    private static final Logger logger = LogManager.getLogger(DimensionParser.class);
    private static final Pattern DIMENSION_PATTERN = Pattern.compile("^\\d+x\\d+$");

    /**
     * Parses dimension specifications into actual Dimension objects.
     * 
     * @param sizesParam comma-separated string of sizes (e.g., "small,medium,500x500")
     * @return list of Dimension objects
     * @throws InvalidDimensionsException if any dimension is invalid
     */
    public List<Dimension> parseDimensions(String sizesParam) {
        if (!StringUtils.hasText(sizesParam)) {
            logger.warn("No sizes parameter provided, using defaults");
            return getDefaultDimensions();
        }

        List<Dimension> dimensions = new ArrayList<>();
        Set<Dimension> uniqueDimensions = new LinkedHashSet<>();

        String[] sizes = sizesParam.split(",");

        if (sizes.length > Constants.MAX_THUMBNAIL_SIZES) {
            throw new InvalidDimensionsException(
                String.format("Too many thumbnail sizes requested: %d. Maximum allowed: %d",
                    sizes.length, Constants.MAX_THUMBNAIL_SIZES)
            );
        }

        for (String size : sizes) {
            String trimmedSize = size.trim();
            Dimension dimension = parseSingleDimension(trimmedSize);
            uniqueDimensions.add(dimension);
        }

        dimensions.addAll(uniqueDimensions);
        logger.debug("Parsed dimensions: {}", dimensions);
        
        return dimensions;
    }

    /**
     * Parses a single dimension specification.
     */
    private Dimension parseSingleDimension(String size) {
        if (!StringUtils.hasText(size)) {
            throw new InvalidDimensionsException("Empty dimension specification");
        }

        String lowerSize = size.toLowerCase();

        return switch (lowerSize) {
            case "small" -> new Dimension(Constants.PRESET_SMALL, Constants.PRESET_SMALL);
            case "medium" -> new Dimension(Constants.PRESET_MEDIUM, Constants.PRESET_MEDIUM);
            case "large" -> new Dimension(Constants.PRESET_LARGE, Constants.PRESET_LARGE);
            default -> parseCustomDimension(size);
        };
    }

    /**
     * Parses custom dimension in format "WIDTHxHEIGHT".
     */
    private Dimension parseCustomDimension(String size) {
        if (!DIMENSION_PATTERN.matcher(size).matches()) {
            throw new InvalidDimensionsException(
                String.format("Invalid dimension format: '%s'. Expected format: WIDTHxHEIGHT (e.g., 500x500)", size)
            );
        }

        String[] parts = size.split("x");
        try {
            int width = Integer.parseInt(parts[0]);
            int height = Integer.parseInt(parts[1]);
            
            validateDimensionValues(width, height);
            
            return new Dimension(width, height);
        } catch (NumberFormatException e) {
            throw new InvalidDimensionsException(
                String.format("Dimension values must be valid integers: %s", size), e
            );
        }
    }

    /**
     * Validates that dimension values are within acceptable range.
     */
    private void validateDimensionValues(int width, int height) {
        if (width < Constants.MIN_DIMENSION || width > Constants.MAX_DIMENSION) {
            throw new InvalidDimensionsException(
                String.format("Width %d is outside valid range [%d, %d]",
                    width, Constants.MIN_DIMENSION, Constants.MAX_DIMENSION)
            );
        }

        if (height < Constants.MIN_DIMENSION || height > Constants.MAX_DIMENSION) {
            throw new InvalidDimensionsException(
                String.format("Height %d is outside valid range [%d, %d]",
                    height, Constants.MIN_DIMENSION, Constants.MAX_DIMENSION)
            );
        }
    }

    /**
     * Returns default dimensions if none are specified.
     */
    private List<Dimension> getDefaultDimensions() {
        return List.of(
            new Dimension(Constants.PRESET_SMALL, Constants.PRESET_SMALL),
            new Dimension(Constants.PRESET_MEDIUM, Constants.PRESET_MEDIUM),
            new Dimension(Constants.PRESET_LARGE, Constants.PRESET_LARGE)
        );
    }
}
