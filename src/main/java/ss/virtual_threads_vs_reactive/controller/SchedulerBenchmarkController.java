package ss.virtual_threads_vs_reactive.controller;

import lombok.extern.slf4j.Slf4j;
import ss.virtual_threads_vs_reactive.model.SchedulerType;
import ss.virtual_threads_vs_reactive.model.dto.SchedulerBenchmarkResult;
import ss.virtual_threads_vs_reactive.service.SchedulerBenchmarkService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/scheduler-benchmark")
@Slf4j
public class SchedulerBenchmarkController {

    @Autowired
    private SchedulerBenchmarkService schedulerBenchmarkService;

    @GetMapping("/run")
    public ResponseEntity<SchedulerBenchmarkResult> runBenchmark(
            @RequestParam(defaultValue = "100") int concurrentRequests,
            @RequestParam(defaultValue = "1000") int delayMs,
            @RequestParam(defaultValue = "io") String workType,
            @RequestParam(defaultValue = "BOUNDED_ELASTIC,VIRTUAL_THREAD,PARALLEL") String schedulerTypes) {
        
        try {
            List<SchedulerType> types = parseSchedulerTypes(schedulerTypes);
            
            SchedulerBenchmarkResult result = schedulerBenchmarkService.runBenchmark(
                    concurrentRequests, delayMs, workType, types);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error running scheduler benchmark", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    private List<SchedulerType> parseSchedulerTypes(String schedulerTypesStr) {
        return Arrays.stream(schedulerTypesStr.split(","))
                .map(String::trim)
                .map(SchedulerType::valueOf)
                .collect(Collectors.toList());
    }
} 