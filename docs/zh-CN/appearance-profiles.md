# 外观配置档案

VAdmin 的发布坐标为 `io.github.youngledo:vadmin-spring-boot-starter`。

VAdmin 只有一个可选的视觉语言维度：

| 视觉语言 | 所有者 | 行为 |
| --- | --- | --- |
| `vaadin` | 宿主应用配置 | 默认值，直接使用 Vaadin Aura 与官方 API。 |
| `ant` | 宿主应用配置 | VAdmin 实现的、受 Ant Design 启发的显式替代方案。 |

两种语言都使用 Vaadin 的 `ColorScheme` API。每个 UI session 都可选择跟随系统、浅色或深色。
跟随系统由浏览器或操作系统决定；VAdmin 不维护重复的深色主题属性，也不维护独立的色彩 token
体系。

## 宿主配置

在宿主应用配置中选择视觉语言：

```yaml
app:
  appearance:
    visual-language: ant # vaadin | ant
```

也可通过 `APP_APPEARANCE_VISUAL_LANGUAGE` 提供同一配置。未知或空白值会安全回退为
`vaadin`。宿主会将解析后的值设置为 UI 根节点的
`data-vadmin-visual-language` 属性。

Aura 的官方 `--aura-base-size` 属性控制全局密度。若需要通过宿主配置设置它，请直接传入
12 到 24 的整数，不引入 VAdmin 密度别名：

```yaml
app:
  appearance:
    aura-base-size: 16
```

对应的环境变量为 `APP_APPEARANCE_AURA_BASE_SIZE`。未设置或超出支持范围时，VAdmin 保持
Aura 默认值。

## 模块边界

业务模块使用 Vaadin 组件 variant、Aura 和基础样式属性，以及共享 Flow 页面模式。模块不得导入
VAdmin 全局样式表、修改全局主题属性，或依赖 Ant 专用选择器及
`--vadmin-ant-*` token。

常见操作图标可使用 `admin-flow` 中的 `AdminIcon.of(AdminIconName)`。导航元数据仍必须使用
经过 `AdminIconCatalog` 校验的键。模块不得引用宿主 SVG 文件或 CSS mask 选择器；VAdmin 决定
语义图标在所选视觉语言中的实际渲染方式。

VAdmin 负责 Ant 的按钮、字段、菜单、对话框、通知和致密数据工作区呈现。业务模块继续使用
Vaadin Flow 原生语义，但不得针对覆盖层或 Grid 内部结构编写样式。
