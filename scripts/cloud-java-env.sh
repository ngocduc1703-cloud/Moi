#!/usr/bin/env bash
# Source this file. Proxy names and certificates are read from the current machine.
set -euo pipefail
TOOLS_DIR="${TOOLS_DIR:-/workspace/.tile-tools}"
CACHE_DIR="${CACHE_DIR:-/workspace/.tile-cache}"
export JAVA_HOME="$TOOLS_DIR/jdk" ANDROID_HOME="$TOOLS_DIR/android"
export PATH="$JAVA_HOME/bin:$TOOLS_DIR/maven/bin:$PATH"
export TOOLS_DIR CACHE_DIR
if [ -n "${HTTPS_PROXY:-}" ]; then
  python3 - <<'PY'
import os, urllib.parse, xml.etree.ElementTree as E
u=urllib.parse.urlsplit(os.environ['HTTPS_PROXY'])
root=E.Element('settings', xmlns='http://maven.apache.org/SETTINGS/1.0.0')
proxy=E.SubElement(E.SubElement(root,'proxies'),'proxy')
for k,v in {'id':'cloud-proxy','active':'true','protocol':'http','host':u.hostname,'port':str(u.port or 80)}.items(): E.SubElement(proxy,k).text=v
for key,value in [('username',u.username),('password',u.password)]:
 if value: E.SubElement(proxy,key).text=urllib.parse.unquote(value)
file=os.path.join(os.environ['TOOLS_DIR'],'maven-settings.xml')
E.ElementTree(root).write(file,encoding='utf-8',xml_declaration=True)
os.chmod(file,0o600)
PY
  MAVEN_SETTINGS=(-s "$TOOLS_DIR/maven-settings.xml")
else MAVEN_SETTINGS=(); fi
if [ -n "${CODEX_PROXY_CERT:-}" ] && [ -f "$CODEX_PROXY_CERT" ]; then
  if [ ! -f "$TOOLS_DIR/cloud-cacerts" ]; then
    cp "$JAVA_HOME/lib/security/cacerts" "$TOOLS_DIR/cloud-cacerts"
    keytool -importcert -noprompt -alias cloud-egress -file "$CODEX_PROXY_CERT" -keystore "$TOOLS_DIR/cloud-cacerts" -storepass changeit
  fi
  export MAVEN_OPTS="${MAVEN_OPTS:-} -Djavax.net.ssl.trustStore=$TOOLS_DIR/cloud-cacerts"
fi
