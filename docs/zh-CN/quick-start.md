# 快速开始

VAdmin 的发布坐标为 `io.github.youngledo:vadmin-spring-boot-starter`。

[English](../en/quick-start.md) | 简体中文

`vadmin-spring-boot-starter` 是正常接入时使用的依赖。它提供默认 Flow 外壳和主题、本地登录、
系统管理、模块组装和 Spring 适配器。应用只需提供数据源、Flyway 迁移和业务模块。

## 前置条件

- JDK 25。
- Docker Desktop，用于 PostgreSQL 或完整 Compose 栈。
- 可访问 Maven Central 的网络。仓库自带 Maven 4 RC6 Wrapper。

## 添加 Starter

向 Spring Boot 应用添加唯一的第一方依赖：

```xml
<dependency>
  <groupId>io.github.youngledo</groupId>
  <artifactId>vadmin-spring-boot-starter</artifactId>
  <version>${vadmin.version}</version>
</dependency>
```

确保应用已有的 `@EnableVaadin` 扫描包含 Starter 的 UI 包。这是 Vaadin 的路由发现要求，
不是自行组装外壳：

```java
@EnableVaadin({"com.example.inventory", "io.github.youngledo.vadmin.starter",
        "io.github.youngledo.vadmin.springsecurity.ui"})
@SpringBootApplication
public class InventoryApplication {
}
```

通过 `spring-boot:run` 使用开发模式时，使用方还需直接声明 Vaadin 的可选开发服务器。它有意
不从 starter 传递，并且不会进入生产制品：

```xml
<dependency>
  <groupId>com.vaadin</groupId>
  <artifactId>vaadin-dev</artifactId>
  <version>${vaadin.version}</version>
  <optional>true</optional>
</dependency>
```

在使用方应用中配置 PostgreSQL 和 Flyway。使用方的迁移位置应与 starter 迁移分开，且不得
改写已经进入任何环境的迁移：

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/inventory
    username: inventory
    password: change-me
  flyway:
    locations: classpath:db/migration
```

启动应用。空数据库首次启动时，提供高强度 `APP_BOOTSTRAP_PASSWORD`；初始 `admin` 账户会
使用该密码。之后更改此变量不会重置已有账户。

```bash
APP_BOOTSTRAP_PASSWORD='replace-this-secret' ./mvnw -B -ntp spring-boot:run
```

访问 `http://localhost:8080`。starter 提供首页、导航、Users、Roles、Permissions、Audit、
`zh-CN` 与 `en-US`、浅色/深色模式，以及所选视觉语言和密度。普通使用方不定义 Flow 外壳、
`AdminHostLayout`、`AppShellConfigurator` 或 `@Theme`。

## 运行参考应用

仓库中的参考应用是精简的 starter 使用方和验收夹具。使用 PostgreSQL 在本地运行：

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
  ./mvnw -B -ntp -pl :vadmin-reference-app -am spring-boot:run
```

启动完整容器栈：

```bash
cp .env.example .env
docker compose --env-file .env up --build
```

开发 profile 只使用本地空数据库的示例密码。生产类数据库必须通过受保护渠道设置
`APP_BOOTSTRAP_PASSWORD`。生产制品不包含 Vaadin 开发服务器：

```bash
./mvnw -B -ntp -Pproduction -pl :vadmin-reference-app -am package -DskipTests
SPRING_PROFILES_ACTIVE=prod \
  java -jar vadmin-reference-app/target/vadmin-reference-app-0.1.0.jar
```

清除本地演示数据会删除 Compose 卷。执行前确认没有需要保留的数据：

```bash
docker compose down --volumes
```

添加业务页面请阅读[扩展指南](extension-guide.md)。只有有意完全替换外壳时，使用方才需要
提供 `AdminHostLayout` 和 `AppShellConfigurator`。
