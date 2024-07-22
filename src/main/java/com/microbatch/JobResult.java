package com.microbatch;

public class JobResult<T> {
    private final boolean accepted;

    public JobResult(boolean accepted) {
        this.accepted = accepted;
    }

    public boolean isAccepted() {
        return accepted;
    }
}