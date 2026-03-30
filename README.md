# HDR Demo - Percentiles and Long-Tail Distribution

This project reads timestamps (in seconds) from a text file, computes intervals between consecutive events, and prints a percentile distribution using **HdrHistogram**.

## Sample Input File

The example dataset is `sample-gc-times.txt`.

Expected format:
- one numeric value per line
- values must be in chronological order (increasing)
- blank lines are ignored

Example:

```text
0.125
0.290
0.515
...
9.750
```

## Run the Program

First, build the project:

```zsh
cd /Users/ahmed.khalef/IdeaProjects/me/hdr-demo
mvn clean install -q
```

Then, run with percentile distribution only:

```zsh
java -cp target/classes:$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout) com.example.hdr.App sample-gc-times.txt
```

Run with percentile distribution + ASCII histogram:

```zsh
java -cp target/classes:$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout) com.example.hdr.App sample-gc-times.txt --histogram
```

Run with percentile distribution + chart (PNG):

```zsh
java -cp target/classes:$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout) com.example.hdr.App sample-gc-times.txt --chart
```

Run with all outputs:

```zsh
java -cp target/classes:$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout) com.example.hdr.App sample-gc-times.txt --histogram --chart
```

You can also use `--hist` instead of `--histogram` or `--graph` instead of `--chart`.

## Observed Results (with `sample-gc-times.txt`)

Percentile output excerpt:

```text
Value     Percentile
165.89    0.000000000000
220.16    0.100000000000
460.80    0.500000000000
741.38    0.900000000000
782.34    1.000000000000
#[Mean    =       481.13, StdDeviation   =       184.79]
#[Max     =       782.34, Total count    =           20]
```

Quick interpretation:
- p10 ~ **220.16 µs**: 10% of the intervals are <= 220 µs
- p50 ~ **460.80 µs**: half of the intervals are <= 460 µs
- p90 ~ **741.38 µs**: 90% of the intervals are <= 741 µs
- max ~ **782.34 µs**: a few cases are noticeably slower than the median

ASCII histogram excerpt:

```text
ASCII Histogram (intervals in ms)
[ 125.00 -  190.50) ms | ####################                     (2)
[ 190.50 -  256.00) ms | ##############################           (3)
...
[ 714.50 -  780.00) ms | ####################                     (2)
```

## What Is a Long-Tail Distribution?

A **long-tail distribution** means:
- most values stay in a common or "normal" range
- a smaller number of values are much larger
- these rare high values push the average up and impact perceived latency

In this dataset:
- the core of the distribution is around ~220-650 µs
- upper percentiles (p90 to max) rise to ~782 µs
- the tail is visible and meaningful for performance analysis

In practice, monitor **p95/p99/max** in addition to the mean, because tail values are usually what users feel during rare slow events.

