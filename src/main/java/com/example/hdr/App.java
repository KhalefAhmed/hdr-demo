package com.example.hdr;

import org.HdrHistogram.Histogram;
import org.knowm.xchart.*;
import org.knowm.xchart.style.Styler;

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
        boolean showChart = false;

        for (String arg : args) {
            if ("--hist".equals(arg) || "--histogram".equals(arg)) {
                showAsciiHistogram = true;
            } else if ("--chart".equals(arg) || "--graph".equals(arg)) {
                showChart = true;
            } else if (inputPath == null) {
                inputPath = arg;
            } else {
                throw new IllegalArgumentException("Unknown argument: " + arg);
            }
        }

        if (inputPath == null) {
            System.out.println("Usage: App <input-file> [--hist|--histogram] [--chart|--graph]");
            return;
        }

        final List<String> lines = Files.readAllLines(Paths.get(inputPath));
        final List<Double> intervalsMs = new ArrayList<>();

        double last = -1; // -1 signifie "pas encore initialisé"
        for (final String tVal : lines) {
            if (tVal == null || tVal.isBlank()) continue;

            double parsed = Double.parseDouble(tVal.trim());

            if (last < 0) {
                // Premier point : on initialise juste la référence, pas d'intervalle
                last = parsed;
                continue;
            }

            double gcInterval = parsed - last;
            last = parsed;

            if (gcInterval > 0) {
                HISTOGRAM.recordValue((long) (gcInterval * NORMALIZER));
                intervalsMs.add(gcInterval * 1000.0);
            }
        }

        if (intervalsMs.isEmpty()) {
            System.out.println("No numeric values found in input file.");
            return;
        }

        HISTOGRAM.outputPercentileDistribution(System.out, 1000.0);

        if (showAsciiHistogram) {
            printAsciiHistogram(intervalsMs, ASCII_HISTOGRAM_BINS, ASCII_HISTOGRAM_MAX_WIDTH);
        }

        if (showChart) {
            try {
                generateChart(intervalsMs, "hdr-histogram.png");
                System.out.println("\nChart saved to: hdr-histogram.png");
            } catch (IOException e) {
                System.err.println("Error generating chart: " + e.getMessage());
            }
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
                if (index == bins) index = bins - 1;
                counts[index]++;
            }
        }

        int peak = 0;
        for (int count : counts) if (count > peak) peak = count;

        System.out.println("\nASCII Histogram (intervals in ms)");
        for (int i = 0; i < bins; i++) {
            double start = min + (max - min) * i / bins;
            double end   = min + (max - min) * (i + 1) / bins;
            int barLength = peak == 0 ? 0 : (int) Math.round((counts[i] * 1.0 / peak) * maxWidth);
            String bar = "#".repeat(Math.max(0, barLength));
            System.out.printf("[%7.2f - %7.2f) ms | %-40s (%d)%n", start, end, bar, counts[i]);
        }
    }

    private static void generateChart(List<Double> valuesMs, String outputPath) throws IOException {
        // Utilisation de la classe Histogram de XChart pour calculer les bins
        org.knowm.xchart.Histogram xchartHist = new org.knowm.xchart.Histogram(valuesMs, 20);

        CategoryChart chart = new CategoryChartBuilder()
                .width(900)
                .height(600)
                .title("GC Interval Distribution")
                .xAxisTitle("Interval (ms)")
                .yAxisTitle("Frequency")
                .build();

        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
        chart.getStyler().setAvailableSpaceFill(0.96);
        chart.getStyler().setPlotGridVerticalLinesVisible(false);

        chart.addSeries("Intervals", xchartHist.getxAxisData(), xchartHist.getyAxisData());

        BitmapEncoder.saveBitmap(chart, outputPath, BitmapEncoder.BitmapFormat.PNG);
    }
}