#!/usr/bin/env bash
# 构建带版本号的 APK（默认 debug）
# 用法: ./scripts/build-apk.sh [debug|release]
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

BUILD_TYPE="${1:-debug}"
case "$BUILD_TYPE" in
  debug|release) ;;
  *) echo "用法: $0 [debug|release]"; exit 1 ;;
esac

ANDROID_SDK_ROOT="${ANDROID_SDK_ROOT:-${ANDROID_HOME:-$HOME/Android/Sdk}}"
export ANDROID_SDK_ROOT
export ANDROID_HOME="$ANDROID_SDK_ROOT"

install_sdk_if_needed() {
  if [ -f "$ANDROID_SDK_ROOT/platforms/android-34/android.jar" ] && \
     [ -d "$ANDROID_SDK_ROOT/build-tools/34.0.0" ]; then
    return 0
  fi

  echo "==> 安装 Android SDK 到 $ANDROID_SDK_ROOT"
  mkdir -p "$ANDROID_SDK_ROOT/cmdline-tools"
  local zip="/tmp/android-cmdline-tools.zip"
  if [ ! -f "$zip" ]; then
    curl -fsSL -o "$zip" \
      "https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip"
  fi
  if [ ! -x "$ANDROID_SDK_ROOT/cmdline-tools/latest/bin/sdkmanager" ]; then
    rm -rf "$ANDROID_SDK_ROOT/cmdline-tools/latest"
    unzip -q -o "$zip" -d "$ANDROID_SDK_ROOT/cmdline-tools"
    # zip 解压为 cmdline-tools/cmdline-tools → 重命名为 latest
    if [ -d "$ANDROID_SDK_ROOT/cmdline-tools/cmdline-tools" ]; then
      mv "$ANDROID_SDK_ROOT/cmdline-tools/cmdline-tools" "$ANDROID_SDK_ROOT/cmdline-tools/latest"
    fi
  fi

  # 预写许可证，避免交互与 pipefail/SIGPIPE
  mkdir -p "$ANDROID_SDK_ROOT/licenses"
  printf '%s\n' "24333f8a63b6825ea9c5514fde331cd0cc823dcb" \
    > "$ANDROID_SDK_ROOT/licenses/android-sdk-license"
  printf '%s\n' "84831b9409646a918e30573bab4c9c91346d8abd" \
    > "$ANDROID_SDK_ROOT/licenses/android-sdk-preview-license"
  printf '%s\n' "d56f5187479451eabf01fb78af6dfcb131a6481e" \
    > "$ANDROID_SDK_ROOT/licenses/android-sdk-arm-dbt-license"

  "$ANDROID_SDK_ROOT/cmdline-tools/latest/bin/sdkmanager" --sdk_root="$ANDROID_SDK_ROOT" \
    "platform-tools" \
    "platforms;android-34" \
    "build-tools;34.0.0" > /tmp/sdkmanager.log 2>&1 || {
      echo "sdkmanager 失败，见 /tmp/sdkmanager.log"
      tail -80 /tmp/sdkmanager.log
      exit 1
    }
}

write_local_properties() {
  cat > "$ROOT/local.properties" <<EOF
sdk.dir=$ANDROID_SDK_ROOT
EOF
}

install_sdk_if_needed
write_local_properties

chmod +x "$ROOT/gradlew"
TASK="assembleDebug"
[ "$BUILD_TYPE" = "release" ] && TASK="assembleRelease"

echo "==> ./gradlew :app:$TASK"
./gradlew ":app:$TASK" --no-daemon

OUT_DIR="$ROOT/app/build/outputs/apk/$BUILD_TYPE"
APK="$(ls -1 "$OUT_DIR"/Vava8-*.apk 2>/dev/null | head -1 || true)"
if [ -z "$APK" ]; then
  APK="$(ls -1 "$OUT_DIR"/*.apk 2>/dev/null | head -1 || true)"
fi
if [ -z "$APK" ]; then
  echo "未找到 APK: $OUT_DIR"
  exit 1
fi

ARTIFACT_DIR="${ARTIFACT_DIR:-/opt/cursor/artifacts}"
mkdir -p "$ARTIFACT_DIR" "$ROOT/dist"
cp -f "$APK" "$ARTIFACT_DIR/"
cp -f "$APK" "$ROOT/dist/"
echo "==> APK: $APK"
echo "==> 已复制到: $ARTIFACT_DIR/$(basename "$APK")"
echo "==> 已复制到: $ROOT/dist/$(basename "$APK")"
ls -lh "$APK"
