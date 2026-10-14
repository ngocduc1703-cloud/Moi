Unicode true
!include "MUI2.nsh"
!include "x64.nsh"
!define APPNAME "Vườn Ghép Ba"
Name "${APPNAME}"
OutFile "../dist/VuonGhepBa-Setup.exe"
InstallDir "$LOCALAPPDATA\VuonGhepBa"
InstallDirRegKey HKCU "Software\Garden\VuonGhepBa" "InstallDir"
RequestExecutionLevel user
SetCompressor /SOLID lzma
!define MUI_ABORTWARNING
!define MUI_FINISHPAGE_RUN "$INSTDIR\VuonGhepBa.exe"
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
  File "../dist/windows/VuonGhepBa.exe"
  File "../README.md"
  File "../LICENSE"
  File "../docs/THIRD_PARTY.md"
  SetOutPath "$INSTDIR\launch4j-license"
  File /r "../dist/windows/launch4j-license/*.*"
  SetOutPath "$INSTDIR\runtime"
  File /r "../dist/windows/runtime/*.*"
  SetOutPath "$INSTDIR"
  WriteUninstaller "$INSTDIR\Uninstall.exe"
  CreateShortcut "$DESKTOP\Vuon Ghep Ba.lnk" "$INSTDIR\VuonGhepBa.exe"
  CreateShortcut "$SMPROGRAMS\Vuon Ghep Ba.lnk" "$INSTDIR\VuonGhepBa.exe"
  WriteRegStr HKCU "Software\Garden\VuonGhepBa" "InstallDir" "$INSTDIR"
  WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\VuonGhepBa" "DisplayName" "${APPNAME}"
  WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\VuonGhepBa" "DisplayVersion" "1.0.0"
  WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\VuonGhepBa" "Publisher" "Garden"
  WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\VuonGhepBa" "UninstallString" '"$INSTDIR\Uninstall.exe"'
  WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\VuonGhepBa" "DisplayIcon" "$INSTDIR\VuonGhepBa.exe"
  WriteRegDWORD HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\VuonGhepBa" "NoModify" 1
  WriteRegDWORD HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\VuonGhepBa" "NoRepair" 1
SectionEnd
Section "Uninstall"
  SetShellVarContext current
  Delete "$DESKTOP\Vuon Ghep Ba.lnk"
  Delete "$SMPROGRAMS\Vuon Ghep Ba.lnk"
  Delete "$INSTDIR\VuonGhepBa.exe"
  Delete "$INSTDIR\README.md"
  Delete "$INSTDIR\LICENSE"
  Delete "$INSTDIR\THIRD_PARTY.md"
  RMDir /r "$INSTDIR\runtime"
  RMDir /r "$INSTDIR\launch4j-license"
  Delete "$INSTDIR\Uninstall.exe"
  RMDir "$INSTDIR"
  DeleteRegKey HKCU "Software\Garden\VuonGhepBa"
  DeleteRegKey HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\VuonGhepBa"
SectionEnd
