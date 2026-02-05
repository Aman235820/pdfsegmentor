# PDF Content Segmenter

A Java application that segments PDF documents into distinct sections based on vertical whitespace between text blocks.

## Features

- Analyzes PDF documents to identify text blocks and whitespace gaps
- Segments PDFs based on the largest vertical white spaces
- Produces multiple output PDF files, one for each segment
- Uses Apache PDFBox for PDF manipulation (no image processing)

## Requirements

- Java 11 or higher
- Maven 3.6+

## Build

Using Maven:
```bash
mvn clean package
```

Or using the Maven wrapper (no Maven installation required):
```bash
# Windows
mvnw.cmd clean package

# Linux/Mac
./mvnw clean package
```

This creates an executable JAR with all dependencies at `target/pdf-segmenter-1.0.0.jar`

## Usage

```bash
java -jar target/pdf-segmenter-1.0.0.jar <input.pdf> <num_cuts> [output_dir]
```

### Arguments

| Argument | Required | Description |
|----------|----------|-------------|
| input.pdf | Yes | Path to the PDF file to segment |
| num_cuts | Yes | Number of cuts to make (creates num_cuts + 1 segments) |
| output_dir | No | Output directory for segments (defaults to `<input_name>_segments/`) |

### Examples

Segment a PDF into 4 parts (3 cuts):
```bash
java -jar target/pdf-segmenter-1.0.0.jar document.pdf 3
```

Specify custom output directory:
```bash
java -jar target/pdf-segmenter-1.0.0.jar report.pdf 2 ./output
```

## How It Works

1. **Text Extraction**: Uses PDFBox's text extraction to get the Y-positions of all text elements
2. **Block Detection**: Groups text elements into logical blocks based on line spacing
3. **Gap Analysis**: Calculates vertical gaps between consecutive text blocks
4. **Cut Point Selection**: Selects the N largest gaps as cut points
5. **Segmentation**: Creates separate PDF files for each segment

## Project Structure

```
src/
├── main/java/com/pdfsegmenter/
│   ├── App.java                    # CLI entry point
│   ├── analyzer/
│   │   ├── PDFAnalyzer.java        # Core analysis logic
│   │   └── TextPositionExtractor.java
│   ├── model/
│   │   ├── TextBlock.java
│   │   ├── WhitespaceGap.java
│   │   └── CutPoint.java
│   └── segmenter/
│       └── PDFSegmenter.java       # PDF splitting logic
└── test/java/com/pdfsegmenter/
    ├── analyzer/
    │   └── PDFAnalyzerTest.java
    ├── model/
    │   ├── TextBlockTest.java
    │   ├── WhitespaceGapTest.java
    │   └── CutPointTest.java
    └── segmenter/
        └── PDFSegmenterTest.java
```

## Running Tests

```bash
mvn test
```

## Design Decisions

- **No Image Processing**: Relies entirely on PDFBox's text extraction API to analyze document structure
- **Significant Whitespace Detection**: Uses a dynamic threshold (1.8x average line spacing) to identify meaningful gaps
- **Page Boundary Handling**: Gaps spanning page boundaries are calculated by combining remaining space on current page with starting position on next page
- **Graceful Degradation**: If fewer gaps exist than requested cuts, the application makes as many cuts as possible

## Error Handling

- Invalid file paths produce descriptive error messages
- Non-PDF files are rejected with appropriate errors
- Empty or malformed PDFs are handled gracefully
- Zero or negative cut counts are validated

# pdfsegmentor
