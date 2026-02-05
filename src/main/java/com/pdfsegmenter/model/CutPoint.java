package com.pdfsegmenter.model;

public class CutPoint implements Comparable<CutPoint> {
    private final int pageNumber;
    private final float yPosition;
    private final int blockIndex;

    public CutPoint(int pageNumber, float yPosition, int blockIndex) {
        this.pageNumber = pageNumber;
        this.yPosition = yPosition;
        this.blockIndex = blockIndex;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public float getYPosition() {
        return yPosition;
    }

    public int getBlockIndex() {
        return blockIndex;
    }

    @Override
    public int compareTo(CutPoint other) {
        int pageCompare = Integer.compare(this.pageNumber, other.pageNumber);
        if (pageCompare != 0) return pageCompare;
        return Float.compare(this.yPosition, other.yPosition);
    }

    @Override
    public String toString() {
        return String.format("CutPoint[page=%d, y=%.2f, blockIdx=%d]", pageNumber, yPosition, blockIndex);
    }
}

