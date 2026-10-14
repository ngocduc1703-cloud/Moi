Unicode true
!include "MUI2.nsh"
!include "x64.nsh"
!define APPNAME "Đại Lộ Zombie"
Name "${APPNAME}"
OutFile "../dist/DaiLoZombie-Setup.exe"
InstallDir "$LOCALAPPDATA\DaiLoZombie"
InstallDirRegKey HKCU "Software\ZombieRush\DaiLoZombie" "InstallDir"
RequestExecutionLevel user
SetCompressor /SOLID lzma
!define MUI_ABORTWARNING
!define MUI_FINISHPAGE_RUN "$INSTDIR\DaiLoZombie.exe"
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
  File "../dist/windows/DaiLoZombie.exe"
  File "../README.md"
  File "../LICENSE"
  File "../docs/THIRD_PARTY.md"
  SetOutPath "$INSTDIR\launch4j-license"
  File /r "../dist/windows/launch4j-license/*.*"
  SetOutPath "$INSTDIR\runtime"
  File /r "../dist/windows/runtime/*.*"
  SetOutPath "$INSTDIR"
  WriteUninstaller "$INSTDIR\Uninstall.exe"
  CreateShortcut "$DESKTOP\Dai Lo Zombie.lnk" "$INSTDIR\DaiLoZombie.exe"
  CreateShortcut "$SMPROGRAMS\Dai Lo Zombie.lnk" "$INSTDIR\DaiLoZombie.exe"
  WriteRegStr HKCU "Software\ZombieRush\DaiLoZombie" "InstallDir" "$INSTDIR"
  WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\DaiLoZombie" "DisplayName" "${APPNAME}"
  WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\DaiLoZombie" "DisplayVersion" "1.0.0"
  WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\DaiLoZombie" "Publisher" "ZombieRush"
  WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\DaiLoZombie" "UninstallString" '"$INSTDIR\Uninstall.exe"'
  WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\DaiLoZombie" "DisplayIcon" "$INSTDIR\DaiLoZombie.exe"
  WriteRegDWORD HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\DaiLoZombie" "NoModify" 1
  WriteRegDWORD HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\DaiLoZombie" "NoRepair" 1
SectionEnd
Section "Uninstall"
  SetShellVarContext current
  Delete "$DESKTOP\Dai Lo Zombie.lnk"
  Delete "$SMPROGRAMS\Dai Lo Zombie.lnk"
  Delete "$INSTDIR\DaiLoZombie.exe"
  Delete "$INSTDIR\README.md"
  Delete "$INSTDIR\LICENSE"
  Delete "$INSTDIR\THIRD_PARTY.md"
  RMDir /r "$INSTDIR\runtime"
  RMDir /r "$INSTDIR\launch4j-license"
  Delete "$INSTDIR\Uninstall.exe"
  RMDir "$INSTDIR"
  DeleteRegKey HKCU "Software\ZombieRush\DaiLoZombie"
  DeleteRegKey HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\DaiLoZombie"
SectionEnd
