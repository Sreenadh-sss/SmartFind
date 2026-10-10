@echo off
echo ======================================================================
echo  Starting SmartFind Frontend Web Server (Port 5500)...
echo ======================================================================
cd /d "%~dp0SmartFindFrontend"

powershell -Command "Write-Host 'Serving frontend at http://localhost:5500... Press Ctrl+C to stop.'; Start-Process 'http://localhost:5500/index.html'; python -m http.server 5500 2>$null || npx --yes http-server -p 5500 2>$null || (Write-Host 'Opening file directly in default browser...' ; Start-Process 'index.html')"
pause
