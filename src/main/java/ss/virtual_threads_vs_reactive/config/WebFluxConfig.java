package ss.virtual_threads_vs_reactive.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebFluxConfig {

    // Configure WebFlux to use a custom scheduler
    @Bean
    public Runnable configureReactorSchedulers() {
        // Set the number of parallel threads for WebFlux
        System.setProperty("reactor.netty.ioWorkerCount", "10");
        
        // For metrics, we'll use Spring Boot's auto-configuration
        // which already sets up Micrometer with Reactor
        
        return () -> {}; // Return a no-op Runnable
    }
} 