package microbatching;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MicroBatchingLibraryTest {

    private MicroBatchingLibrary<String> library;
    private List<List<String>> processedBatches;

    @BeforeEach
    void setUp() {
        processedBatches = new ArrayList<>();
        BatchProcessor<String> batchProcessor = batch -> {
            System.out.println("Processing batch: " + batch);
            processedBatches.add(new ArrayList<>(batch));
        };

        library = new MicroBatchingLibrary<>(batchProcessor, 5, 1000);
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        library.shutdown();
    }

    @Test
    void testSubmitJob() {
        JobResult<String> result = library.submitJob("Job 1");
        assertTrue(result.isAccepted());
    }

    @Test
    void testBatchProcessing() throws InterruptedException {
        for (int i = 1; i <= 6; i++) {
            library.submitJob("Job " + i);
        }

        TimeUnit.SECONDS.sleep(2);

        assertEquals(1, processedBatches.size());
        assertEquals(5, processedBatches.getFirst().size());
    }

    @Test
    void testBatchSizeChange() throws InterruptedException {
        for (int i = 1; i <= 6; i++) {
            library.submitJob("Job " + i);
        }

        TimeUnit.SECONDS.sleep(2);
        library.setBatchSize(3);

        for (int i = 7; i <= 9; i++) {
            library.submitJob("Job " + i);
        }

        TimeUnit.SECONDS.sleep(2);

        assertEquals(2, processedBatches.size());
        assertEquals(3, processedBatches.get(1).size());
    }

    @Test
    void testBatchFrequencyChange() throws InterruptedException {
        for (int i = 1; i <= 5; i++) {
            library.submitJob("Job " + i);
        }

        library.setBatchFrequencyMs(500);

        for (int i = 6; i <= 10; i++) {
            library.submitJob("Job " + i);
        }

        TimeUnit.SECONDS.sleep(3);

        assertEquals(2, processedBatches.size());
    }

    @Test
    void testShutdownProcessing() throws InterruptedException {
        for (int i = 1; i <= 8; i++) {
            library.submitJob("Job " + i);
        }

        TimeUnit.SECONDS.sleep(2);

        library.shutdown();

        assertEquals(2, processedBatches.size());
        assertEquals(3, processedBatches.get(1).size()); // Remaining jobs processed on shutdown
    }

    @Test
    void testConcurrentJobSubmissions() throws InterruptedException {
        int numJobs = 1000;
        int numThreads = 10;
        CountDownLatch latch = new CountDownLatch(numJobs);
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);

        for (int i = 0; i < numJobs; i++) {
            executorService.submit(() -> {
                library.submitJob("Job " + latch.getCount());
                latch.countDown();
            });
        }

        latch.await();
        executorService.shutdown();
        library.shutdown();
        TimeUnit.SECONDS.sleep(30);


        int expectedBatches = numJobs / library.getBatchSize() + (numJobs % library.getBatchSize() > 0 ? 1 : 0);
        assertEquals(expectedBatches, processedBatches.size());
    }

    @Test
    void testShutdownDuringBatchProcessing() throws InterruptedException {
        for (int i = 1; i <= 6; i++) {
            library.submitJob("Job " + i);
        }

        new Thread(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(500); // Simulate delay
                library.shutdown();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        TimeUnit.SECONDS.sleep(2);

        assertEquals(2, processedBatches.size());
        assertEquals(5, processedBatches.getFirst().size());
    }

    @Test
    void testJobResultHandling() {
        JobResult<String> acceptedResult = library.submitJob("Accepted Job");
        assertTrue(acceptedResult.isAccepted());

        try {
            library.shutdown();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        JobResult<String> rejectedResult = library.submitJob("Rejected Job");
        assertFalse(rejectedResult.isAccepted());
    }

    @Test
    void testBatchSizeAndFrequencyBoundaries() throws InterruptedException {
        library.setBatchSize(1);
        library.setBatchFrequencyMs(10);

        for (int i = 1; i <= 10; i++) {
            library.submitJob("Job " + i);
        }

        TimeUnit.SECONDS.sleep(2);

        assertEquals(10, processedBatches.size());
        for (List<String> batch : processedBatches) {
            assertEquals(1, batch.size());
        }
    }

    @Test
    void testBatchProcessorPerformance() throws InterruptedException {
        int numJobs = 100000;
        AtomicInteger counter = new AtomicInteger();
        BatchProcessor<String> batchProcessor = batch -> counter.addAndGet(batch.size());

        library = new MicroBatchingLibrary<>(batchProcessor, 1000, 10);

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < numJobs; i++) {
            library.submitJob("Job " + i);
        }

        library.shutdown();
        long endTime = System.currentTimeMillis();

        long elapsedTime = endTime - startTime;
        System.out.println("Processed " + numJobs + " jobs in " + elapsedTime + " ms");
        assertEquals(numJobs, counter.get());
    }


}