package microbatching;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

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
}