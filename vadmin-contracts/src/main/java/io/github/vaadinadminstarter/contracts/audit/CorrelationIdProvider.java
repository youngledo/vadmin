package io.github.vaadinadminstarter.contracts.audit;

@FunctionalInterface
public interface CorrelationIdProvider {
    String currentCorrelationId();
}
