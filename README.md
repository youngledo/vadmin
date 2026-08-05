# Vaadin Admin Starter

面向 Java 团队的、基于 Vaadin Flow 的开源管理后台脚手架。它提供可运行的 Spring
Boot 参考应用，以及认证、RBAC、审计、CRUD、文件存储、Flyway 迁移、错误处理和
容器化的生产基线，而不是一组展示页。

首个版本使用 Java 25、Maven 4.0.0-rc-6、Spring Boot 4.1.0、Vaadin Flow 25.2.5、
PostgreSQL 18 和 Flyway 13.1.0。UI 编程模型仅支持 Vaadin Flow；Hilla、React 与
TypeScript 不在首个版本范围内。Spring Boot 是当前运行时，但 contracts 和 platform
不绑定 Spring，为后续适配其他运行框架保留边界。

Java、Maven、Spring Boot 或 Vaadin 的升级均通过独立 PR 完成，并需经过完整 CI
验证。发布记录与兼容性策略见 [CHANGELOG.md](CHANGELOG.md)。

## 快速启动

```bash
cp .env.example .env
docker compose --env-file .env up --build
```

打开 `http://localhost:8080`，用 `admin` 和 `.env` 中
`APP_BOOTSTRAP_PASSWORD` 的值登录。详细步骤见[快速开始](docs/quick-start.md)。

## 文档

- [需求](docs/requirements.md)与[架构](docs/architecture.md)
- [快速开始](docs/quick-start.md)
- [安全说明](docs/security.md)
- [扩展指南](docs/extension-guide.md)
- [部署与运维](docs/deployment.md)
- [贡献指南](docs/contributing.md)
- [变更记录与升级策略](CHANGELOG.md)

## 验证

```bash
./mvnw -B -ntp verify
./mvnw -B -ntp -Pproduction verify
docker compose --env-file .env.example config
docker build -t vaadin-admin-starter:local .
```

Vaadin 是 Vaadin Ltd. 的商标。本项目与 Vaadin Ltd. 没有隶属、赞助或认可关系。
