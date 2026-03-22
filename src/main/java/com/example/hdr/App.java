package com.example.hdr;

import org.HdrHistogram.Histogram;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class App {

    private static final long NORMALIZER = 1_000_000;
    private static final Histogram HISTOGRAM = new Histogram(TimeUnit.MINUTES.toMicros(1), 2);

    public static void main(String[] args) throws IOException {
        final List<String> values = Files.readAllLines(Paths.get(args[0]));

        double last = 0;
        for (final String tVal : values) {
            double parsed = Double.parseDouble(tVal);
            double gcInterval = parsed - last;
            last = parsed;
            HISTOGRAM.recordValue((long) (gcInterval * NORMALIZER));
        }
        HISTOGRAM.outputPercentileDistribution(System.out, 1000.0);
    }
}
