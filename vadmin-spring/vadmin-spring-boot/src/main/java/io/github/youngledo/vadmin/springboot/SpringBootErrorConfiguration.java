package io.github.youngledo.vadmin.springboot;

import io.github.youngledo.vadmin.springboot.error.CorrelationIdFilter;
import io.github.youngledo.vadmin.springboot.error.MdcCorrelationIdProvider;
import io.github.youngledo.vadmin.springboot.error.ProblemDetailMapper;
import io.github.youngledo.vadmin.contracts.audit.CorrelationIdProvider;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration(proxyBeanMethods = false)
public class SpringBootErrorConfiguration {
    @Bean
    CorrelationIdProvider correlationIdProvider() {
        return new MdcCorrelationIdProvider();
    }

    @Bean
    ProblemDetailMapper problemDetailMapper(CorrelationIdProvider correlationIdProvider) {
        return new ProblemDetailMapper(correlationIdProvider);
    }

    @Bean
    FilterRegistrationBean<CorrelationIdFilter> correlationIdFilter() {
        var registration = new FilterRegistrationBean<>(new CorrelationIdFilter());
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }
}
