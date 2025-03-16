package ss.virtual_threads_vs_reactive.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.resources.LoopResources;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    // WebClient for traditional thread pool approach
    @Bean(name = "traditionalWebClient")
    public WebClient traditionalWebClient() {
        // Use a fixed thread pool for traditional approach
        HttpClient httpClient = HttpClient.create(
                ConnectionProvider.builder("traditional")
                        .maxConnections(500)
                        .pendingAcquireTimeout(Duration.ofSeconds(60))
                        .build())
                .runOn(LoopResources.create("traditional", 20, true));
        
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
    
    // WebClient for virtual threads approach
    @Bean(name = "virtualWebClient")
    public WebClient virtualWebClient() {
        // For virtual threads, we'll use a simpler approach
        HttpClient httpClient = HttpClient.create(
                ConnectionProvider.builder("virtual")
                        .maxConnections(1000)
                        .pendingAcquireTimeout(Duration.ofSeconds(60))
                        .build());
        
        // We don't need to customize the LoopResources for the client
        // The client will use the default event loop, but our controller will use virtual threads
        
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
    
    // WebClient for reactive approach (default WebFlux behavior)
    @Bean(name = "reactiveWebClient")
    public WebClient reactiveWebClient() {
        return WebClient.create();
    }
    
    // Default WebClient builder
    @Bean(name = "customWebClientBuilder")
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
} 