$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path $PSScriptRoot -Parent
if (!$env:JAVA_HOME -or !$env:ANDROID_HOME) { throw 'Set JAVA_HOME (JDK 17+) and ANDROID_HOME (Android SDK) first.' }
& "$env:JAVA_HOME\bin\java.exe" "$PSScriptRoot\AndroidBuild.java" $projectRoot $env:ANDROID_HOME
if ($LASTEXITCODE -ne 0) { throw 'APK build failed.' }
