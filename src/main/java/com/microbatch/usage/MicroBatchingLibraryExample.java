package com.microbatch.usage;

import java.util.List;

import com.microbatch.BatchProcessor;
import com.microbatch.MicroBatchingLibrary;

public class MicroBatchingLibraryExample {

    public static void main(String[] args) throws InterruptedException {
        BatchProcessor<String> batchProcessor = new BatchProcessor<>() {
            @Override
            public void processBatch(List<String> batch) {
                System.out.println("Processing batch: " + batch);
            }
        };

        // Create the MicroBatchingLibrary instance
        MicroBatchingLibrary<String> library = new MicroBatchingLibrary<>(batchProcessor, 5, 1000);

        var initialBatchSize = library.getBatchSize();
        System.out.println("initialBatchSize: " + initialBatchSize);

        var initialFrequency = library.getBatchFrequencyMs();
        System.out.println("initialFrequency: " + initialFrequency);

        // Submit jobs to the library
        library.submitJob("Job 1");
        library.submitJob("Job 2");
        library.submitJob("Job 3");
        library.submitJob("Job 4");
        library.submitJob("Job 5");
        library.submitJob("Job 6");

        // Update the batch size
        library.setBatchSize(3);

        System.out.println("updated batch size: " + library.getBatchSize());

        // Update the batch frequency
        library.setBatchFrequencyMs(500);

        System.out.println("updated batch frequency: " + library.getBatchFrequencyMs());

        // Submit jobs to the library
        library.submitJob("Job 7");
        library.submitJob("Job 8");
        library.submitJob("Job 9");
        library.submitJob("Job 10");
        library.submitJob("Job 11");
        library.submitJob("Job 12");

        // Shutdown the library
        library.shutdown();
    }
}