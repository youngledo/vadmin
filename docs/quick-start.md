# 快速开始

Vaadin Admin Starter 是供 Java 团队构建内部业务系统的 Vaadin Flow
脚手架。首个版本的运行时为 Spring Boot，数据库为 PostgreSQL。

## 前置条件

- JDK 25。
- Docker Desktop（用于 PostgreSQL 或完整 Compose 栈）。
- 网络可访问 Maven Central；项目自带 Maven 4 RC6 Wrapper。

## 从源码构建并运行

先准备本地环境变量。示例文件仅包含可公开的本地开发值：

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

开发模式下访问 `http://localhost:8080`，使用 `admin` / `change-me` 登录。此启动路径
保留 Vaadin 开发服务器，用于修改 Java Flow 视图、`admin-theme` CSS 或主题配置后的
本地迭代。开发模式的密码只适用于本机空数据库，不能用于共享环境。

参考应用使用 `ApplicationShell` 注册名为 `admin-theme` 的 Flow 主题；登录后可从当前
用户菜单切换浅色和深色模式。该选择只保留在当前 Vaadin session，新的会话会恢复
浅色模式。

使用生产构建和外部化引导密码时，改用以下命令：

```bash
./mvnw -B -ntp -Pproduction -pl admin-reference-app -am package -DskipTests
SPRING_PROFILES_ACTIVE=prod java -jar admin-reference-app/target/admin-reference-app-0.1.0-SNAPSHOT.jar
```

访问 `http://localhost:8080`，使用用户名 `admin` 和
`APP_BOOTSTRAP_PASSWORD` 的值登录。这里的 `local-admin-change-me` 仅为本地示例；
登录后立刻替换密码。生产环境必须由受保护的密钥机制注入该变量。

可执行 JAR 默认不包含 Vaadin 开发服务器，因此不要使用 `development` profile 启动
它。需要使用 Flow 开发模式时，从 Maven 运行应用并保留 `vaadin-dev` 依赖，而不要
将开发模式作为部署 JAR 的运行方式。

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
