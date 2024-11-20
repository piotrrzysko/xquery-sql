package io.github.piotrrzysko;

import io.airlift.slice.DynamicSliceOutput;
import io.airlift.slice.Slice;
import io.airlift.slice.SliceOutput;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.VerboseMode;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.openjdk.jmh.annotations.Mode.AverageTime;
import static org.openjdk.jmh.annotations.Scope.Thread;

@State(Thread)
@OutputTimeUnit(NANOSECONDS)
@BenchmarkMode(AverageTime)
@Fork(1)
@Warmup(iterations = 3)
@Measurement(iterations = 5)
public class BenchmarkRegex
{
    @Benchmark
    public boolean benchmarkRe2j(DotStarAroundData data)
    {
        return data.re2j.matches(data.source);
    }

    @Benchmark
    public boolean benchmarkJoni(DotStarAroundData data)
    {
        return data.joni.matches(data.source);
    }

    @Benchmark
    public boolean benchmarkSaxon(DotStarAroundData data)
    {
        return data.saxon.matches(data.source);
    }

    @State(Thread)
    public static class DotStarAroundData
    {
        @Param({
                ".*x.*",
                ".*(x|y).*",
                "longdotstar",
                "phone",
                "literal"
        })
        private String patternString;

        @Param({
                "1024",
                "32768"
        })
        private int sourceLength;

        private Re2JRegex re2j;
        private JoniRegex joni;
        private SaxonRegex saxon;
        private Slice source;

        @Setup
        public void setup()
        {
            SliceOutput sliceOutput = new DynamicSliceOutput(sourceLength);
            String pattern;
            switch (patternString) {
                case ".*x.*":
                    pattern = ".*x.*";
                    IntStream.generate(() -> 97).limit(sourceLength).forEach(sliceOutput::appendByte);
                    break;
                case ".*(x|y).*":
                    pattern = ".*(x|y).*";
                    IntStream.generate(() -> 97).limit(sourceLength).forEach(sliceOutput::appendByte);
                    break;
                case "longdotstar":
                    pattern = ".*coolfunctionname.*";
                    ThreadLocalRandom.current().ints(97, 123).limit(sourceLength).forEach(sliceOutput::appendByte);
                    break;
                case "phone":
                    pattern = "\\d{3}/\\d{3}/\\d{4}";
                    // 47: '/', 48-57: '0'-'9'
                    ThreadLocalRandom.current().ints(47, 58).limit(sourceLength).forEach(sliceOutput::appendByte);
                    break;
                case "literal":
                    pattern = "literal";
                    // 97-122: 'a'-'z'
                    ThreadLocalRandom.current().ints(97, 123).limit(sourceLength).forEach(sliceOutput::appendByte);
                    break;
                default:
                    throw new IllegalArgumentException("pattern: " + patternString + " not supported");
            }

            re2j = new Re2JRegex(pattern);
            joni = new JoniRegex(pattern);
            saxon = new SaxonRegex(pattern, "");
            source = sliceOutput.slice();
        }
    }

    public static void main(String[] args)
            throws RunnerException
    {
        Options options = new OptionsBuilder()
                .verbosity(VerboseMode.NORMAL)
                .resultFormat(ResultFormatType.JSON)
                .result("benchmark-results/" + System.currentTimeMillis() + ".json")
                .include(".*" + BenchmarkRegex.class.getSimpleName() + ".*")
                .build();

        new Runner(options).run();
    }

    @Test
    public void testCorrectness()
    {
        DotStarAroundData data = new DotStarAroundData();
        for (int sourceLength : List.of(1024, 32768)) {
            for (String pattern : List.of(".*x.*", ".*(x|y).*", "longdotstar", "phone", "literal")) {
                data.sourceLength = sourceLength;
                data.patternString = pattern;

                data.setup();

                assertThat(benchmarkRe2j(data) == benchmarkJoni(data) == benchmarkSaxon(data))
                        .isTrue();
            }
        }
    }
}
