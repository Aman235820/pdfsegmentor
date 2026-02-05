package com.pdfsegmenter.segmenter;

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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PDFSegmenterTest {
    
    private PDFSegmenter segmenter;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        segmenter = new PDFSegmenter();
    }
    
    @Test
    void shouldCreateSegments() throws IOException {
        File inputPdf = createTestPdf("input.pdf");
        File outputDir = tempDir.resolve("output").toFile();
        
        List<File> segments = segmenter.segment(inputPdf, 2, outputDir);
        
        assertFalse(segments.isEmpty());
        assertTrue(segments.size() <= 3);
        
        for (File segment : segments) {
            assertTrue(segment.exists());
            assertTrue(segment.getName().endsWith(".pdf"));
        }
    }
    
    @Test
    void shouldThrowExceptionForNonExistentFile() {
        File nonExistent = new File("nonexistent.pdf");
        File outputDir = tempDir.resolve("output").toFile();
        
        assertThrows(IOException.class, () -> 
            segmenter.segment(nonExistent, 2, outputDir)
        );
    }
    
    @Test
    void shouldThrowExceptionForNonPdfFile() throws IOException {
        File textFile = tempDir.resolve("test.txt").toFile();
        textFile.createNewFile();
        File outputDir = tempDir.resolve("output").toFile();
        
        assertThrows(IOException.class, () -> 
            segmenter.segment(textFile, 2, outputDir)
        );
    }
    
    @Test
    void shouldThrowExceptionForInvalidCutCount() throws IOException {
        File inputPdf = createTestPdf("input.pdf");
        File outputDir = tempDir.resolve("output").toFile();
        
        assertThrows(IllegalArgumentException.class, () -> 
            segmenter.segment(inputPdf, 0, outputDir)
        );
    }
    
    @Test
    void shouldCreateOutputDirectoryIfNotExists() throws IOException {
        File inputPdf = createTestPdf("input.pdf");
        File outputDir = tempDir.resolve("new_dir").toFile();
        
        assertFalse(outputDir.exists());
        
        segmenter.segment(inputPdf, 1, outputDir);
        
        assertTrue(outputDir.exists());
    }
    
    @Test
    void shouldNameSegmentsSequentially() throws IOException {
        File inputPdf = createTestPdf("document.pdf");
        File outputDir = tempDir.resolve("output").toFile();
        
        List<File> segments = segmenter.segment(inputPdf, 2, outputDir);
        
        for (int i = 0; i < segments.size(); i++) {
            assertTrue(segments.get(i).getName().contains("segment_" + (i + 1)));
        }
    }
    
    @Test
    void shouldHandleSinglePagePdf() throws IOException {
        File inputPdf = createSimplePdf("single.pdf");
        File outputDir = tempDir.resolve("output").toFile();
        
        List<File> segments = segmenter.segment(inputPdf, 1, outputDir);
        
        assertFalse(segments.isEmpty());
    }
    
    private File createTestPdf(String name) throws IOException {
        File pdfFile = tempDir.resolve(name).toFile();
        
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.LETTER);
            doc.addPage(page);
            
            try (PDPageContentStream content = new PDPageContentStream(doc, page)) {
                content.beginText();
                content.setFont(PDType1Font.HELVETICA, 12);
                
                content.newLineAtOffset(50, 700);
                content.showText("Header Section");
                
                content.newLineAtOffset(0, -100);
                content.showText("Main content paragraph one");
                
                content.newLineAtOffset(0, -80);
                content.showText("Main content paragraph two");
                
                content.newLineAtOffset(0, -100);
                content.showText("Footer Section");
                
                content.endText();
            }
            
            doc.save(pdfFile);
        }
        
        return pdfFile;
    }
    
    private File createSimplePdf(String name) throws IOException {
        File pdfFile = tempDir.resolve(name).toFile();
        
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.LETTER);
            doc.addPage(page);
            
            try (PDPageContentStream content = new PDPageContentStream(doc, page)) {
                content.beginText();
                content.setFont(PDType1Font.HELVETICA, 12);
                content.newLineAtOffset(50, 700);
                content.showText("Simple PDF content");
                content.endText();
            }
            
            doc.save(pdfFile);
        }
        
        return pdfFile;
    }
}

