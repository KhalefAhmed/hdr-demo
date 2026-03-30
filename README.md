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

Percentile distribution only:

```zsh
cd /Users/ahmed.khalef/IdeaProjects/me/hdr-demo
mvn -q -Dexec.mainClass=com.example.hdr.App -Dexec.args="sample-gc-times.txt" org.codehaus.mojo:exec-maven-plugin:3.5.0:java
```

Percentile distribution + ASCII histogram:

```zsh
cd /Users/ahmed.khalef/IdeaProjects/me/hdr-demo
mvn -q -Dexec.mainClass=com.example.hdr.App -Dexec.args="sample-gc-times.txt --histogram" org.codehaus.mojo:exec-maven-plugin:3.5.0:java
```

You can also use `--hist`.

## Observed Results (with `sample-gc-times.txt`)

Percentile output excerpt:

```text
Value     Percentile
125.44    0.000000000000
460.80    0.500000000000
741.38    0.900000000000
782.34    1.000000000000
#[Mean    = 464.18, StdDeviation = 195.62]
#[Max     = 782.34, Total count  = 21]
```

Quick interpretation:
- p50 ~ **460.80 ms**: half of the intervals are <= 460 ms
- p90 ~ **741.38 ms**: 90% of the intervals are <= 741 ms
- max ~ **782.34 ms**: a few cases are noticeably slower than the median

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
- the core of the distribution is around ~220-650 ms
- upper percentiles (p90 to max) rise to ~782 ms
- the tail is not huge, but it is visible and meaningful for performance analysis

In practice, monitor **p95/p99/max** in addition to the mean, because tail values are usually what users feel during rare slow events.

