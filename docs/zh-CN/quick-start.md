# 快速开始

[English](../quick-start.md) | 简体中文

Vaadin Admin Starter 是面向 Java 团队、用于构建内部业务应用的 Vaadin Flow
脚手架。首个版本使用 Spring Boot 作为运行时，并使用 PostgreSQL 作为数据库。

## 前置条件

- JDK 25。
- Docker Desktop，用于运行 PostgreSQL 或完整 Compose 栈。
- 可访问 Maven Central 的网络。项目自带 Maven 4 RC6 Wrapper。

## 从源码构建并运行

首先准备本地环境变量。示例文件只包含面向本地开发的公开值：

```bash
cp .env.example .env
docker compose --env-file .env up -d postgres
set -a
. ./.env
set +a
export DATABASE_URL="jdbc:postgresql://localhost:5432/${POSTGRES_DB}"
export DATABASE_USERNAME="${POSTGRES_USER}"
export DATABASE_PASSWORD="${POSTGRES_PASSWORD}"
SPRING_PROFILES_ACTIVE=development \
  ./mvnw -B -ntp -pl admin-reference-app -am spring-boot:run
```

在开发模式下，访问 `http://localhost:8080`，使用 `admin` / `change-me`
登录。该启动方式保留 Vaadin 开发服务器，以便在修改 Java Flow 视图、`admin-theme`
CSS 或主题配置后进行本地迭代。开发模式密码只适用于本机上的空数据库，不能用于共享
环境。

参考应用使用 `ApplicationShell` 注册名为 `admin-theme` 的 Flow 主题。登录后，可通过
当前用户菜单在浅色和深色模式之间切换。该选择只保留在当前 Vaadin session；新 session
会恢复为浅色模式。

如需使用外部化引导密码进行生产构建，请改用以下命令：

```bash
./mvnw -B -ntp -Pproduction -pl admin-reference-app -am package -DskipTests
SPRING_PROFILES_ACTIVE=prod java -jar admin-reference-app/target/admin-reference-app-0.1.0-SNAPSHOT.jar
```

访问 `http://localhost:8080`，使用用户名 `admin` 和
`APP_BOOTSTRAP_PASSWORD` 的值登录。`local-admin-change-me` 仅为本地示例；登录后请
立即替换密码。生产环境必须通过受保护的密钥管理机制注入该变量。

可执行 JAR 默认不包含 Vaadin 开发服务器，因此不要使用 `development` profile 启动它。
如需使用 Flow 开发模式，请通过 Maven 运行应用并确保 `vaadin-dev` 依赖可用；不要把
开发模式作为部署 JAR 的运行方式。

## 使用 Compose 启动完整栈

```bash
cp .env.example .env
docker compose --env-file .env up --build
```

Compose 会在启动应用前等待 PostgreSQL 健康检查通过。首次针对空数据库启动时，应用会
创建 `admin` 用户，并将 `APP_BOOTSTRAP_PASSWORD` 设为其密码。停止服务但保留命名卷
不会重新创建管理员或重置密码。

清除本地演示数据会删除数据库和附件。执行前请确认没有需要保留的数据：

```bash
docker compose down --volumes
```
