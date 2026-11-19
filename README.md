# 轻记 NotePad

轻记 NotePad 是一个使用 Java 开发的原生 Android 记事本应用，基于 ContentProvider 和 SQLite 实现本地数据存储。应用采用便签式首页设计，支持笔记分类、全文搜索、计划目标、当天规划、图片笔记、主题设置和密码保护的私密笔记本。

## 主要功能

### 便签式首页

- 首页使用彩色便签方块展示不同笔记分类。
- 点击分类便签可以查看该分类下的所有笔记。
- 首页提供“所有笔记”入口。
- 首页包含“计划目标”和“当天规划”两个可编辑便签。
- 计划目标适合记录长期方向和目标。
- 当天规划根据日期保存，每天显示当天的规划内容。

### 笔记管理

- 支持新建、查看、编辑和删除笔记。
- 支持按最近编辑、最近创建和标题名称排序。
- 支持置顶重要笔记。
- 笔记列表显示最后保存时间。
- 只有实际保存笔记时才更新最后保存时间，单纯查看不会改变时间。
- 时间格式为：`HH:mm MM-dd-yyyy`，例如：`23:13 11-18-2026`。
- 支持按笔记标题或正文内容实时搜索。
- 支持浅色模式、深色模式和跟随系统主题。

### 自定义分类

- 内置未分类、学习、工作、生活和灵感等基础分类。
- 用户可以创建自己的分类。
- 每个分类会自动分配一种便签颜色。
- 分类会保存到本地，后续启动时继续显示。
- 进入某个分类后新建笔记，默认使用当前分类。
- 编辑笔记时可以切换或新增分类。
- 分类选项会自动换行显示，方便添加更多自定义分类。

### 私密笔记本

- 首页提供带锁图标的“私密笔记本”入口。
- 第一次进入时要求设置并确认密码。
- 后续每次进入私密笔记本都需要输入密码。
- 私密笔记本支持普通笔记的完整功能，包括新建、编辑、删除、搜索、排序、置顶和图片附件。
- 私密笔记本页面菜单中提供“修改私密密码”功能。
- 修改密码时需要验证当前密码。
- 修改成功后，下次进入需要使用最新密码。
- 密码使用随机盐和 PBKDF2 进行哈希处理后保存在本机，不保存密码明文。
- “所有笔记”、普通分类和普通搜索结果不会显示私密笔记本中的内容。

### 图片笔记

- 支持从系统图片选择器添加图片。
- 图片会复制到应用私有目录，避免依赖外部临时路径。
- 编辑器支持完整比例的图片预览，避免图片被裁切。
- 点击图片可以进入全屏查看。
- 全屏模式支持双指缩放、拖动查看和双击放大。
- 双击已放大的图片可以恢复到完整显示状态。

### 偏好设置

- 支持跟随系统、浅色模式和深色模式。
- 支持设置笔记默认排序方式。
- 主题和排序设置会保存在本地。

## 技术实现

- 开发语言：Java
- 平台：Android
- 最低支持版本：Android 7.0（API 24）
- 数据库：SQLite
- 数据访问：ContentProvider
- 界面组件：AndroidX、Material Components
- 数据存储：SQLite、SharedPreferences、应用私有文件目录
- 图片显示：自定义 Matrix 缩放和拖动
- 密码存储：随机盐 + PBKDF2 哈希

## 项目结构

```text
app/
├── src/main/java/com/example/notepad/
│   ├── HomeActivity.java
│   ├── MainActivity.java
│   ├── EditorActivity.java
│   ├── PlanningEditorActivity.java
│   ├── PrivateNotebookActivity.java
│   ├── PrivatePasswordManager.java
│   ├── ImagePreviewActivity.java
│   ├── ZoomableImageView.java
│   ├── NotePadProvider.java
│   ├── CategoryPreferences.java
│   └── PlanningStore.java
├── src/main/res/
│   ├── layout/
│   ├── drawable/
│   ├── menu/
│   └── values/
└── build.gradle.kts
```

## 构建与运行

1. 使用 Android Studio 打开项目根目录。
2. 等待 Gradle 同步完成。
3. 选择 Android 7.0 或更高版本的模拟器或真机。
4. 点击 Run 运行应用。

也可以在 Windows PowerShell 中构建调试版 APK：

```powershell
.\gradlew.bat :app:assembleDebug
```

构建完成后，APK 输出位置：

```text
app/build/outputs/apk/debug/app-debug.apk
```

## 权限与隐私

- 应用不依赖网络服务。
- 笔记、分类、计划和设置均保存在本机。
- 私密笔记本通过本地密码进行访问控制，密码以加盐哈希形式保存。
- 私密内容不会出现在“所有笔记”和普通分类中。
