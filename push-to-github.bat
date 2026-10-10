@echo off
setlocal
echo ======================================================================
echo  SmartFind - Push Repository to GitHub
echo ======================================================================

set "PATH=C:\Users\K.SREENADH\mingit\cmd;%PATH%"

set /p REPO_URL="Enter your GitHub Repository URL (e.g., https://github.com/username/SmartFind.git): "

if "%REPO_URL%"=="" (
    echo [ERROR] No URL entered. Aborting.
    pause
    exit /b 1
)

echo [INFO] Configuring remote origin...
git remote add origin %REPO_URL% 2>nul || git remote set-url origin %REPO_URL%

echo [INFO] Pushing main branch to GitHub...
git push -u origin main

if %errorlevel% equ 0 (
    echo.
    echo [SUCCESS] Your SmartFind project is now live on GitHub!
) else (
    echo.
    echo [NOTE] If prompted for authentication, log in using your GitHub credentials
    echo or a Personal Access Token (PAT).
)

pause
