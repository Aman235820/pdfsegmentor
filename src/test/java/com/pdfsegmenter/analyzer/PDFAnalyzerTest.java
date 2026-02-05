package com.pdfsegmenter.analyzer;

import com.pdfsegmenter.model.CutPoint;
import com.pdfsegmenter.model.TextBlock;
import com.pdfsegmenter.model.WhitespaceGap;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PDFAnalyzerTest {
    
    private PDFAnalyzer analyzer;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        analyzer = new PDFAnalyzer();
    }
    
    @Test
    void shouldDetermineCutPointsFromGaps() {
        List<WhitespaceGap> gaps = new ArrayList<>();
        gaps.add(new WhitespaceGap(10f, 100f, 0, 0));
        gaps.add(new WhitespaceGap(50f, 200f, 0, 1));
        gaps.add(new WhitespaceGap(30f, 300f, 0, 2));
        gaps.add(new WhitespaceGap(25f, 400f, 0, 3));
        
        List<CutPoint> cutPoints = analyzer.determineCutPoints(gaps, 2);
        
        assertEquals(2, cutPoints.size());
        assertEquals(200f, cutPoints.get(0).getYPosition());
        assertEquals(300f, cutPoints.get(1).getYPosition());
    }
    
    @Test
    void shouldHandleEmptyGapsList() {
        List<CutPoint> cutPoints = analyzer.determineCutPoints(new ArrayList<>(), 3);
        assertTrue(cutPoints.isEmpty());
    }
    
    @Test
    void shouldHandleZeroCuts() {
        List<WhitespaceGap> gaps = new ArrayList<>();
        gaps.add(new WhitespaceGap(50f, 200f, 0, 1));
        
        List<CutPoint> cutPoints = analyzer.determineCutPoints(gaps, 0);
        assertTrue(cutPoints.isEmpty());
    }
    
    @Test
    void shouldLimitCutsToAvailableGaps() {
        List<WhitespaceGap> gaps = new ArrayList<>();
        gaps.add(new WhitespaceGap(50f, 200f, 0, 0));
        gaps.add(new WhitespaceGap(30f, 300f, 0, 1));
        
        List<CutPoint> cutPoints = analyzer.determineCutPoints(gaps, 5);
        
        assertEquals(2, cutPoints.size());
    }
    
    @Test
    void shouldSortCutPointsByPosition() {
        List<WhitespaceGap> gaps = new ArrayList<>();
        gaps.add(new WhitespaceGap(50f, 400f, 0, 3));
        gaps.add(new WhitespaceGap(60f, 100f, 0, 0));
        gaps.add(new WhitespaceGap(40f, 250f, 0, 1));
        
        List<CutPoint> cutPoints = analyzer.determineCutPoints(gaps, 3);
        
        assertEquals(100f, cutPoints.get(0).getYPosition());
        assertEquals(250f, cutPoints.get(1).getYPosition());
        assertEquals(400f, cutPoints.get(2).getYPosition());
    }
    
    @Test
    void shouldExtractTextBlocksFromDocument() throws IOException {
        File testPdf = createTestPdfWithBlocks(tempDir);
        
        try (PDDocument doc = PDDocument.load(testPdf)) {
            List<TextBlock> blocks = analyzer.extractTextBlocks(doc);
            assertFalse(blocks.isEmpty());
        }
    }
    
    @Test
    void shouldAnalyzePdfFile() throws IOException {
        File testPdf = createTestPdfWithBlocks(tempDir);
        
        PDFAnalyzer.AnalysisResult result = analyzer.analyze(testPdf, 2);
        
        assertNotNull(result);
        assertNotNull(result.getTextBlocks());
        assertNotNull(result.getGaps());
        assertNotNull(result.getCutPoints());
        assertEquals(1, result.getTotalPages());
    }
    
    private File createTestPdfWithBlocks(Path dir) throws IOException {
        File pdfFile = dir.resolve("test.pdf").toFile();
        
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.LETTER);
            doc.addPage(page);
            
            try (PDPageContentStream content = new PDPageContentStream(doc, page)) {
                content.beginText();
                content.setFont(PDType1Font.HELVETICA, 12);
                
                content.newLineAtOffset(50, 700);
                content.showText("First block of text");
                
                content.newLineAtOffset(0, -100);
                content.showText("Second block with large gap above");
                
                content.newLineAtOffset(0, -50);
                content.showText("Third block with smaller gap");
                
                content.endText();
            }
            
            doc.save(pdfFile);
        }
        
        return pdfFile;
    }
}

