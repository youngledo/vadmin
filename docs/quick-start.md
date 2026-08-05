# 快速开始

Vaadin Admin Starter 是供 Java 团队构建内部业务系统的 Vaadin Flow
脚手架。首个版本的运行时为 Spring Boot，数据库为 PostgreSQL。

## 前置条件

- JDK 25。
- Docker Desktop（用于 PostgreSQL 或完整 Compose 栈）。
- 网络可访问 Maven Central；项目自带 Maven 4 RC6 Wrapper。

## 从源码启动

先准备本地环境变量。示例文件仅包含可公开的本地开发值：

```bash
cp .env.example .env
docker compose --env-file .env up -d postgres
./mvnw -B -ntp -pl admin-reference-app -am package -DskipTests
SPRING_PROFILES_ACTIVE=development java -jar admin-reference-app/target/admin-reference-app-0.1.0-SNAPSHOT.jar
```

访问 `http://localhost:8080`，使用用户名 `admin` 和密码 `change-me`
登录。开发配置只应在本机使用；登录后立刻替换该密码。

如果希望使用 `.env` 中的密码而不是开发回退值，请不要使用
`development` profile，并导出其中的数据库和 `APP_BOOTSTRAP_PASSWORD`
变量后再启动应用。

## 使用 Compose 启动完整栈

```bash
cp .env.example .env
docker compose --env-file .env up --build
```

Compose 会等待 PostgreSQL 的健康检查通过后再启动应用。首次对空数据库启动时，
应用创建 `admin` 用户，并将 `APP_BOOTSTRAP_PASSWORD` 设为其密码。停止服务但保留
命名卷不会重新创建管理员或重置密码。

清除本地演示数据会删除数据库与附件，执行前请确认没有需要保留的数据：

```bash
docker compose down --volumes
```
