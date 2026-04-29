# yunjing

一个基于 Android + Jetpack Compose 的多模块项目，当前工程集成了 Unity 模块，用于在 Android 应用中承载 Unity 相关能力。

## 项目简介

- 项目名称：`yunjing`
- 构建方式：Gradle Kotlin DSL
- 主应用模块：`app`
- Unity 模块：`unityLibrary`
- 最低 Android 版本：`minSdk 29`
- 目标 Android 版本：`targetSdk 36`
- Java 版本：`17`
- Kotlin 版本：`2.0.21`

## 项目结构

```text
yunjing/
├─ app/                         Android 主应用模块
├─ unityLibrary/                Unity 导出的 Android Library 模块
├─ gradle/                      Gradle 包装器与版本管理目录
├─ build.gradle.kts             根构建脚本
├─ settings.gradle.kts          模块注册与仓库配置
├─ gradle.properties            项目级 Gradle 配置
├─ SERVER_OPERATION.md          服务端操作说明
└─ yunjing_dump.sql             数据库导出文件
```

## 主要技术栈

- Jetpack Compose
- Kotlin
- AndroidX Navigation
- Retrofit + Gson
- OkHttp
- Coil
- SceneView
- ZXing / ML Kit
- Media3
- Unity Library 集成

## 环境要求

在本地构建前，请确认以下环境已准备完成：

- Android Studio
- Android SDK
- JDK 17
- Gradle Wrapper（项目已自带 `gradlew` / `gradlew.bat`）
- 如需构建 Unity 相关能力，需具备与项目配置匹配的 Android NDK

## 本地运行

### 1. 使用 Android Studio

1. 使用 Android Studio 打开项目根目录 `yunjing`
2. 等待 Gradle Sync 完成
3. 选择 `app` 模块
4. 连接设备或启动模拟器
5. 运行项目

### 2. 使用命令行构建

Windows：

```powershell
.\gradlew.bat assembleDebug
```

如需执行单元测试：

```powershell
.\gradlew.bat test
```

## 模块说明

### `app`

Android 主应用模块，当前使用 Jetpack Compose 构建界面，并依赖 `unityLibrary` 提供 Unity 集成能力。

### `unityLibrary`

Unity 导出的 Android Library 模块，用于将 Unity 内容集成到 Android 应用中。

## 开发说明

- 根工程使用 `settings.gradle.kts` 管理模块与仓库
- 仓库配置包含 `google()`、`mavenCentral()`，以及 `unityLibrary/libs` 本地依赖目录
- `gradle.properties` 已显式设置 `file.encoding=UTF-8`
- 若 Unity 模块重新导出，建议优先检查 `unityLibrary` 目录及其依赖是否同步更新

## 备注

- `shared/` 与 `database/` 目录当前存在于仓库根目录中，如需在 README 中补充其职责，建议结合实际业务再细化说明
- `local.properties` 通常包含本地环境路径配置，不建议提交敏感或机器绑定信息
