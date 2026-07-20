# 推送到 GitHub

目标仓库：https://github.com/zephyrkeaton/vava8

本仓库以 **1.0.0** 作为首个公开发布版本。

## 方式一：一键脚本（推荐）

在有 `zephyrkeaton/vava8` 写权限的机器上：

```bash
# 从原仓库导出分支检出首版树
git clone --branch export/zephyr-v1.0.0 --single-branch \
  https://github.com/jeunix2000-svg/vava8.git /tmp/vava8-zephyr-migrate
cd /tmp/vava8-zephyr-migrate

# 构建 APK（可选；也可用已有 dist/Vava8-1.0.0.apk）
./scripts/build-apk.sh debug

# 推送 main 并创建唯一 Release
./scripts/finish-zephyr-migration.sh
```

或使用产物目录：`/opt/cursor/artifacts/finish-zephyr-migration.sh`。

## 方式二：PowerShell

```powershell
cd C:\tmp\1\vava8
.\PUSH_TO_GITHUB.ps1
```

## 方式三：手动

```bash
git clone --branch export/zephyr-v1.0.0 --single-branch \
  https://github.com/jeunix2000-svg/vava8.git vava8
cd vava8
git remote set-url origin https://github.com/zephyrkeaton/vava8.git
git push -u origin HEAD:main
./scripts/build-apk.sh debug
gh release create v1.0.0 dist/Vava8-1.0.0.apk \
  --repo zephyrkeaton/vava8 \
  --title "Vava8 1.0.0" \
  --notes "Initial release"
```
