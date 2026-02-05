package com.pdfsegmenter;

import com.pdfsegmenter.segmenter.PDFSegmenter;

import java.io.File;
import java.util.List;

public class App {
    
    public static void main(String[] args) {
        if (args.length < 2) {
            printUsage();
            System.exit(1);
        }
        
        String inputPath = args[0];
        int numCuts;
        
        try {
            numCuts = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.err.println("Error: Number of cuts must be a valid integer");
            System.exit(1);
            return;
        }
        
        String outputPath = args.length > 2 ? args[2] : getDefaultOutputDir(inputPath);
        
        try {
            File inputFile = new File(inputPath);
            File outputDir = new File(outputPath);
            
            System.out.println("Input PDF: " + inputFile.getAbsolutePath());
            System.out.println("Number of cuts: " + numCuts);
            System.out.println("Output directory: " + outputDir.getAbsolutePath());
            System.out.println();
            
            PDFSegmenter segmenter = new PDFSegmenter();
            List<File> segments = segmenter.segment(inputFile, numCuts, outputDir);
            
            System.out.println("Successfully created " + segments.size() + " segments:");
            for (File segment : segments) {
                System.out.println("  - " + segment.getName());
            }
            
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid argument: " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Error processing PDF: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    private static void printUsage() {
        System.out.println("PDF Content Segmenter");
        System.out.println();
        System.out.println("Usage: java -jar pdf-segmenter.jar <input.pdf> <num_cuts> [output_dir]");
        System.out.println();
        System.out.println("Arguments:");
        System.out.println("  input.pdf   - Path to the PDF file to segment");
        System.out.println("  num_cuts    - Number of cuts to make (creates num_cuts + 1 segments)");
        System.out.println("  output_dir  - (Optional) Output directory for segments");
        System.out.println();
        System.out.println("Example:");
        System.out.println("  java -jar pdf-segmenter.jar document.pdf 3 ./output");
    }
    
    private static String getDefaultOutputDir(String inputPath) {
        File inputFile = new File(inputPath);
        File parent = inputFile.getParentFile();
        String baseName = inputFile.getName().replaceFirst("[.][^.]+$", "");
        return new File(parent != null ? parent : new File("."), baseName + "_segments").getAbsolutePath();
    }
}

