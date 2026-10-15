Unicode true
!include "MUI2.nsh"
!include "x64.nsh"
!define APPNAME "Qua Sông IQ"
Name "${APPNAME}"
OutFile "../dist/QuaSongIQ-Setup.exe"
InstallDir "$LOCALAPPDATA\QuaSongIQ"
InstallDirRegKey HKCU "Software\RiverIQ\QuaSongIQ" "InstallDir"
RequestExecutionLevel user
SetCompressor /SOLID lzma
!define MUI_ABORTWARNING
!define MUI_FINISHPAGE_RUN "$INSTDIR\QuaSongIQ.exe"
!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_DIRECTORY
!insertmacro MUI_PAGE_INSTFILES
!insertmacro MUI_PAGE_FINISH
!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES
!insertmacro MUI_LANGUAGE "Vietnamese"
Function .onInit
  ${IfNot} ${RunningX64}
    MessageBox MB_OK "Ứng dụng cần Windows 10/11 64-bit."
    Abort
  ${EndIf}
FunctionEnd
Section "Game" SEC01
  SetShellVarContext current
  SetOutPath "$INSTDIR"
  File "../dist/windows/QuaSongIQ.exe"
  File "../README.md"
  File "../LICENSE"
  SetOutPath "$INSTDIR\launch4j-license"
  File /r "../dist/windows/launch4j-license/*.*"
  SetOutPath "$INSTDIR\runtime"
  File /r "../dist/windows/runtime/*.*"
  SetOutPath "$INSTDIR"
  WriteUninstaller "$INSTDIR\Uninstall.exe"
  CreateShortcut "$DESKTOP\Qua Song IQ.lnk" "$INSTDIR\QuaSongIQ.exe"
  CreateShortcut "$SMPROGRAMS\Qua Song IQ.lnk" "$INSTDIR\QuaSongIQ.exe"
  WriteRegStr HKCU "Software\RiverIQ\QuaSongIQ" "InstallDir" "$INSTDIR"
  WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\QuaSongIQ" "DisplayName" "${APPNAME}"
  WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\QuaSongIQ" "DisplayVersion" "1.0.0"
  WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\QuaSongIQ" "UninstallString" '"$INSTDIR\Uninstall.exe"'
  WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\QuaSongIQ" "DisplayIcon" "$INSTDIR\QuaSongIQ.exe"
SectionEnd
Section "Uninstall"
  SetShellVarContext current
  Delete "$DESKTOP\Qua Song IQ.lnk"
  Delete "$SMPROGRAMS\Qua Song IQ.lnk"
  RMDir /r "$INSTDIR"
  DeleteRegKey HKCU "Software\RiverIQ\QuaSongIQ"
  DeleteRegKey HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\QuaSongIQ"
SectionEnd
