#!/usr/bin/env bash
set -euo pipefail
ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
: "${ANDROID_HOME:?Set ANDROID_HOME to an Android SDK with platform 35 and build-tools 35.0.0}"
: "${JAVA_HOME:?Set JAVA_HOME to a JDK 17 or newer}"
"$JAVA_HOME/bin/java" "$ROOT_DIR/scripts/AndroidBuild.java" "$ROOT_DIR" "$ANDROID_HOME"
