package com.example.hdr;

import org.HdrHistogram.Histogram;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class App {

    private static final long NORMALIZER = 1_000_000;
    private static final Histogram HISTOGRAM = new Histogram(TimeUnit.MINUTES.toMicros(1), 2);
    private static final int ASCII_HISTOGRAM_BINS = 10;
    private static final int ASCII_HISTOGRAM_MAX_WIDTH = 40;

    public static void main(String[] args) throws IOException {
        String inputPath = null;
        boolean showAsciiHistogram = false;

        for (String arg : args) {
            if ("--hist".equals(arg) || "--histogram".equals(arg)) {
                showAsciiHistogram = true;
            } else if (inputPath == null) {
                inputPath = arg;
            } else {
                throw new IllegalArgumentException("Unknown argument: " + arg);
            }
        }

        if (inputPath == null) {
            System.out.println("Usage: App <input-file> [--hist|--histogram]");
            return;
        }

        final List<String> values = Files.readAllLines(Paths.get(inputPath));
        final List<Double> intervalsMs = new ArrayList<>();

        double last = 0;
        for (final String tVal : values) {
            if (tVal == null || tVal.isBlank()) {
                continue;
            }

            double parsed = Double.parseDouble(tVal.trim());
            double gcInterval = parsed - last;
            last = parsed;
            HISTOGRAM.recordValue((long) (gcInterval * NORMALIZER));
            intervalsMs.add(gcInterval * 1000.0);
        }

        if (intervalsMs.isEmpty()) {
            System.out.println("No numeric values found in input file.");
            return;
        }

        HISTOGRAM.outputPercentileDistribution(System.out, 1000.0);

        if (showAsciiHistogram) {
            printAsciiHistogram(intervalsMs, ASCII_HISTOGRAM_BINS, ASCII_HISTOGRAM_MAX_WIDTH);
        }
    }

    private static void printAsciiHistogram(List<Double> valuesMs, int bins, int maxWidth) {
        double min = valuesMs.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
        double max = valuesMs.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);

        int[] counts = new int[bins];
        if (Double.compare(min, max) == 0) {
            counts[0] = valuesMs.size();
        } else {
            double range = max - min;
            for (double value : valuesMs) {
                int index = (int) ((value - min) / range * bins);
                if (index == bins) {
                    index = bins - 1;
                }
                counts[index]++;
            }
        }

        int peak = 0;
        for (int count : counts) {
            if (count > peak) {
                peak = count;
            }
        }

        System.out.println();
        System.out.println("ASCII Histogram (intervals in ms)");
        for (int i = 0; i < bins; i++) {
            double start = min + (max - min) * i / bins;
            double end = min + (max - min) * (i + 1) / bins;

            int barLength = peak == 0 ? 0 : (int) Math.round((counts[i] * 1.0 / peak) * maxWidth);
            String bar = "#".repeat(Math.max(0, barLength));
            System.out.printf("[%7.2f - %7.2f) ms | %-40s (%d)%n", start, end, bar, counts[i]);
        }
    }
}
