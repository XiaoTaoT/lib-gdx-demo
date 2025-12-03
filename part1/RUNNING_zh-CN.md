## 项目运行与问题记录（Windows + 中国网络环境）

### 一、项目简介

这是一个使用 **LibGDX** 搭建的多平台 Demo 项目，包含：

- **`core`**：跨平台的游戏 / 应用核心逻辑（`Main`、`FirstScreen` 等）。
- **`lwjgl3`**：桌面端启动模块，通过 LWJGL3 启动 `core` 中的主程序。

本说明以 **Windows 10 + Gradle Wrapper + JDK 17** 为前提环境，记录了从零到成功运行过程中遇到的问题和解决办法。

---

### 二、环境准备

- **操作系统**：Windows 10
- **必需软件**：
  - JDK 17（任意发行版，例如 Adoptium、Oracle、Zulu 等）
  - PowerShell 或命令行终端

#### 1. 安装并验证 JDK 17

1. 安装 JDK 17 到本机，例如目录：`D:\software\soft_two\Java\jdk-17.0.12`。
2. 在 PowerShell 中执行：

```powershell
java -version
```

确认输出中的版本为 17。

> 如需更稳妥，可以在系统环境变量中设置：
>
> - `JAVA_HOME` 指向 JDK 17 安装目录
> - 将 `%JAVA_HOME%\bin` 加入 `Path`

---

### 三、首次运行项目步骤

在 PowerShell 中执行：

```powershell
cd D:\workspace\lib-gdx-demo\part1
.\gradlew.bat lwjgl3:run
```

- 首次运行会下载 Gradle 自身及依赖，时间可能较长。
- 成功后会弹出一个 LibGDX 窗口，说明项目已正常启动。

---

### 四、遇到的问题与解决方案

#### 问题 1：无法从 foojay 下载 JDK Toolchain

**报错示例：**

```text
Unable to download toolchain matching the requirements ({languageVersion=17, vendor=any vendor, implementation=vendor-specific, nativeImageCapable=false}) from 'https://api.foojay.io/disco/v3.0/ids/2d57bdd1e17a18f83ff073919daa35ba/redirect'
```

**原因说明：**

- Gradle 使用 **Java Toolchain** 功能，会尝试从 `foojay.io` 自动下载满足要求的 JDK 17。
- 在中国网络环境下访问该地址较慢或被阻断，导致下载失败，从而构建失败。

**解决思路：**

- 不再让 Gradle 自动下载 JDK，而是让它使用本机已经安装好的 **JDK 17**。

#### 解决方案 A：在 `gradle.properties` 中显式配置本地 JDK

1. 打开项目根目录下的 `gradle.properties` 文件。
2. 在文件末尾添加（或根据实际路径调整）：

```properties
org.gradle.java.home=D:/software/soft_two/Java/jdk-17.0.12
org.gradle.java.installations.paths=D:/software/soft_two/Java/jdk-17.0.12
org.gradle.java.installations.auto-detect=false
org.gradle.java.installations.auto-download=false
```

说明：

- `org.gradle.java.home`  
  指定 Gradle 本身运行时使用的 JDK 目录。
- `org.gradle.java.installations.paths`  
  告诉 Gradle Toolchain 去哪里扫描可用的 JDK 安装。
- `org.gradle.java.installations.auto-download=false`  
  禁用从远程（如 foojay）自动下载 JDK。

3. 关闭当前 PowerShell 窗口，重新打开一个新的 PowerShell。

4. 可选：检查 Gradle 是否识别到 JDK 17（需要 Gradle 支持该任务时）：

```powershell
.\gradlew.bat -q javaToolchains
```

5. 再次运行项目：

```powershell
.\gradlew.bat lwjgl3:run
```

如果本地 JDK 路径正确，项目应能正常启动。

#### 解决方案 B：使用 `JAVA_HOME` 并关闭自动下载（备选）

如果更习惯通过环境变量管理 JDK，也可以：

1. 系统环境变量中设置：

- `JAVA_HOME=D:\software\soft_two\Java\jdk-17.0.12`
- `Path` 中加入 `%JAVA_HOME%\bin`

2. 在 `gradle.properties` 中至少保留：

```properties
org.gradle.java.installations.auto-download=false
```

3. 确认命令行使用的是 JDK 17：

```powershell
java -version
```

4. 然后同样执行：

```powershell
cd D:\workspace\lib-gdx-demo\part1
.\gradlew.bat lwjgl3:run
```

---

#### 问题 2：中文文字显示为方块（乱码）

**现象：**

- 在 LibGDX 窗口中显示的中文文字变成了一个个小方块（□），看起来像乱码。

**原因说明：**

- LibGDX 默认的 `BitmapFont`（通过 `new BitmapFont()` 创建）只包含基本的拉丁字符（A-Z、数字、常见符号），**不包含中文字形**。
- 当 LibGDX 遇到字体中不存在的字符时，会用“缺失字形”的小方块来代替，所以中文显示为方块。

