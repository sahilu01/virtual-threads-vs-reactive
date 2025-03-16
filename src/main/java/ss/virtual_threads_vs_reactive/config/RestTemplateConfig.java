package ss.virtual_threads_vs_reactive.config;

import org.springframework.context.annotation.Configuration;

// This class is no longer needed in a pure WebFlux application
// You can either delete it entirely or keep it empty for now
@Configuration
public class RestTemplateConfig {
    // All beans removed as they're now defined in WebClientConfig
} 