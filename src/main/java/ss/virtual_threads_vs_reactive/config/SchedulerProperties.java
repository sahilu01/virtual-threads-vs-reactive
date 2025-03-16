package ss.virtual_threads_vs_reactive.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@ConfigurationProperties(prefix = "scheduler")
public class SchedulerProperties {
    
    private SchedulerNewParallelProperties newParallel = new SchedulerNewParallelProperties();
    private SchedulerNewSingleProperties newSingle = new SchedulerNewSingleProperties();
    private SchedulerExecutorProperties executor = new SchedulerExecutorProperties();
    private SchedulerThreadPoolProperties threadPool = new SchedulerThreadPoolProperties();
    private SchedulerNewBoundedElasticProperties newBoundedElastic = new SchedulerNewBoundedElasticProperties();

    public SchedulerNewParallelProperties getNewParallel() {
        return newParallel;
    }

    public void setNewParallel(SchedulerNewParallelProperties newParallel) {
        this.newParallel = newParallel;
    }

    public SchedulerNewSingleProperties getNewSingle() {
        return newSingle;
    }

    public void setNewSingle(SchedulerNewSingleProperties newSingle) {
        this.newSingle = newSingle;
    }

    public SchedulerExecutorProperties getExecutor() {
        return executor;
    }

    public void setExecutor(SchedulerExecutorProperties executor) {
        this.executor = executor;
    }

    public SchedulerThreadPoolProperties getThreadPool() {
        return threadPool;
    }

    public void setThreadPool(SchedulerThreadPoolProperties threadPool) {
        this.threadPool = threadPool;
    }

    public SchedulerNewBoundedElasticProperties getNewBoundedElastic() {
        return newBoundedElastic;
    }

    public void setNewBoundedElastic(SchedulerNewBoundedElasticProperties newBoundedElastic) {
        this.newBoundedElastic = newBoundedElastic;
    }

    public static class SchedulerNewParallelProperties {
        private String name = "parallel";
        private int parallelism = Runtime.getRuntime().availableProcessors();
        private boolean daemon = true;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getParallelism() {
            return parallelism;
        }

        public void setParallelism(int parallelism) {
            this.parallelism = parallelism;
        }

        public boolean getDaemon() {
            return daemon;
        }

        public void setDaemon(boolean daemon) {
            this.daemon = daemon;
        }
    }

    public static class SchedulerNewSingleProperties {
        private String name = "single";
        private boolean daemon = true;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean getDaemon() {
            return daemon;
        }

        public void setDaemon(boolean daemon) {
            this.daemon = daemon;
        }
    }

    public static class SchedulerExecutorProperties {
        public enum ExecutorType {
            WORK_STEALING_POOL,
            CACHED_THREAD_POOL,
            FIXED_THREAD_POOL,
            SINGLE_THREAD_POOL
        }

        private ExecutorType type = ExecutorType.FIXED_THREAD_POOL;
        private int parallelism = Runtime.getRuntime().availableProcessors();
        private int numberOfThread = 10;

        public ExecutorType getType() {
            return type;
        }

        public void setType(ExecutorType type) {
            this.type = type;
        }

        public int getParallelism() {
            return parallelism;
        }

        public void setParallelism(int parallelism) {
            this.parallelism = parallelism;
        }

        public int getNumberOfThread() {
            return numberOfThread;
        }

        public void setNumberOfThread(int numberOfThread) {
            this.numberOfThread = numberOfThread;
        }
    }

    public static class SchedulerThreadPoolProperties {
        private int corePoolSize = 10;
        private int maximumPoolSize = 50;
        private Duration ttl = Duration.ofSeconds(60);
        private boolean allowCoreThreadTimeOut = true;
        private int queueSize = 100;
        private QueueType queueType = QueueType.LINKED_BLOCKING_QUEUE;

        public enum QueueType {
            LINKED_BLOCKING_QUEUE,
            ARRAY_BLOCKING_QUEUE,
            SYNCHRONOUS_QUEUE,
            LINKED_TRANSFER_QUEUE
        }

        public int getCorePoolSize() {
            return corePoolSize;
        }

        public void setCorePoolSize(int corePoolSize) {
            this.corePoolSize = corePoolSize;
        }

        public int getMaximumPoolSize() {
            return maximumPoolSize;
        }

        public void setMaximumPoolSize(int maximumPoolSize) {
            this.maximumPoolSize = maximumPoolSize;
        }

        public Duration getTtl() {
            return ttl;
        }

        public void setTtl(Duration ttl) {
            this.ttl = ttl;
        }

        public boolean getAllowCoreThreadTimeOut() {
            return allowCoreThreadTimeOut;
        }

        public void setAllowCoreThreadTimeOut(boolean allowCoreThreadTimeOut) {
            this.allowCoreThreadTimeOut = allowCoreThreadTimeOut;
        }

        public int getQueueSize() {
            return queueSize;
        }

        public void setQueueSize(int queueSize) {
            this.queueSize = queueSize;
        }

        public QueueType getQueueType() {
            return queueType;
        }

        public void setQueueType(QueueType queueType) {
            this.queueType = queueType;
        }
    }

    public static class SchedulerNewBoundedElasticProperties {
        private int threadSize = 10;
        private int queueSize = 100;
        private String name = "bounded-elastic";
        private Duration ttl = Duration.ofSeconds(60);
        private boolean daemon = true;

        public int getThreadSize() {
            return threadSize;
        }

        public void setThreadSize(int threadSize) {
            this.threadSize = threadSize;
        }

        public int getQueueSize() {
            return queueSize;
        }

        public void setQueueSize(int queueSize) {
            this.queueSize = queueSize;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Duration getTtl() {
            return ttl;
        }

        public void setTtl(Duration ttl) {
            this.ttl = ttl;
        }

        public boolean getDaemon() {
            return daemon;
        }

        public void setDaemon(boolean daemon) {
            this.daemon = daemon;
        }
    }
} 