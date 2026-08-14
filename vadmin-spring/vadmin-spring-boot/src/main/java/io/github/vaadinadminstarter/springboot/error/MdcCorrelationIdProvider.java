package io.github.vaadinadminstarter.springboot.error;

import io.github.vaadinadminstarter.contracts.audit.CorrelationIdProvider;
import org.slf4j.MDC;

public final class MdcCorrelationIdProvider implements CorrelationIdProvider {
    @Override
    public String currentCorrelationId() {
        return MDC.get(CorrelationIdFilter.MDC_KEY);
    }
}
