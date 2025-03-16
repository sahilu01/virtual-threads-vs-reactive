package ss.virtual_threads_vs_reactive.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class WorkService {

    // Simulate CPU-bound work
    public String performCpuIntensiveWork(int iterations) {
        String threadName = Thread.currentThread().toString();
        log.info("Starting CPU-intensive work on thread: {}", threadName);
        
        long startTime = System.currentTimeMillis();
        
        // Simulate CPU-intensive work
        double result = 0;
        for (int i = 0; i < iterations; i++) {
            result += Math.sqrt(i) * Math.random();
        }
        
        long duration = System.currentTimeMillis() - startTime;
        
        log.info("Completed CPU-intensive work on thread: {} in {}ms", threadName, duration);
        return String.format("CPU work completed in %dms with result: %.2f | Thread: %s", 
                duration, result, threadName);
    }
    
    // Simulate I/O-bound work
    public String performIoWork(int delayMs) throws InterruptedException {
        String threadName = Thread.currentThread().toString();
        log.info("Starting I/O work on thread: {}", threadName);
        
        long startTime = System.currentTimeMillis();
        
        // Simulate I/O operation with a delay
        Thread.sleep(delayMs);
        
        long duration = System.currentTimeMillis() - startTime;
        
        log.info("Completed I/O work on thread: {} in {}ms", threadName, duration);
        return String.format("I/O work completed in %dms | Thread: %s", duration, threadName);
    }
    
    // Reactive version for I/O work
    public Mono<String> performIoWorkReactive(int delayMs) {
        String threadName = Thread.currentThread().toString();
        log.info("Starting reactive I/O work on thread: {}", threadName);
        
        long startTime = System.currentTimeMillis();
        
        return Mono.delay(Duration.ofMillis(delayMs))
                .map(ignored -> {
                    long duration = System.currentTimeMillis() - startTime;
                    String completionThread = Thread.currentThread().toString();
                    log.info("Completed reactive I/O work on thread: {} in {}ms", completionThread, duration);
                    return String.format("Reactive I/O work completed in %dms | Thread: %s", duration, completionThread);
                });
    }
    
    // Async version for virtual threads
    public CompletableFuture<String> performIoWorkAsync(int delayMs) {
        String threadName = Thread.currentThread().toString();
        log.info("Starting async I/O work on thread: {}", threadName);
        
        long startTime = System.currentTimeMillis();
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(delayMs);
                long duration = System.currentTimeMillis() - startTime;
                String completionThread = Thread.currentThread().toString();
                log.info("Completed async I/O work on thread: {} in {}ms", completionThread, duration);
                return String.format("Async I/O work completed in %dms | Thread: %s", duration, completionThread);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "Work interrupted | Thread: " + threadName;
            }
        });
    }
} 