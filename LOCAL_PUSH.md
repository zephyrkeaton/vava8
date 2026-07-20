# 本地推送到 zephyrkeaton/vava8

本压缩包为 **Vava8 1.0.0 首版**（versionCode=1），可直接初始化新仓库。

## 步骤

```bash
# 1. 解压
unzip vava8-1.0.0-init.zip -d vava8
cd vava8

# 2. 初始化 git（单 commit 首版）
git init
git checkout -b main
git add .
git commit -m "Initial release: Vava8 1.0.0"

# 3. 关联并推送到新仓库
git remote add origin https://github.com/zephyrkeaton/vava8.git
git push -u origin main --force

# 4. 创建唯一 Release（含 APK）
gh release create v1.0.0 dist/Vava8-1.0.0.apk \
  --repo zephyrkeaton/vava8 \
  --title "Vava8 1.0.0" \
  --notes "Initial release"
```

Windows PowerShell 也可直接运行 `PUSH_TO_GITHUB.ps1`（需先 `git init` 并 commit）。

## 包内说明

- 源码：完整 Android 项目
- `dist/Vava8-1.0.0.apk`：首版安装包（versionName=1.0.0, versionCode=1）
- `scripts/build-apk.sh`：本地重新构建 APK
