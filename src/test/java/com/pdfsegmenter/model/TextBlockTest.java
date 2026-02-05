package com.pdfsegmenter.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TextBlockTest {

    @Test
    void shouldCalculateHeight() {
        TextBlock block = new TextBlock(100f, 150f, 0, "Test content");
        assertEquals(50f, block.getHeight(), 0.01f);
    }

    @Test
    void shouldHandleNegativeHeightCalculation() {
        TextBlock block = new TextBlock(150f, 100f, 0, "Test content");
        assertEquals(50f, block.getHeight(), 0.01f);
    }

    @Test
    void shouldReturnCorrectProperties() {
        TextBlock block = new TextBlock(100f, 200f, 2, "Sample text");
        
        assertEquals(100f, block.getStartY());
        assertEquals(200f, block.getEndY());
        assertEquals(2, block.getPageNumber());
        assertEquals("Sample text", block.getContent());
    }

    @Test
    void shouldFormatToString() {
        TextBlock block = new TextBlock(100f, 200f, 1, "Test");
        String str = block.toString();
        
        assertTrue(str.contains("page=1"));
        assertTrue(str.contains("100.00"));
        assertTrue(str.contains("200.00"));
    }
}

