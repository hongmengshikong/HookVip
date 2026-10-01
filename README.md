# XPosed Hook 模块

本项目是一个基于 **现代 libxposed API（LSPosed API 102）** 的 Android 应用 Hook 工具，
用于修改指定 App 内部方法返回值，实现特定功能。

---

## 迁移到 LSPosed API 102

本项目已从旧版 Xposed API（de.robv.android.xposed，本地 XposedBridgeAPI-89.jar）
迁移到 **libxposed API 102**，对应 LSPosed 2.2.0+。

| 项 | 迁移前 | 迁移后 |
|----|--------|--------|
| 模块入口 | assets/xposed_init + IXposedHookLoadPackage | META-INF/xposed/java_init.list + extends XposedModule |
| 模块声明 | AndroidManifest.xml 里 4 个 xposed* meta-data | META-INF/xposed/module.prop（minApiVersion / targetApiVersion / staticScope） |
| 作用域 | res/values/array.xml + xposedscope meta-data | META-INF/xposed/scope.list（staticScope=false，由用户/前端勾选） |
| Hook 写法 | XposedHelpers.findAndHookMethod(...) + XC_MethodHook | xposed.hook(method).intercept(chain -> ...) |
| 加载回调 | handleLoadPackage(XC_LoadPackage.LoadPackageParam) | onPackageReady(PackageReadyParam)（拿到最终 ClassLoader） |
| 编译依赖 | compileOnly(files("libs/XposedBridgeAPI-89.jar")) | compileOnly(io.github.libxposed:api:102.0.0) |
| 激活状态 | 无 | implementation(io.github.libxposed:service:102.0.0) + XposedServiceHelper |
| 构建 | AGP 8.9.2 / Gradle 8.11.1 / compileSdk 35 | AGP 9.4.1 / Gradle 9.8 / compileSdk 37 |

### 关键改动

1. **入口重写**（HookEntry）：继承 io.github.libxposed.api.XposedModule，
   在 onModuleLoaded() 里把 XposedInterface 交给 XposedUtil，
   在 onPackageReady() 里按包名分发到各个 *AppHooker。
2. **新增两个工具类**（API 102 起模块不能再调用 legacy Xposed API，这些能力必须自带）：
   - util/XposedUtil：持有 XposedInterface，提供 hook(...) / log(...)；
     默认使用 ExceptionMode.PROTECTIVE，并给每个 hook 设置稳定 id（API 102 的原子替换特性）。
   - util/ReflectUtil：替代 XposedHelpers 的 findClass / findMethod / callMethod / callStaticMethod。
3. **10 个 AppHooker 全部改写**：beforeHookedMethod/setResult → chain.proceed() / 直接返回常量；
   afterHookedMethod → 先 chain.proceed() 再返回替换值；XC_MethodReplacement → chain -> null。
4. **加固应用的 ClassLoader**：保留 UniversalRealClassLoaderUtil（Hook Application.attach），
   并加了 3 秒兜底 —— 如果 Application.attach 没给出真实 ClassLoader，就退回包自身的 ClassLoader，
   避免加固壳差异导致 hook 完全不生效。
5. **模块激活状态**：service/ModuleStatus 通过 libxposed service 直接问框架要
   getFrameworkName/Version、getScope，以及 API 102 的 getRunningTargets()，主界面直接展示。
6. **删除**：app/src/main/assets/xposed_init、res/values/array.xml、app/libs/XposedBridgeAPI-89.jar，
   以及从未被注册过的死代码 Hook.java。

### 构建环境

| 项 | 版本 |
|----|------|
| Gradle | 9.8.0（wrapper 自带） |
| AGP | 9.4.1 |
| compileSdk / minSdk / targetSdk | 37 / 28 / 35 |
| libxposed | api:102.0.0（compileOnly）+ service:102.0.0（implementation） |

> **为什么必须用 AGP 9.x**：libxposed:service:102.0.0 要求 compileSdk >= 37，
> 而 AGP 8.x 读不了 android-37.0（平台用的是 SDK XML v4）。

## 前端（模块 App）

底部三个 Tab，界面参考 Rain_1.1.4 的信息架构，用 Material 3 组件实现（日夜跟随 + Material You 动态取色）。

| Tab | 内容 |
|:-|:-|
| **应用** | 搜索框、勾选统计、全部勾选 / 全部取消、10 个目标应用的开关列表（真实图标与名称、包名、适配版本、安装状态） |
| **模块** | 激活状态卡、已勾选 / 已安装统计、框架与 libxposed API 信息、使用步骤 |
| **设置** | 深色模式、动态取色、关于 |

