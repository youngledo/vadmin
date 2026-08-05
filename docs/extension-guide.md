# 扩展指南

本项目是模块化单体。`admin-contracts` 定义 Java 级端口和通用模型，
`admin-platform` 定义授权后的业务用例；Flow、Spring Security、JPA、Spring Boot
位于适配器层；`admin-reference-app` 是组合根和示例业务模块。新增业务功能应沿着
这个依赖方向组织，不要让 Flow 视图直接调用 JPA repository。

## 新增权限和页面

先在 `ApplicationConfiguration.permissionCatalog()` 中新增稳定的权限代码，例如
`orders:order:read`。再在相同配置类中加入 `PageDefinition`，提供稳定的 `pageId`、
中文标题键、图标、顺序、路由和所需权限。启动时目录会同步到数据库，现有角色仍需
由管理员显式授予新权限。

Flow 页面使用 `@Route` 注册，并在页面进入前检查与页面定义相同的权限。参考应用的
受保护页面通过 `SecuredView` 完成此检查；新页面应遵循同样的重定向策略：未登录进入
登录页，无权限进入 `access-denied`。页面上的按钮只是体验层控制，创建、修改、删除
等平台用例必须再次调用 `AuthorizationService`。

## 新增业务模块

业务模块应定义自己的实体、查询模型、端口和用例。建议的调用方向是：

```text
Flow view -> business use case -> repository / audit / file-storage ports
                                     ^
                           Spring JPA or other adapter
```

将业务规则和授权放入用例，而非页面事件监听器；将 JPA 映射、SQL 和事务放入 Spring
适配器或参考应用的持久化实现。为每个变更用例增加权限检查和审计结果；为每个新增
表提供不可变的 Flyway 版本化迁移。核心模块不得导入 Spring、JPA、Flyway 或具体
适配器类型。

## 替换文件存储

`FileStorage` 是保存、读取和删除二进制内容的端口。默认的 `LocalFileStorage` 适合
本地开发和演示。生产对象存储适配器应以不透明 UUID 作为键，保留流式读取语义，
处理失败时提供可诊断日志，并在业务删除操作中调用同一删除语义。

新增适配器后，在组合根提供唯一的 `FileStorage` bean，并配置其凭据和存储位置。若
适配器带来新的事务、一致性、重试、删除延迟或安全语义，先记录架构决策并为失败与
恢复路径补充集成测试。

## 中文和品牌

当前参考 UI 以中文为先。新增页面、验证信息和导航标题应先提供中文；需要其他语言
时使用消息键和 Spring `MessageSource`，不要把业务规则绑定到展示文本。品牌名称、
主布局标题、导航文案以及主题变量应由应用层维护。保留 Vaadin 的可访问性和深浅主题
约定，不要修改可复用模块来硬编码某个组织的名称或颜色。

## HTTP 错误与可观测性

对自定义 HTTP API，复用 `ProblemDetailMapper` 将 `BusinessFailure` 映射为 RFC 9457
Problem Details；不要把 Spring 的 `ProblemDetail` 放进 contracts 或 platform。对 Flow
交互，复用 `FlowErrorMapper` 的字段校验、访问拒绝和安全失败呈现。

所有新的 HTTP 入口都应保留 `X-Correlation-Id` 的请求传播，并把关联 ID 写入日志与
相关审计事件。错误响应与日志只能包含安全的诊断信息。
