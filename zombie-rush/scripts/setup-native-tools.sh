#!/usr/bin/env bash
set -euo pipefail
TOOLS_DIR="${TOOLS_DIR:-/workspace/.tile-tools}"
CACHE_DIR="${CACHE_DIR:-/workspace/.tile-cache}"
APT_DIR="$TOOLS_DIR/apt"
mkdir -p "$APT_DIR/etc/sources.list.d" "$APT_DIR/etc/apt.conf.d" "$APT_DIR/etc/preferences.d" "$APT_DIR/lists/partial" "$APT_DIR/cache/archives/partial" "$CACHE_DIR/debs"
cat > "$APT_DIR/bootstrap.conf" <<EOF
Dir::Etc "$APT_DIR/etc";
Dir::State::lists "$APT_DIR/lists";
Dir::Cache "$APT_DIR/cache";
EOF
printf '%s\n' 'deb [signed-by=/usr/share/keyrings/debian-archive-keyring.gpg] https://deb.debian.org/debian trixie main' > "$APT_DIR/etc/sources.list"
APT_FLAGS=()
if [ -n "${HTTPS_PROXY:-}" ]; then APT_FLAGS=(-o "Acquire::https::Proxy=$HTTPS_PROXY"); fi
APT_CONFIG="$APT_DIR/bootstrap.conf" /usr/bin/apt-get "${APT_FLAGS[@]}" update
(cd "$CACHE_DIR/debs"; APT_CONFIG="$APT_DIR/bootstrap.conf" /usr/bin/apt-get "${APT_FLAGS[@]}" download nsis nsis-common xvfb)
for package in "$CACHE_DIR"/debs/*.deb; do dpkg-deb -x "$package" "$TOOLS_DIR/native"; done
export NSISDIR="$TOOLS_DIR/native/usr/share/nsis"
"$TOOLS_DIR/native/usr/bin/makensis" -VERSION
printf 'NSIS and Xvfb installed under %s/native (system files unchanged).\n' "$TOOLS_DIR"
