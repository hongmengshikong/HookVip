# LSPosed Hook 模块

本项目是一个基于 LSPosed 的 Android 应用 Hook 工具，用于修改指定 App 内部方法返回值，实现特定功能。

---

## 支持的应用

| 应用名称 | 包名 | 支持版本 |
|----------|------|----------|
| 体重日记 | com.kproduce.weight | 理论全版本通杀（测试到 3.0.9） |
| 番茄轻断食 | com.swhh.fasting.tomato | 理论全版本通杀（测试到 3.4.7） |

---

## 功能说明

### 体重日记（com.kproduce.weight）

- Hook 用户设置类 UserSettingsKV，强制用户为永久 VIP。
- Hook 写入方法 UserSettingsKV.n(int)，阻止修改 VIP 权限。
- 使用 DexKit 扫描方法，通过字段名匹配 Hook 成功率高。
- 支持直接修改返回值，保证 VIP 永久生效。

### 番茄轻断食（com.swhh.fasting.tomato）

- 自动获取目标应用的真实 `ClassLoader`，解决加固/StubApplication问题。
- 支持延迟 hook，确保在 `attachBaseContext` 执行后才注册 hook。
- 独立封装 AppHooker，每个 App 的 hook 逻辑可单独维护。
- 日志清晰，可查看原始返回值和修改后的返回值。

---

## 使用方法

1. 将模块编译为 Xposed 模块 APK 并安装到支持 Xposed 的设备/模拟器。  
2. 在 Xposed Installer 中激活模块，并重启应用。  

---

## 注意事项

- 虽然理论支持全版本，但建议参考测试信息的版本进行验证。
- 本项目仅用于学习和研究目的，请勿用于非法用途。
- 日志通过 `Logcat` 打印，便于调试和查看 hook 结果。

---

## 测试信息

| 应用名称 | 测试版本 |
|----------|----------|
| 体重日记 | <= 3.0.9 |
| 番茄轻断食 | <= 3.4.7 |

---

## 免责申明

本项目仅用于**学习、研究和安全测试目的**，作者不对任何因使用本项目产生的直接或间接损失负责。  
使用本项目前，请确保遵守相关法律法规及应用的使用条款，不得用于未经授权的破解或侵犯他人权益行为。  

---

## 联系与贡献

欢迎 Issues 和 PR，提交新的 AppHooker 或优化现有逻辑。