![应用页](docs/device/apps.png)
![模块页](docs/device/module.png)
![设置页](docs/device/settings.png)

### 「勾选应用」是怎么生效的

**开关就是 LSPosed 的作用域本身**，只有一个真源，不存在两份状态：

1. 界面上的勾选状态直接来自框架：service API 的 getScope()。
2. 默认（刚装完、还没勾过任何东西）**全部不勾选**；在 LSPosed 管理页面里勾过的应用，
   打开本 App 时也会自动是勾选状态。
3. 在前端点开关会直接改作用域：勾上 → requestScope()，取消 → removeScope()。
   requestScope 会拉起 LSPosed 的授权确认（见下图），批准后回到列表自动刷新。
4. 模块只会被加载进作用域内的进程，所以「没勾选 = 模块根本不会进那个应用」，
   不需要再加一层运行时开关。

![作用域请求](docs/device/scope-request.png)

> 没有引入额外的导出组件、也没有单独的配置文件 —— 勾选状态就是 LSPosed 自己的作用域。

### 前端代码位置

    HookVIP/app/src/main/java/com/hook/vip/
    ├─ App.java                       # Application：连框架 + 动态取色
    ├─ MainActivity.java              # 底部导航外壳
    ├─ ui/AppsFragment.java           # 应用页（搜索 / 全选 / 开关 / 作用域引导）
    ├─ ui/AppAdapter.java             # 列表项：图标 + 名称 + 徽标 + 开关
    ├─ ui/ModuleFragment.java         # 模块页：激活状态与环境信息
    ├─ ui/SettingsFragment.java       # 设置页
    ├─ data/TargetCatalog.java        # 10 个目标应用目录（与 scope.list 对应）
    ├─ data/AppPrefs.java             # 深色模式 / 动态取色
    └─ service/ModuleStatus.java      # 问框架要激活状态 / 作用域 / 运行中的目标，并改作用域

---

## 支持的应用

| 应用名称 | 包名 | 支持版本 |
|----------|------|----------|
| 体重日记 | com.kproduce.weight | 理论全版本通杀（测试到 3.5.3） |
| 番茄轻断食 | com.swhh.fasting.tomato | 理论全版本通杀（测试到 3.4.7） |
| 番茄闪轻 | com.qyxy.tomato.android | 理论全版本通杀（测试到 2.1.7） |
| 一念 | com.mt.copyidea | 理论全版本通杀（测试到 2.4.0） |
| 背书匠 | com.zzdbwku.zizbnea | 理论全版本通杀（测试到 2.2.5） |
| 一木清单 | com.wangc.todolist | 理论全版本通杀（测试到 2.4.3） |
| 一言 | com.jhyan.yan | 理论全版本通杀（测试到 5.1.0） |
| 一木记账 | com.wangc.bill | 适配6.5.5（测试到 6.5.5） |
| Q日记 | com.slfteam.qdiary | 适配1.7.81（测试到1.7.81 ） |

---

## 开发日志

放弃对懒人驾考的逆向研究，因本人能力有限，原代码会触发风控导致设备无法使用

---

## 使用方法

1. 编译并安装 APK（./gradlew assembleDebug 或 ./gradlew assembleRelease）。
2. 在 LSPosed 中启用模块（作用域默认是空的，模块不会强勾任何应用）。
3. 打开本模块的 App，在「应用」页勾选需要解锁的应用；也可以直接在 LSPosed 管理页面里勾。
4. 重启被勾选的目标应用即可生效。

---

## 注意事项

- 虽然理论支持全版本，但建议参考测试信息的版本进行验证。
- 本项目仅用于学习和研究目的，请勿用于非法用途。
- 日志通过 Logcat 与 LSPosed 日志输出，tag 仍为 kong，便于调试和查看 hook 结果。

---

## 测试信息

| 应用名称 | 测试版本 |
|----------|----------|
| 体重日记 | <= 3.5.3 |
| 番茄轻断食 | <= 3.4.7 |
| 番茄闪轻 | <= 2.1.7 |
| 一念 | ==2.4.0 |
| 背书匠 | <=2.2.4 |
| 一木清单 | <=2.4.3 |
| 一言 | ==5.1.0 |
| 一木记账 | ==6.5.5 |
| Q日记 | ==1.7.81 |

---

## 免责申明

本项目仅用于**学习、研究和安全测试目的**，作者不对任何因使用本项目产生的直接或间接损失负责。  
使用本项目前，请确保遵守相关法律法规及应用的使用条款，不得用于未经授权的破解或侵犯他人权益行为。  

---

## 联系与贡献

欢迎 Issues 和 PR，提交新的 AppHooker 或优化现有逻辑。