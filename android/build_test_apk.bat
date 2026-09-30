@echo off
setlocal

echo ==========================================
echo        GOVIND - TEST APK BUILDER
echo ==========================================

cd /d "%~dp0"

echo.
echo [1/3] Cleaning previous debug build...
call gradlew.bat clean

if errorlevel 1 (
    echo.
    echo BUILD FAILED during clean.
    pause
    exit /b 1
)

echo.
echo [2/3] Building Debug APK...
call gradlew.bat assembleDebug

if errorlevel 1 (
    echo.
    echo BUILD FAILED.
    pause
    exit /b 1
)

echo.
echo [3/3] Locating APK...

set "APK_PATH="

for /r "app\build\outputs\apk" %%F in (*.apk) do (
    if /i "%%~nF"=="app-debug" set "APK_PATH=%%F"
)

if not defined APK_PATH (
    echo.
    echo APK was not found.
    pause
    exit /b 1
)

echo.
echo ==========================================
echo BUILD SUCCESSFUL
echo ==========================================
echo.
echo APK:
echo %APK_PATH%
echo.

echo Copying APK to project root...
copy /Y "%APK_PATH%" "%CD%\Govind-Test.apk" >nul

if errorlevel 1 (
    echo Failed to copy APK.
    pause
    exit /b 1
)

echo.
echo Created:
echo %CD%\Govind-Test.apk
echo.

echo Installing APK on connected Android device/emulator...
adb install -r "%CD%\Govind-Test.apk"

if errorlevel 1 (
    echo.
    echo APK built successfully, but installation failed.
    echo Make sure the emulator is running and ADB is available.
    pause
    exit /b 1
)

echo.
echo ==========================================
echo GOVIND TEST APK INSTALLED SUCCESSFULLY
echo ==========================================
echo.

pause