package microbatching;

import java.util.List;

public class SimpleBatchProcessor<T> implements BatchProcessor<T> {
    @Override
    public void processBatch(List<T> batch) {
        // Implement the batch processing logic here
        System.out.println("Processing batch: " + batch);
    }
}