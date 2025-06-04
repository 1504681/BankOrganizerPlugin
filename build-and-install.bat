@echo off
echo Building Debug Enemy List Plugin...
echo.

gradlew clean build

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Build failed!
    pause
    exit /b 1
)

echo.
echo Installing plugin...
set DEST=C:\Users\endot\.runelite\plugins
if not exist "%DEST%" mkdir "%DEST%"

copy /Y "build\libs\DebugPlugin-1.0.0.jar" "%DEST%\"

echo.
echo Plugin installed successfully!
echo Restart RuneLite and look for "Debug Enemy List" in the plugin list.
pause