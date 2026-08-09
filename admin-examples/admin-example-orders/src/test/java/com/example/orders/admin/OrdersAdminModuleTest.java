package com.example.orders.admin;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.vaadinadminstarter.contracts.auth.CurrentUser;
import io.github.vaadinadminstarter.contracts.auth.AuthorizationService;
import io.github.vaadinadminstarter.contracts.auth.CurrentUserProvider;
import io.github.vaadinadminstarter.contracts.auth.PermissionCode;
import io.github.vaadinadminstarter.flow.navigation.AdminModule;
import io.github.vaadinadminstarter.contracts.navigation.PagedQuery;
import io.github.vaadinadminstarter.flow.patterns.AdminPageFrame;
import io.github.vaadinadminstarter.flow.patterns.DataWorkspace;

class OrdersAdminModuleTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(OrdersAutoConfiguration.class, AuthenticationConfiguration.class);

    @Test
    void contributesTheOrdersPageAndBothTranslationBundles() {
        var module = OrdersAdminModule.create();

        assertThat(module.moduleId()).isEqualTo("orders");
        assertThat(module.permissions()).extracting(PermissionCode::value).containsExactly("orders:order:read");
        assertThat(module.navigationGroups())
                .singleElement()
                .satisfies(group -> {
                    assertThat(group.id()).isEqualTo("business");
                    assertThat(group.titleKey()).isEqualTo("orders.nav.group");
                });
        assertThat(module.pages())
                .singleElement()
                .satisfies(page -> {
                    assertThat(page.route()).isEqualTo("orders");
                    assertThat(page.requiredPermission().value()).isEqualTo("orders:order:read");
                    assertThat(page.viewType()).isEqualTo(OrdersView.class);
                });
        assertThat(module.messageBundles())
                .singleElement()
                .satisfies(bundle -> {
                    assertThat(bundle.baseName()).isEqualTo("orders.i18n.messages");
                    assertThat(resourceExists(bundle.baseName(), Locale.SIMPLIFIED_CHINESE)).isTrue();
                    assertThat(resourceExists(bundle.baseName(), Locale.US)).isTrue();
                });
    }

    @Test
    void returnsStableRowsForTheReadOnlyOrdersWorkspace() {
        var orders = OrderQueryService.demo();

        assertThat(orders.orders(new PagedQuery(0, 2, "number", true, Map.of())))
                .satisfies(page -> {
                    assertThat(page.total()).isEqualTo(3);
                    assertThat(page.items()).extracting(OrderRow::number)
                            .containsExactly("ORD-1001", "ORD-1002");
                });

        var currentUser = new CurrentUser(UUID.randomUUID(), "orders-reader",
                Set.of(PermissionCode.of("orders:order:read")), 0);
        var authorization = new AuthorizationService() {
            @Override public boolean hasPermission(CurrentUser user, PermissionCode permission) {
                return user.permissions().contains(permission);
            }
            @Override public void requirePermission(CurrentUser user, PermissionCode permission) {
                if (!hasPermission(user, permission)) throw new SecurityException("permission denied");
            }
        };
        var view = new OrdersView(() -> Optional.of(currentUser), authorization, orders);

        assertThat(view.getChildren())
                .singleElement()
                .isInstanceOfSatisfying(AdminPageFrame.class,
                        frame -> assertThat(frame.getChildren()).anyMatch(DataWorkspace.class::isInstance));
    }

    @Test
    void autoConfigurationProvidesTheDescriptorQueryServiceAndPrototypeView() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(AdminModule.class);
            assertThat(context).hasSingleBean(OrderQueryService.class);
            assertThat(context).hasSingleBean(OrdersView.class);
            assertThat(context.getBean(OrdersView.class)).isNotSameAs(context.getBean(OrdersView.class));
        });
    }

    private boolean resourceExists(String baseName, Locale locale) {
        return java.util.ResourceBundle.getBundle(baseName, locale).containsKey("orders.title");
    }

    @Configuration(proxyBeanMethods = false)
    static class AuthenticationConfiguration {
        @Bean
        CurrentUserProvider currentUserProvider() {
            return Optional::empty;
        }

        @Bean
        AuthorizationService authorizationService() {
            return new AuthorizationService() {
                @Override public boolean hasPermission(CurrentUser user, PermissionCode permission) {
                    return user.permissions().contains(permission);
                }
                @Override public void requirePermission(CurrentUser user, PermissionCode permission) {
                    if (!hasPermission(user, permission)) throw new SecurityException("permission denied");
                }
            };
        }
    }
}
