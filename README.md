# 轻记 NotePad

基于老师提供的 NotePad 源码结构完成的 Android 记事本项目。数据层沿用
ContentProvider + SQLite 的课程思路，界面改为适配当前 Android 工程的实现。

## 已完成要求

- 必做：笔记列表显示最后编辑时间，使用“今天 / 昨天 / 日期”友好格式。
- 必做：支持按标题或正文实时搜索。
- 扩展：笔记分类与按分类筛选。
- 扩展：便签式首页，分类以彩色便签展示，点击进入该分类的笔记。
- 扩展：用户可创建自己的分类，分类会同时出现在首页和笔记编辑器。
- 扩展：可编辑“计划目标”和按日期保存的“当天规划”便签。
- 扩展：带锁的私密笔记本，首次设置密码、后续解锁，并可在私密页面修改密码。
- 扩展：编辑器美化、柔和卡片界面、浅色/深色/跟随系统主题。
- 扩展：偏好设置，可选择主题和默认排序方式。
- 扩展：图片笔记，从系统图片选择器添加图片并保存在应用私有目录。
- 扩展：笔记置顶、长按操作和按编辑时间/创建时间/标题排序。

按照作业范围，不包含导出、云备份与恢复、语音搜索、OCR、PDF、视频等功能。

## 主要代码

- `HomeActivity`：便签首页、计划目标、当天规划和自定义分类入口。
- `MainActivity`：分类笔记列表、全文搜索、排序和置顶。
- `EditorActivity`：新建/编辑笔记、分类、图片附件和保存。
- `PlanningEditorActivity`：编辑计划目标和当天规划。
- `PreferencesActivity`：主题与排序偏好。
- `CategoryPreferences`：保存用户创建的分类和便签颜色。
- `NotePadProvider`：SQLite 数据访问与 ContentProvider 接口。
- `NoteImageStore`：应用私有目录中的图片复制、读取和清理。

## 构建

在 Windows PowerShell 中运行：

```powershell
.\gradlew.bat :app:assembleDebug
```

调试 APK 输出到：

```text
app\build\outputs\apk\debug\app-debug.apk
```
