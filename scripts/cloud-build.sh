#!/usr/bin/env bash
# Build with the toolchain prepared in this cloud workspace, retaining proxy/CA verification.
set -euo pipefail
cd "$(dirname "$0")/.."
export JAVA_HOME=/workspace/tooling/jdk
export ANDROID_HOME=/workspace/tooling/android-sdk
if [[ ! -x "$JAVA_HOME/bin/javac" || ! -d "$ANDROID_HOME/platforms/android-35" ]]; then
  echo 'Cloud toolchain is missing. Use Android Studio with JDK 17+ and Android SDK 35; see README.md.' >&2
  exit 1
fi
printf 'sdk.dir=%s\n' "$ANDROID_HOME" > local.properties
java_options=(-Djavax.net.ssl.trustStore=/etc/ssl/certs/java/cacerts)
if [[ -n "${HTTPS_PROXY:-}" ]]; then
  # Only the endpoint is used; credentials are never copied or printed.
  read -r proxy_host proxy_port < <(python3 - <<'PY'
import os, urllib.parse
p = urllib.parse.urlsplit(os.environ['HTTPS_PROXY'])
print(p.hostname, p.port or 80)
PY
)
  java_options+=("-Dhttps.proxyHost=$proxy_host" "-Dhttps.proxyPort=$proxy_port" "-Dhttp.proxyHost=$proxy_host" "-Dhttp.proxyPort=$proxy_port")
fi
./gradlew --no-daemon --max-workers=2 "${java_options[@]}" "${@:-assembleDebug}"
