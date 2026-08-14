package io.github.youngledo.vadmin.contracts.audit;

@FunctionalInterface
public interface CorrelationIdProvider {
    String currentCorrelationId();
}
