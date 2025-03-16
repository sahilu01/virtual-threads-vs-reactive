package ss.virtual_threads_vs_reactive.controller;

import lombok.extern.slf4j.Slf4j;
import ss.virtual_threads_vs_reactive.model.dto.BenchmarkResult;
import ss.virtual_threads_vs_reactive.service.BenchmarkService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/benchmark")
@Slf4j
public class BenchmarkController {

    @Autowired
    private BenchmarkService benchmarkService;

    @GetMapping("/run")
    public ResponseEntity<BenchmarkResult> runBenchmark(
            @RequestParam(defaultValue = "100") int concurrentRequests,
            @RequestParam(defaultValue = "1000") int delayMs,
            @RequestParam(defaultValue = "io") String workType) {
        
        BenchmarkResult result = benchmarkService.runBenchmark(concurrentRequests, delayMs, workType);
        return ResponseEntity.ok(result);
    }
} 