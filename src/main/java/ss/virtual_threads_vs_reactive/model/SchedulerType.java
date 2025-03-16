package ss.virtual_threads_vs_reactive.model;

public enum SchedulerType {
    SINGLE,
    PARALLEL,
    NEW_PARALLEL,
    NEW_SINGLE,
    EXECUTOR_WORK_STEALING,
    EXECUTOR_CACHED,
    EXECUTOR_FIXED,
    EXECUTOR_SINGLE,
    THREAD_POOL,
    BOUNDED_ELASTIC,
    NEW_BOUNDED_ELASTIC,
    VIRTUAL_THREAD,
    IMMEDIATE
} 