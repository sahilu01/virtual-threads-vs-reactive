package ss.virtual_threads_vs_reactive.model.dto;

import java.util.List;

public record SchedulerBenchmarkResult(
    int concurrentRequests,
    int delayMs,
    String workType,
    List<SchedulerResult> results
) {
    public record SchedulerResult(
        String schedulerType,
        long totalTimeMs,
        double avgResponseTimeMs,
        double throughputRps,
        double successRate,
        String description
    ) {}
} 