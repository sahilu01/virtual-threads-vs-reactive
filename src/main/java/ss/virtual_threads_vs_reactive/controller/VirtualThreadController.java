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
            @RequestParam(defaultValue = "10000000") int iterations) {
        log.info("Received CPU work request on virtual thread: {}", Thread.currentThread());
        
        long startTime = System.currentTimeMillis();
        
        // Use virtual threads for computation
        return Mono.fromCallable(() -> workService.performCpuIntensiveWork(iterations))
                .subscribeOn(virtualScheduler)
                .map(result -> {
                    long responseTime = System.currentTimeMillis() - startTime;
                    return new ApiResponse(true, 
                            "Completed CPU work request with virtual threads",
                            responseTime);
                })
                .onErrorResume(e -> {
                    long responseTime = System.currentTimeMillis() - startTime;
                    log.error("Error in CPU work", e);
                    return Mono.just(new ApiResponse(false, "Error: " + e.getMessage(), responseTime));
                });
    }

    @GetMapping("/io")
    public Mono<ApiResponse> performIoWork(
            @RequestParam(defaultValue = "1000") int delayMs) {
        log.info("Received I/O work request on virtual thread: {}", Thread.currentThread());
        
        long startTime = System.currentTimeMillis();
        
        // Use virtual threads for I/O
        return Mono.fromCallable(() -> {
                try {
                    return workService.performIoWork(delayMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
            })
            .subscribeOn(virtualScheduler)
            .map(result -> {
                long responseTime = System.currentTimeMillis() - startTime;
                return new ApiResponse(true, 
                        "Completed I/O work request with virtual threads",
                        responseTime);
            })
            .onErrorResume(e -> {
                long responseTime = System.currentTimeMillis() - startTime;
                log.error("Error in I/O work", e);
                return Mono.just(new ApiResponse(false, "Error: " + e.getMessage(), responseTime));
            });
    }
    
    @GetMapping("/async")
    public Mono<ApiResponse> performAsyncWork(
            @RequestParam(defaultValue = "1000") int delayMs) {
        log.info("Received Virtual async work request on thread: {}", Thread.currentThread());
        
        long startTime = System.currentTimeMillis();
        
        // Create an executor for virtual threads
        var executor = Executors.newVirtualThreadPerTaskExecutor();
        
        // Create a CompletableFuture that uses a virtual thread
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            try {
                String threadInfo = Thread.currentThread().toString();
                log.info("Executing async work on virtual thread: {}", threadInfo);
                
                // Simulate I/O work directly here instead of using the service
                Thread.sleep(delayMs);
                
                log.info("Completed async work on virtual thread: {}", threadInfo);
                return "Task completed on " + threadInfo;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }, executor);
        
        // Convert the future to a Mono
        return Mono.fromFuture(future)
                .map(result -> {
                    long responseTime = System.currentTimeMillis() - startTime;
                    return new ApiResponse(true, 
                            "Completed async work request with virtual threads",
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