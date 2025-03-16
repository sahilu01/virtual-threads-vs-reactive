package ss.virtual_threads_vs_reactive.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse {
    private boolean success;
    private String message;
    private long responseTimeMs;
    
    public ApiResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.responseTimeMs = 0;
    }

    public boolean success() {
        return success;
    }
} 