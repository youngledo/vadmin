# Vaadin Admin Starter

An open-source Vaadin Flow admin starter for Java teams building internal
business applications. It provides a runnable Spring Boot reference
application and a production baseline for authentication, RBAC, auditing,
CRUD, file storage, Flyway migrations, error handling, and containerization.
It is not a collection of showcase pages.

The first release uses Java 25, Maven 4.0.0-rc-6, Spring Boot 4.1.0, Vaadin
Flow 25.2.5, PostgreSQL 18, and Flyway 13.1.0. Vaadin Flow is the only UI
programming model in scope; Hilla, React, and TypeScript are not included.
Spring Boot is the current runtime, while `contracts` and `platform` remain
independent of Spring to preserve future runtime-adapter options.

Java, Maven, Spring Boot, and Vaadin upgrades require a separate pull request
and full CI verification. See [CHANGELOG.md](CHANGELOG.md) for release notes
and the compatibility policy.

## Quick Start

```bash
cp .env.example .env
docker compose --env-file .env up --build
```

Open `http://localhost:8080` and sign in with `admin` and the value of
`APP_BOOTSTRAP_PASSWORD` in `.env`.

## Documentation

| Guide | English | 简体中文 |
| --- | --- | --- |
| Quick Start | [Read](docs/quick-start.md) | [阅读](docs/zh-CN/quick-start.md) |
| Architecture | [Read](docs/architecture.md) | [阅读](docs/zh-CN/architecture.md) |
| Extension Guide | [Read](docs/extension-guide.md) | [阅读](docs/zh-CN/extension-guide.md) |
| Deployment | [Read](docs/deployment.md) | [阅读](docs/zh-CN/deployment.md) |
| Security | [Read](docs/security.md) | [阅读](docs/zh-CN/security.md) |
| Contributing | [Read](docs/contributing.md) | [阅读](docs/zh-CN/contributing.md) |

## Verification

```bash
./mvnw -B -ntp verify
./mvnw -B -ntp -Pproduction verify
docker compose --env-file .env.example config
docker build -t vaadin-admin-starter:local .
```

Vaadin is a trademark of Vaadin Ltd. This project is not affiliated with,
sponsored by, or endorsed by Vaadin Ltd.
