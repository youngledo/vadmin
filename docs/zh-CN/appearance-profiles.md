# 外观配置档案

VAdmin 的发布坐标为 `io.github.youngledo:vadmin-spring-boot-starter`。

参考应用将外观划分为三个相互独立的维度：

| 维度 | 可选值 | 所有者 |
| --- | --- | --- |
| 视觉语言 | `vaadin`、`ant` | 宿主应用配置 |
| 色彩模式 | `light`、`dark` | 当前 UI session |
| 密度 | `comfortable`、`compact` | 宿主应用配置 |

`ant` 是受 Ant Design 的运营界面层级和信息密度启发的 Flow 原生视觉语言。它不引入
Ant Design、React、Vue 或其它前端运行时依赖；Vaadin Flow 始终是所有档案共用的组件和
Java 编程模型。

Ant 档案包含由宿主拥有的中性图标语言、紧凑应用壳，以及对高频控件和覆盖层的处理。它刻意保持“受 Ant 启发”而不是
像素级复刻；`vaadin` 仍是受支持的并行基线，并继续渲染 Vaadin 回退图标。

## 宿主配置

在宿主应用配置中选择视觉语言和密度：

```yaml
app:
  appearance:
    visual-language: ant # vaadin | ant
    density: compact # comfortable | compact
```

也可通过 `APP_APPEARANCE_VISUAL_LANGUAGE` 和
`APP_APPEARANCE_DENSITY` 提供相同配置。对应的 Spring 属性为
`app.appearance.visual-language` 与 `app.appearance.density`。未知或空白值会安全回退为
`vaadin` 和 `comfortable`。宿主会将解析后的值设置到 Flow UI 根节点的
`data-admin-visual-language` 与 `data-admin-density` 属性。

本版本中，视觉语言和密度是宿主选择，而不是普通终端用户偏好。应用壳中的外观菜单仍只
控制用户 session 的浅色/深色模式；切换色彩模式不会替换已选的视觉语言或密度。

## 模块边界

业务模块只能使用公开的 `--admin-*` 语义令牌和共享 Flow 页面模式。模块不得导入宿主的
`admin-theme`、注册全局 `@Theme`、选择视觉语言或密度、全局修改 Lumo 变量，或依赖仅由
Ant 档案提供的选择器。这样模块才能在每个宿主外观档案下保持可移植性。

常见操作图标可使用 `admin-flow` 中的 `AdminIcon.of(AdminIconName)`。导航元数据仍必须使用
经过 `AdminIconCatalog` 校验的键。模块不得引用宿主 SVG 文件或 CSS mask 选择器；由宿主决定
语义图标在当前档案中的实际渲染方式。

控件和覆盖层也由宿主主题负责。业务模块继续使用 Vaadin 原生的焦点、禁用、校验、对话框和通知语义，
但不得针对覆盖层内部结构或 Ant 专用选择器编写样式。

致密数据工作区同样由宿主负责呈现，包括 Grid 表头和行、选中态、状态反馈、分页页脚和危险操作后果说明。
模块只组合 `DataWorkspace` 与 `PagedGrid.getPaginationBar()`，不得选择档案或针对 Grid 内部结构编写样式。
