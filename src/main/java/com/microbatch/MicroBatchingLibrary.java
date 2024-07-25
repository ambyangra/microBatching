package com.microbatch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MicroBatchingLibrary<T> {
    private final BatchProcessor<T> batchProcessor;
    private int batchSize;
    private long batchFrequencyMs;
    private final BlockingQueue<T> jobQueue;
    private boolean isShutdown = false;

    public MicroBatchingLibrary(BatchProcessor<T> batchProcessor, int batchSize, long batchFrequencyMs) {
        validateUserInput(batchSize, batchFrequencyMs);
        this.batchProcessor = batchProcessor;
        this.batchSize = batchSize;
        this.batchFrequencyMs = batchFrequencyMs;
        this.jobQueue = new LinkedBlockingQueue<>();
        // The batch processing is started from an instance of MicroBatchingLibrary
        this.startBatchProcessing();
    }

    public JobResult<T> submitJob(T job) {
        synchronized (this) {
            if (isShutdown) {
                throw new IllegalStateException("MicroBatchingLibrary is already shutdown");
            }
            // Adds the job submitted by the User to a job queue
            jobQueue.offer(job);
            return new JobResult<>(true);
        }
    }

    // Allows the user to update batchSize
    public void setBatchSize(int batchSize) {
        validateUserInput(batchSize, this.batchFrequencyMs);
        this.batchSize = batchSize;
    }

    // Allows the user to check/view the batchSize
    public int getBatchSize() {
        return this.batchSize;
    }

    // Allows the user to update batchFrequency
    public void setBatchFrequencyMs(long batchFrequencyMs) {
        validateUserInput(this.batchSize, batchFrequencyMs);
        this.batchFrequencyMs = batchFrequencyMs;
    }

    // Allows the user to check/view the batchFrequency
    public long getBatchFrequencyMs() {
        return this.batchFrequencyMs;
    }

    // this has to make sure all previously accepted jobs are processed
    public void shutdown() throws InterruptedException {
        synchronized (this) {
            isShutdown = true;
            // it waits for the jobs to be processed, and checks the job queue every 100ms
            while (!jobQueue.isEmpty()) {
                Thread.sleep(100);
            }
        }
    }

    private void startBatchProcessing() {
        new Thread(() -> {
            while (!isShutdown) {
                processBatch();
                try {
                    Thread.sleep(batchFrequencyMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
    }

    private void processBatch() {
        synchronized (this) {
            List<T> batch = new ArrayList<>(batchSize);
            // This will batch the jobs and call the batchProcessor with the batch
            // If there are more jobs than the batchSize, it ignores them for now, meaning
            // the jobs will remain in the queue till next time processBatch() is called.
            // This ensures the jobs are not lost/ignored.
            for (int i = 0; i < batchSize; i++) {
                T job = jobQueue.poll();
                if (job == null) {
                    break;
                }
                batch.add(job);
            }
            if (!batch.isEmpty()) {
                batchProcessor.processBatch(batch);
            }
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