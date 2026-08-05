# 贡献指南

## 兼容性基线

提交必须在 Java 25 和项目 Maven Wrapper（Maven 4.0.0-rc-6）上构建。当前经过验证的
技术基线为 Spring Boot 4.1.0、Vaadin Flow 25.2.5、PostgreSQL 18 和 Flyway 13.1.0。
Maven 4 仍为 RC 版本；其 GA 升级同样需要独立变更。

Java、Maven、Spring Boot 或 Vaadin 的主版本升级必须通过独立 PR 完成，说明兼容性
影响，并通过全部 CI 验证。依赖的小版本或补丁升级也应先在完整测试矩阵中验证。

## 本地验证

```bash
./mvnw -B -ntp verify
./mvnw -B -ntp -Pproduction verify
docker compose --env-file .env.example config
docker build -t vaadin-admin-starter:local .
```

修改核心权限、数据库迁移、安全适配器或 Flow 交互时，应增加相应的单元、集成或
浏览器测试。提交前运行 `git diff --check`，并确保架构测试仍阻止核心模块依赖 Spring
或持久化实现。

## 代码和文档约定

使用 Java 25 的标准格式与 UTF-8 源码。保持现有 package 与模块边界，避免与当前任务
无关的重排或格式化。面向用户的 UI 文案和运维文档以中文为先；安全、版本和部署变更
必须同步更新相关文档。

新增运行时、持久化、身份、文件存储、消息或外部服务适配器前，必须先记录架构决策：
它实现哪个 contracts 端口、依赖方向是什么、失败语义如何处理、如何配置和验证。没有
该决策，不应直接把第三方 SDK 引入 core 或 Flow 模块。
