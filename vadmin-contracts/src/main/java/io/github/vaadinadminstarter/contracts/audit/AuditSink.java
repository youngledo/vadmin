package io.github.vaadinadminstarter.contracts.audit;

public interface AuditSink { void append(AuditEvent event); }
