package io.github.vaadinadminstarter.springboot;

import io.github.vaadinadminstarter.springboot.error.CorrelationIdFilter;
import io.github.vaadinadminstarter.springboot.error.MdcCorrelationIdProvider;
import io.github.vaadinadminstarter.springboot.error.ProblemDetailMapper;
import io.github.vaadinadminstarter.contracts.audit.CorrelationIdProvider;
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
