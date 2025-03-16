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
            @RequestParam(defaultValue = "10000000") int iterations,
            @RequestParam(defaultValue = "1") int concurrentRequests) {
        log.info("Received WebFlux CPU work request on thread: {} with {} concurrent requests", 
                Thread.currentThread(), concurrentRequests);
        
        long startTime = System.currentTimeMillis();
        
        return Flux.range(0, concurrentRequests)
                .flatMap(i -> Mono.fromCallable(() -> workService.performCpuIntensiveWork(iterations)))
                .collectList()
                .map(results -> {
                    long responseTime = System.currentTimeMillis() - startTime;
                    return new ApiResponse(true, 
                            String.format("Completed %d concurrent CPU work requests with WebFlux", results.size()),
                            responseTime);
                })
                .onErrorResume(e -> {
                    long responseTime = System.currentTimeMillis() - startTime;
                    log.error("Error in WebFlux CPU work", e);
                    return Mono.just(new ApiResponse(false, "Error: " + e.getMessage(), responseTime));
                }).subscribeOn(reactiveScheduler);
    }

    @GetMapping("/io")
    public Mono<ApiResponse> performIoWork(
            @RequestParam(defaultValue = "1000") int delayMs,
            @RequestParam(defaultValue = "1") int concurrentRequests) {
        log.info("Received WebFlux I/O work request on thread: {} with {} concurrent requests", 
                Thread.currentThread(), concurrentRequests);
        
        long startTime = System.currentTimeMillis();
        
        return Flux.range(0, concurrentRequests)
                .flatMap(i -> workService.performIoWorkReactive(delayMs))
                .collectList()
                .map(results -> {
                    long responseTime = System.currentTimeMillis() - startTime;
                    return new ApiResponse(true, 
                            String.format("Completed %d concurrent I/O work requests with WebFlux", results.size()),
                            responseTime);
                })
                .onErrorResume(e -> {
                    long responseTime = System.currentTimeMillis() - startTime;
                    log.error("Error in WebFlux I/O work", e);
                    return Mono.just(new ApiResponse(false, "Error: " + e.getMessage(), responseTime));
                }).subscribeOn(reactiveScheduler);
    }

    @GetMapping("/async")
    public Mono<ApiResponse> performAsyncWork(
            @RequestParam(defaultValue = "1000") int delayMs,
            @RequestParam(defaultValue = "1") int concurrentRequests) {
        log.info("Received WebFlux async work request on thread: {} with {} concurrent requests", 
                Thread.currentThread(), concurrentRequests);

      // Create a list to hold all the futures
      List<CompletableFuture<String>> futures = new ArrayList<>();
        long startTime = System.currentTimeMillis();

      var executor = Executors.newVirtualThreadPerTaskExecutor();


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
          });
    }
} 