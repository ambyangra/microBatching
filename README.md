# MicroBatching Library

A micro-batching library that groups individual tasks into small batches for processing. This can improve throughput by
reducing the number of requests made to a downstream system. The library allows for configurable batch size and
processing frequency, and ensures that all submitted jobs are processed before shutdown.

## Features

- Submit individual jobs and receive a microbatching.JobResult.
- Process accepted jobs in batches using a provided microbatching.BatchProcessor.
- Configurable batch size and frequency.
- Graceful shutdown to ensure all previously accepted jobs are processed.

## Usage

### Installation

Clone the repository:

```
bash
git clone <https://github.com/ambyangra/microbatching.git>
cd microbatching
```

### Code Example

Here is an example usage of the MicroBatching library:
```
java
package microbatching;

public class TestExamle {

    public static void main(String[] args) {
        microbatching.BatchProcessor<String> processor = batch -> {
            System.out.println("Processing batch: " + batch);
        };

        microbatching.MicroBatchingLibrary<String> library = new MicroBatchingLibrary<>(processor, 5, 1000);

        for (int i = 1; i <= 12; i++) {
            library.submitJob("Job " + i);
            try {
                // Simulate varying submission times
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Adjust batch size and frequency after initial submissions
        library.setBatchSize(3);
        library.setBatchFrequencyMs(500);

        try {
            // Allow some time for processing
            Thread.sleep(5000);
            library.shutdown();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

    }
}
```
An example usage class, `MicroBatchingLibraryExample`, is also provided in `src/main/microbatching/usage`

### Classes

#### microbatching.MicroBatchingLibrary

A micro-batching library that groups individual tasks into small batches for processing.

*Constructor:*

java
public microbatching.MicroBatchingLibrary(microbatching.BatchProcessor<T> batchProcessor, int batchSize, long
batchFrequencyMs)

- batchProcessor: The batch processor to process batches of jobs.
- batchSize: The size of each batch.
- batchFrequencyMs: The frequency in milliseconds to process batches.

*Methods:*

- microbatching.JobResult<T> submitJob(T job): Submits a job to the micro-batching library.
- void setBatchSize(int batchSize): Sets the batch size.
- int getBatchSize(): Gets the current batch size.
- void setBatchFrequencyMs(long batchFrequencyMs): Sets the batch frequency.
- long getBatchFrequencyMs(): Gets the current batch frequency in milliseconds.
- void shutdown() throws InterruptedException: Shuts down the micro-batching library, ensuring all previously accepted
  jobs are processed before returning.

#### microbatching.BatchProcessor

Interface for processing batches of jobs.

*Method:*

java
void processBatch(List<T> batch)

- batch: The batch of jobs to be processed.

#### microbatching.JobResult

Class representing the result of a job submission.

*Constructor:*

java
public microbatching.JobResult(boolean accepted)

- accepted: Indicates whether the job submission was successful.

*Methods:*

- boolean isAccepted(): Returns whether the job submission was successful.
