package cn.game.core.execute;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/** 从新JVM读取原始测量，核对计数/分位数和资源回收；不将10ms当作不稳定的单测阈值。 */
class RequestTimingBenchmarkTest {
    /** 子进程必须生成所有矩阵行，不能以旧成功、固定数字或缺失报告通过。 */
    @Test void freshJvmProducesCompleteRecomputableMatrix() throws Exception {
        String configured = System.getenv("S4_BENCHMARK_ROOT");
        Path runtime = configured == null ? Files.createTempDirectory("request-timing-benchmark-") : Path.of(configured);
        Path output = runtime.resolve("benchmark.json"), log = runtime.resolve("benchmark-jvm.log");
        Assertions.assertFalse(Files.exists(output), "每次执行须用全新私有目录");
        Process process = new ProcessBuilder(Path.of(System.getProperty("java.home"), "bin", "java").toString(), "-Xmx512m", "-cp",
                System.getProperty("java.class.path"), RequestTimingBenchmark.class.getName(), output.toString())
                .redirectErrorStream(true).redirectOutput(log.toFile()).start();
        try {
            Assertions.assertTrue(process.waitFor(90, TimeUnit.SECONDS), "基准超时，参阅私有子JVM日志");
            Assertions.assertEquals(0, process.exitValue(), "子JVM失败，参阅私有日志");
        } finally {
            if (process.isAlive()) { process.destroyForcibly(); Assertions.assertTrue(process.waitFor(5, TimeUnit.SECONDS)); }
        }
        Assertions.assertTrue(Files.size(output) > 0);
        JsonNode report = new ObjectMapper().readTree(output.toFile());
        Assertions.assertEquals("IN_PROCESS_REAL_MAILBOX_PROTOBUF_GAMECLIENT_SYNTHETIC_TRANSPORT", report.path("measurementScope").asText());
        Assertions.assertTrue(report.path("resourcesClosed").asBoolean());
        Assertions.assertEquals(10, report.path("executionClassHashes").size());
        Assertions.assertEquals(24, report.path("windows").size());
        java.util.Set<String> combinations = new java.util.HashSet<>();
        int measured = 0, warmup = 0;
        for (JsonNode window : report.path("windows")) {
            String key = window.path("phase").asText() + ":" + window.path("instrumentation").asBoolean() + ":"
                    + window.path("concurrency").asInt() + ":" + window.path("distribution").asText();
            Assertions.assertTrue(combinations.add(key), "矩阵重复");
            int count = window.path("count").asInt();
            Assertions.assertEquals(window.path("phase").asText().equals("measured") ? 512 : 128, count);
            Assertions.assertEquals(count, window.path("success").asInt());
            Assertions.assertEquals(0, window.path("failure").asInt());
            Assertions.assertEquals(0, window.path("reject").asInt());
            Assertions.assertEquals(0, window.path("timeout").asInt());
            Assertions.assertTrue(window.path("resourcesClosed").asBoolean());
            Assertions.assertTrue(window.path("durationNanosIncludingDrain").asLong() > 0);
            Assertions.assertEquals(count, window.path("rawNanos").size());
            long[] samples = new long[count];
            for (int i = 0; i < count; i++) { samples[i] = window.path("rawNanos").get(i).asLong(); Assertions.assertTrue(samples[i] > 0); }
            Arrays.sort(samples);
            Assertions.assertEquals(samples[(int) Math.ceil(count * .50) - 1], window.path("p50Nanos").asLong());
            Assertions.assertEquals(samples[(int) Math.ceil(count * .95) - 1], window.path("p95Nanos").asLong());
            Assertions.assertEquals(samples[(int) Math.ceil(count * .99) - 1], window.path("p99Nanos").asLong());
            long over = Arrays.stream(samples).filter(n -> n > 10_000_000L).count();
            Assertions.assertEquals(over, window.path("over10ms").asLong());
            Assertions.assertEquals(count, window.path("over10msDenominator").asInt());
            Assertions.assertEquals((double) over / count, window.path("over10msProportion").asDouble());
            Assertions.assertEquals(0, window.path("dbSamples").asInt());
            if (window.path("phase").asText().equals("measured")) { measured += count; } else { warmup += count; }
        }
        Assertions.assertEquals(6144, measured); Assertions.assertEquals(1536, warmup);
    }
}
