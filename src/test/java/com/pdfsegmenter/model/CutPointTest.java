package com.pdfsegmenter.model;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CutPointTest {

    @Test
    void shouldReturnCorrectProperties() {
        CutPoint cut = new CutPoint(2, 300f, 5);
        
        assertEquals(2, cut.getPageNumber());
        assertEquals(300f, cut.getYPosition());
        assertEquals(5, cut.getBlockIndex());
    }

    @Test
    void shouldSortByPageThenByY() {
        CutPoint first = new CutPoint(0, 100f, 0);
        CutPoint second = new CutPoint(0, 200f, 1);
        CutPoint third = new CutPoint(1, 50f, 2);
        
        List<CutPoint> cuts = Arrays.asList(third, first, second);
        Collections.sort(cuts);
        
        assertEquals(0, cuts.get(0).getPageNumber());
        assertEquals(100f, cuts.get(0).getYPosition());
        
        assertEquals(0, cuts.get(1).getPageNumber());
        assertEquals(200f, cuts.get(1).getYPosition());
        
        assertEquals(1, cuts.get(2).getPageNumber());
    }

    @Test
    void shouldFormatToString() {
        CutPoint cut = new CutPoint(1, 250f, 3);
        String str = cut.toString();
        
        assertTrue(str.contains("page=1"));
        assertTrue(str.contains("250.00"));
        assertTrue(str.contains("blockIdx=3"));
    }
}

