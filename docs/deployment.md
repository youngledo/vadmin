# 部署与运维

VAdmin publication coordinate: `io.github.youngledo:vadmin-spring-boot-starter`.

## 容器镜像

`Dockerfile` 在 Java 25 JDK 阶段执行生产构建，最终阶段使用 Java 25 JRE，只复制
可执行 JAR，并以 UID `10001` 的非 root 用户运行。默认监听 8080，并设置
`SPRING_PROFILES_ACTIVE=prod`。

```bash
docker build -t vadmin:local .
docker run --rm -p 8080:8080 \
  -e DATABASE_URL=jdbc:postgresql://db.example:5432/vadmin \
  -e DATABASE_USERNAME=vadmin \
  -e DATABASE_PASSWORD='replace-me' \
  -e APP_BOOTSTRAP_PASSWORD='replace-on-first-start' \
  vadmin:local
```

生产部署应让数据库密码、引导密码和任何对象存储凭据由平台的密钥机制提供。不要把
这些值烘焙进镜像、Compose 文件或镜像标签。

## 数据库与迁移

Flyway 在 JPA 适配器可用前执行版本化 SQL 迁移，JPA 随后以 `ddl-auto: validate`
校验模式。迁移文件一旦进入已部署环境便不可改写；修复模式或数据变更时新增下一个
版本化迁移。应用升级前先在生产数据副本验证迁移和回滚方案。

默认 PostgreSQL 地址、用户名与密码分别来自 `DATABASE_URL`、
`DATABASE_USERNAME`、`DATABASE_PASSWORD`。将数据库连接限制在受信网络，并由运行平台执行
TLS、访问控制、日志收集与健康检查策略。

## 备份和恢复

Starter 基线只持久化访问控制和审计数据，因此恢复备份必须包含 PostgreSQL 数据库及其
Flyway schema history。业务模块如接入 `FileStorage` 或其它外部持久化能力，应由使用方
明确记录其一致性边界、备份范围和恢复顺序，并在隔离环境完成恢复演练后再进行生产切换。

## Compose 开发栈

`docker-compose.yml` 使用 PostgreSQL 18 和 `postgres-data` 命名卷。PostgreSQL 的
`pg_isready` 健康检查通过后，应用服务才会启动。检查渲染后的配置：

```bash
docker compose --env-file .env.example config
```

`.env.example` 是本地示例，不是生产密钥文件。部署中请创建受保护的 `.env` 或使用
平台密钥注入，并避免把它提交到 Git。