**解决思路：**

- 使用 **`gdx-freetype`** 扩展库，从 TTF/OTF 字体文件动态生成包含中文的 `BitmapFont`。
- 在 `Screen.show()` 中**只生成一次字体**，之后每帧复用，保证性能。

#### 解决方案：使用 gdx-freetype 生成中文字体

##### 步骤 1：添加 FreeType 依赖

1. **修改 `core/build.gradle`**，在 `dependencies` 中添加：

```groovy
dependencies {
  api "com.badlogicgames.gdx:gdx:$gdxVersion"
  api "org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion"

  // 支持使用 TTF/OTF 字体生成 BitmapFont（中文等）
  api "com.badlogicgames.gdx:gdx-freetype:$gdxVersion"
}
```

2. **修改 `lwjgl3/build.gradle`**，在 `dependencies` 中添加：

```groovy
dependencies {
  implementation "com.badlogicgames.gdx:gdx-backend-lwjgl3:$gdxVersion"
  implementation "com.badlogicgames.gdx:gdx-lwjgl3-angle:$gdxVersion"
  implementation "com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-desktop"
  // FreeType 本地实现，用于在桌面端支持 TTF/OTF 字体
  implementation "com.badlogicgames.gdx:gdx-freetype-platform:$gdxVersion:natives-desktop"
  implementation project(':core')
}
```

3. **在 IDE 中刷新 Gradle 项目**（Reload / Reimport Gradle Project），确保依赖下载完成。

##### 步骤 2：准备中文字体文件

1. 准备一个包含中文的字体文件，例如：
   - 微软雅黑：`msyh.ttf`（Windows 自带）
   - 宋体：`simsun.ttc`（Windows 自带，TTC 格式）
   - 或其他你喜欢的 TTF/OTF 字体

2. 将字体文件复制到项目的资源目录：**`assets/ui/`** 下，例如：
   - `assets/ui/simsun.ttc`
   - 或 `assets/ui/msyh.ttf`

> **注意：** 路径必须与代码中的 `Gdx.files.internal("ui/字体文件名")` 保持一致。

##### 步骤 3：修改代码生成中文字体

在 `FirstScreen.java` 的 `show()` 方法中，替换原来的字体创建代码：

```java
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

...

@Override
public void show() {
    // ... 其他初始化代码 ...
    
    batch = new SpriteBatch();

    // 使用 FreeType 从 TTF/TTC 生成支持中文的 BitmapFont（只生成一次，然后复用）
    FreeTypeFontGenerator generator =
            new FreeTypeFontGenerator(Gdx.files.internal("ui/simsun.ttc"));
    FreeTypeFontGenerator.FreeTypeFontParameter parameter =
            new FreeTypeFontGenerator.FreeTypeFontParameter();
    parameter.size = 28; // 字号

    // 只生成会用到的字符，避免一次生成整个中文字符集导致体积过大
    parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS
            + "背景颜色會隨时间變化Hello LibGDX!Time:. s";

    font = generator.generateFont(parameter);
    generator.dispose(); // 生成后就可以释放生成器

    time = 0f;
}
```

**关键点说明：**

- **生成时机**：在 `show()` 中生成一次，之后一直复用同一个 `font`，不在 `render()` 中重复生成，保证性能。
- **`parameter.characters`**：只包含实际会显示的字符，这样生成的字体贴图较小。如果以后需要显示新的中文，把新字符追加到这里。
- **`generator.dispose()`**：字体贴图生成完成后，释放生成器资源。

##### 步骤 4：运行验证

运行项目：

```powershell
cd D:\workspace\lib-gdx-demo\part1
.\gradlew.bat lwjgl3:run
```

如果一切正常，中文文字应该能正常显示，不再是小方块。

**关于 TTC 字体格式：**

- **TTC（TrueType Collection）** 是打包了多个字体子字库的格式（如 `simsun.ttc` 包含宋体、黑体等）。
- `gdx-freetype` 在桌面环境下**支持 TTC 格式**，可以直接使用。
- 如果将来在某些平台（如某些 Linux/Android 环境）遇到兼容性问题，可以考虑将 TTC 转换为单个 TTF 文件使用。

**性能说明：**

- **启动时生成一次**：字体生成只在 `show()` 中执行一次，启动时略微慢一点，但之后每帧渲染就是普通 `BitmapFont` 绘制，性能与默认字体几乎相同。
- **字符集可控**：只生成实际用到的字符，不会一次性生成数千个汉字贴图，显存/内存压力可控。

---

### 五、目前验证通过的运行方式

在完成上面的配置后：

1. **JDK 配置**（问题 1 的解决方案 A 或 B）
2. **中文字体支持**（问题 2 的解决方案）

运行项目：

```powershell
cd D:\workspace\lib-gdx-demo\part1
.\gradlew.bat lwjgl3:run
```

可以在 Windows 10 + 中国网络环境下成功启动 LibGDX 桌面应用，并正常显示中文文字。
