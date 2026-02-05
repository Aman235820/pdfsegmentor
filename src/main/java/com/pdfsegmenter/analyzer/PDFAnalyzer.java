package com.pdfsegmenter.analyzer;

import com.pdfsegmenter.model.CutPoint;
import com.pdfsegmenter.model.TextBlock;
import com.pdfsegmenter.model.WhitespaceGap;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.TextPosition;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class PDFAnalyzer {
    private static final float LINE_GROUPING_THRESHOLD = 3.0f;

    public List<TextBlock> extractTextBlocks(PDDocument document) throws IOException {
        List<TextBlock> allBlocks = new ArrayList<>();
        
        for (int pageNum = 0; pageNum < document.getNumberOfPages(); pageNum++) {
            List<TextBlock> pageBlocks = extractPageTextBlocks(document, pageNum);
            allBlocks.addAll(pageBlocks);
        }
        
        return allBlocks;
    }

    private List<TextBlock> extractPageTextBlocks(PDDocument document, int pageNum) throws IOException {
        TextPositionExtractor extractor = new TextPositionExtractor();
        extractor.setStartPage(pageNum + 1);
        extractor.setEndPage(pageNum + 1);
        extractor.getText(document);
        
        List<TextPosition> positions = extractor.getPositions();
        if (positions.isEmpty()) {
            return Collections.emptyList();
        }
        
        return groupPositionsIntoBlocks(positions, pageNum);
    }

    private List<TextBlock> groupPositionsIntoBlocks(List<TextPosition> positions, int pageNum) {
        Map<Float, List<TextPosition>> lineGroups = new TreeMap<>();
        
        for (TextPosition pos : positions) {
            float y = pos.getY();
            Float matchingLine = findMatchingLine(lineGroups.keySet(), y);
            
            if (matchingLine != null) {
                lineGroups.get(matchingLine).add(pos);
            } else {
                List<TextPosition> newLine = new ArrayList<>();
                newLine.add(pos);
                lineGroups.put(y, newLine);
            }
        }
        
        List<Float> sortedYPositions = new ArrayList<>(lineGroups.keySet());
        Collections.sort(sortedYPositions);
        
        return mergeIntoTextBlocks(sortedYPositions, lineGroups, pageNum);
    }

    private Float findMatchingLine(Set<Float> existingLines, float y) {
        for (Float line : existingLines) {
            if (Math.abs(line - y) < LINE_GROUPING_THRESHOLD) {
                return line;
            }
        }
        return null;
    }

    private List<TextBlock> mergeIntoTextBlocks(List<Float> sortedY, Map<Float, List<TextPosition>> lineGroups, int pageNum) {
        if (sortedY.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<TextBlock> blocks = new ArrayList<>();
        float avgLineSpacing = calculateAverageLineSpacing(sortedY);
        float significantGapThreshold = avgLineSpacing * 1.8f;
        
        float blockStartY = sortedY.get(0);
        float blockEndY = sortedY.get(0);
        StringBuilder blockContent = new StringBuilder();
        blockContent.append(getLineText(lineGroups.get(sortedY.get(0))));
        
        for (int i = 1; i < sortedY.size(); i++) {
            float currentY = sortedY.get(i);
            float gap = currentY - blockEndY;
            
            if (gap > significantGapThreshold) {
                blocks.add(new TextBlock(blockStartY, blockEndY, pageNum, blockContent.toString().trim()));
                blockStartY = currentY;
                blockContent = new StringBuilder();
            }
            
            blockEndY = currentY;
            blockContent.append(" ").append(getLineText(lineGroups.get(currentY)));
        }
        
        blocks.add(new TextBlock(blockStartY, blockEndY, pageNum, blockContent.toString().trim()));
        return blocks;
    }

    private float calculateAverageLineSpacing(List<Float> sortedY) {
        if (sortedY.size() < 2) return 15.0f;
        
        float totalSpacing = 0;
        int count = 0;
        
        for (int i = 1; i < sortedY.size(); i++) {
            float spacing = sortedY.get(i) - sortedY.get(i - 1);
            if (spacing > 0 && spacing < 50) {
                totalSpacing += spacing;
                count++;
            }
        }
        
        return count > 0 ? totalSpacing / count : 15.0f;
    }

    private String getLineText(List<TextPosition> positions) {
        if (positions == null || positions.isEmpty()) return "";
        
        positions.sort(Comparator.comparing(TextPosition::getX));
        StringBuilder sb = new StringBuilder();
        for (TextPosition pos : positions) {
            sb.append(pos.getUnicode());
        }
        return sb.toString();
    }

    public List<WhitespaceGap> findWhitespaceGaps(List<TextBlock> blocks, PDDocument document) {
        List<WhitespaceGap> gaps = new ArrayList<>();
        
        for (int i = 0; i < blocks.size() - 1; i++) {
            TextBlock current = blocks.get(i);
            TextBlock next = blocks.get(i + 1);
            
            float gapSize;
            if (current.getPageNumber() == next.getPageNumber()) {
                gapSize = next.getStartY() - current.getEndY();
            } else {
                PDPage currentPage = document.getPage(current.getPageNumber());
                float pageHeight = currentPage.getMediaBox().getHeight();
                float remainingOnCurrentPage = pageHeight - current.getEndY();
                float startOnNextPage = next.getStartY();
                gapSize = remainingOnCurrentPage + startOnNextPage;
            }
            
            if (gapSize > 0) {
                gaps.add(new WhitespaceGap(gapSize, current.getEndY(), current.getPageNumber(), i));
            }
        }
        
        return gaps;
    }

    public List<CutPoint> determineCutPoints(List<WhitespaceGap> gaps, int numCuts) {
        if (gaps.isEmpty() || numCuts <= 0) {
            return Collections.emptyList();
        }
        
        List<WhitespaceGap> sortedGaps = new ArrayList<>(gaps);
        Collections.sort(sortedGaps);
        
        int actualCuts = Math.min(numCuts, sortedGaps.size());
        List<CutPoint> cutPoints = new ArrayList<>();
        
        for (int i = 0; i < actualCuts; i++) {
            WhitespaceGap gap = sortedGaps.get(i);
            cutPoints.add(new CutPoint(gap.getPageNumber(), gap.getYPosition(), gap.getBlockIndexBefore()));
        }
        
        Collections.sort(cutPoints);
        return cutPoints;
    }

    public AnalysisResult analyze(File pdfFile, int numCuts) throws IOException {
        try (PDDocument document = PDDocument.load(pdfFile)) {
            List<TextBlock> blocks = extractTextBlocks(document);
            List<WhitespaceGap> gaps = findWhitespaceGaps(blocks, document);
            List<CutPoint> cutPoints = determineCutPoints(gaps, numCuts);
            
            return new AnalysisResult(blocks, gaps, cutPoints, document.getNumberOfPages());
        }
    }

    public static class AnalysisResult {
        private final List<TextBlock> textBlocks;
        private final List<WhitespaceGap> gaps;
        private final List<CutPoint> cutPoints;
        private final int totalPages;

        public AnalysisResult(List<TextBlock> textBlocks, List<WhitespaceGap> gaps, 
                             List<CutPoint> cutPoints, int totalPages) {
            this.textBlocks = textBlocks;
            this.gaps = gaps;
            this.cutPoints = cutPoints;
            this.totalPages = totalPages;
        }

        public List<TextBlock> getTextBlocks() { return textBlocks; }
        public List<WhitespaceGap> getGaps() { return gaps; }
        public List<CutPoint> getCutPoints() { return cutPoints; }
        public int getTotalPages() { return totalPages; }
    }
}

