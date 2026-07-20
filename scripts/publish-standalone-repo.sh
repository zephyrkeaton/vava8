#!/usr/bin/env bash
# 将当前仓库（首版 1.0.0 树）推送到 zephyrkeaton/vava8
# 前提：当前凭据对该仓库有 push 权限
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
REMOTE_URL="${VAVA8_REMOTE:-https://github.com/zephyrkeaton/vava8.git}"
BRANCH="${VAVA8_BRANCH:-main}"

cd "$ROOT"
echo "Pushing HEAD -> $REMOTE_URL ($BRANCH)"
git push -u "$REMOTE_URL" "HEAD:$BRANCH"
echo "Done. Open $REMOTE_URL"
