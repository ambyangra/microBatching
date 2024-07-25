package com.microbatch.usage;

import java.util.List;

import com.microbatch.BatchProcessor;
import com.microbatch.JobResult;
import com.microbatch.MicroBatchingLibrary;

public class MicroBatchingLibraryExample {
    
    public static void main(String[] args) throws InterruptedException {
        // Create a BatchProcessor implementation
        BatchProcessor<String> batchProcessor = new BatchProcessor<>() {
            @Override
            public void processBatch(List<String> batch) {
                // Implement the batch processing logic here
                System.out.println("Processing batch: " + batch);
            }
        };

        // Create the MicroBatchingLibrary instance
        MicroBatchingLibrary<String> library = new MicroBatchingLibrary<>(batchProcessor, 5, 1000);

        // Submit jobs to the library
        JobResult<String> result1 = library.submitJob("Job 1");
        JobResult<String> result2 = library.submitJob("Job 2");
        JobResult<String> result3 = library.submitJob("Job 3");
        JobResult<String> result4 = library.submitJob("Job 4");
        JobResult<String> result5 = library.submitJob("Job 5");
        JobResult<String> result6 = library.submitJob("Job 6");

        // Check the results
        System.out.println("Job 1 accepted: " + result1.isAccepted());
        System.out.println("Job 2 accepted: " + result2.isAccepted());
        System.out.println("Job 3 accepted: " + result3.isAccepted());
        System.out.println("Job 4 accepted: " + result4.isAccepted());
        System.out.println("Job 5 accepted: " + result5.isAccepted());
        System.out.println("Job 6 accepted: " + result6.isAccepted());

        // Update the batch size
        library.setBatchSize(3);

        // Update the batch frequency
        library.setBatchFrequencyMs(500);

        // Shutdown the library
        library.shutdown();
    }
}