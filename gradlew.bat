@echo off
setlocal
set "GRADLE_VERSION=8.7"
set "APP_DIR=%~dp0"
if defined GRADLE_USER_HOME (
  set "CACHE_ROOT=%GRADLE_USER_HOME%"
) else (
  set "CACHE_ROOT=%USERPROFILE%\.gradle"
)
set "CACHE_DIR=%CACHE_ROOT%\wrapper\manual-dists\gradle-%GRADLE_VERSION%"
set "GRADLE_BIN=%CACHE_DIR%\gradle-%GRADLE_VERSION%\bin\gradle.bat"
if not exist "%GRADLE_BIN%" (
  if not exist "%CACHE_DIR%" mkdir "%CACHE_DIR%"
  powershell -NoProfile -ExecutionPolicy Bypass -Command "$ProgressPreference='SilentlyContinue'; Invoke-WebRequest -Uri 'https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip' -OutFile '%CACHE_DIR%\gradle.zip'; Expand-Archive -Force '%CACHE_DIR%\gradle.zip' '%CACHE_DIR%'; Remove-Item '%CACHE_DIR%\gradle.zip'"
  if errorlevel 1 exit /b 1
)
call "%GRADLE_BIN%" -p "%APP_DIR%" %*
exit /b %ERRORLEVEL%
