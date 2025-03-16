package ss.virtual_threads_vs_reactive.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.*;

@Configuration
public class ThreadingConfig {

    @Value("${thread.pool.core-size:10}")
    private int corePoolSize;
    
    @Value("${thread.pool.max-size:50}")
    private int maxPoolSize;
    
    @Value("${thread.pool.queue-capacity:100}")
    private int queueCapacity;
    
    @Value("${thread.pool.keep-alive-seconds:60}")
    private int keepAliveSeconds;
    
    @Value("${thread.pool.allow-core-timeout:true}")
    private boolean allowCoreThreadTimeout;

    // Create a scheduler for traditional thread pool
    @Bean(name = "traditionalScheduler")
    public Scheduler traditionalScheduler() {
       // Create a custom ThreadPoolExecutor with configurable parameters
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                keepAliveSeconds, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(queueCapacity),
                new ThreadPoolExecutor.CallerRunsPolicy());
        
        executor.allowCoreThreadTimeOut(allowCoreThreadTimeout);
        
        // Name the threads for better monitoring
        executor.setThreadFactory(new CustomThreadFactory("traditional-worker-"));
        
        return Schedulers.fromExecutor(executor);
        
        //Schedulers.newBoundedElastic(corePoolSize, maxPoolSize, "bounded-elastic-thread");
    }
    
    // Create a scheduler for virtual threads
    @Bean(name = "virtualScheduler")
    public Scheduler virtualScheduler() {
        return Schedulers.fromExecutor(Executors.newVirtualThreadPerTaskExecutor());
    }
    
    // Create a scheduler for reactive programming
    @Bean(name = "reactiveScheduler")
    public Scheduler reactiveScheduler() {
        return Schedulers.newParallel("reactive", 10);
    }
    
    // Custom thread factory to name threads
    private static class CustomThreadFactory implements ThreadFactory {
        private final String prefix;
        private final ThreadFactory defaultFactory = Executors.defaultThreadFactory();
        
        public CustomThreadFactory(String prefix) {
            this.prefix = prefix;
        }
        
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = defaultFactory.newThread(r);
            thread.setName(prefix + thread.getName());
            return thread;
        }
    }
} 