$javaPath = "C:\Program Files\JetBrains\PyCharm 2025.2.1.1\jbr"
$env:JAVA_HOME = $javaPath
$env:PATH = "$javaPath\bin;$env:PATH"

Write-Host "Java Home set to: $env:JAVA_HOME"
Write-Host "Cleaning project..."
.\gradlew.bat clean
if ($LASTEXITCODE -ne 0) {
    Write-Error "Clean failed."
    exit $LASTEXITCODE
}

Write-Host "Running project..."
.\gradlew.bat run
