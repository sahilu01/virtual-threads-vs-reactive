package ss.virtual_threads_vs_reactive.model.dto;

import lombok.Data;

@Data
public class BenchmarkResult {
    private long traditionalThreadsDuration;
    private long virtualThreadsDuration;
    private long webFluxDuration;
    
    // Calculated metrics
    public double getVirtualVsTraditionalImprovement() {
        return calculateImprovement(traditionalThreadsDuration, virtualThreadsDuration);
    }
    
    public double getWebFluxVsTraditionalImprovement() {
        return calculateImprovement(traditionalThreadsDuration, webFluxDuration);
    }
    
    public double getWebFluxVsVirtualImprovement() {
        return calculateImprovement(webFluxDuration,virtualThreadsDuration);
    }
    
    private double calculateImprovement(long baseline, long improved) {
        if (baseline == 0) return 0;
        return ((double)(baseline - improved) / baseline) * 100;
    }
} 