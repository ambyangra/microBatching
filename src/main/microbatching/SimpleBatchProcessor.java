package microbatching;

import java.util.List;

public class SimpleBatchProcessor<T> implements BatchProcessor<T> {
    @Override
    public void processBatch(List<T> batch) {
        /* Implementing a simple processor as this
            is to be replaced by an external API
         */
        System.out.println("Processing batch: " + batch);
    }
}