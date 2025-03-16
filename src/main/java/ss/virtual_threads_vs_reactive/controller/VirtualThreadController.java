package ss.virtual_threads_vs_reactive.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import ss.virtual_threads_vs_reactive.model.dto.ApiResponse;
import ss.virtual_threads_vs_reactive.service.WorkService;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

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
        
        // Use virtual threads for computation
        return Mono.fromCallable(() -> workService.performCpuIntensiveWork(iterations))
                .subscribeOn(virtualScheduler)
                .map(result -> new ApiResponse(true, result))
                .onErrorResume(e -> {
                    log.error("Error in CPU work", e);
                    return Mono.just(new ApiResponse(false, "Error: " + e.getMessage()));
                });
    }

    @GetMapping("/io")
    public Mono<ApiResponse> performIoWork(
            @RequestParam(defaultValue = "1000") int delayMs) {
        log.info("Received I/O work request on virtual thread: {}", Thread.currentThread());
        
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
            .map(result -> new ApiResponse(true, result))
            .onErrorResume(e -> {
                log.error("Error in I/O work", e);
                return Mono.just(new ApiResponse(false, "Error: " + e.getMessage()));
            });
    }
    
    @GetMapping("/async")
    public Mono<ApiResponse> performAsyncWork(
            @RequestParam(defaultValue = "1000") int delayMs) {
        log.info("Received async work request on thread: {}", Thread.currentThread());
        
        // Create the CompletableFuture first, then pass it to Mono.fromFuture
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            try {
                return workService.performIoWork(delayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }, Executors.newVirtualThreadPerTaskExecutor());
        
        // Now pass the CompletableFuture to Mono.fromFuture
        return Mono.fromFuture(future)
            .map(result -> new ApiResponse(true, result))
            .onErrorResume(e -> {
                log.error("Error in async work", e);
                return Mono.just(new ApiResponse(false, "Error: " + e.getMessage()));
            });
    }
} 