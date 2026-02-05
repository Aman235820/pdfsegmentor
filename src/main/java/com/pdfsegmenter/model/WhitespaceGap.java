package com.pdfsegmenter.model;

public class WhitespaceGap implements Comparable<WhitespaceGap> {
    private final float gapSize;
    private final float yPosition;
    private final int pageNumber;
    private final int blockIndexBefore;

    public WhitespaceGap(float gapSize, float yPosition, int pageNumber, int blockIndexBefore) {
        this.gapSize = gapSize;
        this.yPosition = yPosition;
        this.pageNumber = pageNumber;
        this.blockIndexBefore = blockIndexBefore;
    }

    public float getGapSize() {
        return gapSize;
    }

    public float getYPosition() {
        return yPosition;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public int getBlockIndexBefore() {
        return blockIndexBefore;
    }

    @Override
    public int compareTo(WhitespaceGap other) {
        return Float.compare(other.gapSize, this.gapSize);
    }

    @Override
    public String toString() {
        return String.format("Gap[size=%.2f, page=%d, y=%.2f]", gapSize, pageNumber, yPosition);
    }
}

