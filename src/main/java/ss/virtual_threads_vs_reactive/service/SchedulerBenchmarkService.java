package ss.virtual_threads_vs_reactive.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import ss.virtual_threads_vs_reactive.config.SchedulerFactory;
import ss.virtual_threads_vs_reactive.model.SchedulerType;
import ss.virtual_threads_vs_reactive.model.dto.ApiResponse;
import ss.virtual_threads_vs_reactive.model.dto.SchedulerBenchmarkResult;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class SchedulerBenchmarkService {

    @Autowired
    private SchedulerFactory schedulerFactory;
    
    @Autowired
    private WorkService workService;

    public SchedulerBenchmarkResult runBenchmark(int concurrentRequests, int delayMs, String workType, List<SchedulerType> schedulerTypes) {
        log.info("Running scheduler benchmark with {} concurrent requests, {}ms delay, work type: {}, schedulers: {}", 
                concurrentRequests, delayMs, workType, schedulerTypes);
        
        List<SchedulerBenchmarkResult.SchedulerResult> results = new ArrayList<>();
        
        for (SchedulerType schedulerType : schedulerTypes) {
            log.info("Testing scheduler: {}", schedulerType);
            
            Scheduler scheduler = schedulerFactory.createScheduler(schedulerType);
            
            long startTime = System.currentTimeMillis();
            
            // Run the benchmark for this scheduler
            List<ApiResponse> responses = runTest(concurrentRequests, delayMs, workType, scheduler);
            
            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            
            // Calculate success rate
            long successCount = responses.stream().filter(ApiResponse::success).count();
            double successRate = (double) successCount / responses.size() * 100;
            
            // Calculate average response time
            double avgResponseTime = totalTime / (double) concurrentRequests;
            
            // Calculate throughput (requests per second)
            double throughput = concurrentRequests / (totalTime / 1000.0);
            
            results.add(new SchedulerBenchmarkResult.SchedulerResult(
                    schedulerType.name(),
                    totalTime,
                    avgResponseTime,
                    throughput,
                    successRate,
                    getSchedulerDescription(schedulerType)
            ));
            
            // Dispose the scheduler if needed
            if (schedulerType != SchedulerType.BOUNDED_ELASTIC && 
                schedulerType != SchedulerType.PARALLEL && 
                schedulerType != SchedulerType.SINGLE) {
                try {
                    scheduler.dispose();
                } catch (Exception e) {
                    log.warn("Error disposing scheduler: {}", e.getMessage());
                }
            }
            
            // Give some time for cleanup between tests
            try {
                TimeUnit.MILLISECONDS.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        // Sort results by total time (fastest first)
        results.sort((r1, r2) -> Long.compare(r1.totalTimeMs(), r2.totalTimeMs()));
        
        return new SchedulerBenchmarkResult(
                concurrentRequests,
                delayMs,
                workType,
                results
        );
    }
    
    private List<ApiResponse> runTest(int concurrentRequests, int delayMs, String workType, Scheduler scheduler) {
        return Flux.range(0, concurrentRequests)
                .flatMap(i -> {
                    Mono<ApiResponse> mono;
                    
                    switch (workType) {
                        case "cpu":
                            mono = Mono.fromCallable(() -> workService.performCpuIntensiveWork(10000000))
                                    .map(result -> new ApiResponse(true, result));
                            break;
                        case "async":
                            mono = Mono.fromFuture(() -> workService.performIoWorkAsync(delayMs))
                                    .map(result -> new ApiResponse(true, result));
                            break;
                        case "io":
                        default:
                            mono = Mono.fromCallable(() -> {
                                try {
                                    return workService.performIoWork(delayMs);
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                    throw new RuntimeException(e);
                                }
                            }).map(result -> new ApiResponse(true, result));
                    }
                    
                    return mono.subscribeOn(scheduler)
                            .onErrorResume(e -> {
                                log.error("Error in scheduler benchmark", e);
                                return Mono.just(new ApiResponse(false, "Error: " + e.getMessage()));
                            });
                })
                .collectList()
                .block(Duration.ofMinutes(5)); // Generous timeout for large benchmarks
    }
    
    private String getSchedulerDescription(SchedulerType type) {
        return switch (type) {
            case SINGLE -> "Single-threaded scheduler";
            case PARALLEL -> "Multi-threaded scheduler with CPU core count threads";
            case NEW_PARALLEL -> "Custom multi-threaded scheduler";
            case NEW_SINGLE -> "Custom single-threaded scheduler";
            case EXECUTOR_WORK_STEALING -> "Work-stealing thread pool executor";
            case EXECUTOR_CACHED -> "Cached thread pool executor";
            case EXECUTOR_FIXED -> "Fixed-size thread pool executor";
            case EXECUTOR_SINGLE -> "Single-thread executor";
            case THREAD_POOL -> "Custom thread pool executor";
            case BOUNDED_ELASTIC -> "Bounded elastic scheduler (default)";
            case NEW_BOUNDED_ELASTIC -> "Custom bounded elastic scheduler";
            case VIRTUAL_THREAD -> "Virtual threads (Project Loom)";
            case IMMEDIATE -> "Immediate scheduler (executes on caller thread)";
        };
    }
} 