package com.tvd12.ezyfoxserver.client.metrics;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.tvd12.ezyfox.io.EzyDates;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class EzyMetricsRecorder {

    protected final AtomicLong startRecordingAt =
        new AtomicLong(System.currentTimeMillis());
    protected final AtomicLong endRecordingAt =
        new AtomicLong(System.currentTimeMillis());
    protected final Map<String, Long> requestCountByAppCommand =
        new ConcurrentHashMap<>();
    protected final Map<String, Long> responseCountByAppCommand =
        new ConcurrentHashMap<>();
    protected final Map<Object, Long> requestCountBySystemCommand =
        new ConcurrentHashMap<>();
    protected final Map<Object, Long> responseCountBySystemCommand =
        new ConcurrentHashMap<>();

    private static final EzyMetricsRecorder DEFAULT = new EzyMetricsRecorder();

    public static EzyMetricsRecorder getDefault() {
        return DEFAULT;
    }

    public void startRecording() {
        startRecordingAt.set(System.currentTimeMillis());
    }

    public void endRecording() {
        endRecordingAt.set(System.currentTimeMillis());
    }

    public void increaseAppRequestCount(
        String appCommand
    ) {
        requestCountByAppCommand.compute(
            appCommand,
            (k, v) -> v != null ? v + 1 : 1
        );
    }

    public void increaseAppResponseCount(
        String appCommand
    ) {
        responseCountByAppCommand.compute(
            appCommand,
            (k, v) -> v != null ? v + 1 : 1
        );
    }

    public void increaseSystemRequestCount(
        Object systemCommand
    ) {
        requestCountBySystemCommand.compute(
            systemCommand,
            (k, v) -> v != null ? v + 1 : 1
        );
    }

    public void increaseSystemResponseCount(
        Object systemCommand
    ) {
        responseCountBySystemCommand.compute(
            systemCommand,
            (k, v) -> v != null ? v + 1 : 1
        );
    }

    public long getStartRecordingAt() {
        return startRecordingAt.get();
    }

    public long getEndRecordingAt() {
        return endRecordingAt.get();
    }

    public long getRecordedTime() {
        return getEndRecordingAt() - getStartRecordingAt();
    }

    public Map<String, Long> getRequestCountByAppCommand() {
        return new HashMap<>(requestCountByAppCommand);
    }

    public Map<String, Long> getResponseCountByAppCommand() {
        return new HashMap<>(responseCountByAppCommand);
    }

    public Map<Object, Long> getRequestCountBySystemCommand() {
        return new HashMap<>(requestCountBySystemCommand);
    }

    public Map<Object, Long> getResponseCountBySystemCommand() {
        return new HashMap<>(responseCountBySystemCommand);
    }

    public long getTotalAppRequestCount() {
        return requestCountByAppCommand
            .values()
            .stream()
            .mapToLong(it -> it)
            .sum();
    }

    public long getTotalAppResponseCount() {
        return responseCountByAppCommand
            .values()
            .stream()
            .mapToLong(it -> it)
            .sum();
    }

    public long getTotalSystemRequestCount() {
        return requestCountBySystemCommand
            .values()
            .stream()
            .mapToLong(it -> it)
            .sum();
    }

    public long getTotalSystemResponseCount() {
        return responseCountBySystemCommand
            .values()
            .stream()
            .mapToLong(it -> it)
            .sum();
    }

    public BigDecimal getAppRequestPerSecond() {
        return new BigDecimal(getTotalAppRequestCount())
            .divide(
                new BigDecimal(getRecordedTime()),
                RoundingMode.UP
            )
            .multiply(new BigDecimal(1000));
    }

    public BigDecimal getAppResponsePerSecond() {
        return new BigDecimal(getTotalAppResponseCount())
            .divide(
                new BigDecimal(getRecordedTime()),
                RoundingMode.UP
            )
            .multiply(new BigDecimal(1000));
    }

    public BigDecimal getSystemRequestPerSecond() {
        return new BigDecimal(getTotalSystemRequestCount())
            .divide(
                new BigDecimal(getRecordedTime()),
                2,
                RoundingMode.UP
            )
            .multiply(new BigDecimal(1000));
    }

    public BigDecimal getSystemResponsePerSecond() {
        return new BigDecimal(getTotalSystemResponseCount())
            .divide(
                new BigDecimal(getRecordedTime()),
                2,
                RoundingMode.UP
            )
            .multiply(new BigDecimal(1000));
    }

    public void printMetrics() throws IOException {
        printMetrics(System.out);
    }

    @SuppressWarnings("MethodLength")
    public void printMetrics(
        OutputStream outputStream
    ) throws IOException {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put(
            "startRecordingAt",
            EzyDates.format(getStartRecordingAt())
        );
        metrics.put(
            "endRecordingAt",
            EzyDates.format(getEndRecordingAt())
        );
        metrics.put(
            "totalAppRequestCount",
            getTotalAppRequestCount()
        );
        metrics.put(
            "totalAppResponseCount",
            getTotalAppResponseCount()
        );
        metrics.put(
            "totalSystemRequestCount",
            getTotalSystemRequestCount()
        );
        metrics.put(
            "totalSystemResponseCount",
            getTotalSystemResponseCount()
        );
        metrics.put(
            "appRequestPerSecond",
            getAppRequestPerSecond()
        );
        metrics.put(
            "appResponsePerSecond",
            getAppResponsePerSecond()
        );
        metrics.put(
            "systemRequestPerSecond",
            getSystemRequestPerSecond()
        );
        metrics.put(
            "systemResponsePerSecond",
            getSystemResponsePerSecond()
        );
        metrics.put(
            "requestCountByAppCommand",
            getRequestCountByAppCommand()
        );
        metrics.put(
            "responseCountByAppCommand",
            getResponseCountByAppCommand()
        );
        metrics.put(
            "requestCountBySystemCommand",
            getRequestCountBySystemCommand()
        );
        metrics.put(
            "responseCountBySystemCommand",
            getResponseCountBySystemCommand()
        );
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        outputStream.write(
            objectMapper.writeValueAsBytes(metrics)
        );
    }
}
