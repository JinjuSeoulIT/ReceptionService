$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $PSScriptRoot
$port = 8283

$listener = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue |
    Select-Object -First 1

if ($listener) {
    $owner = Get-Process -Id $listener.OwningProcess -ErrorAction Stop
    if ($owner.ProcessName -ne "java") {
        throw "Port $port is already in use by non-Java process '$($owner.ProcessName)' (PID $($owner.Id)). Stop it manually before starting reception."
    }

    Write-Host "Stopping existing Java process on port $port (PID $($owner.Id))..."
    Stop-Process -Id $owner.Id -Force
    Start-Sleep -Seconds 1
}

Write-Host "Starting reception backend on port $port..."
& "$projectRoot\gradlew.bat" bootRun --args="--server.port=$port"
