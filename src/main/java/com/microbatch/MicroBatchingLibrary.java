package com.microbatch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MicroBatchingLibrary<T> {
    private final BatchProcessor<T> batchProcessor;
    private final int batchSize;
    private final long batchFrequencyMillis;
    private final List<T> pendingJobs;
    private final ScheduledExecutorService scheduler;
    private boolean isShutdown;

    public MicroBatchingLibrary(BatchProcessor<T> batchProcessor, int batchSize, long batchFrequencyMillis) {
        this.batchProcessor = batchProcessor;
        this.batchSize = batchSize;
        this.batchFrequencyMillis = batchFrequencyMillis;
        this.pendingJobs = new ArrayList<>();
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.isShutdown = false;

        // Schedule the batch processing task
        this.scheduler.scheduleAtFixedRate(this::processBatch, batchFrequencyMillis, batchFrequencyMillis,
                TimeUnit.MILLISECONDS);
    }

    public JobResult<T> submitJob(T job) {
        synchronized (pendingJobs) {
            if (isShutdown) {
                throw new IllegalStateException("MicroBatchingLibrary is already shutdown");
            }
            pendingJobs.add(job);
            return new JobResult<>(true);
        }
    }

    public void shutdown() {
        synchronized (pendingJobs) {
            isShutdown = true;
            scheduler.shutdown();
            try {
                scheduler.awaitTermination(batchFrequencyMillis * 2, TimeUnit.MILLISECONDS);
                processBatch();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void processBatch() {
        synchronized (pendingJobs) {
            if (pendingJobs.isEmpty()) {
                return;
            }

            List<T> batch = new ArrayList<>();
            for (int i = 0; i < Math.min(batchSize, pendingJobs.size()); i++) {
                batch.add(pendingJobs.remove(0));
            }

            batchProcessor.processBatch(batch);
        }
    }
}