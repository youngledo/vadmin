# Extension Guide

VAdmin publication coordinate: `io.github.youngledo:vadmin-spring-boot-starter`.

[简体中文](../zh-CN/extension-guide.md) | English

`vadmin-spring-boot-starter` owns the default shell, theme, home page, and system
administration. Extend a normal consumer by contributing a business
`AdminModule`; do not copy or create a shell, layout, theme, or system module.

This guide uses a neutral `inventory` module. The module is assembled at
startup, not installed as a runtime plugin.

## Inventory Module

Declare a module ID, navigation group, page metadata, permission, and two
translation bundles. Page title and intent keys must start with the module ID.

```java
package com.example.inventory;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.github.youngledo.vadmin.contracts.auth.PermissionCode;
import io.github.youngledo.vadmin.flow.navigation.AdminMessageBundle;
import io.github.youngledo.vadmin.flow.navigation.AdminModule;
import io.github.youngledo.vadmin.flow.navigation.AdminNavigationGroup;
import io.github.youngledo.vadmin.flow.navigation.AdminPage;
import java.util.List;
import java.util.Set;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;

@Configuration(proxyBeanMethods = false)
public class InventoryModuleConfiguration {
  static final PermissionCode INVENTORY_READ =
      PermissionCode.of("inventory:item:read");

  @Bean
  AdminModule inventoryModule() {
    return AdminModule.of("inventory",
        List.of(new AdminNavigationGroup("inventory", "inventory.navigation", 200)),
        List.of(new AdminPage("inventory.items", "inventory", "inventory.items.title",
            "inventory.items.intent", "cube", 100, "inventory/items", INVENTORY_READ,
            InventoryView.class)),
        Set.of(INVENTORY_READ),
        List.of(new AdminMessageBundle("inventory", "i18n.inventory")));
  }

  @Bean
  @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  InventoryView inventoryView() {
    return new InventoryView();
  }
}

final class InventoryView extends VerticalLayout {
  InventoryView() {
    add("Inventory");
  }
}
```

The view intentionally has no `@Route`. `vadmin-spring-flow` registers the
route from `AdminPage` and applies its required permission before construction.
The permission must be declared in both the page and module. Duplicate module
IDs, page IDs, routes, navigation groups, permissions, or incompatible
translation resources fail application startup.

## Translation Resources

Place two bundles on the consumer classpath:

```text
src/main/resources/i18n/inventory_en_US.properties
src/main/resources/i18n/inventory_zh_CN.properties
```

For example:

```properties
# inventory_en_US.properties
inventory.navigation=Inventory
inventory.items.title=Items
inventory.items.intent=Browse stock and availability
```

```properties
# inventory_zh_CN.properties
inventory.navigation=库存
inventory.items.title=库存项目
inventory.items.intent=查看库存和可用性
```

The composite `I18NProvider` combines these resources with the starter and
other modules. Resolve module metadata with `getTranslation(page.titleKey())`
and `getTranslation(page.intentKey())`; do not render raw message keys.

## Production Frontend Anchor

Because the route is registered dynamically, the host must provide one static
Flow production anchor for every consumer dynamic view. Put the anchor on the
consumer's existing Spring Boot application class; a normal consumer does not
create a replacement layout:

```java
package com.example.inventory;

import com.vaadin.flow.component.dependency.Uses;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Uses(InventoryView.class)
@SpringBootApplication
public final class InventoryApplication {
}
```

Use `@Uses(InventoryView.class)` only for the consumer view. Do not put
`@Route` on `InventoryView`, and do not add `@Uses` for the starter's system
views: the starter owns those anchors.

## Page And Authorization Boundaries

Views use framework-neutral use cases and authorization contracts, never JPA
repositories. A page declaration controls navigation visibility and direct
route access; every mutation must still authorize in the application or
platform use case before changing state.

Use `AdminPageFrame`, `PageHeader`, `PageToolbar`, `DataWorkspace`, dialogs,
and feedback patterns from `admin-flow` for standard Flow administration
workflows. Keep business-specific CSS scoped to the module and use the
semantic `--admin-*` tokens. Modules must not register a global `@Theme`,
modify global Lumo variables, or target starter theme internals.

## Intentional Shell Replacement

A custom shell is an explicit complete replacement. Only choose it when the
starter shell and theme cannot meet the product boundary. Provide an
`AdminHostLayout`, an `AppShellConfigurator`, and `@Theme` configuration owned
by the consumer, then provide production anchors for every dynamic view it
composes.

Do not replace isolated system pages, selected shell components, or individual
theme internals. A replacement still uses the starter's module assembly,
`AdminModuleRegistry`, composite translations, permission catalog, and route
guards. Consumers that only add business pages should retain the default shell.

## Module Checklist

- Depend on `vadmin-spring-boot-starter` in the application.
- Contribute one `AdminModule` bean with module-owned IDs, routes, permissions,
  icon keys, and both `zh-CN` and `en-US` message resources.
- Make each dynamically registered View a Spring bean, normally with prototype
  scope, and omit `@Route`.
- Add one host `@Uses(ModuleView.class)` production anchor for each consumer
  dynamic View.
- Authorize mutations in use cases and use shared Flow patterns and semantic
  theme tokens.
- Leave the default shell, theme, system administration, and its production
  anchors to the starter unless intentionally replacing the complete shell.
