package com.thumbnailapi.config;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for thread pool and concurrent request handling.
 * 
 * Configures an ExecutorService for efficient handling of multiple
 * concurrent thumbnail generation requests.
 */
@Configuration
public class ExecutorServiceConfig {

    private static final Logger logger = LogManager.getLogger(ExecutorServiceConfig.class);

    /**
     * Creates a ThreadPoolExecutor for image processing tasks.
     * 
     * Configuration targets:
     * - Core threads: 10 (always running)
     * - Max threads: 100 (scaled up as needed)
     * - Queue: 500 tasks (unbounded growth)
     * - Rejection policy: CallerRunsPolicy (backpressure)
     * - Target: handle 50-500 concurrent requests
     * 
     * @return configured ThreadPoolExecutor
     */
    @Bean
    public ThreadPoolExecutor thumbnailExecutor() {
        int corePoolSize = 10;
        int maxPoolSize = 100;
        int queueCapacity = 500;
        long keepAliveTime = 60;
        TimeUnit timeUnit = TimeUnit.SECONDS;

        ThreadPoolExecutor executor = new ThreadPoolExecutor(
            corePoolSize,
            maxPoolSize,
            keepAliveTime,
            timeUnit,
            new LinkedBlockingQueue<>(queueCapacity),
            new ThreadFactory() {
                private final ThreadFactory defaultFactory = Executors.defaultThreadFactory();
                private final java.util.concurrent.atomic.AtomicInteger count = new java.util.concurrent.atomic.AtomicInteger();

                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = defaultFactory.newThread(r);
                    thread.setName("thumbnail-processor-" + count.incrementAndGet());
                    thread.setDaemon(false);
                    return thread;
                }
            },
            new ThreadPoolExecutor.CallerRunsPolicy()
        );

        logger.info("ThreadPoolExecutor configured: corePoolSize={}, maxPoolSize={}, queueCapacity={}",
            corePoolSize, maxPoolSize, queueCapacity);

        // Add shutdown hook for graceful shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down ExecutorService");
            executor.shutdown();
            try {
                if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                    logger.warn("ExecutorService did not terminate gracefully, forcing shutdown");
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                logger.error("Interrupted while awaiting ExecutorService termination", e);
                executor.shutdownNow();
            }
        }));

        return executor;
    }
}
