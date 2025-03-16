package ss.virtual_threads_vs_reactive.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;
import ss.virtual_threads_vs_reactive.model.SchedulerType;
import ss.virtual_threads_vs_reactive.model.dto.ApiResponse;
import ss.virtual_threads_vs_reactive.model.dto.BenchmarkResult;
import ss.virtual_threads_vs_reactive.model.dto.SchedulerBenchmarkResult;
import ss.virtual_threads_vs_reactive.service.BenchmarkService;
import ss.virtual_threads_vs_reactive.service.SchedulerBenchmarkService;

import java.util.List;

@Controller
@Slf4j
public class UIController {

    @Autowired
    private BenchmarkService benchmarkService;
    
    @Autowired
    private TraditionalController traditionalController;
    
    @Autowired
    private VirtualThreadController virtualThreadController;
    
    @Autowired
    private WebFluxController webFluxController;

    @Autowired
    private SchedulerBenchmarkService schedulerBenchmarkService;

    @GetMapping("/")
    public String home() {
        return "index";
    }
    
    @GetMapping("/traditional")
    public String traditionalPage() {
        return "traditional";
    }
    
    @GetMapping("/virtual")
    public String virtualPage() {
        return "virtual";
    }
    
    @GetMapping("/webflux")
    public String webfluxPage() {
        return "webflux";
    }

    @GetMapping("/benchmark")
    public String benchmark(
            @RequestParam(defaultValue = "100") int concurrentRequests,
            @RequestParam(defaultValue = "1000") int delayMs,
            @RequestParam(defaultValue = "io") String workType,
            Model model) {
        
        log.info("Running benchmark from UI with {} requests, {}ms delay, type: {}", 
                concurrentRequests, delayMs, workType);
        
        BenchmarkResult result = benchmarkService.runBenchmark(concurrentRequests, delayMs, workType);
        
        model.addAttribute("result", result);
        model.addAttribute("params", new BenchmarkParams(concurrentRequests, delayMs, workType));
        
        return "benchmark-results";
    }
    
    @GetMapping("/run-traditional")
    public String runTraditional(
            @RequestParam(defaultValue = "1000") int delayMs,
            @RequestParam(defaultValue = "io") String workType,
            Model model) {
        
        log.info("Running traditional thread test with {}ms delay, type: {}", delayMs, workType);
        
        Mono<ApiResponse> responseMono;
        if ("cpu".equals(workType)) {
            responseMono = traditionalController.performCpuWork(10000000);
        } else if ("async".equals(workType)) {
            responseMono = traditionalController.performAsyncWork(delayMs);
        } else {
            responseMono = traditionalController.performIoWork(delayMs);
        }
        
        ApiResponse response = responseMono.block();
        model.addAttribute("response", response);
        model.addAttribute("strategy", "Traditional Threads");
        model.addAttribute("workType", workType);
        model.addAttribute("delayMs", delayMs);
        
        return "result";
    }
    
    @GetMapping("/run-virtual")
    public String runVirtual(
            @RequestParam(defaultValue = "1000") int delayMs,
            @RequestParam(defaultValue = "io") String workType,
            Model model) {
        
        log.info("Running virtual thread test with {}ms delay, type: {}", delayMs, workType);
        
        Mono<ApiResponse> responseMono;
        if ("cpu".equals(workType)) {
            responseMono = virtualThreadController.performCpuWork(10000000);
        } else if ("async".equals(workType)) {
            responseMono = virtualThreadController.performAsyncWork(delayMs);
        } else {
            responseMono = virtualThreadController.performIoWork(delayMs);
        }
        
        ApiResponse response = responseMono.block();
        model.addAttribute("response", response);
        model.addAttribute("strategy", "Virtual Threads");
        model.addAttribute("workType", workType);
        model.addAttribute("delayMs", delayMs);
        
        return "result";
    }
    
    @GetMapping("/run-webflux")
    public String runWebFlux(
            @RequestParam(defaultValue = "1000") int delayMs,
            @RequestParam(defaultValue = "io") String workType,
            Model model) {
        
        log.info("Running WebFlux test with {}ms delay, type: {}", delayMs, workType);
        
        Mono<ApiResponse> responseMono;
        if ("cpu".equals(workType)) {
            responseMono = webFluxController.performCpuWork(10000000);
        } else if ("async".equals(workType)) {
            responseMono = webFluxController.performAsyncWork(delayMs);
        } else {
            responseMono = webFluxController.performIoWork(delayMs);
        }
        
        ApiResponse response = responseMono.block();
        model.addAttribute("response", response);
        model.addAttribute("strategy", "WebFlux");
        model.addAttribute("workType", workType);
        model.addAttribute("delayMs", delayMs);
        
        return "result";
    }
    
    @GetMapping("/scheduler-benchmark")
    public String schedulerBenchmarkPage(Model model) {
        model.addAttribute("schedulerTypes", SchedulerType.values());
        return "scheduler-benchmark";
    }
    
    @GetMapping("/run-scheduler-benchmark")
    public String runSchedulerBenchmark(
            @RequestParam(defaultValue = "100") int concurrentRequests,
            @RequestParam(defaultValue = "1000") int delayMs,
            @RequestParam(defaultValue = "io") String workType,
            @RequestParam(required = false) List<SchedulerType> schedulerTypes,
            Model model) {
        
        log.info("Running scheduler benchmark from UI with {} requests, {}ms delay, type: {}, schedulers: {}", 
                concurrentRequests, delayMs, workType, schedulerTypes);
        
        if (schedulerTypes == null || schedulerTypes.isEmpty()) {
            schedulerTypes = List.of(
                    SchedulerType.BOUNDED_ELASTIC,
                    SchedulerType.VIRTUAL_THREAD,
                    SchedulerType.PARALLEL
            );
        }
        
        SchedulerBenchmarkResult result = schedulerBenchmarkService.runBenchmark(
                concurrentRequests, delayMs, workType, schedulerTypes);
        
        model.addAttribute("result", result);
        
        return "scheduler-benchmark-results";
    }
    
    // Simple class to hold benchmark parameters for the UI
    public static class BenchmarkParams {
        private final int concurrentRequests;
        private final int delayMs;
        private final String workType;
        
        public BenchmarkParams(int concurrentRequests, int delayMs, String workType) {
            this.concurrentRequests = concurrentRequests;
            this.delayMs = delayMs;
            this.workType = workType;
        }
        
        public int getConcurrentRequests() {
            return concurrentRequests;
        }
        
        public int getDelayMs() {
            return delayMs;
        }
        
        public String getWorkType() {
            return workType;
        }
    }
} 