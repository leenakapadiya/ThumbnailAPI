package com.thumbnailapi.util;

import java.awt.Dimension;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.thumbnailapi.exception.InvalidDimensionsException;

/**
 * Unit tests for DimensionParser.
 */
class DimensionParserTest {

    private DimensionParser dimensionParser;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        dimensionParser = new DimensionParser();
    }

    @Test
    void testParsePresetDimensions() {
        List<Dimension> dimensions = (List<Dimension>) dimensionParser.parseDimensions("small,medium,large");
        
        assertEquals(3, dimensions.size());
        assertTrue(dimensions.stream().anyMatch(d -> d.width == 150 && d.height == 150));
        assertTrue(dimensions.stream().anyMatch(d -> d.width == 300 && d.height == 300));
        assertTrue(dimensions.stream().anyMatch(d -> d.width == 600 && d.height == 600));
    }

    @Test
    void testParseCustomDimensions() {
        List<Dimension> dimensions = (List<Dimension>) dimensionParser.parseDimensions("500x500,800x600");
        
        assertEquals(2, dimensions.size());
        assertTrue(dimensions.stream().anyMatch(d -> d.width == 500 && d.height == 500));
        assertTrue(dimensions.stream().anyMatch(d -> d.width == 800 && d.height == 600));
    }

    @Test
    void testParseMixedDimensions() {
        List<Dimension> dimensions = (List<Dimension>) dimensionParser.parseDimensions("small,500x500,large");
        
        assertEquals(3, dimensions.size());
        assertTrue(dimensions.stream().anyMatch(d -> d.width == 150 && d.height == 150));
        assertTrue(dimensions.stream().anyMatch(d -> d.width == 500 && d.height == 500));
        assertTrue(dimensions.stream().anyMatch(d -> d.width == 600 && d.height == 600));
    }

    @Test
    void testParseEmptyString() {
        List<Dimension> dimensions = (List<Dimension>) dimensionParser.parseDimensions("");
        
        // Should return defaults
        assertEquals(3, dimensions.size());
    }

    @Test
    void testParseNullString() {
        List<Dimension> dimensions = (List<Dimension>) dimensionParser.parseDimensions(null);
        
        // Should return defaults
        assertEquals(3, dimensions.size());
    }

    @Test
    void testParseInvalidDimensionFormat() {
        var exception = assertThrows(InvalidDimensionsException.class, () -> 
            dimensionParser.parseDimensions("500"));
        assertNotNull(exception);
    }

    @Test
    void testParseInvalidDimensionValues() {
        var exception = assertThrows(InvalidDimensionsException.class, () -> 
            dimensionParser.parseDimensions("abc x def"));
        assertNotNull(exception);
    }

    @Test
    void testParseDimensionOutOfRange() {
        var exception1 = assertThrows(InvalidDimensionsException.class, () -> 
            dimensionParser.parseDimensions("5x5")); // Below minimum
        assertNotNull(exception1);
            
        var exception2 = assertThrows(InvalidDimensionsException.class, () -> 
            dimensionParser.parseDimensions("3000x3000")); // Above maximum
        assertNotNull(exception2);
    }

    @Test
    void testParseRemovesDuplicates() {
        List<Dimension> dimensions = (List<Dimension>) dimensionParser.parseDimensions("small,small,medium,500x500,500x500");
        
        // Should have 3 unique dimensions
        assertEquals(3, dimensions.size());
    }

    @Test
    void testParseWithSpaces() {
        List<Dimension> dimensions = (List<Dimension>) dimensionParser.parseDimensions("small , medium , 500x500");
        
        assertEquals(3, dimensions.size());
    }
}
