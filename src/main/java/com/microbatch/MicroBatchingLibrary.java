package com.microbatch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * A micro-batching library that groups individual tasks into small batches
 * for processing. The library allows for configurable batch size and
 * processing frequency, and ensures that all submitted jobs are processed
 * before shutdown.
 *
 * @param <T> the type of jobs to be processed
 */
public class MicroBatchingLibrary<T> {
    private final BatchProcessor<T> batchProcessor;
    private volatile int batchSize;
    private volatile long batchFrequencyMs;
    private final BlockingQueue<T> jobQueue;
    private volatile boolean isShutdown = false;
    private Thread batchProcessingThread;
    private final CountDownLatch startLatch = new CountDownLatch(1);
    private final List<T> batch = new ArrayList<>();

    /**
     * Constructs a new MicroBatchingLibrary with the specified batch processor,
     * batch size, and batch frequency.
     *
     * @param batchProcessor   the batch processor to process batches of jobs
     * @param batchSize        the size of each batch
     * @param batchFrequencyMs the frequency in milliseconds to process batches
     */
    public MicroBatchingLibrary(BatchProcessor<T> batchProcessor, int batchSize, long batchFrequencyMs) {
        validateUserInput(batchSize, batchFrequencyMs);
        this.batchProcessor = batchProcessor;
        this.batchSize = batchSize;
        this.batchFrequencyMs = batchFrequencyMs;
        this.jobQueue = new LinkedBlockingQueue<>();
        this.startBatchProcessing();
    }

    /**
     * Submits a job to the micro-batching library.
     *
     * @param job the job to be submitted
     * @return the result of the job submission
     */
    public JobResult<T> submitJob(T job) {
        if (isShutdown) {
            return new JobResult<>(false);
        }
        boolean accepted = jobQueue.offer(job);
        System.out.println("jobResult for Job: " + job.toString() + " is: " + accepted);
        if (accepted) {
            startLatch.countDown(); // Allow the processing thread to start processing
        }
        return new JobResult<>(accepted);
    }

    /**
     * Sets the batch size.
     *
     * @param batchSize the new batch size
     */
    public synchronized void setBatchSize(int batchSize) {
        validateUserInput(batchSize, this.batchFrequencyMs);
        System.out.println("Batch size updated to: " + batchSize);
        this.batchSize = batchSize;
    }

    /**
     * Gets the batch size.
     *
     * @return the current batch size
     */
    public int getBatchSize() {
        return this.batchSize;
    }

    /**
     * Sets the batch frequency.
     *
     * @param batchFrequencyMs the new batch frequency in milliseconds
     */
    public synchronized void setBatchFrequencyMs(long batchFrequencyMs) {
        validateUserInput(this.batchSize, batchFrequencyMs);
        System.out.println("Batch frequency updated to: " + batchFrequencyMs + " ms");
        this.batchFrequencyMs = batchFrequencyMs;
    }

    /**
     * Gets the batch frequency.
     *
     * @return the current batch frequency in milliseconds
     */
    public long getBatchFrequencyMs() {
        return this.batchFrequencyMs;
    }

    /**
     * Shuts down the micro-batching library, ensuring all previously accepted jobs
     * are processed before returning.
     *
     * @throws InterruptedException if the shutdown process is interrupted
     */
    public void shutdown() throws InterruptedException {
        synchronized (this) {
            isShutdown = true;
        }
        if (batchProcessingThread != null) {
            batchProcessingThread.join();
        }

        // Process any remaining jobs in the queue
        while (!jobQueue.isEmpty() || !batch.isEmpty()) {
            processBatch();
        }
        if (batchProcessingThread != null) {
            batchProcessingThread.join();
        }
        processRemainingJobs();
    }

    private void startBatchProcessing() {
        batchProcessingThread = new Thread(() -> {
            try {
                startLatch.await(); // Wait for the first job to be submitted
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

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
        // Add jobs from the queue to the batch
        jobQueue.drainTo(batch, batchSize - batch.size());

        if (batch.size() >= batchSize) {
            batchProcessor.processBatch(new ArrayList<>(batch));
            batch.clear();
        } else {
            // If batch size is not met, do nothing
            System.out.println("Batch size not met, will try again in the next cycle");
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