package io.github.youngledo.vadmin.contracts.audit;

public interface AuditSink { void append(AuditEvent event); }
