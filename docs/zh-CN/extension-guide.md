# 扩展指南

VAdmin 的发布坐标为 `io.github.youngledo:vadmin-spring-boot-starter`。

[English](../en/extension-guide.md) | 简体中文

`vadmin-spring-boot-starter` 拥有默认外壳、主题、首页和系统管理。普通使用方通过贡献业务
`AdminModule` 扩展应用；不要复制或创建外壳、布局、主题或系统模块。

## 使用宿主认证

默认情况下，starter 启用 VAdmin 本地 IAM：本地用户、角色、权限、审计和系统管理页面。
如果宿主已经拥有账号、会话和授权边界，可设置下列配置关闭这些本地能力：

```yaml
vadmin:
  local-iam:
    enabled: false
```

同时从 starter 排除本地 JPA 适配器，避免其 Spring Data JPA 运行时和迁移资源进入宿主：

```xml
<dependency>
  <groupId>io.github.youngledo</groupId>
  <artifactId>vadmin-spring-boot-starter</artifactId>
  <version>0.1.1-SNAPSHOT</version>
  <exclusions>
    <exclusion>
      <groupId>io.github.youngledo</groupId>
      <artifactId>vadmin-spring-jpa</artifactId>
    </exclusion>
  </exclusions>
</dependency>
```

宿主随后负责登录、退出、会话失效，并提供 `CurrentUserProvider` 与
`AuthorizationService` Bean。VAdmin 保持 `CurrentUser` 与 `PermissionCode` 契约，
不依赖宿主的 principal 类型、用户表或认证协议。该模式不会装配本地 IAM 服务和 Users、Roles、
Permissions、Audit 页面；宿主仍可贡献业务 `AdminModule` 与 Flow View。

本指南使用中性的 `inventory` 模块。模块在启动时组装，并非运行时插件。

## Inventory 模块

声明模块 ID、导航分组、页面元数据、权限和两套翻译资源。页面标题与意图 key 必须以模块 ID
开头：

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

该 View 有意不声明 `@Route`。`vadmin-spring-flow` 根据 `AdminPage` 注册路由，并在创建 View
前应用所需权限。权限必须同时在页面和模块中声明。重复的模块 ID、页面 ID、路由、导航分组、
权限或不兼容的翻译资源会导致应用启动失败。

## 翻译资源

将两套资源包放入使用方 classpath：

```text
src/main/resources/i18n/inventory_en_US.properties
src/main/resources/i18n/inventory_zh_CN.properties
```

例如：

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

组合 `I18NProvider` 会将这些资源与 starter 及其他模块资源合并。模块元数据使用
`getTranslation(page.titleKey())` 和 `getTranslation(page.intentKey())` 解析；不要显示原始
消息 key。

## 生产前端锚点

由于路由动态注册，宿主必须为每个使用方动态 View 提供一个静态 Flow 生产前端锚点。将锚点
放在使用方已有的 Spring Boot 应用类上；普通使用方不创建替换布局：

```java
package com.example.inventory;

import com.vaadin.flow.component.dependency.Uses;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Uses(InventoryView.class)
@SpringBootApplication
public final class InventoryApplication {
}
```

只为使用方 View 使用 `@Uses(InventoryView.class)`。不要给 `InventoryView` 添加 `@Route`，
也不要为 starter 系统 View 添加 `@Uses`：starter 自己拥有这些锚点。

## 页面与授权边界

View 使用与框架无关的用例和授权契约，绝不直接使用 JPA repository。页面声明控制导航可见性
和直接路由访问；每个变更操作仍必须在应用或平台用例中再次授权后才能改变状态。

标准 Flow 管理工作流应优先使用 `admin-flow` 提供的高层模式：`AdminPageFrame`、`PageHeader`、
`PageToolbar`、`DataWorkspace`、对话框和反馈模式。缺少的重复管理工作流应当扩展到 VAdmin，
而不是由每个使用方各自重建页面框架。真正领域专属的 CSS 必须限制在模块范围内，并基于
Vaadin 已公开的组件 API；模块不得注册全局 `@Theme`、修改全局主题属性、定位组件内部结构，
或依赖 VAdmin 的视觉语言 CSS。

## 有意替换外壳

自定义外壳是明确的完整替换。只有 starter 外壳和主题无法满足产品边界时才选择它。使用方需
提供自己拥有的 `AdminHostLayout`、`AppShellConfigurator` 和 `@Theme` 配置，并为所组合的每个
动态 View 提供生产锚点。

不要替换零散系统页面、部分外壳组件或单独主题内部结构。替换方案仍使用 starter 的模块组装、
`AdminModuleRegistry`、组合翻译、权限目录和路由守卫。仅添加业务页面的使用方应保留默认
外壳。

## 模块清单

- 在应用中依赖 `vadmin-spring-boot-starter`。
- 贡献一个 `AdminModule` Bean，使用模块拥有的 ID、路由、权限、图标 key，以及 `zh-CN` 和
  `en-US` 两套消息资源。
- 每个动态注册 View 都是 Spring Bean，通常使用 prototype 作用域，且不声明 `@Route`。
- 为每个使用方动态 View 增加一个宿主 `@Uses(ModuleView.class)` 生产锚点。
- 在用例中授权变更操作，并使用共享 Flow 模式和 Vaadin 组件 API。
- 除非有意完全替换外壳，否则将默认外壳、主题、系统管理及其生产锚点交由 starter 管理。
