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
import reactor.core.scheduler.Schedulers;
import ss.virtual_threads_vs_reactive.model.dto.ApiResponse;
import ss.virtual_threads_vs_reactive.service.WorkService;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/webflux")
@Slf4j
public class WebFluxController {

    @Autowired
    private WorkService workService;

    @Autowired
    @Qualifier("reactiveScheduler")
    private Scheduler reactiveScheduler;

    @GetMapping("/cpu")
    public Mono<ApiResponse> performCpuWork(
            @RequestParam(defaultValue = "10000000") int iterations) {
        log.info("Received WebFlux CPU work request on thread: {}", Thread.currentThread());
        
        long startTime = System.currentTimeMillis();
        
        return Mono.fromCallable(() -> workService.performCpuIntensiveWork(iterations))
                .subscribeOn(reactiveScheduler)
                .map(result -> {
                    long responseTime = System.currentTimeMillis() - startTime;
                    return new ApiResponse(true, 
                            "Completed CPU work request with WebFlux",
                            responseTime);
                })
                .onErrorResume(e -> {
                    long responseTime = System.currentTimeMillis() - startTime;
                    log.error("Error in WebFlux CPU work", e);
                    return Mono.just(new ApiResponse(false, "Error: " + e.getMessage(), responseTime));
                });
    }

    @GetMapping("/io")
    public Mono<ApiResponse> performIoWork(
            @RequestParam(defaultValue = "1000") int delayMs) {
        log.info("Received WebFlux I/O work request on thread: {}", Thread.currentThread());
        
        long startTime = System.currentTimeMillis();
        
        return workService.performIoWorkReactive(delayMs)
                .subscribeOn(reactiveScheduler)
                .map(result -> {
                    long responseTime = System.currentTimeMillis() - startTime;
                    return new ApiResponse(true, 
                            "Completed I/O work request with WebFlux",
                            responseTime);
                })
                .onErrorResume(e -> {
                    long responseTime = System.currentTimeMillis() - startTime;
                    log.error("Error in WebFlux I/O work", e);
                    return Mono.just(new ApiResponse(false, "Error: " + e.getMessage(), responseTime));
                });
    }

    @GetMapping("/async")
    public Mono<ApiResponse> performAsyncWork(
            @RequestParam(defaultValue = "1000") int delayMs) {
        log.info("Received WebFlux async work request on thread: {}", Thread.currentThread());
        
        long startTime = System.currentTimeMillis();
        
        // Use Mono.defer to ensure each subscription gets its own execution context
        return Mono.defer(() -> {
                String threadInfo = Thread.currentThread().toString();
                log.info("Starting async work on WebFlux thread: {}", threadInfo);
                
                // Use delayElement which is non-blocking and truly reactive
                return Mono.just("Task completed")
                        .delayElement(Duration.ofMillis(delayMs))
                        .doOnNext(result -> {
                            String completionThread = Thread.currentThread().toString();
                            log.info("Completed async work on WebFlux thread: {}", completionThread);
                        });
            })
            .subscribeOn(reactiveScheduler)
            .map(result -> {
                long responseTime = System.currentTimeMillis() - startTime;
                return new ApiResponse(true, 
                        "Completed async work request with WebFlux",
                        responseTime);
            })
            .onErrorResume(e -> {
                long responseTime = System.currentTimeMillis() - startTime;
                log.error("Error in WebFlux async work", e);
                return Mono.just(new ApiResponse(false, "Error: " + e.getMessage(), responseTime));
            });
    }
} 