# Vava8 发吧 · Android App

面向 [www.vava8.com](https://www.vava8.com/index.php) 的原生 Android 客户端。

仓库：https://github.com/zephyrkeaton/vava8

## 下载安装

最新安装包在 [GitHub Releases](https://github.com/zephyrkeaton/vava8/releases) 提供：

- **当前版本 1.0.2**：[Vava8-1.0.2.apk](https://github.com/zephyrkeaton/vava8/releases/download/v1.0.2/Vava8-1.0.2.apk)
- 发布页：https://github.com/zephyrkeaton/vava8/releases/tag/v1.0.2

下载后在手机上允许「未知来源」安装即可。

## 功能覆盖

| 网站能力 | App 实现 |
|---------|---------|
| 首页信息流 / 资讯 / 最热 / 热评 / 原创 / 关注 | 首页顶部分类 Tab + 无限滚动 |
| 频道列表与频道浏览 | 发现页频道宫格 + 全部频道 |
| 搜索 | 独立搜索页（解析站点搜索结果） |
| 帖子详情 | 原生壳 + WebView 渲染正文 |
| 评论 / 点赞 / 收藏 / 关注 / 分享 | 详情页底栏操作；留言框避开键盘遮挡 |
| 登录 / 注册 / 退出 | 「我的」入口；Cookie + 会话持久化；可选保存用户名密码并自动续期 |
| 个人中心 | 「我的」内：我的主贴 / 回复 / 回复我的 / 提醒 / 收藏 / 私信（WebView）；本地浏览历史 |
| 发帖 | 底导「发帖」页 |
| 关于我们 / 隐私 / 规则 / 联系 | 「我的」内嵌 WebView |
| 无图模式（省流量） | 「我的」开关 / 首页图标快捷切换，列表与正文均不加载图片 |
| 主题：浅色 / 深色 / 随系统 | 「我的」与文章阅读设置，偏好持久化 |
| 底栏避让系统导航 | NavigationBar 应用系统 navigationBars insets |
| 详情页右滑返回 | 任意位置向右滑动回到上一页（发现页会回到原先标题位置） |
| 底栏 Tab 滑动切换 | 首页↔发现↔发帖↔我的：跟手滑动 + 滑入滑出动画 |
| 发帖草稿 | 未发布内容自动保存，退出 App 后仍可恢复，发布或清空后删除 |
| 列表返回顶部 | 首页 / 发现页右侧浮动按钮，可上下拖动；位置跨页面与杀进程保留 |

## 交互要点

- 底导四栏：首页 / 发现 / 发帖 / 我的；相邻页左右滑跟手切换，并带滑入滑出动画
- 首页：品牌标题 + 收藏快捷入口 + 上滑收起顶栏 + 圆角搜索条 + 分类 Chip
- 信息流：左文右图紧凑列表；右侧浮动按钮可上下拖动且记住位置，一键回顶
- 详情：任意位置右滑返回列表；底栏互动；留言框随键盘抬起；更多菜单以半弹窗调节字号与主题

## 技术栈

- Kotlin + Jetpack Compose + Material 3
- Navigation Compose / ViewModel / OkHttp / Kotlinx Serialization / Coil / DataStore

数据直接请求站点公开接口，例如：

- `index.php?app=index&act=api_list`
- `index.php?app=index&act=api_view`
- `index.php?app=index&act=api_comments`
- `index.php?app=index&act=api_toggle_like`
- `index.php?app=users&act=do_login`

## 构建运行

1. 安装 [Android Studio](https://developer.android.com/studio)（Ladybug / Koala 及以上）
2. 用 Android Studio 打开本目录
3. 连接真机或启动模拟器，运行 `app` 配置

命令行（需本机已配置 Android SDK，或使用下方脚本自动安装）：

```bash
chmod +x gradlew scripts/build-apk.sh
./scripts/build-apk.sh debug
# APK: app/build/outputs/apk/debug/Vava8-<version>.apk
# 同时复制到 dist/ 与 /opt/cursor/artifacts/
```

## 目录结构

```
app/src/main/java/com/vava8/app/
  data/          # API、Cookie、Repository、模型
  navigation/    # 底导与路由
  ui/            # 首页 / 发现 / 发帖 / 我的 / 详情 / 搜索 / 登录
  theme/         # 主题与阅读偏好
```

## 说明

- 登录态通过 CookieJar 持久化；发帖/评论/收藏等需登录。
- 搜索页解析 HTML 结果页（站点未提供独立 JSON 搜索接口）。
- 本仓库不含广告 SDK；内容与账号体系仍归属 vava8.com。
