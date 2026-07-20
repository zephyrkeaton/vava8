#!/usr/bin/env bash
# 将已准备好的 1.0.0 首版推送到 https://github.com/zephyrkeaton/vava8
# 并创建唯一 Release（仅 Vava8-1.0.0.apk）
#
# 前提：当前凭据对 zephyrkeaton/vava8 有 push / release 权限
# （Cursor GitHub App 需勾选该仓库，或使用 zephyrkeaton 账号的 PAT / gh auth login）
#
# 用法（在本仓库根目录）：
#   ./scripts/finish-zephyr-migration.sh
# 或指定已检出的首版目录：
#   ./scripts/finish-zephyr-migration.sh /path/to/checkout
set -euo pipefail

REMOTE_URL="${VAVA8_REMOTE:-https://github.com/zephyrkeaton/vava8.git}"
BRANCH="${VAVA8_BRANCH:-main}"
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
SRC="${1:-$ROOT}"

if [ ! -d "$SRC/.git" ]; then
  echo "找不到 git 仓库: $SRC"
  exit 1
fi

cd "$SRC"

# 确保远程指向目标仓库（不改动原仓库 main）
if git remote get-url origin >/dev/null 2>&1; then
  git remote set-url origin "$REMOTE_URL"
else
  git remote add origin "$REMOTE_URL"
fi

echo "==> push $SRC -> $REMOTE_URL ($BRANCH)"
git push -u origin "HEAD:$BRANCH"

APK=""
for c in \
  "$SRC/dist/Vava8-1.0.0.apk" \
  "/opt/cursor/artifacts/Vava8-1.0.0.apk"
do
  if [ -f "$c" ]; then APK="$c"; break; fi
done
if [ -z "$APK" ]; then
  echo "未找到 Vava8-1.0.0.apk，正在构建…"
  ./scripts/build-apk.sh debug
  APK="$SRC/dist/Vava8-1.0.0.apk"
fi

echo "==> create release v1.0.0 with $APK"
gh release delete v1.0.0 --repo zephyrkeaton/vava8 --yes 2>/dev/null || true
gh release create v1.0.0 "$APK" \
  --repo zephyrkeaton/vava8 \
  --title "Vava8 1.0.0" \
  --notes "$(cat <<'NOTES'
## Vava8 1.0.0

首个公开发布版本。

面向 www.vava8.com 的 Android 客户端：首页信息流、发现频道、发帖、个人中心、详情互动、浏览历史等。

安装包：`Vava8-1.0.0.apk`
NOTES
)"

echo "Done: https://github.com/zephyrkeaton/vava8/releases/tag/v1.0.0"
