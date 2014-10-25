package de.witi;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class StreamBenchmark {
    private static final int N = 5_000_000;
    private static final List<String> strings = new ArrayList<>(N);

    @Setup
    public static final void setup() {
        final Random random = new Random();
        for (int i = 0; i < N; ++i) {
            strings.add(new BigInteger(130, random).toString(32));
        }
    }

    @Benchmark
    public String stream() {
        return strings.stream()
                .map(String::toLowerCase)
                .filter(string -> string.startsWith("a"))
                .map(string -> string.replaceAll("a", "z"))
                .sorted()
                .collect(Collectors.joining(","));
    }

    @Benchmark
    public String parallelStream() {
        return strings.parallelStream()
                .map(String::toLowerCase)
                .filter(string -> string.startsWith("a"))
                .map(string -> string.replaceAll("a", "z"))
                .sorted()
                .collect(Collectors.joining(","));
    }

    @Benchmark
    public String classic() {
        List<String> filtered = new ArrayList<>(strings.size());

        for (String string : strings) {
            String lower = string.toLowerCase();
            if (lower.startsWith("a")) {
                filtered.add(lower.replaceAll("a", "z"));
            }
        }

        Collections.sort(filtered);

        StringBuilder builder = new StringBuilder();
        builder.append(filtered.get(0));

        for (int i = 1; i < filtered.size(); i++) {
            builder.append(",");
            builder.append(filtered.get(i));
        }

        return builder.toString();
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(StreamBenchmark.class.getSimpleName())
                .warmupIterations(5)
                .measurementIterations(5)
                .forks(1)
                .jvmArgs("-server", "-Xms2048m", "-Xmx2048m")
                .build();

        new Runner(opt).run();
    }
}
