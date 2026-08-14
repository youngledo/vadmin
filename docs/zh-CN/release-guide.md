# 发布指南

VAdmin 的发布坐标为 `io.github.youngledo:vadmin-spring-boot-starter`。

[English](../en/release-guide.md) | 简体中文

## 范围

本指南定义首个公开 `0.1.0` 版本。该版本将 `vadmin-spring-boot-starter` 作为受支持的接入制品：
默认外壳和主题、系统管理、模块组装，以及使用方所需的 Spring 适配器。

## 已验证兼容性基线

| 领域 | `0.1.0` 已验证基线 | 发布边界 |
| --- | --- | --- |
| JDK | Java 25 | 构建和运行使用方均要求 Java 25。 |
| 构建工具 | Maven Wrapper 4.0.0-rc-6 | 升级到 Maven GA 属于独立工作。 |
| 运行时 | Spring Boot 4.1.0 | Spring Boot 是唯一受支持的运行时。 |
| UI | Vaadin Flow 25.2.5 | Flow 是唯一 UI 模型；不包含 Hilla、React、Vue 或 TypeScript。 |
| 数据库与迁移 | PostgreSQL 18 Compose 基线，Flyway 13.1.0 | Testcontainers 覆盖不构成额外部署承诺。 |
| 外观 | `vaadin`、`ant`；浅色/深色；舒适/紧凑 | 所有 profile 共享同一套 Flow、权限、模块和 i18n 契约。 |
| UI 语言 | `zh-CN`、`en-US` | starter 与使用方模块资源都必须支持这两种语言。 |
| 外部身份 | 可选的标准 OIDC 授权码登录 | 提供商生命周期、SCIM、SAML、LDAP、MFA、租户和数据范围策略由使用方负责。 |

升级 Java、Maven、Spring Boot 或 Vaadin 必须使用独立 PR，更新兼容性证据，并完成普通和生产
验证。

## 使用方升级规则

1. 将所有 `io.github.youngledo` 依赖升级到相同目标版本。不得混用已发布制品和
   snapshot 同级制品。
2. 使用 Java 25 运行使用方，并让 Spring Boot 与 Vaadin 与已验证版本表保持一致。
3. 在类生产数据库副本上审查 Flyway 迁移。每个迁移只执行一次，且不得改写已应用的迁移。
4. 保持文档化模块契约：`AdminModule` 元数据、已声明权限、路由、图标 key、`zh-CN` 与
   `en-US` 资源、没有 `@Route` 的 prototype View Bean，以及每个使用方动态 View 的宿主
   `@Uses` 锚点。
5. 保留 starter 默认外壳、主题和系统管理，除非使用方有意使用自己拥有的
   `AdminHostLayout`、`AppShellConfigurator` 和 `@Theme` 完全替换外壳。
6. 保留外部提供的密钥和部署配置。更改 `APP_BOOTSTRAP_PASSWORD` 不会重置已有 `admin` 账户。
7. 重新运行授权、语言、外观和代表性业务流测试。OIDC 使用方还必须验证已有本地账户映射。

## 发布检查清单

在准备发布提交的干净检出中运行。记录命令输出、工具版本、镜像摘要、发布提交和标签。

### 源码与文档

- [ ] 清理有意生成的制品后，`git status --short` 为空。
- [ ] `git diff --check` 没有输出。
- [ ] `CHANGELOG.md` 将发布内容移出 `Unreleased`，包含发布日期并标明不兼容变更。
- [ ] README、架构、快速开始、扩展和发布指南一致说明：`vadmin-spring-boot-starter` 提供默认
  外壳、主题和系统管理。
- [ ] 当前指南不包含已移除的示例领域引用，也不声称普通使用方必须提供外壳或默认
  `AdminHostLayout`。
- [ ] 根 `pom.xml` 使用发布版本；第一方 reactor 制品均解析到该精确版本。
- [ ] 许可证、第三方 notices 和内置图标归属信息仍包含在源码和发布归档中。

### 验证与打包

```bash
./mvnw -B -ntp verify
./mvnw -B -ntp -Pproduction verify
docker compose --env-file .env.example config
docker build -t vadmin:0.1.0-rc .
```

- [ ] 两条 Maven 命令均以 `BUILD SUCCESS` 结束。
- [ ] 单元、架构、Testcontainers、OIDC、基线浏览器和视觉 profile 浏览器套件均为零 failures、
  零 errors。
- [ ] 生产输出包含 starter 系统页面和使用方动态模块路由，且不依赖 Vaadin 开发服务器。
- [ ] Compose 配置渲染时没有未解析变量。
- [ ] 镜像以非 root 运行时启动，并使用外部凭据连接全新的 PostgreSQL 18 服务。
- [ ] 评审者登录后切换语言和浅色/深色，打开 Users、Roles、Permissions 和 Audit，再确认
  按权限过滤的使用方模块页面能在所选视觉语言和密度下正常显示。

## 发布

Maven 的 `central-release` profile 会附加源码和 Javadoc 归档、使用 GPG 签名全部发布文件，并
通过 Maven Central Publisher Portal 发布。推荐的发布路径是手动触发 `Publish Release` GitHub
Actions 工作流。其发布 job 绑定到 `vadmin` Environment，因此该环境中的 secrets 和仓库级
secrets 均可用。首次使用前，配置以下受保护的值，绝不可提交它们：

- `MAVEN_CENTRAL_USERNAME` 和 `MAVEN_CENTRAL_PASSWORD`：Maven Central Portal 发布凭据。
- `GPG_PRIVATE_KEY`：ASCII-armored 格式的私有签名密钥。
- `GPG_PASSPHRASE`：私钥口令。

从已验证的发布提交创建并推送 `vX.Y.Z` 标签，然后在 Actions 页面使用该精确标签运行
`Publish Release`。工作流会校验检出的标签与根 Maven 版本一致，使用 `central-release`
profile 发布并等待 Central Portal 完成；仅在成功后才创建 GitHub Release。`dry_run` 输入默认
为 `true`：先执行只构建和签名、不发布的生产预检；预检成功后，再使用同一标签并关闭
`dry_run` 执行正式发布。

如确有必要在本地发布，凭据应只存在于本机 Maven `settings.xml` 中名为 `central` 的 server
条目，绝不可提交：

```bash
./mvnw -B -ntp -Pproduction,central-release deploy
```

该 profile 会等待 Central Portal 发布完成。然后在干净的使用方项目中确认
`io.github.youngledo:vadmin-spring-boot-starter:0.1.0` 可以解析。

## 发布后记录

记录发布版本与标签、提交 SHA、验证日期、Java/Maven/Spring Boot/Vaadin 版本、命令结果、
镜像摘要、已知限制，以及发布说明和 staged 或已发布制品链接。将记录保存在发布系统中，
不要写入源码控制的密钥或本地 shell 历史。
