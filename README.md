# Vaadin Admin Starter

Vaadin Admin Starter is a Spring Boot and Vaadin Flow baseline for internal
business applications. Add `admin-spring-starter` to receive local login,
permission-filtered navigation, the default shell and theme, Users, Roles,
Permissions, Audit, locale selection, and appearance controls. It is not a
collection of showcase pages or a runtime plugin platform.

The first release uses Java 25, Maven 4.0.0-rc-6, Spring Boot 4.1.0, Vaadin
Flow 25.2.5, PostgreSQL, and Flyway. Flow is the only UI model in scope; Hilla,
React, and TypeScript are not included.

The starter owns the baseline experience. Consumers add business capabilities
as `AdminModule` beans and Flow view beans; they do not create a shell, theme,
or system-administration module for normal adoption. See the English
[Extension Guide](docs/en/extension-guide.md) for the module, translation, and
production-anchor contract.

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
| Quick Start | [Read](docs/en/quick-start.md) | [阅读](docs/zh-CN/quick-start.md) |
| Architecture | [Read](docs/en/architecture.md) | [阅读](docs/zh-CN/architecture.md) |
| Extension Guide | [Read](docs/en/extension-guide.md) | [阅读](docs/zh-CN/extension-guide.md) |
| Theme Tokens | [Read](docs/en/theme-tokens.md) | -- |
| Release Guide | [Read](docs/en/release-guide.md) | [阅读](docs/zh-CN/release-guide.md) |
| Security | -- | [阅读](docs/security.md) |
| Contributing | -- | [阅读](docs/contributing.md) |

## Verification

```bash
./mvnw -B -ntp verify
./mvnw -B -ntp -Pproduction verify
docker compose --env-file .env.example config
docker build -t vaadin-admin-starter:local .
```

Vaadin is a trademark of Vaadin Ltd. This project is not affiliated with,
sponsored by, or endorsed by Vaadin Ltd.
