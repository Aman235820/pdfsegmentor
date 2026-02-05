package com.pdfsegmenter.segmenter;

import com.pdfsegmenter.analyzer.PDFAnalyzer;
import com.pdfsegmenter.model.CutPoint;
import com.pdfsegmenter.model.TextBlock;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class PDFSegmenter {
    private final PDFAnalyzer analyzer;

    public PDFSegmenter() {
        this.analyzer = new PDFAnalyzer();
    }

    public List<File> segment(File inputFile, int numCuts, File outputDir) throws IOException {
        validateInputs(inputFile, numCuts, outputDir);
        
        PDFAnalyzer.AnalysisResult analysis = analyzer.analyze(inputFile, numCuts);
        List<CutPoint> cutPoints = analysis.getCutPoints();
        
        if (cutPoints.isEmpty()) {
            return copyAsIs(inputFile, outputDir);
        }
        
        return performSegmentation(inputFile, cutPoints, analysis.getTextBlocks(), outputDir);
    }

    private void validateInputs(File inputFile, int numCuts, File outputDir) throws IOException {
        if (!inputFile.exists()) {
            throw new IOException("Input file does not exist: " + inputFile.getAbsolutePath());
        }
        if (!inputFile.getName().toLowerCase().endsWith(".pdf")) {
            throw new IOException("Input file must be a PDF: " + inputFile.getName());
        }
        if (numCuts < 1) {
            throw new IllegalArgumentException("Number of cuts must be at least 1");
        }
        if (!outputDir.exists()) {
            Files.createDirectories(outputDir.toPath());
        }
    }

    private List<File> copyAsIs(File inputFile, File outputDir) throws IOException {
        List<File> outputs = new ArrayList<>();
        String baseName = getBaseName(inputFile.getName());
        File outputFile = new File(outputDir, baseName + "_segment_1.pdf");
        Files.copy(inputFile.toPath(), outputFile.toPath());
        outputs.add(outputFile);
        return outputs;
    }

    private List<File> performSegmentation(File inputFile, List<CutPoint> cutPoints, 
                                           List<TextBlock> blocks, File outputDir) throws IOException {
        List<File> outputFiles = new ArrayList<>();
        String baseName = getBaseName(inputFile.getName());
        
        try (PDDocument sourceDoc = PDDocument.load(inputFile)) {
            List<Segment> segments = calculateSegments(cutPoints, blocks, sourceDoc.getNumberOfPages());
            
            for (int i = 0; i < segments.size(); i++) {
                Segment seg = segments.get(i);
                File outputFile = new File(outputDir, baseName + "_segment_" + (i + 1) + ".pdf");
                
                createSegmentPdf(sourceDoc, seg, outputFile);
                outputFiles.add(outputFile);
            }
        }
        
        return outputFiles;
    }

    private List<Segment> calculateSegments(List<CutPoint> cutPoints, List<TextBlock> blocks, int totalPages) {
        List<Segment> segments = new ArrayList<>();
        
        int startBlockIdx = 0;
        int startPage = 0;
        float startY = 0;
        
        for (CutPoint cut : cutPoints) {
            int endBlockIdx = cut.getBlockIndex();
            int endPage = cut.getPageNumber();
            float endY = cut.getYPosition();
            
            segments.add(new Segment(startPage, endPage, startY, endY, startBlockIdx, endBlockIdx));
            
            startBlockIdx = endBlockIdx + 1;
            startPage = endPage;
            startY = endY;
        }
        
        int lastBlockIdx = blocks.isEmpty() ? 0 : blocks.size() - 1;
        int lastPage = totalPages - 1;
        segments.add(new Segment(startPage, lastPage, startY, Float.MAX_VALUE, startBlockIdx, lastBlockIdx));
        
        return segments;
    }

    private void createSegmentPdf(PDDocument sourceDoc, Segment segment, File outputFile) throws IOException {
        try (PDDocument newDoc = new PDDocument()) {
            for (int pageNum = segment.startPage; pageNum <= segment.endPage && pageNum < sourceDoc.getNumberOfPages(); pageNum++) {
                PDPage sourcePage = sourceDoc.getPage(pageNum);
                PDPage newPage = new PDPage(sourcePage.getMediaBox());
                
                newDoc.importPage(sourcePage);
            }
            
            if (newDoc.getNumberOfPages() == 0) {
                PDPage emptyPage = new PDPage(PDRectangle.LETTER);
                newDoc.addPage(emptyPage);
            }
            
            newDoc.save(outputFile);
        }
    }

    private String getBaseName(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(0, lastDot) : fileName;
    }

    private static class Segment {
        final int startPage;
        final int endPage;
        final float startY;
        final float endY;
        final int startBlockIdx;
        final int endBlockIdx;

        Segment(int startPage, int endPage, float startY, float endY, int startBlockIdx, int endBlockIdx) {
            this.startPage = startPage;
            this.endPage = endPage;
            this.startY = startY;
            this.endY = endY;
            this.startBlockIdx = startBlockIdx;
            this.endBlockIdx = endBlockIdx;
        }
    }
}

