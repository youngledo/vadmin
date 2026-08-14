# Changelog

本项目遵循 Keep a Changelog 的结构，并使用语义化版本表达公开兼容性。

## [Unreleased]

## [0.1.0] - 2026-08-14

### Added

- Java 25、Maven 4.0.0-rc-6、Spring Boot 4.1.0、Vaadin Flow 25.2.5、
  PostgreSQL 18 和 Flyway 13.1.0 的验证兼容性基线。
- Maven、生产构建和 Testcontainers 验证的 GitHub Actions 工作流。
- 面向 Maven 与 GitHub Actions 的 Dependabot 周期性更新检查。
- Vaadin Flow 原生的 `ant` 外观配置：中性图标语言、应用壳、常用控件与覆盖层、
  数据工作区、服务端分页，以及桌面和窄屏浏览器验证；`vaadin` 外观仍保持为并行基线。
- 可审计的 `0.1.0` 发布准备指南，记录当前验证基线、版本策略、升级规则和发布门禁。
- `io.github.youngledo:vadmin-spring-boot-starter` 的首个公开 Maven Central 发布，包含源码、
  Javadoc 与 GPG 签名制品。

### Upgrade policy

- Java、Maven、Spring Boot 或 Vaadin 的版本升级必须使用独立 PR，说明兼容性影响，
  并完成全部 CI 验证。
- Maven 4 当前使用 RC6；升级到 Maven 4 GA 也按独立版本升级处理。
- 发布前需同时验证常规构建与 `production` profile。

### Breaking changes

- 首个公开版本，无此前公开 API 或坐标可兼容。
