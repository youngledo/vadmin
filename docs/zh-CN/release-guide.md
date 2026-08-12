# 发布指南

[English](../en/release-guide.md) | 简体中文

## 范围

本指南用于准备首个公开 `0.1.0` 版本。当前源码版本仍为
`0.1.0-SNAPSHOT`；本文不会发布制品、创建标签、修改 Maven 版本，也不代表已经获得
Maven Central 发布授权。

发布目标是为构建内部管理应用的 Java 团队提供可复现的 Spring Boot 与 Vaadin Flow
基线。范围包括可复用模块、独立打包的订单示例模块和参考应用；不承诺支持所有
Spring Boot、Vaadin、JVM、数据库、身份提供商或视觉语言版本。

## 已验证兼容性基线

根 `pom.xml`、Maven Wrapper、生产 profile、Compose 栈和测试套件是下表的事实来源。
发布记录的是已验证的精确版本，而不是未经验证的兼容性范围。

| 领域 | `0.1.0` 已验证基线 | 发布边界 |
| --- | --- | --- |
| JDK | Java 25 | 构建和运行参考应用均要求 Java 25。 |
| 构建工具 | Maven Wrapper 4.0.0-rc-6 | Maven 4 仍是候选版本；升级到 Maven GA 属于独立工作。 |
| 运行时 | Spring Boot 4.1.0 | Spring Boot 是唯一受支持的运行时。 |
| UI | Vaadin Flow 25.2.5 | Flow 是唯一 UI 编程模型；不包含 Hilla、React、Vue 或 TypeScript。 |
| 数据库与迁移 | PostgreSQL 18 Compose 基线，Flyway 13.1.0 | 部分 Testcontainers 用例使用 PostgreSQL 17；这只是测试覆盖，不构成另一项部署承诺。 |
| 视觉语言 | `vaadin`、`ant` | 二者共享同一套 Flow/Java、路由、权限、模块与 i18n 契约。 |
| 颜色与密度 | 浅色/深色 session 模式；舒适/紧凑宿主密度 | 它们是相互独立的外观维度。 |
| UI 语言 | `zh-CN`、`en-US` | 模块必须提供两套声明的消息资源包。 |
| 外部身份 | 标准 OIDC 授权码登录，可选启用 | Keycloak 仅是测试夹具；提供商生命周期、SCIM、SAML、LDAP、MFA、租户和数据范围策略由使用方负责。 |

升级 Java、Maven、Spring Boot 或 Vaadin 必须使用独立 PR，更新兼容性证据，并通过两个
验证 profile。依赖的补丁或小版本升级也必须先测试，再记录为受支持版本。

## 版本策略

Reactor 版本由根 `pom.xml` 统一管理，所有第一方 Maven 模块继承该版本。公开发布在一个
独立的 release 变更中将 `0.1.0-SNAPSHOT` 改为 `0.1.0`；不得发布混合版本的第一方制品。

项目遵循语义化版本的原则和 Keep a Changelog：

| 变更 | `1.0` 前的版本处理 | 必须记录 |
| --- | --- | --- |
| 文档、测试、兼容性修复 | 可独立发布时使用 patch 版本 | Changelog 条目与发布验证。 |
| 新增已文档化 API、配置、模块元数据或行为 | minor 版本 | 兼容性说明、迁移说明与完整验证。 |
| 移除或改变已文档化的使用方行为 | `0.x` 期间的下一个 minor 版本 | 明确的不兼容变更章节、迁移指南与完整验证。 |
| 未发布的 snapshot 工作 | 不提供兼容性承诺 | 保持在 `Unreleased`，不得暗示其是可使用的正式版本。 |

首版使用方契约只包括已经文档化的 API 和配置。内部实现类、Vaadin shadow DOM、profile
私有 CSS 选择器、测试夹具和参考应用业务数据不是公开扩展 API。

## 使用方升级规则

升级使用方前，阅读目标版本的发布说明，并为数据库和文件存储制定回滚方案。按以下顺序
执行检查：

1. 将所有 `io.github.vaadinadminstarter` 依赖升级到同一目标版本。不得混用已发布模块和
   snapshot 同级模块。
2. 使用 Java 25 和项目 Maven Wrapper 基线运行使用方，并让 Spring Boot、Vaadin 版本与
   已验证版本表保持一致。
