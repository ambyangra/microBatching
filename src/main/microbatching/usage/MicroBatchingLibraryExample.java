package microbatching.usage;

import microbatching.BatchProcessor;
import microbatching.MicroBatchingLibrary;

import java.util.List;


/**
 * Example usage of the MicroBatchingLibrary.
 */
public class MicroBatchingLibraryExample {

    public static void main(String[] args) throws InterruptedException {
        BatchProcessor<String> batchProcessor = new BatchProcessor<>() {
            @Override
            public void processBatch(List<String> batch) {
                System.out.println("Processing batch: " + batch);
            }
        };

        // Create the java.microbatching.MicroBatchingLibrary instance
        MicroBatchingLibrary<String> library = new MicroBatchingLibrary<>(batchProcessor, 5, 1000);

        var initialBatchSize = library.getBatchSize();
        System.out.println("initialBatchSize: " + initialBatchSize);

        var initialFrequency = library.getBatchFrequencyMs();
        System.out.println("initialFrequency: " + initialFrequency);

        // Submit initial set of jobs to the library
        for (int i = 1; i <= 6; i++) {
            library.submitJob("Job " + i);
        }

        // Update the batch size
        library.setBatchSize(3);
        System.out.println("updated batch size: " + library.getBatchSize());

        // Update the batch frequency
        library.setBatchFrequencyMs(500);
        System.out.println("updated batch frequency: " + library.getBatchFrequencyMs());

        // Submit more jobs to the library
        for (int i = 7; i <= 20; i++) {
            library.submitJob("Job " + i);
        }

        // Allow some time for processing
        Thread.sleep(2000);

        // Submit even more jobs to the library
        for (int i = 21; i <= 30; i++) {
            library.submitJob("Job " + i);
        }

        // Allow some time for processing
        Thread.sleep(2000);

        // Submit additional jobs to test further
        for (int i = 31; i <= 50; i++) {
            library.submitJob("Job " + i);
        }

        // Shutdown the library
        library.shutdown();
    }
}