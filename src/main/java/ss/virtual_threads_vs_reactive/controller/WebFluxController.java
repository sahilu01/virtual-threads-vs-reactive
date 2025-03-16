package ss.virtual_threads_vs_reactive.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ss.virtual_threads_vs_reactive.model.dto.ApiResponse;
import ss.virtual_threads_vs_reactive.service.WorkService;

@RestController
@RequestMapping("/api/webflux")
@Slf4j
public class WebFluxController {

    @Autowired
    private WorkService workService;

    @GetMapping("/cpu")
    public Mono<ApiResponse> performCpuWork(
            @RequestParam(defaultValue = "10000000") int iterations) {
        log.info("Received WebFlux CPU work request on thread: {}", Thread.currentThread());
        
        return Mono.fromCallable(() -> workService.performCpuIntensiveWork(iterations))
                .subscribeOn(Schedulers.boundedElastic())
                .map(result -> new ApiResponse(true, result))
                .onErrorResume(e -> {
                    log.error("Error in WebFlux CPU work", e);
                    return Mono.just(new ApiResponse(false, "Error: " + e.getMessage()));
                });
    }

    @GetMapping("/io")
    public Mono<ApiResponse> performIoWork(
            @RequestParam(defaultValue = "1000") int delayMs) {
        log.info("Received WebFlux I/O work request on thread: {}", Thread.currentThread());
        
        return workService.performIoWorkReactive(delayMs)
                .map(result -> new ApiResponse(true, result))
                .onErrorResume(e -> {
                    log.error("Error in WebFlux I/O work", e);
                    return Mono.just(new ApiResponse(false, "Error: " + e.getMessage()));
                });
    }

    @GetMapping("/async")
public Mono<ApiResponse> performAsyncWork(
        @RequestParam(defaultValue = "1000") int delayMs) {
    log.info("Received WebFlux async work request on thread: {}", Thread.currentThread());
    
    return workService.performIoWorkReactive(delayMs)
            .map(result -> new ApiResponse(true, result))
            .onErrorResume(e -> {
                log.error("Error in WebFlux async work", e);
                return Mono.just(new ApiResponse(false, "Error: " + e.getMessage()));
            });
}
} 