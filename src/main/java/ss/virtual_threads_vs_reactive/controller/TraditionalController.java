package ss.virtual_threads_vs_reactive.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import ss.virtual_threads_vs_reactive.model.dto.ApiResponse;
import ss.virtual_threads_vs_reactive.service.WorkService;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/api/traditional")
@Slf4j
public class TraditionalController {

    @Autowired
    private WorkService workService;

    @Autowired
    @Qualifier("traditionalScheduler")
    private Scheduler traditionalScheduler;

    @GetMapping("/cpu")
    public Mono<ApiResponse> performCpuWork(
            @RequestParam(defaultValue = "10000000") int iterations,
            @RequestParam(defaultValue = "1") int concurrentRequests) {
        log.info("Received CPU work request on thread: {} with {} concurrent requests", 
                Thread.currentThread(), concurrentRequests);
        
        long startTime = System.currentTimeMillis();
        
        // Use boundedElastic scheduler to simulate traditional thread pool behavior
        return Flux.range(0, concurrentRequests)
                .flatMap(i -> Mono.fromCallable(() -> workService.performCpuIntensiveWork(iterations)))
                .collectList()
                .map(results -> {
                    long responseTime = System.currentTimeMillis() - startTime;
                    return new ApiResponse(true, 
                            String.format("Completed %d concurrent CPU work requests", results.size()),
                            responseTime);
                })
                .onErrorResume(e -> {
                    long responseTime = System.currentTimeMillis() - startTime;
                    log.error("Error in CPU work", e);
                    return Mono.just(new ApiResponse(false, "Error: " + e.getMessage(), responseTime));
                }).subscribeOn(traditionalScheduler);
    }

    @GetMapping("/io")
    public Mono<ApiResponse> performIoWork(
            @RequestParam(defaultValue = "1000") int delayMs,
            @RequestParam(defaultValue = "1") int concurrentRequests) {
        log.info("Received I/O work request on thread: {} with {} concurrent requests", 
                Thread.currentThread(), concurrentRequests);
        
        long startTime = System.currentTimeMillis();
        
        // Use boundedElastic scheduler to simulate traditional thread pool behavior
        return Flux.range(0, concurrentRequests)
                .flatMap(i -> Mono.fromCallable(() -> {
                        try {
                            return workService.performIoWork(delayMs);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            throw new RuntimeException(e);
                        }
                    }))
                .collectList()
                .map(results -> {
                    long responseTime = System.currentTimeMillis() - startTime;
                    return new ApiResponse(true, 
                            String.format("Completed %d concurrent I/O work requests", results.size()),
                            responseTime);
                })
                .onErrorResume(e -> {
                    long responseTime = System.currentTimeMillis() - startTime;
                    log.error("Error in I/O work", e);
                    return Mono.just(new ApiResponse(false, "Error: " + e.getMessage(), responseTime));
                }).subscribeOn(traditionalScheduler);
    }
    
    @GetMapping("/async")
    public Mono<ApiResponse> performAsyncWork(
            @RequestParam(defaultValue = "1000") int delayMs,
            @RequestParam(defaultValue = "1") int concurrentRequests) {
        log.info("Received Traditional async work request on thread: {} with {} concurrent requests", 
                Thread.currentThread(), concurrentRequests);
        
        long startTime = System.currentTimeMillis();
        
        // Create a fixed thread pool for this batch of requests
        var executor = Executors.newFixedThreadPool(Math.min(concurrentRequests, 10));
        
        return Flux.range(0, concurrentRequests)
                .flatMap(i -> {
                    // Create a new CompletableFuture for each request using the thread pool
                    CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                        try {
                            String threadInfo = Thread.currentThread().toString();
                            log.info("Executing async work on traditional thread: {}", threadInfo);
                            return workService.performIoWork(delayMs);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            throw new RuntimeException(e);
                        }
                    }, executor);
                    
                    return Mono.fromFuture(future)
                        .doOnNext(result -> {
                            String threadInfo = Thread.currentThread().toString();
                            log.info("Completed async work on thread: {}", threadInfo);
                        });
                })
                .collectList()
                .map(results -> {
                    long responseTime = System.currentTimeMillis() - startTime;
                    return new ApiResponse(true, 
                            String.format("Completed %d concurrent async work requests with traditional threads", results.size()),
                            responseTime);
                })
                .onErrorResume(e -> {
                    long responseTime = System.currentTimeMillis() - startTime;
                    log.error("Error in async work", e);
                    return Mono.just(new ApiResponse(false, "Error: " + e.getMessage(), responseTime));
                })
                .doFinally(signal -> {
                    // Clean up the executor when done
                    executor.shutdown();
                });
    }
} 