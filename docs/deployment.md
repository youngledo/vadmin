# 部署与运维

## 容器镜像

`Dockerfile` 在 Java 25 JDK 阶段执行生产构建，最终阶段使用 Java 25 JRE，只复制
可执行 JAR，并以 UID `10001` 的非 root 用户运行。默认监听 8080，并设置
`SPRING_PROFILES_ACTIVE=prod`。

```bash
docker build -t vaadin-admin-starter:local .
docker run --rm -p 8080:8080 \
  -e DATABASE_URL=jdbc:postgresql://db.example:5432/vaadin_admin \
  -e DATABASE_USERNAME=vaadin_admin \
  -e DATABASE_PASSWORD='replace-me' \
  -e APP_BOOTSTRAP_PASSWORD='replace-on-first-start' \
  -e APP_FILE_STORAGE_DIRECTORY=/var/lib/vaadin-admin-starter/files \
  -v vaadin-admin-files:/var/lib/vaadin-admin-starter/files \
  vaadin-admin-starter:local
```

生产部署应让数据库密码、引导密码和任何对象存储凭据由平台的密钥机制提供。不要把
这些值烘焙进镜像、Compose 文件或镜像标签。

## 数据库与迁移

Flyway 在 JPA 适配器可用前执行版本化 SQL 迁移，JPA 随后以 `ddl-auto: validate`
校验模式。迁移文件一旦进入已部署环境便不可改写；修复模式或数据变更时新增下一个
版本化迁移。应用升级前先在生产数据副本验证迁移和回滚方案。

默认 PostgreSQL 地址、用户名与密码分别来自 `DATABASE_URL`、
`DATABASE_USERNAME`、`DATABASE_PASSWORD`。文件存储目录来自
`APP_FILE_STORAGE_DIRECTORY`。将数据库连接限制在受信网络，并由运行平台执行
TLS、访问控制、日志收集与健康检查策略。

## 备份和恢复

客户附件在数据库中保存元数据与不透明文件 ID，实际内容由 `FileStorage` 保存。
因此可恢复备份必须同时包含：

1. PostgreSQL 数据库（包括 Flyway schema history）。
2. 与该数据库同一恢复点的文件存储目录或对象存储前缀。

只恢复其中之一会造成无法下载的附件或孤立文件。恢复时先在隔离环境验证数据库与
文件副本的时间点匹配，再进行生产切换。

## Compose 开发栈

`docker-compose.yml` 使用 PostgreSQL 18、`postgres-data` 命名卷和
`application-files` 命名卷。PostgreSQL 的 `pg_isready` 健康检查通过后，应用
服务才会启动。检查渲染后的配置：

```bash
docker compose --env-file .env.example config
```

`.env.example` 是本地示例，不是生产密钥文件。部署中请创建受保护的 `.env` 或使用
平台密钥注入，并避免把它提交到 Git。
