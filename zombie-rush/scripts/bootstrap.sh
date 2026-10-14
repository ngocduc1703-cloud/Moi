#!/usr/bin/env bash
set -euo pipefail
TOOLS_DIR="${TOOLS_DIR:-/workspace/.tile-tools}"
CACHE_DIR="${CACHE_DIR:-/workspace/.tile-cache}"
mkdir -p "$TOOLS_DIR" "$CACHE_DIR"
verified_download() {
  local url="$1" destination="$2" algorithm="$3" expected="$4"
  if [ ! -f "$destination" ]; then curl -fL --retry 2 "$url" -o "$destination.part"; mv "$destination.part" "$destination"; fi
  printf '%s  %s\n' "$expected" "$destination" | "${algorithm}sum" -c -
}
TEMURIN_VERSION="21.0.12.1_1"
TEMURIN_TAG="jdk-21.0.12.1%2B1"
JDK_FILE="OpenJDK21U-jdk_x64_linux_hotspot_${TEMURIN_VERSION}.tar.gz"
JDK_URL="https://github.com/adoptium/temurin21-binaries/releases/download/$TEMURIN_TAG/$JDK_FILE"
if [ ! -x "$TOOLS_DIR/jdk/bin/javac" ]; then
  curl -fsSL "$JDK_URL.sha256.txt" -o "$CACHE_DIR/jdk.sha256"
  verified_download "$JDK_URL" "$CACHE_DIR/$JDK_FILE" sha256 "$(awk '{print $1}' "$CACHE_DIR/jdk.sha256")"
  mkdir -p "$TOOLS_DIR/jdk"; tar -xzf "$CACHE_DIR/$JDK_FILE" -C "$TOOLS_DIR/jdk" --strip-components=1
fi
MAVEN_URL="https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.9/apache-maven-3.9.9-bin.tar.gz"
if [ ! -x "$TOOLS_DIR/maven/bin/mvn" ]; then
  curl -fsSL "$MAVEN_URL.sha512" -o "$CACHE_DIR/maven.sha512"
  verified_download "$MAVEN_URL" "$CACHE_DIR/maven.tar.gz" sha512 "$(awk '{print $1}' "$CACHE_DIR/maven.sha512")"
  mkdir -p "$TOOLS_DIR/maven"; tar -xzf "$CACHE_DIR/maven.tar.gz" -C "$TOOLS_DIR/maven" --strip-components=1
fi
export JAVA_HOME="$TOOLS_DIR/jdk"
export PATH="$JAVA_HOME/bin:$TOOLS_DIR/maven/bin:$PATH"
mkdir -p "$TOOLS_DIR/android/platforms" "$TOOLS_DIR/android/build-tools"
if [ ! -f "$TOOLS_DIR/android/platforms/android-35/android.jar" ]; then
  verified_download https://dl.google.com/android/repository/platform-35_r02.zip "$CACHE_DIR/platform35.zip" sha1 0bb560a90a7a2cbd0dd8348224d518b638fe7949
  mkdir -p "$CACHE_DIR/platform"; unzip -q -o "$CACHE_DIR/platform35.zip" -d "$CACHE_DIR/platform"
  mv "$CACHE_DIR/platform/android-35" "$TOOLS_DIR/android/platforms/android-35"
fi
if [ ! -x "$TOOLS_DIR/android/build-tools/35.0.0/aapt2" ]; then
  verified_download https://dl.google.com/android/repository/build-tools_r35_linux.zip "$CACHE_DIR/buildtools35.zip" sha1 2cfaa0bbb2336e9ec18ed3ecea84fa2e2af607bc
  mkdir -p "$CACHE_DIR/buildtools"; unzip -q -o "$CACHE_DIR/buildtools35.zip" -d "$CACHE_DIR/buildtools"
  mv "$CACHE_DIR/buildtools/android-15" "$TOOLS_DIR/android/build-tools/35.0.0"
fi
export ANDROID_HOME="$TOOLS_DIR/android"
printf 'JDK, Maven and Android build tools are ready.\n'
java -version
mvn -version
