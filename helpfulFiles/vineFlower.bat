@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

if "%~1"=="" (
    echo 用法: 把 .jar 文件拖到本脚本上，或 vineFlower.bat path\to\input.jar
    pause
    exit /b 1
)

set "INPUT_JAR=%~1"
set "VF_JAR=D:\.gradle\caches\modules-2\files-2.1\org.vineflower\vineflower\1.12.0\85570609a0a5941a7d2918b6260b209de810f66f\vineflower-1.12.0.jar"

if not exist "%VF_JAR%" (
    echo [错误] 找不到 Vineflower: %VF_JAR%
    pause
    exit /b 1
)

if not exist "%INPUT_JAR%" (
    echo [错误] 找不到文件: %INPUT_JAR%
    pause
    exit /b 1
)

:: 在同目录生成 sources.jar
for %%F in ("%INPUT_JAR%") do (
    set "DIR=%%~dpF"
    set "NAME=%%~nF"
)
set "TEMP_DIR=%DIR%%NAME%-src-tmp"
set "OUTPUT_JAR=%DIR%%NAME%-sources.jar"

echo [Vineflower] 反编译: %INPUT_JAR%
echo [Vineflower] 输出:   %OUTPUT_JAR%

:: 清理临时目录
if exist "%TEMP_DIR%" rmdir /s /q "%TEMP_DIR%"
mkdir "%TEMP_DIR%"

java -jar "%VF_JAR%" -dgs=1 -ind="    " -log=WARN "%INPUT_JAR%" "%TEMP_DIR%"
if errorlevel 1 (
    echo [错误] 反编译失败
    rmdir /s /q "%TEMP_DIR%"
    pause
    exit /b 1
)

:: 打包 sources.jar
echo [Vineflower] 打包 sources.jar ...
cd /d "%TEMP_DIR%"
jar cf "%OUTPUT_JAR%" .
cd /d "%~dp0"

:: 清理
rmdir /s /q "%TEMP_DIR%"

echo [Vineflower] 完成: %OUTPUT_JAR%
pause
