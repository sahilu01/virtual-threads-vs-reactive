    package ss.virtual_threads_vs_reactive.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import ss.virtual_threads_vs_reactive.model.SchedulerType;

import java.util.concurrent.*;

@Component
public class SchedulerFactory {

    @Autowired
    private SchedulerProperties properties;

    public Scheduler createScheduler(SchedulerType type) {
        return switch (type) {
            case SINGLE -> Schedulers.single();
            case PARALLEL -> Schedulers.parallel();
            case NEW_PARALLEL -> newParallelScheduler(properties.getNewParallel());
            case NEW_SINGLE -> newSingleScheduler(properties.getNewSingle());
            case EXECUTOR_WORK_STEALING -> fromExecutor(properties.getExecutor(), SchedulerProperties.SchedulerExecutorProperties.ExecutorType.WORK_STEALING_POOL);
            case EXECUTOR_CACHED -> fromExecutor(properties.getExecutor(), SchedulerProperties.SchedulerExecutorProperties.ExecutorType.CACHED_THREAD_POOL);
            case EXECUTOR_FIXED -> fromExecutor(properties.getExecutor(), SchedulerProperties.SchedulerExecutorProperties.ExecutorType.FIXED_THREAD_POOL);
            case EXECUTOR_SINGLE -> fromExecutor(properties.getExecutor(), SchedulerProperties.SchedulerExecutorProperties.ExecutorType.SINGLE_THREAD_POOL);
            case THREAD_POOL -> newThreadPoolScheduler(properties.getThreadPool());
            case BOUNDED_ELASTIC -> Schedulers.boundedElastic();
            case NEW_BOUNDED_ELASTIC -> newBoundedElastic(properties.getNewBoundedElastic());
            case VIRTUAL_THREAD -> Schedulers.fromExecutorService(Executors.newVirtualThreadPerTaskExecutor());
            case IMMEDIATE -> Schedulers.immediate();
        };
    }

    private Scheduler newParallelScheduler(SchedulerProperties.SchedulerNewParallelProperties properties) {
        return Schedulers.newParallel(properties.getName(), properties.getParallelism(), properties.getDaemon());
    }

    private Scheduler newSingleScheduler(SchedulerProperties.SchedulerNewSingleProperties properties) {
        return Schedulers.newSingle(properties.getName(), properties.getDaemon());
    }

    private Scheduler fromExecutor(SchedulerProperties.SchedulerExecutorProperties properties, 
                                  SchedulerProperties.SchedulerExecutorProperties.ExecutorType type) {
        return switch (type) {
            case WORK_STEALING_POOL -> Schedulers.fromExecutorService(
                    Executors.newWorkStealingPool(properties.getParallelism()));
            case CACHED_THREAD_POOL -> Schedulers.fromExecutorService(
                    Executors.newCachedThreadPool());
            case FIXED_THREAD_POOL -> Schedulers.fromExecutorService(
                    Executors.newFixedThreadPool(properties.getNumberOfThread()));
            case SINGLE_THREAD_POOL -> Schedulers.fromExecutorService(
                    Executors.newSingleThreadExecutor());
        };
    }

    private Scheduler newThreadPoolScheduler(SchedulerProperties.SchedulerThreadPoolProperties properties) {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                properties.getCorePoolSize(),
                properties.getMaximumPoolSize(),
                properties.getTtl().toMillis(),
                TimeUnit.MILLISECONDS,
                createBlockingQueue(properties));
        
        executor.allowCoreThreadTimeOut(properties.getAllowCoreThreadTimeOut());
        return Schedulers.fromExecutor(executor);
    }

    private BlockingQueue<Runnable> createBlockingQueue(SchedulerProperties.SchedulerThreadPoolProperties properties) {
        return switch (properties.getQueueType()) {
            case LINKED_BLOCKING_QUEUE -> new LinkedBlockingQueue<>(properties.getQueueSize());
            case ARRAY_BLOCKING_QUEUE -> new ArrayBlockingQueue<>(properties.getQueueSize());
            case SYNCHRONOUS_QUEUE -> new SynchronousQueue<>();
            case LINKED_TRANSFER_QUEUE -> new LinkedTransferQueue<>();
        };
    }

    private Scheduler newBoundedElastic(SchedulerProperties.SchedulerNewBoundedElasticProperties properties) {
        return Schedulers.newBoundedElastic(
                properties.getThreadSize(),
                properties.getQueueSize(),
                properties.getName(),
                (int) properties.getTtl().getSeconds(),
                properties.getDaemon());
    }
} 