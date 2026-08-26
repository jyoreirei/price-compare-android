#!/usr/bin/env sh
set -eu

GRADLE_VERSION="8.7"
APP_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
CACHE_DIR="${GRADLE_USER_HOME:-$HOME/.gradle}/wrapper/manual-dists/gradle-${GRADLE_VERSION}"
GRADLE_BIN="$CACHE_DIR/gradle-${GRADLE_VERSION}/bin/gradle"

if [ ! -x "$GRADLE_BIN" ]; then
  mkdir -p "$CACHE_DIR"
  ARCHIVE="$CACHE_DIR/gradle.zip"
  URL="https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip"
  if command -v curl >/dev/null 2>&1; then
    curl -fL "$URL" -o "$ARCHIVE"
  elif command -v wget >/dev/null 2>&1; then
    wget -O "$ARCHIVE" "$URL"
  else
    echo "需要 curl 或 wget 下载 Gradle。" >&2
    exit 1
  fi
  unzip -q -o "$ARCHIVE" -d "$CACHE_DIR"
  rm -f "$ARCHIVE"
fi

exec "$GRADLE_BIN" -p "$APP_DIR" "$@"