3. 审查所有 Flyway 迁移。在类生产数据库副本上只执行一次迁移，并在部署前验证对应的
   文件存储备份。不得改写已经进入任何环境的迁移文件。
4. 对独立 Flow 模块，保持文档化的 `AdminModule` 契约：模块拥有权限、路由、图标 key、
   `zh-CN` 与 `en-US` 资源包，以及没有 `@Route` 的路由视图。不要新增宿主
   `PermissionCatalog`，也不要选择宿主主题内部结构。
5. 保留外部提供的密钥与部署配置。特别是已有 `admin` 账户不会因改变
   `APP_BOOTSTRAP_PASSWORD` 而被重置。
6. 重新运行使用方的授权、语言、外观和代表性业务流测试。OIDC 使用方还必须验证已有
   本地账户映射；starter 不会基于声明创建账户或授予角色。

## 发布检查清单

在准备发布提交的干净检出中运行此清单。将命令输出、工具版本、镜像摘要、发布提交和
标签记录在发布记录中。

### 源码与文档

- [ ] 清理有意生成的制品后，`git status --short` 为空。
- [ ] `git diff --check` 没有输出。
- [ ] `CHANGELOG.md` 将发布内容移出 `Unreleased`，包含发布日期并标明不兼容变更。
- [ ] 兼容性表、快速开始、部署、安全、扩展和发布指南对精确版本及支持运行时边界没有
  矛盾。
- [ ] 根 `pom.xml` 从 `0.1.0-SNAPSHOT` 改为发布版本，所有第一方 reactor 制品解析到相同
  的精确版本。
- [ ] `LICENSE`、第三方 notices 和内置图标归属信息仍包含在源码与发布归档中。

### 验证与打包

```bash
./mvnw -B -ntp verify
./mvnw -B -ntp -Pproduction verify
./scripts/verify-standalone-consumer.sh
docker compose --env-file .env.example config
docker build -t vaadin-admin-starter:0.1.0-rc .
```

- [ ] 两条 Maven 命令均以 `BUILD SUCCESS` 结束。
- [ ] 单元、架构、Testcontainers、OIDC、基线浏览器、Ant 桌面和 Ant 紧凑浏览器套件均为
  零 failures、零 errors。
- [ ] 生产构建包含动态组合的 Flow 路由，且在没有 Vaadin 开发服务器时可运行。
- [ ] 独立使用方脚本完成本地制品安装、已认证的订单浏览器流程，以及使用方自身的生产前端打包。
- [ ] Compose 配置渲染时没有未解析变量。
- [ ] 镜像以非 root 运行时镜像构建，并能使用外部提供的凭据连接全新的 PostgreSQL 18
  服务启动。
- [ ] 评审者登录后切换语言和浅色/深色，打开 Users、Customers、Roles、Audit 和 Orders，
  验证所选宿主视觉语言（`vaadin` 或 `ant`）及舒适/紧凑密度。

### 发布前置条件

当前仓库尚未配置公开制品发布。上传公开 `0.1.0` 前，应在独立的发布变更中增加并验证目标
仓库的坐标、凭据、签名、POM 元数据（名称、描述、URL、SCM、开发者和许可证）、适用时的
源码与 Javadoc 制品以及制品来源证明要求。发布或提升前，应在干净的使用方项目中检查
staged 制品。

仓库为本地 Maven 仓库中的 snapshot 提供了等价的发布前接入验收路径：

```bash
./scripts/verify-standalone-consumer.sh
```

它会将第一方 reactor 制品安装到本地仓库，然后验证一个仅通过坐标消费 starter 与
`admin-example-orders` 的独立 Spring Boot 宿主。应在常规发布检查完成后运行它；该结果是
本地使用方接入的证据，不是制品发布步骤。

本地 reactor 构建通过并不代表可以上传制品、推送发布标签、创建 GitHub Release 或暴露
引导密码。

## 发布后记录

发布记录应包含发布版本与标签、提交 SHA、验证日期、Java/Maven/Spring Boot/Vaadin 版本、
Maven 命令结果、容器镜像摘要、已知限制以及发布说明和 staged 或已发布制品链接。将它放在
发布系统记录中，不要放在源码控制的密钥或本地 shell 历史中。
