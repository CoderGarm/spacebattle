package de.yuga.spacebattle;

import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;

public class ManualShutdownFailureAnalyzer extends AbstractFailureAnalyzer<ManualShutdownException> {
    @Override
    protected FailureAnalysis analyze(final Throwable rootFailure, final ManualShutdownException cause) {
        return new FailureAnalysis(cause.getMessage(), cause.getAction(), cause);
    }
}
