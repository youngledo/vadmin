# Vaadin Admin Starter

面向 Java 开发团队的、基于 Vaadin Flow 的开源管理后台脚手架。

第一版使用 Java 25、Spring Boot 4.1.0、Vaadin Flow 25.2.5 和 PostgreSQL。
UI 编程模型仅支持 Vaadin Flow；Hilla、React 与 TypeScript 不在第一版范围内。

## 构建要求

项目通过 Maven Wrapper 固定使用 Maven `4.0.0-rc-6`。这是 Maven 4 的预发布版本，
在 Maven 4 GA 发布后会通过独立升级变更评估和更新。

```bash
./mvnw -B -ntp verify
```

详细的需求、架构和实施计划位于 [docs](docs/) 目录。

Vaadin 是 Vaadin Ltd. 的商标。本项目与 Vaadin Ltd. 没有隶属、赞助或认可关系。
