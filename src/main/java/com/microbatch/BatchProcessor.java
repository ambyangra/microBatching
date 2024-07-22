package com.microbatch;

import java.util.List;

public interface BatchProcessor<T> {
    void processBatch(List<T> batch);
}