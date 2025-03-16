# Virtual Threads vs Reactive

A comprehensive demonstration and benchmarking application for comparing different threading models and scheduler implementations in Java. This project showcases the performance characteristics of traditional platform threads, virtual threads (Project Loom), and reactive programming using Spring WebFlux.

## Overview

This application provides a visual and interactive way to compare the performance of:

1. **Traditional Platform Threads** - Using Java's standard threading model with bounded thread pools.
2. **Virtual Threads** - Using Java 21's virtual threads (Project Loom).
3. **Reactive Programming** - Using Spring WebFlux and Project Reactor.
4. **Various Reactor Schedulers** - Comparing different scheduler implementations.

The application simulates different types of workloads (I/O-bound, CPU-bound, and asynchronous operations) and measures performance metrics such as throughput, response time, and success rate.

## Features

- **Thread Model Benchmarking**: Compare traditional threads, virtual threads, and reactive programming.
- **Scheduler Benchmarking**: Test and compare different Reactor scheduler implementations.
- **Configurable Parameters**: Adjust concurrent requests, delay times, and work types.
- **Visual Results**: View benchmark results with interactive charts and tables.
- **API Endpoints**: Access all functionality programmatically through REST APIs.
- **Configurable Schedulers**: Customize scheduler properties through `application.properties`.
  
## Screenshots

<img width="2553" alt="Screenshot 2025-03-16 at 5 07 36 PM" src="https://github.com/user-attachments/assets/5a1ab25d-7f24-449e-9766-f9b4bc935460" />
<img width="1447" alt="Screenshot 2025-03-16 at 5 07 13 PM" src="https://github.com/user-attachments/assets/952871f8-f48d-4947-9043-7462a76c6189" />
<img width="990" alt="Screenshot 2025-03-16 at 5 08 08 PM" src="https://github.com/user-attachments/assets/c517f84d-b3c2-456a-933c-cca52e56f30b" />


## Scheduler Types

The application supports benchmarking the following scheduler types:

- **SINGLE**: Single-threaded scheduler.
- **PARALLEL**: Multi-threaded scheduler with CPU core count threads.
- **NEW_PARALLEL**: Custom parallel scheduler with a configurable thread count.
- **NEW_SINGLE**: Custom single-threaded scheduler.
- **EXECUTOR_WORK_STEALING**: Work-stealing thread pool.
- **EXECUTOR_CACHED**: Cached thread pool.
- **EXECUTOR_FIXED**: Fixed-size thread pool.
- **EXECUTOR_SINGLE**: Single-thread executor.
- **THREAD_POOL**: Custom thread pool executor.
- **BOUNDED_ELASTIC**: Bounded elastic scheduler (default in WebFlux).
- **NEW_BOUNDED_ELASTIC**: Custom bounded elastic scheduler.
- **VIRTUAL_THREAD**: Java 21's virtual threads.
- **IMMEDIATE**: Immediate scheduler (executes on the caller thread).

## Getting Started

### Prerequisites

- Java 21 or higher.
- Maven 3.6 or higher.
- Spring Boot 3.x.

### Running the Application

1. Clone the repository:
   ```sh
   git clone https://github.com/sahilu01/virtual-threads-vs-reactive.git
   cd virtual-threads-vs-reactive
   ```

2. Build the application:
   ```sh
   mvn clean package
   ```

3. Run the application:
   ```sh
   java -jar target/virtual-threads-vs-reactive-0.0.1-SNAPSHOT.jar
   ```

4. Access the application:
   ```sh
   http://localhost:8080
   ```

## Usage

### Web Interface

The application provides an intuitive web interface with the following sections:

1. **Home Page**: Overview of available thread models and benchmarks.
2. **Traditional Threads**: Test traditional platform threads.
3. **Virtual Threads**: Test Java 21's virtual threads.
4. **WebFlux**: Test reactive programming with WebFlux.
5. **Thread Models Benchmark**: Compare all thread models.
6. **Scheduler Benchmark**: Compare different Reactor schedulers.

### API Endpoints

The application exposes the following REST APIs:

#### Thread Model APIs

- **Traditional Threads**: `/api/traditional/{type}?delayMs={delay}`
- **Virtual Threads**: `/api/virtual/{type}?delayMs={delay}`
- **WebFlux**: `/api/webflux/{type}?delayMs={delay}`

Where:
- `{type}` can be: `io`, `cpu`, or `async`.
- `{delay}` is the simulated delay in milliseconds.

#### Benchmark APIs

- **Thread Models Benchmark**: `/api/benchmark/run?concurrentRequests={requests}&delayMs={delay}&workType={type}`
- **Scheduler Benchmark**: `/api/scheduler-benchmark/run?concurrentRequests={requests}&delayMs={delay}&workType={type}&schedulerTypes={types}`

Where:
- `{requests}` is the number of concurrent requests.
- `{delay}` is the simulated delay in milliseconds.
- `{type}` can be: `io`, `cpu`, or `async`.
- `{types}` is a comma-separated list of scheduler types to benchmark.

## Configuration

### Scheduler Properties

You can configure the scheduler properties in `application.properties`:

```properties
# New Parallel Scheduler
scheduler.new-parallel.name=custom-parallel
scheduler.new-parallel.parallelism=16
scheduler.new-parallel.daemon=true

# New Single Scheduler
scheduler.new-single.name=custom-single
scheduler.new-single.daemon=true

# Executor Scheduler
scheduler.executor.parallelism=16
scheduler.executor.number-of-threads=20
scheduler.executor.type=FIXED_THREAD_POOL

# Thread Pool Scheduler
scheduler.thread-pool.core-pool-size=10
scheduler.thread-pool.maximum-pool-size=50
scheduler.thread-pool.ttl=60s
scheduler.thread-pool.allow-core-thread-time-out=true
scheduler.thread-pool.queue-size=100
scheduler.thread-pool.queue-type=LINKED_BLOCKING_QUEUE

# New Bounded Elastic Scheduler
scheduler.new-bounded-elastic.thread-size=20
scheduler.new-bounded-elastic.queue-size=1000
scheduler.new-bounded-elastic.name=custom-bounded-elastic
scheduler.new-bounded-elastic.ttl=60s
scheduler.new-bounded-elastic.daemon=true
```

## Architecture

The application follows a clean architecture with the following components:

- **Controllers**: Handle HTTP requests and responses.
- **Services**: Contain business logic and benchmarking functionality.
- **Models**: Define data structures and DTOs.
- **Configuration**: Configure schedulers and thread pools.
- **Templates**: Thymeleaf templates for the web interface.

## Key Components

- **SchedulerBenchmarkService**: Runs benchmarks for different scheduler types.
- **SchedulerFactory**: Creates and configures different scheduler implementations.
- **WorkService**: Simulates different types of workloads.
- **UIController**: Handles web interface requests.
- **SchedulerBenchmarkController**: Exposes scheduler benchmark APIs.

## Performance Considerations

- **I/O-bound workloads**: Virtual threads and reactive programming typically outperform traditional threads.
- **CPU-bound workloads**: Traditional threads may perform better due to less overhead.
- **High concurrency**: Virtual threads and reactive programming excel with high concurrency.
- **Low latency**: Different schedulers have different latency characteristics.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.



## Acknowledgments

- Spring Boot and Spring WebFlux teams.
- Project Reactor team.
- Project Loom team.
