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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/virtual")
@Slf4j
public class VirtualThreadController {

    @Autowired
    private WorkService workService;

    @Autowired
    @Qualifier("virtualScheduler")
    private Scheduler virtualScheduler;

    @GetMapping("/cpu")
    public Mono<ApiResponse> performCpuWork(
            @RequestParam(defaultValue = "10000000") int iterations,
            @RequestParam(defaultValue = "1") int concurrentRequests) {
        log.info("Received CPU work request on virtual thread: {} with {} concurrent requests", 
                Thread.currentThread(), concurrentRequests);
        
        long startTime = System.currentTimeMillis();
        
        // Use virtual threads for computation
        return Flux.range(0, concurrentRequests)
                .flatMap(i -> Mono.fromCallable(() -> workService.performCpuIntensiveWork(iterations)))
                .collectList()
                .map(results -> {
                    long responseTime = System.currentTimeMillis() - startTime;
                    return new ApiResponse(true, 
                            String.format("Completed %d concurrent CPU work requests with virtual threads", results.size()),
                            responseTime);
                })
                .onErrorResume(e -> {
                    long responseTime = System.currentTimeMillis() - startTime;
                    log.error("Error in CPU work", e);
                    return Mono.just(new ApiResponse(false, "Error: " + e.getMessage(), responseTime));
                }).subscribeOn(virtualScheduler);
    }

    @GetMapping("/io")
    public Mono<ApiResponse> performIoWork(
            @RequestParam(defaultValue = "1000") int delayMs,
            @RequestParam(defaultValue = "1") int concurrentRequests) {
        log.info("Received I/O work request on virtual thread: {} with {} concurrent requests", 
                Thread.currentThread(), concurrentRequests);
        
        long startTime = System.currentTimeMillis();
        
        // Use virtual threads for I/O
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
                            String.format("Completed %d concurrent I/O work requests with virtual threads", results.size()),
                            responseTime);
                })
                .onErrorResume(e -> {
                    long responseTime = System.currentTimeMillis() - startTime;
                    log.error("Error in I/O work", e);
                    return Mono.just(new ApiResponse(false, "Error: " + e.getMessage(), responseTime));
                }).subscribeOn(virtualScheduler);
    }
    
    @GetMapping("/async")
    public Mono<ApiResponse> performAsyncWork(
            @RequestParam(defaultValue = "1000") int delayMs,
            @RequestParam(defaultValue = "1") int concurrentRequests) {
        log.info("Received Virtual async work request on thread: {} with {} concurrent requests",
                Thread.currentThread(), concurrentRequests);
        
        long startTime = System.currentTimeMillis();
        
        // Create a list to hold all the futures
        List<CompletableFuture<String>> futures = new ArrayList<>();
        
        // Create an executor for virtual threads
        var executor = Executors.newVirtualThreadPerTaskExecutor();
        
        // Create all the futures upfront to ensure they start executing immediately
        for (int i = 0; i < concurrentRequests; i++) {
            final int taskId = i;
            CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                try {
                    String threadInfo = Thread.currentThread().toString();
                    log.info("Task {} executing on virtual thread: {}", taskId, threadInfo);
                    
                    // Simulate I/O work directly here instead of using the service
                    Thread.sleep(delayMs);
                    
                    log.info("Task {} completed on virtual thread: {}", taskId, threadInfo);
                    return "Task " + taskId + " completed on " + threadInfo;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
            }, executor);
            
            futures.add(future);
        }
        
        // Convert the list of futures to a single future that completes when all complete
        CompletableFuture<List<String>> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList()));
        
        // Convert the combined future to a Mono
        return Mono.fromFuture(allFutures)
                .map(results -> {
                    long responseTime = System.currentTimeMillis() - startTime;
                    return new ApiResponse(true, 
                            String.format("Completed %d concurrent async work requests with virtual threads", results.size()),
                            responseTime);
                })
                .onErrorResume(e -> {
                    long responseTime = System.currentTimeMillis() - startTime;
                    log.error("Error in async work", e);
                    return Mono.just(new ApiResponse(false, "Error: " + e.getMessage(), responseTime));
                })
                .doFinally(signal -> {
                    // Clean up the executor when done
                    executor.close();
                });
    }
} 