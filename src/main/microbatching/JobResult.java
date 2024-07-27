package microbatching;

/**
 * Class representing the result of a job submission.
 *
 * @param <T> the type of job
 */
public class JobResult<T> {
    private final boolean accepted;

    public JobResult(boolean accepted) {
        this.accepted = accepted;
    }

    public boolean isAccepted() {
        return accepted;
    }
}