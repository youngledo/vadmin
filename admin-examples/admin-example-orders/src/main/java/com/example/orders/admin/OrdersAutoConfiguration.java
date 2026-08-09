package com.example.orders.admin;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;

import io.github.vaadinadminstarter.contracts.auth.AuthorizationService;
import io.github.vaadinadminstarter.contracts.auth.CurrentUserProvider;
import io.github.vaadinadminstarter.flow.navigation.AdminModule;

/** Spring Boot entry point for the independently packaged orders module. */
@AutoConfiguration
public class OrdersAutoConfiguration {
    @Bean
    AdminModule ordersAdminModule() {
        return OrdersAdminModule.create();
    }

    @Bean
    OrderQueryService orderQueryService() {
        return OrderQueryService.demo();
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    OrdersView ordersView(CurrentUserProvider currentUser, AuthorizationService authorization,
                          OrderQueryService orderQueryService) {
        return new OrdersView(currentUser, authorization, orderQueryService);
    }
}
