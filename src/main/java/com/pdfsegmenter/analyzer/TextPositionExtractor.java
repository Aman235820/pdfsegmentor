package com.pdfsegmenter.analyzer;

import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TextPositionExtractor extends PDFTextStripper {
    private final List<TextPosition> positions = new ArrayList<>();
    private int currentPage = 0;

    public TextPositionExtractor() throws IOException {
        super();
    }

    @Override
    protected void processTextPosition(TextPosition text) {
        positions.add(text);
    }

    @Override
    protected void startPage(org.apache.pdfbox.pdmodel.PDPage page) throws IOException {
        currentPage++;
        super.startPage(page);
    }

    public List<TextPosition> getPositions() {
        return positions;
    }

    public void clear() {
        positions.clear();
        currentPage = 0;
    }
}

