# 比价助手 Android

一款完全离线运行的 Android 商品单位价格比较器。没有账号、网络请求、服务器、数据库或历史记录。

## 功能

- 同时比较 2～5 个商品
- 重量：克、千克
- 容量：毫升、升
- 数量：个、包、盒（数量类要求单位一致）
- 支持组合装：组合数量 × 单件规格
- 支持无优惠、立减、折扣、直接实付金额
- 按单位价格排序
- 以最优商品的总规格为基准，计算相对第二名的等量节省金额
- 修改输入后旧结果立即失效

## 架构

- UI：原生 Android Java，单 Activity、程序化布局
- 业务逻辑：`domain/PriceCalculator.java`，与 Android UI 解耦
- 数据：仅保存在当前页面内存中；进程退出即消失
- 后端：无
- 外部依赖：无
- 最低系统：Android 7.0（API 24）

## 用 Android Studio 构建 APK

1. 安装最新版 Android Studio，并安装 Android SDK 35。
2. 在 Android Studio 选择 **Open**，打开本项目文件夹。
3. 等待 Gradle 同步完成。
4. 选择菜单 **Build → Build Bundle(s) / APK(s) → Build APK(s)**。
5. 调试 APK 位于 `app/build/outputs/apk/debug/app-debug.apk`。

也可以在项目目录执行：

```bash
./gradlew assembleDebug
```

Windows 使用：

```bat
gradlew.bat assembleDebug
```

## 安装与启动

将 `app-debug.apk` 发送到 Android 手机，点击文件安装。首次安装时，系统可能要求允许当前浏览器或文件管理器“安装未知应用”。安装完成后，在桌面点击绿色图标“比价助手”。

如已配置 ADB，也可以执行：

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.example.pricecompare/.MainActivity
```

## 核心逻辑测试

测试入口：`app/src/test/java/com/example/pricecompare/domain/PriceCalculatorTest.java`。它覆盖重量单位换算、折扣、组合装、混合类型拦截和非法折扣拦截。
