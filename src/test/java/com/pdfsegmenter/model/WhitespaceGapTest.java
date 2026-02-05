package com.pdfsegmenter.model;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WhitespaceGapTest {

    @Test
    void shouldReturnCorrectProperties() {
        WhitespaceGap gap = new WhitespaceGap(50f, 200f, 1, 5);
        
        assertEquals(50f, gap.getGapSize());
        assertEquals(200f, gap.getYPosition());
        assertEquals(1, gap.getPageNumber());
        assertEquals(5, gap.getBlockIndexBefore());
    }

    @Test
    void shouldSortByGapSizeDescending() {
        WhitespaceGap small = new WhitespaceGap(10f, 100f, 0, 0);
        WhitespaceGap medium = new WhitespaceGap(25f, 200f, 0, 1);
        WhitespaceGap large = new WhitespaceGap(50f, 300f, 0, 2);
        
        List<WhitespaceGap> gaps = Arrays.asList(small, medium, large);
        Collections.sort(gaps);
        
        assertEquals(50f, gaps.get(0).getGapSize());
        assertEquals(25f, gaps.get(1).getGapSize());
        assertEquals(10f, gaps.get(2).getGapSize());
    }

    @Test
    void shouldFormatToString() {
        WhitespaceGap gap = new WhitespaceGap(30f, 150f, 2, 3);
        String str = gap.toString();
        
        assertTrue(str.contains("size=30.00"));
        assertTrue(str.contains("page=2"));
    }
}

