package microbatching;

import java.util.List;

/**
 * Interface representing a batch processor that processes a list of jobs.
 *
 * @param <T> the type of jobs to be processed
 */
public interface BatchProcessor<T> {
    void processBatch(List<T> batch);
}