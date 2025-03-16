package ss.virtual_threads_vs_reactive.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import ss.virtual_threads_vs_reactive.model.dto.ApiResponse;
import ss.virtual_threads_vs_reactive.model.dto.BenchmarkResult;

import java.time.Duration;

@Service
@Slf4j
public class BenchmarkService {

    @Autowired
    @Qualifier("traditionalWebClient")
    private WebClient traditionalWebClient;
    
    @Autowired
    @Qualifier("virtualWebClient")
    private WebClient virtualWebClient;
    
    @Autowired
    @Qualifier("reactiveWebClient")
    private WebClient reactiveWebClient;

    public BenchmarkResult runBenchmark(int concurrentRequests, int delayMs, String workType) {
        log.info("Starting benchmark with {} concurrent requests, {}ms delay, work type: {}", 
                concurrentRequests, delayMs, workType);
        
        BenchmarkResult result = new BenchmarkResult();
        
        // Benchmark traditional threads
        long traditionalStart = System.currentTimeMillis();
        benchmarkTraditional(concurrentRequests, delayMs, workType);
        long traditionalDuration = System.currentTimeMillis() - traditionalStart;
        result.setTraditionalThreadsDuration(traditionalDuration);
        
        // Benchmark virtual threads
        long virtualStart = System.currentTimeMillis();
        benchmarkVirtual(concurrentRequests, delayMs, workType);
        long virtualDuration = System.currentTimeMillis() - virtualStart;
        result.setVirtualThreadsDuration(virtualDuration);
        
        // Benchmark WebFlux
        long webfluxStart = System.currentTimeMillis();
        benchmarkWebFlux(concurrentRequests, delayMs, workType);
        long webfluxDuration = System.currentTimeMillis() - webfluxStart;
        result.setWebFluxDuration(webfluxDuration);
        
        log.info("Benchmark completed: Traditional: {}ms, Virtual: {}ms, WebFlux: {}ms",
                traditionalDuration, virtualDuration, webfluxDuration);
        
        return result;
    }
    
    private void benchmarkTraditional(int concurrentRequests, int delayMs, String workType) {
        String url = "http://localhost:8080/api/traditional/" + workType + "?delayMs=" + delayMs;
        
        Flux.range(0, concurrentRequests)
            .flatMap(i -> traditionalWebClient.get().uri(url).retrieve().bodyToMono(ApiResponse.class))
            .collectList()
            .block(Duration.ofMinutes(1));
    }
    
    private void benchmarkVirtual(int concurrentRequests, int delayMs, String workType) {
        String url = "http://localhost:8080/api/virtual/" + workType + "?delayMs=" + delayMs;
        
        Flux.range(0, concurrentRequests)
            .flatMap(i -> virtualWebClient.get().uri(url).retrieve().bodyToMono(ApiResponse.class))
            .collectList()
            .block(Duration.ofMinutes(1));
    }
    
    private void benchmarkWebFlux(int concurrentRequests, int delayMs, String workType) {
        String url = "http://localhost:8080/api/webflux/" + workType + "?delayMs=" + delayMs;
        
        Flux.range(0, concurrentRequests)
            .flatMap(i -> reactiveWebClient.get().uri(url).retrieve().bodyToMono(ApiResponse.class))
            .collectList()
            .block(Duration.ofMinutes(1));
    }
} 