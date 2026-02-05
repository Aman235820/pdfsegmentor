package com.pdfsegmenter.model;

public class TextBlock {
    private final float startY;
    private final float endY;
    private final int pageNumber;
    private final String content;

    public TextBlock(float startY, float endY, int pageNumber, String content) {
        this.startY = startY;
        this.endY = endY;
        this.pageNumber = pageNumber;
        this.content = content;
    }

    public float getStartY() {
        return startY;
    }

    public float getEndY() {
        return endY;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public String getContent() {
        return content;
    }

    public float getHeight() {
        return Math.abs(endY - startY);
    }

    @Override
    public String toString() {
        return String.format("TextBlock[page=%d, y=%.2f-%.2f]", pageNumber, startY, endY);
    }
}

