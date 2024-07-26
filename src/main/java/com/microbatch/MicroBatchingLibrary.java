package com.microbatch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class MicroBatchingLibrary<T> {
    private final BatchProcessor<T> batchProcessor;
    private volatile int batchSize;
    private volatile long batchFrequencyMs;
    private final BlockingQueue<T> jobQueue;
    private volatile boolean isShutdown = false;
    private Thread batchProcessingThread;

    public MicroBatchingLibrary(BatchProcessor<T> batchProcessor, int batchSize, long batchFrequencyMs) {
        validateUserInput(batchSize, batchFrequencyMs);
        this.batchProcessor = batchProcessor;
        this.batchSize = batchSize;
        this.batchFrequencyMs = batchFrequencyMs;
        this.jobQueue = new LinkedBlockingQueue<>();
        this.startBatchProcessing();
    }

    public JobResult<T> submitJob(T job) {
        if (isShutdown) {
            return new JobResult<>(false);
        }
        boolean accepted = jobQueue.offer(job);
        System.out.println("Job" + job.toString() + " accepted: " + accepted);
        return new JobResult<>(accepted);
    }

    public synchronized void setBatchSize(int batchSize) {
        validateUserInput(batchSize, this.batchFrequencyMs);
        System.out.println("Batch size updated");
        this.batchSize = batchSize;
    }

    public int getBatchSize() {
        return this.batchSize;
    }

    public synchronized void setBatchFrequencyMs(long batchFrequencyMs) {
        validateUserInput(this.batchSize, batchFrequencyMs);
        System.out.println("Batch frequency updated");
        this.batchFrequencyMs = batchFrequencyMs;
    }

    public long getBatchFrequencyMs() {
        return this.batchFrequencyMs;
    }

    public void shutdown() throws InterruptedException {
        synchronized (this) {
            isShutdown = true;
        }
        if (batchProcessingThread != null) {
            batchProcessingThread.join();
        }
        processRemainingJobs();
    }

    private void startBatchProcessing() {
        batchProcessingThread = new Thread(() -> {
            while (!isShutdown) {
                try {
                    processBatch();
                    TimeUnit.MILLISECONDS.sleep(batchFrequencyMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        batchProcessingThread.start();
    }

    private void processBatch() {
        List<T> batch = new ArrayList<>();
        jobQueue.drainTo(batch, batchSize);

        if (!batch.isEmpty()) {
            batchProcessor.processBatch(batch);
        } else {
            System.out.println("Batch is empty, skipping processing");
        }
    }

    private void processRemainingJobs() {
        while (!jobQueue.isEmpty()) {
            processBatch();
        }
    }

    private void validateUserInput(int batchSize, long batchFrequencyMs) {
        if (batchSize <= 0) {
            throw new IllegalArgumentException("Batch size must be a positive integer");
        }
        if (batchFrequencyMs <= 0) {
            throw new IllegalArgumentException("Batch frequency must be a positive value in milliseconds");
        }
    }
}