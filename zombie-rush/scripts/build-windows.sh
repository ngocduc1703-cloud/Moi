#!/usr/bin/env bash
set -euo pipefail
ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"
source "$ROOT_DIR/scripts/cloud-java-env.sh"
mkdir -p dist/windows
mvn "${MAVEN_SETTINGS[@]}" -Dmaven.repo.local="$CACHE_DIR/m2" -B package -Pwindows
JRE_NAME=OpenJDK21U-jre_x64_windows_hotspot_21.0.12.1_1.zip
JRE_URL="https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.12.1%2B1/$JRE_NAME"
if [ ! -f "$CACHE_DIR/windows-jre.sha256" ]; then curl -fsSL "$JRE_URL.sha256.txt" -o "$CACHE_DIR/windows-jre.sha256"; fi
if [ ! -f "$CACHE_DIR/windows-jre.zip" ]; then curl -fL --retry 2 "$JRE_URL" -o "$CACHE_DIR/windows-jre.zip.part"; mv "$CACHE_DIR/windows-jre.zip.part" "$CACHE_DIR/windows-jre.zip"; fi
printf '%s  %s\n' "$(awk '{print $1}' "$CACHE_DIR/windows-jre.sha256")" "$CACHE_DIR/windows-jre.zip" | sha256sum -c -
if [ ! -f dist/windows/runtime/bin/javaw.exe ]; then
  mkdir -p .build/windows-runtime
  unzip -q -o "$CACHE_DIR/windows-jre.zip" -d .build/windows-runtime
  mv .build/windows-runtime/jdk-21.0.12.1+1-jre dist/windows/runtime
fi
cp README.md LICENSE docs/THIRD_PARTY.md dist/windows/
LAUNCH4J_WORK="$CACHE_DIR/m2/net/sf/launch4j/launch4j/3.50/launch4j-3.50-workdir-linux64"
mkdir -p dist/windows/launch4j-license
cp "$LAUNCH4J_WORK/head/LICENSE.txt" dist/windows/launch4j-license/HEADER-LICENSE.txt
cp "$LAUNCH4J_WORK/w32api/MinGW.LICENSE.txt" dist/windows/launch4j-license/
if [ -x "$TOOLS_DIR/native/usr/bin/makensis" ]; then
  export NSISDIR="$TOOLS_DIR/native/usr/share/nsis"
  "$TOOLS_DIR/native/usr/bin/makensis" scripts/windows-installer.nsi
elif command -v makensis >/dev/null; then makensis scripts/windows-installer.nsi
else printf 'NSIS is absent: portable ZIP only. Run scripts/setup-native-tools.sh for installer.\n'; fi
python3 - <<'PY'
from pathlib import Path
from zipfile import ZipFile, ZIP_DEFLATED
root=Path('dist/windows')
with ZipFile('dist/DaiLoZombie-Windows-Portable.zip','w',ZIP_DEFLATED,compresslevel=6) as z:
 for p in sorted(root.rglob('*')):
  if p.is_file(): z.write(p,Path('DaiLoZombie')/p.relative_to(root))
PY
printf 'Windows files written to dist/.\n'
