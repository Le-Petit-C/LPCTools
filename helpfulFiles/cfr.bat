@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

if "%~1"=="" (
    echo 用法: 把 .jar 文件拖到本脚本上，或 cfr.bat path\to\input.jar
    pause
    exit /b 1
)

set "INPUT_JAR=%~1"
set "CFR_JAR=D:\.gradle\caches\modules-2\files-2.1\net.fabricmc\cfr\0.2.2\749198a61958be2cda4d8ae8db1e4b963703d4ea\cfr-0.2.2.jar"

if not exist "%CFR_JAR%" (
    echo [错误] 找不到 CFR: %CFR_JAR%
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

echo [CFR] 反编译: %INPUT_JAR%
echo [CFR] 输出:   %OUTPUT_JAR%

:: 清理临时目录
if exist "%TEMP_DIR%" rmdir /s /q "%TEMP_DIR%"
mkdir "%TEMP_DIR%"

java -jar "%CFR_JAR%" "%INPUT_JAR%" --outputdir "%TEMP_DIR%" --caseinsensitivefs true --renameillegalidents true
if errorlevel 1 (
    echo [错误] 反编译失败
    rmdir /s /q "%TEMP_DIR%"
    pause
    exit /b 1
)

:: 打包 sources.jar
echo [CFR] 打包 sources.jar ...
cd /d "%TEMP_DIR%"
jar cf "%OUTPUT_JAR%" .
cd /d "%~dp0"

:: 清理
rmdir /s /q "%TEMP_DIR%"

echo [CFR] 完成: %OUTPUT_JAR%
pause
